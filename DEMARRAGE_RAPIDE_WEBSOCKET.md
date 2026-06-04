# 🚀 Démarrage Rapide - WebSocket Temps Réel

## 🎯 Résumé du Problème et Solution

**Problème** : Les messages ne s'affichent pas en temps réel, il faut rafraîchir la page.

**Cause** : Le `OutboxProcessor.java` était manquant - les messages étaient enregistrés en base mais jamais broadcastés via WebSocket.

**Solution** : ✅ `OutboxProcessor.java` créé et corrigé (utilise `OutboxEventStatus.SENT`)

---

## ✅ Fichiers Créés/Modifiés

### **Backend**
1. ✅ `OutboxProcessor.java` - Processeur qui broadcast les messages
2. ✅ `application.properties` - Flyway désactivé (`spring.flyway.enabled=false`)

### **Frontend**  
3. ✅ `notification-toast.service.ts` - Service de notifications toast
4. ✅ `toast-container.component.ts` - Composant d'affichage des toasts
5. ✅ `chat.service.ts` - Ajout affichage toast
6. ✅ `app.ts` + `app.html` - Integration du ToastContainer

### **Base de Données**
7. ✅ `fix-database.sql` - Script de correction des colonnes NOT NULL

---

## 🚀 Étapes de Démarrage

### **1. Base de Données - Déjà Corrigée ✅**
```bash
# Déjà exécuté, pas besoin de refaire
docker exec -i spawnta-postgres psql -U spawnta -d spawnta < fix-database.sql
```

### **2. Rebuild Backend (IMPORTANT)**
```bash
cd backend

# Option A : Maven local
mvn clean install -DskipTests
mvn spring-boot:run

# Option B : Docker (si problèmes réseau Docker Hub, sauter)
docker-compose build backend
docker-compose up -d backend
```

### **3. Vérifier le Démarrage**
```bash
# Attendre 30 secondes puis vérifier
docker logs spawnta-backend | findstr "Started BackendApplication in"

# Devrait afficher :
# INFO --- [main] com.spawnta.BackendApplication : Started BackendApplication in 15.5 seconds
```

### **4. Vérifier OutboxProcessor**
```bash
# Chercher les logs du processor
docker logs spawnta-backend | findstr "OutboxProcessor"

# Devrait afficher (après quelques secondes) :
# DEBUG --- [scheduling-1] c.s.service.OutboxProcessor : Processing 0 pending outbox events
```

---

## 🧪 Test Temps Réel

### **Ouvrir 2 Navigateurs**

**Chrome (Utilisateur A)** :
```
1. http://localhost:4200/login
2. Se connecter
3. Aller sur /chat
4. Sélectionner une conversation
```

**Firefox (Utilisateur B)** :
```
1. http://localhost:4200/login  
2. Se connecter (autre compte)
3. Aller sur /chat
4. Envoyer un message à A : "Test temps réel!"
```

### **✅ Résultat Attendu**
- Le message apparaît **INSTANTANÉMENT** sur Chrome
- **Pas de rafraîchissement nécessaire**
- Badge "Messages" se met à jour automatiquement
- Toast notification si A n'est pas sur `/chat`

---

## 🐛 Troubleshooting

### **Problème 1 : Backend ne démarre pas (crashloop)**

**Symptôme** :
```bash
docker logs spawnta-backend | findstr "Started"
# Affiche plusieurs "Starting BackendApplication..." mais jamais "Started ... in X seconds"
```

**Solutions** :

#### **A. Vérifier les erreurs Flyway**
```bash
docker logs spawnta-backend | findstr "ERROR"
# Si vous voyez des erreurs Flyway → Vérifier que spring.flyway.enabled=false
```

#### **B. Reconstruire proprement**
```bash
# Supprimer l'image actuelle
docker-compose rm -f backend
docker rmi spawnta-backend

# Rebuild from scratch
docker-compose build --no-cache backend
docker-compose up -d backend
```

#### **C. Démarrage local (si Docker pose problème)**
```bash
cd backend
mvn clean install -DskipTests
mvn spring-boot:run

# Attendre le message :
# Started BackendApplication in 12.345 seconds (process running for 15.678)
```

---

### **Problème 2 : OutboxProcessor ne démarre pas**

**Symptôme** :
```bash
docker logs spawnta-backend | findstr "OutboxProcessor"
# Aucune sortie
```

**Vérifications** :

1. **Fichier existe ?**
```bash
dir backend\src\main\java\com\spawnta\service\OutboxProcessor.java
# Doit exister
```

2. **@EnableScheduling activé ?**
```java
// backend/src/main/java/com/spawnta/BackendApplication.java
@SpringBootApplication
@EnableScheduling  // ← Doit être présent
public class BackendApplication { ... }
```

3. **Recompiler**
```bash
cd backend
mvn clean install
mvn spring-boot:run
```

---

### **Problème 3 : Messages n'arrivent toujours pas en temps réel**

**Vérifications** :

#### **A. WebSocket connecté ?**
1. Chrome DevTools (F12)
2. Network → Filter: `WS`
3. Chercher `ws://localhost:8080/ws`
4. Status doit être `101 Switching Protocols`

#### **B. Outbox events traités ?**
```bash
docker exec -it spawnta-postgres psql -U spawnta -d spawnta
```

```sql
-- Voir les événements en attente
SELECT COUNT(*) FROM outbox_events WHERE status = 'PENDING';

-- Devrait être 0 ou très peu

-- Voir les événements traités
SELECT * FROM outbox_events WHERE status = 'SENT' ORDER BY id DESC LIMIT 5;

-- Devrait montrer des événements récents
```

#### **C. Logs détaillés**
```bash
# Activer les logs DEBUG
docker exec -it spawnta-backend sh -c 'echo "logging.level.com.spawnta.service.OutboxProcessor=DEBUG" >> /app/config/application.properties'

# Redémarrer
docker restart spawnta-backend
```

---

## 📊 Vérification Complète

### **Script de Diagnostic**
```bash
# Exécuter ce script pour tout vérifier
cd C:\Users\allou\OneDrive\Bureau\Projts_Academique\JEE\spawnta

echo "=== 1. Backend Status ==="
docker ps | findstr spawnta-backend

echo ""
echo "=== 2. Backend Started? ==="
docker logs spawnta-backend 2>&1 | findstr "Started BackendApplication in" | Select-Object -Last 1

echo ""
echo "=== 3. OutboxProcessor Active? ==="
docker logs spawnta-backend 2>&1 | findstr "OutboxProcessor" | Select-Object -Last 3

echo ""
echo "=== 4. Outbox Events PENDING ==="
docker exec -it spawnta-postgres psql -U spawnta -d spawnta -c "SELECT COUNT(*) as pending FROM outbox_events WHERE status = 'PENDING';"

echo ""
echo "=== 5. Outbox Events SENT (Last 3) ==="
docker exec -it spawnta-postgres psql -U spawnta -d spawnta -c "SELECT id, topic, status, created_at FROM outbox_events WHERE status = 'SENT' ORDER BY id DESC LIMIT 3;"
```

---

## 🎉 Si Tout Fonctionne

### **Indicateurs de Succès** :

✅ Backend démarre en < 30s  
✅ Logs contiennent "Started BackendApplication in X seconds"  
✅ Logs contiennent "Processing X pending outbox events" (toutes les 500ms)  
✅ `outbox_events` PENDING = 0  
✅ WebSocket connecté dans DevTools  
✅ Messages arrivent sans refresh  
✅ Toast notifications apparaissent  

---

## 📚 Documentation

- **[SOLUTION_WEBSOCKET.md](./SOLUTION_WEBSOCKET.md)** - Vue d'ensemble complète
- **[FIX_WEBSOCKET_RAPIDE.md](./FIX_WEBSOCKET_RAPIDE.md)** - Guide 5 minutes
- **[DEBUG_WEBSOCKET.md](./DEBUG_WEBSOCKET.md)** - Troubleshooting détaillé
- **[CHAT_TEMPS_REEL.md](./CHAT_TEMPS_REEL.md)** - Architecture technique

---

## 🆘 En Cas de Blocage

Si après toutes ces étapes ça ne fonctionne toujours pas :

### **Option 1 : Démarrage Local (Recommandé)**
```bash
cd backend
mvn spring-boot:run

# Dans un autre terminal
cd frontend
npm start

# Tester à http://localhost:4200
```

### **Option 2 : Rebuild Complet Docker**
```bash
docker-compose down
docker system prune -a --volumes
docker-compose up -d
```

### **Option 3 : Mode Debug**
```bash
# Backend avec logs DEBUG
cd backend
mvn spring-boot:run -Dlogging.level.com.spawnta=DEBUG

# Regarder les logs pour voir où ça bloque
```

---

✨ **Le système est prêt, il suffit de redémarrer proprement le backend!** ✨
