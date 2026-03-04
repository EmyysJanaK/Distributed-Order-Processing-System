package com.example.apigateway.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                // Order Service
                .route("order-service", r -> r
                        .path("/api/orders/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .circuitBreaker(c -> c
                                        .setName("order-service-cb")
                                        .setFallbackUri("forward:/fallback/orders")))
                        .uri("lb://order-service"))

                // Payment Service
                .route("payment-service", r -> r
                        .path("/api/payments/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .circuitBreaker(c -> c
                                        .setName("payment-service-cb")
                                        .setFallbackUri("forward:/fallback/payments")))
                        .uri("lb://payment-service"))

                // Inventory Service
                .route("inventory-service", r -> r
                        .path("/api/inventory/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .circuitBreaker(c -> c
                                        .setName("inventory-service-cb")
                                        .setFallbackUri("forward:/fallback/inventory")))
                        .uri("lb://inventory-service"))

                // Notification Service
                .route("notification-service", r -> r
                        .path("/api/notifications/**")
                        .filters(f -> f
                                .stripPrefix(1)
                                .circuitBreaker(c -> c
                                        .setName("notification-service-cb")
                                        .setFallbackUri("forward:/fallback/notifications")))
                        .uri("lb://notification-service"))
                .build();
    }
}

