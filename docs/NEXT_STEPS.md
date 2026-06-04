# 📋 NEXT STEPS - Phase 5 Implementation Guide

**Current Status**: Phase 5B Starting
**Focus**: BillingService + Admin Backend + Testing

---

## 🔴 ÉTAPE 1 : Validation & Test (À FAIRE MAINTENANT)

### 1.1 - Vérifier la compilation
```bash
cd c:\Users\PC\IdeaProjects\spawnta\backend

# Clean et rebuild
mvn clean compile

# Vérifier qu'il n'y a pas d'erreurs de compilation
# Les erreurs possibles:
# - Imports manquants
# - Lombok pas reconnu
# - References circulaires
```

### 1.2 - Redémarrer les conteneurs
```bash
cd c:\Users\PC\IdeaProjects\spawnta

# Arrêter complètement
docker-compose down -v

# Redémarrer
docker-compose up -d

# Vérifier les logs
docker logs -f spawnta-backend

# Checklist:
# ✓ Migrations V3 et V4 exécutées
# ✓ Aucune erreur SQL
# ✓ Backend démarre sur port 8080
# ✓ Swagger accessible http://localhost:8080/swagger-ui.html
```

### 1.3 - Vérifier les tables dans PostgreSQL
```bash
# Entrer dans le conteneur PostgreSQL
docker exec -it spawnta-postgres psql -U spawnta -d spawnta

# Vérifier les nouvelles tables
\dt subscription_plans
\dt user_subscriptions
\dt admin_audit_logs
\dt user_reports
\dt activity_reports
\dt moderation_actions

# Vérifier les colonnes dans users
\d users
# Doit montrer: stripe_customer_id, subscription_tier, suspended_until, is_banned

# Quitter
\q
```

### 1.4 - Tester les endpoints (via Swagger ou Postman)
```
GET /api/subscription/plans
Response: []  (vide car pas de plans créés)

GET /api/subscription/current
Response: 401 (non authentifié)
```

---

## 🟡 ÉTAPE 2 : Setup Stripe Webhook (À FAIRE ENSUITE)

### 2.1 - Ajouter Stripe Webhook Secret au .env
```properties
# Dans le fichier .env
STRIPE_WEBHOOK_SECRET=whsec_test_51TdfYkDbEM70iy3...  # À générer depuis Stripe Dashboard
```

### 2.2 - Ajouter à application.properties
```properties
# Dans backend/src/main/resources/application.properties
stripe.webhook.secret=${STRIPE_WEBHOOK_SECRET:whsec_test_default}
```

### 2.3 - Créer StripeWebhookConfig (config pour webhook)
**Fichier**: `backend/src/main/java/com/spawnta/config/StripeWebhookConfig.java`

```java
@Configuration
public class StripeWebhookConfig {
    
    @Value("${stripe.webhook.secret}")
    private String webhookSecret;
    
    @Bean
    public String stripeWebhookSecret() {
        return webhookSecret;
    }
}
```

### 2.4 - Créer les produits Stripe et Fixtures

**Via Stripe Dashboard**:
1. Aller à https://dashboard.stripe.com/products
2. Créer 3 produits:

**Produit 1: FREE Plan**
- Name: "Free Plan"
- Type: Service
- Price: $0/month
- Billing: Recurring
- → Copier le stripe_product_id et stripe_price_id

**Produit 2: Starter Plan**
- Name: "Starter Plan"
- Type: Service
- Price: $9.99/month
- Billing: Recurring

**Produit 3: Professional Plan**
- Name: "Professional Plan"
- Type: Service
- Price: $24.99/month
- Billing: Recurring

### 2.5 - Créer la migration Flyway pour pré-charger les plans

**Fichier**: `backend/src/main/resources/db/migration/V5__insert_subscription_plans.sql`

```sql
-- Insert subscription plans with Stripe IDs
INSERT INTO subscription_plans (tier, name, description, monthly_price, stripe_product_id, stripe_price_id, created_at, updated_at) VALUES
('FREE', 'Free Plan', 'Basic features for exploring Spawnta', 0.00, 'prod_xxxxx', 'price_xxxxx', NOW(), NOW()),
('STARTER', 'Starter Plan', 'For active users and community leaders', 9.99, 'prod_yyyyy', 'price_yyyyy', NOW(), NOW()),
('PROFESSIONAL', 'Professional Plan', 'For businesses and organizations', 24.99, 'prod_zzzzz', 'price_zzzzz', NOW(), NOW());

-- Insert plan features
INSERT INTO plan_features (plan_id, feature) VALUES
((SELECT id FROM subscription_plans WHERE tier='FREE'), 'Basic profile'),
((SELECT id FROM subscription_plans WHERE tier='FREE'), '5 activities per week'),
((SELECT id FROM subscription_plans WHERE tier='FREE'), 'Standard search'),

((SELECT id FROM subscription_plans WHERE tier='STARTER'), 'Unlimited activities'),
((SELECT id FROM subscription_plans WHERE tier='STARTER'), 'Enhanced profile'),
((SELECT id FROM subscription_plans WHERE tier='STARTER'), 'Advanced search'),
((SELECT id FROM subscription_plans WHERE tier='STARTER'), 'Priority support'),

((SELECT id FROM subscription_plans WHERE tier='PROFESSIONAL'), 'Unlimited everything'),
((SELECT id FROM subscription_plans WHERE tier='PROFESSIONAL'), 'Analytics dashboard'),
((SELECT id FROM subscription_plans WHERE tier='PROFESSIONAL'), '24/7 support'),
((SELECT id FROM subscription_plans WHERE tier='PROFESSIONAL'), 'API access');
```

### 2.6 - Tester le webhook local

```bash
# Installer Stripe CLI si pas fait
# https://stripe.com/docs/stripe-cli

# Tester le webhook
stripe listen --forward-to localhost:8080/api/subscription/webhook

# Output: > Ready! Your webhook signing secret is: whsec_test_xxxxx
# Copier ce secret dans .env

# Dans un autre terminal, déclencher un événement test
stripe trigger payment_intent.succeeded
```

---

## 🟠 ÉTAPE 3 : Implémenter BillingService (Prochaine semaine)

### 3.1 - Créer BillingService.java
**Fichier**: `backend/src/main/java/com/spawnta/subscription/service/BillingService.java`

```java
@Service
@Transactional
public class BillingService {
    
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final StripeService stripeService;
    
    // Upgrade user subscription
    public void upgradeSubscription(User user, SubscriptionTier newTier) throws StripeException {
        UserSubscription current = userSubscriptionRepository.findByUserId(user.getId())
            .orElse(null);
        
        SubscriptionPlan newPlan = subscriptionPlanRepository.findByTier(newTier)
            .orElseThrow(() -> new IllegalArgumentException("Invalid tier"));
        
        if (current != null) {
            // Update existing subscription
            current.setPlan(newPlan);
            current.setUpdatedAt(LocalDateTime.now());
            userSubscriptionRepository.save(current);
        } else {
            // Create new subscription
            String stripeCustomerId = stripeService.createOrUpdateCustomer(user);
            UserSubscription newSubscription = UserSubscription.builder()
                .user(user)
                .plan(newPlan)
                .stripeCustomerId(stripeCustomerId)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(LocalDateTime.now())
                .renewalDate(LocalDateTime.now().plusMonths(1))
                .build();
            userSubscriptionRepository.save(newSubscription);
        }
    }
    
    // Scheduled task: auto-renew subscriptions
    @Scheduled(cron = "0 0 * * * *")  // Every day at midnight
    public void autoRenewSubscriptions() {
        LocalDateTime now = LocalDateTime.now();
        List<UserSubscription> expiringSubscriptions = 
            userSubscriptionRepository.findByStatusAndRenewalDateBefore(
                SubscriptionStatus.ACTIVE, now
            );
        
        for (UserSubscription sub : expiringSubscriptions) {
            try {
                sub.setRenewalDate(now.plusMonths(1));
                userSubscriptionRepository.save(sub);
                logger.info("Renewed subscription for user: {}", sub.getUser().getId());
            } catch (Exception e) {
                logger.error("Error renewing subscription", e);
            }
        }
    }
}
```

### 3.2 - Tests pour BillingService
**Fichier**: `backend/src/test/java/com/spawnta/subscription/service/BillingServiceTest.java`

```java
@SpringBootTest
@Transactional
class BillingServiceTest {
    
    @MockBean
    private StripeService stripeService;
    
    @Autowired
    private BillingService billingService;
    
    @Test
    void testUpgradeSubscription() {
        // Given
        User user = new User();
        user.setId(1L);
        
        // When
        billingService.upgradeSubscription(user, SubscriptionTier.STARTER);
        
        // Then
        UserSubscription subscription = userSubscriptionRepository.findByUserId(1L).get();
        assertThat(subscription.getPlan().getTier()).isEqualTo(SubscriptionTier.STARTER);
    }
}
```

---

## 📝 Commandes Rapides

```bash
# Redémarrer backend seulement
docker-compose restart backend

# Voir les logs du backend
docker logs -f spawnta-backend

# Entrer dans le backend container
docker exec -it spawnta-backend bash

# Build sans tests
mvn clean install -DskipTests

# Run tests uniquement
mvn test
```

---

## ✅ Checklist Avant de Continuer

- [ ] Migrations V3 et V4 exécutées sans erreur
- [ ] Nouvelles tables créées dans PostgreSQL
- [ ] Backend démarre sans erreur
- [ ] Swagger endpoints visibles
- [ ] Stripe account créé et API keys générées
- [ ] Webhook secret obtenu
- [ ] 3 produits Stripe créés avec leurs IDs
- [ ] Migration V5 créée pour pré-charger les plans

---

## 🎯 Objectif de la Semaine

✅ **Par fin de cette semaine**, avoir:
1. Toutes les migrations appliquées
2. Stripe webhook configuré et testé
3. BillingService implémenté avec tests
4. Plans de subscription chargés dans la DB
5. Backend prêt pour intégration frontend

