# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-22 AS build
WORKDIR /app

# Copy pom.xml first so dependency layer is cached separately from source changes
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

# ---- Runtime stage ----
FROM eclipse-temurin:22-jre
WORKDIR /app

COPY --from=build /app/target/filelink-1.0.0.jar app.jar

# Render sets $PORT at runtime; application.properties already reads server.port=${PORT:8080}
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]