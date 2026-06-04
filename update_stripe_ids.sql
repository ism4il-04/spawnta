-- Script pour mettre à jour les IDs Stripe dans la base de données
-- À exécuter après avoir créé les produits dans Stripe Dashboard

-- INSTRUCTIONS:
-- 1. Créez les 3 produits dans Stripe Dashboard
-- 2. Copiez les IDs de produit (prod_xxxxx) et de prix (price_xxxxx)
-- 3. Remplacez les valeurs ci-dessous
-- 4. Exécutez ce script

-- ============================================
-- FREE PLAN
-- ============================================
UPDATE subscription_plans 
SET stripe_product_id = 'COPIEZ_ICI_LE_prod_xxxxx_DU_FREE_PLAN',
    stripe_price_id = 'COPIEZ_ICI_LE_price_xxxxx_DU_FREE_PLAN'
WHERE tier = 'FREE';

-- ============================================
-- STARTER PLAN
-- ============================================
UPDATE subscription_plans 
SET stripe_product_id = 'COPIEZ_ICI_LE_prod_xxxxx_DU_STARTER_PLAN',
    stripe_price_id = 'COPIEZ_ICI_LE_price_xxxxx_DU_STARTER_PLAN'
WHERE tier = 'STARTER';

-- ============================================
-- PROFESSIONAL PLAN
-- ============================================
UPDATE subscription_plans 
SET stripe_product_id = 'COPIEZ_ICI_LE_prod_xxxxx_DU_PROFESSIONAL_PLAN',
    stripe_price_id = 'COPIEZ_ICI_LE_price_xxxxx_DU_PROFESSIONAL_PLAN'
WHERE tier = 'PROFESSIONAL';

-- ============================================
-- VÉRIFICATION
-- ============================================
-- Exécutez cette requête pour vérifier que tout est bien configuré
SELECT 
    id,
    tier,
    name,
    monthly_price,
    stripe_product_id,
    stripe_price_id
FROM subscription_plans
ORDER BY monthly_price;

-- Le résultat devrait montrer les 3 plans avec leurs IDs Stripe réels
