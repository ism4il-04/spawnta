-- ═══════════════════════════════════════════════════════════════
-- Script de Correction Base de Données - Spawnta
-- ═══════════════════════════════════════════════════════════════

\echo '🔧 Correction des colonnes NOT NULL sans valeur par défaut...'

-- Users : Colonnes de gamification
ALTER TABLE users ALTER COLUMN xp SET DEFAULT 0;
ALTER TABLE users ALTER COLUMN total_xp_earned SET DEFAULT 0;
ALTER TABLE users ALTER COLUMN current_level_xp_required SET DEFAULT 100;
ALTER TABLE users ALTER COLUMN level SET DEFAULT 1;
ALTER TABLE users ALTER COLUMN is_banned SET DEFAULT false;
ALTER TABLE users ALTER COLUMN profile_public SET DEFAULT true;

UPDATE users SET xp = 0 WHERE xp IS NULL;
UPDATE users SET total_xp_earned = 0 WHERE total_xp_earned IS NULL;
UPDATE users SET current_level_xp_required = 100 WHERE current_level_xp_required IS NULL;
UPDATE users SET level = 1 WHERE level IS NULL;
UPDATE users SET is_banned = false WHERE is_banned IS NULL;
UPDATE users SET profile_public = true WHERE profile_public IS NULL;

\echo '✅ Users corrigés'

-- Payment Transactions : updated_at
ALTER TABLE payment_transactions ALTER COLUMN updated_at SET DEFAULT NOW();
UPDATE payment_transactions SET updated_at = created_at WHERE updated_at IS NULL;

\echo '✅ Payment transactions corrigés'

-- Invoices : updated_at
ALTER TABLE invoices ALTER COLUMN updated_at SET DEFAULT NOW();
UPDATE invoices SET updated_at = created_at WHERE updated_at IS NULL;

\echo '✅ Invoices corrigés'

\echo ''
\echo '────────────────────────────────────────────────────────────'
\echo '🔍 Vérification : Colonnes NOT NULL restantes sans défaut'
\echo '────────────────────────────────────────────────────────────'

SELECT 
    table_name,
    column_name,
    data_type,
    is_nullable,
    column_default
FROM information_schema.columns 
WHERE table_schema = 'public' 
  AND is_nullable = 'NO' 
  AND column_default IS NULL
  AND column_name NOT IN ('id', 'email', 'password', 'first_name', 'last_name', 'role', 'created_at', 'subscription_tier')
ORDER BY table_name, column_name;

\echo ''
\echo '────────────────────────────────────────────────────────────'
\echo '✅ Correction terminée!'
\echo '────────────────────────────────────────────────────────────'
