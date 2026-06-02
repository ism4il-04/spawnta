# SPAWNTA - Roadmap & Planning

> Derniere mise a jour : 2 Juin 2026 - Phase 5 Planning Document

---

## Stack

- API : Spring Boot 4 + Spring Data JPA + Spring Security + Stripe SDK
- Frontend : Angular 21 + Angular Material + Leaflet + ngx-charts
- Admin : Angular 21 + Angular Material + Chart.js
- Geo : PostgreSQL 15 + PostGIS
- Temps reel : WebSockets + Kafka
- Media : Cloudinary
- Paiement : Stripe API
- Monitoring : Spring Boot Admin, Actuator, Logstash

---

## Phases

### Phase 0 - Infrastructure et fondations ✅
Statut : Termine

- Setup backend Spring Boot 4 (Java 21)
- Setup frontend Angular 21
- Setup admin Angular 21 (skeleton)
- Docker Compose dev/prod
- PostgreSQL/PostGIS
- Migrations Flyway
- Base CI/CD GitHub Actions

### Phase 1 - Identite, profil et securite ✅
Statut : Termine

- JWT + refresh token (3600s + 7j)
- login / signup / logout
- verification email (template-based)
- profil riche (bio, reseaux, interets, galerie)
- avatar (Cloudinary ready)
- confidentialite et role-based access

### Phase 2 - Activites et cartographie interactive ✅
Statut : Termine

#### Backend
- [x] Support activites `MEETUP` et `TRIP`
- [x] Recherche geospatiale `ST_DWithin`
- [x] Recuperation par rayon avec filtres
- [x] Filtres : categorie, date, type, mode participation
- [x] Participation `DIRECT` et `APPROVAL`
- [x] Message d'introduction pour demandes
- [x] Gestion capacite max / illimitee
- [x] Limite creation hebdo (utilisateur gratuit)
- [x] Approbation demandes par hote

#### Frontend
- [x] Carte Leaflet integree avec zoom intelligent
- [x] Affichage dynamique meetups et trajets
- [x] Creation d'activite depuis la carte (click -> modal)
- [x] Detail d'activite avec tout le contexte
- [x] Filtres cartes (categorie, date, rayon, type)
- [x] UI redimensionnee et harmonisee au theme gradient

### Phase 3 - Social temps reel et pipeline evenementiel ✅
Statut : Termine

- [x] Chat groupe intra-activite (WebSocket)
- [x] Chat prive 1-to-1 (WebSocket)
- [x] Historique messages persistant
- [x] Status lecture des messages
- [x] Topics Kafka : activity.created, user.joined, etc.
- [x] Notifications temps reel (WebSocket push)
- [x] Pipeline Kafka durable pour business events
- [x] Inbox notifications avec filtres

### Phase 4 - Post-sortie, gamification et IA ✅
Statut : Termine

- [x] Entites gamification (User XP/level, UserAchievement, UserLevelHistory)
- [x] Service XP & leveling (awards systematiques)
- [x] Validation presence : QR code check-in/out
- [x] Entite Attendance avec time tracking
- [x] Systeme notation activites (1-5 stars, reviews)
- [x] Moteur recommandations : scoring interets/distance/popularity
- [x] Pipeline notifications temps reel (Kafka + WebSocket)
- [x] Endpoints REST gamification securises
- [x] UI dashboard XP/badges/levels
- [x] UI check-in & rating composants
- [x] UI feed recommandations avec scroll infini
- [x] Inbox notifications avec actions (marquer lu, supprimer)

### Phase 5 - Monetisation et Admin Dashboard 🚀
Statut : En cours

#### Scope

**Premium Subscription System** : Plans, paiement Stripe, billing
**Admin Dashboard Metier** : Analytics, users, activities, moderation queue
**Admin Dashboard Technique** : Health, logs, metrics
**Moderation & Sante Communaute** : Reports, actions, suspension/ban
**Frontend Features** : Paywall, premium badges, stats personalisees

#### Deliverables

```
BACKEND (Java Spring Boot)
├── 5.1.1 - Entites Subscription (User, SubscriptionPlan, PaymentTransaction)
├── 5.1.2 - Stripe Service Integration
├── 5.1.3 - Invoice & Billing Logic
├── 5.1.4 - Endpoints Premium (/subscription/*, /billing/*)
├── 5.2.1 - Entites Admin Audit & Reporting
├── 5.2.2 - AdminAnalyticsService (KPIs, dashboards)
├── 5.2.3 - AdminAuditService (logs)
├── 5.2.4 - AdminModerationService (reports, actions)
├── 5.2.5 - Endpoints Admin protegees (/admin/analytics, /admin/users, etc)
└── 5.3.1 - Batch Job (auto-reactivation suspension)

FRONTEND (Angular User)
├── 5.4.1 - Paywall Modal & CTA Upgrade
├── 5.4.2 - Premium Badge Display (profiles, listings)
├── 5.4.3 - Premium Stats Dashboard (utilisateur premium)
├── 5.4.4 - Limits Indicator & Upgrade CTA
└── 5.4.5 - Profile Status Display (banned/suspended)

ADMIN FRONTEND (Angular)
├── 5.5.1 - Sidebar Navigation & Layout
├── 5.5.2 - Dashboard Page (KPI cards, charts)
├── 5.5.3 - Users Page (tableau, recherche, details, actions)
├── 5.5.4 - Activities Page (tableau, filtres, suppression)
├── 5.5.5 - Reports Page (moderation queue, voting)
├── 5.5.6 - Subscriptions Page (plans, stats, billing trends)
├── 5.5.7 - Analytics Page (graphes users/activities/revenue over time)
├── 5.5.8 - Technical Page (health checks, logs, metrics)
└── 5.5.9 - Permissions & Role Guards (admin-only access)
```

---

## Phases Details

### Phase 5.1 : Backend Premium & Paiement

#### 5.1.1 - Entites Subscription
- Enum `SubscriptionTier` : FREE, STARTER, PROFESSIONAL
- Entity `SubscriptionPlan` : id, tier, name, price, features[], createdAt
- Entity `UserSubscription` : user, plan, startDate, endDate, isActive, stripeCustomerId
- Entity `PaymentTransaction` : user, amount, currency, status, stripePaymentIntentId, timestamp
- Entity `Invoice` : user, amount, period, status, pdfUrl, timestamp
- Update Entity `User` : currentSubscription (FK), subscriptionStartDate, subscriptionEndDate

**Tache** : 4-5 heures + 1h tests unitaires

#### 5.1.2 - Stripe Service Integration
```java
// StripeService
- createOrUpdateCustomer(User) -> stripeCustomerId
- createCheckoutSession(User, SubscriptionPlan) -> sessionUrl
- handleWebhookEvent(StripeEvent) -> process payment/cancellation
- cancelSubscription(User)
- getInvoices(User) -> List<Invoice>
```

**Tache** : 5-6 heures + 1h tests integration

#### 5.1.3 - Invoice & Billing Logic
```java
// BillingService
- generateInvoice(User, SubscriptionPlan, date) -> Invoice entity
- upgradeSubscription(User, newTier)
- downgradeSubscription(User, newTier)
- renewSubscription(User) [batch job]
- issueRefund(PaymentTransaction) -> RefundRequest
```

**Tache** : 3-4 heures + 30min tests

#### 5.1.4 - Endpoints Premium
```
POST   /api/subscription/plans              -> List<SubscriptionPlan>
GET    /api/subscription/current            -> UserSubscription
POST   /api/subscription/upgrade            -> (planId) -> checkout URL
POST   /api/subscription/cancel             -> Boolean
GET    /api/subscription/invoices           -> List<Invoice>
POST   /api/subscription/refund             -> (transactionId) -> RefundRequest
GET    /api/subscription/{userId}           -> UserSubscription [ADMIN]
```

**Tache** : 3-4 heures + 1h tests integration

---

### Phase 5.2 : Backend Admin - Data & Analytics

#### 5.2.1 - Entites Admin Audit & Reporting
- Entity `AdminAuditLog` : adminId, action, targetType, targetId, details, timestamp
- Entity `UserReport` : reportedBy, reportedUser, reason, status (PENDING/RESOLVED), timestamp
- Entity `ActivityReport` : reportedBy, reportedActivity, reason, status, timestamp
- Entity `ModerationAction` : admin, targetUser/Activity, actionType (WARN/SUSPEND/BAN), duration, timestamp

**Tache** : 2-3 heures

#### 5.2.2 - AdminAnalyticsService
```java
// Calculs KPIs
- getActiveUsersCount(dateRange)
- getNewUsersCount(dateRange)
- getActivitiesCreatedCount(dateRange)
- getAverageRating()
- getTotalRevenue(dateRange)
- getSubscriptionStats() -> {totalPremium, churnRate, ...}
- getEngagementMetrics() -> {chatVolume, checkInRate, ...}
- getUsersByRegion() -> geo stats
- getMostPopularCategories() -> top N
- getGrowthTrends(metric, dateRange) -> time series
```

**Tache** : 5-6 heures + 2h tests + query optimization

#### 5.2.3 - AdminAuditService
```java
// Logging & filtering
- logAction(admin, action, target, details)
- getAuditLogs(filters: admin, action, dateRange, limit) -> Page
- exportAuditLogs(format: CSV/JSON)
```

**Tache** : 2-3 heures + 1h tests

#### 5.2.4 - AdminModerationService
```java
// Moderation workflows
- submitReport(user, type, target, reason)
- getReportsQueue(status, sort) -> Page
- approveReport(reportId, action, duration)
- rejectReport(reportId, reason)
- suspendUser(userId, duration)
- banUser(userId, reason)
- restoreUser(userId)
- deleteActivity(activityId, reason)
- issueWarning(userId, reason)
```

**Tache** : 6-7 heures + 1h tests

#### 5.2.5 - Endpoints Admin Protegees
```
GET    /api/admin/analytics                 -> Analytics dashboard data
GET    /api/admin/analytics/export          -> CSV/JSON time series
GET    /api/admin/users                     -> Page<User> (search, sort, filters)
GET    /api/admin/users/{id}                -> User detail + subscription + reports
POST   /api/admin/users/{id}/suspend        -> (days)
POST   /api/admin/users/{id}/ban            -> ()
POST   /api/admin/users/{id}/restore        -> ()
GET    /api/admin/activities                -> Page<Activity> (search, filters)
DELETE /api/admin/activities/{id}           -> (reason)
GET    /api/admin/reports                   -> Page<Report> (type, status, sort)
POST   /api/admin/reports/{id}/approve      -> (action, duration)
POST   /api/admin/reports/{id}/reject       -> (reason)
GET    /api/admin/audit-logs                -> Page<AuditLog> (search, dateRange)
```

**Tache** : 4-5 heures + 1h tests

---

### Phase 5.3 : Backend Batch Jobs & Cron

#### 5.3.1 - Scheduled Tasks
```java
// SubscriptionBatchService
@Scheduled(cron = "0 0 0 * * *")
- autoRenewSubscriptions() // Renouvelle subscriptions expirees
- autoReactivateSuspendedUsers() // Reactivation apres suspension tempo
- checkSubscriptionExpiry() // Email notifications
- generateMonthlyInvoices() // Auto-invoicing

// CacheWarmupService
@Scheduled(cron = "0 0 6 * * *")
- preloadAnalyticsCache() // Pour faster dashboard
```

**Tache** : 3-4 heures + 1h tests

---

### Phase 5.4 : Frontend User - Premium Features

#### 5.4.1 - Paywall Modal & CTA Upgrade
- Modal avec affichage plans (FREE, STARTER, PROFESSIONAL)
- Compare features par plan
- Button "Upgrade Now" -> Stripe checkout
- Success page confirmation after payment

**Tache** : 3-4 heures

#### 5.4.2 - Premium Badge Display
- Badge "⭐ Premium" affiche sur profile utilisateur
- Badge sur listings activites si createur premium
- Styling avec Material badge

**Tache** : 1-2 heures

#### 5.4.3 - Premium Stats Dashboard
- Page `/premium-stats` pour utilisateurs premium
- Charts : activities created, people joined, earnings, etc.
- Export option (CSV)

**Tache** : 4-5 heures

#### 5.4.4 - Limits Indicator & Upgrade CTA
- Affichage : "You have 2/5 activities left this week"
- Button CTA "Upgrade to unlimited" si limite atteinte
- Integration au create activity workflow

**Tache** : 2-3 heures

#### 5.4.5 - Profile Status Display
- "User suspended until [date]" warning
- "User banned" message si ban permanent
- Block interactions si user banned

**Tache** : 2-3 heures

---

### Phase 5.5 : Admin Frontend - Dashboard Complet

#### 5.5.1 - Layout Architecture
- Sidebar collapsible avec navigation (Material Sidenav)
- Main content area avec breadcrumbs
- Header avec user profile dropdown + logout
- Responsive design (mobile-friendly)

**Tache** : 3-4 heures

#### 5.5.2 - Dashboard Metier (Home Page)
- KPI Cards : Active users, Activities this week, Premium MRR, Avg rating
- Charts :
  - Line chart : Users growth (last 30 days)
  - Bar chart : Activities by category
  - Pie chart : Revenue by subscription tier
  - Gauge : Community health score
- Quick actions : View reports, Manage subscriptions

**Tache** : 5-6 heures (ngx-charts integration)

#### 5.5.3 - Users Management Page
- Paginated table : id, name, email, subscription, status, created_at
- Search by email/name
- Filters : subscription_tier, status (active/suspended/banned)
- Column sorting
- Detail modal : view user, subscription history, reports against
- Actions : Suspend, Ban, Restore, View profile

**Tache** : 6-7 heures (Material table, inline editing)

#### 5.5.4 - Activities Management Page
- Paginated table : id, title, creator, category, participants, created_at
- Search by title/creator
- Filters : category, status (ongoing/finished)
- Detail modal : map preview, participants list, chat preview
- Actions : View detail, Delete (with reason modal)

**Tache** : 5-6 heures

#### 5.5.5 - Reports & Moderation Queue
- List of pending reports : reported item, reason, reporter, created_at
- Detail panel : full context, comment thread
- Actions : Approve (select action type), Reject (with reason)
- Status tracking : pending -> reviewed -> resolved
- Filters by : type (user/activity), status, date range

**Tache** : 6-7 heures

#### 5.5.6 - Subscriptions Management
- Plans management : display current plans, pricing, feature compare
- Update plan pricing & features (with confirmation)
- Revenue stats : MRR, ARPU, churn rate, LTV
- Customers list : subscriber info, renewal dates, next billing
- Export subscription data

**Tache** : 5-6 heures

#### 5.5.7 - Analytics & Reporting
- Advanced analytics page avec multiples charts
- Metrics dashboard : select metric, date range
- Time series view : User signups, Activity creation, Revenue
- Cohort analysis : retention, churn
- Custom report builder (basic)
- Export options : CSV, PDF

**Tache** : 7-8 heures

#### 5.5.8 - Technical Dashboard
- Health checks : Status chaque service (Backend, DB, Redis, Kafka)
- System metrics : CPU, Memory, Disk usage
- API metrics : Request rate, Error rate, Response time
- Recent logs : live tail (via backend actuator)
- Feature flags management

**Tache** : 5-6 heures

#### 5.5.9 - Permissions & Auth
- Guard route : admin-only (check role)
- Logout + session management
- Profile dropdown avec settings
- Admin role must be set manually in DB or via create admin endpoint

**Tache** : 2-3 heures

---

## Phase 5 Timeline Estimate

### Sprint 1 (Week 1-2)
- 5.1.1 : Entities (4h)
- 5.1.2 : Stripe Service (6h)
- 5.2.1 : Admin Entities (3h)
- 5.2.2 : Analytics Service (6h)
- **Subtotal : 19h**

### Sprint 2 (Week 3-4)
- 5.1.3 : Billing Logic (4h)
- 5.1.4 : Premium Endpoints (4h)
- 5.2.3 : Audit Service (3h)
- 5.2.4 : Moderation Service (7h)
- 5.2.5 : Admin Endpoints (5h)
- **Subtotal : 23h**

### Sprint 3 (Week 5-6)
- 5.3.1 : Batch Jobs (4h)
- 5.4 : Frontend Premium Features (13h)
- Backend Testing & Integration (4h)
- **Subtotal : 21h**

### Sprint 4 (Week 7-9)
- 5.5 : Admin Frontend (50h total)
  - Layout (4h)
  - Dashboard (6h)
  - Users (7h)
  - Activities (6h)
  - Reports (7h)
  - Subscriptions (6h)
  - Analytics (8h)
  - Technical (6h)
  - Auth & Guards (3h)
- **Subtotal : 50h**

### Total Phase 5 Estimate
- **Backend** : ~37 hours
- **Frontend User** : ~13 hours
- **Frontend Admin** : ~50 hours
- **Testing & Integration** : ~10 hours
- **TOTAL** : ~110 hours (~2.5-3 weeks full-time, ~4-5 weeks half-time)

---

## 📚 Documentation

- Detailed spec : `/docs/Spawnta_Detailed_Specifications.md`
- Technical spec : `/docs/Spawnta_Technical_Specifications.md`
- Current progress : This file (planning.md)
- Status snapshot : `avancement.md`

---

## Priorites Phase 5

**Must-Have (MVP)**
1. Stripe integration + subscription entities
2. Admin dashboard analytics + user management
3. Moderation queue + suspend/ban actions
4. Premium paywall + CTA in UI

**Should-Have**
5. Premium stats dashboard
6. Technical dashboard
7. Reports & moderation workflows

**Nice-to-Have**
8. Export reports
9. Feature flags management
10. Advanced cohort analysis

