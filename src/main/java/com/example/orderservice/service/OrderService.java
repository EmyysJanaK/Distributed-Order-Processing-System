package com.example.orderservice.service;

import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderStatus;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.model.OrderCreatedEvent;
import org.springframework.context.ApplicationEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        Order order = Order.builder()
                .customerId(request.getCustomerId())
                .totalAmount(request.getTotalAmount())
                .status(OrderStatus.PENDING)
                .build();
        Order savedOrder = orderRepository.save(order);
        // Publish event after persistence (will be handled after commit)
        OrderCreatedEvent event = OrderCreatedEvent.builder()
            .orderId(savedOrder.getId())
            .customerId(savedOrder.getCustomerId())
            .totalAmount(savedOrder.getTotalAmount())
            .timestamp(java.time.Instant.now())
            .build();
        eventPublisher.publishEvent(event);
        return savedOrder;
    }

    // DTO for order creation
    @lombok.Data
    public static class CreateOrderRequest {
        private UUID customerId;
        private BigDecimal totalAmount;
    }
}
