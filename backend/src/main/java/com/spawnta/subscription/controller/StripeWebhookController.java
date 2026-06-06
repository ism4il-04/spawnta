package com.spawnta.subscription.controller;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Charge;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.model.Invoice;
import com.stripe.model.Subscription;
import com.stripe.net.Webhook;
import com.spawnta.subscription.service.StripeWebhookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling Stripe webhook events
 * Receives notifications from Stripe about payment events, subscription changes, etc.
 */
@RestController
@RequestMapping("/api/subscription/webhook")
public class StripeWebhookController {

    private static final Logger logger = LoggerFactory.getLogger(StripeWebhookController.class);

    private final StripeWebhookService webhookService;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    public StripeWebhookController(StripeWebhookService webhookService) {
        this.webhookService = webhookService;
    }

    /**
     * Handle incoming webhook from Stripe
     * @param payload Raw JSON payload from Stripe
     * @param sigHeader Stripe-Signature header for verification
     * @return 200 OK if processed successfully, 400 if verification fails
     */
    @PostMapping
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader
    ) {
        logger.info("Received Stripe webhook");

        Event event;

        try {
            // Verify webhook signature to ensure it's from Stripe
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            logger.error("⚠️ Webhook signature verification failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid signature");
        }

        logger.info("✅ Webhook verified. Event type: {}", event.getType());

        // Handle the event based on its type
        try {
            handleEvent(event);
            return ResponseEntity.ok("Webhook processed successfully");
        } catch (Exception e) {
            logger.error("❌ Error processing webhook event: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error processing webhook");
        }
    }

    /**
     * Route event to appropriate handler based on event type
     */
    private void handleEvent(Event event) {
        StripeObject stripeObject = null;

        // 1. Primary Method: Standard SDK Deserializer
        try {
            stripeObject = event.getDataObjectDeserializer().getObject().orElse(null);
        } catch (Exception e) {
            logger.warn("⚠️ Standard deserialization failed for {}: {}", event.getType(), e.getMessage());
        }

        // 2. Fallback Method: Manual Extraction from Data Object (Most reliable for version mismatches)
        if (stripeObject == null) {
            try {
                stripeObject = event.getData().getObject();
                if (stripeObject != null) {
                    logger.info("�️ Extracted object manually from event data");
                }
            } catch (Exception e) {
                logger.error("❌ All deserialization attempts failed for event: {}", event.getType());
                return;
            }
        }

        if (stripeObject == null) {
            logger.error("❌ Failed to extract Stripe object for event: {}", event.getType());
            return;
        }

        logger.info("📦 Handling event [{}] with object type: {}", event.getType(), stripeObject.getClass().getSimpleName());

        // Handle different event types
        switch (event.getType()) {
            case "checkout.session.completed":
                if (stripeObject instanceof Session) {
                    webhookService.handleCheckoutCompleted((Session) stripeObject);
                }
                break;

            case "customer.subscription.created":
                if (stripeObject instanceof Subscription) {
                    webhookService.handleSubscriptionCreated((Subscription) stripeObject);
                }
                break;

            case "invoice.payment_succeeded":
            case "invoice.paid":
            case "invoice_payment.paid":
                if (stripeObject instanceof Invoice) {
                    webhookService.handleInvoicePaymentSucceeded((Invoice) stripeObject);
                }
                break;

            case "invoice.payment_failed":
                if (stripeObject instanceof Invoice) {
                    webhookService.handleInvoicePaymentFailed((Invoice) stripeObject);
                }
                break;

            case "customer.subscription.updated":
                if (stripeObject instanceof Subscription) {
                    webhookService.handleSubscriptionUpdated((Subscription) stripeObject);
                }
                break;

            case "customer.subscription.deleted":
                if (stripeObject instanceof Subscription) {
                    webhookService.handleSubscriptionDeleted((Subscription) stripeObject);
                }
                break;

            case "charge.refunded":
                if (stripeObject instanceof Charge) {
                    webhookService.handleChargeRefunded((Charge) stripeObject);
                }
                break;

            default:
                logger.info("Unhandled event type: {}", event.getType());
        }
    }
}
