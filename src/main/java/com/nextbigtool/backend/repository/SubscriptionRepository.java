package com.nextbigtool.backend.repository;

import com.nextbigtool.backend.entity.user.AppUser;
import com.nextbigtool.backend.entity.user.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    Optional<Subscription> findByUser(AppUser user);
    Optional<Subscription> findByRazorpayOrderId(String orderId);
    Optional<Subscription> findByRazorpaySubscriptionId(String subscriptionId);
}
