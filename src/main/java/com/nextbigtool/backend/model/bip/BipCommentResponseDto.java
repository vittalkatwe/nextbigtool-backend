package com.nextbigtool.backend.model.bip;

import com.nextbigtool.backend.entity.bip.BipComment;

import java.time.LocalDateTime;

public class BipCommentResponseDto {
    private Long id;
    private Long postId;
    private Long parentId;
    private Long userId;
    private String userEmail;
    private String userFirstname;
    private String content;
    private long likeCount;
    private boolean liked;
    private LocalDateTime createdAt;

    public static BipCommentResponseDto from(BipComment c, long likeCount, boolean liked) {
        BipCommentResponseDto dto = new BipCommentResponseDto();
        dto.id = c.getId();
        dto.postId = c.getPost().getId();
        dto.parentId = c.getParent() != null ? c.getParent().getId() : null;
        dto.userId = c.getUser().getId();
        dto.userEmail = c.getUser().getEmail();
        dto.userFirstname = c.getUser().getFirstname();
        dto.content = c.getContent();
        dto.likeCount = likeCount;
        dto.liked = liked;
        dto.createdAt = c.getCreatedAt();
        return dto;
    }

    public Long getId() { return id; }
    public Long getPostId() { return postId; }
    public Long getParentId() { return parentId; }
    public Long getUserId() { return userId; }
    public String getUserEmail() { return userEmail; }
    public String getUserFirstname() { return userFirstname; }
    public String getContent() { return content; }
    public long getLikeCount() { return likeCount; }
    public boolean isLiked() { return liked; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
