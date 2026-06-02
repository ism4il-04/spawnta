-- Insert subscription plans with Stripe IDs
INSERT INTO subscription_plans (tier, name, description, monthly_price, stripe_product_id, stripe_price_id, created_at, updated_at)
SELECT 'FREE', 'Free Plan', 'Basic features for exploring Spawnta', 0.00, 'prod_free_dummy', 'price_free_dummy', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM subscription_plans WHERE tier = 'FREE');

INSERT INTO subscription_plans (tier, name, description, monthly_price, stripe_product_id, stripe_price_id, created_at, updated_at)
SELECT 'STARTER', 'Starter Plan', 'For active users and community leaders', 9.99, 'prod_starter_dummy', 'price_starter_dummy', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM subscription_plans WHERE tier = 'STARTER');

INSERT INTO subscription_plans (tier, name, description, monthly_price, stripe_product_id, stripe_price_id, created_at, updated_at)
SELECT 'PROFESSIONAL', 'Professional Plan', 'For businesses and organizations', 24.99, 'prod_professional_dummy', 'price_professional_dummy', NOW(), NOW()
WHERE NOT EXISTS (SELECT 1 FROM subscription_plans WHERE tier = 'PROFESSIONAL');

-- Insert plan features
INSERT INTO plan_features (plan_id, feature)
SELECT plan_id, feature
FROM (
    VALUES
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
        ((SELECT id FROM subscription_plans WHERE tier='PROFESSIONAL'), 'API access')
) AS desired(plan_id, feature)
WHERE NOT EXISTS (
    SELECT 1
    FROM plan_features existing
    WHERE existing.plan_id = desired.plan_id
      AND existing.feature = desired.feature
);
