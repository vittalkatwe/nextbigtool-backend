package com.nextbigtool.backend.controller.subscription;

import com.nextbigtool.backend.service.subscription.SubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/subscriptions")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class SubscriptionController {

    @Autowired
    private SubscriptionService subscriptionService;

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMySubscription() {
        return subscriptionService.getMySubscription();
    }
}
