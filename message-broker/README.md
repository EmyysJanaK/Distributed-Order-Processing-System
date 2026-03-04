# Message Broker - Kafka Configuration
#
# Kafka is provisioned via docker-compose.yml at the root.
# This directory holds Kafka-related configuration and documentation.
#
# Topics used:
#   - order.created       published by: order-service     consumed by: payment-service, inventory-service, notification-service
#   - payment.processed   published by: payment-service   consumed by: notification-service
#
# Kafka runs inside Docker on port 29092 (internal) and 9092 (host).
# Auto topic creation is enabled in docker-compose (KAFKA_AUTO_CREATE_TOPICS_ENABLE=true).
#
# To create topics manually (if auto-creation is disabled):
#   docker exec kafka kafka-topics --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 3 --topic order.created
#   docker exec kafka kafka-topics --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 3 --topic payment.processed
#
# To list topics:
#   docker exec kafka kafka-topics --list --bootstrap-server localhost:9092
#
# To consume messages from a topic:
#   docker exec kafka kafka-console-consumer --bootstrap-server localhost:9092 --topic order.created --from-beginning

