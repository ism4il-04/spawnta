# 📊 État d'Avancement — SPAWNTA

> **Dernière mise à jour :** 07 Mai 2026
> **Progression globale :** 15% [▓▓░░░░░░░░░░░░░]

---

## 📈 Détail des Travaux par Phase

### ✅ PHASE 0 : Fondations (100%)
- **Backend** : Spring Boot 3.4, Flyway migrations, PostgreSQL PostGIS.
- **Frontend** : Angular 21, Angular Material themes, Routing.
- **DevOps** : Docker Compose (Full stack), CI/CD GitHub Actions.

### 🚧 PHASE 1 : Identité & Sécurité (40%)
- **Backend (API Security) :**
  - [x] Service JWT (`JwtService`) pour tokens stateless.
  - [x] Configuration de sécurité Spring.
  - [ ] En cours : Endpoints d'inscription (`/register`) et connexion.
  - [ ] À faire : Mécanisme Refresh Token et OAuth2 Google.
- **Profil & Social (En attente) :**
  - [ ] Modèle Profil riche (Galerie, Bio, Pays).
  - [ ] Sélection des 10 centres d'intérêt obligatoires.
  - [ ] Intégration API Cloudinary pour avatars.

### ⏳ PHASES FUTURES
- **Phase 2 :** Cartographie interactive & Moteur d'activités (0%)
- **Phase 3 :** Messagerie temps réel (Kafka/WebSockets) (0%)
- **Phase 4 :** IA (Recommandations) & Gamification (XP) (0%)
- **Phase 5 :** Dashboard Admin & Modèles Premium (0%)

---

## 🛠️ Sprint en cours (S2)
1. Finalisation de la logique `/register` avec validation des données.
2. Mise en place du module de gestion des photos de profil via Cloudinary.
3. Création de l'interface Angular pour la sélection des centres d'intérêt.

---

## 🚩 Notes Techniques
- Kafka est opérationnel dans le cluster Docker, prêt pour la Phase 3.
- PostGIS est activé en base de données pour les calculs de distance (Phase 2).

---
*Lien rapide : [planning.md](file:///c:/Users/PC/IdeaProjects/spawnta/planning.md)*
