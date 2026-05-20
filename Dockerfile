# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle

# Download dependencies (cached layer)
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon

COPY src ./src

RUN ./gradlew bootJar --no-daemon -x test
RUN JAR_FILE=$(ls build/libs/*.jar | grep -v -- '-plain\.jar$' | head -n 1) && cp "$JAR_FILE" app.jar

# Stage 2: Run (JDK image so jcmd/JFR tooling is available in container)
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app

COPY --from=builder /app/app.jar app.jar

EXPOSE 8001

ENTRYPOINT ["java", "-jar", "app.jar"]
