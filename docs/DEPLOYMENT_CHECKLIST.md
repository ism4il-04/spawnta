# ✅ DEPLOYMENT CHECKLIST - Phase 5 Backend

**Status**: Ready for validation  
**Date**: 02 Juin 2026

---

## 🚀 STEP 1: Verify Compilation (30 min)

- [ ] Open terminal in `c:\Users\PC\IdeaProjects\spawnta\backend`
- [ ] Run: `mvn clean compile`
- [ ] ✅ Result: `BUILD SUCCESS` (no red errors)

**If errors**:
- [ ] Check pom.xml for Stripe dependency
- [ ] Verify all new files are created
- [ ] Run `mvn clean` to reset

---

## 🐳 STEP 2: Deploy Migrations (15 min)

- [ ] Stop containers: `docker-compose down -v`
- [ ] Start fresh: `docker-compose up -d`
- [ ] Wait 30 seconds for initialization
- [ ] Check backend logs: `docker logs -f spawnta-backend --tail=50`

**Expected in logs**:
```
Migrating schema "public" to version 3 - create_subscription_tables
Migrating schema "public" to version 4 - create_admin_and_moderation_tables
Successfully applied 2 migrations
```

✅ If you see both migrations → SUCCESS
❌ If migrations fail → Check PostgreSQL logs

---

## 🗄️ STEP 3: Verify Database (10 min)

```bash
# Connect to PostgreSQL
docker exec -it spawnta-postgres psql -U spawnta -d spawnta

# Check migration history
SELECT version, description, success FROM flyway_schema_history;
# Should show V3 and V4 as success

# Check new tables exist
\dt
# Should show:
# - subscription_plans
# - plan_features
# - user_subscriptions
# - payment_transactions
# - invoices
# - admin_audit_logs
# - user_reports
# - activity_reports
# - moderation_actions

# Check users table has new columns
\d users
# Should show:
# - stripe_customer_id
# - subscription_tier
# - suspended_until
# - is_banned

# Exit
\q
```

✅ All tables and columns present → SUCCESS

---

## 🌐 STEP 4: Verify API Endpoints (15 min)

### 4.1 - Via Swagger UI
1. Open browser: `http://localhost:8080/swagger-ui.html`
2. Look for **"Subscription"** section
3. Should see 6 endpoints in the list:
   - GET /api/subscription/plans
   - GET /api/subscription/current
   - POST /api/subscription/upgrade
   - POST /api/subscription/cancel
   - GET /api/subscription/invoices
   - POST /api/subscription/webhook

✅ All 6 endpoints visible → SUCCESS

### 4.2 - Via cURL (Test endpoints)
```bash
# Test 1: Get subscription plans (no auth needed)
curl -X GET http://localhost:8080/api/subscription/plans

# Expected response: [] (empty list, plans not created yet)
# Status: 200 OK

# Test 2: Get current subscription (needs JWT)
# Get a JWT token first (login endpoint)
curl -X GET http://localhost:8080/api/subscription/current \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Expected: 401 or user's subscription
# Status: 200 or 401 (both OK - means endpoint exists)
```

✅ Endpoints respond (200/401) → SUCCESS

---

## ✅ STEP 5: Backend Startup Verification (10 min)

- [ ] Check backend is running: `docker ps`
  - Should see `spawnta-backend` with status `Up X minutes`

- [ ] Check for errors: `docker logs spawnta-backend | grep ERROR`
  - Should return nothing (or known errors only)

- [ ] Verify actuator health: `curl http://localhost:8080/actuator/health`
  - Expected: `{"status":"UP"}`

✅ Backend running healthily → SUCCESS

---

## 🔧 STEP 6: Verify Code Changes (5 min)

```bash
# Navigate to project
cd c:\Users\PC\IdeaProjects\spawnta

# Count new files
dir /s backend\src\main\java\com\spawnta\subscription
dir /s backend\src\main\java\com\spawnta\admin
dir /s backend\src\main\java\com\spawnta\moderation

# Should show:
# - subscription: ~20-25 files
# - admin: ~2 files
# - moderation: ~5-6 files
```

✅ All files present → SUCCESS

---

## 📊 FINAL VERIFICATION SUMMARY

| Check | Expected | Status | Notes |
|-------|----------|--------|-------|
| Compilation | BUILD SUCCESS | ⏳ TODO | |
| Migration V3 | Applied | ⏳ TODO | |
| Migration V4 | Applied | ⏳ TODO | |
| New Tables | 9 tables | ⏳ TODO | |
| New Columns | 4 added | ⏳ TODO | |
| Swagger Endpoints | 6 endpoints | ⏳ TODO | |
| Backend Health | UP | ⏳ TODO | |
| Logs Errors | None | ⏳ TODO | |

---

## 🎯 If Everything Passes

✅ **Phase 5 Backend Infrastructure is READY**

You can now:
1. Implement BillingService
2. Create admin services
3. Build frontend components
4. Setup Stripe webhook

**Next**: Go to `NEXT_STEPS.md` → ÉTAPE 2

---

## ❌ Troubleshooting

### Issue: Migrations Failed
```
Solution:
1. Stop containers: docker-compose down -v
2. Check PostgreSQL logs: docker logs spawnta-postgres
3. Look for SQL errors in the migration files
4. Fix SQL if needed
5. Restart: docker-compose up -d
```

### Issue: Port 8080 Already In Use
```
Solution:
1. Kill existing process: lsof -i :8080 | kill -9
2. Or change port in docker-compose.yml
3. Restart containers
```

### Issue: Endpoints Not Visible in Swagger
```
Solution:
1. Force refresh Swagger: Ctrl+Shift+Delete (clear cache)
2. Hard refresh browser: Ctrl+F5
3. Check that backend restarted properly
4. Verify SubscriptionController is in correct package
```

### Issue: Migration Rollback Needed
```
Danger Zone - Only if something is broken:
docker exec spawnta-postgres psql -U spawnta -d spawnta

DELETE FROM flyway_schema_history WHERE version IN (3, 4);
DROP TABLE IF EXISTS subscription_plans, plan_features, user_subscriptions,
  payment_transactions, invoices, admin_audit_logs, user_reports,
  activity_reports, moderation_actions;

\q

# Then restart
docker-compose restart backend
```

---

## 📞 Support

**Questions about**:
- Migrations → Check migration files in `db/migration/`
- Entities → Check entity files in `subscription/entity/`
- Endpoints → Check SubscriptionController
- Database → Run SQL queries in PostgreSQL

**Need help**?
- Consult [QUICK_REFERENCE.md](./QUICK_REFERENCE.md)
- Consult [NEXT_STEPS.md](./NEXT_STEPS.md)
- Consult [PHASE_5_RECAP.md](./PHASE_5_RECAP.md)

---

**When Done**: Mark all boxes ✅ and move to NEXT_STEPS.md
