# Distributed Order Processing System

Production-grade **modular monorepo** microservices architecture built with Spring Boot 3.2, Spring Cloud, Kafka, and PostgreSQL.

---

## Architecture Overview

```
                        ┌─────────────────┐
                        │   API Gateway   │  :8080
                        │ (Spring Cloud   │
                        │   Gateway)      │
                        └────────┬────────┘
                                 │ routes
           ┌─────────────────────┼──────────────────────┐
           ▼                     ▼                      ▼
   ┌──────────────┐     ┌──────────────┐     ┌──────────────────┐
   │ Order Service│     │Payment Service│    │Inventory Service │
   │   :8081      │     │   :8082      │     │    :8083         │
   └──────┬───────┘     └──────┬───────┘     └────────┬─────────┘
          │ publishes          │ publishes            │ consumes
          ▼                    ▼                      │
   ┌─────────────────────────────────────────────┐   │
   │              Apache Kafka                   │◄──┘
   │  Topics: order.created, payment.processed   │
   └──────────────────┬──────────────────────────┘
                      │ consumes
                      ▼
             ┌──────────────────┐
             │Notification Svc  │  :8084
             └──────────────────┘

   ┌────────────────────┐    ┌─────────────────────────────────┐
   │  Service Registry  │    │  Monitoring Stack               │
   │  (Eureka) :8761    │    │  Prometheus :9090 / Grafana :3000│
   └────────────────────┘    └─────────────────────────────────┘
```

---

## Module Structure

```
distributed-order-processing-system/   ← Parent POM (aggregator)
├── common-events/                     ← Shared event contracts (plain JAR)
├── service-registry/                  ← Eureka Server
├── api-gateway/                       ← Spring Cloud Gateway + circuit breaker
├── order-service/                     ← Order management + transactional outbox
├── payment-service/                   ← Payment processing (Kafka consumer)
├── inventory-service/                 ← Stock management (Kafka consumer)
├── notification-service/              ← Email notifications (Kafka consumer)
├── message-broker/                    ← Kafka docker config
├── monitoring-stack/                  ← Prometheus + Grafana configs
├── docker-compose.yml                 ← Full local dev environment
├── .github/workflows/ci.yml           ← Per-module CI/CD pipeline
└── .env.example                       ← Environment variable template
```

---

## Service Port Map

| Service            | Port  | Description                        |
|--------------------|-------|------------------------------------|
| API Gateway        | 8080  | Single entry point for all APIs    |
| Order Service      | 8081  | Create and manage orders           |
| Payment Service    | 8082  | Process payments via Kafka events  |
| Inventory Service  | 8083  | Reserve stock via Kafka events     |
| Notification Svc   | 8084  | Send emails via Kafka events       |
| Service Registry   | 8761  | Eureka dashboard                   |
| Prometheus         | 9090  | Metrics scraping                   |
| Grafana            | 3000  | Dashboards (admin/admin)           |
| Kafka              | 9092  | Message broker                     |
| PostgreSQL (orders)| 5432  | Orders database                    |
| PostgreSQL (pay)   | 5433  | Payments database                  |
| PostgreSQL (inv)   | 5434  | Inventory database                 |

---

## Getting Started

### Prerequisites
- Docker & Docker Compose
- Java 17 (for local development)
- Maven 3.9+

### 1. Clone & configure environment
```bash
git clone <repo-url>
cd distributed-order-processing-system
cp .env.example .env
# Edit .env with your values
```

### 2. Run the full stack
```bash
docker compose up --build -d
```

### 3. Verify services are up
```bash
# Eureka dashboard
open http://localhost:8761

# Create an order via API Gateway
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"customerId":"550e8400-e29b-41d4-a716-446655440000","totalAmount":99.99}'
```

---

## Build Individual Modules

```bash
# Build only order-service (and its dependencies)
mvn -pl order-service -am clean package -DskipTests

# Build only payment-service
mvn -pl payment-service -am clean package -DskipTests

# Build entire monorepo
mvn clean verify
```

---

## Key Design Patterns

| Pattern                  | Where Used                          |
|--------------------------|-------------------------------------|
| Transactional Outbox     | order-service → Kafka               |
| Event-Driven Architecture| Kafka topics between all services   |
| Service Discovery        | Eureka (all services register)      |
| API Gateway              | Single entry, circuit breaker       |
| Circuit Breaker          | API Gateway (Resilience4j)          |
| Shared Contracts         | common-events module                |
| Database per Service     | Each service owns its PostgreSQL DB |

---

## CI/CD

GitHub Actions workflow (`.github/workflows/ci.yml`) uses **path-based filtering** to build only the modules that changed:

- Push to `main` → builds changed modules + pushes Docker images to GHCR
- PR → builds and tests changed modules only
- Change to root `pom.xml` → triggers full monorepo build

---

## Observability

- **Health**: `GET /actuator/health` on each service
- **Metrics**: `GET /actuator/prometheus` scraped by Prometheus
- **Dashboards**: Grafana at `http://localhost:3000` (admin/admin)

