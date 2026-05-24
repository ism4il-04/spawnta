# ── Stage 1: build ──────────────────────────────────────────────
FROM node:20-alpine AS builder
 
WORKDIR /app
 
# Copy package files first for dependency caching
COPY package*.json .
RUN npm install --legacy-peer-deps
 
# Copy source and build for production
COPY . .
RUN npm run build -- --configuration production
 
# ── Stage 2: serve ──────────────────────────────────────────────
FROM nginx:1.25-alpine AS runtime
 
# Remove default nginx content
RUN rm -rf /usr/share/nginx/html/*
 
# Copy Angular build output
COPY --from=builder /app/dist/frontend/browser /usr/share/nginx/html
 
# Copy SPA nginx config (handles Angular routing)
COPY nginx.conf /etc/nginx/conf.d/default.conf
 
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
