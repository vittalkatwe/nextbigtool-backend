package com.nextbigtool.backend.controller.tool;

import com.nextbigtool.backend.model.tool.ToolSubmitRequestDto;
import com.nextbigtool.backend.service.tool.ToolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/tools")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class ToolController {

    @Autowired
    private ToolService toolService;

    /**
     * Submit a new tool
     * POST /api/v1/tools
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> submitTool(@RequestBody ToolSubmitRequestDto request) {
        return toolService.submitTool(request);
    }

    /**
     * Get all tools submitted by the currently logged-in user
     * GET /api/v1/tools/my
     */
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMyTools() {
        return toolService.getMyTools();
    }

    /**
     * Public listing — only approved tools
     * GET /api/v1/tools
     */
    @GetMapping
    public ResponseEntity<?> getApprovedTools() {
        return toolService.getApprovedTools();
    }

    /**
     * Featured tools (active featuredUntil > now)
     * GET /api/v1/tools/featured
     */
    @GetMapping("/featured")
    public ResponseEntity<?> getFeaturedTools() {
        return toolService.getFeaturedTools();
    }

    /**
     * Get single tool by ID
     * GET /api/v1/tools/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getToolById(@PathVariable Long id) {
        return toolService.getToolById(id);
    }
}