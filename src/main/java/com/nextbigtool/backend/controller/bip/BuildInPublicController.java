package com.nextbigtool.backend.controller.bip;

import com.nextbigtool.backend.model.bip.BipPostRequestDto;
import com.nextbigtool.backend.service.bip.BuildInPublicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/bip")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class BuildInPublicController {

    @Autowired
    private BuildInPublicService bipService;

    @GetMapping("/feed")
    public ResponseEntity<?> getFeed(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return bipService.getFeed(page, size);
    }

    @GetMapping("/{postId}")
    public ResponseEntity<?> getPost(@PathVariable Long postId) {
        return bipService.getPost(postId);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createPost(@RequestBody BipPostRequestDto request) {
        return bipService.createPost(request);
    }

    @PostMapping("/{postId}/like")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> toggleLike(@PathVariable Long postId) {
        return bipService.toggleLike(postId);
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyPosts() {
        return bipService.getMyPosts();
    }

    // ── Comments ─────────────────────────────────────────────────────────────

    @GetMapping("/{postId}/comments")
    public ResponseEntity<?> getComments(@PathVariable Long postId) {
        return bipService.getComments(postId);
    }

    @PostMapping("/{postId}/comments")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> addComment(@PathVariable Long postId, @RequestBody Map<String, String> body) {
        String content = body.get("content");
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Content is required"));
        }
        return bipService.addComment(postId, content);
    }

    @PostMapping("/{postId}/comments/{commentId}/replies")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> addReply(@PathVariable Long postId, @PathVariable Long commentId,
                                       @RequestBody Map<String, String> body) {
        String content = body.get("content");
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "error", "Content is required"));
        }
        return bipService.addReply(postId, commentId, content);
    }

    @PostMapping("/comments/{commentId}/like")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> toggleCommentLike(@PathVariable Long commentId) {
        return bipService.toggleCommentLike(commentId);
    }
}
