# ---- Build Stage ----
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /workspace

COPY pom.xml .
COPY common-events/pom.xml common-events/pom.xml
COPY service-registry/pom.xml service-registry/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY order-service/pom.xml order-service/pom.xml
COPY payment-service/pom.xml payment-service/pom.xml
COPY inventory-service/pom.xml inventory-service/pom.xml
COPY notification-service/pom.xml notification-service/pom.xml

RUN mvn dependency:go-offline -B --no-transfer-progress

COPY common-events/src common-events/src
COPY notification-service/src notification-service/src

RUN mvn -pl notification-service -am clean package -DskipTests -B --no-transfer-progress

# ---- Runtime Stage ----
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=builder /workspace/notification-service/target/*.jar app.jar

HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8084/actuator/health || exit 1

EXPOSE 8084
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]

