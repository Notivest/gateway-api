# Build
FROM gradle:8.8-jdk21-alpine AS build
WORKDIR /app
COPY . .
RUN gradle bootJar --no-daemon

# Runtime
FROM eclipse-temurin:21-jre
WORKDIR /app
RUN apt-get update && apt-get install -y --no-install-recommends ca-certificates curl && rm -rf /var/lib/apt/lists/*
RUN curl -fsSL https://download.newrelic.com/newrelic/java-agent/newrelic-agent/current/newrelic.jar -o /app/newrelic.jar
COPY --from=build /app/build/libs/*.jar /app/app.jar
ENV NEW_RELIC_APP_NAME=api-gateway \
    NEW_RELIC_DISTRIBUTED_TRACING_ENABLED=true \
    NEW_RELIC_APPLICATION_LOGGING_FORWARDING_ENABLED=false \
    NEW_RELIC_LOG_FILE_NAME=STDOUT \
    JAVA_OPTS="-XX:MaxRAMPercentage=75"
EXPOSE 8080
RUN useradd -r -u 1001 appuser
USER appuser
ENTRYPOINT ["sh","-c","exec java $JAVA_OPTS -javaagent:/app/newrelic.jar -jar /app/app.jar"]
