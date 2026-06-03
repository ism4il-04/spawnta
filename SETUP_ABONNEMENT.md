# Setup Abonnement Stripe - Guide Rapide

## 1. Cloner et démarrer le projet
```bash
git pull origin main
docker-compose up -d
```

## 2. Attendre 30 secondes, puis remplir les plans d'abonnement
```bash
docker exec -it spawnta-postgres psql -U spawnta -d spawnta
```

```sql
-- Insérer les 3 plans (FREE, STARTER, PROFESSIONAL)
INSERT INTO subscription_plans (tier, name, description, monthly_price, stripe_product_id, stripe_price_id, is_active)
VALUES 
('FREE', 'Free Plan', 'Basic features for exploring Spawnta', 0.00, 'prod_UdWyFRQBgaWzel', 'price_1TeFuCRTAe1LMgiGjne6mu3k', true),
('STARTER', 'Starter Plan', 'For active users and community leaders', 9.99, 'prod_UdX0F4DNhIaK8E', 'price_1TeFw4RTAe1LMgiGtK0lJ7J3', true),
('PROFESSIONAL', 'Professional Plan', 'For businesses and organizations', 24.99, 'prod_UdX1m1eieookQS', 'price_1TeFx3RTAe1LMgiGHeNLGHUb', true);

-- Insérer les features
INSERT INTO plan_features (plan_id, feature_text, display_order) VALUES
(1, 'Basic profile', 1), (1, '5 activities per week', 2), (1, 'Standard search', 3),
(2, 'Priority support', 1), (2, 'Unlimited activities', 2), (2, 'Advanced search', 3), (2, 'Enhanced profile', 4),
(3, '24/7 support', 1), (3, 'API access', 2), (3, 'Analytics dashboard', 3), (3, 'Unlimited everything', 4);

\q
```

## 3. Tester l'abonnement
- Ouvrir: `http://localhost:4200/subscription`
- Cliquer "Choisir ce plan" (STARTER ou PROFESSIONAL)
- Carte test Stripe: `4242 4242 4242 4242`
- Expiration: `12/26` | CVC: `123`

## 4. Après paiement réussi - Activer manuellement (webhook désactivé)
```bash
docker exec -it spawnta-postgres psql -U spawnta -d spawnta
```

```sql
UPDATE users SET subscription_tier = 'STARTER' WHERE email = 'votre_email@exemple.com';
\q
```

✅ Rafraîchir la page - l'abonnement est actif !
