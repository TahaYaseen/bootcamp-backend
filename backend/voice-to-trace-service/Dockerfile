# ================================
# VoiceToTrace Backend - Dockerfile
# ================================

# Base image for Java
FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy project files
COPY . .

# Set environment variables (for .env support)
ENV SPRING_CONFIG_LOCATION=classpath:/application.yml
ENV SPRING_PROFILES_ACTIVE=default
ENV SERVER_PORT=8081

# Copy the .env variables for runtime (if using docker-compose)
COPY .env .env

# Build and package the application
RUN ./mvnw clean package -DskipTests || mvn clean package -DskipTests

# Expose server port
EXPOSE 8081

# Run the application
ENTRYPOINT ["java", "-jar", "target/voice-to-trace-service-0.0.1-SNAPSHOT.jar"]