package com.nextbigtool.backend.entity.newsletter;

import com.nextbigtool.backend.entity.user.AppUser;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "newsletter_subscribers",
        uniqueConstraints = @UniqueConstraint(columnNames = {"publisher_id", "subscriber_id"}))
public class NewsletterSubscriber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "publisher_id", nullable = false)
    private AppUser publisher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscriber_id", nullable = false)
    private AppUser subscriber;

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false, updatable = false)
    private LocalDateTime subscribedAt;

    @PrePersist
    public void onCreate() { this.subscribedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public AppUser getPublisher() { return publisher; }
    public void setPublisher(AppUser publisher) { this.publisher = publisher; }
    public AppUser getSubscriber() { return subscriber; }
    public void setSubscriber(AppUser subscriber) { this.subscriber = subscriber; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    public LocalDateTime getSubscribedAt() { return subscribedAt; }
}
