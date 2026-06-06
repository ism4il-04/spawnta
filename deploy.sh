#!/usr/bin/env bash
# =============================================================================
#  SPAWNTA - Kubernetes Deployment Script
#  Deploy all services (infra + backend + frontend + admin) to a K8s cluster
#  and run smoke tests on backend & frontend.
# =============================================================================
set -euo pipefail

# ─── Colour helpers ──────────────────────────────────────────────────────────
RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'
BLUE='\033[0;34m'; CYAN='\033[0;36m'; BOLD='\033[1m'; NC='\033[0m'

log()     { echo -e "${BLUE}[INFO]${NC}  $*"; }
success() { echo -e "${GREEN}[OK]${NC}    $*"; }
warn()    { echo -e "${YELLOW}[WARN]${NC}  $*"; }
error()   { echo -e "${RED}[ERROR]${NC} $*"; exit 1; }
section() { echo -e "\n${BOLD}${CYAN}══════════════════════════════════════════${NC}"; \
            echo -e "${BOLD}${CYAN}  $*${NC}"; \
            echo -e "${BOLD}${CYAN}══════════════════════════════════════════${NC}"; }

# ─── Configuration ───────────────────────────────────────────────────────────
NAMESPACE="${NAMESPACE:-spawnta}"
REGISTRY="${REGISTRY:-}"                      # e.g. "docker.io/myuser"  or "" for local
IMAGE_TAG="${IMAGE_TAG:-latest}"
KUBE_CONTEXT="${KUBE_CONTEXT:-}"              # leave empty to use current context
ENV_FILE="${ENV_FILE:-.env}"

BACKEND_IMAGE="spawnta-backend:${IMAGE_TAG}"
FRONTEND_IMAGE="spawnta-frontend:${IMAGE_TAG}"
ADMIN_IMAGE="spawnta-admin:${IMAGE_TAG}"

if [[ -n "$REGISTRY" ]]; then
  BACKEND_IMAGE="${REGISTRY}/spawnta-backend:${IMAGE_TAG}"
  FRONTEND_IMAGE="${REGISTRY}/spawnta-frontend:${IMAGE_TAG}"
  ADMIN_IMAGE="${REGISTRY}/spawnta-admin:${IMAGE_TAG}"
fi

BACKEND_PORT=8080
FRONTEND_PORT=80
ADMIN_PORT=80

# Health-check timeouts (seconds)
DEPLOY_TIMEOUT=300
HEALTH_RETRIES=30
HEALTH_SLEEP=10

# ─── Helper: check required tools ────────────────────────────────────────────
check_deps() {
  section "Checking dependencies"
  for cmd in kubectl docker curl; do
    if command -v "$cmd" &>/dev/null; then
      success "$cmd found"
    else
      error "$cmd is required but not installed."
    fi
  done
}

# ─── Load .env file ──────────────────────────────────────────────────────────
load_env() {
  section "Loading environment variables"
  if [[ ! -f "$ENV_FILE" ]]; then
    warn ".env file not found at $ENV_FILE — using system environment variables."
    return
  fi
  # Export every non-comment, non-empty line
  set -o allexport
  # shellcheck disable=SC1090
  while IFS='=' read -r key value; do
    # Skip comments and empty lines
    [[ "$key" =~ ^#.*$ ]] && continue
    [[ -z "$key" ]] && continue
    # Remove leading/trailing whitespace and quotes
    key=$(echo "$key" | xargs)
    value=$(echo "$value" | xargs | sed -e 's/^"//' -e 's/"$//' -e "s/^'//" -e "s/'$//")
    # Export the variable
    export "$key=$value"
  done < <(grep -v '^#' "$ENV_FILE" | grep -v '^\s*$' | sed 's/\r$//')
  set +o allexport
  success "Loaded $ENV_FILE"
}

# ─── Set kubectl context ─────────────────────────────────────────────────────
set_context() {
  section "Kubernetes context"
  if [[ -n "$KUBE_CONTEXT" ]]; then
    kubectl config use-context "$KUBE_CONTEXT"
    success "Using context: $KUBE_CONTEXT"
  else
    CURRENT_CTX=$(kubectl config current-context 2>/dev/null || echo "none")
    success "Using current context: $CURRENT_CTX"
  fi
  kubectl cluster-info --request-timeout=10s | head -2
}

# ─── Build Docker images ──────────────────────────────────────────────────────
build_images() {
  section "Building Docker images"

  log "Building Backend image → $BACKEND_IMAGE"
  docker build -t "$BACKEND_IMAGE" ./backend
  success "Backend image built"

  log "Building Frontend image → $FRONTEND_IMAGE"
  docker build -t "$FRONTEND_IMAGE" ./frontend
  success "Frontend image built"

  log "Building Admin image → $ADMIN_IMAGE"
  docker build -t "$ADMIN_IMAGE" ./admin
  success "Admin image built"
}

# ─── Push images (skip when REGISTRY is empty = local cluster / minikube) ────
push_images() {
  if [[ -z "$REGISTRY" ]]; then
    warn "REGISTRY not set — skipping push (assuming local cluster like minikube/kind)."

    # Load images into minikube/kind if applicable
    if command -v minikube &>/dev/null && minikube status &>/dev/null 2>&1; then
      log "Loading images into minikube..."
      minikube image load "$BACKEND_IMAGE"
      minikube image load "$FRONTEND_IMAGE"
      minikube image load "$ADMIN_IMAGE"
      success "Images loaded into minikube"
    elif command -v kind &>/dev/null; then
      KIND_CLUSTER=$(kind get clusters 2>/dev/null | head -1)
      if [[ -n "$KIND_CLUSTER" ]]; then
        log "Loading images into kind cluster: $KIND_CLUSTER"
        kind load docker-image "$BACKEND_IMAGE" --name "$KIND_CLUSTER"
        kind load docker-image "$FRONTEND_IMAGE" --name "$KIND_CLUSTER"
        kind load docker-image "$ADMIN_IMAGE" --name "$KIND_CLUSTER"
        success "Images loaded into kind"
      fi
    fi
    return
  fi

  section "Pushing Docker images to registry"
  docker push "$BACKEND_IMAGE"
  docker push "$FRONTEND_IMAGE"
  docker push "$ADMIN_IMAGE"
  success "All images pushed"
}

# ─── Create namespace & secrets ──────────────────────────────────────────────
setup_namespace() {
  section "Namespace & Secrets"

  kubectl create namespace "$NAMESPACE" --dry-run=client -o yaml | kubectl apply -f -
  success "Namespace '$NAMESPACE' ready"

  # Build kubectl secret from env vars
  kubectl -n "$NAMESPACE" create secret generic spawnta-secrets \
    --from-literal=POSTGRES_DB="${POSTGRES_DB:-spawnta}" \
    --from-literal=POSTGRES_USER="${POSTGRES_USER:-spawnta}" \
    --from-literal=POSTGRES_PASSWORD="${POSTGRES_PASSWORD:-changeme}" \
    --from-literal=REDIS_PASSWORD="${REDIS_PASSWORD:-changeme}" \
    --from-literal=JWT_SECRET="${JWT_SECRET:-spawnta_jwt_secret_change_me_very_long_min_32}" \
    --from-literal=JWT_ACCESS_EXPIRY="${JWT_ACCESS_EXPIRY:-3600000}" \
    --from-literal=CLOUDINARY_CLOUD_NAME="${CLOUDINARY_CLOUD_NAME:-}" \
    --from-literal=CLOUDINARY_API_KEY="${CLOUDINARY_API_KEY:-}" \
    --from-literal=CLOUDINARY_API_SECRET="${CLOUDINARY_API_SECRET:-}" \
    --from-literal=MAIL_HOST="${MAIL_HOST:-smtp.gmail.com}" \
    --from-literal=MAIL_PORT="${MAIL_PORT:-587}" \
    --from-literal=MAIL_USERNAME="${MAIL_USERNAME:-}" \
    --from-literal=MAIL_PASSWORD="${MAIL_PASSWORD:-}" \
    --from-literal=STRIPE_PUBLISHABLE_KEY="${STRIPE_PUBLISHABLE_KEY:-}" \
    --from-literal=STRIPE_SECRET_KEY="${STRIPE_SECRET_KEY:-}" \
    --from-literal=STRIPE_WEBHOOK_SECRET="${STRIPE_WEBHOOK_SECRET:-}" \
    --from-literal=GOOGLE_CLIENT_ID="${GOOGLE_CLIENT_ID:-}" \
    --from-literal=GOOGLE_CLIENT_SECRET="${GOOGLE_CLIENT_SECRET:-}" \
    --dry-run=client -o yaml | kubectl apply -f -

  success "Secret 'spawnta-secrets' applied"
}

# ─── Apply Kubernetes manifests ───────────────────────────────────────────────
apply_manifests() {
  section "Applying Kubernetes manifests"

  # ── StorageClass PVC for Postgres ────────────────────────────────────────
  kubectl -n "$NAMESPACE" apply -f - <<EOF
---
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: postgres-pvc
  namespace: $NAMESPACE
spec:
  accessModes: [ReadWriteOnce]
  resources:
    requests:
      storage: 5Gi
---
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: redis-pvc
  namespace: $NAMESPACE
spec:
  accessModes: [ReadWriteOnce]
  resources:
    requests:
      storage: 1Gi
---
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: kafka-pvc
  namespace: $NAMESPACE
spec:
  accessModes: [ReadWriteOnce]
  resources:
    requests:
      storage: 3Gi
---
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: zookeeper-pvc
  namespace: $NAMESPACE
spec:
  accessModes: [ReadWriteOnce]
  resources:
    requests:
      storage: 1Gi
---
apiVersion: v1
kind: PersistentVolumeClaim
metadata:
  name: uploads-pvc
  namespace: $NAMESPACE
spec:
  accessModes: [ReadWriteOnce]
  resources:
    requests:
      storage: 2Gi
EOF
  success "PersistentVolumeClaims created"

  # ── PostgreSQL (PostGIS) ─────────────────────────────────────────────────
  kubectl -n "$NAMESPACE" apply -f - <<EOF
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: postgres
  namespace: $NAMESPACE
  labels:
    app: postgres
spec:
  replicas: 1
  selector:
    matchLabels:
      app: postgres
  template:
    metadata:
      labels:
        app: postgres
    spec:
      containers:
      - name: postgres
        image: postgis/postgis:15-3.3-alpine
        ports:
        - containerPort: 5432
        env:
        - name: POSTGRES_DB
          valueFrom:
            secretKeyRef:
              name: spawnta-secrets
              key: POSTGRES_DB
        - name: POSTGRES_USER
          valueFrom:
            secretKeyRef:
              name: spawnta-secrets
              key: POSTGRES_USER
        - name: POSTGRES_PASSWORD
          valueFrom:
            secretKeyRef:
              name: spawnta-secrets
              key: POSTGRES_PASSWORD
        volumeMounts:
        - name: postgres-data
          mountPath: /var/lib/postgresql/data
        readinessProbe:
          exec:
            command: [sh, -c, "pg_isready -U \$(POSTGRES_USER) -d \$(POSTGRES_DB)"]
          initialDelaySeconds: 10
          periodSeconds: 5
          failureThreshold: 10
        resources:
          requests:
            memory: "256Mi"
            cpu: "250m"
          limits:
            memory: "512Mi"
            cpu: "500m"
      volumes:
      - name: postgres-data
        persistentVolumeClaim:
          claimName: postgres-pvc
---
apiVersion: v1
kind: Service
metadata:
  name: postgres
  namespace: $NAMESPACE
spec:
  selector:
    app: postgres
  ports:
  - port: 5432
    targetPort: 5432
  clusterIP: None
EOF
  success "PostgreSQL deployed"

  # ── Redis ────────────────────────────────────────────────────────────────
  kubectl -n "$NAMESPACE" apply -f - <<EOF
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: redis
  namespace: $NAMESPACE
  labels:
    app: redis
spec:
  replicas: 1
  selector:
    matchLabels:
      app: redis
  template:
    metadata:
      labels:
        app: redis
    spec:
      containers:
      - name: redis
        image: redis:7-alpine
        command: [sh, -c, "redis-server --requirepass \$(REDIS_PASSWORD)"]
        ports:
        - containerPort: 6379
        env:
        - name: REDIS_PASSWORD
          valueFrom:
            secretKeyRef:
              name: spawnta-secrets
              key: REDIS_PASSWORD
        volumeMounts:
        - name: redis-data
          mountPath: /data
        readinessProbe:
          exec:
            command: [sh, -c, "redis-cli -a \$(REDIS_PASSWORD) ping | grep PONG"]
          initialDelaySeconds: 5
          periodSeconds: 5
        resources:
          requests:
            memory: "128Mi"
            cpu: "100m"
          limits:
            memory: "256Mi"
            cpu: "200m"
      volumes:
      - name: redis-data
        persistentVolumeClaim:
          claimName: redis-pvc
---
apiVersion: v1
kind: Service
metadata:
  name: redis
  namespace: $NAMESPACE
spec:
  selector:
    app: redis
  ports:
  - port: 6379
    targetPort: 6379
  clusterIP: None
EOF
  success "Redis deployed"

  # ── Zookeeper ────────────────────────────────────────────────────────────
  kubectl -n "$NAMESPACE" apply -f - <<EOF
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: zookeeper
  namespace: $NAMESPACE
  labels:
    app: zookeeper
spec:
  replicas: 1
  selector:
    matchLabels:
      app: zookeeper
  template:
    metadata:
      labels:
        app: zookeeper
    spec:
      containers:
      - name: zookeeper
        image: confluentinc/cp-zookeeper:7.5.0
        ports:
        - containerPort: 2181
        env:
        - name: ZOOKEEPER_CLIENT_PORT
          value: "2181"
        - name: ZOOKEEPER_TICK_TIME
          value: "2000"
        volumeMounts:
        - name: zookeeper-data
          mountPath: /var/lib/zookeeper
        resources:
          requests:
            memory: "256Mi"
            cpu: "200m"
          limits:
            memory: "512Mi"
            cpu: "400m"
      volumes:
      - name: zookeeper-data
        persistentVolumeClaim:
          claimName: zookeeper-pvc
---
apiVersion: v1
kind: Service
metadata:
  name: zookeeper
  namespace: $NAMESPACE
spec:
  selector:
    app: zookeeper
  ports:
  - port: 2181
    targetPort: 2181
  clusterIP: None
EOF
  success "Zookeeper deployed"

  # ── Kafka ────────────────────────────────────────────────────────────────
  # We need the node IP for KAFKA_ADVERTISED_LISTENERS
  NODE_IP=$(kubectl get nodes -o jsonpath='{.items[0].status.addresses[?(@.type=="InternalIP")].address}' 2>/dev/null || echo "localhost")

  kubectl -n "$NAMESPACE" apply -f - <<EOF
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: kafka
  namespace: $NAMESPACE
  labels:
    app: kafka
spec:
  replicas: 1
  selector:
    matchLabels:
      app: kafka
  template:
    metadata:
      labels:
        app: kafka
    spec:
      containers:
      - name: kafka
        image: confluentinc/cp-kafka:7.5.0
        ports:
        - containerPort: 9092
        - containerPort: 29092
        env:
        - name: KAFKA_BROKER_ID
          value: "1"
        - name: KAFKA_ZOOKEEPER_CONNECT
          value: "zookeeper:2181"
        - name: KAFKA_ADVERTISED_LISTENERS
          value: "PLAINTEXT://${NODE_IP}:9092,PLAINTEXT_INTERNAL://kafka:29092"
        - name: KAFKA_LISTENER_SECURITY_PROTOCOL_MAP
          value: "PLAINTEXT:PLAINTEXT,PLAINTEXT_INTERNAL:PLAINTEXT"
        - name: KAFKA_INTER_BROKER_LISTENER_NAME
          value: "PLAINTEXT_INTERNAL"
        - name: KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR
          value: "1"
        - name: KAFKA_AUTO_CREATE_TOPICS_ENABLE
          value: "true"
        volumeMounts:
        - name: kafka-data
          mountPath: /var/lib/kafka/data
        resources:
          requests:
            memory: "512Mi"
            cpu: "300m"
          limits:
            memory: "1Gi"
            cpu: "600m"
      volumes:
      - name: kafka-data
        persistentVolumeClaim:
          claimName: kafka-pvc
---
apiVersion: v1
kind: Service
metadata:
  name: kafka
  namespace: $NAMESPACE
spec:
  selector:
    app: kafka
  ports:
  - name: external
    port: 9092
    targetPort: 9092
  - name: internal
    port: 29092
    targetPort: 29092
  clusterIP: None
EOF
  success "Kafka deployed"

  # ── Backend (Spring Boot) ────────────────────────────────────────────────
  kubectl -n "$NAMESPACE" apply -f - <<EOF
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: backend
  namespace: $NAMESPACE
  labels:
    app: backend
spec:
  replicas: 1
  selector:
    matchLabels:
      app: backend
  template:
    metadata:
      labels:
        app: backend
    spec:
      initContainers:
      - name: wait-for-postgres
        image: busybox:1.36
        command: ['sh', '-c',
          'until nc -z postgres 5432; do echo "waiting for postgres..."; sleep 3; done']
      - name: wait-for-redis
        image: busybox:1.36
        command: ['sh', '-c',
          'until nc -z redis 6379; do echo "waiting for redis..."; sleep 3; done']
      - name: wait-for-kafka
        image: busybox:1.36
        command: ['sh', '-c',
          'until nc -z kafka 29092; do echo "waiting for kafka..."; sleep 3; done']
      containers:
      - name: backend
        image: ${BACKEND_IMAGE}
        imagePullPolicy: IfNotPresent
        ports:
        - containerPort: ${BACKEND_PORT}
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "dev"
        - name: SPRING_FLYWAY_ENABLED
          value: "false"
        - name: DB_HOST
          value: "postgres"
        - name: SPRING_DATASOURCE_URL
          value: "jdbc:postgresql://postgres:5432/\$(POSTGRES_DB)"
        - name: SPRING_DATASOURCE_USERNAME
          valueFrom:
            secretKeyRef:
              name: spawnta-secrets
              key: POSTGRES_USER
        - name: SPRING_DATASOURCE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: spawnta-secrets
              key: POSTGRES_PASSWORD
        - name: POSTGRES_DB
          valueFrom:
            secretKeyRef:
              name: spawnta-secrets
              key: POSTGRES_DB
        - name: SPRING_DATA_REDIS_HOST
          value: "redis"
        - name: SPRING_DATA_REDIS_PASSWORD
          valueFrom:
            secretKeyRef:
              name: spawnta-secrets
              key: REDIS_PASSWORD
        - name: SPRING_KAFKA_BOOTSTRAP_SERVERS
          value: "kafka:29092"
        - name: SPRING_APP_JWT_SECRET
          valueFrom:
            secretKeyRef:
              name: spawnta-secrets
              key: JWT_SECRET
        - name: SPRING_APP_JWT_ACCESS_EXPIRY
          valueFrom:
            secretKeyRef:
              name: spawnta-secrets
              key: JWT_ACCESS_EXPIRY
        - name: CLOUDINARY_CLOUD_NAME
          valueFrom:
            secretKeyRef:
              name: spawnta-secrets
              key: CLOUDINARY_CLOUD_NAME
        - name: CLOUDINARY_API_KEY
          valueFrom:
            secretKeyRef:
              name: spawnta-secrets
              key: CLOUDINARY_API_KEY
        - name: CLOUDINARY_API_SECRET
          valueFrom:
            secretKeyRef:
              name: spawnta-secrets
              key: CLOUDINARY_API_SECRET
        - name: SPRING_MAIL_HOST
          valueFrom:
            secretKeyRef:
              name: spawnta-secrets
              key: MAIL_HOST
        - name: SPRING_MAIL_PORT
          valueFrom:
            secretKeyRef:
              name: spawnta-secrets
              key: MAIL_PORT
        - name: SPRING_MAIL_USERNAME
          valueFrom:
            secretKeyRef:
              name: spawnta-secrets
              key: MAIL_USERNAME
        - name: SPRING_MAIL_PASSWORD
          valueFrom:
            secretKeyRef:
              name: spawnta-secrets
              key: MAIL_PASSWORD
        - name: STRIPE_PUBLISHABLE_KEY
          valueFrom:
            secretKeyRef:
              name: spawnta-secrets
              key: STRIPE_PUBLISHABLE_KEY
        - name: STRIPE_SECRET_KEY
          valueFrom:
            secretKeyRef:
              name: spawnta-secrets
              key: STRIPE_SECRET_KEY
        - name: STRIPE_WEBHOOK_SECRET
          valueFrom:
            secretKeyRef:
              name: spawnta-secrets
              key: STRIPE_WEBHOOK_SECRET
        - name: GOOGLE_CLIENT_ID
          valueFrom:
            secretKeyRef:
              name: spawnta-secrets
              key: GOOGLE_CLIENT_ID
        - name: GOOGLE_CLIENT_SECRET
          valueFrom:
            secretKeyRef:
              name: spawnta-secrets
              key: GOOGLE_CLIENT_SECRET
        volumeMounts:
        - name: uploads
          mountPath: /app/uploads
        readinessProbe:
          httpGet:
            path: /actuator/health
            port: ${BACKEND_PORT}
          initialDelaySeconds: 40
          periodSeconds: 10
          failureThreshold: 15
        livenessProbe:
          httpGet:
            path: /actuator/health
            port: ${BACKEND_PORT}
          initialDelaySeconds: 60
          periodSeconds: 15
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"
      volumes:
      - name: uploads
        persistentVolumeClaim:
          claimName: uploads-pvc
---
apiVersion: v1
kind: Service
metadata:
  name: backend
  namespace: $NAMESPACE
spec:
  selector:
    app: backend
  ports:
  - port: ${BACKEND_PORT}
    targetPort: ${BACKEND_PORT}
  type: ClusterIP
EOF
  success "Backend deployed"

  # ── Frontend (Angular) ───────────────────────────────────────────────────
  kubectl -n "$NAMESPACE" apply -f - <<EOF
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: frontend
  namespace: $NAMESPACE
  labels:
    app: frontend
spec:
  replicas: 2
  selector:
    matchLabels:
      app: frontend
  template:
    metadata:
      labels:
        app: frontend
    spec:
      containers:
      - name: frontend
        image: ${FRONTEND_IMAGE}
        imagePullPolicy: IfNotPresent
        ports:
        - containerPort: ${FRONTEND_PORT}
        readinessProbe:
          httpGet:
            path: /
            port: ${FRONTEND_PORT}
          initialDelaySeconds: 10
          periodSeconds: 5
        resources:
          requests:
            memory: "64Mi"
            cpu: "50m"
          limits:
            memory: "128Mi"
            cpu: "100m"
---
apiVersion: v1
kind: Service
metadata:
  name: frontend
  namespace: $NAMESPACE
spec:
  selector:
    app: frontend
  ports:
  - port: 4200
    targetPort: ${FRONTEND_PORT}
  type: NodePort
EOF
  success "Frontend deployed"

  # ── Admin (Angular) ──────────────────────────────────────────────────────
  kubectl -n "$NAMESPACE" apply -f - <<EOF
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: admin
  namespace: $NAMESPACE
  labels:
    app: admin
spec:
  replicas: 1
  selector:
    matchLabels:
      app: admin
  template:
    metadata:
      labels:
        app: admin
    spec:
      containers:
      - name: admin
        image: ${ADMIN_IMAGE}
        imagePullPolicy: IfNotPresent
        ports:
        - containerPort: ${ADMIN_PORT}
        readinessProbe:
          httpGet:
            path: /
            port: ${ADMIN_PORT}
          initialDelaySeconds: 10
          periodSeconds: 5
        resources:
          requests:
            memory: "64Mi"
            cpu: "50m"
          limits:
            memory: "128Mi"
            cpu: "100m"
---
apiVersion: v1
kind: Service
metadata:
  name: admin
  namespace: $NAMESPACE
spec:
  selector:
    app: admin
  ports:
  - port: 4300
    targetPort: ${ADMIN_PORT}
  type: NodePort
EOF
  success "Admin deployed"
}

# ─── Wait for all deployments to become ready ─────────────────────────────────
wait_for_deployments() {
  section "Waiting for deployments to be ready (timeout: ${DEPLOY_TIMEOUT}s)"

  for deploy in postgres redis zookeeper kafka backend frontend admin; do
    log "Waiting for deployment/$deploy ..."
    if kubectl -n "$NAMESPACE" rollout status deployment/"$deploy" \
        --timeout="${DEPLOY_TIMEOUT}s"; then
      success "deployment/$deploy is ready"
    else
      warn "deployment/$deploy may not be fully ready — check: kubectl -n $NAMESPACE get pods"
    fi
  done
}

# ─── Resolve service URLs ─────────────────────────────────────────────────────
get_service_url() {
  local svc=$1 port=$2
  local node_ip node_port

  node_ip=$(kubectl get nodes -o jsonpath='{.items[0].status.addresses[?(@.type=="InternalIP")].address}' 2>/dev/null || echo "localhost")
  node_port=$(kubectl -n "$NAMESPACE" get svc "$svc" -o jsonpath='{.spec.ports[0].nodePort}' 2>/dev/null || echo "")

  if [[ -n "$node_port" ]]; then
    echo "http://${node_ip}:${node_port}"
  else
    # fallback: port-forward mode
    echo "http://localhost:${port}"
  fi
}

# ─── Smoke tests ──────────────────────────────────────────────────────────────
run_smoke_tests() {
  section "Running Smoke Tests"

  # ── Resolve URLs ────────────────────────────────────────────────────────
  # Backend: use port-forward in background
  log "Setting up port-forward for backend on localhost:18080 ..."
  kubectl -n "$NAMESPACE" port-forward svc/backend 18080:"${BACKEND_PORT}" &>/dev/null &
  PF_BACKEND_PID=$!
  sleep 3

  BACKEND_URL="http://localhost:18080"
  FRONTEND_URL=$(get_service_url frontend 4200)
  ADMIN_URL=$(get_service_url admin 4300)

  TESTS_PASSED=0
  TESTS_FAILED=0

  # ── Helper test function ─────────────────────────────────────────────────
  http_test() {
    local name="$1" url="$2" expected_code="${3:-200}" body_contains="${4:-}"
    local actual_code body
    actual_code=$(curl -s -o /tmp/spawnta_test_body -w "%{http_code}" \
      --max-time 10 --connect-timeout 5 "$url" 2>/dev/null || echo "000")
    body=$(cat /tmp/spawnta_test_body 2>/dev/null || echo "")

    if [[ "$actual_code" == "$expected_code" ]]; then
      if [[ -n "$body_contains" && ! "$body" == *"$body_contains"* ]]; then
        echo -e "  ${RED}✗ FAIL${NC}  $name → HTTP $actual_code but body missing '$body_contains'"
        ((TESTS_FAILED++))
      else
        echo -e "  ${GREEN}✓ PASS${NC}  $name → HTTP $actual_code"
        ((TESTS_PASSED++))
      fi
    else
      echo -e "  ${RED}✗ FAIL${NC}  $name → Expected HTTP $expected_code, got $actual_code"
      ((TESTS_FAILED++))
    fi
  }

  echo ""
  log "── Backend API Tests (${BACKEND_URL}) ──"

  # 1. Actuator health
  http_test "Backend: /actuator/health" \
    "${BACKEND_URL}/actuator/health" "200" "UP"

  # 2. Actuator info
  http_test "Backend: /actuator/info" \
    "${BACKEND_URL}/actuator/info" "200"

  # 3. Subscription plans (public endpoint)
  http_test "Backend: GET /api/subscription/plans" \
    "${BACKEND_URL}/api/subscription/plans" "200"

  # 4. Auth endpoint responds (even if 400/401, it exists)
  http_test "Backend: POST /api/auth/login (exists)" \
    "${BACKEND_URL}/api/auth/login" "400"

  # 5. Signup endpoint exists
  http_test "Backend: POST /api/auth/signup (exists)" \
    "${BACKEND_URL}/api/auth/signup" "400"

  # 6. Protected endpoint returns 401
  http_test "Backend: GET /api/subscription/current (requires auth)" \
    "${BACKEND_URL}/api/subscription/current" "401"

  # 7. Webhook endpoint reachable
  http_test "Backend: POST /api/subscription/webhook (exists)" \
    "${BACKEND_URL}/api/subscription/webhook" "400"

  # 8. Admin endpoint protected
  http_test "Backend: GET /api/admin/analytics (requires admin)" \
    "${BACKEND_URL}/api/admin/analytics" "401"

  # 9. WebSocket endpoint info (HTTP upgrade returns 400 without proper headers)
  http_test "Backend: WebSocket /ws (endpoint exists)" \
    "${BACKEND_URL}/ws" "400"

  echo ""
  log "── Frontend UI Tests (${FRONTEND_URL}) ──"

  # 10. Frontend home page
  http_test "Frontend: / (Angular SPA)" \
    "${FRONTEND_URL}" "200" "Spawnta"

  # 11. Angular routing (returns 200 for all routes - nginx SPA config)
  http_test "Frontend: /login route" \
    "${FRONTEND_URL}/login" "200"

  http_test "Frontend: /map route" \
    "${FRONTEND_URL}/map" "200"

  http_test "Frontend: /subscription route" \
    "${FRONTEND_URL}/subscription" "200"

  echo ""
  log "── Admin UI Tests (${ADMIN_URL}) ──"

  # 14. Admin home page
  http_test "Admin: / (Angular SPA)" \
    "${ADMIN_URL}" "200"

  # ── Cleanup port-forward ─────────────────────────────────────────────────
  kill "$PF_BACKEND_PID" 2>/dev/null || true

  # ── Summary ──────────────────────────────────────────────────────────────
  echo ""
  echo -e "${BOLD}════════════════════════════════════${NC}"
  echo -e "${BOLD}  Test Results${NC}"
  echo -e "${BOLD}════════════════════════════════════${NC}"
  echo -e "  ${GREEN}PASSED${NC}: $TESTS_PASSED"
  echo -e "  ${RED}FAILED${NC}: $TESTS_FAILED"
  echo -e "${BOLD}════════════════════════════════════${NC}"

  if [[ "$TESTS_FAILED" -gt 0 ]]; then
    warn "Some tests failed. Check service logs:"
    echo "  kubectl -n $NAMESPACE logs deployment/backend --tail=50"
    echo "  kubectl -n $NAMESPACE logs deployment/frontend --tail=20"
    echo "  kubectl -n $NAMESPACE get pods"
  else
    success "All smoke tests passed! 🎉"
  fi
}

# ─── Print access info ────────────────────────────────────────────────────────
print_access_info() {
  section "Access Information"

  FRONTEND_URL=$(get_service_url frontend 4200)
  ADMIN_URL=$(get_service_url admin 4300)
  NODE_IP=$(kubectl get nodes -o jsonpath='{.items[0].status.addresses[?(@.type=="InternalIP")].address}' 2>/dev/null || echo "localhost")

  echo -e "  ${GREEN}Frontend${NC}  : $FRONTEND_URL"
  echo -e "  ${GREEN}Admin${NC}     : $ADMIN_URL"
  echo -e "  ${GREEN}Backend${NC}   : kubectl -n $NAMESPACE port-forward svc/backend 8080:8080"
  echo ""
  echo -e "  ${CYAN}Useful commands:${NC}"
  echo "  kubectl -n $NAMESPACE get pods"
  echo "  kubectl -n $NAMESPACE get svc"
  echo "  kubectl -n $NAMESPACE logs deployment/backend -f"
  echo "  kubectl -n $NAMESPACE logs deployment/frontend -f"
  echo ""

  # Port-forward hints for local clusters
  echo -e "  ${YELLOW}If using minikube / kind:${NC}"
  echo "  minikube service frontend -n $NAMESPACE --url"
  echo "  minikube service admin    -n $NAMESPACE --url"
  echo "  kubectl -n $NAMESPACE port-forward svc/backend 8080:8080 &"
}

# ─── Teardown (optional) ──────────────────────────────────────────────────────
teardown() {
  section "Teardown: deleting namespace '$NAMESPACE'"
  kubectl delete namespace "$NAMESPACE" --ignore-not-found
  success "Namespace '$NAMESPACE' deleted"
}

# ─── Rollback ─────────────────────────────────────────────────────────────────
rollback() {
  section "Rolling back all deployments"
  for deploy in backend frontend admin; do
    kubectl -n "$NAMESPACE" rollout undo deployment/"$deploy" 2>/dev/null && \
      success "Rolled back $deploy" || warn "$deploy has no previous revision"
  done
}

# ─── Entry point ─────────────────────────────────────────────────────────────
usage() {
  cat <<HELP
Usage: $0 [COMMAND]

Commands:
  deploy    (default) Build images, push, apply K8s manifests, wait, test
  build     Build Docker images only
  push      Push images to registry only
  apply     Apply K8s manifests only (no build/push)
  test      Run smoke tests only
  rollback  Undo last rollout for backend/frontend/admin
  teardown  Delete namespace and all resources
  status    Show pod/service status
  help      Show this message

Environment variables:
  NAMESPACE     K8s namespace  (default: spawnta)
  REGISTRY      Docker registry prefix  (default: empty = local)
  IMAGE_TAG     Image tag  (default: latest)
  KUBE_CONTEXT  kubectl context to use  (default: current)
  ENV_FILE      Path to .env file  (default: .env)

Examples:
  ./deploy.sh
  REGISTRY=docker.io/myuser IMAGE_TAG=v1.2 ./deploy.sh deploy
  ./deploy.sh test
  ./deploy.sh teardown
HELP
}

CMD="${1:-deploy}"

case "$CMD" in
  deploy)
    check_deps
    load_env
    set_context
    build_images
    push_images
    setup_namespace
    apply_manifests
    wait_for_deployments
    run_smoke_tests
    print_access_info
    ;;
  build)
    build_images
    ;;
  push)
    load_env
    push_images
    ;;
  apply)
    load_env
    set_context
    setup_namespace
    apply_manifests
    wait_for_deployments
    ;;
  test)
    load_env
    set_context
    run_smoke_tests
    ;;
  rollback)
    set_context
    rollback
    ;;
  teardown)
    set_context
    teardown
    ;;
  status)
    set_context
    echo -e "\n${BOLD}Pods:${NC}"
    kubectl -n "$NAMESPACE" get pods -o wide
    echo -e "\n${BOLD}Services:${NC}"
    kubectl -n "$NAMESPACE" get svc
    echo -e "\n${BOLD}PVCs:${NC}"
    kubectl -n "$NAMESPACE" get pvc
    ;;
  help|--help|-h)
    usage
    ;;
  *)
    error "Unknown command: $CMD — run '$0 help' for usage"
    ;;
esac
