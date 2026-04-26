package com.nextbigtool.backend.entity.bip;

import com.nextbigtool.backend.entity.user.AppUser;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "bip_comments")
public class BipComment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private BuildInPublicPost post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private BipComment parent;

    @Column(nullable = false)
    private Boolean deleted = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() { this.createdAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BuildInPublicPost getPost() { return post; }
    public void setPost(BuildInPublicPost post) { this.post = post; }

    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public BipComment getParent() { return parent; }
    public void setParent(BipComment parent) { this.parent = parent; }

    public Boolean getDeleted() { return deleted; }
    public void setDeleted(Boolean deleted) { this.deleted = deleted; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
