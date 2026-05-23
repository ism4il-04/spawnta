package com.spawnta.service;

import com.spawnta.dto.AuthResponse;
import com.spawnta.dto.LoginRequest;
import com.spawnta.dto.SignupRequest;
import com.spawnta.entity.User;
import com.spawnta.repository.UserRepository;
import com.spawnta.security.JwtService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final StringRedisTemplate redisTemplate;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       StringRedisTemplate redisTemplate) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.redisTemplate = redisTemplate;
    }

    public AuthResponse register(SignupRequest request) {
        if (!request.password().equals(request.confirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = new User(
            request.email(),
            passwordEncoder.encode(request.password()),
            request.firstName(),
            request.lastName()
        );

        userRepository.save(user);

        // Generate email verification token
        String verificationToken = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set("email:verification:token:" + verificationToken, user.getEmail(), Duration.ofDays(1));

        // Simulate email sending
        System.out.println("\n=================================================");
        System.out.println("=== EMAIL VERIFICATION LINK FOR " + user.getEmail() + " ===");
        System.out.println("http://localhost:8080/api/auth/verify-email?token=" + verificationToken);
        System.out.println("=================================================\n");

        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());

        return new AuthResponse(
            token,
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getRole().name()
        );
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
            .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        if (!user.isEmailVerified()) {
            throw new IllegalArgumentException("Email not verified yet");
        }

        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());

        return new AuthResponse(
            token,
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getRole().name()
        );
    }

    public void verifyEmail(String token) {
        String key = "email:verification:token:" + token;
        String email = redisTemplate.opsForValue().get(key);
        if (email == null) {
            throw new IllegalArgumentException("Invalid or expired verification token");
        }

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setEmailVerified(true);
        userRepository.save(user);

        redisTemplate.delete(key);
    }

    public String createRefreshToken(String email) {
        String refreshToken = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set("refresh:token:" + refreshToken, email, Duration.ofDays(7));
        return refreshToken;
    }

    public AuthResponse refresh(String refreshToken) {
        String key = "refresh:token:" + refreshToken;
        String email = redisTemplate.opsForValue().get(key);
        if (email == null) {
            throw new IllegalArgumentException("Invalid or expired refresh token");
        }

        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.isBanned()) {
            throw new IllegalArgumentException("User is banned");
        }

        String newAccessToken = jwtService.generateToken(user.getEmail(), user.getRole().name());

        return new AuthResponse(
            newAccessToken,
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getRole().name()
        );
    }

    public void logout(String refreshToken, String authHeader) {
        redisTemplate.delete("refresh:token:" + refreshToken);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            if (jwtService.isValid(token)) {
                redisTemplate.opsForValue().set("token:blacklist:" + token, "true", Duration.ofHours(1));
            }
        }
    }
}
