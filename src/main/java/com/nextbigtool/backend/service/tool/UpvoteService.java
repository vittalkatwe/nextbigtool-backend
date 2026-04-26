package com.nextbigtool.backend.service.tool;

import com.nextbigtool.backend.entity.tool.Tool;
import com.nextbigtool.backend.entity.tool.ToolUpvote;
import com.nextbigtool.backend.entity.user.AppUser;
import com.nextbigtool.backend.repository.ToolRepository;
import com.nextbigtool.backend.repository.ToolUpvoteRepository;
import com.nextbigtool.backend.service.auth.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;

@Service
public class UpvoteService {

    @Autowired
    private ToolUpvoteRepository upvoteRepository;

    @Autowired
    private ToolRepository toolRepository;

    @Autowired
    private CurrentUserService currentUserService;

    public ResponseEntity<?> getUpvoteCount(Long toolId) {
        try {
            Tool tool = toolRepository.findById(toolId)
                    .orElseThrow(() -> new RuntimeException("Tool not found"));
            long count = upvoteRepository.countByTool(tool);

            AppUser user = getOptionalUser();
            boolean upvoted = user != null && upvoteRepository.existsByToolAndUser(tool, user);

            return ResponseEntity.ok(Map.of("success", true, "count", count, "upvoted", upvoted));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }

    @Transactional
    public ResponseEntity<?> toggleUpvote(Long toolId) {
        try {
            AppUser user = currentUserService.getCurrentUser();
            Tool tool = toolRepository.findById(toolId)
                    .orElseThrow(() -> new RuntimeException("Tool not found"));

            Optional<ToolUpvote> existing = upvoteRepository.findByToolAndUser(tool, user);
            boolean upvoted;
            if (existing.isPresent()) {
                upvoteRepository.delete(existing.get());
                upvoted = false;
            } else {
                ToolUpvote upvote = new ToolUpvote();
                upvote.setTool(tool);
                upvote.setUser(user);
                upvoteRepository.save(upvote);
                upvoted = true;
            }

            long count = upvoteRepository.countByTool(tool);
            return ResponseEntity.ok(Map.of("success", true, "upvoted", upvoted, "count", count));
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
