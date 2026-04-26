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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
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
            double engagementRate = viewCount > 0 ? Math.round(((upvoteCount + commentCount) * 100.0 / viewCount) * 10.0) / 10.0 : 0.0;

            if (plan == PlanType.FREE) {
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("viewCount", viewCount);
                data.put("upvoteCount", upvoteCount);
                data.put("commentCount", commentCount);
                data.put("engagementRate", engagementRate);
                return ResponseEntity.ok(Map.of("success", true, "data", data, "plan", "FREE"));
            }

            // ── BASIC + CORE ──────────────────────────────────────────────────
            List<AppUser> distinctViewers = viewRepository.findDistinctViewersByTool(tool);
            long uniqueViewers = distinctViewers.size();
            double conversionRate = viewCount > 0 ? Math.round((upvoteCount * 100.0 / viewCount) * 10.0) / 10.0 : 0.0;
            long rank = toolRepository.countToolsRankedAbove(tool.getCategory(), upvoteCount) + 1;

            List<Map<String, Object>> upvoters = upvoteRepository.findByTool(tool).stream()
                    .map(u -> {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("id", u.getUser().getId());
                        m.put("email", u.getUser().getEmail());
                        m.put("name", u.getUser().getFirstname() != null ? u.getUser().getFirstname() : "");
                        return m;
                    })
                    .collect(Collectors.toList());

            List<Map<String, Object>> viewers = distinctViewers.stream()
                    .map(v -> {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("id", v.getId());
                        m.put("email", v.getEmail());
                        m.put("name", v.getFirstname() != null ? v.getFirstname() : "");
                        return m;
                    })
                    .collect(Collectors.toList());

            // Last 30 days view trend (daily buckets)
            List<Map<String, Object>> dailyViews = buildDailyViewTrend(tool, 30);

            // Last 7 days trend for quick comparison
            long viewsLast7d = dailyViews.stream().skip(Math.max(0, dailyViews.size() - 7))
                    .mapToLong(d -> ((Number) d.get("count")).longValue()).sum();
            long viewsPrev7d = dailyViews.stream()
                    .skip(Math.max(0, dailyViews.size() - 14))
                    .limit(7)
                    .mapToLong(d -> ((Number) d.get("count")).longValue()).sum();
            double viewsTrend = viewsPrev7d > 0 ? Math.round((viewsLast7d - viewsPrev7d) * 100.0 / viewsPrev7d * 10) / 10.0 : 0.0;

            if (plan == PlanType.BASIC) {
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("viewCount", viewCount);
                data.put("uniqueViewers", uniqueViewers);
                data.put("upvoteCount", upvoteCount);
                data.put("commentCount", commentCount);
                data.put("engagementRate", engagementRate);
                data.put("conversionRate", conversionRate);
                data.put("categoryRank", rank);
                data.put("viewsLast7d", viewsLast7d);
                data.put("viewsTrend", viewsTrend);
                data.put("dailyViews", dailyViews);
                data.put("upvoters", upvoters);
                data.put("viewers", viewers);
                return ResponseEntity.ok(Map.of("success", true, "data", data, "plan", "BASIC"));
            }

            // ── CORE ──────────────────────────────────────────────────────────
            List<AppUser> commenters = commentRepository.findByToolAndDeletedFalseOrderByCreatedAtAsc(tool)
                    .stream().map(c -> c.getUser()).distinct().collect(Collectors.toList());

            List<AppUser> interestedRaw = upvoteRepository.findByTool(tool).stream()
                    .map(u -> u.getUser())
                    .collect(Collectors.toCollection(ArrayList::new));
            commenters.forEach(c -> {
                if (interestedRaw.stream().noneMatch(u -> u.getId().equals(c.getId()))) {
                    interestedRaw.add(c);
                }
            });

            List<Map<String, Object>> interestedUsers = interestedRaw.stream()
                    .map(u -> {
                        Map<String, Object> m = new LinkedHashMap<>();
                        m.put("id", u.getId());
                        m.put("email", u.getEmail());
                        m.put("name", u.getFirstname() != null ? u.getFirstname() : "");
                        boolean upvoted = upvoters.stream().anyMatch(up -> up.get("id").equals(u.getId()));
                        boolean commented = commenters.stream().anyMatch(c -> c.getId().equals(u.getId()));
                        m.put("upvoted", upvoted);
                        m.put("commented", commented);
                        return m;
                    })
                    .collect(Collectors.toList());

            // Engagement score: composite (0-100)
            double engagementScore = Math.min(100.0,
                    Math.round((engagementRate * 0.4 + conversionRate * 0.4 + Math.min(20, uniqueViewers * 0.2)) * 10) / 10.0);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("viewCount", viewCount);
            data.put("uniqueViewers", uniqueViewers);
            data.put("upvoteCount", upvoteCount);
            data.put("commentCount", commentCount);
            data.put("engagementRate", engagementRate);
            data.put("conversionRate", conversionRate);
            data.put("categoryRank", rank);
            data.put("viewsLast7d", viewsLast7d);
            data.put("viewsTrend", viewsTrend);
            data.put("dailyViews", dailyViews);
            data.put("engagementScore", engagementScore);
            data.put("upvoters", upvoters);
            data.put("viewers", viewers);
            data.put("interestedUsers", interestedUsers);

            return ResponseEntity.ok(Map.of("success", true, "data", data, "plan", "CORE"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    private List<Map<String, Object>> buildDailyViewTrend(Tool tool, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        List<ToolView> views = viewRepository.findByToolSince(tool, since);

        // Build a map of date → count for last N days
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM d");
        Map<LocalDate, Long> buckets = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        for (int i = days - 1; i >= 0; i--) {
            buckets.put(today.minusDays(i), 0L);
        }
        views.forEach(v -> {
            LocalDate d = v.getViewedAt().toLocalDate();  // fix it, it should be created date not viewwed date
            buckets.merge(d, 1L, Long::sum);
        });

        return buckets.entrySet().stream().map(e -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("date", e.getKey().format(fmt));
            m.put("count", e.getValue());
            return m;
        }).collect(Collectors.toList());
    }

    private AppUser getOptionalUser() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) return null;
            return currentUserService.getCurrentUser();
        } catch (Exception e) { return null; }
    }
}
