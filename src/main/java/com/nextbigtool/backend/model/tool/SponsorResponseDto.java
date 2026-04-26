package com.nextbigtool.backend.model.tool;

import com.nextbigtool.backend.entity.tool.CampaignDuration;
import com.nextbigtool.backend.entity.tool.Sponsor;
import com.nextbigtool.backend.entity.tool.SponsorStatus;

import java.time.LocalDateTime;

public class SponsorResponseDto {

    private Long id;
    private String productName;
    private String tagline;
    private String logoUrl;
    private String websiteUrl;
    private CampaignDuration duration;
    private String estimatedImpressions;
    private String displayPrice;
    private String additionalNotes;
    private SponsorStatus status;
    private LocalDateTime createdAt;
    private Long submittedById;

    public static SponsorResponseDto from(Sponsor sponsor) {
        SponsorResponseDto dto = new SponsorResponseDto();
        dto.id = sponsor.getId();
        dto.productName = sponsor.getProductName();
        dto.tagline = sponsor.getTagline();
        dto.logoUrl = sponsor.getLogoUrl();
        dto.websiteUrl = sponsor.getWebsiteUrl();
        dto.duration = sponsor.getDuration();
        dto.estimatedImpressions = sponsor.getDuration().getEstimatedImpressions();
        dto.displayPrice = sponsor.getDuration().getDisplayPrice();
        dto.additionalNotes = sponsor.getAdditionalNotes();
        dto.status = sponsor.getStatus();
        dto.createdAt = sponsor.getCreatedAt();
        dto.submittedById = sponsor.getSubmittedBy().getId();
        return dto;
    }

    // ── Getters ──────────────────────────────────────────────────────────────
    public Long getId() { return id; }
    public String getProductName() { return productName; }
    public String getTagline() { return tagline; }
    public String getLogoUrl() { return logoUrl; }
    public String getWebsiteUrl() { return websiteUrl; }
    public CampaignDuration getDuration() { return duration; }
    public String getEstimatedImpressions() { return estimatedImpressions; }
    public String getDisplayPrice() { return displayPrice; }
    public String getAdditionalNotes() { return additionalNotes; }
    public SponsorStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Long getSubmittedById() { return submittedById; }
}