package com.nextbigtool.backend.controller.newsletter;

import com.nextbigtool.backend.service.newsletter.NewsletterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/newsletter")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class NewsletterController {

    @Autowired private NewsletterService newsletterService;

    @GetMapping("/stats")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getStats() {
        return newsletterService.getMyStats();
    }

    @GetMapping("/subscribers")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getSubscribers() {
        return newsletterService.getMySubscribers();
    }

    @GetMapping("/issues")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getIssues() {
        return newsletterService.getMyIssues();
    }

    @PostMapping("/issues")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> publishIssue(@RequestBody Map<String, String> body) {
        return newsletterService.publishIssue(body.get("subject"), body.get("content"));
    }

    @PostMapping("/issues/{id}/read")
    public ResponseEntity<?> trackOpen(@PathVariable Long id) {
        return newsletterService.trackOpen(id);
    }

    @PostMapping("/subscribe/{publisherId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> subscribe(@PathVariable Long publisherId) {
        return newsletterService.subscribeToUser(publisherId);
    }
}
