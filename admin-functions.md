L'écosystème Spawnta fonctionne comme une boucle de rétroaction entre les utilisateurs (le "Terrain") et les administrateurs (la "Tour de Contrôle"). Voici comment les fonctionnalités Admin interagissent directement avec l'expérience utilisateur.

### 1. La Boucle de Modération (Sécurité)
C'est l'interaction la plus critique. Elle garantit que la communauté reste saine.

- Côté Utilisateur : Un utilisateur remarque un comportement inapproprié ou une activité suspecte. Il clique sur "Signaler". Cela crée une entrée dans les tables user_reports ou activity_reports .
- Côté Admin : Le signalement apparaît instantanément dans la Moderation Queue ( moderation.component.ts ).
- L'Action Admin : L'admin peut passer le rapport en INVESTIGATING . S'il décide de bannir l'utilisateur ou de supprimer l'activité :
  - L'utilisateur banni verra son accès refusé lors de la prochaine vérification du JWT par le JwtAuthenticationFilter.java .
  - L'activité supprimée disparaîtra de la carte et du flux de tous les utilisateurs en temps réel.
### 2. Gestion des Comptes et Rôles
L'administrateur a un pouvoir total sur l'identité des utilisateurs via le AdminUsersController.java .

- Interaction : Un administrateur peut promouvoir un utilisateur au rang de MODERATOR ou modifier son subscription_tier manuellement (ex: offrir un accès Premium).
- Impact : Cela débloque immédiatement des fonctionnalités pour l'utilisateur, comme la création d'activités illimitées ou l'accès à des badges exclusifs gérés par le GamificationService.java .
### 3. Flux Financier (Abonnements)
L'admin ne gère pas directement l'argent, mais il surveille la santé économique via l'intégration Stripe .

- Côté Utilisateur : L'utilisateur souscrit à un plan via le SubscriptionController.java .
- Côté Admin : Une fois le paiement validé, Stripe envoie un Webhook au StripeWebhookController.java .
- Interaction : L'admin voit le revenu total et le nombre d'abonnés augmenter sur son Dashboard . Il peut consulter les transactions dans le module Subscriptions pour résoudre d'éventuels litiges.
### 4. Surveillance des Activités (Le Pouls)
L'admin surveille ce qui se passe réellement sur la plateforme.

- Côté Utilisateur : Chaque création d'activité, participation ou check-in via QR Code génère des données.
- Côté Admin : Ces données sont agrégées par le AdminDashboardService.java .
- Interaction : L'admin utilise ces stats pour comprendre quelles catégories d'activités sont les plus populaires et peut décider de mettre en avant certains types d'événements ou de nettoyer les activités obsolètes/inactives.
### 5. Journaux d'Audit (Transparence)
Toutes les actions sensibles de l'admin sont enregistrées.

- Fonctionnement : Lorsqu'un admin supprime un utilisateur ou résout un rapport, une entrée est créée dans AdminAuditLog.java .
- Utilité : Cela permet de vérifier "qui a fait quoi" si un utilisateur conteste une sanction, garantissant que l'équipe d'administration reste elle aussi responsable.
### Résumé Technique de l'Interaction
Action Admin Impact Utilisateur Composant Backend Clé Ban / Suspension Accès API bloqué (403) AdminUserService Suppression d'Activité Disparition du Map/Feed ActivityService Validation de Rapport Notification de sanction AdminModerationService Changement de Rôle Nouveaux droits d'accès UserRepository Suivi Stripe Mise à jour du grade Premium StripeWebhookService

En résumé, l'interface Admin est le cerveau qui régule le comportement des utilisateurs, protège la communauté et assure la rentabilité de Spawnta.