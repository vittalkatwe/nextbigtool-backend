package com.nextbigtool.backend.model.tool;

import com.nextbigtool.backend.entity.tool.*;

import java.util.List;

public class ToolSubmitRequestDto {

    private String productName;
    private String websiteUrl;
    private String logoUrl;
    private String tagline;
    private String description;

    // ── Media ────────────────────────────────────────────────────────────────
    private List<String> screenshots; // max 10, S3 URLs
    private String videoUrl;          // YouTube/Vimeo/S3 URL

    private ToolCategory category;
    private List<TargetAudience> targetAudiences;
    private List<UseCase> useCases;
    private List<Platform> platforms;
    private List<AlternativeDto> alternatives;

    private String mrr;
    private String arr;
    private String discountCode;

    public static class AlternativeDto {
        private String name;
        private String url;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
    }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getWebsiteUrl() { return websiteUrl; }
    public void setWebsiteUrl(String websiteUrl) { this.websiteUrl = websiteUrl; }

    public String getLogoUrl() { return logoUrl; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }

    public String getTagline() { return tagline; }
    public void setTagline(String tagline) { this.tagline = tagline; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getScreenshots() { return screenshots; }
    public void setScreenshots(List<String> screenshots) { this.screenshots = screenshots; }

    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }

    public ToolCategory getCategory() { return category; }
    public void setCategory(ToolCategory category) { this.category = category; }

    public List<TargetAudience> getTargetAudiences() { return targetAudiences; }
    public void setTargetAudiences(List<TargetAudience> targetAudiences) { this.targetAudiences = targetAudiences; }

    public List<UseCase> getUseCases() { return useCases; }
    public void setUseCases(List<UseCase> useCases) { this.useCases = useCases; }

    public List<Platform> getPlatforms() { return platforms; }
    public void setPlatforms(List<Platform> platforms) { this.platforms = platforms; }

    public List<AlternativeDto> getAlternatives() { return alternatives; }
    public void setAlternatives(List<AlternativeDto> alternatives) { this.alternatives = alternatives; }

    public String getMrr() { return mrr; }
    public void setMrr(String mrr) { this.mrr = mrr; }

    public String getArr() { return arr; }
    public void setArr(String arr) { this.arr = arr; }

    public String getDiscountCode() { return discountCode; }
    public void setDiscountCode(String discountCode) { this.discountCode = discountCode; }
}
