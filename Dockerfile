# ---------- Stage 1: Build ----------
FROM gradle:9.2.1-jdk21-alpine AS builder

WORKDIR /project

# Copy build config files first (better Docker layer caching)
COPY settings.gradle ./
COPY gradle/libs.versions.toml gradle/libs.versions.toml
COPY app/build.gradle app/build.gradle

# Copy source code
COPY app/src app/src

# Build fat JAR with ShadowJar
RUN gradle shadowJar --no-daemon -p app

# ---------- Stage 2: Run ----------
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy built jar from builder stage
COPY --from=builder /project/app/build/libs/*-all.jar app.jar

# Create data directory for H2 database
RUN mkdir -p /app/data

# Volume for H2 persistent data
VOLUME /app/data

EXPOSE 7000

ENTRYPOINT ["java", "-jar", "app.jar"]
