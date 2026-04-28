# 🏙️ Spawnta

> Real-estate platform built with **Spring Boot 4** + **Angular 21** — containerised with Docker & deployed via GitHub Actions.

---

## 📁 Project Structure

```
spawnta/
├── backend/          # Spring Boot 4 (Java 21) — REST API, WebSocket, Kafka
├── frontend/         # Angular 21 — user-facing SPA
├── admin/            # Angular 21 — back-office dashboard
├── devops/
│   └── nginx/        # Reverse-proxy (SSL termination, routing)
├── .github/workflows/
│   ├── ci.yml        # PR / develop → lint, test, build
│   └── deploy.yml    # main → Docker Hub → VPS
├── docker-compose.yml       # Development stack
├── docker-compose.prod.yml  # Production overrides
└── .env.example             # Environment template
```

## 🛠️ Tech Stack

| Layer         | Technology                                     |
| ------------- | ---------------------------------------------- |
| Backend       | Spring Boot 4, Spring Security, JPA, Flyway    |
| Frontend      | Angular 21, Angular Material, Leaflet, Chart.js |
| Admin         | Angular 21                                     |
| Database      | PostgreSQL 15 + PostGIS                        |
| Cache         | Redis 7                                        |
| Messaging     | Apache Kafka (Confluent 7.5)                   |
| Auth          | JWT + OAuth2 (Google)                           |
| Storage       | Cloudinary                                     |
| Reverse Proxy | Nginx + Let's Encrypt (Certbot)                |
| CI/CD         | GitHub Actions → Docker Hub → VPS deploy       |
| Monitoring    | Spring Boot Admin, Actuator, Logstash JSON     |

## 🚀 Quick Start (Development)

### Prerequisites

- **Docker** & **Docker Compose** v2+
- **Java 21** (for local backend dev)
- **Node 20** & **npm** (for local frontend dev)

### 1. Clone & configure

```bash
git clone https://github.com/<your-org>/spawnta.git
cd spawnta
cp .env.example .env    # ← fill in your secrets
```

### 2. Start everything

```bash
docker compose up -d
```

| Service   | URL                          |
| --------- | ---------------------------- |
| Frontend  | http://localhost:4200        |
| Admin     | http://localhost:4300        |
| Backend   | http://localhost:8080        |
| Swagger   | http://localhost:8080/swagger-ui.html |
| PostgreSQL| localhost:5432               |
| Redis     | localhost:6379               |
| Kafka     | localhost:9092               |

### 3. Local development (without Docker)

```bash
# Backend
cd backend
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev

# Frontend
cd frontend
npm install && npm start    # → http://localhost:4200

# Admin
cd admin
npm install && npm start    # → http://localhost:4300
```

## 🏗️ Production Deployment

Pushing to `main` triggers the full CI/CD pipeline:

1. **ci.yml** — runs tests & builds on PRs / develop
2. **deploy.yml** — builds Docker images → pushes to Docker Hub → deploys via SSH

```bash
# Manual production deploy
docker compose -f docker-compose.yml -f docker-compose.prod.yml --env-file .env up -d
```

## 📦 Docker Images

| Image                         | Description        |
| ----------------------------- | ------------------- |
| `i5m4il/spawnta-backend`      | Spring Boot API     |
| `i5m4il/spawnta-frontend`     | Angular user SPA    |
| `i5m4il/spawnta-admin`        | Angular admin panel |
| `i5m4il/spawnta-nginx`        | Reverse proxy       |

## 🔒 Environment Variables

See [`.env.example`](.env.example) for the full list. Key variables:

- `POSTGRES_*` — Database credentials
- `REDIS_PASSWORD` — Redis auth
- `JWT_SECRET` — Token signing key
- `CLOUDINARY_*` — Image upload service
- `MAIL_*` — SMTP credentials
- `GOOGLE_CLIENT_*` — OAuth2 provider

## 👥 Team

**ENSA — Génie Informatique S8 — Java Enterprise Edition**

## 📄 License

See [LICENSE.txt](LICENSE.txt)
