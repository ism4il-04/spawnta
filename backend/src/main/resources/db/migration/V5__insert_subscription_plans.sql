-- Insert subscription plans with Stripe IDs
INSERT INTO subscription_plans (tier, name, description, monthly_price, stripe_product_id, stripe_price_id, created_at, updated_at) VALUES
('FREE', 'Free Plan', 'Basic features for exploring Spawnta', 0.00, 'prod_free_dummy', 'price_free_dummy', NOW(), NOW()),
('STARTER', 'Starter Plan', 'For active users and community leaders', 9.99, 'prod_starter_dummy', 'price_starter_dummy', NOW(), NOW()),
('PROFESSIONAL', 'Professional Plan', 'For businesses and organizations', 24.99, 'prod_professional_dummy', 'price_professional_dummy', NOW(), NOW())
ON CONFLICT (tier) DO NOTHING;

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
((SELECT id FROM subscription_plans WHERE tier='PROFESSIONAL'), 'API access')
ON CONFLICT (plan_id, feature) DO NOTHING;
