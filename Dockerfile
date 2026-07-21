# syntax=docker/dockerfile:1

########################################
# Stage 1: build the Vue frontend
########################################
FROM node:16-bullseye AS vue-build
WORKDIR /app/src/main/vue

# Install dependencies first (better layer caching)
COPY src/main/vue/package.json src/main/vue/package-lock.json ./
# --legacy-peer-deps: trumbowyg declares a peer dep spelled "jQuery" (capital J),
# which the registry (lowercase "jquery" only) can't resolve, breaking npm ci.
RUN npm ci --legacy-peer-deps

# Build the Vue app -> src/main/vue/dist
COPY src/main/vue/ ./
RUN npm run build

########################################
# Stage 2: build the Spring Boot app (Java 17)
########################################
FROM eclipse-temurin:17-jdk AS spring-build
WORKDIR /app

# Gradle wrapper + build config first for caching
COPY gradlew ./
COPY gradle/ ./gradle/
COPY build.gradle.kts settings.gradle.kts ./
RUN chmod +x gradlew && ./gradlew --version

# Application sources
COPY src/ ./src/

# Bring in the compiled Vue app from stage 1 (updateVue copies from here)
COPY --from=vue-build /app/src/main/vue/dist ./src/main/vue/dist

# Build the executable jar (skip tests for a fast debug image)
RUN ./gradlew bootJar -x test --no-daemon

########################################
# Stage 3: runtime (Java 17)
########################################
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=spring-build /app/build/libs/*.jar app.jar

# App port + remote JVM debug port
EXPOSE 9050 5005

# Enable remote debugging (attach from IntelliJ/VS Code on port 5005)
ENV JAVA_TOOL_OPTIONS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"

ENTRYPOINT ["java", "-jar", "app.jar"]