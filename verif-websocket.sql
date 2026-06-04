-- ═══════════════════════════════════════════════════════════════
-- Script de Vérification WebSocket / Outbox
-- À exécuter dans PostgreSQL
-- ═══════════════════════════════════════════════════════════════

\echo '────────────────────────────────────────────────────────────'
\echo '🔍 VÉRIFICATION 1 : Événements Outbox en attente'
\echo '────────────────────────────────────────────────────────────'

SELECT 
    COUNT(*) as total_pending,
    MIN(created_at) as oldest_pending,
    MAX(created_at) as newest_pending
FROM outbox_events 
WHERE status = 'PENDING';

\echo ''
\echo '❌ Si total_pending > 0 et oldest_pending > 5 secondes :'
\echo '   → Le OutboxProcessor ne fonctionne PAS'
\echo '   → Redémarrez le backend'
\echo ''
\echo '✅ Si total_pending = 0 ou oldest_pending < 5 secondes :'
\echo '   → Le OutboxProcessor fonctionne correctement'
\echo ''

\echo '────────────────────────────────────────────────────────────'
\echo '🔍 VÉRIFICATION 2 : Derniers événements traités'
\echo '────────────────────────────────────────────────────────────'

SELECT 
    id,
    topic,
    status,
    retry_count,
    created_at,
    AGE(NOW(), created_at) as age
FROM outbox_events 
WHERE status = 'SENT'
ORDER BY id DESC 
LIMIT 10;

\echo ''
\echo '✅ Si vous voyez des événements SENT récents :'
\echo '   → Les messages sont bien traités'
\echo ''

\echo '────────────────────────────────────────────────────────────'
\echo '🔍 VÉRIFICATION 3 : Événements en échec'
\echo '────────────────────────────────────────────────────────────'

SELECT 
    id,
    topic,
    status,
    retry_count,
    created_at,
    LEFT(payload, 100) as payload_preview
FROM outbox_events 
WHERE status = 'FAILED'
ORDER BY id DESC 
LIMIT 5;

\echo ''
\echo '❌ Si vous voyez des événements FAILED :'
\echo '   → Il y a un problème avec le WebSocket broadcast'
\echo '   → Vérifiez les logs backend pour les erreurs'
\echo ''

\echo '────────────────────────────────────────────────────────────'
\echo '🔍 VÉRIFICATION 4 : Statistiques générales'
\echo '────────────────────────────────────────────────────────────'

SELECT 
    status,
    COUNT(*) as count,
    AVG(retry_count) as avg_retries,
    MAX(created_at) as last_event
FROM outbox_events 
GROUP BY status
ORDER BY status;

\echo ''
\echo '✅ Bon état :'
\echo '   - SENT : Majoritaire'
\echo '   - PENDING : 0 ou très peu'
\echo '   - FAILED : 0 idéalement'
\echo ''

\echo '────────────────────────────────────────────────────────────'
\echo '🔍 VÉRIFICATION 5 : Messages récents dans le chat'
\echo '────────────────────────────────────────────────────────────'

SELECT 
    m.id as message_id,
    m.chat_id,
    u.email as sender,
    LEFT(m.content, 50) as content_preview,
    m.created_at,
    AGE(NOW(), m.created_at) as age
FROM messages m
LEFT JOIN users u ON m.sender_id = u.id
WHERE m.status = 'ACTIVE'
ORDER BY m.created_at DESC
LIMIT 10;

\echo ''
\echo '✅ Pour chaque message récent, vérifiez :'
\echo '   1. Il existe un outbox_event correspondant'
\echo '   2. L''event est COMPLETED'
\echo ''

\echo '────────────────────────────────────────────────────────────'
\echo '🔍 VÉRIFICATION 6 : Corrélation Message ↔ Outbox'
\echo '────────────────────────────────────────────────────────────'

SELECT 
    m.id as message_id,
    m.chat_id,
    m.created_at as message_time,
    oe.id as outbox_id,
    oe.status as outbox_status,
    oe.created_at as outbox_time,
    AGE(oe.created_at, m.created_at) as processing_delay
FROM messages m
LEFT JOIN outbox_events oe ON 
    oe.topic = 'chat.messages' AND
    oe.payload::jsonb->'message'->>'id' = m.id::text
WHERE m.created_at > NOW() - INTERVAL '10 minutes'
ORDER BY m.created_at DESC
LIMIT 10;

\echo ''
\echo '✅ Vérifiez :'
\echo '   - Chaque message a un outbox_event correspondant'
\echo '   - processing_delay < 1 seconde (idéalement)'
\echo '   - outbox_status = COMPLETED'
\echo ''
\echo '❌ Si outbox_id est NULL :'
\echo '   → Le message n''a pas créé d''événement outbox'
\echo '   → Problème dans ChatService.createMessageOutboxEvent()'
\echo ''

\echo '════════════════════════════════════════════════════════════'
\echo '📝 RÉSUMÉ'
\echo '════════════════════════════════════════════════════════════'
\echo ''
\echo 'Pour un fonctionnement normal :'
\echo '  ✅ outbox_events PENDING = 0 (ou < 5s d''ancienneté)'
\echo '  ✅ outbox_events SENT récents visibles'
\echo '  ✅ outbox_events FAILED = 0'
\echo '  ✅ Corrélation messages ↔ outbox : 100%'
\echo '  ✅ processing_delay < 1s'
\echo ''
\echo 'Si problème détecté :'
\echo '  1. Vérifier que OutboxProcessor.java existe'
\echo '  2. Redémarrer le backend : mvn spring-boot:run'
\echo '  3. Vérifier les logs pour "OutboxProcessor"'
\echo '  4. Relancer ce script pour confirmer'
\echo ''
\echo '════════════════════════════════════════════════════════════'
