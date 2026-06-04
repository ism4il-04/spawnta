-- ============================================
-- SCRIPT SQL PRÊT À EXÉCUTER
-- Date: 3 Juin 2026
-- ============================================
-- Ce script contient vos vrais IDs Stripe
-- Copier-coller directement dans PostgreSQL
-- ============================================

-- FREE PLAN
UPDATE subscription_plans 
SET stripe_product_id = 'prod_UdWyFRQBgaWzel',
    stripe_price_id = 'price_1TeFuCRTAe1LMgiGjne6mu3k'
WHERE tier = 'FREE';

-- STARTER PLAN
UPDATE subscription_plans 
SET stripe_product_id = 'prod_UdX0F4DNhIaK8E',
    stripe_price_id = 'price_1TeFw4RTAe1LMgiGtK0lJ7J3'
WHERE tier = 'STARTER';

-- PROFESSIONAL PLAN
UPDATE subscription_plans 
SET stripe_product_id = 'prod_UdX1m1eieookQS',
    stripe_price_id = 'price_1TeFx3RTAe1LMgiGHeNLGHUb'
WHERE tier = 'PROFESSIONAL';

-- VÉRIFICATION
SELECT 
    tier,
    name,
    monthly_price,
    stripe_product_id,
    stripe_price_id
FROM subscription_plans
ORDER BY monthly_price;

-- Résultat attendu:
-- tier         | name              | monthly_price | stripe_product_id     | stripe_price_id
-- -------------+-------------------+---------------+----------------------+----------------------------------
-- FREE         | Free Plan         | 0.00          | prod_UdWyFRQBgaWzel  | price_1TeFuCRTAe1LMgiGjne6mu3k
-- STARTER      | Starter Plan      | 9.99          | prod_UdX0F4DNhIaK8E  | price_1TeFw4RTAe1LMgiGtK0lJ7J3
-- PROFESSIONAL | Professional Plan | 24.99         | prod_UdX1m1eieookQS  | price_1TeFx3RTAe1LMgiGHeNLGHUb
