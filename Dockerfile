# ==============================================================================
# Multi-Stage Production Dockerfile for Emergency Alert System
# Target: Java 21 LTS + Eclipse Temurin Minimal Runtime
# ==============================================================================

# Stage 1: Build & Package
FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

# Copy dependency descriptor first for layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build production jar (skipping tests for container image speed)
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Minimal Distroless / Hardened Runtime Image
FROM eclipse-temurin:21-jre-alpine AS runner

LABEL maintainer="Emergency Alert Operations <ops@emergency-alert.internal>"
LABEL description="AWS-Hosted Emergency Alert System - Java Spring Boot & WebSocket"

# Create unprivileged system group and user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

WORKDIR /app

# Copy executable jar from builder stage
COPY --from=builder /build/target/emergency-alert-system-*.jar app.jar

# Chown to unprivileged user
RUN chown -R appuser:appgroup /app

USER appuser

# Expose standard Spring Boot container port
EXPOSE 8080

# Environment Defaults (Override via AWS ECS / Elastic Beanstalk / Docker Compose)
ENV SPRING_PROFILES_ACTIVE=prod \
    SERVER_PORT=8080 \
    DB_HOST=localhost \
    DB_PORT=3306 \
    DB_NAME=emergency_db \
    DB_USERNAME=emergency_user \
    DB_PASSWORD=emergency_secret \
    JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:+ExitOnOutOfMemoryError"

# Container Healthcheck
HEALTHCHECK --interval=30s --timeout=5s --start-period=40s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/api/public/stats || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
