# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn package -DskipTests

# Stage 2: Create a minimal runtime image
FROM gcr.io/distroless/java21-debian12
WORKDIR /app
COPY --from=build /app/target/app.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
