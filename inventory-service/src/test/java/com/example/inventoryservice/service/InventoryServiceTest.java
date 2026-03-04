package com.example.inventoryservice.service;

import com.example.commonevents.OrderCreatedEvent;
import com.example.inventoryservice.repository.InventoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatNoException;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private InventoryService inventoryService;

    @Test
    @DisplayName("reserveStock - processes event without throwing")
    void reserveStock_doesNotThrow() {
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(UUID.randomUUID())
                .customerId(UUID.randomUUID())
                .totalAmount(BigDecimal.valueOf(50.00))
                .timestamp(Instant.now())
                .build();

        assertThatNoException().isThrownBy(() -> inventoryService.reserveStock(event));
    }
}

