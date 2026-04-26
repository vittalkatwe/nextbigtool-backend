package com.nextbigtool.backend.entity.bip;

import com.nextbigtool.backend.entity.user.AppUser;
import jakarta.persistence.*;

@Entity
@Table(name = "bip_comment_likes", uniqueConstraints = @UniqueConstraint(columnNames = {"comment_id", "user_id"}))
public class BipCommentLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id", nullable = false)
    private BipComment comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BipComment getComment() { return comment; }
    public void setComment(BipComment comment) { this.comment = comment; }

    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }
}
