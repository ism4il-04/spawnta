package com.spawnta.subscription.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.spawnta.subscription.entity.UserSubscription;
import com.spawnta.subscription.entity.SubscriptionStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSubscriptionRepository extends JpaRepository<UserSubscription, Long> {
    Optional<UserSubscription> findByUserId(Long userId);
    Optional<UserSubscription> findByStripeCustomerId(String stripeCustomerId);
    Optional<UserSubscription> findByStripeSubscriptionId(String stripeSubscriptionId);
    
    List<UserSubscription> findByStatus(SubscriptionStatus status);
    List<UserSubscription> findByStatusAndRenewalDateBefore(SubscriptionStatus status, LocalDateTime date);
    List<UserSubscription> findByEndDateBeforeAndStatus(LocalDateTime date, SubscriptionStatus status);
}
