package com.nextbigtool.backend.controller.bip;

import com.nextbigtool.backend.model.bip.BipPostRequestDto;
import com.nextbigtool.backend.service.bip.BuildInPublicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
}
