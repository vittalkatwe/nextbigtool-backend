package com.nextbigtool.backend.entity.tool;

import com.nextbigtool.backend.entity.user.AppUser;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tool_views")
public class ToolView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tool_id", nullable = false)
    private Tool tool;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @Column(nullable = false, updatable = false)
    private LocalDateTime viewedAt;

    @PrePersist
    public void onCreate() { this.viewedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Tool getTool() { return tool; }
    public void setTool(Tool tool) { this.tool = tool; }

    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }

    public LocalDateTime getViewedAt() { return viewedAt; }
    public void setViewedAt(LocalDateTime viewedAt) { this.viewedAt = viewedAt; }
}
