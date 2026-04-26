package com.nextbigtool.backend.controller.tool;

import com.nextbigtool.backend.service.tool.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tools/{toolId}/analytics")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @PostMapping("/view")
    public ResponseEntity<?> recordView(@PathVariable Long toolId) {
        analyticsService.recordView(toolId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAnalytics(@PathVariable Long toolId) {
        return analyticsService.getToolAnalytics(toolId);
    }
}
