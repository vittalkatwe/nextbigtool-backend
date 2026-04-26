package com.nextbigtool.backend.model.tool;

import com.nextbigtool.backend.entity.tool.CampaignDuration;

public class SponsorSubmitRequestDto {

    private String productName;

    private String tagline;

    private String logoUrl;

    private String websiteUrl;

    private CampaignDuration duration;

    private String additionalNotes;

    // ── Getters & Setters ────────────────────────────────────────────────────
    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getTagline() { return tagline; }
    public void setTagline(String tagline) { this.tagline = tagline; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    public String getWebsiteUrl() { return websiteUrl; }
    public void setWebsiteUrl(String websiteUrl) { this.websiteUrl = websiteUrl; }

    public CampaignDuration getDuration() { return duration; }
    public void setDuration(CampaignDuration duration) { this.duration = duration; }

    public String getAdditionalNotes() { return additionalNotes; }
    public void setAdditionalNotes(String additionalNotes) { this.additionalNotes = additionalNotes; }
}