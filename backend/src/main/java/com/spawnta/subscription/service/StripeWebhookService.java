package com.spawnta.subscription.service;

import com.stripe.model.Charge;
import com.stripe.model.checkout.Session;
import com.stripe.model.Invoice;
import com.stripe.model.Subscription;
import com.spawnta.entity.User;
import com.spawnta.repository.UserRepository;
import com.spawnta.service.EmailService;
import com.spawnta.subscription.entity.*;
import com.spawnta.subscription.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

/**
 * Service for handling Stripe webhook events
 */
@Service
@Transactional
public class StripeWebhookService {

    private static final Logger logger = LoggerFactory.getLogger(StripeWebhookService.class);

    private final UserRepository userRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final InvoiceRepository invoiceRepository;
    private final EmailService emailService;

    public StripeWebhookService(
            UserRepository userRepository,
            UserSubscriptionRepository userSubscriptionRepository,
            SubscriptionPlanRepository subscriptionPlanRepository,
            PaymentTransactionRepository paymentTransactionRepository,
            InvoiceRepository invoiceRepository,
            EmailService emailService
    ) {
        this.userRepository = userRepository;
        this.userSubscriptionRepository = userSubscriptionRepository;
        this.subscriptionPlanRepository = subscriptionPlanRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.invoiceRepository = invoiceRepository;
        this.emailService = emailService;
    }

    /**
     * Handle successful checkout session completion
     * This is triggered when a user completes payment
     */
    public void handleCheckoutCompleted(Session session) {
        logger.info("🎉 Processing checkout.session.completed for session: {}", session.getId());

        String stripeCustomerId = session.getCustomer();
        String userIdStr = session.getClientReferenceId();
        
        Optional<User> userOpt = Optional.empty();
        
        // 1. Try finding by clientReferenceId (userId) - most reliable for new customers
        if (userIdStr != null && !userIdStr.isEmpty()) {
            try {
                Long userId = Long.parseLong(userIdStr);
                userOpt = userRepository.findById(userId);
                logger.info("🔍 Looked up user by clientReferenceId: {}", userIdStr);
            } catch (NumberFormatException e) {
                logger.warn("⚠️ Invalid clientReferenceId format: {}", userIdStr);
            }
        }
        
        // 2. Fallback to Stripe customer ID
        if (userOpt.isEmpty() && stripeCustomerId != null) {
            userOpt = userRepository.findByStripeCustomerId(stripeCustomerId);
            logger.info("🔍 Looked up user by stripeCustomerId: {}", stripeCustomerId);
        }

        if (userOpt.isEmpty()) {
            logger.error("❌ User not found for session: {}. Customer: {}, UserID: {}", 
                session.getId(), stripeCustomerId, userIdStr);
            return;
        }

        User user = userOpt.get();
        logger.info("✅ Found user: {} (ID: {})", user.getEmail(), user.getId());

        // Update user with Stripe customer ID if not already set
        if (user.getStripeCustomerId() == null || user.getStripeCustomerId().isEmpty()) {
            user.setStripeCustomerId(stripeCustomerId);
            userRepository.save(user);
            logger.info("✅ Updated user with Stripe Customer ID");
        }

        // Get subscription details from session metadata
        String tierName = (String) session.getMetadata().get("tier");
        if (tierName == null) {
            logger.warn("⚠️ No tier in metadata, subscription tier update will happen via subscription events");
        } else {
            try {
                SubscriptionTier tier = SubscriptionTier.fromString(tierName);
                user.setSubscriptionTier(tier.name());
                userRepository.save(user);
                logger.info("✅ Updated user subscription tier to: {}", tier);
            } catch (IllegalArgumentException e) {
                logger.error("❌ Invalid tier name in metadata: {}", tierName);
            }
        }
    }

    /**
     * Handle subscription created event
     */
    public void handleSubscriptionCreated(Subscription stripeSubscription) {
        logger.info("🆕 Processing customer.subscription.created for subscription: {}", stripeSubscription.getId());
        updateUserSubscriptionFromStripe(stripeSubscription);
    }

    /**
     * Handle subscription update (e.g., plan change, renewal)
     */
    public void handleSubscriptionUpdated(Subscription stripeSubscription) {
        logger.info("🔄 Processing customer.subscription.updated for subscription: {}", stripeSubscription.getId());
        updateUserSubscriptionFromStripe(stripeSubscription);
    }

    /**
     * Common logic for creating or updating UserSubscription from a Stripe Subscription object
     */
    private void updateUserSubscriptionFromStripe(Subscription stripeSubscription) {
        String stripeCustomerId = stripeSubscription.getCustomer();
        logger.info("🔍 Updating subscription from Stripe. Customer ID: {}, Subscription ID: {}", 
            stripeCustomerId, stripeSubscription.getId());
        
        // Find user
        Optional<User> userOpt = userRepository.findByStripeCustomerId(stripeCustomerId);
        
        // Fallback: try finding by userId metadata if customer ID lookup fails
        if (userOpt.isEmpty()) {
            String userIdStr = stripeSubscription.getMetadata().get("userId");
            if (userIdStr != null) {
                try {
                    Long userId = Long.parseLong(userIdStr);
                    userOpt = userRepository.findById(userId);
                    logger.info("🔍 Looked up user by userId metadata: {}", userIdStr);
                } catch (NumberFormatException e) {
                    logger.warn("⚠️ Invalid userId in metadata: {}", userIdStr);
                }
            }
        }

        if (userOpt.isEmpty()) {
            logger.error("❌ User not found for Stripe customer: {} or metadata userId. Cannot update subscription.", stripeCustomerId);
            return;
        }
        
        User user = userOpt.get();
        logger.info("✅ Found user: {} (ID: {})", user.getEmail(), user.getId());
        
        // Get the plan from the subscription items
        if (stripeSubscription.getItems().getData().isEmpty()) {
            logger.error("❌ No items found in Stripe subscription: {}", stripeSubscription.getId());
            return;
        }
        
        String stripePriceId = stripeSubscription.getItems().getData().get(0).getPrice().getId();
        logger.info("📦 Stripe Price ID from subscription: {}", stripePriceId);
        
        Optional<SubscriptionPlan> planOpt = subscriptionPlanRepository.findByStripePriceId(stripePriceId);
        
        if (planOpt.isEmpty()) {
            logger.error("❌ Subscription plan not found for Stripe Price ID: {}. Check your database initialization.", stripePriceId);
            return;
        }
        
        SubscriptionPlan plan = planOpt.get();
        logger.info("📋 Found plan: {} (Tier: {})", plan.getName(), plan.getTier());
        
        // Update user's tier
        String oldTier = user.getSubscriptionTier();
        user.setSubscriptionTier(plan.getTier().name());
        userRepository.save(user);
        logger.info("✅ Updated user {} tier from {} to: {}", user.getEmail(), oldTier, plan.getTier());
        
        // Update UserSubscription record
        UserSubscription userSubscription = userSubscriptionRepository
                .findByStripeSubscriptionId(stripeSubscription.getId())
                .orElse(null);

        if (userSubscription == null) {
            logger.info("ℹ️ No UserSubscription found for Stripe Subscription ID: {}. Checking for existing active subscription to replace.", 
                stripeSubscription.getId());
            
            Optional<UserSubscription> existingActive = userSubscriptionRepository
                    .findByUserId(user.getId());
            
            if (existingActive.isPresent()) {
                userSubscription = existingActive.get();
                String oldStripeId = userSubscription.getStripeSubscriptionId();
                
                // If the old one is different from the new one, we should ideally cancel the old one on Stripe
                if (oldStripeId != null && !oldStripeId.equals(stripeSubscription.getId())) {
                    logger.warn("⚠️ User {} already has another active Stripe subscription: {}. Replacing with: {}", 
                        user.getEmail(), oldStripeId, stripeSubscription.getId());
                    // Note: In a real production app, you might want to call Stripe API to cancel the old one here
                    // or handle pro-rata credits. For now, we just update our local record.
                }
            } else {
                userSubscription = new UserSubscription();
                userSubscription.setCreatedAt(LocalDateTime.now());
                logger.info("🆕 Creating NEW UserSubscription record");
            }
        } else {
            logger.info("🔄 Updating EXISTING UserSubscription record (ID: {})", userSubscription.getId());
        }
        
        userSubscription.setUser(user);
        userSubscription.setPlan(plan);
        userSubscription.setStatus(SubscriptionStatus.ACTIVE);
        userSubscription.setStripeCustomerId(stripeCustomerId);
        userSubscription.setStripeSubscriptionId(stripeSubscription.getId());
        
        // Handle dates with fallbacks to avoid NOT NULL constraints in DB
        LocalDateTime startDate = toLocalDateTime(stripeSubscription.getCurrentPeriodStart());
        LocalDateTime renewalDate = toLocalDateTime(stripeSubscription.getCurrentPeriodEnd());
        
        if (startDate == null) {
            startDate = LocalDateTime.now();
            logger.warn("⚠️ Subscription start date was null from Stripe, using now()");
        }
        if (renewalDate == null) {
            renewalDate = startDate.plusMonths(1);
            logger.warn("⚠️ Subscription renewal date was null from Stripe, using startDate + 1 month");
        }
        
        userSubscription.setStartDate(startDate);
        userSubscription.setRenewalDate(renewalDate);
        userSubscription.setUpdatedAt(LocalDateTime.now());
        
        if (userSubscription.getId() == null) {
            userSubscription.setCreatedAt(LocalDateTime.now());
            logger.info("🆕 Creating NEW UserSubscription record");
        } else {
            logger.info("🔄 Updating EXISTING UserSubscription record (ID: {})", userSubscription.getId());
        }
        
        userSubscriptionRepository.save(userSubscription);
        logger.info("✅ Saved UserSubscription record for user: {}", user.getEmail());

        // Send confirmation email if it's a new subscription or status changed to ACTIVE
        try {
            emailService.sendSubscriptionConfirmation(user, plan);
            logger.info("✅ Sent confirmation email to: {}", user.getEmail());
        } catch (Exception e) {
            logger.error("❌ Failed to send confirmation email: {}", e.getMessage());
        }
    }

    /**
     * Handle successful invoice payment
     */
    public void handleInvoicePaymentSucceeded(Invoice stripeInvoice) {
        logger.info("💰 Processing invoice.payment_succeeded for invoice: {}", stripeInvoice.getId());

        String stripeCustomerId = stripeInvoice.getCustomer();
        Optional<User> userOpt = userRepository.findByStripeCustomerId(stripeCustomerId);
        if (userOpt.isEmpty()) {
            logger.error("❌ User not found for customer: {}", stripeCustomerId);
            return;
        }

        User user = userOpt.get();

        // 1. Create/Update Invoice record
        com.spawnta.subscription.entity.Invoice invoice = invoiceRepository
                .findByStripeInvoiceId(stripeInvoice.getId())
                .orElse(new com.spawnta.subscription.entity.Invoice());
        
        invoice.setUser(user);
        invoice.setStripeInvoiceId(stripeInvoice.getId());
        invoice.setAmount(BigDecimal.valueOf(stripeInvoice.getAmountPaid() / 100.0));
        invoice.setCurrency(stripeInvoice.getCurrency().toUpperCase());
        invoice.setStatus(InvoiceStatus.PAID);
        
        LocalDateTime invoiceDate = toLocalDateTime(stripeInvoice.getCreated());
        if (invoiceDate == null) {
            invoiceDate = LocalDateTime.now();
        }
        invoice.setInvoiceDate(invoiceDate);
        invoice.setDueDate(toLocalDateTime(stripeInvoice.getDueDate()));
        
        if (stripeInvoice.getStatusTransitions() != null && stripeInvoice.getStatusTransitions().getPaidAt() != null) {
            invoice.setPaidDate(toLocalDateTime(stripeInvoice.getStatusTransitions().getPaidAt()));
        }
        
        if (invoice.getId() == null) {
            invoice.setCreatedAt(LocalDateTime.now());
        }
        invoice.setUpdatedAt(LocalDateTime.now());

        invoiceRepository.save(invoice);
        logger.info("✅ Saved invoice record");

        // 2. Create payment transaction if payment intent exists
        String paymentIntentId = stripeInvoice.getPaymentIntent();
        if (paymentIntentId != null) {
            PaymentTransaction transaction = paymentTransactionRepository
                    .findByStripePaymentIntentId(paymentIntentId)
                    .orElse(new PaymentTransaction());

            transaction.setUser(user);
            transaction.setStripePaymentIntentId(paymentIntentId);
            transaction.setAmount(BigDecimal.valueOf(stripeInvoice.getAmountPaid() / 100.0));
            transaction.setCurrency(stripeInvoice.getCurrency().toUpperCase());
            transaction.setStatus(PaymentStatus.SUCCEEDED);
            
            if (transaction.getId() == null) {
                transaction.setCreatedAt(LocalDateTime.now());
            }
            transaction.setUpdatedAt(LocalDateTime.now());

            paymentTransactionRepository.save(transaction);
            logger.info("✅ Saved payment transaction record");
        } else {
            logger.warn("⚠️ No PaymentIntent found for invoice: {}. Skipping transaction record.", stripeInvoice.getId());
        }
    }

    /**
     * Handle failed invoice payment
     */
    public void handleInvoicePaymentFailed(Invoice stripeInvoice) {
        logger.warn("⚠️ Processing invoice.payment_failed for invoice: {}", stripeInvoice.getId());

        String stripeCustomerId = stripeInvoice.getCustomer();
        Optional<User> userOpt = userRepository.findByStripeCustomerId(stripeCustomerId);
        if (userOpt.isEmpty()) {
            logger.error("❌ User not found for customer: {}", stripeCustomerId);
            return;
        }

        User user = userOpt.get();

        // Send payment failed notification email
        try {
            emailService.sendPaymentFailedNotification(user);
            logger.info("✅ Sent payment failed notification to: {}", user.getEmail());
        } catch (Exception e) {
            logger.error("❌ Failed to send notification email: {}", e.getMessage());
        }
    }

    /**
     * Handle charge refund
     */
    public void handleChargeRefunded(Charge charge) {
        logger.info("💸 Processing charge.refunded for charge: {}", charge.getId());

        String stripePaymentIntentId = charge.getPaymentIntent();
        if (stripePaymentIntentId == null) return;

        Optional<PaymentTransaction> transactionOpt = paymentTransactionRepository
                .findByStripePaymentIntentId(stripePaymentIntentId);

        if (transactionOpt.isPresent()) {
            PaymentTransaction transaction = transactionOpt.get();
            transaction.setStatus(PaymentStatus.REFUNDED);
            transaction.setUpdatedAt(LocalDateTime.now());
            paymentTransactionRepository.save(transaction);
            logger.info("✅ Marked transaction as REFUNDED");
        }
    }

    /**
     * Handle subscription cancellation/deletion
     */
    public void handleSubscriptionDeleted(Subscription stripeSubscription) {
        logger.info("❌ Processing customer.subscription.deleted for subscription: {}", stripeSubscription.getId());

        String stripeCustomerId = stripeSubscription.getCustomer();
        Optional<User> userOpt = userRepository.findByStripeCustomerId(stripeCustomerId);
        if (userOpt.isEmpty()) {
            logger.error("❌ User not found for customer: {}", stripeCustomerId);
            return;
        }

        User user = userOpt.get();

        // Update user to FREE tier (User entity expects String)
        user.setSubscriptionTier(SubscriptionTier.FREE.name());
        userRepository.save(user);
        logger.info("✅ Downgraded user to FREE tier");

        // Update UserSubscription status
        UserSubscription userSubscription = userSubscriptionRepository
                .findByStripeSubscriptionId(stripeSubscription.getId())
                .orElse(null);

        if (userSubscription != null) {
            userSubscription.setStatus(SubscriptionStatus.CANCELLED);
            userSubscription.setEndDate(LocalDateTime.now());
            userSubscription.setCancelReason("Cancelled via Stripe");
            userSubscription.setUpdatedAt(LocalDateTime.now());
            userSubscriptionRepository.save(userSubscription);
            logger.info("✅ Marked subscription as CANCELLED");
        }

        // Send cancellation email
        try {
            emailService.sendSubscriptionCancellation(user);
            logger.info("✅ Sent cancellation email to: {}", user.getEmail());
        } catch (Exception e) {
            logger.error("❌ Failed to send cancellation email: {}", e.getMessage());
        }
    }

    /**
     * Convert Unix timestamp to LocalDateTime
     */
    private LocalDateTime toLocalDateTime(Long timestamp) {
        if (timestamp == null) return null;
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), ZoneId.systemDefault());
    }
}
