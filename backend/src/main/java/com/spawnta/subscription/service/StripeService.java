package com.spawnta.subscription.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import com.stripe.model.Customer;
import com.stripe.model.Subscription;
import com.stripe.model.Event;
import com.stripe.model.Coupon;
import com.stripe.model.checkout.Session;
import com.stripe.param.CouponCreateParams;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.SubscriptionCancelParams;
import com.stripe.param.checkout.SessionCreateParams;

import com.spawnta.entity.User;
import com.spawnta.repository.UserRepository;
import com.spawnta.subscription.entity.*;
import com.spawnta.subscription.repository.*;
import com.spawnta.subscription.dto.*;
import com.spawnta.subscription.service.SubscriptionDiscountService;

/**
 * Service for handling all Stripe interactions
 * Including customer management, checkout sessions, invoices, and webhook handling
 */
@Service
@Transactional
public class StripeService {
    
    private static final Logger logger = LoggerFactory.getLogger(StripeService.class);
    
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final InvoiceRepository invoiceRepository;
    private final UserRepository userRepository;
    private final SubscriptionDiscountService discountService;
    
    @Value("${stripe.api.secretKey}")
    private String stripeSecretKey;
    
    @Value("${stripe.api.publicKey}")
    private String stripePublishableKey;
    
    @Value("${app.frontend-url}")
    private String frontendUrl;
    
    public StripeService(
            SubscriptionPlanRepository subscriptionPlanRepository,
            UserSubscriptionRepository userSubscriptionRepository,
            PaymentTransactionRepository paymentTransactionRepository,
            InvoiceRepository invoiceRepository,
            UserRepository userRepository,
            SubscriptionDiscountService discountService
    ) {
        this.subscriptionPlanRepository = subscriptionPlanRepository;
        this.userSubscriptionRepository = userSubscriptionRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.invoiceRepository = invoiceRepository;
        this.userRepository = userRepository;
        this.discountService = discountService;
    }
    
    /**
     * Initialize Stripe with API key
     * Called on bean creation
     */
    @jakarta.annotation.PostConstruct
    public void initializeStripe() {
        Stripe.apiKey = stripeSecretKey;
        logger.info("Stripe API initialized with key: {}...", stripeSecretKey.substring(0, 20));
    }
    
    /**
     * Create or update a Stripe customer from a User
     * 
     * @param user User to create customer for
     * @return Stripe customer ID
     */
    public String createOrUpdateCustomer(User user) throws StripeException {
        logger.info("Creating/updating Stripe customer for user: {}", user.getId());
        
        // Check if user already has a Stripe customer ID
        if (user.getStripeCustomerId() != null && !user.getStripeCustomerId().isEmpty()) {
            try {
                // Verify the customer still exists on Stripe
                Customer customer = Customer.retrieve(user.getStripeCustomerId());
                logger.info("Verified existing Stripe customer ID: {}", customer.getId());
                return customer.getId();
            } catch (StripeException e) {
                logger.warn("Existing Stripe customer ID {} is invalid or not found: {}. Recreating...", 
                        user.getStripeCustomerId(), e.getMessage());
                // ID is invalid, continue to create a new one
            }
        }
        
        // Create new customer in Stripe
        CustomerCreateParams params = CustomerCreateParams.builder()
                .setEmail(user.getEmail())
                .setName(user.getFirstName() + " " + user.getLastName())
                .putMetadata("userId", user.getId().toString())
                .putMetadata("email", user.getEmail())
                .build();
        
        Customer customer = Customer.create(params);
        logger.info("Created Stripe customer: {} for user: {}", customer.getId(), user.getId());
        
        // Update user with new Stripe customer ID
        user.setStripeCustomerId(customer.getId());
        userRepository.save(user);
        
        return customer.getId();
    }
    
    /**
     * Generate a checkout session for subscription upgrade
     * 
     * @param user User upgrading subscription
     * @param tier Target subscription tier
     * @param successUrl URL to redirect on success
     * @param cancelUrl URL to redirect on cancel
     * @return Checkout session response
     */
    public CheckoutSessionResponse createCheckoutSession(
            User user,
            String tier,
            String successUrl,
            String cancelUrl
    ) throws StripeException {
        logger.info("Creating checkout session for user: {} to tier: {}", user.getId(), tier);
        
        // Use fromString to handle both enum names and IDs (e.g., "pro" -> PROFESSIONAL, "starter" -> STARTER)
        SubscriptionTier subscriptionTier = SubscriptionTier.fromString(tier);
        SubscriptionPlan plan = subscriptionPlanRepository.findByTier(subscriptionTier)
                .orElseThrow(() -> new IllegalArgumentException("Invalid subscription tier: " + tier));
        
        // Create or get Stripe customer
        String stripeCustomerId = createOrUpdateCustomer(user);
        
        // Calculate discount
        int discountPercent = discountService.calculateTotalDiscountPercentage(user);
        String couponId = null;
        if (discountPercent > 0) {
            couponId = getOrCreateCoupon(discountPercent);
        }

        // Create checkout session
        SessionCreateParams.Builder sessionBuilder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .setCustomer(stripeCustomerId)
                .setSuccessUrl(successUrl != null ? successUrl : frontendUrl + "/subscription/success?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(cancelUrl != null ? cancelUrl : frontendUrl + "/subscription/cancel")
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setPrice(plan.getStripePriceId())
                                .setQuantity(1L)
                                .build()
                )
                .setClientReferenceId(user.getId().toString())
                .putMetadata("userId", user.getId().toString())
                .putMetadata("tier", tier);
        
        if (couponId != null) {
            sessionBuilder.addDiscount(
                SessionCreateParams.Discount.builder()
                    .setCoupon(couponId)
                    .build()
            );
        }

        Session session = Session.create(sessionBuilder.build());
        logger.info("Created Stripe checkout session: {} for user: {} with {}% discount", session.getId(), user.getId(), discountPercent);
        
        return CheckoutSessionResponse.builder()
                .sessionId(session.getId())
                .checkoutUrl(session.getUrl())
                .publishableKey(stripePublishableKey)
                .build();
    }

    private String getOrCreateCoupon(int percentOff) throws StripeException {
        String couponId = "DISCOUNT_" + percentOff + "PCT";
        try {
            Coupon coupon = Coupon.retrieve(couponId);
            return coupon.getId();
        } catch (StripeException e) {
            // Create coupon if it doesn't exist
            CouponCreateParams params = CouponCreateParams.builder()
                    .setId(couponId)
                    .setPercentOff(BigDecimal.valueOf(percentOff))
                    .setDuration(CouponCreateParams.Duration.ONCE)
                    .setName(percentOff + "% Off Gamification Reward")
                    .build();
            Coupon coupon = Coupon.create(params);
            return coupon.getId();
        }
    }
    
    /**
     * Handle Stripe webhook events
     * Processes: customer.subscription.created, customer.subscription.updated, customer.subscription.deleted, etc.
     * 
     * @param event Stripe event
     */
    public void handleWebhookEvent(Event event) throws StripeException {
        logger.info("Processing Stripe webhook event: {}", event.getType());
        
        switch (event.getType()) {
            case "checkout.session.completed":
                handleCheckoutSessionCompleted(event);
                break;
            case "customer.subscription.created":
            case "customer.subscription.updated":
                handleSubscriptionUpdated(event);
                break;
            case "customer.subscription.deleted":
                handleSubscriptionDeleted(event);
                break;
            case "invoice.payment_succeeded":
                handleInvoicePaymentSucceeded(event);
                break;
            case "invoice.payment_failed":
                handleInvoicePaymentFailed(event);
                break;
            case "charge.refunded":
                handleChargeRefunded(event);
                break;
            default:
                logger.debug("Unhandled webhook event type: {}", event.getType());
        }
    }
    
    /**
     * Handle checkout session completed event
     */
    private void handleCheckoutSessionCompleted(Event event) throws StripeException {
        logger.info("Handling checkout.session.completed event");
        
        Session session = (Session) event.getDataObjectDeserializer().getObject().orElse(null);
        if (session == null) {
            logger.error("Failed to deserialize checkout session from event");
            return;
        }
        
        String userId = session.getClientReferenceId();
        String subscriptionId = session.getSubscription();
        
        logger.info("Checkout completed for user: {} with subscription: {}", userId, subscriptionId);
        // Subscription will be created through customer.subscription.created event
    }
    
    /**
     * Handle subscription updated/created event
     */
    private void handleSubscriptionUpdated(Event event) throws StripeException {
        logger.info("Handling subscription updated event");
        
        Subscription subscription = (Subscription) event.getDataObjectDeserializer().getObject().orElse(null);
        if (subscription == null) {
            logger.error("Failed to deserialize subscription from event");
            return;
        }
        
        String customerId = subscription.getCustomer();
        String stripePriceId = subscription.getItems().getData().get(0).getPrice().getId();
        
        logger.info("Subscription updated for customer: {} with price: {}", customerId, stripePriceId);
        
        SubscriptionPlan plan = subscriptionPlanRepository.findByStripePriceId(stripePriceId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Stripe price ID: " + stripePriceId));
        
        // Find or create user subscription
        UserSubscription userSubscription = userSubscriptionRepository.findByStripeCustomerId(customerId)
                .orElseGet(() -> {
                    User user = userRepository.findByStripeCustomerId(customerId)
                            .orElseThrow(() -> new IllegalArgumentException("User not found with Stripe Customer ID: " + customerId));
                    return UserSubscription.builder()
                            .user(user)
                            .stripeCustomerId(customerId)
                            .build();
                });
        
        userSubscription.setStripeSubscriptionId(subscription.getId());
        userSubscription.setPlan(plan);
        userSubscription.setStatus(SubscriptionStatus.ACTIVE);
        userSubscription.setStartDate(LocalDateTime.ofInstant(
                subscription.getCurrentPeriodStart() == null ? 
                new java.util.Date().toInstant() : 
                new java.util.Date(subscription.getCurrentPeriodStart() * 1000).toInstant(),
                ZoneId.systemDefault()
        ));
        userSubscription.setRenewalDate(LocalDateTime.ofInstant(
                new java.util.Date(subscription.getCurrentPeriodEnd() * 1000).toInstant(),
                ZoneId.systemDefault()
        ));
        
        userSubscriptionRepository.save(userSubscription);
        
        // Update user subscription tier
        User user = userSubscription.getUser();
        if (user != null) {
            user.setSubscriptionTier(plan.getTier().name());
            userRepository.save(user);
        }
        
        logger.info("Updated subscription for customer: {}", customerId);
    }
    
    /**
     * Handle subscription deleted event
     */
    private void handleSubscriptionDeleted(Event event) throws StripeException {
        logger.info("Handling subscription deleted event");
        
        Subscription subscription = (Subscription) event.getDataObjectDeserializer().getObject().orElse(null);
        if (subscription == null) {
            logger.error("Failed to deserialize subscription from event");
            return;
        }
        
        UserSubscription userSubscription = userSubscriptionRepository.findByStripeSubscriptionId(subscription.getId())
                .orElse(null);
        
        if (userSubscription != null) {
            userSubscription.setStatus(SubscriptionStatus.CANCELLED);
            userSubscription.setEndDate(LocalDateTime.now());
            userSubscriptionRepository.save(userSubscription);
            
            User user = userSubscription.getUser();
            if (user != null) {
                user.setSubscriptionTier("FREE");
                userRepository.save(user);
            }
            
            logger.info("Cancelled subscription for customer: {}", subscription.getCustomer());
        }
    }
    
    /**
     * Handle invoice payment succeeded event
     */
    private void handleInvoicePaymentSucceeded(Event event) throws StripeException {
        logger.info("Handling invoice payment succeeded event");
        
        com.stripe.model.Invoice stripeInvoice = (com.stripe.model.Invoice) event.getDataObjectDeserializer().getObject().orElse(null);
        if (stripeInvoice == null) {
            logger.error("Failed to deserialize invoice from event");
            return;
        }
        
        saveOrUpdateInvoice(stripeInvoice, InvoiceStatus.PAID);
    }
    
    /**
     * Handle invoice payment failed event
     */
    private void handleInvoicePaymentFailed(Event event) throws StripeException {
        logger.info("Handling invoice payment failed event");
        
        com.stripe.model.Invoice stripeInvoice = (com.stripe.model.Invoice) event.getDataObjectDeserializer().getObject().orElse(null);
        if (stripeInvoice == null) {
            logger.error("Failed to deserialize invoice from event");
            return;
        }
        
        saveOrUpdateInvoice(stripeInvoice, InvoiceStatus.OPEN);
    }
    
    /**
     * Handle charge refunded event
     */
    private void handleChargeRefunded(Event event) throws StripeException {
        logger.info("Handling charge refunded event");
        
        Charge charge = (Charge) event.getDataObjectDeserializer().getObject().orElse(null);
        if (charge == null) {
            logger.error("Failed to deserialize charge from event");
            return;
        }
        
        logger.info("Charge refunded: {} for amount: {}", charge.getId(), charge.getAmountRefunded());
    }
    
    /**
     * Save or update invoice from Stripe
     */
    private void saveOrUpdateInvoice(com.stripe.model.Invoice stripeInvoice, InvoiceStatus status) {
        try {
            com.spawnta.subscription.entity.Invoice invoice = invoiceRepository.findByStripeInvoiceId(stripeInvoice.getId())
                    .orElseGet(() -> com.spawnta.subscription.entity.Invoice.builder()
                            .stripeInvoiceId(stripeInvoice.getId())
                            .build());
            
            invoice.setAmount(BigDecimal.valueOf(stripeInvoice.getAmountDue() / 100.0));
            invoice.setStatus(status);
            invoice.setPdfUrl(stripeInvoice.getReceiptNumber());
            invoice.setInvoiceNumber(stripeInvoice.getNumber());
            invoice.setInvoiceDate(LocalDateTime.ofInstant(
                    new Date(stripeInvoice.getCreated() * 1000).toInstant(),
                    ZoneId.systemDefault()
            ));
            
            if (status == InvoiceStatus.PAID && stripeInvoice.getStatusTransitions() != null && stripeInvoice.getStatusTransitions().getPaidAt() != null) {
                invoice.setPaidDate(LocalDateTime.ofInstant(
                        java.time.Instant.ofEpochSecond(stripeInvoice.getStatusTransitions().getPaidAt()),
                        ZoneId.systemDefault()
                ));
            }
            
            invoiceRepository.save(invoice);
            logger.info("Saved invoice: {}", stripeInvoice.getId());
        } catch (Exception e) {
            logger.error("Error saving invoice from Stripe event", e);
        }
    }
    
    /**
     * Cancel user's subscription
     */
    public void cancelSubscription(User user, String reason) throws StripeException {
        logger.info("Cancelling subscription for user: {}", user.getId());
        
        UserSubscription userSubscription = userSubscriptionRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalStateException("User has no active subscription"));
        
        if (userSubscription.getStripeSubscriptionId() != null) {
            SubscriptionCancelParams params = SubscriptionCancelParams.builder()
                    .build();
            
            Subscription.retrieve(userSubscription.getStripeSubscriptionId()).cancel(params);
        }
        
        userSubscription.setStatus(SubscriptionStatus.CANCELLED);
        userSubscription.setCancelReason(reason);
        userSubscription.setEndDate(LocalDateTime.now());
        userSubscriptionRepository.save(userSubscription);
        
        logger.info("Cancelled subscription for user: {}", user.getId());
    }
    
    /**
     * Get invoices for user
     */
    public List<InvoiceDTO> getUserInvoices(User user) {
        List<Invoice> invoices = invoiceRepository.findByUserId(user.getId());
        return invoices.stream()
                .map(this::mapInvoiceToDTO)
                .toList();
    }
    
    /**
     * Map Invoice entity to DTO
     */
    private InvoiceDTO mapInvoiceToDTO(Invoice invoice) {
        return InvoiceDTO.builder()
                .id(invoice.getId())
                .amount(invoice.getAmount())
                .currency(invoice.getCurrency())
                .status(invoice.getStatus().toString())
                .pdfUrl(invoice.getPdfUrl())
                .invoiceNumber(invoice.getInvoiceNumber())
                .invoiceDate(invoice.getInvoiceDate())
                .dueDate(invoice.getDueDate())
                .paidDate(invoice.getPaidDate())
                .build();
    }
}
