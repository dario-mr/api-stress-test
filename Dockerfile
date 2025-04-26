# This is necessary due to a bug that emerged on the raspberry pi about not being able to unzip spring boot jar at runtime...
# java.lang.NoClassDefFoundError: org/springframework/boot/loader/zip/ZipContent$Loader
# This dockerfile is a workaround until I figure out a proper fix.
# Original Dockerfile is Dockerfile_orig.

# Stage 1: Build and explode
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .

# Package and explode
RUN mvn -B -q package -DskipTests -DskipChecks -Dmaven.javadoc.skip=true -Dcheckstyle.skip=true -Denforcer.skip=true -Dmdep.analyze.skip=true && \
    mkdir exploded && cd exploded && \
    jar -xf ../target/app.jar

# Stage 2: Minimal runtime image
FROM gcr.io/distroless/java21-debian12
WORKDIR /app

# Copy exploded contents
COPY --from=build /app/exploded/ .

ENTRYPOINT ["java", "-cp", "BOOT-INF/classes:BOOT-INF/lib/*", "com.dario.ast.Application"]
