package com.nextbigtool.backend.controller.tool;

import com.nextbigtool.backend.service.tool.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/tools/{toolId}/comments")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class CommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping
    public ResponseEntity<?> getComments(@PathVariable Long toolId) {
        return commentService.getComments(toolId);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> addComment(@PathVariable Long toolId, @RequestBody Map<String, String> body) {
        return commentService.addComment(toolId, body.get("content"));
    }

    @PostMapping("/{commentId}/replies")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> reply(@PathVariable Long toolId,
                                   @PathVariable Long commentId,
                                   @RequestBody Map<String, String> body) {
        return commentService.replyToComment(toolId, commentId, body.get("content"));
    }

    @PostMapping("/{commentId}/like")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> toggleLike(@PathVariable Long toolId,
                                        @PathVariable Long commentId) {
        return commentService.toggleLike(commentId);
    }
}
