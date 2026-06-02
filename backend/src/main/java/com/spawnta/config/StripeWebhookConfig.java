package com.spawnta.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StripeWebhookConfig {
    
    @Value("${stripe.webhook.secret}")
    private String webhookSecret;
    
    @Bean
    public String stripeWebhookSecret() {
        return webhookSecret;
    }
}
