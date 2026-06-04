package com.spawnta.admin.service;

import com.spawnta.admin.dto.AdminDashboardDTO;
import com.spawnta.admin.dto.AdminSubscriptionsDTO;
import com.spawnta.admin.entity.AdminAuditLog;
import com.spawnta.admin.repository.AdminAuditLogRepository;
import com.spawnta.entity.Role;
import com.spawnta.entity.User;
import com.spawnta.moderation.entity.ReportStatus;
import com.spawnta.moderation.repository.ActivityReportRepository;
import com.spawnta.moderation.repository.UserReportRepository;
import com.spawnta.repository.ActivityRepository;
import com.spawnta.repository.UserRepository;
import com.spawnta.subscription.entity.PaymentStatus;
import com.spawnta.subscription.entity.SubscriptionStatus;
import com.spawnta.subscription.entity.UserSubscription;
import com.spawnta.subscription.repository.PaymentTransactionRepository;
import com.spawnta.subscription.repository.SubscriptionPlanRepository;
import com.spawnta.subscription.repository.UserSubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;
    private final UserReportRepository userReportRepository;
    private final ActivityReportRepository activityReportRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final AdminAuditLogRepository auditLogRepository;

    public AdminDashboardService(
            UserRepository userRepository,
            ActivityRepository activityRepository,
            UserReportRepository userReportRepository,
            ActivityReportRepository activityReportRepository,
            SubscriptionPlanRepository subscriptionPlanRepository,
            UserSubscriptionRepository userSubscriptionRepository,
            PaymentTransactionRepository paymentTransactionRepository,
            AdminAuditLogRepository auditLogRepository) {
        this.userRepository = userRepository;
        this.activityRepository = activityRepository;
        this.userReportRepository = userReportRepository;
        this.activityReportRepository = activityReportRepository;
        this.subscriptionPlanRepository = subscriptionPlanRepository;
        this.userSubscriptionRepository = userSubscriptionRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(readOnly = true)
    public AdminDashboardDTO getDashboard() {
        List<User> users = userRepository.findAll();
        LocalDateTime now = LocalDateTime.now();
        return new AdminDashboardDTO(
            users.size(),
            users.stream().filter(user -> Role.ADMIN.equals(user.getRole())).count(),
            users.stream().filter(User::isPremium).count(),
            users.stream().filter(User::isBanned).count(),
            users.stream().filter(user -> user.getSuspendedUntil() != null && user.getSuspendedUntil().isAfter(now)).count(),
            users.stream().filter(user -> !user.isEmailVerified()).count(),
            activityRepository.count(),
            activityRepository.findAllByScheduledAtAfter(now).size(),
            userReportRepository.countByStatus(ReportStatus.OPEN),
            activityReportRepository.countByStatus(ReportStatus.OPEN),
            subscriptionPlanRepository.count(),
            userSubscriptionRepository.findByStatus(SubscriptionStatus.ACTIVE).size(),
            successfulPaymentsTotal(),
            recentAuditLogs(),
            recentActivities()
        );
    }

    @Transactional(readOnly = true)
    public AdminSubscriptionsDTO getSubscriptions() {
        List<UserSubscription> subscriptions = userSubscriptionRepository.findAll();
        return new AdminSubscriptionsDTO(
            subscriptionPlanRepository.count(),
            subscriptions.stream().filter(subscription -> SubscriptionStatus.ACTIVE.equals(subscription.getStatus())).count(),
            subscriptions.stream().filter(subscription -> SubscriptionStatus.PENDING.equals(subscription.getStatus())).count(),
            subscriptions.stream().filter(subscription -> SubscriptionStatus.PAST_DUE.equals(subscription.getStatus())).count(),
            subscriptions.stream().filter(subscription -> SubscriptionStatus.CANCELLED.equals(subscription.getStatus())).count(),
            successfulPaymentsTotal(),
            subscriptionPlanRepository.findAll().stream()
                .sorted(Comparator.comparing(plan -> plan.getTier().name()))
                .map(plan -> new AdminSubscriptionsDTO.PlanDTO(
                    plan.getId(),
                    plan.getTier().name(),
                    plan.getName(),
                    plan.getDescription(),
                    plan.getMonthlyPrice(),
                    plan.getFeatures()
                ))
                .toList(),
            subscriptions.stream()
                .sorted(Comparator.comparing(UserSubscription::getCreatedAt).reversed())
                .map(subscription -> {
                    User user = subscription.getUser();
                    return new AdminSubscriptionsDTO.UserSubscriptionAdminDTO(
                        subscription.getId(),
                        user.getId(),
                        user.getEmail(),
                        user.getFirstName() + " " + user.getLastName(),
                        subscription.getPlan().getTier().name(),
                        subscription.getPlan().getName(),
                        subscription.getStatus().name(),
                        subscription.getStartDate(),
                        subscription.getRenewalDate(),
                        subscription.getEndDate()
                    );
                })
                .toList()
        );
    }

    private BigDecimal successfulPaymentsTotal() {
        return paymentTransactionRepository.findByStatus(PaymentStatus.SUCCEEDED).stream()
            .map(transaction -> transaction.getAmount() == null ? BigDecimal.ZERO : transaction.getAmount())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<AdminDashboardDTO.PlatformActivityDTO> recentActivities() {
        List<AdminDashboardDTO.PlatformActivityDTO> activities = new ArrayList<>();

        // Recent User Joins
        userRepository.findAll().stream()
            .sorted(Comparator.comparing(User::getCreatedAt).reversed())
            .limit(5)
            .forEach(u -> activities.add(new AdminDashboardDTO.PlatformActivityDTO(
                "USER_JOIN",
                u.getFirstName() + " " + u.getLastName() + " rejoint",
                u.getEmail() + " a rejoint Spawnta",
                "users",
                u.getCreatedAt()
            )));

        // Recent Activities Created
        activityRepository.findAll().stream()
            .sorted(Comparator.comparing(com.spawnta.entity.Activity::getCreatedAt).reversed())
            .limit(5)
            .forEach(a -> activities.add(new AdminDashboardDTO.PlatformActivityDTO(
                "ACTIVITY_CREATE",
                "Nouvelle activité: " + a.getTitle(),
                "Créée par " + a.getHost().getFirstName(),
                "calendar",
                a.getCreatedAt()
            )));

        // Recent User Reports
        userReportRepository.findAll().stream()
            .sorted(Comparator.comparing(com.spawnta.moderation.entity.UserReport::getCreatedAt).reversed())
            .limit(5)
            .forEach(r -> activities.add(new AdminDashboardDTO.PlatformActivityDTO(
                "NEW_REPORT",
                "Signalement utilisateur",
                "Raison: " + r.getReason(),
                "shield",
                r.getCreatedAt()
            )));

        return activities.stream()
            .sorted(Comparator.comparing(AdminDashboardDTO.PlatformActivityDTO::timestamp).reversed())
            .limit(10)
            .toList();
    }

    private List<AdminDashboardDTO.AuditEntryDTO> recentAuditLogs() {
        return auditLogRepository.findAll().stream()
            .sorted(Comparator.comparing(AdminAuditLog::getCreatedAt).reversed())
            .limit(8)
            .map(log -> new AdminDashboardDTO.AuditEntryDTO(
                log.getId(),
                log.getAdmin().getEmail(),
                log.getAction(),
                log.getTargetType(),
                log.getTargetId(),
                log.getDetails(),
                log.getCreatedAt()
            ))
            .toList();
    }
}
