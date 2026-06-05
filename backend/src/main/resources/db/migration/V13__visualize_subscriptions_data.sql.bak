-- 1. Créer un utilisateur de test (Ayman Premium) - Pass: password123
INSERT INTO users (email, password, first_name, last_name, role, email_verified, subscription_tier, stripe_customer_id, created_at)
SELECT 'aymanesaber13@gmail.com', '$2a$10$JCQqi/5u2QYeY0AMhdITR.f7DdHQR6Jo3zdr7.tjBrZ3r4B6p/HDC', 'Ayman', 'Premium', 'ADMIN', true, 'STARTER', 'cus_test_ayman_123', NOW()
WHERE NOT EXISTS (SELECT 1 FROM users WHERE email = 'aymanesaber13@gmail.com');

-- 2. Lui assigner un abonnement Starter actif
INSERT INTO user_subscriptions (user_id, plan_id, stripe_customer_id, stripe_subscription_id, start_date, renewal_date, status, created_at, updated_at)
SELECT 
    (SELECT id FROM users WHERE email = 'aymanesaber13@gmail.com'),
    (SELECT id FROM subscription_plans WHERE tier = 'STARTER'),
    'cus_test_ayman_123',
    'sub_test_ayman_456',
    NOW() - INTERVAL '15 days',
    NOW() + INTERVAL '15 days',
    'ACTIVE',
    NOW(),
    NOW()
WHERE EXISTS (SELECT 1 FROM users WHERE email = 'aymanesaber13@gmail.com')
  AND NOT EXISTS (SELECT 1 FROM user_subscriptions WHERE stripe_customer_id = 'cus_test_ayman_123');

-- 3. Insérer des transactions
INSERT INTO payment_transactions (user_id, stripe_payment_intent_id, amount, currency, status, description, created_at)
VALUES 
    ((SELECT id FROM users WHERE email = 'aymanesaber13@gmail.com'), 'pi_test_1', 9.99, 'EUR', 'SUCCEEDED', 'Starter Plan - Monthly', NOW() - INTERVAL '15 days'),
    ((SELECT id FROM users WHERE email = 'aymanesaber13@gmail.com'), 'pi_test_2', 9.99, 'EUR', 'SUCCEEDED', 'Starter Plan - Monthly Renewal', NOW() - INTERVAL '45 days');

-- 4. Insérer une facture
INSERT INTO invoices (user_id, stripe_invoice_id, amount, currency, status, invoice_number, invoice_date, created_at)
VALUES 
    ((SELECT id FROM users WHERE email = 'aymanesaber13@gmail.com'), 'in_test_1', 9.99, 'EUR', 'PAID', 'INV-2024-001', NOW() - INTERVAL '15 days', NOW());

-- 5. Créer une activité de test pour l'Admin
INSERT INTO activities (title, description, category, activity_type, host_id, scheduled_at, max_participants, created_at)
SELECT 'Randonnée Test Admin', 'Une activité pour tester l''affichage admin.', 'HIKING', 'MEETUP', (SELECT id FROM users WHERE email = 'aymanesaber13@gmail.com'), NOW() + INTERVAL '2 days', 10, NOW()
WHERE NOT EXISTS (SELECT 1 FROM activities WHERE title = 'Randonnée Test Admin');
