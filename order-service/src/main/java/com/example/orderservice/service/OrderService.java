package com.example.orderservice.service;

import com.example.commonevents.OrderCreatedEvent;
import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderStatus;
import com.example.orderservice.model.OutboxEvent;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        Order order = Order.builder()
                .customerId(request.getCustomerId())
                .totalAmount(request.getTotalAmount())
                .status(OrderStatus.PENDING)
                .build();
        Order savedOrder = orderRepository.save(order);
        log.info("Order created with id={}", savedOrder.getId());

        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(savedOrder.getId())
                .customerId(savedOrder.getCustomerId())
                .totalAmount(savedOrder.getTotalAmount())
                .timestamp(Instant.now())
                .build();

        try {
            String payload = objectMapper.writeValueAsString(event);
            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .aggregateType("Order")
                    .aggregateId(savedOrder.getId())
                    .type("OrderCreatedEvent")
                    .payload(payload)
                    .createdAt(Instant.now())
                    .published(false)
                    .build();
            outboxEventRepository.save(outboxEvent);
            log.info("OutboxEvent saved for orderId={}", savedOrder.getId());
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize OrderCreatedEvent for outbox", e);
        }
        return savedOrder;
    }

    @Data
    public static class CreateOrderRequest {
        @NotNull
        private UUID customerId;
        @NotNull
        @Positive
        private BigDecimal totalAmount;
    }
}
