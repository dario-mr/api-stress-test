# Stage 1: Build the application
FROM maven:3.9.9-eclipse-temurin-21-jammy AS build
WORKDIR /app
COPY . .
RUN mvn -B -q package -DskipTests -Dmaven.javadoc.skip=true -Dcheckstyle.skip=true -Denforcer.skip=true -Dmdep.analyze.skip=true

# Stage 2: Create a minimal runtime image
FROM gcr.io/distroless/java21-debian12
WORKDIR /app
COPY --from=build /app/target/app.jar app.jar
# Check jar validity
RUN unzip -t app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
