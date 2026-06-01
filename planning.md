# SPAWNTA - Roadmap

> Derniere mise a jour : 24 Mai 2026

---

## Stack

- API : Spring Boot + Spring Data JPA + Spring Security
- Frontend : Angular 21 + Angular Material + Leaflet
- Geo : PostgreSQL + PostGIS
- Temps reel : WebSockets + Kafka
- Media : Cloudinary

---

## Phases

### Phase 0 - Infrastructure et fondations
Statut : Termine

- Setup backend Spring
- Setup frontend Angular
- Docker Compose
- PostgreSQL/PostGIS
- Base des migrations

### Phase 1 - Identite, profil et securite
Statut : Termine

- JWT + refresh token
- login / signup / logout
- verification email
- profil riche
- interets
- galerie
- avatar
- confidentialite

### Phase 2 - Activites et cartographie interactive
Statut : Termine

#### Backend
- [x] Support des activites `MEETUP` et `TRIP`
- [x] Recherche geospatiale avec `ST_DWithin`
- [x] Recuperation par rayon
- [x] Filtres par categorie, date, type d'activite et mode de participation
- [x] Participation `DIRECT` et `APPROVAL`
- [x] Message d'introduction pour les demandes
- [x] Gestion de capacite max / illimitee
- [x] Limite de creation hebdomadaire pour utilisateur gratuit
- [x] Approbation des demandes par l'hote

#### Frontend
- [x] Carte Leaflet integree
- [x] Affichage des meetups et trajets
- [x] Creation d'activite depuis la carte
- [x] Detail d'activite
- [x] Filtres cartes relies a l'API
- [x] UI redimensionnee et harmonisee avec le theme principal

### Phase 3 - Social temps reel et pipeline evenementiel
Statut : A venir

- Chat de groupe
- Chat prive
- WebSocket securise
- Topics Kafka metier
- notifications temps reel

### Phase 4 - Post-sortie, gamification et IA
Statut : Termine

- [x] Planification complète avec architecture
- [x] Entités gamification (XP, niveaux, badges) — Commit 1
- [x] Validation de présence (check-in, QR code) — Commit 2
- [x] Système de notation d'activités — Commit 2
- [x] Schéma recommandations et notifications — Commit 3
- [x] Services gamification (XP & leveling) — Commit 4
- [x] Services validation et présence — Commit 5
- [x] Moteur recommandations (scoring) — Commit 6
- [x] Pipeline notifications temps réel — Commit 7
- [x] API endpoints gamification — Commit 8
- [x] API endpoints attendance & rating — Commit 9
- [x] API endpoints recommandations — Commit 10
- [x] UI composants gamification — Commit 11
- [x] UI composants check-in & rating — Commit 12
- [x] UI feed recommandations & inbox notifications

### Phase 5 - Monetisation et dashboard admin
Statut : A venir

- abonnement premium
- dashboard admin technique
- dashboard admin metier
- moderation utilisateurs / activites

---

## 📚 Documentation


- Sprint courant : Phase 3 - Social temps reel, chat et pipeline evenementiel
- Progression globale estimee : 70%
- Phase active a venir : Phase 3 - Social temps reel, chat et pipeline evenementiel

