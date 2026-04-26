package com.nextbigtool.backend.service.tool;

import com.nextbigtool.backend.entity.tool.Sponsor;
import com.nextbigtool.backend.entity.tool.SponsorStatus;
import com.nextbigtool.backend.entity.user.AppUser;
import com.nextbigtool.backend.model.tool.SponsorResponseDto;
import com.nextbigtool.backend.model.tool.SponsorSubmitRequestDto;
import com.nextbigtool.backend.repository.SponsorRepository;
import com.nextbigtool.backend.service.auth.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SponsorService {

    @Autowired
    private SponsorRepository sponsorRepository;

    @Autowired
    private CurrentUserService currentUserService;

    /**
     * Submit a sponsorship / ad campaign
     */
    @Transactional
    public ResponseEntity<?> submitSponsor(SponsorSubmitRequestDto request) {
        try {
            AppUser user = currentUserService.getCurrentUser();

            Sponsor sponsor = new Sponsor();
            sponsor.setSubmittedBy(user);
            sponsor.setProductName(request.getProductName().trim());
            sponsor.setTagline(request.getTagline());
            sponsor.setLogoUrl(request.getLogoUrl());
            sponsor.setWebsiteUrl(request.getWebsiteUrl().trim());
            sponsor.setDuration(request.getDuration());
            sponsor.setAdditionalNotes(request.getAdditionalNotes());
            sponsor.setStatus(SponsorStatus.PENDING);

            Sponsor saved = sponsorRepository.save(sponsor);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "success", true,
                    "message", "Sponsorship request submitted successfully. Our team will contact you shortly.",
                    "data", SponsorResponseDto.from(saved)
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "error", "Failed to submit sponsorship: " + e.getMessage()
            ));
        }
    }

    /**
     * Get all sponsorship requests by current user
     */
    public ResponseEntity<?> getMySponsorships() {
        try {
            AppUser user = currentUserService.getCurrentUser();
            List<SponsorResponseDto> list = sponsorRepository.findBySubmittedBy(user)
                    .stream()
                    .map(SponsorResponseDto::from)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(Map.of("success", true, "data", list));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }
}