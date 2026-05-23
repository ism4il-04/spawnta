package com.spawnta.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    /** Null when spring.mail.host is not configured (dev mode). */
    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${app.frontend-url:http://localhost:4200}")
    private String frontendUrl;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${spring.mail.username:noreply@spawnta.com}")
    private String fromEmail;

    public void sendVerificationEmail(String toEmail, String token) {
        String link = frontendUrl + "/verify-email?token=" + token;

        if (mailSender == null || mailHost.isBlank()) {
            log.info("=== [DEV MODE] Verification link for {}: {} ===", toEmail, link);
            return;
        }

        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(fromEmail);
            msg.setTo(toEmail);
            msg.setSubject("Verify your Spawnta account");
            msg.setText(
                "Welcome to Spawnta!\n\n" +
                "Click the link below to verify your email address:\n" +
                link + "\n\n" +
                "This link is valid for 24 hours.\n\n" +
                "If you did not create an account, you can ignore this email."
            );
            mailSender.send(msg);
        } catch (Exception e) {
            log.error("Failed to send verification email to {}: {}", toEmail, e.getMessage());
            log.info("=== [FALLBACK] Verification link for {}: {} ===", toEmail, link);
        }
    }
}
