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
Statut : En cours (15 commits planifiés)

- [x] Planification complète avec architecture
- [x] Entités gamification (XP, niveaux, badges) — Commit 1
- [x] Validation de présence (check-in, QR code) — Commit 2
- [x] Système de notation d'activités — Commit 2
- [ ] Schéma recommandations et notifications — Commit 3
- [ ] Services gamification (XP & leveling) — Commit 4
- [ ] Services validation et présence — Commit 5
- [ ] Moteur recommandations (scoring) — Commit 6
- [ ] Pipeline notifications temps réel — Commit 7
- [ ] API endpoints gamification — Commit 8
- [ ] API endpoints attendance & rating — Commit 9
- [ ] API endpoints recommandations — Commit 10
- [ ] UI composants gamification — Commit 11
- [ ] UI composants check-in & rating — Commit 12
- [ ] UI feed recommandations & inbox notifications — Commit 13
- [ ] Tests unitaires & intégration — Commit 14
- [ ] Documentation API & deployment — Commit 15

**Détails**: Voir `PHASE4_IMPLEMENTATION_PLAN.md` pour plan complet, architecture, fichiers à créer, et timeline (7-8 semaines estimées).

### Phase 5 - Monetisation et dashboard admin
Statut : A venir

- abonnement premium
- dashboard admin technique
- dashboard admin metier
- moderation utilisateurs / activites

---

## 📚 Documentation

- `PHASE4_EXECUTIVE_SUMMARY.md` - High-level overview, business impact, timeline (read first!)
- `PHASE4_IMPLEMENTATION_PLAN.md` - Complete technical architecture, 15 commits breakdown
- `PHASE4_COMMIT_CHECKLIST.md` - Detailed per-commit checklist with verification steps
- `PHASE4_QUICK_REFERENCE.md` - Quick lookup tables, APIs, debugging tips

- Sprint courant : preparation Phase 3
- Progression globale estimee : 45%
- Phase active a venir : temps reel, chat et pipeline evenementiel
