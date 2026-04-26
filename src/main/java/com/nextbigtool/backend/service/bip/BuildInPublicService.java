package com.nextbigtool.backend.service.bip;

import com.nextbigtool.backend.entity.bip.BipComment;
import com.nextbigtool.backend.entity.bip.BipCommentLike;
import com.nextbigtool.backend.entity.bip.BuildInPublicPost;
import com.nextbigtool.backend.entity.bip.PostLike;
import com.nextbigtool.backend.entity.bip.PostType;
import com.nextbigtool.backend.entity.tool.Tool;
import com.nextbigtool.backend.entity.user.AppUser;
import com.nextbigtool.backend.entity.user.PlanType;
import com.nextbigtool.backend.entity.user.Subscription;
import com.nextbigtool.backend.model.bip.BipCommentResponseDto;
import com.nextbigtool.backend.model.bip.BipPostRequestDto;
import com.nextbigtool.backend.model.bip.BipPostResponseDto;
import com.nextbigtool.backend.repository.*;
import com.nextbigtool.backend.service.auth.CurrentUserService;
import com.nextbigtool.backend.service.subscription.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BuildInPublicService {

    @Autowired private BuildInPublicPostRepository postRepository;
    @Autowired private PostLikeRepository postLikeRepository;
    @Autowired private BipCommentRepository commentRepository;
    @Autowired private BipCommentLikeRepository commentLikeRepository;
    @Autowired private ToolRepository toolRepository;
    @Autowired private CurrentUserService currentUserService;
    @Autowired private SubscriptionService subscriptionService;

    public ResponseEntity<?> getFeed(int page, int size) {
        AppUser currentUser = getOptionalUser();
        var posts = postRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page, size));
        List<BipPostResponseDto> dtos = posts.stream()
                .map(p -> {
                    long likes = postLikeRepository.countByPost(p);
                    boolean liked = currentUser != null && postLikeRepository.existsByPostAndUser(p, currentUser);
                    long commentCount = commentRepository.countByPostAndDeletedFalse(p);
                    BipPostResponseDto dto = BipPostResponseDto.from(p, likes, liked);
                    dto.setCommentCount(commentCount);
                    return dto;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(Map.of(
                "success", true,
                "data", dtos,
                "totalPages", posts.getTotalPages(),
                "totalElements", posts.getTotalElements()
        ));
    }

    public ResponseEntity<?> getPost(Long postId) {
        AppUser currentUser = getOptionalUser();
        BuildInPublicPost post = postRepository.findById(postId)
                .orElse(null);
        if (post == null) return ResponseEntity.notFound().build();

        long likes = postLikeRepository.countByPost(post);
        boolean liked = currentUser != null && postLikeRepository.existsByPostAndUser(post, currentUser);
        long commentCount = commentRepository.countByPostAndDeletedFalse(post);

        BipPostResponseDto dto = BipPostResponseDto.from(post, likes, liked);
        dto.setCommentCount(commentCount);
        return ResponseEntity.ok(Map.of("success", true, "data", dto));
    }

    @Transactional
    public ResponseEntity<?> createPost(BipPostRequestDto request) {
        try {
            AppUser user = currentUserService.getCurrentUser();
            Subscription sub = subscriptionService.getOrCreateFree(user);
            PlanType plan = sub.getEffectivePlan();

            if (plan == PlanType.FREE) {
                long total = postRepository.countByUser(user);
                if (total >= 1) return planLimitError("Free plan allows only 1 post. Upgrade to post more.");
            } else if (plan == PlanType.BASIC) {
                LocalDateTime startOfMonth = LocalDateTime.now().with(TemporalAdjusters.firstDayOfMonth()).toLocalDate().atStartOfDay();
                long thisMonth = postRepository.countByUserAndCreatedAtAfter(user, startOfMonth);
                if (thisMonth >= 5) return planLimitError("Basic plan allows 5 posts per month. Upgrade to Core for unlimited.");
            }

            if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Title is required"));
            }

            BuildInPublicPost post = new BuildInPublicPost();
            post.setUser(user);
            post.setType(request.getType());
            post.setTitle(request.getTitle().trim());
            post.setContent(request.getContent() != null ? request.getContent().trim() : "");
            post.setMetricLabel(request.getMetricLabel());
            post.setMetricValue(request.getMetricValue());

            if (request.getToolId() != null) {
                toolRepository.findById(request.getToolId()).ifPresent(post::setTool);
            }

            BuildInPublicPost saved = postRepository.save(post);
            BipPostResponseDto dto = BipPostResponseDto.from(saved, 0, false);
            dto.setCommentCount(0);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("success", true, "data", dto));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @Transactional
    public ResponseEntity<?> toggleLike(Long postId) {
        try {
            AppUser user = currentUserService.getCurrentUser();
            BuildInPublicPost post = postRepository.findById(postId)
                    .orElseThrow(() -> new RuntimeException("Post not found"));

            Optional<PostLike> existing = postLikeRepository.findByPostAndUser(post, user);
            boolean liked;
            if (existing.isPresent()) {
                postLikeRepository.delete(existing.get());
                liked = false;
            } else {
                PostLike like = new PostLike();
                like.setPost(post);
                like.setUser(user);
                postLikeRepository.save(like);
                liked = true;
            }
            return ResponseEntity.ok(Map.of("success", true, "liked", liked, "count", postLikeRepository.countByPost(post)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    public ResponseEntity<?> getMyPosts() {
        AppUser user = currentUserService.getCurrentUser();
        Subscription sub = subscriptionService.getOrCreateFree(user);
        PlanType plan = sub.getEffectivePlan();

        List<BipPostResponseDto> posts = postRepository.findByUserOrderByCreatedAtDesc(user)
                .stream().map(p -> {
                    BipPostResponseDto dto = BipPostResponseDto.from(p, postLikeRepository.countByPost(p), false);
                    dto.setCommentCount(commentRepository.countByPostAndDeletedFalse(p));
                    return dto;
                })
                .collect(Collectors.toList());

        int limit = switch (plan) {
            case FREE -> 1;
            case BASIC -> 5;
            case CORE -> Integer.MAX_VALUE;
        };

        return ResponseEntity.ok(Map.of("success", true, "data", posts, "planLimit", limit, "planType", plan));
    }

    // ── Comments ─────────────────────────────────────────────────────────────

    public ResponseEntity<?> getComments(Long postId) {
        AppUser currentUser = getOptionalUser();
        BuildInPublicPost post = postRepository.findById(postId).orElse(null);
        if (post == null) return ResponseEntity.notFound().build();

        List<BipCommentResponseDto> dtos = commentRepository
                .findByPostAndDeletedFalseOrderByCreatedAtAsc(post)
                .stream()
                .map(c -> {
                    long likes = commentLikeRepository.countByComment(c);
                    boolean liked = currentUser != null && commentLikeRepository.existsByCommentAndUser(c, currentUser);
                    return BipCommentResponseDto.from(c, likes, liked);
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(Map.of("success", true, "data", dtos));
    }

    @Transactional
    public ResponseEntity<?> addComment(Long postId, String content) {
        try {
            AppUser user = currentUserService.getCurrentUser();
            BuildInPublicPost post = postRepository.findById(postId)
                    .orElseThrow(() -> new RuntimeException("Post not found"));

            BipComment comment = new BipComment();
            comment.setPost(post);
            comment.setUser(user);
            comment.setContent(content.trim());

            BipComment saved = commentRepository.save(comment);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("success", true, "data", BipCommentResponseDto.from(saved, 0, false)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @Transactional
    public ResponseEntity<?> addReply(Long postId, Long parentId, String content) {
        try {
            AppUser user = currentUserService.getCurrentUser();
            BuildInPublicPost post = postRepository.findById(postId)
                    .orElseThrow(() -> new RuntimeException("Post not found"));
            BipComment parent = commentRepository.findById(parentId)
                    .orElseThrow(() -> new RuntimeException("Parent comment not found"));

            BipComment reply = new BipComment();
            reply.setPost(post);
            reply.setUser(user);
            reply.setContent(content.trim());
            reply.setParent(parent);

            BipComment saved = commentRepository.save(reply);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("success", true, "data", BipCommentResponseDto.from(saved, 0, false)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @Transactional
    public ResponseEntity<?> toggleCommentLike(Long commentId) {
        try {
            AppUser user = currentUserService.getCurrentUser();
            BipComment comment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new RuntimeException("Comment not found"));

            Optional<BipCommentLike> existing = commentLikeRepository.findByCommentAndUser(comment, user);
            boolean liked;
            if (existing.isPresent()) {
                commentLikeRepository.delete(existing.get());
                liked = false;
            } else {
                BipCommentLike like = new BipCommentLike();
                like.setComment(comment);
                like.setUser(user);
                commentLikeRepository.save(like);
                liked = true;
            }
            return ResponseEntity.ok(Map.of("success", true, "liked", liked, "count", commentLikeRepository.countByComment(comment)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private ResponseEntity<?> planLimitError(String msg) {
        return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                .body(Map.of("success", false, "error", msg, "limitReached", true));
    }

    private AppUser getOptionalUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) return null;
            return currentUserService.getCurrentUser();
        } catch (Exception e) { return null; }
    }
}
