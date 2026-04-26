package com.nextbigtool.backend.controller.tool;

import com.nextbigtool.backend.service.tool.UpvoteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tools/{toolId}/upvote")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class UpvoteController {

    @Autowired
    private UpvoteService upvoteService;

    @GetMapping("/count")
    public ResponseEntity<?> getCount(@PathVariable Long toolId) {
        return upvoteService.getUpvoteCount(toolId);
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> toggle(@PathVariable Long toolId) {
        return upvoteService.toggleUpvote(toolId);
    }
}
