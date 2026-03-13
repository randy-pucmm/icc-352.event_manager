FROM eclipse-temurin:21

WORKDIR /app

# Copy fat jar
COPY build/libs/*-all.jar app.jar

# Expose Javalin port
EXPOSE 7000

# Run app
ENTRYPOINT ["java", "-jar", "app.jar"]