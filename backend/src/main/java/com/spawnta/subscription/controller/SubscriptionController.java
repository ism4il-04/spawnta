package com.spawnta.subscription.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.stripe.exception.StripeException;

import com.spawnta.entity.User;
import com.spawnta.repository.UserRepository;
import com.spawnta.subscription.dto.*;
import com.spawnta.subscription.entity.SubscriptionPlan;
import com.spawnta.subscription.entity.UserSubscription;
import com.spawnta.subscription.repository.SubscriptionPlanRepository;
import com.spawnta.subscription.repository.UserSubscriptionRepository;
import com.spawnta.subscription.service.StripeService;
import com.spawnta.subscription.service.SubscriptionDiscountService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
    private final SubscriptionDiscountService discountService;
    
    public SubscriptionController(
            StripeService stripeService,
            SubscriptionPlanRepository subscriptionPlanRepository,
            UserSubscriptionRepository userSubscriptionRepository,
            UserRepository userRepository,
            SubscriptionDiscountService discountService
    ) {
        this.stripeService = stripeService;
        this.subscriptionPlanRepository = subscriptionPlanRepository;
        this.userSubscriptionRepository = userSubscriptionRepository;
        this.userRepository = userRepository;
        this.discountService = discountService;
    }
    
    /**
     * Get all available subscription plans
     */
    @GetMapping("/plans")
    @Operation(summary = "Get all subscription plans")
    public ResponseEntity<List<SubscriptionPlanDTO>> getPlans(Authentication authentication) {
        User user = null;
        if (authentication != null && authentication.isAuthenticated()) {
            user = userRepository.findByEmail(authentication.getName()).orElse(null);
        }

        final User finalUser = user;
        List<SubscriptionPlanDTO> plans = subscriptionPlanRepository.findAll()
                .stream()
                .map(plan -> mapPlanToDTO(plan, finalUser))
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
            // Return a default "FREE" subscription DTO if none exists
            SubscriptionPlan freePlan = subscriptionPlanRepository.findByTier(com.spawnta.subscription.entity.SubscriptionTier.FREE)
                    .orElse(null);
            
            return ResponseEntity.ok(UserSubscriptionDTO.builder()
                    .plan(freePlan != null ? mapPlanToDTO(freePlan, user) : null)
                    .status("ACTIVE")
                    .build());
        }
        
        return ResponseEntity.ok(mapSubscriptionToDTO(subscription, user));
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
            
            // Check if user already has an active subscription
            UserSubscription existingSub = userSubscriptionRepository.findByUserIdAndStatus(user.getId(), com.spawnta.subscription.entity.SubscriptionStatus.ACTIVE)
                    .orElse(null);
            
            if (existingSub != null) {
                // Check if they are trying to "upgrade" to the same tier
                if (existingSub.getPlan().getTier().getId().equalsIgnoreCase(request.getTier())) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(Map.of("error", "Vous avez deja un abonnement actif pour ce niveau."));
                }
                
                // If they want to change, they should ideally cancel the old one or we handle it in Stripe
                // For now, let's ask them to cancel or handle it by informing them.
                logger.info("User {} already has an active subscription ({}). Processing as potential change.", 
                    user.getEmail(), existingSub.getPlan().getTier());
            }
            
            CheckoutSessionResponse response = stripeService.createCheckoutSession(
                    user,
                    request.getTier(),
                    request.getSuccessUrl(),
                    request.getCancelUrl()
            );
            
            return ResponseEntity.ok(response);
        } catch (StripeException e) {
            logger.error("Stripe error during upgrade: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Stripe configuration error: " + e.getMessage()));
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Stripe configuration error: " + e.getMessage()));
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
     * Map SubscriptionPlan entity to DTO
     */
    private SubscriptionPlanDTO mapPlanToDTO(SubscriptionPlan plan, User user) {
        BigDecimal discountedPrice = plan.getMonthlyPrice();
        String discountReason = null;

        if (user != null && plan.getMonthlyPrice().compareTo(BigDecimal.ZERO) > 0) {
            int discountPercent = discountService.calculateTotalDiscountPercentage(user);
            if (discountPercent > 0) {
                BigDecimal discountAmount = plan.getMonthlyPrice()
                        .multiply(BigDecimal.valueOf(discountPercent))
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                discountedPrice = plan.getMonthlyPrice().subtract(discountAmount);
                discountReason = discountService.getDiscountReason(user);
            }
        }

        return SubscriptionPlanDTO.builder()
                .id(plan.getId())
                .tier(plan.getTier().getId())
                .name(plan.getName())
                .description(plan.getDescription())
                .monthlyPrice(plan.getMonthlyPrice())
                .discountedPrice(discountedPrice)
                .discountReason(discountReason)
                .features(plan.getFeatures())
                .build();
    }
    
    /**
     * Map UserSubscription entity to DTO
     */
    private UserSubscriptionDTO mapSubscriptionToDTO(UserSubscription subscription, User user) {
        return UserSubscriptionDTO.builder()
                .id(subscription.getId())
                .plan(mapPlanToDTO(subscription.getPlan(), user))
                .status(subscription.getStatus().name())
                .startDate(subscription.getStartDate())
                .endDate(subscription.getEndDate())
                .renewalDate(subscription.getRenewalDate())
                .isActive(subscription.isActive())
                .build();
    }
}
