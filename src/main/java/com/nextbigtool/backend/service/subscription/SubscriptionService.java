package com.nextbigtool.backend.service.subscription;

import com.nextbigtool.backend.entity.user.AppUser;
import com.nextbigtool.backend.entity.user.BillingCycle;
import com.nextbigtool.backend.entity.user.PlanType;
import com.nextbigtool.backend.entity.user.Subscription;
import com.nextbigtool.backend.entity.user.SubscriptionStatus;
import com.nextbigtool.backend.repository.SubscriptionRepository;
import com.nextbigtool.backend.repository.UserRepository;
import com.nextbigtool.backend.service.auth.CurrentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Service
public class SubscriptionService {

    @Autowired private SubscriptionRepository subscriptionRepository;
    @Autowired private CurrentUserService currentUserService;
    @Autowired private UserRepository userRepository;

    @Transactional
    public Subscription createFreeSubscription(AppUser user) {
        Subscription sub = new Subscription();
        sub.setUser(user);
        sub.setPlanType(PlanType.FREE);
        return subscriptionRepository.save(sub);
    }

    public ResponseEntity<?> getMySubscription() {
        AppUser user = currentUserService.getCurrentUser();
        Subscription sub = subscriptionRepository.findByUser(user)
                .orElseGet(() -> createFreeSubscription(user));

        Map<String, Object> data = new java.util.LinkedHashMap<>();
        data.put("planType", sub.getPlanType());
        data.put("effectivePlan", sub.getEffectivePlan());
        data.put("billingCycle", sub.getBillingCycle());
        data.put("isActive", sub.isActive());
        data.put("subscriptionStatus", sub.getSubscriptionStatus());
        data.put("createdAt", sub.getCreatedAt());
        if (sub.getUpgradedAt() != null) data.put("upgradedAt", sub.getUpgradedAt());
        if (sub.getPeriodEnd() != null) data.put("periodEnd", sub.getPeriodEnd());
        if (sub.getRazorpaySubscriptionId() != null) data.put("razorpaySubscriptionId", sub.getRazorpaySubscriptionId());

        return ResponseEntity.ok(Map.of("success", true, "data", data));
    }

    // ── Monthly recurring subscription ───────────────────────────────────────

    @Transactional
    public Subscription activateMonthlySubscription(AppUser user, PlanType planType,
                                                    String razorpaySubscriptionId, String firstPaymentId) {
        Subscription sub = getOrCreateFree(user);
        sub.setPlanType(planType);
        sub.setBillingCycle(BillingCycle.MONTHLY);
        sub.setSubscriptionStatus(SubscriptionStatus.ACTIVE);
        sub.setUpgradedAt(LocalDateTime.now());
        sub.setRazorpaySubscriptionId(razorpaySubscriptionId);
        sub.setRazorpayPaymentId(firstPaymentId);
        sub.setPeriodEnd(LocalDateTime.now().plusMonths(1));
        return subscriptionRepository.save(sub);
    }

    /** Called by webhook: subscription.charged — extends access by 1 month */
    @Transactional
    public void handleSubscriptionCharged(String razorpaySubscriptionId, String paymentId) {
        subscriptionRepository.findByRazorpaySubscriptionId(razorpaySubscriptionId).ifPresent(sub -> {
            sub.setSubscriptionStatus(SubscriptionStatus.ACTIVE);
            sub.setRazorpayPaymentId(paymentId);
            // Extend from current periodEnd or now, whichever is later
            LocalDateTime base = sub.getPeriodEnd() != null && sub.getPeriodEnd().isAfter(LocalDateTime.now())
                    ? sub.getPeriodEnd()
                    : LocalDateTime.now();
            sub.setPeriodEnd(base.plusMonths(1));
            subscriptionRepository.save(sub);
        });
    }

    /** Called by webhook: subscription.halted — payment failed, revoke immediately */
    @Transactional
    public void handleSubscriptionHalted(String razorpaySubscriptionId) {
        subscriptionRepository.findByRazorpaySubscriptionId(razorpaySubscriptionId).ifPresent(sub -> {
            sub.setSubscriptionStatus(SubscriptionStatus.HALTED);
            subscriptionRepository.save(sub);
        });
    }

    /** Called by webhook: subscription.cancelled — let period drain, then expire */
    @Transactional
    public void handleSubscriptionCancelled(String razorpaySubscriptionId) {
        subscriptionRepository.findByRazorpaySubscriptionId(razorpaySubscriptionId).ifPresent(sub -> {
            sub.setSubscriptionStatus(SubscriptionStatus.CANCELLED);
            // Keep periodEnd as-is — plan reverts to FREE when it expires
            subscriptionRepository.save(sub);
        });
    }

    /** Called by webhook: subscription.completed — all cycles done */
    @Transactional
    public void handleSubscriptionCompleted(String razorpaySubscriptionId) {
        subscriptionRepository.findByRazorpaySubscriptionId(razorpaySubscriptionId).ifPresent(sub -> {
            sub.setSubscriptionStatus(SubscriptionStatus.COMPLETED);
            subscriptionRepository.save(sub);
        });
    }

    // ── Yearly one-time plan ─────────────────────────────────────────────────

    @Transactional
    public Subscription activatePlan(AppUser user, PlanType planType, BillingCycle billingCycle,
                                     String razorpayOrderId, String razorpayPaymentId) {
        Subscription sub = getOrCreateFree(user);
        sub.setPlanType(planType);
        sub.setBillingCycle(billingCycle);
        sub.setSubscriptionStatus(SubscriptionStatus.ACTIVE);
        sub.setUpgradedAt(LocalDateTime.now());
        sub.setRazorpayOrderId(razorpayOrderId);
        sub.setRazorpayPaymentId(razorpayPaymentId);
        sub.setPeriodEnd(LocalDateTime.now().plusYears(1));
        return subscriptionRepository.save(sub);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    public Subscription getOrCreateFree(AppUser user) {
        return subscriptionRepository.findByUser(user)
                .orElseGet(() -> createFreeSubscription(user));
    }
}
