package com.spawnta.subscription.service;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stripe.exception.StripeException;
import com.spawnta.entity.User;
import com.spawnta.repository.UserRepository;
import com.spawnta.subscription.entity.*;
import com.spawnta.subscription.repository.*;

@Service
@Transactional
public class BillingService {
    
    private static final Logger logger = LoggerFactory.getLogger(BillingService.class);
    
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final UserRepository userRepository;
    private final StripeService stripeService;
    
    public BillingService(
            UserSubscriptionRepository userSubscriptionRepository,
            SubscriptionPlanRepository subscriptionPlanRepository,
            UserRepository userRepository,
            StripeService stripeService
    ) {
        this.userSubscriptionRepository = userSubscriptionRepository;
        this.subscriptionPlanRepository = subscriptionPlanRepository;
        this.userRepository = userRepository;
        this.stripeService = stripeService;
    }
    
    /**
     * Upgrade a user's subscription to a new tier.
     * 
     * @param user The user to upgrade
     * @param newTier The target tier
     * @throws StripeException If interaction with Stripe fails
     */
    public void upgradeSubscription(User user, SubscriptionTier newTier) throws StripeException {
        logger.info("Upgrading user {} to tier {}", user.getId(), newTier);
        
        SubscriptionPlan plan = subscriptionPlanRepository.findByTier(newTier)
                .orElseThrow(() -> new IllegalArgumentException("Subscription plan not found for tier: " + newTier));
        
        UserSubscription current = userSubscriptionRepository.findByUserId(user.getId())
                .orElse(null);
        
        if (current != null) {
            current.setPlan(plan);
            current.setStatus(SubscriptionStatus.ACTIVE);
            current.setRenewalDate(LocalDateTime.now().plusMonths(1));
            current.setEndDate(null);
            userSubscriptionRepository.save(current);
        } else {
            String stripeCustomerId = stripeService.createOrUpdateCustomer(user);
            UserSubscription newSubscription = UserSubscription.builder()
                    .user(user)
                    .plan(plan)
                    .stripeCustomerId(stripeCustomerId)
                    .status(SubscriptionStatus.ACTIVE)
                    .startDate(LocalDateTime.now())
                    .renewalDate(LocalDateTime.now().plusMonths(1))
                    .build();
            userSubscriptionRepository.save(newSubscription);
        }
        
        // Sync the subscription tier directly on the user entity
        user.setSubscriptionTier(newTier.name());
        userRepository.save(user);
        
        logger.info("Successfully upgraded user {} to tier {}", user.getId(), newTier);
    }
    
    /**
     * Scheduled task to check and automatically renew active subscriptions that have expired.
     */
    @Scheduled(cron = "0 0 0 * * *") // Daily at midnight
    public void autoRenewSubscriptions() {
        logger.info("Running auto-renew subscriptions task");
        LocalDateTime now = LocalDateTime.now();
        
        // Find active subscriptions whose renewal date is in the past
        List<UserSubscription> expiringSubscriptions = userSubscriptionRepository
                .findByStatusAndRenewalDateBefore(SubscriptionStatus.ACTIVE, now);
        
        logger.info("Found {} subscriptions requiring renewal check", expiringSubscriptions.size());
        
        for (UserSubscription subscription : expiringSubscriptions) {
            try {
                // If it doesn't have a cancellation date or end date set, we roll it forward.
                if (subscription.getEndDate() == null) {
                    subscription.setRenewalDate(now.plusMonths(1));
                    userSubscriptionRepository.save(subscription);
                    logger.info("Auto-renewed subscription for user: {}", subscription.getUser().getId());
                } else {
                    // Subscription cancelled but reached period end -> expire it
                    subscription.setStatus(SubscriptionStatus.EXPIRED);
                    userSubscriptionRepository.save(subscription);
                    
                    User user = subscription.getUser();
                    user.setSubscriptionTier("FREE");
                    userRepository.save(user);
                    logger.info("Expired cancelled subscription for user: {}", user.getId());
                }
            } catch (Exception e) {
                logger.error("Error auto-renewing subscription ID: {}", subscription.getId(), e);
            }
        }
    }
}
