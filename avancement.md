# 📊 État d'Avancement — SPAWNTA

> **Dernière mise à jour :** 23 Mai 2026
> **Progression globale :** 30% [▓▓▓▓▓░░░░░░░░░░]

---

## 📈 Détail des Travaux par Phase

### ✅ PHASE 0 : Fondations (100%)
- **Backend** : Spring Boot 3.4, Flyway migrations, PostgreSQL PostGIS.
- **Frontend** : Angular 21, Angular Material themes, Routing.
- **DevOps** : Docker Compose (Full stack), CI/CD GitHub Actions.

### ✅ PHASE 1 : Identité & Sécurité (100%)
- **Backend (API Security) :**
  - [x] Service JWT (`JwtService`) pour tokens stateless.
  - [x] Filtre JWT (`JwtAuthenticationFilter`) — toutes les routes protégées.
  - [x] Configuration de sécurité Spring avec `@EnableMethodSecurity`.
  - [x] Endpoints `/api/auth/signup`, `/api/auth/login`.
  - [x] Mécanisme **Refresh Token** (opaque token → Redis, rotation à chaque `/refresh`).
  - [x] Endpoint `/api/auth/logout` (révocation du refresh token).
- **Profil Riche (Backend) :**
  - [x] Entité `User` étendue : bio, avatarUrl, gallery, visitedCountries.
  - [x] Réseaux sociaux : facebook, instagram, whatsapp.
  - [x] Centres d'intérêt : enum `Interest` (25 catégories), max 10 par utilisateur.
  - [x] Confidentialité : champ `profilePublic` (Public vs Privé).
  - [x] Migration Flyway V2 : colonnes profil + tables `user_interests`, `user_gallery`, `user_visited_countries`.
  - [x] `CloudinaryService` : upload avatar (crop 400×400) et photos de galerie.
  - [x] `ProfileController` : GET/PUT `/api/users/me`, PUT `/interests`, POST/DELETE `/avatar`, `/gallery`.
- **Frontend :**
  - [x] `JwtInterceptor` (functional) — injecte le Bearer token sur toutes les requêtes.
  - [x] `AuthGuard` — protège les routes privées.
  - [x] `ProfileService` — appels API profil.
  - [x] Page Profil complète : bio, réseaux sociaux, sélecteur d'intérêts (chips, max 10), galerie photos, upload avatar.
  - [x] Routing mis à jour : `/profile` (protégé), redirection post-login.

### 🚧 PHASE 2 : Cartographie interactive & Moteur d'activités (En cours)
### ⏳ PHASES FUTURES
- **Phase 3 :** Messagerie temps réel (Kafka/WebSockets) (0%)
- **Phase 4 :** IA (Recommandations) & Gamification (XP) (0%)
- **Phase 5 :** Dashboard Admin & Modèles Premium (0%)

---

## 🛠️ Sprint Actuel (S3)
1. Début Phase 2 : entités `Activity` + `Location` (PostGIS Point/LineString).
2. Interface cartographique Leaflet avec clusters d'activités.
3. Logique métier pour la capacité et la participation aux activités.

---

## 🚩 Notes Techniques
- Kafka est opérationnel dans le cluster Docker, prêt pour la Phase 3.
- PostGIS est activé en base de données pour les calculs de distance (Phase 2).
- Les variables d'environnement Cloudinary doivent être renseignées avant le premier upload (`CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET`).

---
*Lien rapide : [planning.md](planning.md)*
