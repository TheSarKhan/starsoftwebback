# ============================================================
# StarSoft backend — production image
# Multi-stage: build with Maven, run on slim JRE.
# ============================================================

# --- Stage 1: build ---
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

# Cache dependencies separately from source for faster rebuilds
COPY pom.xml ./
RUN mvn -B -q dependency:go-offline

COPY src ./src
RUN mvn -B -q clean package -DskipTests \
    && mv target/*.jar target/app.jar

# --- Stage 2: runtime ---
FROM eclipse-temurin:17-jre-alpine AS runtime
WORKDIR /app

# su-exec: lightweight tool to drop privileges in entrypoint scripts
RUN apk add --no-cache su-exec \
    && addgroup -S khansoft && adduser -S khansoft -G khansoft \
    && mkdir -p /app/images /app/logs

COPY --from=build /build/target/app.jar /app/app.jar
COPY docker-entrypoint.sh /app/docker-entrypoint.sh
RUN chmod +x /app/docker-entrypoint.sh

EXPOSE 8080

# Honour container memory limits, sane GC defaults
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"

# Entrypoint runs as root, fixes volume permissions, then drops to khansoft
ENTRYPOINT ["/app/docker-entrypoint.sh"]
