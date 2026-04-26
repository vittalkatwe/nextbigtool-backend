package com.nextbigtool.backend.entity.bip;

import com.nextbigtool.backend.entity.user.AppUser;
import jakarta.persistence.*;

@Entity
@Table(name = "bip_post_likes", uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "user_id"}))
public class PostLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private BuildInPublicPost post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    private String reaction = "🔥";

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public BuildInPublicPost getPost() { return post; }
    public void setPost(BuildInPublicPost post) { this.post = post; }

    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }

    public String getReaction() { return reaction; }
    public void setReaction(String reaction) { this.reaction = reaction; }
}
