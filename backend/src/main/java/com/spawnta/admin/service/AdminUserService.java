package com.spawnta.admin.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.spawnta.admin.dto.AdminUserDTO;
import com.spawnta.admin.dto.AdminUsersResponseDTO;
import com.spawnta.admin.dto.AdminUsersSummaryDTO;
import com.spawnta.admin.dto.ModerateUserRequest;
import com.spawnta.admin.entity.AdminAuditLog;
import com.spawnta.admin.repository.AdminAuditLogRepository;
import com.spawnta.entity.Role;
import com.spawnta.entity.User;
import com.spawnta.repository.UserRepository;

@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final AdminAuditLogRepository auditLogRepository;

    public AdminUserService(UserRepository userRepository, AdminAuditLogRepository auditLogRepository) {
        this.userRepository = userRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(readOnly = true)
    public AdminUsersResponseDTO listUsers(String search, String status, String tier) {
        String normalizedSearch = normalize(search);
        String normalizedStatus = normalize(status);
        String normalizedTier = normalize(tier);
        LocalDateTime now = LocalDateTime.now();

        List<User> filteredUsers = userRepository.findAll().stream()
                .filter(user -> matchesSearch(user, normalizedSearch))
                .filter(user -> matchesStatus(user, normalizedStatus, now))
                .filter(user -> matchesTier(user, normalizedTier))
                .sorted(Comparator.comparing(User::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        return new AdminUsersResponseDTO(
                filteredUsers.stream().map(this::toDto).toList(),
                buildSummary(userRepository.findAll(), now)
        );
    }

    @Transactional
    public AdminUserDTO updateRole(Long userId, Role newRole, String adminEmail) {
        User admin = requireAdmin(adminEmail);
        User target = requireTarget(userId);
        ensureNotSelf(admin, target, "change role of");

        target.setRole(newRole);
        User saved = userRepository.save(target);
        audit(admin, "UPDATE_ROLE", target, "Role changed to " + newRole);
        return toDto(saved);
    }

    @Transactional
    public AdminUserDTO banUser(Long userId, ModerateUserRequest request, String adminEmail) {
        User admin = requireAdmin(adminEmail);
        User target = requireTarget(userId);
        ensureNotSelf(admin, target, "ban");

        target.setBanned(true);
        target.setSuspendedUntil(null);
        target.setSuspensionReason(cleanReason(request.reason(), "Banned by admin"));
        User saved = userRepository.save(target);
        audit(admin, "BAN_USER", target, saved.getSuspensionReason());
        return toDto(saved);
    }

    @Transactional
    public AdminUserDTO suspendUser(Long userId, ModerateUserRequest request, String adminEmail) {
        User admin = requireAdmin(adminEmail);
        User target = requireTarget(userId);
        ensureNotSelf(admin, target, "suspend");

        int days = request.days() == null || request.days() < 1 ? 7 : request.days();
        target.setBanned(false);
        target.setSuspendedUntil(LocalDateTime.now().plusDays(days));
        target.setSuspensionReason(cleanReason(request.reason(), "Suspended by admin"));
        User saved = userRepository.save(target);
        audit(admin, "SUSPEND_USER", target, saved.getSuspensionReason() + " (" + days + " days)");
        return toDto(saved);
    }

    @Transactional
    public AdminUserDTO restoreUser(Long userId, String adminEmail) {
        User admin = requireAdmin(adminEmail);
        User target = requireTarget(userId);

        target.setBanned(false);
        target.setSuspendedUntil(null);
        target.setSuspensionReason(null);
        User saved = userRepository.save(target);
        audit(admin, "RESTORE_USER", target, "User restored");
        return toDto(saved);
    }

    private AdminUsersSummaryDTO buildSummary(List<User> users, LocalDateTime now) {
        return new AdminUsersSummaryDTO(
                users.size(),
                users.stream().filter(user -> Role.ADMIN.equals(user.getRole())).count(),
                users.stream().filter(User::isPremium).count(),
                users.stream().filter(User::isBanned).count(),
                users.stream().filter(user -> isSuspended(user, now)).count(),
                users.stream().filter(user -> !user.isEmailVerified()).count()
        );
    }

    private AdminUserDTO toDto(User user) {
        return new AdminUserDTO(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole(),
                user.isEmailVerified(),
                user.getSubscriptionTier(),
                user.isPremium(),
                user.isBanned(),
                user.getSuspendedUntil(),
                user.getSuspensionReason(),
                user.getLevel(),
                user.getXp(),
                user.getCreatedAt()
        );
    }

    private boolean matchesSearch(User user, String search) {
        if (search == null || search.isBlank()) {
            return true;
        }
        String haystack = (user.getEmail() + " " + user.getFirstName() + " " + user.getLastName()).toLowerCase(Locale.ROOT);
        return haystack.contains(search);
    }

    private boolean matchesStatus(User user, String status, LocalDateTime now) {
        if (status == null || status.isBlank() || "all".equals(status)) {
            return true;
        }
        return switch (status) {
            case "active" -> !user.isBanned() && !isSuspended(user, now);
            case "banned" -> user.isBanned();
            case "suspended" -> isSuspended(user, now);
            case "unverified" -> !user.isEmailVerified();
            case "admin" -> Role.ADMIN.equals(user.getRole());
            default -> true;
        };
    }

    private boolean matchesTier(User user, String tier) {
        if (tier == null || tier.isBlank() || "all".equals(tier)) {
            return true;
        }
        return tier.equals(normalize(user.getSubscriptionTier()));
    }

    private boolean isSuspended(User user, LocalDateTime now) {
        return user.getSuspendedUntil() != null && user.getSuspendedUntil().isAfter(now);
    }

    private User requireAdmin(String email) {
        User admin = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Admin user not found"));
        if (!Role.ADMIN.equals(admin.getRole())) {
            throw new IllegalArgumentException("User is not an admin");
        }
        return admin;
    }

    private User requireTarget(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Target user not found"));
    }

    private void ensureNotSelf(User admin, User target, String action) {
        if (admin.getId().equals(target.getId())) {
            throw new IllegalArgumentException("Admins cannot " + action + " their own account");
        }
    }

    private void audit(User admin, String action, User target, String details) {
        auditLogRepository.save(AdminAuditLog.builder()
                .admin(admin)
                .action(action)
                .targetType("USER")
                .targetId(target.getId())
                .details(details)
                .build());
    }

    private String cleanReason(String reason, String fallback) {
        return reason == null || reason.isBlank() ? fallback : reason.trim();
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toLowerCase(Locale.ROOT);
    }
}
