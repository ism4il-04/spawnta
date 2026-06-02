package com.spawnta.subscription.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.spawnta.subscription.entity.SubscriptionPlan;
import com.spawnta.subscription.entity.SubscriptionTier;

import java.util.Optional;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {
    Optional<SubscriptionPlan> findByTier(SubscriptionTier tier);
    Optional<SubscriptionPlan> findByStripeProductId(String stripeProductId);
    Optional<SubscriptionPlan> findByStripePriceId(String stripePriceId);
}
