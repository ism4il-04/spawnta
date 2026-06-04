package com.spawnta.controller;

import com.spawnta.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Autowired
    private EmailService emailService;

    @Value("${spring.mail.host:}")
    private String mailHost;

    @Value("${spring.mail.port:}")
    private String mailPort;

    @Value("${spring.mail.username:}")
    private String mailUsername;

    @Value("${spring.mail.password:}")
    private String mailPassword;

    @GetMapping("/email-config")
    public ResponseEntity<Map<String, Object>> getEmailConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("mailHost", mailHost);
        config.put("mailPort", mailPort);
        config.put("mailUsername", mailUsername);
        config.put("mailPasswordSet", !mailPassword.isEmpty());
        config.put("mailSenderAvailable", mailSender != null);
        config.put("mailHostBlank", mailHost.isBlank());
        
        return ResponseEntity.ok(config);
    }

    @PostMapping("/send-test-email")
    public ResponseEntity<Map<String, String>> sendTestEmail(@RequestParam String email) {
        try {
            emailService.sendVerificationEmail(email, "test-token-123");
            return ResponseEntity.ok(Map.of("message", "Test email sent to " + email));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}