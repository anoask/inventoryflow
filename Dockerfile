# Multi-stage build for Railway, Fly.io, or any Docker host.
# Build context: repository root (where pom.xml lives).

FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /build
COPY pom.xml .
COPY src ./src
RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /build/target/inventoryflow.jar app.jar
EXPOSE 8080
# Small Railway instances: respect cgroup memory limits (reduces sudden OOM kills).
ENV JAVA_TOOL_OPTIONS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
ENV JAVA_OPTS=""
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
