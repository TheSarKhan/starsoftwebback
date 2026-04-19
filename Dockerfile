# ============================================================
# KhanSoft backend — production image
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

# Create non-root user and writable directories before switching user.
# chown on images/logs ensures Docker named-volume mounts inherit correct ownership.
RUN addgroup -S khansoft && adduser -S khansoft -G khansoft \
    && mkdir -p /app/images /app/logs \
    && chown -R khansoft:khansoft /app

COPY --from=build --chown=khansoft:khansoft /build/target/app.jar /app/app.jar

USER khansoft

EXPOSE 8080

# Honour container memory limits, sane GC defaults
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
