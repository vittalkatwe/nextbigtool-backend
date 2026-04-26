package com.nextbigtool.backend.model.bip;

import com.nextbigtool.backend.entity.bip.BuildInPublicPost;
import com.nextbigtool.backend.entity.bip.PostType;

import java.time.LocalDateTime;

public class BipPostResponseDto {
    private Long id;
    private Long userId;
    private String userEmail;
    private String userFirstname;
    private PostType type;
    private String title;
    private String content;
    private String metricLabel;
    private String metricValue;
    private Long toolId;
    private String toolName;
    private long likeCount;
    private boolean liked;
    private long commentCount;
    private LocalDateTime createdAt;

    public static BipPostResponseDto from(BuildInPublicPost post, long likeCount, boolean liked) {
        BipPostResponseDto dto = new BipPostResponseDto();
        dto.id = post.getId();
        dto.userId = post.getUser().getId();
        dto.userEmail = post.getUser().getEmail();
        dto.userFirstname = post.getUser().getFirstname();
        dto.type = post.getType();
        dto.title = post.getTitle();
        dto.content = post.getContent();
        dto.metricLabel = post.getMetricLabel();
        dto.metricValue = post.getMetricValue();
        if (post.getTool() != null) {
            dto.toolId = post.getTool().getId();
            dto.toolName = post.getTool().getProductName();
        }
        dto.likeCount = likeCount;
        dto.liked = liked;
        dto.createdAt = post.getCreatedAt();
        return dto;
    }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getUserEmail() { return userEmail; }
    public String getUserFirstname() { return userFirstname; }
    public PostType getType() { return type; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getMetricLabel() { return metricLabel; }
    public String getMetricValue() { return metricValue; }
    public Long getToolId() { return toolId; }
    public String getToolName() { return toolName; }
    public long getLikeCount() { return likeCount; }
    public boolean isLiked() { return liked; }
    public long getCommentCount() { return commentCount; }
    public void setCommentCount(long commentCount) { this.commentCount = commentCount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
