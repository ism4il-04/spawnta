package com.spawnta.service;

import com.spawnta.dto.AuthResponse;
import com.spawnta.dto.LoginRequest;
import com.spawnta.dto.SignupRequest;
import com.spawnta.entity.User;
import com.spawnta.exception.EmailNotVerifiedException;
import com.spawnta.repository.UserRepository;
import com.spawnta.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final EmailService emailService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       RefreshTokenService refreshTokenService,
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.emailService = emailService;
    }

    public void signup(SignupRequest request) {
        System.out.println("=== AuthService.signup() START ===");
        System.out.println("Checking if email exists: " + request.email());
        
        if (userRepository.existsByEmail(request.email())) {
            System.err.println("Email already exists: " + request.email());
            throw new IllegalArgumentException("Email already registered");
        }

        System.out.println("Creating new user...");
        User user = new User(
            request.email(),
            passwordEncoder.encode(request.password()),
            request.firstName(),
            request.lastName()
        );

        String token = UUID.randomUUID().toString();
        user.setEmailVerified(false);
        user.setVerificationToken(token);
        
        System.out.println("Saving user to database...");
        try {
            userRepository.save(user);
            System.out.println("User saved successfully with ID: " + user.getId());
        } catch (Exception e) {
            System.err.println("Error saving user: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }

        System.out.println("Sending verification email...");
        try {
            emailService.sendVerificationEmail(user.getEmail(), token);
            System.out.println("Verification email sent successfully");
        } catch (Exception e) {
            System.err.println("Error sending email: " + e.getMessage());
            e.printStackTrace();
            // Don't throw here - user is already created
        }
        
        System.out.println("=== AuthService.signup() END ===");
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        if (!user.isEmailVerified()) {
            throw new EmailNotVerifiedException("Please verify your email before logging in", user.getEmail());
        }

        return buildAuthResponse(user);
    }

    public AuthResponse verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
            .orElseThrow(() -> new IllegalArgumentException("Invalid or expired verification token"));

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        userRepository.save(user);

        return buildAuthResponse(user);
    }

    public void resendVerification(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.isEmailVerified()) {
            throw new IllegalArgumentException("Email is already verified");
        }

        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        userRepository.save(user);

        emailService.sendVerificationEmail(email, token);
    }

    public AuthResponse refresh(String refreshToken) {
        String email = refreshTokenService.validateAndGetEmail(refreshToken);
        // Rotate: revoke old, issue new pair
        refreshTokenService.revoke(refreshToken);
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return buildAuthResponse(user);
    }

    public void logout(String refreshToken) {
        refreshTokenService.revoke(refreshToken);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private AuthResponse buildAuthResponse(User user) {
        String accessToken  = jwtService.generateToken(user.getEmail(), user.getRole().name());
        String refreshToken = refreshTokenService.createToken(user.getEmail());
        return new AuthResponse(
            accessToken,
            refreshToken,
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getRole().name()
        );
    }
}
