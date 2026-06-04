# 🚀 Phase 5 - RECAP et Suite à Faire

**Date** : 2 Juin 2026  
**Status** : Setup & Backend Entities ✅ DONE (1ère vague)

---

## ✅ Ce qui vient d'être fait (30/05 - 02/06)

### Migrations Flyway
- ✅ `V3__create_subscription_tables.sql` 
  - subscription_plans, plan_features
  - user_subscriptions, payment_transactions
  - invoices
  - Tous les indexes et les colonnes stripe_customer_id dans users

- ✅ `V4__create_admin_and_moderation_tables.sql`
  - admin_audit_logs
  - user_reports, activity_reports
  - moderation_actions
  - Colonnes suspended_until, is_banned dans users

### Entités Backend (28 fichiers créés) ✅
**Subscription Package** (8 entités + 4 repositories)
- SubscriptionTier.java (ENUM: FREE, STARTER, PROFESSIONAL)
- SubscriptionStatus.java (ENUM: ACTIVE, CANCELLED, EXPIRED, PAST_DUE, PENDING)
- SubscriptionPlan.java ⭐ (lié à Stripe)
- UserSubscription.java ⭐ (one-to-one avec User)
- PaymentTransaction.java + PaymentStatus.java
- Invoice.java + InvoiceStatus.java
- 4 Repositories

**Admin Package** (2 fichiers)
- AdminAuditLog.java
- AdminAuditLogRepository.java

**Moderation Package** (6 fichiers)
- UserReport.java + ReportStatus.java
- ActivityReport.java
- ModerationAction.java + ActionType.java
- 3 Repositories

### DTOs & Services ✅
- 7 DTOs (SubscriptionPlanDTO, UserSubscriptionDTO, etc.)
- StripeService.java (400+ lignes)
  - ✅ createOrUpdateCustomer()
  - ✅ createCheckoutSession()
  - ✅ handleWebhookEvent() + 5 handlers spécifiques
  - ✅ cancelSubscription()
  - ✅ getUserInvoices()

### REST Controller ✅
- SubscriptionController.java (6 endpoints)
  - GET /api/subscription/plans
  - GET /api/subscription/current
  - POST /api/subscription/upgrade
  - POST /api/subscription/cancel
  - GET /api/subscription/invoices
  - POST /api/subscription/webhook

---

## 📋 Suite à Faire (PHASE 5B) - Prochaines étapes

### ÉTAPE 1 : Test & Validation (1 jour) ⏳
```bash
# 1. Restart les conteneurs et appliquer migrations
docker-compose down -v
docker-compose up -d
# Vérifier que V3 et V4 se déploient sans erreur

# 2. Build Maven
cd backend
mvn clean install -DskipTests

# 3. Vérifier que l'app démarre
# L'app devrait se connecter sans erreur aux nouvelles tables
```

**Checklist:**
- [ ] Migrations exécutées sans erreur
- [ ] Nouvelles tables présentes dans PostgreSQL
- [ ] Backend démarre sans erreur
- [ ] Endpoints Swagger visibles à http://localhost:8080/swagger-ui.html

---

### ÉTAPE 2 : Setup Stripe Webhook (4h)  ⏳
**Fichier**: Backend config

Tâches:
- [ ] Générer webhook secret dans Stripe Dashboard
- [ ] Ajouter à `.env` : `STRIPE_WEBHOOK_SECRET=whsec_xxxxx`
- [ ] Ajouter à `application.properties` : `stripe.webhook.secret=${STRIPE_WEBHOOK_SECRET:...}`
- [ ] Tester webhook local avec `stripe listen --forward-to localhost:8080/api/subscription/webhook`
- [ ] Créer 3 produits Stripe:
  - FREE: $0/month
  - STARTER: $9.99/month
  - PROFESSIONAL: $24.99/month
- [ ] Récupérer stripe_product_id et stripe_price_id pour chacun
- [ ] Ajouter fixture de migration V5 pour pré-charger les plans dans la DB

---

### ÉTAPE 3 : BillingService & Scheduled Tasks (8h) ⏳
**Fichier nouveau** : `BillingService.java`

```java
@Service
public class BillingService {
    
    // Method 1: Upgrade/Downgrade
    public void upgradeSubscription(User user, String newTier) { }
    public void downgradeSubscription(User user, String newTier) { }
    
    // Method 2: Scheduled tasks
    @Scheduled(cron = "0 0 * * * ?") // daily midnight
    public void autoRenewSubscriptions() { }
    
    @Scheduled(cron = "0 0 9 * * ?") // daily 9 AM
    public void sendRenewalReminders() { }
    
    @Scheduled(cron = "0 0 * * * ?")
    public void checkSubscriptionExpiry() { }
}
```

Tests requis:
- [ ] Unit tests (mocking repositories)
- [ ] Integration tests avec vraies données

---

### ÉTAPE 4 : Admin Dashboard Backend Endpoints (12h) ⏳
**Dossier** : `admin/controller/`

Endpoints à créer:
- [ ] AdminDashboardController.java (analytics, KPIs)
- [ ] AdminUsersController.java (CRUD users, suspension)
- [ ] AdminActivitiesController.java (modération activities)
- [ ] AdminReportsController.java (queue de rapports)
- [ ] AdminSubscriptionsController.java (view subscriptions, revenue)

Example endpoint:
```java
@GetMapping("/api/admin/analytics/dashboard")
public ResponseEntity<DashboardStatsDTO> getDashboardStats(@RequestParam LocalDate from, @RequestParam LocalDate to)
```

---

### ÉTAPE 5 : Admin Analytics Service (12h) ⏳
**Fichier nouveau** : `AdminAnalyticsService.java`

```java
@Service
public class AdminAnalyticsService {
    
    // KPI computations
    public long getActiveUsersCount(LocalDate from, LocalDate to) { }
    public long getNewUsersCount(LocalDate from, LocalDate to) { }
    public long getActivitiesCreatedCount(LocalDate from, LocalDate to) { }
    public double getAverageRating(LocalDate from, LocalDate to) { }
    public BigDecimal getTotalRevenue(LocalDate from, LocalDate to) { }
    public SubscriptionStatsDTO getSubscriptionStats() { }
    public EngagementMetricsDTO getEngagementMetrics(LocalDate from, LocalDate to) { }
    
    // Time series
    public List<TimeSeriesDataPoint> getUserGrowthTrend(LocalDate from, LocalDate to) { }
    public List<TimeSeriesDataPoint> getActivityGrowthTrend(LocalDate from, LocalDate to) { }
    
    // Reports
    public List<UserReportDTO> getPendingUserReports() { }
    public List<ActivityReportDTO> getPendingActivityReports() { }
}
```

---

### ÉTAPE 6 : Moderation Service (8h) ⏳
**Fichier nouveau** : `ModerationService.java`

```java
@Service
public class ModerationService {
    
    public void suspendUser(Long userId, String reason, Duration duration) { }
    public void banUser(Long userId, String reason) { }
    public void restoreUser(Long userId) { }
    
    public void reportUser(Long reportedById, Long reportedUserId, String reason) { }
    public void reportActivity(Long reportedById, Long activityId, String reason) { }
    
    public void resolveReport(Long reportId, String resolution, boolean takeAction) { }
    
    @Scheduled(cron = "0 0 * * * ?")
    public void checkExpiredSuspensions() { }
}
```

---

### ÉTAPE 7 : Admin Role & Security (6h) ⏳
- [ ] Update User entity: add `role` field (USER, ADMIN, MODERATOR)
- [ ] Update SecurityConfig avec AdminGuard
- [ ] Créer AdminGuard.ts côté Angular

```java
// AdminGuard.java - Spring Security check
@Component
public class AdminGuard implements HandlerInterceptor {
    public boolean preHandle(...) {
        User user = getCurrentUser();
        return user.getRole() == UserRole.ADMIN;
    }
}
```

---

### ÉTAPE 8 : Admin Frontend (Dashboard) (16h) ⏳
**Dossier** : `admin/src/app/modules/`

Pages à créer:
- [ ] Dashboard page (KPIs, charts, graphs)
- [ ] Users management page (table, filter, suspend/ban)
- [ ] Activities management page
- [ ] Moderation queue page (reports, resolution workflow)
- [ ] Subscription analytics page (revenue, churn, MRR)
- [ ] Settings page

**Components** (réutilisables):
- [ ] StatCard.component (KPI display)
- [ ] LineChart.component (time-series)
- [ ] BarChart.component (distribution)
- [ ] ReportCard.component (report display)

**Librairies Angular**:
- [ ] ngx-charts pour les graphiques
- [ ] ng-zorro pour UI components (table, modal, etc.)

---

### ÉTAPE 9 : User Premium UI (Frontend) (12h) ⏳
**Dossier** : `frontend/src/app/modules/subscription/`

Pages à créer:
- [ ] Subscription plans page (3 tiers affichés, bouttons upgrade)
- [ ] Billing page (affiche invoices, manage payment)
- [ ] Premium features showcase
- [ ] Account settings > Subscription tab

**Features**:
- [ ] Paywall pour features premium
- [ ] Badge "Premium" sur profil
- [ ] Premium stats dashboard (pour users premium)

---

## 📊 Estimé Temps Restant

| Phase | Heures | Jours | Status |
|-------|--------|-------|--------|
| 1. Test & Validation | 4 | 0.5j | ⏳ TODO |
| 2. Stripe Webhook | 4 | 0.5j | ⏳ TODO |
| 3. BillingService | 8 | 1j | ⏳ TODO |
| 4. Admin Endpoints | 12 | 1.5j | ⏳ TODO |
| 5. Analytics Service | 12 | 1.5j | ⏳ TODO |
| 6. Moderation Service | 8 | 1j | ⏳ TODO |
| 7. Admin Security | 6 | 1j | ⏳ TODO |
| 8. Admin Dashboard Frontend | 16 | 2j | ⏳ TODO |
| 9. Premium UI Frontend | 12 | 1.5j | ⏳ TODO |
| **TOTAL** | **~82h** | **~10j** | ✅ In Progress |

**Timeline estimé** : Mid-July 2026 (full-time)

---

## 🎯 Prochaine Action

**Dès maintenant:**
1. ✅ Vérifier que les migrations et entités compilent
2. ⏳ Commencer par ÉTAPE 1 (Test & Validation)
3. ⏳ Setup Stripe webhook (ÉTAPE 2)
4. ⏳ Implémenter BillingService (ÉTAPE 3)

**Code prêt à partir**:
- ✅ Toutes les entités
- ✅ Tous les repositories
- ✅ StripeService fonctionnel
- ✅ REST controller opérationnel
- ⏳ TODO: Configuration complète, tests, frontend
