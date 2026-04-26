package com.nextbigtool.backend.service.tool;

import com.nextbigtool.backend.entity.tool.Tool;
import com.nextbigtool.backend.entity.tool.ToolView;
import com.nextbigtool.backend.entity.user.AppUser;
import com.nextbigtool.backend.entity.user.PlanType;
import com.nextbigtool.backend.entity.user.Subscription;
import com.nextbigtool.backend.repository.*;
import com.nextbigtool.backend.service.auth.CurrentUserService;
import com.nextbigtool.backend.service.subscription.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    @Autowired private ToolRepository toolRepository;
    @Autowired private ToolViewRepository viewRepository;
    @Autowired private ToolUpvoteRepository upvoteRepository;
    @Autowired private ToolCommentRepository commentRepository;
    @Autowired private CurrentUserService currentUserService;
    @Autowired private SubscriptionService subscriptionService;

    @Transactional
    public void recordView(Long toolId) {
        toolRepository.findById(toolId).ifPresent(tool -> {
            tool.setViewCount(tool.getViewCount() + 1);
            toolRepository.save(tool);

            AppUser user = getOptionalUser();
            if (user != null) {
                ToolView view = new ToolView();
                view.setTool(tool);
                view.setUser(user);
                viewRepository.save(view);
            }
        });
    }

    public ResponseEntity<?> getToolAnalytics(Long toolId) {
        try {
            AppUser user = currentUserService.getCurrentUser();
            Tool tool = toolRepository.findById(toolId)
                    .orElseThrow(() -> new RuntimeException("Tool not found"));

            if (!tool.getSubmittedBy().getId().equals(user.getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("success", false, "error", "Not your tool"));
            }

            Subscription sub = subscriptionService.getOrCreateFree(user);
            PlanType plan = sub.getEffectivePlan();

            long viewCount = tool.getViewCount();
            long upvoteCount = upvoteRepository.countByTool(tool);
            long commentCount = commentRepository.countByToolAndDeletedFalse(tool);

            Map<String, Object> basic = Map.of(
                    "viewCount", viewCount,
                    "upvoteCount", upvoteCount,
                    "commentCount", commentCount
            );

            if (plan == PlanType.FREE) {
                return ResponseEntity.ok(Map.of("success", true, "data", basic, "plan", "FREE"));
            }

            // Basic + Core: who upvoted
            List<Map<String, Object>> upvoters = upvoteRepository.findByTool(tool).stream()
                    .map(u -> Map.of("id", (Object) u.getUser().getId(), "email", u.getUser().getEmail()))
                    .collect(Collectors.toList());

            // Who viewed (authenticated)
            List<Map<String, Object>> viewers = viewRepository.findDistinctViewersByTool(tool).stream()
                    .map(v -> Map.of("id", (Object) v.getId(), "email", v.getEmail()))
                    .collect(Collectors.toList());

            Map<String, Object> advanced = Map.of(
                    "viewCount", viewCount,
                    "upvoteCount", upvoteCount,
                    "commentCount", commentCount,
                    "upvoters", upvoters,
                    "viewers", viewers
            );

            if (plan == PlanType.BASIC) {
                return ResponseEntity.ok(Map.of("success", true, "data", advanced, "plan", "BASIC"));
            }

            // Core: interested users (upvoters + commenters)
            List<AppUser> commenters = commentRepository.findByToolAndDeletedFalseOrderByCreatedAtAsc(tool)
                    .stream().map(c -> c.getUser()).distinct().collect(Collectors.toList());

            List<AppUser> interestedUsers = upvoteRepository.findByTool(tool).stream()
                    .map(u -> u.getUser())
                    .collect(Collectors.toCollection(java.util.ArrayList::new));

            commenters.forEach(c -> {
                if (interestedUsers.stream().noneMatch(u -> u.getId().equals(c.getId()))) {
                    interestedUsers.add(c);
                }
            });


            List<Map<String, Object>> interestedDtos = interestedUsers.stream()
                    .map(u -> Map.<String, Object>of(
                            "id", u.getId(),
                            "email", u.getEmail(),
                            "name", u.getFirstname() != null ? u.getFirstname() : ""))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of("success", true, "data",
                    Map.of("viewCount", viewCount, "upvoteCount", upvoteCount, "commentCount", commentCount,
                           "upvoters", upvoters, "viewers", viewers, "interestedUsers", interestedDtos),
                    "plan", "CORE"));

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
        } catch (Exception e) { return null; }
    }
}
