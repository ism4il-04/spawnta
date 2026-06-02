package com.spawnta.subscription.controller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;

import com.spawnta.entity.User;
import com.spawnta.repository.UserRepository;
import com.spawnta.subscription.dto.*;
import com.spawnta.subscription.entity.UserSubscription;
import com.spawnta.subscription.repository.SubscriptionPlanRepository;
import com.spawnta.subscription.repository.UserSubscriptionRepository;
import com.spawnta.subscription.service.StripeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * REST Controller for subscription management
 */
@RestController
@RequestMapping("/api/subscription")
@Tag(name = "Subscription", description = "User subscription and billing management")
public class SubscriptionController {
    
    private static final Logger logger = LoggerFactory.getLogger(SubscriptionController.class);
    
    private final StripeService stripeService;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final UserRepository userRepository;
    
    @org.springframework.beans.factory.annotation.Value("${stripe.api.secretKey}")
    private String stripeSecretKey;
    
    public SubscriptionController(
            StripeService stripeService,
            SubscriptionPlanRepository subscriptionPlanRepository,
            UserSubscriptionRepository userSubscriptionRepository,
            UserRepository userRepository
    ) {
        this.stripeService = stripeService;
        this.subscriptionPlanRepository = subscriptionPlanRepository;
        this.userSubscriptionRepository = userSubscriptionRepository;
        this.userRepository = userRepository;
    }
    
    /**
     * Get all available subscription plans
     */
    @GetMapping("/plans")
    @Operation(summary = "Get all subscription plans")
    public ResponseEntity<List<SubscriptionPlanDTO>> getPlans() {
        List<SubscriptionPlanDTO> plans = subscriptionPlanRepository.findAll()
                .stream()
                .map(this::mapPlanToDTO)
                .toList();
        return ResponseEntity.ok(plans);
    }
    
    /**
     * Get user's current subscription
     */
    @GetMapping("/current")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get current user subscription")
    public ResponseEntity<UserSubscriptionDTO> getCurrentSubscription(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        UserSubscription subscription = userSubscriptionRepository.findByUserId(user.getId())
                .orElse(null);
        
        if (subscription == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(mapSubscriptionToDTO(subscription));
    }
    
    /**
     * Initiate subscription upgrade
     */
    @PostMapping("/upgrade")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Upgrade subscription tier")
    public ResponseEntity<?> upgradeSubscription(
            @Valid @RequestBody UpgradeSubscriptionRequest request,
            Authentication authentication
    ) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            CheckoutSessionResponse response = stripeService.createCheckoutSession(
                    user,
                    request.getTier(),
                    request.getSuccessUrl(),
                    request.getCancelUrl()
            );
            
            return ResponseEntity.ok(response);
        } catch (StripeException e) {
            logger.error("Stripe error during upgrade: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to create checkout session: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Error during upgrade: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An error occurred: " + e.getMessage()));
        }
    }
    
    /**
     * Cancel subscription
     */
    @PostMapping("/cancel")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Cancel user subscription")
    public ResponseEntity<?> cancelSubscription(
            @Valid @RequestBody CancelSubscriptionRequest request,
            Authentication authentication
    ) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            
            stripeService.cancelSubscription(user, request.getReason());
            
            return ResponseEntity.ok(Map.of("message", "Subscription cancelled successfully"));
        } catch (StripeException e) {
            logger.error("Stripe error during cancellation: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Failed to cancel subscription: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Error during cancellation: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "An error occurred: " + e.getMessage()));
        }
    }
    
    /**
     * Get user's invoices
     */
    @GetMapping("/invoices")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get user invoices")
    public ResponseEntity<List<InvoiceDTO>> getInvoices(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        
        List<InvoiceDTO> invoices = stripeService.getUserInvoices(user);
        return ResponseEntity.ok(invoices);
    }
    
    /**
     * Stripe webhook endpoint for handling Stripe events
     */
    @PostMapping("/webhook")
    @Operation(summary = "Stripe webhook receiver")
    public ResponseEntity<?> handleStripeWebhook(HttpServletRequest request) {
        try {
            String body = new String(request.getInputStream().readAllBytes());
            String sigHeader = request.getHeader("Stripe-Signature");
            
            Event event = Webhook.constructEvent(body, sigHeader, stripeSecretKey);
            
            stripeService.handleWebhookEvent(event);
            
            return ResponseEntity.ok(Map.of("message", "Webhook processed"));
        } catch (Exception e) {
            logger.error("Error processing Stripe webhook: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid webhook signature"));
        }
    }
    
    /**
     * Map SubscriptionPlan entity to DTO
     */
    private SubscriptionPlanDTO mapPlanToDTO(com.spawnta.subscription.entity.SubscriptionPlan plan) {
        return SubscriptionPlanDTO.builder()
                .id(plan.getId())
                .tier(plan.getTier().getId())
                .name(plan.getName())
                .description(plan.getDescription())
                .monthlyPrice(plan.getMonthlyPrice())
                .features(plan.getFeatures())
                .build();
    }
    
    /**
     * Map UserSubscription entity to DTO
     */
    private UserSubscriptionDTO mapSubscriptionToDTO(UserSubscription subscription) {
        return UserSubscriptionDTO.builder()
                .id(subscription.getId())
                .plan(mapPlanToDTO(subscription.getPlan()))
                .status(subscription.getStatus().toString())
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate())
                .renewalDate(subscription.getRenewalDate())
                .isActive(subscription.isActive())
                .build();
    }
}
