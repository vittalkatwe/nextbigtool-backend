package com.nextbigtool.backend.service.tool;

import com.nextbigtool.backend.entity.tool.CommentLike;
import com.nextbigtool.backend.entity.tool.Tool;
import com.nextbigtool.backend.entity.tool.ToolComment;
import com.nextbigtool.backend.entity.user.AppUser;
import com.nextbigtool.backend.model.tool.CommentResponseDto;
import com.nextbigtool.backend.repository.CommentLikeRepository;
import com.nextbigtool.backend.repository.ToolCommentRepository;
import com.nextbigtool.backend.repository.ToolRepository;
import com.nextbigtool.backend.service.auth.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CommentService {

    @Autowired
    private ToolCommentRepository commentRepository;

    @Autowired
    private CommentLikeRepository commentLikeRepository;

    @Autowired
    private ToolRepository toolRepository;

    @Autowired
    private CurrentUserService currentUserService;

    public ResponseEntity<?> getComments(Long toolId) {
        try {
            Tool tool = toolRepository.findById(toolId)
                    .orElseThrow(() -> new RuntimeException("Tool not found"));

            AppUser currentUser = getOptionalUser();
            List<ToolComment> comments = commentRepository.findByToolAndDeletedFalseOrderByCreatedAtAsc(tool);

            List<CommentResponseDto> dtos = comments.stream()
                    .map(c -> {
                        long likes = commentLikeRepository.countByComment(c);
                        boolean liked = currentUser != null && commentLikeRepository.existsByCommentAndUser(c, currentUser);
                        return CommentResponseDto.from(c, likes, liked);
                    })
                    .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of("success", true, "data", dtos));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @Transactional
    public ResponseEntity<?> addComment(Long toolId, String content) {
        try {
            AppUser user = currentUserService.getCurrentUser();
            Tool tool = toolRepository.findById(toolId)
                    .orElseThrow(() -> new RuntimeException("Tool not found"));

            if (content == null || content.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Comment cannot be empty"));
            }

            ToolComment comment = new ToolComment();
            comment.setTool(tool);
            comment.setUser(user);
            comment.setContent(content.trim());
            ToolComment saved = commentRepository.save(comment);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("success", true, "data", CommentResponseDto.from(saved, 0, false)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @Transactional
    public ResponseEntity<?> replyToComment(Long toolId, Long parentId, String content) {
        try {
            AppUser user = currentUserService.getCurrentUser();
            Tool tool = toolRepository.findById(toolId)
                    .orElseThrow(() -> new RuntimeException("Tool not found"));
            ToolComment parent = commentRepository.findById(parentId)
                    .orElseThrow(() -> new RuntimeException("Parent comment not found"));

            if (content == null || content.isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Reply cannot be empty"));
            }

            ToolComment reply = new ToolComment();
            reply.setTool(tool);
            reply.setUser(user);
            reply.setContent(content.trim());
            reply.setParent(parent);
            ToolComment saved = commentRepository.save(reply);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("success", true, "data", CommentResponseDto.from(saved, 0, false)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @Transactional
    public ResponseEntity<?> toggleLike(Long commentId) {
        try {
            AppUser user = currentUserService.getCurrentUser();
            ToolComment comment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new RuntimeException("Comment not found"));

            Optional<CommentLike> existing = commentLikeRepository.findByCommentAndUser(comment, user);
            boolean liked;
            if (existing.isPresent()) {
                commentLikeRepository.delete(existing.get());
                liked = false;
            } else {
                CommentLike like = new CommentLike();
                like.setComment(comment);
                like.setUser(user);
                commentLikeRepository.save(like);
                liked = true;
            }

            long count = commentLikeRepository.countByComment(comment);
            return ResponseEntity.ok(Map.of("success", true, "liked", liked, "likeCount", count));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    private AppUser getOptionalUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) return null;
            return currentUserService.getCurrentUser();
        } catch (Exception e) {
            return null;
        }
    }
}
