package com.example.orderservice.service;

import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderStatus;
import com.example.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;

    @Transactional
    public Order createOrder(CreateOrderRequest request) {
        Order order = Order.builder()
                .customerId(request.getCustomerId())
                .totalAmount(request.getTotalAmount())
                .status(OrderStatus.PENDING)
                .build();
        Order savedOrder = orderRepository.save(order);
        // TODO: Publish event after persistence
        return savedOrder;
    }

    // DTO for order creation
    @lombok.Data
    public static class CreateOrderRequest {
        private UUID customerId;
        private BigDecimal totalAmount;
    }
}
