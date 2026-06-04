# 🔄 Redémarrage après reboot PC

## ❌ Problème

Après redémarrage PC, l'erreur `ERR_EMPTY_RESPONSE` signifie que le backend ne répond pas.

**Cause** : Docker s'arrête au redémarrage du PC, tous les containers sont stoppés.

---

## ✅ Solution : Redémarrer les services

### 1. Vérifier l'état des containers (5 sec)

```bash
docker ps
```

**Si vide ou containers manquants** → Redémarrer

---

### 2. Redémarrer tous les services (30 sec)

```bash
cd C:\Users\allou\OneDrive\Bureau\Projts_Academique\JEE\spawnta
docker-compose up -d
```

**Résultat attendu :**
```
[+] Running 6/6
 ✔ Container spawnta-postgres   Started
 ✔ Container spawnta-redis      Started
 ✔ Container spawnta-zookeeper  Started
 ✔ Container spawnta-kafka      Started
 ✔ Container spawnta-backend    Started
 ✔ Container spawnta-frontend   Started
```

---

### 3. Attendre que les services démarrent (30 sec)

```bash
# Vérifier backend
docker logs spawnta-backend --tail 30

# Chercher cette ligne
# Stripe API initialized with key: sk_test_...
# Started Application in X seconds
```

---

### 4. Tester l'API (5 sec)

```bash
curl http://localhost:8080/api/subscription/plans
```

**Résultat attendu** : JSON avec les 3 plans

---

### 5. Tester le frontend (immédiat)

Ouvrir : `http://localhost:4200`

Vous devriez voir la page d'accueil.

---

## 🧪 Si vous voulez tester les webhooks

### 6. Relancer Stripe CLI

```bash
cd C:\Users\allou\Downloads\stripe_1.42.1_windows_x86_64
stripe.exe listen --forward-to localhost:8080/api/subscription/webhook
```

---

## ⚡ Commande rapide tout-en-un

```bash
cd C:\Users\allou\OneDrive\Bureau\Projts_Academique\JEE\spawnta && docker-compose up -d && timeout 30 && docker logs spawnta-backend --tail 20
```

---

## 🐛 Si ça ne fonctionne toujours pas

### Erreur : "Cannot connect to Docker daemon"

**Cause** : Docker Desktop pas démarré

**Solution** :
1. Ouvrir Docker Desktop
2. Attendre qu'il soit prêt (icône verte)
3. Relancer `docker-compose up -d`

---

### Erreur : Port déjà utilisé

**Cause** : Un autre processus utilise le port 8080 ou 4200

**Solution** :
```bash
# Windows - Trouver processus sur port 8080
netstat -ano | findstr :8080

# Tuer le processus (remplacer PID)
taskkill /PID <PID> /F

# Relancer
docker-compose up -d
```

---

### Backend ne démarre pas

**Voir les logs complets** :
```bash
docker logs spawnta-backend
```

**Causes fréquentes** :
- PostgreSQL pas prêt → Attendre 10 sec de plus
- Erreur de compilation → Vérifier les logs
- Port 8080 occupé → Voir ci-dessus

---

## ✅ Checklist de santé

Après `docker-compose up -d`, vérifier :

```bash
# 1. Tous les containers actifs
docker ps
# Doit montrer 6 containers (postgres, redis, zookeeper, kafka, backend, frontend)

# 2. Backend répond
curl http://localhost:8080/actuator/health
# Doit retourner : {"status":"UP"}

# 3. Frontend accessible
curl http://localhost:4200
# Doit retourner du HTML

# 4. Stripe initialisé
docker logs spawnta-backend | grep "Stripe API initialized"
# Doit trouver la ligne
```

---

## 🚀 Commandes quotidiennes

### Démarrer le projet
```bash
cd C:\Users\allou\OneDrive\Bureau\Projts_Academique\JEE\spawnta
docker-compose up -d
```

### Arrêter le projet
```bash
docker-compose down
```

### Redémarrer un service spécifique
```bash
docker-compose restart backend
docker-compose restart frontend
```

### Voir les logs
```bash
docker-compose logs -f backend
docker-compose logs -f frontend
```

---

**Temps total** : ~1 minute pour tout redémarrer ! 🚀
