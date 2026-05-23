# 📊 État d'Avancement — SPAWNTA

> **Dernière mise à jour :** 23 Mai 2026
> **Progression globale :** 15% [▓▓░░░░░░░░░░░░░]

---

## 📈 Détail des Travaux par Phase

### ✅ PHASE 0 : Fondations (100%)
- **Backend** : Spring Boot 3.4, Flyway migrations, PostgreSQL PostGIS.
- **Frontend** : Angular 21, Angular Material themes, Routing.
- **DevOps** : Docker Compose (Full stack), CI/CD GitHub Actions.

### 🚧 PHASE 1 : Identité, Inscription & Sécurité (40%)
- **Backend (Authentification & Sécurité) :**
  - [x] Service JWT (`JwtService`) pour tokens stateless.
  - [x] Configuration de sécurité Spring (OAuth2 / CORS).
  - [ ] En cours : Implémentation du filtre `JwtAuthenticationFilter` (TODO actuel dans `SecurityConfig`).
  - [ ] En cours : Renommage `/signup` en `/register` et ajout de la validation stricte de mot de passe (min 8 chars, 1 chiffre) et confirmPassword.
  - [ ] À faire : Validation par email (`/verify-email`) et envoi du mail de confirmation.
  - [ ] À faire : Mécanisme Refresh Token (Redis cookie HttpOnly) & Blacklist JWT.
  - [ ] À faire : Intégration OAuth2 Google/Facebook.
- **Profil & Inscription Multi-étapes :**
  - [ ] Migration DB (Flyway V2) : Ajout des colonnes profils et tables d'intérêts/photos/liens.
  - [ ] Inscription Étape 2 (Vérification disponibilité du username en direct, DOB >= 13, Ville).
  - [ ] Inscription Étape 3 (Grid de sélection de 3 à 10 intérêts).
  - [ ] Intégration API Cloudinary (upload avatars avec recadrage 300x300 px) et Galerie (limite 12 photos pour Gratuits).

### ⏳ PHASES FUTURES
- **Phase 2 :** Cartographie interactive (Leaflet), Moteur d'activités (Local Meetup Point vs Excursion LineString), requêtes PostGIS (`ST_DWithin`) & limitations (max 2 activités/semaine pour Gratuits) (0%)
- **Phase 3 :** Messagerie WebSocket (STOMP), sécurité des connexions, Shared Media Space & Pipeline Kafka (0%)
- **Phase 4 :** Validation présence, notation anonyme croisée par pairs, Gamification (calcul XP/Level Up) & IA (recommandations d'activités et d'amis) (0%)
- **Phase 5 :** Back-Office Admin (Spring Boot Admin + CoreUI Angular) & Paiements Stripe/PayPal (0%)

---

## 🛠️ Sprint en cours (S2)
1. **Sécurisation & Enregistrement** : Finalisation du `JwtAuthenticationFilter`, renommage vers `/register`, validation stricte des mots de passe et intégration du service de vérification d'email.
2. **Migration Database V2** : Écriture de la migration Flyway V2 pour le schéma utilisateur étendu.
3. **Média & Profil** : Configuration de Cloudinary pour le recadrage automatique des avatars.
4. **Onboarding Frontend** : Implémentation de la sélection d'intérêts (min 3, max 10) sous Angular.

---

## 🚩 Notes Techniques
- Kafka est opérationnel dans le cluster Docker, prêt pour la Phase 3.
- PostGIS est activé en base de données pour les calculs de distance (Phase 2).

---
*Lien rapide : [planning.md](file:///c:/Users/PC/IdeaProjects/spawnta/planning.md)*
