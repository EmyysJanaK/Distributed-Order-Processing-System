# ---- Build Stage ----
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /workspace

# Copy parent POM and all module POMs first (layer-cache dependency downloads)
COPY pom.xml .
COPY common-events/pom.xml common-events/pom.xml
COPY service-registry/pom.xml service-registry/pom.xml
COPY api-gateway/pom.xml api-gateway/pom.xml
COPY order-service/pom.xml order-service/pom.xml
COPY payment-service/pom.xml payment-service/pom.xml
COPY inventory-service/pom.xml inventory-service/pom.xml
COPY notification-service/pom.xml notification-service/pom.xml

# Download all dependencies (cached unless POMs change)
RUN mvn dependency:go-offline -B --no-transfer-progress

# Copy source code for this module and its dependencies
COPY common-events/src common-events/src
COPY order-service/src order-service/src

# Build only this module and required upstream modules
RUN mvn -pl order-service -am clean package -DskipTests -B --no-transfer-progress

# ---- Runtime Stage ----
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Create non-root user for security
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=builder /workspace/order-service/target/*.jar app.jar

# Health check using actuator
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget -qO- http://localhost:8081/actuator/health || exit 1

EXPOSE 8081
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]

