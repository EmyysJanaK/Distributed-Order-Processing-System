package com.example.inventoryservice.service;

import com.example.commonevents.OrderCreatedEvent;
import com.example.inventoryservice.dto.CreateInventoryRequest;
import com.example.inventoryservice.dto.InventoryResponse;
import com.example.inventoryservice.dto.UpdateInventoryRequest;
import com.example.inventoryservice.model.Inventory;
import com.example.inventoryservice.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public List<InventoryResponse> getAllInventory() {
        return inventoryRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public InventoryResponse getById(UUID id) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inventory item not found with ID: " + id));
        return mapToResponse(inventory);
    }


    @Transactional
    public InventoryResponse createInventory(CreateInventoryRequest request) {
        Inventory inventory = Inventory.builder()
                .sku(request.sku())
                .name(request.name())
                .description(request.description())
                .unitPrice(request.unitPrice())
                .availableQuantity(request.initialQuantity())
                .quantityReserved(0) // Explicitly starts at 0
                .build();

        Inventory savedInventory = inventoryRepository.save(inventory);
        return mapToResponse(savedInventory);
    }

    @Transactional
    public InventoryResponse updateInventory(UUID id, UpdateInventoryRequest request) {
        Inventory existingInventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inventory item not found with ID: " + id));

        existingInventory.setSku(request.sku());
        existingInventory.setName(request.name());
        existingInventory.setDescription(request.description());
        existingInventory.setUnitPrice(request.unitPrice());
        existingInventory.setAvailableQuantity(request.availableQuantity());
        // We purposely DO NOT update quantityReserved here so active orders aren't corrupted

        Inventory savedInventory = inventoryRepository.save(existingInventory);
        return mapToResponse(savedInventory);
    }

    // ==========================================
    // 3. EVENT-DRIVEN OPERATIONS
    // ==========================================

    @Transactional
    public void reserveStock(OrderCreatedEvent event) {
        log.info("Reserving stock for orderId={} customerId={}", event.getOrderId(), event.getCustomerId());

        for (com.example.commonevents.OrderItem item : event.getItems()) {
            Inventory inventory = inventoryRepository.findById(item.productId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + item.productId()));

            if (inventory.getAvailableQuantity() < item.quantity()) {
                log.error("Insufficient stock for product: {}", inventory.getName());
                throw new RuntimeException("Out of stock for product: " + inventory.getName());
            }

            inventory.setAvailableQuantity(inventory.getAvailableQuantity() - item.quantity());
            inventory.setQuantityReserved(inventory.getQuantityReserved() + item.quantity());

            inventoryRepository.save(inventory);
            log.info("Reserved {} units of {}", item.quantity(), inventory.getName());
        }

        log.info("Stock reservation completed for orderId={}", event.getOrderId());
    }

    @Transactional
    public void releaseStock(UUID productId, Integer quantityToRelease) {
        log.info("Releasing {} units back to available stock for product={}", quantityToRelease, productId);

        Inventory inventory = inventoryRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

        inventory.setQuantityReserved(inventory.getQuantityReserved() - quantityToRelease);
        inventory.setAvailableQuantity(inventory.getAvailableQuantity() + quantityToRelease);

        inventoryRepository.save(inventory);
    }


    private InventoryResponse mapToResponse(Inventory inventory) {
        return new InventoryResponse(
                inventory.getId(),
                inventory.getSku(),
                inventory.getName(),
                inventory.getDescription(),
                inventory.getUnitPrice(),
                inventory.getAvailableQuantity(),
                inventory.getQuantityReserved()
        );
    }
}