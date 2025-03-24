# Stage 1: Build the application
FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Run the application
FROM eclipse-temurin:21-jdk
WORKDIR /app

# Copy the built JAR file
COPY --from=builder /app/target/*.jar app.jar

# Make sure logs directory exists
RUN mkdir -p /app/logs

# Expose the port for the bot (if needed)
EXPOSE 8080

# Entry point with environment variables loaded from Docker Compose
ENTRYPOINT ["java", "-jar", "app.jar"]