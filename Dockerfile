LABEL authors="rosti"

# Stage 1: Build the application
FROM maven:3.8.4-openjdk-21 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Create the runtime image
FROM openjdk:21-jdk-slim
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8080 # Or the port your application uses
ENTRYPOINT ["java", "-jar", "app.jar"]