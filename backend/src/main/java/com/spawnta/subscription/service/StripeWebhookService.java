package com.spawnta.subscription.service;

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
        String stripeSubscriptionId = session.getSubscription();

        // Find user by Stripe customer ID
        Optional<User> userOpt = userRepository.findByStripeCustomerId(stripeCustomerId);
        if (userOpt.isEmpty()) {
            logger.error("❌ User not found for Stripe customer: {}", stripeCustomerId);
            return;
        }

        User user = userOpt.get();
        logger.info("✅ Found user: {} (ID: {})", user.getEmail(), user.getId());

        // Get subscription details from session metadata or mode
        String tierName = (String) session.getMetadata().get("tier");
        if (tierName == null) {
            logger.warn("⚠️ No tier in metadata, attempting to derive from subscription");
            // Could fetch subscription details from Stripe API here if needed
            return;
        }

        SubscriptionTier tier = SubscriptionTier.fromString(tierName);
        SubscriptionPlan plan = subscriptionPlanRepository.findByTier(tier)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found for tier: " + tier));

        // Update user subscription tier (User entity expects String, not enum)
        user.setSubscriptionTier(tier.name());
        user.setStripeCustomerId(stripeCustomerId);
        userRepository.save(user);
        logger.info("✅ Updated user subscription tier to: {}", tier);

        // Create or update UserSubscription record
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime renewalDate = now.plusMonths(1);

        UserSubscription userSubscription = userSubscriptionRepository
                .findByUserIdAndStatus(user.getId(), SubscriptionStatus.ACTIVE)
                .orElse(new UserSubscription());

        userSubscription.setUser(user);
        userSubscription.setPlan(plan);
        userSubscription.setStatus(SubscriptionStatus.ACTIVE);
        userSubscription.setStartDate(now);
        userSubscription.setRenewalDate(renewalDate);
        userSubscription.setStripeCustomerId(stripeCustomerId);
        userSubscription.setStripeSubscriptionId(stripeSubscriptionId);
        userSubscription.setCreatedAt(now);
        userSubscription.setUpdatedAt(now);

        userSubscriptionRepository.save(userSubscription);
        logger.info("✅ Created/Updated UserSubscription record");

        // Send confirmation email
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

        // Create invoice record
        com.spawnta.subscription.entity.Invoice invoice = new com.spawnta.subscription.entity.Invoice();
        invoice.setUser(user);
        invoice.setStripeInvoiceId(stripeInvoice.getId());
        invoice.setAmount(BigDecimal.valueOf(stripeInvoice.getAmountPaid() / 100.0)); // Stripe amounts are in cents
        invoice.setCurrency(stripeInvoice.getCurrency().toUpperCase());
        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setInvoiceDate(toLocalDateTime(stripeInvoice.getCreated()));
        invoice.setDueDate(toLocalDateTime(stripeInvoice.getDueDate()));
        invoice.setPaidDate(toLocalDateTime(stripeInvoice.getStatusTransitions().getPaidAt()));
        invoice.setCreatedAt(LocalDateTime.now());
        invoice.setUpdatedAt(LocalDateTime.now());

        invoiceRepository.save(invoice);
        logger.info("✅ Created invoice record");

        // Create payment transaction
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setUser(user);
        transaction.setStripePaymentIntentId(stripeInvoice.getPaymentIntent());
        transaction.setAmount(BigDecimal.valueOf(stripeInvoice.getAmountPaid() / 100.0));
        transaction.setCurrency(stripeInvoice.getCurrency().toUpperCase());
        transaction.setStatus(PaymentStatus.SUCCEEDED);  // Use SUCCEEDED instead of COMPLETED
        transaction.setCreatedAt(LocalDateTime.now());
        transaction.setUpdatedAt(LocalDateTime.now());

        paymentTransactionRepository.save(transaction);
        logger.info("✅ Created payment transaction record");
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
     * Handle subscription update (e.g., plan change)
     */
    public void handleSubscriptionUpdated(Subscription stripeSubscription) {
        logger.info("🔄 Processing customer.subscription.updated for subscription: {}", stripeSubscription.getId());

        String stripeCustomerId = stripeSubscription.getCustomer();
        Optional<User> userOpt = userRepository.findByStripeCustomerId(stripeCustomerId);
        if (userOpt.isEmpty()) {
            logger.error("❌ User not found for customer: {}", stripeCustomerId);
            return;
        }

        User user = userOpt.get();

        // Update renewal date
        UserSubscription userSubscription = userSubscriptionRepository
                .findByStripeSubscriptionId(stripeSubscription.getId())
                .orElse(null);

        if (userSubscription != null) {
            userSubscription.setRenewalDate(toLocalDateTime(stripeSubscription.getCurrentPeriodEnd()));
            userSubscription.setUpdatedAt(LocalDateTime.now());
            userSubscriptionRepository.save(userSubscription);
            logger.info("✅ Updated subscription renewal date");
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
