# PHASE 5 - TODO & Tracking

**Started**: 2 Juin 2026  
**Estimated Completion**: Mid-July 2026  
**Current Progress**: 0% (Planning complete, implementation starting)

---

## SETUP & PREPARATION ⚙️

### Setup Week (1-3 Juin)
- [x] **Branch Creation** (1h) - DONE
  - [x] Create feature/phase5-premium
  - [x] Create feature/phase5-admin-backend
  - [x] Create feature/phase5-user-premium
  - [x] Create feature/phase5-admin-frontend
  - [x] Setup branch protection rules

- [x] **Stripe Setup** (2h) - DONE 
  - [x] Create Stripe account
  - [x] Get API keys (publishable + secret) ✓ Keys in .env
  - [x] Add to .env file: `STRIPE_PUBLIC_KEY`, `STRIPE_SECRET_KEY` ✓ Done
  - [ ] Configure webhook endpoint: `/api/subscription/webhook` - TODO
  - [ ] Test webhook locally: `stripe listen --forward-to localhost:8080/api/webhooks/stripe` - TODO
  - [ ] Create test products in Stripe dashboard - TODO

- [x] **Backend Structure** (1h) - DONE
  - [x] Create package: `com.spawnta.subscription` ✓
  - [x] Create package: `com.spawnta.admin` ✓
  - [x] Create package: `com.spawnta.moderation` ✓
  - [x] Create DTOs folder for each package ✓

- [x] **Database Migrations** (2h) - DONE
  - [x] Create Flyway migration for subscription entities ✓ (V3__create_subscription_tables.sql)
  - [x] Create Flyway migration for admin audit tables ✓ (V4__create_admin_and_moderation_tables.sql)
  - [x] Create Flyway migration for reports/moderation tables ✓ (same as above)
  - [ ] Test migrations locally - TODO (on next docker restart)

- [x] **Dependencies** (1h) - DONE
  - [x] Add Stripe SDK to pom.xml (`stripe/stripe-java`) ✓
  - [ ] Add Chart.js to admin package.json - TODO
  - [ ] Add ngx-charts to admin package.json - TODO
  - [ ] Run `mvn clean install` and `npm install` - TODO

---

## BACKEND: PREMIUM & STRIPE PAYMENT 💳

### Phase 5.1 - Entities (4-5 heures)
- [ ] **SubscriptionTier.java** (30 min)
  - [ ] Enum: FREE, STARTER, PROFESSIONAL
  - [ ] Properties: id, monthlyPrice, activityLimit

- [ ] **SubscriptionPlan.java** (1h)
  - [ ] Entity with Stripe IDs
  - [ ] Plan features collection
  - [ ] Repository & tests

- [ ] **UserSubscription.java** (1h)
  - [ ] One-to-one with User
  - [ ] Stripe subscription tracking
  - [ ] Status enum (ACTIVE, CANCELLED, EXPIRED)
  - [ ] Repository & tests

- [ ] **PaymentTransaction.java** (1h)
  - [ ] Stripe payment intent tracking
  - [ ] Status enum
  - [ ] Repository & tests

- [ ] **Invoice.java** (1h)
  - [ ] Stripe invoice tracking
  - [ ] Invoice status & PDF URL
  - [ ] Repository & tests

### Phase 5.2 - Stripe Service (5-6 heures)
- [ ] **StripeService.java** (5-6h)
  - [ ] `createOrUpdateCustomer()` - Create Stripe customer from User
  - [ ] `createCheckoutSession()` - Generate Stripe checkout URL
  - [ ] `handleStripeWebhook()` - Process webhook events
  - [ ] `cancelSubscription()` - Handle cancellation
  - [ ] `getInvoices()` - Retrieve invoices
  - [ ] Tests: Unit tests for each method (mocked Stripe API)

- [ ] **Webhook Signature Verification** (1h)
  - [ ] Import Stripe webhook library
  - [ ] Implement signature validation
  - [ ] Error handling for invalid signatures

### Phase 5.3 - Billing Service (3-4 heures)
- [ ] **BillingService.java** (3-4h)
  - [ ] `upgradeSubscription()` - Upgrade user to new tier
  - [ ] `downgradeSubscription()` - Downgrade user
  - [ ] `generateInvoice()` - Create invoice record
  - [ ] `autoRenewSubscriptions()` - Scheduled renewal task
  - [ ] Tests: Subscription state transitions

- [ ] **Scheduled Tasks** (1h)
  - [ ] `@Scheduled autoRenewSubscriptions()` (daily at midnight)
  - [ ] `@Scheduled sendRenewalReminders()` (5 days before expiry)
  - [ ] `@Scheduled checkSubscriptionExpiry()` (daily)

### Phase 5.4 - REST Endpoints (3-4 heures)
- [ ] **SubscriptionController.java** (3-4h)
  - [ ] `GET /api/subscription/plans` - List all plans
  - [ ] `GET /api/subscription/current` - Get user's current subscription
  - [ ] `POST /api/subscription/upgrade` - Initiate upgrade flow
  - [ ] `POST /api/subscription/cancel` - Cancel subscription
  - [ ] `GET /api/subscription/invoices` - Get user's invoices
  - [ ] `POST /api/subscription/webhook` - Stripe webhook receiver
  - [ ] Tests: Integration tests for each endpoint

---

## BACKEND: ADMIN - ANALYTICS & MODERATION 📊

### Phase 5.5 - Admin Entities (2-3 heures) - ✅ DONE
- [x] **AdminAuditLog.java** (30 min) ✓
  - [x] Action, targetType, targetId, details
  - [x] Repository

- [x] **UserReport.java** (30 min) ✓
  - [x] ReportedBy, reportedUser, reason, status
  - [x] Repository with status filters

- [x] **ActivityReport.java** (30 min) ✓
  - [x] Similar structure to UserReport
  - [x] Repository

- [x] **ModerationAction.java** (30 min) ✓
  - [x] ActionType enum (WARN, SUSPEND, BAN, RESTORE)
  - [x] Repository

- [ ] **Update User entity** (30 min)
  - [ ] Add `suspendedUntil` field (in migration V4)
  - [ ] Add `role` = BANNED when applicable (in migration V4)
  - [ ] Migration for column changes ✓ (V4__create_admin_and_moderation_tables.sql)

### Phase 5.6 - Admin Analytics Service (5-6 heures)
- [ ] **AdminAnalyticsService.java** (5-6h)
  - [ ] `getActiveUsersCount()` - Count active users in range
  - [ ] `getNewUsersCount()` - Count new signups
  - [ ] `getActivitiesCreatedCount()` - Count created activities
  - [ ] `getAverageRating()` - Calculate average ratings
  - [ ] `getTotalRevenue()` - Sum revenue for period
  - [ ] `getSubscriptionStats()` - Premium vs free breakdown
  - [ ] `getEngagementMetrics()` - Participation, ratings, chat volume
  - [ ] `getUserGrowthTrend()` - Time series data
  - [ ] `getActivityGrowthTrend()` - Time series data
  - [ ] Database query optimization & caching

### Phase 5.7 - Admin Moderation Service (6-7 heures)
- [ ] **AdminModerationService.java** (6-7h)
  - [ ] `submitUserReport()` - Create user report
  - [ ] `submitActivityReport()` - Create activity report
  - [ ] `getReportsQueue()` - Paginated list of pending reports
  - [ ] `approveReport()` - Apply moderation action
  - [ ] `rejectReport()` - Dismiss report
  - [ ] `suspendUser()` - Temporary suspension with expiry
  - [ ] `banUser()` - Permanent ban
  - [ ] `restoreUser()` - Reverse ban/suspension
  - [ ] `deleteActivity()` - Soft delete activity
  - [ ] `issueWarning()` - Send warning without action
  - [ ] Audit logging for all actions
  - [ ] Email notifications

- [ ] **Batch Job: Auto-reactivation** (1h)
  - [ ] `@Scheduled` task to restore users after suspension expires
  - [ ] Send email notification on restoration

### Phase 5.8 - Admin REST Endpoints (4-5 heures)
- [ ] **AdminController.java** (4-5h)
  - [ ] `GET /api/admin/analytics` - Dashboard KPIs
  - [ ] `GET /api/admin/analytics/export` - CSV export
  - [ ] `GET /api/admin/users` - Paginated user list (search, filters)
  - [ ] `GET /api/admin/users/{id}` - User details
  - [ ] `POST /api/admin/users/{id}/suspend` - Suspend user
  - [ ] `POST /api/admin/users/{id}/ban` - Ban user
  - [ ] `POST /api/admin/users/{id}/restore` - Restore user
  - [ ] `GET /api/admin/activities` - Paginated activities
  - [ ] `DELETE /api/admin/activities/{id}` - Delete activity
  - [ ] `GET /api/admin/reports` - Reports queue
  - [ ] `POST /api/admin/reports/{id}/approve` - Approve report
  - [ ] `POST /api/admin/reports/{id}/reject` - Reject report
  - [ ] `GET /api/admin/audit-logs` - Audit log listing
  - [ ] All endpoints require `@PreAuthorize("hasRole('ADMIN')")`

---

## FRONTEND: USER - PREMIUM FEATURES 👤

### Phase 5.9 - Subscription Service (1h)
- [ ] **SubscriptionService** (1h)
  - [ ] `getPlans()` - Fetch all subscription plans
  - [ ] `getCurrentSubscription()` - Get user's current subscription
  - [ ] `upgradeSubscription(tier)` - Initiate Stripe checkout
  - [ ] `cancelSubscription()` - Cancel active subscription
  - [ ] `getInvoices()` - Fetch user's invoices
  - [ ] `getPremiumStats()` - Get premium user analytics

### Phase 5.10 - Paywall Modal (3-4 heures)
- [ ] **PaywallModalComponent** (3-4h)
  - [ ] Display all 3 subscription plans with features
  - [ ] Plan comparison table
  - [ ] "Upgrade Now" button -> Stripe checkout
  - [ ] Success/cancel handling
  - [ ] Material Card styling
  - [ ] Responsive design (mobile)
  - [ ] Tests: Component tests with mocked service

### Phase 5.11 - Premium Badge Component (1-2 heures)
- [ ] **PremiumBadgeComponent** (1-2h)
  - [ ] Display "⭐ STARTER" or "⭐ PROFESSIONAL"
  - [ ] Show only if tier !== FREE
  - [ ] Styling: Different colors per tier
  - [ ] Use on profile pages and activity listings

### Phase 5.12 - Premium Stats Dashboard (4-5 heures)
- [ ] **PremiumStatsComponent** (4-5h)
  - [ ] Route: `/profile/premium-stats`
  - [ ] Display stats: activities created, participants, earnings, ratings
  - [ ] Charts: Activity trends, revenue trends
  - [ ] CSV export button
  - [ ] Only accessible by premium users
  - [ ] Tests: Verify data loads correctly

### Phase 5.13 - Limits Indicator & CTA (2-3 heures)
- [ ] **LimitsBannerComponent** (2-3h)
  - [ ] Show "X / 5 activities created this week"
  - [ ] Progress bar visualization
  - [ ] "Upgrade to Unlimited" button when limit reached
  - [ ] Opens PaywallModal on click
  - [ ] Fetch user's used activities count from API
  - [ ] Show only for FREE tier users

---

## FRONTEND: ADMIN DASHBOARD 🎛️

### Phase 5.14 - Admin Layout & Navigation (3-4 heures)
- [ ] **AdminLayoutComponent** (3-4h)
  - [ ] Sidenav with menu items
  - [ ] Toolbar with user profile dropdown
  - [ ] Logout button
  - [ ] Responsive design (sidebar collapses on mobile)
  - [ ] Dark mode support (optional)
  - [ ] Tests: Navigation routing

- [ ] **AdminGuard** (1h)
  - [ ] Route guard to check for ADMIN role
  - [ ] Redirect to login if not authenticated
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

