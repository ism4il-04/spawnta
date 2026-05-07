# 🗺️ SPAWNTA — Roadmap & Planning Détaillé

> **Spawnta** : L'application web de cartographie sociale spontanée. 
> *Connecter l'humain à travers des activités locales, du café du coin aux randonnées lointaines.*

---

## 👥 Équipe & Responsabilités
| Rôle | Responsable | Focus Technique |
| :--- | :--- | :--- |
| 🛡️ **Dev 1** | Backend Lead | Sécurité (JWT/OAuth2), Architecture Core, Gestion des rôles. |
| 🧠 **Dev 2** | AI & Business | Algorithmes de recommandation, Gamification (XP), Logique métier complexe. |
| 🎨 **Dev 3** | Frontend Lead | UX/UI Angular 21, Animations Material, Cartographie Leaflet. |
| 🚀 **Dev 4** | DevOps & Admin | CI/CD, Infrastructure Docker/Kafka, Dashboard Admin, Monitoring. |

---

## 🛠️ Architecture & Stack
- **API :** Spring Boot 3.4 + Spring Data JPA + Spring Security.
- **Client :** Angular 21 + RxJS + Angular Material + Tailwind CSS (Optionnel).
- **Temps Réel :** WebSockets (STOMP) + Apache Kafka.
- **Géo :** PostgreSQL + Extension PostGIS.
- **Storage :** Cloudinary (Stockage des photos/vidéos).

---

## 📅 Phases de Développement Détaillées

### ✅ PHASE 0 — Infrastructure & Fondations
*Statut : Terminé*

- [x] **Setup Backend** : Initialisation Maven, modules Spring Web, JPA, Security.
- [x] **Setup Database** : Instance Docker PostgreSQL avec extension PostGIS activée.
- [x] **Migrations** : Premier script Flyway pour la table `users` et `roles`.
- [x] **Setup Frontend** : Workspace Angular, installation Angular Material et Leaflet.
- [x] **DevOps** : Pipeline GitHub Actions pour Lint/Test/Build et Docker Hub.

---

### 🚧 PHASE 1 — Identité, Profil & Sécurité
*Statut : En cours (Avancement : 40%)*

- **Authentification (Backend) :**
  - [x] `JwtService` pour génération et validation des tokens.
  - [ ] Endpoints `/api/auth/register` et `/api/auth/login`.
  - [ ] Mécanisme de **Refresh Token** pour sessions prolongées.
  - [ ] Intégration **OAuth2 Google** (Backend-side).
- **Gestion du Profil Riche (Backend/Frontend) :**
  - [ ] **Modèle Profil** : Ajout biographie, galerie (liste de liens Cloudinary), pays visités.
  - [ ] **Réseaux Sociaux** : Champs dédiés (Facebook, Instagram, WhatsApp).
  - [ ] **Centres d'intérêt** : Logique de sélection stricte (Max 10 intérêts).
  - [ ] **Confidentialité** : Service de gestion des champs Publics vs Privés.
  - [ ] **Média** : Service d'upload d'image vers Cloudinary avec redimensionnement.

---

### ⏳ PHASE 2 — Activités & Cartographie Interactive
*Statut : À venir*

- **Moteur d'Activités (Backend) :**
  - [ ] **CRUD Activités** : Titre, description, horodatage, photo de couverture.
  - [ ] **Géospatial** : Support des types `Point` (Lieu unique) et `LineString` (Itinéraire).
  - [ ] **Logique de participation** :
    - [ ] Mode "Direct" vs "Approbation requise".
    - [ ] Gestion stricte de la **Capacité** (Places restantes).
- **Interface Cartographique (Frontend) :**
  - [ ] **Leaflet Integration** : Affichage des clusters d'activités.
  - [ ] **Recherche Géo** : Filtrage par rayon (ST_DWithin) et type d'activité.
  - [ ] **Dynamic View** : Basculement entre vue Carte et vue Liste.

---

### ⏳ PHASE 3 — Social, Temps Réel & Messagerie
*Statut : À venir*

- **Messagerie & Notifications :**
  - [ ] **Chat Individuel** : Communication 1-to-1 en temps réel (WebSockets).
  - [ ] **Chat de Sortie** : Salon de discussion automatique pour chaque activité créée.
  - [ ] **Flux Kafka** : Diffusion des événements (Nouveau message, Nouvelle participation).
- **Fonctionnalités Sociales :**
  - [ ] **Gestion d'amis** : Système de demande, acceptation et blocage.
  - [ ] **Espace Média Partagé** : Galerie collaborative par sortie (Upload pour participants).

---

### ⏳ PHASE 4 — Post-Sortie, Gamification & Intelligence Artificielle
*Statut : À venir*

- **Logique Post-Sortie :**
  - [ ] **Validation de présence** : Interface pour l'organisateur (Marquage présents/absents).
  - [ ] **Système d'Avis** : Notes et commentaires sur les participants (Confiance communauté).
- **Gamification (XP) :**
  - [ ] **Moteur de progression** : Calcul d'XP basé sur (Evaluations + Présence + Nombre participants).
  - [ ] **Niveaux** : Déblocage de badges et visibilité accrue du profil.
- **Moteur d'IA (Recommandations) :**
  - [ ] **Suggestions d'activités** : Analyse sémantique des intérêts vs activités proches.
  - [ ] **Suggestions d'amis** : Basées sur les localisations communes et intérêts partagés.

---

### ⏳ PHASE 5 — Monétisation & Administration
*Statut : À venir*

- **Business Models :**
  - [ ] **Abonnement Premium** :
    - [ ] Création illimitée d'activités.
    - [ ] Visibilité prioritaire (Boost algorithmique sur la carte).
    - [ ] Perks messagerie (Indicateurs de frappe, thèmes).
- **Back-Office Admin :**
  - [ ] **Dashboard Admin** : Statistiques globales (Inscriptions, Activités actives).
  - [ ] **Modération** : Signalement d'activités et bannissement d'utilisateurs.
  - [ ] **Monitoring** : Logs centralisés et santé des services (Actuator).

---

## 💎 Comparatif des Offres
| Feature | 🆓 Free | 💎 Premium |
| :--- | :---: | :---: |
| Activités / Semaine | Limité (3) | **Illimité** |
| Visibilité Carte | Standard | **Prioritaire (Top Search)** |
| Centres d'intérêt | Max 10 | **Illimité** |
| Galerie Photo | Standard | **Étendue** |
| Ads | Présentes | **Ad-Free** |

---

## 📈 Suivi & KPI
- **Sprint Actuel** : S2 — Finalisation de la base Auth & Profil.
- **Vitesse Équipe** : Moyenne de 12 tickets / semaine.
- **Progression Totale** : [▓▓░░░░░░░░░░░░░] 15%

> *Dernière mise à jour : 07 Mai 2026*

