# Spawnta — Technical Specifications

**Project:** Spawnta
**Team:** Ismail LYAMANI, Abdellatif OUMHELLA, Zakariyae EL ALLOUCHE, Mohammed Aymane Saber
**Level/Institution:** 4ème année Génie Informatique, ENSA Tétouan
**Module:** JEE (Java Enterprise Edition)

---

## Table of Contents

1. [Architecture Overview](#1-architecture-overview)
2. [Backend — Spring Boot](#2-backend--spring-boot)
3. [Frontend — Angular](#3-frontend--angular)
4. [Back-Office Admin Interface](#4-back-office-admin-interface)
5. [Database — PostgreSQL](#5-database--postgresql)
6. [Caching — Redis](#6-caching--redis)
7. [Messaging & Events — Kafka](#7-messaging--events--kafka)
8. [Media Storage — Cloudinary / Local](#8-media-storage--cloudinary--local)
9. [Real-Time Communication — WebSocket](#9-real-time-communication--websocket)
10. [Authentication & Security](#10-authentication--security)
11. [AI Integration](#11-ai-integration)
12. [Maps & Geolocation](#12-maps--geolocation)
13. [DevOps & Infrastructure](#13-devops--infrastructure)
14. [Full Technology Stack Summary](#14-full-technology-stack-summary)

---

## 1. Architecture Overview

Spawnta follows a **monolithic modular architecture** (recommended for a JEE academic project with a clear path toward microservices if needed later). The system is divided into the following layers:

```
+------------------------------------------------------------------+
|                           Clients                                |
|   Angular SPA (User App)       Angular SPA (Admin Back-Office)   |
+----------------------------+-------------------------------------+
                             |  HTTPS / WSS
+----------------------------v-------------------------------------+
|                     API Gateway / Nginx                          |
|      (Reverse proxy, SSL termination, static file serving)       |
+----------------------------+-------------------------------------+
                             |
+----------------------------v-------------------------------------+
|              Spring Boot Application (Backend API)               |
|                                                                  |
|  +---------------+  +----------------+  +--------------------+  |
|  |  REST APIs    |  |  WebSocket     |  |  Kafka Producers   |  |
|  | (Controllers) |  |  Handlers      |  |  & Consumers       |  |
|  +-------+-------+  +-------+--------+  +--------------------+  |
|          |                  |                                    |
|  +--------v------------------v---------------------------------+ |
|  |                    Service Layer                            | |
|  |   (Business logic, validation, orchestration)              | |
|  +------------------------------+-----------------------------+ |
|                                 |                               |
|  +------------------------------v-----------------------------+ |
|  |                  Repository Layer (JPA)                    | |
|  +------------------------------+-----------------------------+ |
+--------------------------------+--------------------------------+
                                 |
          +----------------------+-------------------+
          |                      |                   |
+---------v-------+  +-----------v------+  +---------v----------+
|   PostgreSQL    |  |     Redis        |  |   Apache Kafka     |
|  (Primary DB)   |  |  (Cache +        |  | (Event streaming,  |
|  + PostGIS      |  |   Sessions)      |  |  notifications)    |
+-----------------+  +------------------+  +----------+---------+
                                                       |
                                           +-----------v----------+
                                           |  Cloudinary / Local  |
                                           |   (Media Storage)    |
                                           +----------------------+
```

---

## 2. Backend — Spring Boot

### 2.1. Framework & Version

| Item | Choice |
|---|---|
| Framework | Spring Boot 3.x |
| Java Version | Java 21 (LTS) |
| Build Tool | Maven |
| API Style | RESTful JSON API |

### 2.2. Spring Boot Dependencies (starters)

| Dependency | Purpose |
|---|---|
| `spring-boot-starter-web` | REST controllers, HTTP layer |
| `spring-boot-starter-data-jpa` | ORM with Hibernate, repository pattern |
| `spring-boot-starter-security` | Authentication, authorization, filter chain |
| `spring-boot-starter-websocket` | Real-time group chat and direct messaging |
| `spring-boot-starter-data-redis` | Redis integration for caching and session management |
| `spring-boot-starter-validation` | Bean validation (`@Valid`, `@NotBlank`, etc.) |
| `spring-boot-starter-mail` | Email sending (account verification, password reset) |
| `spring-boot-starter-actuator` | Health checks, metrics endpoint for monitoring |
| `spring-kafka` | Kafka producer and consumer support |
| `springdoc-openapi-starter-webmvc-ui` | Auto-generated Swagger UI for API documentation |
| `jjwt` (io.jsonwebtoken) | JWT creation, signing, and validation |
| `cloudinary-http44` | Cloudinary SDK for media upload/management |
| `mapstruct` | DTO <-> Entity mapping |
| `lombok` | Boilerplate reduction (`@Getter`, `@Builder`, etc.) |

### 2.3. Module Structure (Package Layout)

```
com.spawnta
├── config/           # Security config, Kafka config, Redis config, CORS config
├── controller/       # REST controllers (one per domain)
│   ├── AuthController
│   ├── UserController
│   ├── ActivityController
│   ├── ParticipationController
│   ├── ChatController
│   ├── FriendController
│   ├── RatingController
│   ├── NotificationController
│   └── AdminController
├── service/          # Business logic (interfaces + implementations)
├── repository/       # JPA repositories (Spring Data)
├── entity/           # JPA entities mapped to DB tables
├── dto/              # Request/Response DTOs
├── mapper/           # Entity <-> DTO mappers (MapStruct)
├── exception/        # Custom exceptions + global exception handler
├── security/         # JWT filter, UserDetailsService, auth utilities
├── kafka/            # Producers and consumers
├── websocket/        # WebSocket message handlers and config
└── util/             # Shared utilities
```

### 2.4. Key API Endpoints (Summary)

| Module | Endpoint | Method | Description |
|---|---|---|---|
| Auth | `/api/auth/register` | POST | User registration |
| Auth | `/api/auth/login` | POST | Login, returns JWT |
| Auth | `/api/auth/verify-email` | GET | Email verification |
| Auth | `/api/auth/forgot-password` | POST | Send reset email |
| Auth | `/api/auth/reset-password` | POST | Submit new password |
| Auth | `/api/auth/oauth/google` | GET | OAuth Google redirect |
| Users | `/api/users/me` | GET | Get own profile |
| Users | `/api/users/me` | PUT | Update own profile |
| Users | `/api/users/{id}` | GET | View another user's profile |
| Users | `/api/users/search` | GET | Search users by name/username |
| Activities | `/api/activities` | POST | Create an activity |
| Activities | `/api/activities` | GET | List/filter activities (feed) |
| Activities | `/api/activities/{id}` | GET | Activity detail |
| Activities | `/api/activities/{id}` | PUT | Edit activity (host only) |
| Activities | `/api/activities/{id}` | DELETE | Cancel activity (host only) |
| Activities | `/api/activities/nearby` | GET | Map pin query by lat/lng radius |
| Participation | `/api/activities/{id}/join` | POST | Direct join |
| Participation | `/api/activities/{id}/request` | POST | Request to join |
| Participation | `/api/activities/{id}/requests` | GET | Host views pending requests |
| Participation | `/api/activities/{id}/requests/{uid}` | PUT | Host approves/declines |
| Participation | `/api/activities/{id}/leave` | DELETE | Leave an activity |
| Post-Trip | `/api/activities/{id}/attendance` | POST | Host confirms attendance |
| Post-Trip | `/api/activities/{id}/ratings` | POST | Submit experience + peer ratings |
| Friends | `/api/friends/request/{id}` | POST | Send friend request |
| Friends | `/api/friends/request/{id}` | PUT | Accept/decline request |
| Friends | `/api/friends` | GET | List friends |
| Friends | `/api/friends/{id}` | DELETE | Remove friend |
| Notifications | `/api/notifications` | GET | Get notification list |
| Notifications | `/api/notifications/{id}/read` | PUT | Mark as read |
| Admin | `/api/admin/users` | GET | List all users |
| Admin | `/api/admin/users/{id}` | PUT | Ban / modify user |
| Admin | `/api/admin/activities` | GET | List all activities |
| Admin | `/api/admin/activities/{id}` | DELETE | Remove an activity |
| Admin | `/api/admin/stats` | GET | Platform statistics |

---

## 3. Frontend — Angular

### 3.1. Framework & Version

| Item | Choice |
|---|---|
| Framework | Angular 17+ (standalone components, signals) |
| Language | TypeScript 5.x |
| UI Component Library | Angular Material |
| State Management | Angular Signals + NgRx for complex flows |
| HTTP Client | Angular `HttpClient` with interceptors |
| Routing | Angular Router with lazy-loaded feature modules |
| Real-Time | `@stomp/stompjs` + `sockjs-client` for WebSocket |
| Maps | Leaflet.js via `ngx-leaflet` |
| Form Handling | Angular Reactive Forms |
| Package Manager | npm |

### 3.2. Application Structure

```
src/app/
├── core/                    # Singleton services, guards, interceptors
│   ├── services/
│   │   ├── auth.service.ts
│   │   ├── user.service.ts
│   │   └── websocket.service.ts
│   ├── guards/
│   │   ├── auth.guard.ts
│   │   └── role.guard.ts
│   └── interceptors/
│       └── jwt.interceptor.ts   # Attaches Bearer token to all requests
├── shared/                  # Reusable components, pipes, directives
│   ├── components/
│   │   ├── activity-card/
│   │   ├── user-avatar/
│   │   └── star-rating/
│   └── pipes/
│       └── time-ago.pipe.ts
├── features/                # Feature modules (lazy loaded)
│   ├── auth/                # Login, Register, Forgot Password
│   ├── map/                 # Interactive activity map
│   ├── feed/                # Activity feed + filters
│   ├── activity-detail/     # Full activity page + join logic
│   ├── activity-create/     # Creation form (multi-step)
│   ├── trip-hub/            # Group chat + media space
│   ├── profile/             # Own profile + edit
│   ├── user-profile/        # Viewing another user's profile
│   ├── friends/             # Friend list, requests, suggestions
│   ├── messages/            # 1-on-1 conversations list
│   ├── notifications/       # Notification center
│   └── settings/            # Account settings, privacy, subscription
└── app.routes.ts            # Root routing configuration
```

### 3.3. Key Libraries

| Library | Purpose |
|---|---|
| `@angular/material` | UI components (buttons, dialogs, snackbars, etc.) |
| `ngx-leaflet` + `leaflet` | Interactive map rendering |
| `@stomp/stompjs` + `sockjs-client` | WebSocket / STOMP client for real-time chat |
| `@auth0/angular-jwt` | JWT decoding and expiry checks on the frontend |
| `ngx-toastr` | Toast notifications |
| `ngx-image-cropper` | Profile picture cropping before upload |
| `dayjs` | Date formatting and relative time (e.g., "3 hours ago") |
| `chart.js` + `ng2-charts` | Charts in the admin back-office dashboard |

---

## 4. Back-Office Admin Interface

### 4.1. Should You Build It or Use a Ready-Made Solution?

**Short answer: Use a ready-made admin template — do not design the admin UI shell from scratch.**

Two complementary options exist and are both recommended:

---

### Option A — Spring Boot Admin (for infrastructure monitoring)

**What it is:** An open-source web UI that connects directly to your Spring Boot Actuator endpoints. Requires almost zero frontend development — add two Maven dependencies and one `@EnableAdminServer` annotation.

**What it provides out of the box:**
- Application health, uptime, memory, and CPU metrics
- Live log streaming
- Environment variable inspection
- Kafka and datasource health checks

**Limitation:** It is a monitoring-only dashboard. It does not provide CRUD management of users and activities.

---

### Option B — Angular Admin Module with CoreUI Template (for platform management)

**What it is:** A dedicated Angular application (in the same Angular workspace) built on top of **CoreUI for Angular** (`@coreui/angular`) — a free MIT-licensed admin dashboard template that provides the sidebar, top navigation, breadcrumbs, data table layout, and chart widgets. You only implement the data calls and table content.

**Admin screens to implement on top of the template:**

| Screen | What It Does |
|---|---|
| Dashboard | KPI cards: total users, active trips today, new registrations this week; line chart of activity over time |
| User Management | Paginated searchable table of all users; view profile; ban/unban toggle; delete account |
| Activity Management | Paginated table of all activities; filter by status/date/category; view detail; force-delete abusive activities |
| Reports & Moderation | List of reported users/activities; review and take action (warn, ban, remove content) |
| Subscription Management | View Premium subscribers; see status; manually grant or revoke Premium |
| Platform Statistics | Charts: daily active users, trips per day, top categories, XP distribution |
| Broadcast Notifications | Send a push notification to all users or a filtered segment |

**Authentication:** The admin Angular app calls the same Spring Boot `/api/auth/login` endpoint. The JWT carries a `ROLE_ADMIN` claim. An Angular `RoleGuard` blocks access to all admin routes for non-admins. On the backend, all `/api/admin/**` endpoints are secured with `.hasRole("ADMIN")` in the Spring Security config.

---

### 4.2. Recommendation for This Project

Use **both options together**:

- **Spring Boot Admin** handles infrastructure monitoring (health, logs, memory) — takes 15 minutes to set up.
- **CoreUI Angular** handles platform management (users, activities, moderation) — you build on a ready shell so you skip all layout and navigation work.

This is the most practical approach for a JEE module project: it shows professional tooling awareness, saves design time, and delivers a fully functional admin experience.

---

## 5. Database — PostgreSQL

### 5.1. Version & Access

| Item | Choice |
|---|---|
| Version | PostgreSQL 15+ with PostGIS extension |
| Access from Spring Boot | Spring Data JPA + Hibernate ORM |
| Connection Pool | HikariCP (bundled with Spring Boot) |
| Migrations | Flyway (schema versioning; runs automatically on startup) |

### 5.2. Core Entities & Relationships

```
users
  id, username, email, password_hash, full_name, bio, date_of_birth,
  city, level, xp, profile_picture_url, is_premium, is_banned,
  is_email_verified, created_at

user_interests          (many-to-many: users <-> interests)
  user_id, interest_id

interests
  id, name, icon_url

user_photos             (gallery)
  id, user_id, url, caption, uploaded_at

user_social_links
  id, user_id, platform (FACEBOOK|INSTAGRAM|WHATSAPP), value

user_countries_visited  (many-to-many: users <-> countries)
  user_id, country_code

activities
  id, host_user_id, title, description, type (MEETUP|TRIP),
  category_tag_1, category_tag_2, category_tag_3,
  cover_picture_url, start_datetime, end_datetime,
  meeting_lat, meeting_lng, meeting_place_name,
  destination_lat, destination_lng, destination_place_name,
  join_mode (DIRECT|REQUEST), capacity, status (DRAFT|ACTIVE|ENDED|CANCELLED),
  created_at

participations
  id, activity_id, user_id, status (PENDING|APPROVED|DECLINED|LEFT),
  join_message, requested_at, resolved_at, attended (BOOLEAN, set post-trip)

activity_media          (Trip Hub shared media)
  id, activity_id, uploader_user_id, url, media_type (PHOTO|VIDEO),
  caption, uploaded_at

friendships
  id, requester_id, addressee_id, status (PENDING|ACCEPTED|DECLINED|BLOCKED),
  created_at

messages                (1-on-1 direct messages)
  id, sender_id, receiver_id, content, sent_at, is_read

group_messages          (Trip Hub group chat)
  id, activity_id, sender_id, content, sent_at

ratings
  id, activity_id, rater_id, ratee_id (NULL = overall trip rating),
  stars (1-5), review_text, is_anonymous, created_at

rating_tags             (positive peer tags)
  rating_id, tag_name

notifications
  id, recipient_id, type (ENUM), reference_id, reference_type,
  message, is_read, created_at

subscriptions
  id, user_id, plan (FREE|PREMIUM), start_date, end_date, payment_ref
```

### 5.3. Indexing Strategy

| Table | Index |
|---|---|
| `activities` | `(meeting_lat, meeting_lng)` spatial index (PostGIS); `(status, start_datetime)` composite |
| `participations` | `(activity_id, status)` composite |
| `messages` | `(sender_id, receiver_id)` composite |
| `notifications` | `(recipient_id, is_read)` composite |
| `friendships` | `(requester_id, addressee_id)` unique composite |

---

## 6. Caching — Redis

### 6.1. Version & Client

| Item | Choice |
|---|---|
| Version | Redis 7.x |
| Spring Integration | `spring-boot-starter-data-redis` + Lettuce client (default) |

### 6.2. What Gets Cached

| Cache Key Pattern | Content | TTL |
|---|---|---|
| `user:profile:{id}` | Serialized user profile DTO | 10 minutes |
| `activity:detail:{id}` | Full activity detail DTO | 5 minutes |
| `activity:feed:{filterHash}` | Paginated feed result for a given filter combination | 2 minutes |
| `activity:nearby:{lat}:{lng}:{radius}` | Map pin list for a bounding box | 1 minute |
| `user:friends:{id}` | Friend list | 5 minutes |
| `suggestions:activities:{userId}` | AI-generated activity suggestions | 15 minutes |
| `suggestions:friends:{userId}` | AI-generated friend suggestions | 30 minutes |

### 6.3. JWT Token Blacklist

When a user logs out or changes their password, their JWT's unique ID (`jti` claim) is stored in Redis as `token:blacklist:{jti}` with a TTL matching the token's remaining validity time. The JWT filter checks this set on every incoming request to reject invalidated tokens without a database roundtrip.

### 6.4. Rate Limiting

Redis sliding-window counters are used to prevent brute-force attacks on the login endpoint:

- Key: `ratelimit:{ip}:login:{windowTimestamp}` — incremented per attempt; requests are blocked above the configured threshold (e.g., 10 attempts per minute).

---

## 7. Messaging & Events — Kafka

### 7.1. Version & Setup

| Item | Choice |
|---|---|
| Kafka Version | Apache Kafka 3.x |
| Spring Integration | `spring-kafka` |
| Local Deployment | Single broker via Docker Compose |
| Message Format | JSON (plain, no schema registry required for this scope) |

### 7.2. Topics & Their Producers/Consumers

| Topic | Producer | Consumer | Description |
|---|---|---|---|
| `user.registered` | AuthService | NotificationService, EmailService | Triggers welcome email |
| `activity.created` | ActivityService | NotificationService, SuggestionService | Notifies nearby users; refreshes AI cache |
| `participation.requested` | ParticipationService | NotificationService | Notifies host of a join request |
| `participation.approved` | ParticipationService | NotificationService, ChatService | Notifies participant; adds them to Trip Hub |
| `participation.declined` | ParticipationService | NotificationService | Notifies participant of rejection |
| `attendance.confirmed` | AttendanceService | XPService, RatingService | Triggers XP calculation; opens rating window |
| `rating.submitted` | RatingService | XPService, NotificationService | Updates aggregate rating and grants XP |
| `notification.created` | NotificationService | WebSocketDispatcher | Pushes real-time notification to connected user |
| `user.leveled_up` | XPService | NotificationService | Sends level-up celebration notification |

### 7.3. Why Kafka Here

Kafka decouples the services so that, for example, `ParticipationService` does not directly call `NotificationService`. It publishes an event and returns immediately. If the notification consumer is temporarily down, messages are retained in the topic and processed when it recovers. This makes the system more resilient and each service independently testable.

---

## 8. Media Storage — Cloudinary / Local

### 8.1. Strategy

Use **Cloudinary** for production and a **local file system** for development, controlled by the active Spring profile.

| Environment | Storage Backend |
|---|---|
| `dev` profile | Local file system (`/uploads/`), served by Spring's static resource handler |
| `prod` profile | Cloudinary CDN |

### 8.2. Cloudinary Integration

| Item | Detail |
|---|---|
| SDK | `cloudinary-http44` (official Java SDK) |
| Upload flow | Client uploads file to Spring Boot -> Spring Boot uploads to Cloudinary -> Cloudinary URL stored in PostgreSQL |
| Profile pictures | Auto-cropped to 300x300 px via Cloudinary transformation parameters on upload |
| Gallery & Hub media | Original stored; thumbnails auto-generated by Cloudinary |
| Folder structure | `spawnta/profiles/{userId}`, `spawnta/activities/{activityId}`, `spawnta/hub/{activityId}` |

### 8.3. Configuration (application.properties)

```properties
# Cloudinary (prod profile)
cloudinary.cloud-name=${CLOUDINARY_CLOUD_NAME}
cloudinary.api-key=${CLOUDINARY_API_KEY}
cloudinary.api-secret=${CLOUDINARY_API_SECRET}

# Local fallback (dev profile)
storage.local.upload-path=/uploads
storage.local.base-url=http://localhost:8080/files
```

---

## 9. Real-Time Communication — WebSocket

### 9.1. Protocol Stack

| Layer | Technology |
|---|---|
| Transport | SockJS (WebSocket with HTTP long-polling fallback for environments that block WS) |
| Messaging Protocol | STOMP (Simple Text Oriented Messaging Protocol) |
| Spring Integration | `spring-boot-starter-websocket` + `@EnableWebSocketMessageBroker` |
| Broker | Spring's in-memory SimpleBroker (development); upgradeable to RabbitMQ STOMP relay for multi-instance production |

### 9.2. STOMP Destinations

| Destination | Direction | Purpose |
|---|---|---|
| `/app/chat.group/{activityId}` | Client -> Server | Send a message to a trip's group chat |
| `/topic/group/{activityId}` | Server -> Client (broadcast) | Deliver group chat messages to all participants |
| `/app/chat.direct/{receiverId}` | Client -> Server | Send a direct message to a friend |
| `/user/queue/messages` | Server -> Client (unicast) | Deliver a direct message to the specific recipient only |
| `/user/queue/notifications` | Server -> Client (unicast) | Push a real-time notification to a specific user |

### 9.3. Authentication over WebSocket

On the WebSocket CONNECT frame, the client includes the JWT. A Spring `ChannelInterceptor` validates the token before the STOMP session is established. Unauthenticated or token-expired connections are immediately rejected.

---

## 10. Authentication & Security

### 10.1. JWT Flow

1. User logs in via `POST /api/auth/login`.
2. Spring Security validates the credentials against the database.
3. Server issues a signed **access token** (1-hour expiry) and a **refresh token** (7-day expiry, stored in Redis).
4. Access token is returned in the response body; refresh token is set as an `HttpOnly` + `Secure` cookie.
5. Angular's `JwtInterceptor` attaches `Authorization: Bearer {token}` to every outgoing HTTP request.
6. On a 401 response, Angular calls `POST /api/auth/refresh` using the cookie and receives a new access token transparently.

### 10.2. OAuth2 (Google / Facebook)

Spring Security OAuth2 Client handles the authorization code flow. After a successful OAuth login, the backend looks up or creates the user record by email and issues the same JWT format used for email/password login.

### 10.3. Role-Based Access Control

| Role | Who Has It | Access Level |
|---|---|---|
| `ROLE_USER` | All registered users | Standard app features |
| `ROLE_PREMIUM` | Active paying subscribers | Unlimited activities, premium UI perks |
| `ROLE_ADMIN` | Back-office staff accounts | All `/api/admin/**` endpoints |

Roles are embedded in JWT claims and enforced server-side via `@PreAuthorize` annotations and `.hasRole()` rules in the Spring Security filter chain.

### 10.4. Additional Security Measures

- HTTPS enforced in production (via Nginx + Let's Encrypt).
- CORS configured to allow only the known frontend origins.
- Rate limiting on `/api/auth/login` via Redis counters.
- All DTOs validated with `@Valid` before reaching the service layer.
- Parameterized JPA queries prevent SQL injection by design.
- Angular's default HTML sanitization prevents XSS in rendered content.

---

## 11. AI Integration

### 11.1. Approach

For this JEE project, a **rule-based collaborative filtering** approach is used. The logic lives entirely in a `RecommendationService` bean within the Spring Boot application — no separate ML service is needed.

### 11.2. Activity Recommendations Algorithm

The service fetches candidate activities (within a configurable radius, future-dated, with open spots) and scores each one:

| Factor | Weight |
|---|---|
| Matching interest tags between user and activity | 40% |
| Host's average rating | 20% |
| Distance from user's location (closer = higher) | 20% |
| Time until activity starts (sooner = slightly higher) | 10% |
| Friends already participating | 10% |

Top-N results are returned and cached in Redis for 15 minutes.

### 11.3. Friend Recommendations Algorithm

| Factor | Weight |
|---|---|
| Number of shared interests | 40% |
| Number of mutual friends | 30% |
| Common past activity participation | 20% |
| Same city | 10% |

### 11.4. Future Enhancement Path

The `RecommendationService` interface can later be reimplemented as a call to a Python microservice (scikit-learn cosine similarity on user-interest vectors) exposed as a REST endpoint, without changing any other part of the application.

---

## 12. Maps & Geolocation

### 12.1. Frontend Map

| Item | Choice |
|---|---|
| Library | Leaflet.js via `ngx-leaflet` (open-source, no API key required) |
| Tile Provider | OpenStreetMap tiles (free) — optionally Mapbox for better visual quality |
| Geocoding (address -> coords) | Nominatim API (OpenStreetMap, free) |
| Reverse Geocoding (coords -> name) | Nominatim API — used when a user pins a location manually |

### 12.2. Backend Geo Queries

The **PostGIS** extension on PostgreSQL enables efficient spatial queries. The `nearby` endpoint uses a native spatial query:

```sql
SELECT * FROM activities
WHERE ST_DWithin(
  ST_MakePoint(meeting_lng, meeting_lat)::geography,
  ST_MakePoint(:lng, :lat)::geography,
  :radiusInMeters
)
AND status = 'ACTIVE'
AND start_datetime > NOW();
```

This is orders of magnitude faster than computing distances in Java over a full table scan.

---

## 13. DevOps & Infrastructure

### 13.1. Containerization — Docker

Every component runs in a Docker container. A single `docker-compose up` command starts the full local development stack.

**`docker-compose.yml` services:**

| Service | Image | Exposed Port |
|---|---|---|
| `postgres` | `postgres:15-alpine` + PostGIS | 5432 |
| `redis` | `redis:7-alpine` | 6379 |
| `zookeeper` | `confluentinc/cp-zookeeper` | 2181 |
| `kafka` | `confluentinc/cp-kafka` | 9092 |
| `backend` | Built from `./backend/Dockerfile` | 8080 |
| `frontend` | Built from `./frontend/Dockerfile` (Nginx) | 4200 |
| `admin` | Built from `./admin/Dockerfile` (Nginx) | 4300 |

### 13.2. CI/CD Pipeline — GitHub Actions

```
Push to feature branch
        |
        v
[ CI: Build & Test ]  <-- mvn test + ng test
        |
        | (on merge to main)
        v
[ Build Docker Images ]  <-- docker build + push to registry
        |
        v
[ Deploy to Server ]  <-- SSH + docker-compose pull + up
```

**Pipeline stages:**

| Stage | Tools | What It Does |
|---|---|---|
| Lint | Checkstyle (Java), ESLint (Angular) | Code style validation |
| Unit Tests | JUnit 5 + Mockito, Jasmine + Karma | Unit test suites |
| Integration Tests | Spring Boot Test + Testcontainers | Spins up real Postgres + Redis containers in CI |
| Build | `mvn package -DskipTests`, `ng build --configuration production` | Produces JAR + Angular dist bundle |
| Docker Build | `docker build` | Builds images for backend and both frontends |
| Push | GitHub Container Registry (ghcr.io) | Stores versioned and tagged images |
| Deploy | `docker-compose` over SSH | Pulls new images, restarts services |

### 13.3. Reverse Proxy — Nginx

Nginx is the single entry point in production, handling SSL termination and routing:

```nginx
# API traffic -> Spring Boot
location /api/ {
    proxy_pass http://backend:8080;
}

# WebSocket traffic
location /ws/ {
    proxy_pass http://backend:8080;
    proxy_http_version 1.1;
    proxy_set_header Upgrade $http_upgrade;
    proxy_set_header Connection "Upgrade";
}

# User-facing Angular app
location / {
    root /usr/share/nginx/html/app;
    try_files $uri $uri/ /index.html;
}

# Admin back-office (subdomain or sub-path)
location /admin/ {
    root /usr/share/nginx/html/admin;
    try_files $uri $uri/ /admin/index.html;
}
```

SSL/TLS certificates are managed automatically by **Let's Encrypt** (Certbot).

### 13.4. Monitoring & Logging

| Tool | Purpose |
|---|---|
| Spring Boot Actuator | Exposes `/actuator/health`, `/actuator/metrics`, `/actuator/info` |
| Spring Boot Admin | Web UI over Actuator — health dashboard, live log streaming |
| Logback | Structured JSON log output from Spring Boot (log level configurable per package) |
| Prometheus + Grafana | Optional bonus: metrics scraping and visual dashboards |

---

## 14. Full Technology Stack Summary

| Layer | Technology | Version | Notes |
|---|---|---|---|
| **Backend Framework** | Spring Boot | 3.x | Java 21, Maven |
| **Frontend — User App** | Angular | 17+ | TypeScript, standalone components, Angular Material |
| **Frontend — Admin** | Angular + CoreUI | 17+ | Pre-built admin shell; only data tables to implement |
| **Infrastructure Monitor** | Spring Boot Admin | 3.x | Plugs into Actuator; zero-config monitoring |
| **Primary Database** | PostgreSQL | 15+ | PostGIS extension for spatial queries |
| **ORM & Migrations** | Hibernate / Spring Data JPA + Flyway | via Spring Boot | |
| **Cache & Sessions** | Redis | 7.x | Feed cache, token blacklist, rate limiting |
| **Event Streaming** | Apache Kafka | 3.x | Decoupled notifications, XP events, attendance flow |
| **Real-Time Messaging** | WebSocket — STOMP over SockJS | via Spring Boot | Group chat, DMs, live notifications |
| **Media Storage (prod)** | Cloudinary | — | CDN + auto image transformations |
| **Media Storage (dev)** | Local file system | — | Spring static resource handler |
| **Authentication** | JWT + Spring Security + OAuth2 | jjwt 0.12.x | Refresh token in HttpOnly cookie |
| **Maps** | Leaflet.js + OpenStreetMap + Nominatim | ngx-leaflet | No API key required |
| **Containerization** | Docker + Docker Compose | — | Full stack in one command |
| **CI/CD** | GitHub Actions | — | Lint → Test → Build → Docker → Deploy |
| **Reverse Proxy / SSL** | Nginx + Let's Encrypt | — | Single entry point, WebSocket upgrade |
| **API Documentation** | Springdoc OpenAPI (Swagger UI) | — | Auto-generated at `/swagger-ui.html` |
| **Monitoring** | Spring Boot Actuator + Spring Boot Admin | — | Health, logs, metrics |

---

*End of Technical Specifications — Spawnta*
