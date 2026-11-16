# ---- 1. Build the React frontend ----
FROM node:25-alpine AS frontend
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm ci
COPY frontend/ ./
RUN npm run build

# ---- 2. Build the Spring Boot app, with the frontend inside ----
FROM eclipse-temurin:25-jdk AS backend
WORKDIR /app
COPY gradlew settings.gradle build.gradle ./
COPY gradle gradle
COPY src src
COPY --from=frontend /app/frontend/dist src/main/resources/static
RUN ./gradlew bootJar --no-daemon -x test

# ---- 3. The small image that actually runs ----
FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=backend /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
