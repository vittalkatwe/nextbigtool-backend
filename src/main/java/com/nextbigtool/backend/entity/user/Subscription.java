package com.nextbigtool.backend.entity.user;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "subscriptions")
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private AppUser user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanType planType = PlanType.FREE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BillingCycle billingCycle = BillingCycle.MONTHLY;

    @Enumerated(EnumType.STRING)
    private SubscriptionStatus subscriptionStatus;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime upgradedAt;
    private LocalDateTime periodEnd;

    // One-time order payments (yearly)
    private String razorpayOrderId;
    private String razorpayPaymentId;

    // Recurring subscription (monthly)
    private String razorpaySubscriptionId;

    @PrePersist
    public void onCreate() { this.createdAt = LocalDateTime.now(); }

    public boolean isActive() {
        if (planType == PlanType.FREE) return true;
        if (billingCycle == BillingCycle.MONTHLY) {
            // For recurring: active unless HALTED or CANCELLED and period has ended
            if (subscriptionStatus == SubscriptionStatus.HALTED) return false;
        }
        return periodEnd == null || periodEnd.isAfter(LocalDateTime.now());
    }

    public PlanType getEffectivePlan() {
        return isActive() ? planType : PlanType.FREE;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public AppUser getUser() { return user; }
    public void setUser(AppUser user) { this.user = user; }

    public PlanType getPlanType() { return planType; }
    public void setPlanType(PlanType planType) { this.planType = planType; }

    public BillingCycle getBillingCycle() { return billingCycle; }
    public void setBillingCycle(BillingCycle billingCycle) { this.billingCycle = billingCycle; }

    public SubscriptionStatus getSubscriptionStatus() { return subscriptionStatus; }
    public void setSubscriptionStatus(SubscriptionStatus subscriptionStatus) { this.subscriptionStatus = subscriptionStatus; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpgradedAt() { return upgradedAt; }
    public void setUpgradedAt(LocalDateTime upgradedAt) { this.upgradedAt = upgradedAt; }

    public LocalDateTime getPeriodEnd() { return periodEnd; }
    public void setPeriodEnd(LocalDateTime periodEnd) { this.periodEnd = periodEnd; }

    public String getRazorpayOrderId() { return razorpayOrderId; }
    public void setRazorpayOrderId(String razorpayOrderId) { this.razorpayOrderId = razorpayOrderId; }

    public String getRazorpayPaymentId() { return razorpayPaymentId; }
    public void setRazorpayPaymentId(String razorpayPaymentId) { this.razorpayPaymentId = razorpayPaymentId; }

    public String getRazorpaySubscriptionId() { return razorpaySubscriptionId; }
    public void setRazorpaySubscriptionId(String razorpaySubscriptionId) { this.razorpaySubscriptionId = razorpaySubscriptionId; }
}
