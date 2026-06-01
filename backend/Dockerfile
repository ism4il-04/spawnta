# ── Stage 1: build ──────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
 
WORKDIR /app
 
# Copy POM first — Docker caches dependency layer separately
COPY pom.xml .
RUN mvn dependency:go-offline -B
 
# Copy source and build
COPY src ./src
RUN mvn package -DskipTests -B
 
# ── Stage 2: runtime ────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime
 
# Create non-root user for security
RUN addgroup -S spawnta && adduser -S spawnta -G spawnta
USER spawnta
 
WORKDIR /app
 
# Copy only the JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar
 
# Expose Spring Boot default port
EXPOSE 8080
 
# Profile comes from SPRING_PROFILES_ACTIVE (docker-compose: dev or prod)
ENTRYPOINT ["java", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
