-- ============================================
-- Script de mise à jour des IDs Stripe
-- Date: 3 Juin 2026
-- ============================================

-- INSTRUCTIONS:
-- 1. Trouvez les price_id dans Stripe Dashboard:
--    Produits → Cliquez sur chaque produit → Section "Tarification"
-- 2. Remplacez les PRICE_ID_XXX ci-dessous
-- 3. Exécutez ce script dans PostgreSQL

-- ============================================
-- FREE PLAN
-- Product ID: prod_UdWyFRQBgaWzel
-- ============================================
UPDATE subscription_plans 
SET stripe_product_id = 'prod_UdWyFRQBgaWzel',
    stripe_price_id = 'REMPLACEZ_PAR_LE_PRICE_ID_DU_FREE_PLAN'
WHERE tier = 'FREE';

-- ============================================
-- STARTER PLAN  
-- Product ID: prod_UdX0F4DNhIaK8E
-- ============================================
UPDATE subscription_plans 
SET stripe_product_id = 'prod_UdX0F4DNhIaK8E',
    stripe_price_id = 'REMPLACEZ_PAR_LE_PRICE_ID_DU_STARTER_PLAN'
WHERE tier = 'STARTER';

-- ============================================
-- PROFESSIONAL PLAN
-- Product ID: prod_UdX1m1eieookQS
-- ============================================
UPDATE subscription_plans 
SET stripe_product_id = 'prod_UdX1m1eieookQS',
    stripe_price_id = 'REMPLACEZ_PAR_LE_PRICE_ID_DU_PROFESSIONAL_PLAN'
WHERE tier = 'PROFESSIONAL';

-- ============================================
-- VÉRIFICATION
-- ============================================
SELECT 
    tier,
    name,
    monthly_price,
    stripe_product_id,
    stripe_price_id
FROM subscription_plans
ORDER BY monthly_price;

-- Le résultat devrait montrer:
-- FREE         | 0.00  | prod_UdWyFRQBgaWzel | price_xxxxx
-- STARTER      | 9.99  | prod_UdX0F4DNhIaK8E | price_xxxxx
-- PROFESSIONAL | 24.99 | prod_UdX1m1eieookQS | price_xxxxx
