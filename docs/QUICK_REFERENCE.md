# 🗺️ PHASE 5 - QUICK REFERENCE & FILE MAP

**Your Phase 5 Implementation is Here ↓**

---

## 📁 Backend Entities (28 files)

### Subscription Package
```
backend/src/main/java/com/spawnta/subscription/
├── entity/
│   ├── SubscriptionTier.java .......................... Enum (FREE, STARTER, PROFESSIONAL)
│   ├── SubscriptionStatus.java ...................... Enum (ACTIVE, CANCELLED, EXPIRED, PAST_DUE)
│   ├── SubscriptionPlan.java ...................... Entity + Stripe linking
│   ├── UserSubscription.java ....................... Entity (one-to-one with User)
│   ├── PaymentStatus.java .............................. Enum
│   ├── PaymentTransaction.java ..................... Entity + Stripe tracking
│   ├── InvoiceStatus.java ............................... Enum
│   └── Invoice.java .................................... Entity + Invoice history
├── dto/
│   ├── SubscriptionPlanDTO.java
│   ├── UserSubscriptionDTO.java
│   ├── PaymentTransactionDTO.java
│   ├── InvoiceDTO.java
│   ├── UpgradeSubscriptionRequest.java ........... @RequestBody
│   ├── CheckoutSessionResponse.java .............. Checkout URL response
│   └── CancelSubscriptionRequest.java ........... Cancellation request
├── repository/
│   ├── SubscriptionPlanRepository.java ........... findByTier(), findByStripeProductId()
│   ├── UserSubscriptionRepository.java ........... findByUserId(), findByStatus()
│   ├── PaymentTransactionRepository.java ........ findByUserId(), findByStatus()
│   └── InvoiceRepository.java ........................ findByUserId(), findByStatus()
├── service/
│   └── StripeService.java (400+ lines) ............ ⭐ Main service
│       ├── createOrUpdateCustomer()
│       ├── createCheckoutSession()
│       ├── handleWebhookEvent() [5 handlers]
│       ├── cancelSubscription()
│       └── getUserInvoices()
└── controller/
    └── SubscriptionController.java (6 endpoints)
        ├── GET    /api/subscription/plans
        ├── GET    /api/subscription/current
        ├── POST   /api/subscription/upgrade
        ├── POST   /api/subscription/cancel
        ├── GET    /api/subscription/invoices
        └── POST   /api/subscription/webhook
```

### Admin Package
```
backend/src/main/java/com/spawnta/admin/
├── entity/
│   └── AdminAuditLog.java ......................... Audit trail entity
├── repository/
│   └── AdminAuditLogRepository.java .............. findByAdminId(), findByAction()
└── service/
    └── (TODO) AdminAnalyticsService.java ........ KPIs, revenue, growth trends
```

### Moderation Package
```
backend/src/main/java/com/spawnta/moderation/
├── entity/
│   ├── ReportStatus.java .......................... Enum (OPEN, INVESTIGATING, RESOLVED, DISMISSED)
│   ├── UserReport.java ............................ Reported user
│   ├── ActivityReport.java ........................ Reported activity
│   ├── ActionType.java ............................ Enum (WARN, SUSPEND, BAN, RESTORE)
│   └── ModerationAction.java ..................... Moderation actions
├── repository/
│   ├── UserReportRepository.java
│   ├── ActivityReportRepository.java
│   └── ModerationActionRepository.java
└── service/
    └── (TODO) ModerationService.java ............ Suspend, ban, resolve reports
```

---

## 🗄️ Database Migrations

```
backend/src/main/resources/db/migration/
├── V1__create_users_table.sql ..................... Original
├── V2__expand_users_and_add_social_interests.sql  Original
├── V3__create_subscription_tables.sql ✅ NEW
│   └── Tables: subscription_plans, plan_features, user_subscriptions,
│            payment_transactions, invoices
├── V4__create_admin_and_moderation_tables.sql ✅ NEW
│   └── Tables: admin_audit_logs, user_reports, activity_reports,
│            moderation_actions
└── V5__insert_subscription_plans.sql (TODO)
    └── Insert FREE, STARTER, PROFESSIONAL plans
```

---

## 📚 Documentation Files (New)

```
docs/
├── PHASE_5_DETAILED_PLAN.md ..................... Full 30-page plan
├── PHASE_5_RECAP.md ............................. 10-day implementation roadmap
├── NEXT_STEPS.md ................................ Precise bash commands + setup
├── PHASE_5_EXECUTIVE_SUMMARY.md ............... What was delivered + timeline
└── Spawnta_Technical_Specifications.md ....... System architecture

Root:
├── PHASE_5_SUMMARY.md ........................... Quick reference
├── PHASE_5_TODO.md ............................. Updated checklist
└── AVANCEMENT.md ............................... Global progress (updated)
```

---

## 🔧 Configuration Files (Updated)

```
Root:
└── .env ........................................ Added STRIPE keys already ✅

backend/src/main/resources/
└── application.properties ...................... Added stripe config ✅

backend/
└── pom.xml ..................................... Stripe dependency added ✅
```

---

## 🎯 Which Files to Test First?

### 1. Verify Compilation
```bash
cd backend
mvn clean compile
# Should compile without errors
```

### 2. Verify Database Migrations
```bash
docker-compose restart backend
# Check logs for V3 and V4 execution
docker logs spawnta-backend | grep "Flyway"
```

### 3. Verify Endpoints in Swagger
```
http://localhost:8080/swagger-ui.html
→ Look for "Subscription" section (should show 6 endpoints)
```

### 4. Test Endpoints
```bash
# Get plans (no auth needed)
curl http://localhost:8080/api/subscription/plans

# Get current subscription (auth required)
curl -H "Authorization: Bearer YOUR_JWT" \
  http://localhost:8080/api/subscription/current
```

---

## 📊 File Statistics

| Type | Count | LOC | Where |
|------|-------|-----|-------|
| Entities | 13 | 1,200 | `subscription/entity/`, `admin/entity/`, `moderation/entity/` |
| Repositories | 8 | 200 | `*/repository/` |
| Services | 1 | 400 | `subscription/service/StripeService.java` |
| Controllers | 1 | 250 | `subscription/controller/` |
| DTOs | 7 | 300 | `subscription/dto/` |
| Migrations | 2 | 260 | `db/migration/V3 & V4` |
| **Total** | **33** | **2,500+** | |

---

## 🚀 What's Ready vs What's TODO

### ✅ READY NOW
- All entities and repositories
- StripeService (full)
- SubscriptionController (6 endpoints)
- DTOs for validation
- Database schema
- Stripe API integration

### ⏳ TODO NEXT
- BillingService
- AdminAnalyticsService
- ModerationService
- Unit tests
- Integration tests
- Admin controllers
- Frontend (dashboard + premium UI)

---

## 🔗 Key Integration Points

### Frontend → Backend
```
Frontend                              Backend
  ↓
/subscription/plans         →   GET /api/subscription/plans
/subscription/current       →   GET /api/subscription/current
[Upgrade Button]            →   POST /api/subscription/upgrade
  ↓
[Stripe Checkout Modal]     ←   {sessionId, checkoutUrl}
```

### Stripe Webhook → Backend
```
Stripe                              Backend
  ↓
Webhook Event                   →   POST /api/subscription/webhook
(customer.subscription.created)
  ↓
[Verify Signature]
  ↓
[Call handleWebhookEvent()]
  ↓
[Update UserSubscription in DB]
```

---

## 💾 Key Queries

```sql
-- See new tables
\dt subscription_plans, user_subscriptions, admin_audit_logs

-- See migrations
SELECT * FROM flyway_schema_history WHERE version > '2';

-- Test data (after V5 migration)
SELECT * FROM subscription_plans;
SELECT * FROM plan_features;
```

---

## 📞 Troubleshooting

| Issue | Solution |
|-------|----------|
| Migration not running | `docker-compose restart backend` → check logs |
| Endpoints not visible | Refresh Swagger: `http://localhost:8080/swagger-ui.html?urls.primaryName=springdoc` |
| 401 on auth endpoints | Need valid JWT token in Authorization header |
| Stripe API errors | Check `.env` for correct `STRIPE_PUBLIC_KEY` and `STRIPE_SECRET_KEY` |

---

## 📋 Next Action Items

1. [ ] **Today**: Verify everything compiles and deploys
2. [ ] **Tomorrow**: Setup Stripe webhook + create products
3. [ ] **This week**: Implement BillingService
4. [ ] **Next week**: Admin analytics & endpoints
5. [ ] **Week 3**: Moderation service
6. [ ] **Week 4-5**: Frontend

---

**Last Updated**: 02 Juin 2026  
**For Questions**: See NEXT_STEPS.md
