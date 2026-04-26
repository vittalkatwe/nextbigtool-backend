package com.nextbigtool.backend.model.tool;

import com.nextbigtool.backend.entity.tool.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class ToolResponseDto {

    private Long id;
    private String productName;
    private String websiteUrl;
    private String logoUrl;
    private String tagline;
    private String description;
    private List<String> screenshots;
    private String videoUrl;
    private ToolCategory category;
    private List<TargetAudience> targetAudiences;
    private List<UseCase> useCases;
    private List<Platform> platforms;
    private List<AlternativeDto> alternatives;
    private String mrr;
    private String arr;
    private String discountCode;
    private ToolStatus status;
    private LocalDateTime createdAt;
    private Long submittedById;
    private String submittedByEmail;
    private long upvoteCount;
    private long commentCount;

    public static class AlternativeDto {
        private String name;
        private String url;

        public AlternativeDto(String name, String url) {
            this.name = name;
            this.url = url;
        }

        public String getName() { return name; }
        public String getUrl() { return url; }
    }

    public static ToolResponseDto from(Tool tool) {
        ToolResponseDto dto = new ToolResponseDto();
        dto.id = tool.getId();
        dto.productName = tool.getProductName();
        dto.websiteUrl = tool.getWebsiteUrl();
        dto.logoUrl = tool.getLogoUrl();
        dto.tagline = tool.getTagline();
        dto.description = tool.getDescription();
        dto.screenshots = tool.getScreenshots();
        dto.videoUrl = tool.getVideoUrl();
        dto.category = tool.getCategory();
        dto.targetAudiences = tool.getTargetAudiences();
        dto.useCases = tool.getUseCases();
        dto.platforms = tool.getPlatforms();
        dto.mrr = tool.getMrr();
        dto.arr = tool.getArr();
        dto.discountCode = tool.getDiscountCode();
        dto.status = tool.getStatus();
        dto.createdAt = tool.getCreatedAt();
        dto.submittedById = tool.getSubmittedBy().getId();
        dto.submittedByEmail = tool.getSubmittedBy().getEmail();

        if (tool.getAlternatives() != null) {
            dto.alternatives = tool.getAlternatives().stream()
                    .map(a -> new AlternativeDto(a.getName(), a.getUrl()))
                    .collect(Collectors.toList());
        }

        return dto;
    }

    public Long getId() { return id; }
    public String getProductName() { return productName; }
    public String getWebsiteUrl() { return websiteUrl; }
    public String getLogoUrl() { return logoUrl; }
    public String getTagline() { return tagline; }
    public String getDescription() { return description; }
    public List<String> getScreenshots() { return screenshots; }
    public String getVideoUrl() { return videoUrl; }
    public ToolCategory getCategory() { return category; }
    public List<TargetAudience> getTargetAudiences() { return targetAudiences; }
    public List<UseCase> getUseCases() { return useCases; }
    public List<Platform> getPlatforms() { return platforms; }
    public List<AlternativeDto> getAlternatives() { return alternatives; }
    public String getMrr() { return mrr; }
    public String getArr() { return arr; }
    public String getDiscountCode() { return discountCode; }
    public ToolStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public Long getSubmittedById() { return submittedById; }
    public String getSubmittedByEmail() { return submittedByEmail; }
    public long getUpvoteCount() { return upvoteCount; }
    public void setUpvoteCount(long upvoteCount) { this.upvoteCount = upvoteCount; }
    public long getCommentCount() { return commentCount; }
    public void setCommentCount(long commentCount) { this.commentCount = commentCount; }
}
