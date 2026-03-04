package com.example.orderservice.service;

import com.example.commonevents.OrderCreatedEvent;
import com.example.orderservice.model.Order;
import com.example.orderservice.model.OrderStatus;
import com.example.orderservice.model.OutboxEvent;
import com.example.orderservice.repository.OrderRepository;
import com.example.orderservice.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OutboxEventRepository outboxEventRepository;

    @InjectMocks
    private OrderService orderService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // register JavaTimeModule for Instant
        orderService = new OrderService(orderRepository, outboxEventRepository, objectMapper);
    }

    @Test
    @DisplayName("createOrder - saves order with PENDING status and writes outbox event")
    void createOrder_shouldSaveOrderAndOutboxEvent() {
        // Arrange
        UUID customerId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(149.99);

        OrderService.CreateOrderRequest request = new OrderService.CreateOrderRequest();
        request.setCustomerId(customerId);
        request.setTotalAmount(amount);

        Order savedOrder = Order.builder()
                .id(UUID.randomUUID())
                .customerId(customerId)
                .totalAmount(amount)
                .status(OrderStatus.PENDING)
                .build();

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(outboxEventRepository.save(any(OutboxEvent.class))).thenAnswer(i -> i.getArgument(0));

        // Act
        Order result = orderService.createOrder(request);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(result.getCustomerId()).isEqualTo(customerId);
        assertThat(result.getTotalAmount()).isEqualByComparingTo(amount);

        // Verify outbox event was saved with correct fields
        ArgumentCaptor<OutboxEvent> outboxCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository).save(outboxCaptor.capture());
        OutboxEvent capturedOutbox = outboxCaptor.getValue();

        assertThat(capturedOutbox.getAggregateType()).isEqualTo("Order");
        assertThat(capturedOutbox.getType()).isEqualTo("OrderCreatedEvent");
        assertThat(capturedOutbox.isPublished()).isFalse();
        assertThat(capturedOutbox.getPayload()).contains(savedOrder.getId().toString());
    }

    @Test
    @DisplayName("createOrder - outbox payload is valid JSON containing OrderCreatedEvent fields")
    void createOrder_outboxPayloadIsValidJson() throws Exception {
        UUID customerId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(200.00);

        OrderService.CreateOrderRequest request = new OrderService.CreateOrderRequest();
        request.setCustomerId(customerId);
        request.setTotalAmount(amount);

        Order savedOrder = Order.builder()
                .id(UUID.randomUUID())
                .customerId(customerId)
                .totalAmount(amount)
                .status(OrderStatus.PENDING)
                .build();

        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(outboxEventRepository.save(any(OutboxEvent.class))).thenAnswer(i -> i.getArgument(0));

        orderService.createOrder(request);

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository).save(captor.capture());

        String payload = captor.getValue().getPayload();
        OrderCreatedEvent event = objectMapper.readValue(payload, OrderCreatedEvent.class);

        assertThat(event.getOrderId()).isEqualTo(savedOrder.getId());
        assertThat(event.getCustomerId()).isEqualTo(customerId);
        assertThat(event.getTotalAmount()).isEqualByComparingTo(amount);
        assertThat(event.getTimestamp()).isNotNull();
    }
}

