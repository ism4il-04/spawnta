# PHASE 5 - TODO & Tracking

**Started**: 2 Juin 2026  
**Estimated Completion**: Mid-June 2026  
**Current Progress**: 85% (Implementation nearly complete)

---

## SETUP & PREPARATION ⚙️

### Setup Week (1-3 Juin)
- [x] **Branch Creation** ✓
- [x] **Stripe Setup** ✓
  - [x] Configure webhook endpoint: `/api/subscription/webhook` ✓
  - [x] Test webhook locally ✓
  - [x] Create test products in Stripe dashboard ✓
- [x] **Backend Structure** ✓
- [x] **Database Migrations** ✓
- [x] **Dependencies** ✓
  - [x] Add Stripe SDK ✓
  - [x] Add Lucide-Angular to admin package.json ✓

---

## BACKEND: PREMIUM & STRIPE PAYMENT 💳

### Phase 5.1 - Entities ✅ DONE
- [x] **SubscriptionTier.java** ✓
- [x] **SubscriptionPlan.java** ✓
- [x] **UserSubscription.java** ✓
- [x] **PaymentTransaction.java** ✓

### Phase 5.2 - Stripe Service ✅ DONE
- [x] **StripeWebhookController.java** ✓ (Robust implementation with fallback parsing)
- [x] **StripeWebhookService.java** ✓ (Update user subscription and dates)
- [x] **Webhook Signature Verification** ✓

### Phase 5.3 - Billing Service ✅ DONE
- [x] **Upgrade/Downgrade logic via Stripe Webhooks** ✓

### Phase 5.4 - REST Endpoints ✅ DONE
- [x] **SubscriptionController.java** ✓
- [x] **Frontend Polling for Status** ✓

---

## BACKEND: ADMIN - ANALYTICS & MODERATION 📊

### Phase 5.5 - Admin Entities ✅ DONE
- [x] **AdminAuditLog.java** ✓
- [x] **UserReport.java** ✓
- [x] **ActivityReport.java** ✓
- [x] **ModerationAction.java** ✓

### Phase 5.6 - Admin Analytics Service ✅ DONE
- [x] **AdminDashboardService.java** ✓ (Real-time aggregation)
- [x] **AdminActivitiesService.java** ✓

### Phase 5.7 - Admin Moderation Service ✅ DONE
- [x] **AdminUserService.java** ✓ (Ban/Suspend logic)
- [x] **AdminModerationService.java** ✓

### Phase 5.8 - Admin REST Endpoints ✅ DONE
- [x] **AdminDashboardController.java** ✓
- [x] **AdminUsersController.java** ✓
- [x] **AdminActivitiesController.java** ✓
- [x] **AdminModerationController.java** ✓
- [x] **JWT Security Filter (Ban enforcement)** ✓

---

## FRONTEND: USER - PREMIUM FEATURES 👤

### Phase 5.10 - Paywall Modal ✅ DONE
- [x] **SubscriptionComponent** ✓ (Stripe Checkout Integration)

---

## FRONTEND: ADMIN DASHBOARD 🎛️

### Phase 5.14 - Admin Layout & Components ✅ DONE
- [x] **DashboardComponent** ✓ (Real-time charts & activities)
- [x] **UsersComponent** ✓ (Management & Moderation)
- [x] **ActivitiesComponent** ✓ (Management)
- [x] **ModerationComponent** ✓ (Queue management)
- [x] **SubscriptionsComponent** ✓ (Revenue tracking)
  - [ ] Redirect to dashboard if not admin

- [ ] **AdminInterceptor** (1h)
  - [ ] Add Authorization header to all admin API calls
  - [ ] Handle 401/403 errors

### Phase 5.15 - Dashboard Page (5-6 heures)
- [ ] **DashboardComponent** (5-6h)
  - [ ] Display 4 KPI cards (active users, activities, revenue, rating)
  - [ ] User growth line chart (last 30 days)
  - [ ] Activities by category bar chart
  - [ ] Revenue trends chart
  - [ ] Community health gauge chart
  - [ ] Quick action buttons (view reports, manage subs)
  - [ ] Refresh button to reload data
  - [ ] Tests: Verify charts render, data loads

### Phase 5.16 - Users Management Page (6-7 heures)
- [ ] **UsersComponent** (6-7h)
  - [ ] Paginated table with sorting/filtering
  - [ ] Columns: ID, Email, Name, Subscription, Status, Created At
  - [ ] Search by email/name
  - [ ] Filter by subscription tier, status (active/suspended/banned)
  - [ ] Row click -> Detail dialog
  - [ ] Detail dialog shows: Profile info, subscription history, reports
  - [ ] Actions: Suspend (with days + reason), Ban, Restore
  - [ ] Confirmation dialogs for destructive actions
  - [ ] Tests: Table pagination, search, filters

### Phase 5.17 - Activities Management Page (5-6 heures)
- [ ] **ActivitiesComponent** (5-6h)
  - [ ] Paginated table with sorting
  - [ ] Columns: ID, Title, Creator, Category, Participants, Created At
  - [ ] Search by title/creator
  - [ ] Filter by category, status (ongoing/finished)
  - [ ] Row click -> Detail modal
  - [ ] Detail modal: Map preview, participants list, chat preview
  - [ ] Delete button (with reason confirmation)
  - [ ] Tests: Table operations, delete action

### Phase 5.18 - Reports & Moderation Queue (6-7 heures)
- [ ] **ReportsComponent** (6-7h)
  - [ ] List all pending reports (user + activity combined)
  - [ ] Display: reported item, reason, reporter, created_at
  - [ ] Detail panel for each report
  - [ ] Approve button -> Select action type (warn/suspend/ban) + duration
  - [ ] Reject button -> Add reason
  - [ ] Status badges (PENDING, RESOLVED, DISMISSED)
  - [ ] Filter by type (user/activity), status, date range
  - [ ] Tests: Report listing, approval flow

### Phase 5.19 - Subscriptions Management (5-6 heures)
- [ ] **SubscriptionsComponent** (5-6h)
  - [ ] Display current plans with pricing
  - [ ] Plan feature table
  - [ ] Subscriber list with pagination
  - [ ] Revenue statistics (MRR, ARPU, churn)
  - [ ] Update plan pricing (admin only)
  - [ ] CSV export of subscribers
  - [ ] Tests: Plan display, data loads

### Phase 5.20 - Analytics & Reporting Page (7-8 heures)
- [ ] **AnalyticsComponent** (7-8h)
  - [ ] Advanced dashboard with multiple charts
  - [ ] Metric selector (user growth, activity creation, revenue, etc.)
  - [ ] Date range picker
  - [ ] Time series visualizations
  - [ ] Cohort analysis table
  - [ ] Custom report builder (basic)
  - [ ] CSV/PDF export options
  - [ ] Tests: Chart rendering, export functionality

### Phase 5.21 - Technical Dashboard (5-6 heures)
- [ ] **TechnicalComponent** (5-6h)
  - [ ] Service health status (Backend, DB, Redis, Kafka)
  - [ ] System metrics: CPU, Memory, Disk usage
  - [ ] API metrics: Request rate, error rate, response times
  - [ ] Recent logs viewer (tail from backend)
  - [ ] Feature flags toggle (optional)
  - [ ] Uptime graph (last 24h)
  - [ ] Tests: Health check display, log fetching

### Phase 5.22 - Auth & Permissions (2-3 heures)
- [ ] **Admin Role Check** (1h)
  - [ ] Verify user role is ADMIN before allowing access
  - [ ] Store admin info in JWT claims
  - [ ] Tests: Role validation

- [ ] **Route Guards** (1h)
  - [ ] Protected routes with admin guard
  - [ ] Redirect unauthenticated users
  - [ ] Tests: Guard routing logic

- [ ] **Admin Creation** (1h)
  - [ ] Manual DB query or endpoint to create admin users
  - [ ] Document process for team

---

## TESTING & QA 🧪

### Backend Testing (3-4 heures)
- [ ] Unit tests for services (80%+ coverage)
  - [ ] StripeService mocked tests
  - [ ] BillingService state transition tests
  - [ ] AdminAnalyticsService calculation tests
  - [ ] AdminModerationService action tests

- [ ] Integration tests for endpoints
  - [ ] Subscription endpoints
  - [ ] Admin endpoints
  - [ ] Webhook handling

- [ ] Database tests
  - [ ] Migration verification
  - [ ] Query performance checks

### Frontend Testing (2-3 heures)
- [ ] Unit tests for components
  - [ ] Component initialization
  - [ ] Service mocking
  - [ ] User interactions

- [ ] E2E tests (Cypress)
  - [ ] Paywall flow end-to-end
  - [ ] Admin dashboard navigation
  - [ ] User suspension flow

### API Testing (2-3 heures)
- [ ] Postman collection for all endpoints
- [ ] Test with dev/staging credentials
- [ ] Verify error responses
- [ ] Test pagination/filtering

---

## DEPLOYMENT & LAUNCH 🚀

### Pre-Deployment (1-2 heures)
- [ ] All tests passing locally
- [ ] Code review completed
- [ ] Security review (especially admin endpoints)
- [ ] Performance benchmarks OK
- [ ] Stripe webhooks tested in test mode

### Staging Deployment (2-3 heures)
- [ ] Deploy backend to staging
- [ ] Deploy frontend to staging
- [ ] Deploy admin to staging
- [ ] Smoke tests on staging
- [ ] Stripe testing in live mode (but on test data)

### Production Deployment (1-2 heures)
- [ ] Merge all feature branches to main
- [ ] Tag release version
- [ ] Build Docker images
- [ ] Deploy to production (rolling update)
- [ ] Monitor error rates & performance
- [ ] Verify Stripe webhooks in production

### Post-Launch (1 hour)
- [ ] Monitor logs for errors
- [ ] Check Stripe webhook delivery
- [ ] Verify admin users can access dashboard
- [ ] Test payment flow with real card (small amount)
- [ ] Announce availability to team

---

## STATUS TRACKING

### Completion Checklist
- [ ] Backend: Premium & Stripe _______ % (Target: 100%)
- [ ] Backend: Admin _______ % (Target: 100%)
- [ ] Frontend: User Premium _______ % (Target: 100%)
- [ ] Frontend: Admin Dashboard _______ % (Target: 100%)
- [ ] Tests _______ % (Target: >80% coverage)
- [ ] Documentation _______ % (Target: 100%)
- [ ] Deployment _______ % (Target: 100%)

### Weekly Checkpoints
- [ ] Week 1 (9 Juin): Backend entities + Stripe service
- [ ] Week 2 (16 Juin): Admin services + endpoints
- [ ] Week 3 (23 Juin): Frontend user premium + admin layout
- [ ] Week 4 (30 Juin): Admin dashboard pages complete
- [ ] Week 5 (7 Juillet): Testing & staging validation
- [ ] Week 6 (14 Juillet): Production launch

---

## NOTES & DECISIONS

**Stripe Implementation**
- Using test mode for development
- Webhook endpoint: `/api/subscription/webhook`
- Webhook secret: Stored in .env as `STRIPE_WEBHOOK_SECRET`

**Admin Users**
- Role set manually via database for now
- Future: Create admin user endpoint (super-admin only)

**Moderation**
- Soft moderation preferred: suspend before ban
- Suspension is always temporary (expires)
- Ban is permanent until admin restores

**Analytics**
- Data cached in Redis for performance
- Cache expires every 1 hour
- Admin can refresh manually for real-time

**Deployment**
- Feature branches merged to develop first
- develop tested on staging
- main branch only for production releases
- Tags: v1.5.0, v1.5.1, etc.

