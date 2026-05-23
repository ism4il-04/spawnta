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

### 🚧 PHASE 1 — Identité, Inscription & Sécurité
*Statut : En cours (Avancement : 40%)*

- **Authentification & Sécurité (Backend) :**
  - [x] `JwtService` pour génération et validation des tokens JWT.
  - [ ] Implémentation du filtre `JwtAuthenticationFilter` pour sécuriser les routes et configurer le Context Spring Security (TODO actuel dans `SecurityConfig`).
  - [ ] Validation stricte du mot de passe à l'inscription (Min 8 caractères, au moins un chiffre, vérification de correspondance avec `confirmPassword`).
  - [ ] Endpoints `/api/auth/register` (à la place de `/signup`) et `/api/auth/login`.
  - [ ] **Validation par Email** : Envoi d'un mail de confirmation (Spring Mail) via l'endpoint `/api/auth/verify-email` pour valider le compte (obligatoire avant l'étape 2).
  - [ ] **Refresh Token** : Génération d'un token de rafraîchissement (durée 7 jours, stocké dans Redis), retourné sous forme de cookie `HttpOnly` + `Secure`. Endpoint `/api/auth/refresh`.
  - [ ] **Blacklist JWT** : Gestion de la déconnexion via la mise en blacklist des JTI des tokens actifs dans Redis (`token:blacklist:{jti}`).
  - [ ] Intégration **OAuth2 Google / Facebook** (Backend-side) pour l'inscription et la connexion rapides.
- **Logique d'Inscription Multi-étapes & Profil Riche (Backend/Frontend) :**
  - [ ] **Migration DB (Flyway V2)** : Ajout des colonnes de profil dans `users` (`username`, `date_of_birth`, `city`, `level`, `xp`, `profile_picture_url`, `is_premium`, `is_banned`, `is_email_verified`) et création des tables associées (`interests`, `user_interests`, `user_photos`, `user_social_links`, `user_countries_visited`).
  - [ ] **Inscription Étape 2** : Saisie du nom d'utilisateur unique (vérification de disponibilité en temps réel via API `/api/users/check-username`), date de naissance (validation de l'âge >= 13 ans) et ville de résidence.
  - [ ] **Inscription Étape 3 (Centres d'intérêt)** : Sélection de 3 à 10 intérêts (Grille interactive sous Angular).
  - [ ] **Confidentialité** : Gestion des droits de visibilité des champs (Biographie, Galerie, Pays visités, Réseaux Sociaux) : Public, Amis uniquement, ou Privé.
  - [ ] **Réseaux Sociaux** : Liens URL vers Facebook, Instagram, et numéro WhatsApp (format international pour lien de chat direct).
  - [ ] **Média & Cloudinary** : Service d'upload d'image vers Cloudinary (redimensionnement automatique en 300x300 px pour les avatars). Limitation à 12 photos dans la galerie pour les utilisateurs gratuits (illimité pour Premium).

---

### ⏳ PHASE 2 — Activités, Cartographie & Limitations
*Statut : À venir*

- **Moteur d'Activités & Géospatial (Backend) :**
  - [ ] **Types d'Activités** : Support pour **Local Meetup** (Point géographique de rencontre unique) et **Trip / Excursion** (Point de départ `Start Location` et destination `Destination` avec tracé `LineString`).
  - [ ] **Requêtes PostGIS** : Implémentation de la recherche d'activités à proximité à l'aide de la fonction spatiale indexée `ST_DWithin` (filtre par rayon en mètres).
  - [ ] **Logique de participation** :
    - [ ] Mode "Direct" vs "Approbation requise" (avec possibilité d'envoyer un message d'introduction de 150 caractères maximum).
    - [ ] Gestion stricte de la **Capacité** (nombre de places max ou illimité).
    - [ ] Limitation stricte de création : **max 2 activités par semaine** pour le plan Gratuit (illimité pour Premium).
- **Interface Cartographique (Frontend) :**
  - [ ] **Leaflet Integration** : Rendu de la carte interactive avec `ngx-leaflet`, affichage de pins thématiques par catégorie et dessin d'itinéraires.
  - [ ] **Filtres de recherche** : Filtrage dynamique par rayon de distance, date, catégorie et type de participation.

---

### ⏳ PHASE 3 — Social, Temps Réel (WebSockets) & Pipeline Événementiel (Kafka)
*Statut : À venir*

- **Messagerie & Communication Temps Réel :**
  - [ ] **STOMP over SockJS** : Configuration du WebSocket Message Broker pour la diffusion des messages.
  - [ ] **Sécurité WebSocket** : Interception du token JWT lors de la connexion initiale (`ChannelInterceptor`).
  - [ ] **Chat de Sortie (Group Chat)** : Salon automatique créé pour chaque activité. Textes, emojis, et partage d'images autorisés.
  - [ ] **Chat Privé 1-to-1** : Limité aux utilisateurs ayant une relation d'amitié validée.
  - [ ] **Shared Media Space** : Galerie de partage collaborative par sortie (Vidéos max 2 min pour Gratuit vs 10 min pour Premium).
- **Architecture Événementielle (Kafka) :**
  - [ ] Publication et consommation d'événements asynchrones sur les topics suivants :
    - `user.registered` (envoi email de bienvenue).
    - `activity.created` (notification utilisateurs proches).
    - `participation.requested`/`approved`/`declined` (gestion des demandes de participation).
    - `attendance.confirmed` (calcul d'XP).
    - `rating.submitted` (mise à jour des notes et XP).
    - `notification.created` (push temps réel vers WebSocket).
    - `user.leveled_up` (notification de gain de niveau).

---

### ⏳ PHASE 4 — Post-Sortie, Gamification & Intelligence Artificielle
*Statut : À venir*

- **Logique Post-Sortie & Présence :**
  - [ ] **Validation de présence** : Formulaire pour l'organisateur (Présent vs Absent) obligatoire après la date de fin estimée.
  - [ ] **Évaluation par les pairs (Anonyme)** : Évaluation croisée des participants (note de 1 à 5 étoiles) + tags prédéfinis (*Friendly*, *Punctual*, *Fun*, *Respectful*, *Communicative*).
- **Gamification & XP :**
  - [ ] **Moteur de calcul XP** : Algorithme basé sur la participation, le rôle d'hôte (bonus selon le nombre de présents et notes reçues), et les évaluations des pairs.
  - [ ] **Niveaux et Titres** : Déblocage de badges de niveau (ex: *Trail Explorer*) avec animation visuelle sur le profil.
- **Moteur d'IA & Suggestions :**
  - [ ] **Recommandations d'Activités** : Filtrage collaboratif pondérant les intérêts partagés (40%), la note de l'hôte (20%), la distance (20%), le temps restant (10%) et les amis présents (10%).
  - [ ] **Recommandations d'Amis** : Suggestions "People You May Know" basées sur les intérêts communs, les amis mutuels et les sorties communes.

---

### ⏳ PHASE 5 — Monétisation & Administration Dashboard
*Statut : À venir*

- **Business Models & Paiements :**
  - [ ] **Souscription Premium** : Formulaire de paiement (Stripe / PayPal) pour abonnement mensuel/annuel.
  - [ ] Enregistrement et mise à jour immédiate du rôle `ROLE_PREMIUM`.
- **Back-Office d'Administration :**
  - [ ] **Dashboard Technique (Spring Boot Admin)** : Monitoring des ressources, Actuator endpoints, états des connexions et des bases de données.
  - [ ] **Dashboard Métier (Angular + CoreUI Template)** :
    - Gestion des utilisateurs (Recherche, Bannissement/Débannissement).
    - Gestion des activités (Modération, suppression des sorties non conformes).
    - Gestion des rapports d'abus et des abonnements.
    - Graphiques de statistiques d'activité (inscriptions, XP, tops catégories).

---

## 💎 Comparatif des Offres
| Feature | 🆓 Free | 💎 Premium |
| :--- | :---: | :---: |
| Création d'activités / Semaine | Limité (2) | **Illimité** |
| Visibilité Carte & Feed | Standard | **Prioritaire (Boostée)** |
| Centres d'intérêt | Max 10 | **Illimité** |
| Photos Galerie | Max 12 | **Illimité** |
| Durée Vidéo (Shared Space) | Max 2 min | **Max 10 min** |
| Fonctionnalités Chat | Standard | **Accusés de lecture & Indicateur de saisie** |
| Publicités | Présentes | **Sans Publicité** |

---

## 📈 Suivi & KPI
- **Sprint Actuel** : S2 — Finalisation de la base Auth (validation, email verification) & Profil riche (Flyway V2, grid intérêts).
- **Vitesse Équipe** : Moyenne de 12 tickets / semaine.
- **Progression Totale** : [▓▓░░░░░░░░░░░░░] 15%

> *Dernière mise à jour : 23 Mai 2026*

