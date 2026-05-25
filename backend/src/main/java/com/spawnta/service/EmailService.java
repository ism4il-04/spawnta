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

    @Value("${spring.mail.port:587}")
    private String mailPort;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    // Log configuration at startup
    @jakarta.annotation.PostConstruct
    public void logConfiguration() {
        System.out.println("=== EMAIL SERVICE CONFIGURATION ===");
        System.out.println("Mail Host: '" + mailHost + "'");
        System.out.println("Mail Port: '" + mailPort + "'");
        System.out.println("Mail Username: '" + fromEmail + "'");
        System.out.println("Mail Password Set: " + (!mailPassword.isEmpty()));
        System.out.println("Mail Sender Available: " + (mailSender != null));
        System.out.println("Frontend URL: " + frontendUrl);
        System.out.println("=====================================");
    }

    public void sendVerificationEmail(String toEmail, String token) {
        String link = frontendUrl + "/verify-email?token=" + token;

        System.out.println("=== EMAIL SERVICE DEBUG ===");
        System.out.println("Mail Host: '" + mailHost + "'");
        System.out.println("Mail Port: '" + mailPort + "'");
        System.out.println("From Email: '" + fromEmail + "'");
        System.out.println("To Email: '" + toEmail + "'");
        System.out.println("Mail Password Set: " + (!mailPassword.isEmpty()));
        System.out.println("Verification Link: " + link);
        System.out.println("Mail Sender Available: " + (mailSender != null));
        System.out.println("Mail Host is blank: " + mailHost.isBlank());

        if (mailSender == null || mailHost.isBlank()) {
            System.out.println("=== ENTERING DEV MODE ===");
            log.info("=== [DEV MODE] Verification link for {}: {} ===", toEmail, link);
            return;
        }

        try {
            System.out.println("=== ATTEMPTING TO SEND EMAIL ===");
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
            
            System.out.println("Sending email...");
            System.out.println("From: " + msg.getFrom());
            System.out.println("To: " + msg.getTo());
            System.out.println("Subject: " + msg.getSubject());
            
            mailSender.send(msg);
            System.out.println("Email sent successfully!");
            log.info("Verification email sent successfully to {}", toEmail);
        } catch (org.springframework.mail.MailAuthenticationException e) {
            System.err.println("=== AUTHENTICATION ERROR ===");
            System.err.println("Gmail authentication failed. Check:");
            System.err.println("1. 2FA is enabled on Gmail account");
            System.err.println("2. App password is correct (16 chars, no spaces)");
            System.err.println("3. App password is recent (not expired)");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            log.error("Gmail authentication failed for {}: {}", toEmail, e.getMessage());
            log.info("=== [FALLBACK] Verification link for {}: {} ===", toEmail, link);
        } catch (org.springframework.mail.MailSendException e) {
            System.err.println("=== MAIL SEND ERROR ===");
            System.err.println("Failed to send email. Check:");
            System.err.println("1. Internet connection");
            System.err.println("2. SMTP server availability");
            System.err.println("3. Recipient email address");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage());
            log.info("=== [FALLBACK] Verification link for {}: {} ===", toEmail, link);
        } catch (Exception e) {
            System.err.println("=== GENERIC EMAIL ERROR ===");
            System.err.println("Unexpected error: " + e.getClass().getSimpleName());
            System.err.println("Message: " + e.getMessage());
            e.printStackTrace();
            log.error("Failed to send verification email to {}: {}", toEmail, e.getMessage());
            log.info("=== [FALLBACK] Verification link for {}: {} ===", toEmail, link);
        }
    }
}
