# ---------- Stage 1: Build ----------
FROM gradle:9.2.1-jdk21-alpine AS builder

WORKDIR /project

# Copy everything
COPY . .

RUN ./gradlew shadowJar --no-daemon
# ---------- Stage 2: Run ----------
FROM eclipse-temurin:21-jre

WORKDIR /project

# Copy built jar from builder stage
COPY --from=builder /project/app/build/libs/*-all.jar app.jar

EXPOSE 7000

ENTRYPOINT ["java", "-jar", "app.jar"]