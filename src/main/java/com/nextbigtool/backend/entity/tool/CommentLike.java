package com.nextbigtool.backend.entity.tool;

import com.nextbigtool.backend.entity.user.AppUser;
import jakarta.persistence.*;

@Entity
@Table(name = "comment_likes", uniqueConstraints = @UniqueConstraint(columnNames = {"comment_id", "user_id"}))
public class CommentLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    private ToolComment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public ToolComment getComment() { return comment; }
    public void setComment(ToolComment comment) { this.comment = comment; }

    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }
}
