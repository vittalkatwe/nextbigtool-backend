package com.nextbigtool.backend.entity.tool;

import com.nextbigtool.backend.entity.user.AppUser;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "tools")
@Data
public class Tool {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by", nullable = false)
    private AppUser submittedBy;

    // ── Core Info ────────────────────────────────────────────────────────────
    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private String websiteUrl;

    private String logoUrl;

    @Column(length = 160)
    private String tagline;

    @Column(columnDefinition = "TEXT")
    private String description;

    // ── Media ────────────────────────────────────────────────────────────────
    @ElementCollection
    @CollectionTable(name = "tool_screenshots", joinColumns = @JoinColumn(name = "tool_id"))
    @Column(name = "url")
    private List<String> screenshots;

    private String videoUrl;

    // ── Enums ────────────────────────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ToolCategory category;

    @ElementCollection
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "tool_target_audiences", joinColumns = @JoinColumn(name = "tool_id"))
    @Column(name = "audience")
    private List<TargetAudience> targetAudiences;

    @ElementCollection
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "tool_use_cases", joinColumns = @JoinColumn(name = "tool_id"))
    @Column(name = "use_case")
    private List<UseCase> useCases;

    // ── Platforms ────────────────────────────────────────────────────────────
    @ElementCollection
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "tool_platforms", joinColumns = @JoinColumn(name = "tool_id"))
    @Column(name = "platform")
    private List<Platform> platforms;

    // ── Alternatives ─────────────────────────────────────────────────────────
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "tool_id")
    private List<ToolAlternative> alternatives;

    // ── Additional Info ──────────────────────────────────────────────────────
    private String mrr;
    private String arr;
    private String discountCode;

    // ── Featured & Stats ─────────────────────────────────────────────────────
    private LocalDateTime featuredUntil;

    @Column(nullable = false)
    private Long viewCount = 0L;

    // ── Status ───────────────────────────────────────────────────────────────
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ToolStatus status = ToolStatus.PENDING;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime lastModified;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.lastModified = LocalDateTime.now();
    }

    @PreUpdate
    public void onUpdate() {
        this.lastModified = LocalDateTime.now();
    }
}
