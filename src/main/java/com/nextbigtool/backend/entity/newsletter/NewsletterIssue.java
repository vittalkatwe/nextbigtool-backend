package com.nextbigtool.backend.entity.newsletter;

import com.nextbigtool.backend.entity.user.AppUser;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "newsletter_issues")
public class NewsletterIssue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publisher_id", nullable = false)
    private AppUser publisher;

    @Column(nullable = false)
    private String subject;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false, updatable = false)
    private LocalDateTime sentAt;

    @Column(nullable = false)
    private Integer recipientCount = 0;

    @Column(nullable = false)
    private Integer openCount = 0;

    @PrePersist
    public void onCreate() { this.sentAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public AppUser getPublisher() { return publisher; }
    public void setPublisher(AppUser publisher) { this.publisher = publisher; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getSentAt() { return sentAt; }
    public Integer getRecipientCount() { return recipientCount; }
    public void setRecipientCount(Integer recipientCount) { this.recipientCount = recipientCount; }
    public Integer getOpenCount() { return openCount; }
    public void setOpenCount(Integer openCount) { this.openCount = openCount; }
}
