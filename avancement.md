# SPAWNTA - Etat d'Avancement

> Derniere mise a jour : 24 Mai 2026
> Progression globale : 45%

---

## Detail des phases

### Phase 0 - Fondations
Statut : Termine

- Backend Spring Boot, Flyway et PostgreSQL/PostGIS en place
- Frontend Angular 21, Angular Material et routing initialises
- Docker Compose et base CI/CD presents

### Phase 1 - Identite, Profil et Securite
Statut : Termine

- Authentification JWT + refresh token
- Verification email
- Routes protegees et interceptor JWT
- Profil riche : bio, reseaux sociaux, centres d'interet, galerie, avatar

### Phase 2 - Cartographie interactive et Moteur d'activites
Statut : Termine

- Backend activites finalise :
  - types MEETUP et TRIP
  - participation DIRECT et APPROVAL
  - message d'introduction pour les demandes
  - capacite max / illimitee
  - limite de creation pour utilisateurs gratuits
  - endpoints create, nearby, detail, join, approve, pending requests
- Requetes geospatiales PostGIS finalisees :
  - recherche par rayon
  - filtres par categorie, date, type d'activite, mode de participation
- Frontend cartographie finalise :
  - carte Leaflet integree et redimensionnee
  - creation d'activite depuis la carte
  - affichage des meetups et trajets
  - panneau detail et approbation des demandes pour l'hote
  - filtres de recherche relies a l'API
- UI harmonisee :
  - page principale avec navbar
  - auth UI adaptee au theme
  - assets carte/profil corriges

### Phase 3 - Social temps reel et Kafka
Statut : A venir

### Phase 4 - IA et Gamification
Statut : A venir

### Phase 5 - Admin et Premium
Statut : A venir

---

## Verification recente

- Build backend Docker : OK
- Build frontend Docker : OK
- Verification API Phase 2 : OK
  - signup + verify
  - create activity
  - filtered nearby search
  - join request
  - host approval

---

## Notes

- Kafka redemarre encore dans l'environnement local, mais cela n'impacte pas la cloture de la Phase 2.
- Les variables Cloudinary doivent toujours etre configurees pour les uploads reels.
