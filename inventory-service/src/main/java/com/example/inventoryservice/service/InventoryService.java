package com.example.inventoryservice.service;

import com.example.commonevents.OrderCreatedEvent;
import com.example.inventoryservice.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    @Transactional
    public void reserveStock(OrderCreatedEvent event) {
        log.info("Reserving stock for orderId={} customerId={}",
                event.getOrderId(), event.getCustomerId());
        // Real implementation: find item(s) from order, check & decrement stock
        // For now: log and acknowledge (extend with order line items in common-events)
        log.info("Stock reservation completed for orderId={}", event.getOrderId());
    }
}

