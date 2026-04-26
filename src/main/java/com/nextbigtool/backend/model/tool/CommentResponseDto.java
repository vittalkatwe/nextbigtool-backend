package com.nextbigtool.backend.model.tool;

import com.nextbigtool.backend.entity.tool.ToolComment;

import java.time.LocalDateTime;

public class CommentResponseDto {

    private Long id;
    private Long toolId;
    private Long userId;
    private String userEmail;
    private String userFirstname;
    private String content;
    private Long parentId;
    private long likeCount;
    private boolean liked;
    private LocalDateTime createdAt;

    public static CommentResponseDto from(ToolComment comment, long likeCount, boolean liked) {
        CommentResponseDto dto = new CommentResponseDto();
        dto.id = comment.getId();
        dto.toolId = comment.getTool().getId();
        dto.userId = comment.getUser().getId();
        dto.userEmail = comment.getUser().getEmail();
        dto.userFirstname = comment.getUser().getFirstname();
        dto.content = Boolean.TRUE.equals(comment.getDeleted()) ? "[deleted]" : comment.getContent();
        dto.parentId = comment.getParent() != null ? comment.getParent().getId() : null;
        dto.likeCount = likeCount;
        dto.liked = liked;
        dto.createdAt = comment.getCreatedAt();
        return dto;
    }

    public Long getId() { return id; }
    public Long getToolId() { return toolId; }
    public Long getUserId() { return userId; }
    public String getUserEmail() { return userEmail; }
    public String getUserFirstname() { return userFirstname; }
    public String getContent() { return content; }
    public Long getParentId() { return parentId; }
    public long getLikeCount() { return likeCount; }
    public boolean isLiked() { return liked; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
