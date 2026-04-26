package com.nextbigtool.backend.entity.tool;

import com.nextbigtool.backend.entity.user.AppUser;
import jakarta.persistence.*;

@Entity
@Table(name = "tool_upvotes", uniqueConstraints = @UniqueConstraint(columnNames = {"tool_id", "user_id"}))
public class ToolUpvote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tool_id", nullable = false)
    private Tool tool;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Tool getTool() { return tool; }
    public void setTool(Tool tool) { this.tool = tool; }

    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }
}
