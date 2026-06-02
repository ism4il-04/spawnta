# SPAWNTA Phase 5 - Plan Détaillé d'Exécution

**Date** : 2 Juin 2026  
**Projet** : Spawnta - Social Mapping Platform  
**Phase** : 5 - Monetisation & Admin Dashboard  
**Estimé** : ~110 heures (4-5 semaines full-time)

---

## 📊 État du Projet (Snapshot)

### ✅ Phases Complétées
- **Phase 0** : Infrastructure complète (Docker, DB, Kafka, Redis)
- **Phase 1** : Auth JWT, email verification, profiles riches
- **Phase 2** : Activities CRUD, géospatial search, cartographie Leaflet
- **Phase 3** : Chat WebSocket, Kafka messaging, notifications temps réel
- **Phase 4** : Gamification (XP/badges), check-in QR, recommandations, ratings

### 📈 Progression
- **Code** : 115 fichiers Java backend bien structurés
- **Infrastructure** : 7 conteneurs Docker fonctionnels et stables
- **API** : 40+ endpoints REST sécurisés
- **Frontend** : 2 apps Angular (user + admin skeleton)
- **Global** : ~74% completé

### 🔧 Stack Actuel
```
Backend   : Spring Boot 4 (Java 21) + JPA + Security + Actuator
Frontend  : Angular 21 + Material + Leaflet + Chart.js
Admin     : Angular 21 (skeleton - à développer en Phase 5)
Database  : PostgreSQL 15 + PostGIS extensions
Cache     : Redis 7
Messaging : Kafka 7.5 + Zookeeper
Storage   : Cloudinary (config pending)
Auth      : JWT (OAuth2 partial)
```

---

## 🎯 Objectif Phase 5

Transformer Spawnta en plateforme de SaaS avec :
1. **Monetisation** : Plans d'abonnement et système de paiement Stripe
2. **Admin Métier** : Dashboard analytics, gestion utilisateurs, moderation
3. **Admin Technique** : Health checks, logs, metrics, feature flags
4. **Premium UI** : Paywall, premium badges, stats personnalisées

---

## 📋 Roadmap d'Exécution

### Étape 1 : Planification & Setup (1-2 jours)

#### 1.1 - Créer les branches de feature
```bash
# Au lieu de faire tout en 1 branche
git checkout -b feature/phase5-premium        # Entities + Stripe
git checkout -b feature/phase5-admin-backend  # Admin services + endpoints
git checkout -b feature/phase5-admin-frontend # Admin dashboard
git checkout -b feature/phase5-user-premium   # User-facing premium UI
```

#### 1.2 - Setup Stripe Account & Keys
- Créer compte Stripe (https://dashboard.stripe.com)
- Générer API keys (Publishable + Secret)
- Ajouter à `.env` et `application.properties`
- Setup webhooks locale : `stripe listen --forward-to localhost:8080/api/webhooks/stripe`

#### 1.3 - Créer la structure de packages backend
```
src/main/java/com/spawnta/
├── subscription/          # NEW - Premium & billing
│   ├── entity/
│   ├── dto/
│   ├── controller/
│   ├── service/
│   └── repository/
├── admin/                 # NEW - Admin features
│   ├── entity/
│   ├── dto/
│   ├── controller/
│   ├── service/
│   └── repository/
└── moderation/            # NEW - Reports & actions
    ├── entity/
    ├── dto/
    └── service/
```

#### 1.4 - Créer structure frontend admin
```
admin/src/app/
├── core/
│   ├── guards/
│   │   └── admin.guard.ts        # Role-based guard
│   ├── interceptors/
│   │   └── admin-auth.interceptor.ts
│   └── services/
│       ├── admin.service.ts      # API calls
│       └── analytics.service.ts
├── shared/
│   ├── layouts/
│   │   └── admin-layout.component.ts
│   └── components/
│       ├── sidebar.component.ts
│       └── header.component.ts
└── modules/
    ├── dashboard/               # Dashboard page
    ├── users/                   # Users management
    ├── activities/              # Activities management
    ├── reports/                 # Moderation queue
    ├── subscriptions/           # Subscription plans
    ├── analytics/               # Advanced analytics
    └── technical/               # Health checks
```

---

### Étape 2 : Backend Premium & Paiement (1.5 semaines)

#### 2.1 - Entités Subscription
**Fichier** : `backend/src/main/java/com/spawnta/subscription/entity/`

```java
// SubscriptionTier.java - Enum
public enum SubscriptionTier {
    FREE("free", 0, 5),           // 5 activities/week free
    STARTER("starter", 9.99, -1), // unlimited
    PROFESSIONAL("pro", 24.99, -1);
    
    private String id;
    private BigDecimal monthlyPrice;
    private int weeklyActivityLimit; // -1 = unlimited
}

// SubscriptionPlan.java
@Entity
@Table(name = "subscription_plans")
public class SubscriptionPlan {
    @Id
    @GeneratedValue
    private Long id;
    
    @Enumerated(EnumType.STRING)
    private SubscriptionTier tier;
    
    private String name;
    private String description;
    private BigDecimal monthlyPrice;
    private String stripeProductId;
    private String stripePriceId;
    
    @ElementCollection
    @CollectionTable(name = "plan_features")
    private Set<String> features; // ["Unlimited activities", "Premium support", ...]
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

// UserSubscription.java
@Entity
@Table(name = "user_subscriptions")
public class UserSubscription {
    @Id
    @GeneratedValue
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "plan_id")
    private SubscriptionPlan plan;
    
    @Column(name = "stripe_customer_id")
    private String stripeCustomerId;
    
    @Column(name = "stripe_subscription_id")
    private String stripeSubscriptionId;
    
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime renewalDate;
    
    @Enumerated(EnumType.STRING)
    private SubscriptionStatus status; // ACTIVE, CANCELLED, EXPIRED, PAST_DUE
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

// PaymentTransaction.java
@Entity
@Table(name = "payment_transactions")
public class PaymentTransaction {
    @Id
    @GeneratedValue
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(name = "stripe_payment_intent_id")
    private String stripePaymentIntentId;
    
    private BigDecimal amount;
    private String currency; // USD, EUR, ...
    
    @Enumerated(EnumType.STRING)
    private PaymentStatus status; // PENDING, SUCCEEDED, FAILED
    
    private String description;
    private LocalDateTime createdAt;
}

// Invoice.java
@Entity
@Table(name = "invoices")
public class Invoice {
    @Id
    @GeneratedValue
    private Long id;
    
    @ManyToOne
    private User user;
    
    @Column(name = "stripe_invoice_id")
    private String stripeInvoiceId;
    
    private BigDecimal amount;
    private String currency;
    private Integer month;
    private Integer year;
    
    @Enumerated(EnumType.STRING)
    private InvoiceStatus status; // DRAFT, SENT, PAID, FAILED, CANCELLED
    
    private String pdfUrl;
    private LocalDateTime dueDate;
    private LocalDateTime createdAt;
}
```

**Tâche** : 4-5 heures

---

#### 2.2 - Stripe Service & Integration

**Fichier** : `backend/src/main/java/com/spawnta/subscription/service/StripeService.java`

```java
@Service
@Slf4j
public class StripeService {
    
    private final StripeClient stripeClient;
    private final SubscriptionPlanRepository planRepository;
    private final UserSubscriptionRepository userSubRepository;
    private final PaymentTransactionRepository transactionRepository;
    
    @Value("${stripe.api-key}")
    private String stripeApiKey;
    
    @Value("${app.frontend-url}")
    private String frontendUrl;
    
    // ── Customer Management ──
    public String createOrUpdateCustomer(User user) throws StripeException {
        // Si customer existe, retourner ID
        // Sinon créer nouveau
        com.stripe.model.Customer customer = Customer.create(
            CustomerCreateParams.builder()
                .setEmail(user.getEmail())
                .setName(user.getFirstName() + " " + user.getLastName())
                .putMetadata("userId", user.getId().toString())
                .build()
        );
        return customer.getId();
    }
    
    // ── Checkout Session ──
    public String createCheckoutSession(User user, SubscriptionPlan plan) 
            throws StripeException {
        String customerId = createOrUpdateCustomer(user);
        
        Session session = Session.create(
            SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
                .setCustomer(customerId)
                .addLineItem(
                    SessionCreateParams.LineItem.builder()
                        .setPrice(plan.getStripePriceId())
                        .setQuantity(1L)
                        .build()
                )
                .setSuccessUrl(frontendUrl + "/subscription/success?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(frontendUrl + "/subscription/cancel")
                .build()
        );
        
        return session.getUrl();
    }
    
    // ── Webhook Handler ──
    @Transactional
    public void handleStripeWebhook(String payload, String signature) 
            throws SignatureVerificationException {
        Event event = Webhook.constructEvent(payload, signature, webhookSecret);
        
        switch (event.getType()) {
            case "customer.subscription.updated":
                handleSubscriptionUpdated(event);
                break;
            case "customer.subscription.deleted":
                handleSubscriptionCancelled(event);
                break;
            case "payment_intent.succeeded":
                handlePaymentSucceeded(event);
                break;
            case "payment_intent.payment_failed":
                handlePaymentFailed(event);
                break;
            case "invoice.payment_succeeded":
                handleInvoicePaymentSucceeded(event);
                break;
        }
    }
    
    private void handleSubscriptionUpdated(Event event) {
        Subscription sub = (Subscription) event.getDataObjectDeserializer()
            .getObject().orElse(null);
        // Update UserSubscription in DB
        log.info("Subscription updated: {}", sub.getId());
    }
    
    // ── Cancellation ──
    public void cancelSubscription(User user) throws StripeException {
        UserSubscription userSub = userSubRepository.findByUser(user)
            .orElseThrow(() -> new RuntimeException("No subscription found"));
            
        Subscription.retrieve(userSub.getStripeSubscriptionId())
            .cancel();
        
        userSub.setStatus(SubscriptionStatus.CANCELLED);
        userSub.setEndDate(LocalDateTime.now());
        userSubRepository.save(userSub);
    }
    
    // ── Invoices ──
    public List<Invoice> getInvoices(User user) throws StripeException {
        UserSubscription userSub = userSubRepository.findByUser(user)
            .orElse(null);
        if (userSub == null) return Collections.emptyList();
        
        // Fetch from Stripe + map to DB invoices
        InvoiceCollection invoices = Invoice.list(
            InvoiceListParams.builder()
                .setCustomer(userSub.getStripeCustomerId())
                .setLimit(100L)
                .build()
        );
        
        return invoices.getData().stream()
            .map(stripeInvoice -> invoiceRepository.findByStripeInvoiceId(stripeInvoice.getId())
                .orElse(null))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
}
```

**Tâche** : 5-6 heures + 1h tests

---

#### 2.3 - Billing Service

**Fichier** : `backend/src/main/java/com/spawnta/subscription/service/BillingService.java`

```java
@Service
@Transactional
@Slf4j
public class BillingService {
    
    private final StripeService stripeService;
    private final UserSubscriptionRepository userSubRepository;
    private final InvoiceRepository invoiceRepository;
    private final SubscriptionPlanRepository planRepository;
    private final UserRepository userRepository;
    
    // ── Upgrade ──
    public String upgradeSubscription(User user, SubscriptionTier newTier) 
            throws StripeException {
        SubscriptionPlan newPlan = planRepository.findByTier(newTier)
            .orElseThrow(() -> new RuntimeException("Plan not found"));
        
        UserSubscription userSub = userSubRepository.findByUser(user)
            .orElseThrow(() -> new RuntimeException("No subscription found"));
        
        // Update subscription in Stripe
        Subscription stripeSub = Subscription.retrieve(userSub.getStripeSubscriptionId());
        stripeSub.update(
            SubscriptionUpdateParams.builder()
                .addItem(
                    SubscriptionUpdateParams.Item.builder()
                        .setId(stripeSub.getItems().getData().get(0).getId())
                        .setPrice(newPlan.getStripePriceId())
                        .build()
                )
                .setProrationBehavior(SubscriptionUpdateParams.ProrationBehavior.ALWAYS_INVOICE)
                .build()
        );
        
        userSub.setPlan(newPlan);
        userSubRepository.save(userSub);
        
        return "Upgraded to " + newTier;
    }
    
    // ── Generate Invoice ──
    public Invoice generateInvoice(User user, SubscriptionPlan plan, LocalDate date) {
        Invoice invoice = new Invoice();
        invoice.setUser(user);
        invoice.setAmount(plan.getMonthlyPrice());
        invoice.setCurrency("USD");
        invoice.setMonth(date.getMonthValue());
        invoice.setYear(date.getYear());
        invoice.setStatus(InvoiceStatus.DRAFT);
        invoice.setCreatedAt(LocalDateTime.now());
        
        return invoiceRepository.save(invoice);
    }
    
    // ── Renewal (scheduled) ──
    @Scheduled(cron = "0 0 0 * * *") // Daily at midnight
    @Transactional
    public void autoRenewSubscriptions() {
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
        List<UserSubscription> expiring = userSubRepository
            .findByRenewalDateBetween(
                LocalDateTime.now(),
                tomorrow
            );
        
        expiring.forEach(sub -> {
            try {
                Subscription stripeSub = Subscription.retrieve(sub.getStripeSubscriptionId());
                // Stripe handles auto-renewal, just log it
                log.info("Auto-renewal for user {}", sub.getUser().getId());
                sub.setRenewalDate(LocalDateTime.now().plusMonths(1));
                sub.setStatus(SubscriptionStatus.ACTIVE);
                userSubRepository.save(sub);
            } catch (StripeException e) {
                log.error("Renewal failed for user {}: {}", sub.getUser().getId(), e.getMessage());
            }
        });
    }
}
```

**Tâche** : 3-4 heures + 30min tests

---

#### 2.4 - REST Endpoints Premium

**Fichier** : `backend/src/main/java/com/spawnta/subscription/controller/SubscriptionController.java`

```java
@RestController
@RequestMapping("/api/subscription")
@Slf4j
@AllArgsConstructor
public class SubscriptionController {
    
    private final StripeService stripeService;
    private final BillingService billingService;
    private final SubscriptionPlanRepository planRepository;
    private final UserSubscriptionRepository userSubRepository;
    
    // ── Public endpoints ──
    
    @GetMapping("/plans")
    public ResponseEntity<?> getPlans() {
        List<SubscriptionPlan> plans = planRepository.findAll();
        return ResponseEntity.ok(plans);
    }
    
    @GetMapping("/current")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCurrentSubscription(@AuthenticationPrincipal User user) {
        UserSubscription sub = userSubRepository.findByUser(user)
            .orElse(UserSubscription.defaultFree(user)); // Return FREE if none
        return ResponseEntity.ok(sub);
    }
    
    @PostMapping("/upgrade")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> upgradeSubscription(
            @AuthenticationPrincipal User user,
            @RequestBody UpgradeRequest req) {
        try {
            SubscriptionPlan plan = planRepository.findByTier(req.getTier())
                .orElseThrow(() -> new RuntimeException("Plan not found"));
            
            String checkoutUrl = stripeService.createCheckoutSession(user, plan);
            return ResponseEntity.ok(Map.of("checkoutUrl", checkoutUrl));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> cancelSubscription(@AuthenticationPrincipal User user) {
        try {
            stripeService.cancelSubscription(user);
            return ResponseEntity.ok(Map.of("message", "Subscription cancelled"));
        } catch (Exception e) {
            return ResponseEntity.status(400).body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/invoices")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getInvoices(@AuthenticationPrincipal User user) {
        try {
            List<Invoice> invoices = stripeService.getInvoices(user);
            return ResponseEntity.ok(invoices);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
    
    // ── Webhook ──
    
    @PostMapping("/webhook")
    public ResponseEntity<?> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature) {
        try {
            stripeService.handleStripeWebhook(payload, signature);
            return ResponseEntity.ok(Map.of("status", "received"));
        } catch (SignatureVerificationException e) {
            return ResponseEntity.status(400).body(Map.of("error", "Invalid signature"));
        }
    }
}

@Data
class UpgradeRequest {
    private SubscriptionTier tier;
}
```

**Tâche** : 3-4 heures + 1h tests

---

### Étape 3 : Backend Admin - Analytics & Moderation (1.5 semaines)

#### 3.1 - Admin Entities

**Fichiers** : `backend/src/main/java/com/spawnta/admin/entity/`

```java
// AdminAuditLog.java
@Entity
@Table(name = "admin_audit_logs")
public class AdminAuditLog {
    @Id
    @GeneratedValue
    private Long id;
    
    @ManyToOne
    private User admin;
    
    private String action; // "SUSPEND_USER", "DELETE_ACTIVITY", etc.
    private String targetType; // "USER", "ACTIVITY"
    private Long targetId;
    
    @Column(columnDefinition = "TEXT")
    private String details; // JSON with change details
    
    private LocalDateTime createdAt;
}

// UserReport.java
@Entity
@Table(name = "user_reports")
public class UserReport {
    @Id
    @GeneratedValue
    private Long id;
    
    @ManyToOne
    private User reportedBy;
    
    @ManyToOne
    private User reportedUser;
    
    private String reason; // "Inappropriate behavior", etc.
    
    @Enumerated(EnumType.STRING)
    private ReportStatus status; // PENDING, UNDER_REVIEW, RESOLVED, DISMISSED
    
    @Column(columnDefinition = "TEXT")
    private String adminNotes;
    
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private Long resolvedBy; // Admin ID
}

// ActivityReport.java
@Entity
@Table(name = "activity_reports")
public class ActivityReport {
    @Id
    @GeneratedValue
    private Long id;
    
    @ManyToOne
    private User reportedBy;
    
    @ManyToOne
    private Activity reportedActivity;
    
    private String reason;
    
    @Enumerated(EnumType.STRING)
    private ReportStatus status;
    
    @Column(columnDefinition = "TEXT")
    private String adminNotes;
    
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private Long resolvedBy;
}

// ModerationAction.java
@Entity
@Table(name = "moderation_actions")
public class ModerationAction {
    @Id
    @GeneratedValue
    private Long id;
    
    @ManyToOne
    private User admin;
    
    @ManyToOne
    private User targetUser;
    
    @Enumerated(EnumType.STRING)
    private ActionType actionType; // WARN, SUSPEND, BAN
    
    private Integer suspensionDays;
    private String reason;
    
    @Column(columnDefinition = "TEXT")
    private String details;
    
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt; // For temporary suspensions
}

// Enums
public enum ActionType {
    WARN, SUSPEND, BAN, RESTORE, SOFT_DELETE
}

public enum ReportStatus {
    PENDING, UNDER_REVIEW, RESOLVED, DISMISSED, APPEAL
}
```

**Tâche** : 2-3 heures

---

#### 3.2 - AdminAnalyticsService

**Fichier** : `backend/src/main/java/com/spawnta/admin/service/AdminAnalyticsService.java`

```java
@Service
@Slf4j
@AllArgsConstructor
public class AdminAnalyticsService {
    
    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;
    private final ActivityParticipantRepository participantRepository;
    private final UserSubscriptionRepository subRepository;
    private final ActivityRatingRepository ratingRepository;
    private final PaymentTransactionRepository paymentRepository;
    
    // ── User Metrics ──
    public long getActiveUsersCount(LocalDate startDate, LocalDate endDate) {
        return userRepository.countActiveUsersInRange(startDate, endDate);
    }
    
    public long getNewUsersCount(LocalDate startDate, LocalDate endDate) {
        return userRepository.countUsersCreatedInRange(startDate, endDate);
    }
    
    public long getTotalUsers() {
        return userRepository.count();
    }
    
    // ── Activity Metrics ──
    public long getActivitiesCreatedCount(LocalDate startDate, LocalDate endDate) {
        return activityRepository.countCreatedInRange(startDate, endDate);
    }
    
    public Map<String, Long> getActivitiesByCategory() {
        return activityRepository.findAll().stream()
            .collect(Collectors.groupingBy(
                Activity::getCategory,
                Collectors.counting()
            ));
    }
    
    public double getAverageRating() {
        List<ActivityRating> ratings = ratingRepository.findAll();
        if (ratings.isEmpty()) return 0.0;
        return ratings.stream()
            .mapToInt(ActivityRating::getRating)
            .average()
            .orElse(0.0);
    }
    
    // ── Revenue Metrics ──
    public BigDecimal getTotalRevenue(LocalDate startDate, LocalDate endDate) {
        return paymentRepository.sumAmountInRange(startDate, endDate);
    }
    
    public Map<String, Object> getSubscriptionStats() {
        long totalPremium = subRepository.countByStatusAndTierNot(
            SubscriptionStatus.ACTIVE, 
            SubscriptionTier.FREE
        );
        long free = userRepository.count() - totalPremium;
        
        return Map.of(
            "totalPremium", totalPremium,
            "totalFree", free,
            "premiumPercentage", (double) totalPremium / userRepository.count() * 100,
            "mrrValue", calculateMRR()
        );
    }
    
    private BigDecimal calculateMRR() {
        return subRepository.findAll().stream()
            .filter(s -> s.getStatus() == SubscriptionStatus.ACTIVE)
            .map(s -> s.getPlan().getMonthlyPrice())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    
    // ── Engagement Metrics ──
    public Map<String, Object> getEngagementMetrics() {
        long totalParticipations = participantRepository.count();
        long totalRatings = ratingRepository.count();
        
        return Map.of(
            "totalParticipations", totalParticipations,
            "totalRatings", totalRatings,
            "avgParticipantsPerActivity", activityRepository.findAll().stream()
                .mapToLong(a -> a.getParticipants().size())
                .average()
                .orElse(0.0)
        );
    }
    
    // ── Growth Trends ──
    public List<DailyMetric> getUserGrowthTrend(LocalDate startDate, LocalDate endDate) {
        return userRepository.getDailyCountInRange(startDate, endDate);
    }
    
    public List<DailyMetric> getActivityGrowthTrend(LocalDate startDate, LocalDate endDate) {
        return activityRepository.getDailyCountInRange(startDate, endDate);
    }
    
    // ── Advanced ──
    public Map<String, Long> getUsersByRegion() {
        // Utilisé pour analyse géographique
        return userRepository.findAll().stream()
            .collect(Collectors.groupingBy(
                u -> extractRegion(u.getLastLatitude(), u.getLastLongitude()),
                Collectors.counting()
            ));
    }
}

@Data
class DailyMetric {
    private LocalDate date;
    private Long count;
}
```

**Tâche** : 5-6 heures + 2h tests + query optimization

---

#### 3.3 - AdminModerationService

**Fichier** : `backend/src/main/java/com/spawnta/admin/service/AdminModerationService.java`

```java
@Service
@Transactional
@Slf4j
@AllArgsConstructor
public class AdminModerationService {
    
    private final UserReportRepository userReportRepository;
    private final ActivityReportRepository activityReportRepository;
    private final ModerationActionRepository actionRepository;
    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;
    private final AdminAuditLogRepository auditRepository;
    private final EmailService emailService;
    
    // ── Report Submission ──
    public UserReport submitUserReport(User reporter, Long reportedUserId, String reason) {
        User reportedUser = userRepository.findById(reportedUserId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        UserReport report = new UserReport();
        report.setReportedBy(reporter);
        report.setReportedUser(reportedUser);
        report.setReason(reason);
        report.setStatus(ReportStatus.PENDING);
        report.setCreatedAt(LocalDateTime.now());
        
        return userReportRepository.save(report);
    }
    
    public ActivityReport submitActivityReport(User reporter, Long activityId, String reason) {
        Activity activity = activityRepository.findById(activityId)
            .orElseThrow(() -> new RuntimeException("Activity not found"));
        
        ActivityReport report = new ActivityReport();
        report.setReportedBy(reporter);
        report.setReportedActivity(activity);
        report.setReason(reason);
        report.setStatus(ReportStatus.PENDING);
        report.setCreatedAt(LocalDateTime.now());
        
        return activityReportRepository.save(report);
    }
    
    // ── Moderation Actions ──
    @Transactional
    public void suspendUser(Long userId, Integer days, String reason, User admin) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Create action record
        ModerationAction action = new ModerationAction();
        action.setAdmin(admin);
        action.setTargetUser(user);
        action.setActionType(ActionType.SUSPEND);
        action.setSuspensionDays(days);
        action.setReason(reason);
        action.setCreatedAt(LocalDateTime.now());
        action.setExpiresAt(LocalDateTime.now().plusDays(days));
        
        actionRepository.save(action);
        
        // Update user status
        user.setSuspendedUntil(LocalDateTime.now().plusDays(days));
        userRepository.save(user);
        
        // Audit log
        logAdminAction(admin, "SUSPEND_USER", "USER", userId, 
            Map.of("days", days, "reason", reason));
        
        // Notify user
        emailService.sendSuspensionNotice(user, days, reason);
    }
    
    @Transactional
    public void banUser(Long userId, String reason, User admin) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setRole(Role.BANNED);
        userRepository.save(user);
        
        ModerationAction action = new ModerationAction();
        action.setAdmin(admin);
        action.setTargetUser(user);
        action.setActionType(ActionType.BAN);
        action.setReason(reason);
        action.setCreatedAt(LocalDateTime.now());
        
        actionRepository.save(action);
        
        logAdminAction(admin, "BAN_USER", "USER", userId, Map.of("reason", reason));
        emailService.sendBanNotice(user, reason);
    }
    
    @Transactional
    public void restoreUser(Long userId, User admin) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setRole(Role.USER);
        user.setSuspendedUntil(null);
        userRepository.save(user);
        
        logAdminAction(admin, "RESTORE_USER", "USER", userId, Collections.emptyMap());
        emailService.sendRestorationNotice(user);
    }
    
    @Transactional
    public void deleteActivity(Long activityId, String reason, User admin) {
        Activity activity = activityRepository.findById(activityId)
            .orElseThrow(() -> new RuntimeException("Activity not found"));
        
        // Soft delete or mark as deleted
        activity.setDeleted(true);
        activity.setDeletionReason(reason);
        activityRepository.save(activity);
        
        logAdminAction(admin, "DELETE_ACTIVITY", "ACTIVITY", activityId, 
            Map.of("reason", reason));
        
        emailService.sendActivityDeletionNotice(activity.getCreator(), activity, reason);
    }
    
    // ── Report Approval ──
    @Transactional
    public void approveReport(Long reportId, ActionType actionType, Integer suspensionDays, 
                              User admin) {
        // Handle user report or activity report
        UserReport report = userReportRepository.findById(reportId)
            .orElseThrow(() -> new RuntimeException("Report not found"));
        
        report.setStatus(ReportStatus.RESOLVED);
        report.setResolvedAt(LocalDateTime.now());
        report.setResolvedBy(admin.getId());
        userReportRepository.save(report);
        
        if (actionType == ActionType.SUSPEND) {
            suspendUser(report.getReportedUser().getId(), suspensionDays, 
                report.getReason(), admin);
        } else if (actionType == ActionType.BAN) {
            banUser(report.getReportedUser().getId(), report.getReason(), admin);
        }
    }
    
    // ── Audit Logging ──
    private void logAdminAction(User admin, String action, String targetType, 
                                Long targetId, Map<String, ?> details) {
        AdminAuditLog log = new AdminAuditLog();
        log.setAdmin(admin);
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setDetails(new ObjectMapper().valueToTree(details).toString());
        log.setCreatedAt(LocalDateTime.now());
        
        auditRepository.save(log);
    }
}
```

**Tâche** : 6-7 heures + 1h tests

---

#### 3.4 - Admin REST Endpoints

**Fichier** : `backend/src/main/java/com/spawnta/admin/controller/AdminController.java`

```java
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
@AllArgsConstructor
public class AdminController {
    
    private final AdminAnalyticsService analyticsService;
    private final AdminModerationService moderationService;
    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;
    private final UserReportRepository reportRepository;
    
    // ── Analytics ──
    @GetMapping("/analytics")
    public ResponseEntity<?> getDashboardData(
            @RequestParam(defaultValue = "7") Integer days) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);
        
        Map<String, Object> data = Map.of(
            "activeUsers", analyticsService.getActiveUsersCount(startDate, endDate),
            "newUsers", analyticsService.getNewUsersCount(startDate, endDate),
            "activitiesCreated", analyticsService.getActivitiesCreatedCount(startDate, endDate),
            "avgRating", analyticsService.getAverageRating(),
            "revenue", analyticsService.getTotalRevenue(startDate, endDate),
            "subscriptionStats", analyticsService.getSubscriptionStats(),
            "engagementMetrics", analyticsService.getEngagementMetrics()
        );
        
        return ResponseEntity.ok(data);
    }
    
    // ── Users Management ──
    @GetMapping("/users")
    public ResponseEntity<?> listUsers(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String search) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<User> users;
        
        if (search != null && !search.isEmpty()) {
            users = userRepository.findByEmailContainingOrFirstNameContaining(search, search, pageable);
        } else {
            users = userRepository.findAll(pageable);
        }
        
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserDetail(@PathVariable Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(user);
    }
    
    @PostMapping("/users/{id}/suspend")
    public ResponseEntity<?> suspendUser(
            @PathVariable Long id,
            @RequestBody SuspendRequest req,
            @AuthenticationPrincipal User admin) {
        
        moderationService.suspendUser(id, req.getDays(), req.getReason(), admin);
        return ResponseEntity.ok(Map.of("message", "User suspended"));
    }
    
    @PostMapping("/users/{id}/ban")
    public ResponseEntity<?> banUser(
            @PathVariable Long id,
            @RequestBody BanRequest req,
            @AuthenticationPrincipal User admin) {
        
        moderationService.banUser(id, req.getReason(), admin);
        return ResponseEntity.ok(Map.of("message", "User banned"));
    }
    
    @PostMapping("/users/{id}/restore")
    public ResponseEntity<?> restoreUser(
            @PathVariable Long id,
            @AuthenticationPrincipal User admin) {
        
        moderationService.restoreUser(id, admin);
        return ResponseEntity.ok(Map.of("message", "User restored"));
    }
    
    // ── Activities Management ──
    @GetMapping("/activities")
    public ResponseEntity<?> listActivities(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String category) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Activity> activities;
        
        if (category != null && !category.isEmpty()) {
            activities = activityRepository.findByCategory(category, pageable);
        } else {
            activities = activityRepository.findAll(pageable);
        }
        
        return ResponseEntity.ok(activities);
    }
    
    @DeleteMapping("/activities/{id}")
    public ResponseEntity<?> deleteActivity(
            @PathVariable Long id,
            @RequestBody DeleteActivityRequest req,
            @AuthenticationPrincipal User admin) {
        
        moderationService.deleteActivity(id, req.getReason(), admin);
        return ResponseEntity.ok(Map.of("message", "Activity deleted"));
    }
    
    // ── Reports Management ──
    @GetMapping("/reports")
    public ResponseEntity<?> listReports(
            @RequestParam(defaultValue = "PENDING") String status,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<?> reports = reportRepository.findByStatus(
            ReportStatus.valueOf(status), 
            pageable
        );
        
        return ResponseEntity.ok(reports);
    }
    
    @PostMapping("/reports/{id}/approve")
    public ResponseEntity<?> approveReport(
            @PathVariable Long id,
            @RequestBody ApproveReportRequest req,
            @AuthenticationPrincipal User admin) {
        
        moderationService.approveReport(id, req.getActionType(), 
            req.getSuspensionDays(), admin);
        
        return ResponseEntity.ok(Map.of("message", "Report approved and action taken"));
    }
}

// DTO classes
@Data
class SuspendRequest {
    private Integer days;
    private String reason;
}

@Data
class BanRequest {
    private String reason;
}

@Data
class DeleteActivityRequest {
    private String reason;
}

@Data
class ApproveReportRequest {
    private ActionType actionType;
    private Integer suspensionDays;
}
```

**Tâche** : 4-5 heures + 1h tests

---

### Étape 4 : Frontend User - Premium Features (5-6 jours)

#### 4.1 - Paywall Modal Component

**Fichier** : `frontend/src/app/features/subscription/paywall-modal.component.ts`

```typescript
import { Component, OnInit, Inject } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from '@angular/material/dialog';
import { SubscriptionService } from '../../core/services/subscription.service';

@Component({
  selector: 'app-paywall-modal',
  templateUrl: './paywall-modal.component.html',
  styleUrls: ['./paywall-modal.component.scss']
})
export class PaywallModalComponent implements OnInit {
  plans: SubscriptionPlan[] = [];
  selectedPlan: SubscriptionPlan | null = null;
  loading = false;
  
  constructor(
    private subService: SubscriptionService,
    public dialogRef: MatDialogRef<PaywallModalComponent>,
    @Inject(MAT_DIALOG_DATA) public data: any
  ) {}
  
  ngOnInit(): void {
    this.loadPlans();
  }
  
  loadPlans(): void {
    this.subService.getPlans().subscribe(
      plans => this.plans = plans
    );
  }
  
  selectPlan(plan: SubscriptionPlan): void {
    this.selectedPlan = plan;
  }
  
  upgradeToPlan(): void {
    if (!this.selectedPlan) return;
    
    this.loading = true;
    this.subService.upgradeSubscription(this.selectedPlan.tier).subscribe(
      (response: any) => {
        // Redirect to Stripe checkout
        window.location.href = response.checkoutUrl;
      },
      error => {
        this.loading = false;
        console.error('Upgrade failed', error);
      }
    );
  }
  
  close(): void {
    this.dialogRef.close();
  }
}
```

**Template** : `paywall-modal.component.html`

```html
<div class="paywall-modal">
  <h2>Upgrade to Premium</h2>
  <p>Unlock unlimited activities and exclusive features</p>
  
  <div class="plans-grid">
    <mat-card 
      *ngFor="let plan of plans" 
      class="plan-card"
      [class.selected]="selectedPlan?.id === plan.id"
      (click)="selectPlan(plan)">
      
      <mat-card-header>
        <h3>{{ plan.name }}</h3>
        <p class="price">${{ plan.monthlyPrice }}/month</p>
      </mat-card-header>
      
      <mat-card-content>
        <ul class="features">
          <li *ngFor="let feature of plan.features">
            <mat-icon>check_circle</mat-icon>
            {{ feature }}
          </li>
        </ul>
      </mat-card-content>
    </mat-card>
  </div>
  
  <div class="actions">
    <button 
      mat-raised-button 
      color="primary" 
      (click)="upgradeToPlan()"
      [disabled]="!selectedPlan || loading">
      {{ loading ? 'Processing...' : 'Upgrade Now' }}
    </button>
    <button mat-button (click)="close()">Cancel</button>
  </div>
</div>
```

**Tâche** : 3-4 heures

---

#### 4.2 - Premium Badge & Status Display

**Fichier** : `frontend/src/app/shared/components/premium-badge.component.ts`

```typescript
import { Component, Input } from '@angular/core';
import { SubscriptionTier } from '../../types/subscription.types';

@Component({
  selector: 'app-premium-badge',
  template: `
    <span class="premium-badge" [class]="'tier-' + tier.toLowerCase()" 
          *ngIf="tier !== 'FREE'">
      ⭐ {{ tier }}
    </span>
  `,
  styleUrls: ['./premium-badge.component.scss']
})
export class PremiumBadgeComponent {
  @Input() tier: SubscriptionTier = 'FREE';
}
```

**Style** : `premium-badge.component.scss`

```scss
.premium-badge {
  display: inline-block;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 0.75rem;
  font-weight: bold;
  
  &.tier-starter {
    background-color: #e3f2fd;
    color: #1976d2;
  }
  
  &.tier-professional {
    background-color: #fff3e0;
    color: #f57c00;
  }
}
```

**Tâche** : 1-2 heures

---

#### 4.3 - Premium Stats Dashboard

**Fichier** : `frontend/src/app/features/profile/premium-stats.component.ts`

```typescript
import { Component, OnInit } from '@angular/core';
import { SubscriptionService } from '../../core/services/subscription.service';

@Component({
  selector: 'app-premium-stats',
  templateUrl: './premium-stats.component.html',
  styleUrls: ['./premium-stats.component.scss']
})
export class PremiumStatsComponent implements OnInit {
  stats: PremiumStats | null = null;
  loading = true;
  
  constructor(private subService: SubscriptionService) {}
  
  ngOnInit(): void {
    this.loadStats();
  }
  
  loadStats(): void {
    this.subService.getPremiumStats().subscribe(
      data => {
        this.stats = data;
        this.loading = false;
      },
      error => {
        console.error('Failed to load stats', error);
        this.loading = false;
      }
    );
  }
  
  exportCSV(): void {
    // Generate CSV export
  }
}

interface PremiumStats {
  activitiesCreated: number;
  totalParticipants: number;
  averageRating: number;
  earnings: number;
  trends: TrendData[];
}

interface TrendData {
  date: string;
  value: number;
}
```

**Tâche** : 4-5 heures

---

#### 4.4 - Limits Indicator & CTA

**Fichier** : `frontend/src/app/features/activities/create-activity/limits-banner.component.ts`

```typescript
import { Component, OnInit } from '@angular/core';
import { SubscriptionService } from '../../../core/services/subscription.service';
import { MatDialog } from '@angular/material/dialog';
import { PaywallModalComponent } from '../../subscription/paywall-modal.component';

@Component({
  selector: 'app-limits-banner',
  template: `
    <mat-card class="limits-card" *ngIf="!unlimited">
      <mat-card-content>
        <div class="limit-progress">
          <p>{{ usedActivities }} / {{ maxActivities }} activities created this week</p>
          <mat-progress-bar 
            mode="determinate" 
            [value]="(usedActivities / maxActivities) * 100">
          </mat-progress-bar>
        </div>
        
        <button 
          mat-raised-button 
          color="accent"
          (click)="openPaywall()"
          *ngIf="usedActivities >= maxActivities">
          Upgrade to Unlimited
        </button>
      </mat-card-content>
    </mat-card>
  `,
  styleUrls: ['./limits-banner.component.scss']
})
export class LimitsBannerComponent implements OnInit {
  usedActivities = 0;
  maxActivities = 5;
  unlimited = false;
  
  constructor(
    private subService: SubscriptionService,
    private dialog: MatDialog
  ) {}
  
  ngOnInit(): void {
    this.checkLimits();
  }
  
  checkLimits(): void {
    this.subService.getCurrentSubscription().subscribe(
      sub => {
        this.unlimited = sub.plan.tier !== 'FREE';
        this.maxActivities = sub.plan.maxActivities;
        this.loadUsedActivities();
      }
    );
  }
  
  loadUsedActivities(): void {
    // API call to count activities created this week
  }
  
  openPaywall(): void {
    this.dialog.open(PaywallModalComponent);
  }
}
```

**Tâche** : 2-3 heures

---

### Étape 5 : Frontend Admin Dashboard (50-60 heures)

#### 5.1 - Admin Layout Architecture

**Fichier** : `admin/src/app/shared/layouts/admin-layout.component.ts`

```typescript
import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-admin-layout',
  templateUrl: './admin-layout.component.html',
  styleUrls: ['./admin-layout.component.scss']
})
export class AdminLayoutComponent {
  sidenavOpen = true;
  
  navigationItems = [
    { label: 'Dashboard', icon: 'dashboard', link: '/admin/dashboard' },
    { label: 'Users', icon: 'people', link: '/admin/users' },
    { label: 'Activities', icon: 'event', link: '/admin/activities' },
    { label: 'Reports', icon: 'flag', link: '/admin/reports' },
    { label: 'Subscriptions', icon: 'card_membership', link: '/admin/subscriptions' },
    { label: 'Analytics', icon: 'trending_up', link: '/admin/analytics' },
    { label: 'Technical', icon: 'settings', link: '/admin/technical' },
  ];
  
  constructor(
    private router: Router,
    private authService: AuthService
  ) {}
  
  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
  
  toggleSidenav(): void {
    this.sidenavOpen = !this.sidenavOpen;
  }
}
```

**Template** : `admin-layout.component.html`

```html
<mat-sidenav-container>
  <mat-sidenav #sidenav [opened]="sidenavOpen" mode="side">
    <mat-toolbar color="primary">
      <span>Spawnta Admin</span>
    </mat-toolbar>
    
    <mat-nav-list>
      <mat-list-item 
        *ngFor="let item of navigationItems"
        [routerLink]="item.link"
        routerLinkActive="active">
        <mat-icon matListItemIcon>{{ item.icon }}</mat-icon>
        <span matListItemTitle>{{ item.label }}</span>
      </mat-list-item>
    </mat-nav-list>
  </mat-sidenav>
  
  <mat-sidenav-content>
    <mat-toolbar color="primary">
      <button mat-icon-button (click)="toggleSidenav()">
        <mat-icon>menu</mat-icon>
      </button>
      <span class="spacer"></span>
      
      <button mat-icon-button [matMenuTriggerFor]="menu">
        <mat-icon>account_circle</mat-icon>
      </button>
      <mat-menu #menu="matMenu">
        <button mat-menu-item (click)="logout()">
          <mat-icon>logout</mat-icon>
          <span>Logout</span>
        </button>
      </mat-menu>
    </mat-toolbar>
    
    <div class="main-content">
      <router-outlet></router-outlet>
    </div>
  </mat-sidenav-content>
</mat-sidenav-container>
```

**Tâche** : 3-4 heures

---

#### 5.2 - Dashboard Page (Home avec KPIs)

**Fichier** : `admin/src/app/modules/dashboard/dashboard.component.ts`

```typescript
import { Component, OnInit } from '@angular/core';
import { AdminService } from '../../core/services/admin.service';
import { Chart } from 'chart.js/auto';

@Component({
  selector: 'app-admin-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {
  dashboardData: DashboardData | null = null;
  loading = true;
  
  userChart: Chart | null = null;
  categoryChart: Chart | null = null;
  
  constructor(private adminService: AdminService) {}
  
  ngOnInit(): void {
    this.loadDashboardData();
  }
  
  loadDashboardData(): void {
    this.adminService.getDashboardData().subscribe(
      data => {
        this.dashboardData = data;
        this.loading = false;
        this.initCharts();
      },
      error => {
        console.error('Failed to load dashboard', error);
        this.loading = false;
      }
    );
  }
  
  initCharts(): void {
    if (!this.dashboardData) return;
    
    // User growth chart
    const userCtx = document.getElementById('userChart') as HTMLCanvasElement;
    this.userChart = new Chart(userCtx, {
      type: 'line',
      data: {
        labels: this.dashboardData.trends.userGrowth.map(t => t.date),
        datasets: [{
          label: 'New Users',
          data: this.dashboardData.trends.userGrowth.map(t => t.count),
          borderColor: '#1976d2',
          fill: false
        }]
      }
    });
    
    // Category distribution
    const categoryCtx = document.getElementById('categoryChart') as HTMLCanvasElement;
    this.categoryChart = new Chart(categoryCtx, {
      type: 'bar',
      data: {
        labels: Object.keys(this.dashboardData.activitiesByCategory),
        datasets: [{
          label: 'Activities',
          data: Object.values(this.dashboardData.activitiesByCategory),
          backgroundColor: '#1976d2'
        }]
      }
    });
  }
}

interface DashboardData {
  kpis: {
    activeUsers: number;
    newUsers: number;
    activitiesCreated: number;
    avgRating: number;
    revenue: number;
  };
  trends: {
    userGrowth: TrendData[];
  };
  activitiesByCategory: Record<string, number>;
}

interface TrendData {
  date: string;
  count: number;
}
```

**Template** : `dashboard.component.html`

```html
<div class="dashboard-container">
  <h1>Dashboard</h1>
  
  <!-- KPI Cards -->
  <div class="kpi-grid">
    <mat-card class="kpi-card">
      <mat-card-header>Active Users</mat-card-header>
      <mat-card-content>
        <p class="kpi-value">{{ dashboardData?.kpis.activeUsers }}</p>
      </mat-card-content>
    </mat-card>
    
    <mat-card class="kpi-card">
      <mat-card-header>Activities This Week</mat-card-header>
      <mat-card-content>
        <p class="kpi-value">{{ dashboardData?.kpis.activitiesCreated }}</p>
      </mat-card-content>
    </mat-card>
    
    <mat-card class="kpi-card">
      <mat-card-header>Premium MRR</mat-card-header>
      <mat-card-content>
        <p class="kpi-value">${{ dashboardData?.kpis.revenue }}</p>
      </mat-card-content>
    </mat-card>
    
    <mat-card class="kpi-card">
      <mat-card-header>Avg Rating</mat-card-header>
      <mat-card-content>
        <p class="kpi-value">{{ dashboardData?.kpis.avgRating | number:'1.1-1' }}</p>
      </mat-card-content>
    </mat-card>
  </div>
  
  <!-- Charts -->
  <div class="charts-grid">
    <mat-card>
      <mat-card-header>User Growth</mat-card-header>
      <canvas id="userChart"></canvas>
    </mat-card>
    
    <mat-card>
      <mat-card-header>Activities by Category</mat-card-header>
      <canvas id="categoryChart"></canvas>
    </mat-card>
  </div>
</div>
```

**Tâche** : 5-6 heures

---

**[Les sections 5.3 - 5.9 suivent le même pattern]**

*Pour économiser les tokens, les autres pages (Users, Activities, Reports, Subscriptions, Analytics, Technical) suivent le même modèle avec Material tables, dialogs et services API.*

**Estimé total 5.5** : ~50 heures

---

## Stratégie de Déploiement Phase 5

### Pre-Deployment Checklist

- [ ] Stripe account setup & webhook configured
- [ ] Environment variables (.env) mise à jour
- [ ] Migrations Flyway créées pour nouvelles entités
- [ ] Tests unitaires backend (>80% coverage)
- [ ] Tests E2E frontend (core workflows)
- [ ] Staging deployment & smoke tests
- [ ] Admin user créé manuellement dans DB
- [ ] Stripe webhooks testées en production
- [ ] Monitoring alerts configurés

### Deployment Steps

1. **Backend Deploy**
   ```bash
   # Merge feature/phase5-premium et feature/phase5-admin-backend
   # Tests passent
   # Docker build & push
   # K8s deployment / VPS restart
   ```

2. **Frontend Deploy**
   ```bash
   # Merge feature/phase5-user-premium
   # npm run build --prod
   # Docker build & push
   # Nginx serve updated app
   ```

3. **Admin Deploy**
   ```bash
   # Merge feature/phase5-admin-frontend
   # npm run build --prod
   # Docker build & push
   # Serve on :4300
   ```

4. **Post-Deployment Validation**
   - Test Stripe checkout flow end-to-end
   - Verify webhooks are being processed
   - Check admin dashboard loads without errors
   - Monitor error rates and latency
   - Test suspension/ban workflows

---

## Success Criteria Phase 5

**Backend**
- ✓ 50+ new API endpoints functional
- ✓ Stripe integration tested (testmode & production mode)
- ✓ Admin audit logs working
- ✓ Moderation workflows tested
- ✓ 90% code coverage on critical services

**Frontend User**
- ✓ Paywall flow working
- ✓ Premium badge displays correctly
- ✓ Stats dashboard loads
- ✓ Upgrade CTA appears when limits hit

**Frontend Admin**
- ✓ All 8 dashboard pages functional
- ✓ Data loads without errors
- ✓ User suspend/ban actions work
- ✓ Charts display correctly
- ✓ Reports queue functional

**Infrastructure**
- ✓ No new incidents in logs
- ✓ Response times <500ms for dashboards
- ✓ Stripe webhook latency <2s
- ✓ Admin pages load in <3s

---

## Prochaines Étapes

1. **Créer branches feature** et commencer Étape 2
2. **Stripe setup** (keys, webhooks)
3. **Backend entities** & services
4. **Tests & validation** before frontend
5. **Admin dashboard** (iterative)
6. **E2E tests** et staging validation
7. **Go-live** avec monitoring

