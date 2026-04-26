package com.nextbigtool.backend.controller.tool;

import com.nextbigtool.backend.model.tool.SponsorSubmitRequestDto;
import com.nextbigtool.backend.service.tool.SponsorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sponsors")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:5173"})
public class SponsorController {

    @Autowired
    private SponsorService sponsorService;

    /**
     * Submit a sponsorship/ad campaign
     * POST /api/v1/sponsors
     */
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> submitSponsor(@RequestBody SponsorSubmitRequestDto request) {
        return sponsorService.submitSponsor(request);
    }

    /**
     * Get all sponsorship requests submitted by the current user
     * GET /api/v1/sponsors/my
     */
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getMySponsorships() {
        return sponsorService.getMySponsorships();
    }
}