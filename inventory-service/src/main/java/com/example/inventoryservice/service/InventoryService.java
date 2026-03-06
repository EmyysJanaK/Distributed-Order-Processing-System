package com.example.inventoryservice.service;

import com.example.commonevents.OrderCreatedEvent;
import com.example.inventoryservice.dto.InventoryResponse;
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
@Slf4j //for logging
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    //READ OPERATIONS
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
    public InventoryResponse createInventory(Inventory request) {
        // Saves the new inventory entity to the database and maps it to the response DTO
        Inventory savedInventory = inventoryRepository.save(request);
        return mapToResponse(savedInventory);
    }

    @Transactional
    public InventoryResponse updateInventory(UUID id, Inventory updatedData) {
        // 1. Find the existing record
        Inventory existingInventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Inventory item not found with ID: " + id));

        // 2. Update the fields safely (Do not overwrite the ID)
        existingInventory.setId(updatedData.getId());
        existingInventory.setName(updatedData.getName());
        existingInventory.setAvailableQuantity(updatedData.getAvailableQuantity());
        existingInventory.setQuantityReserved(updatedData.getQuantityReserved());

        // 3. Save and return
        Inventory savedInventory = inventoryRepository.save(existingInventory);
        return mapToResponse(savedInventory);
    }

    @Transactional
    public void reserveStock(OrderCreatedEvent event) {
        log.info("Reserving stock for orderId={} customerId={}",
                event.getOrderId(), event.getCustomerId());

        for (com.example.commonevents.OrderItem item : event.getItems()) {
            Inventory inventory = inventoryRepository.findById(item.productId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + item.productId()));

            // Corrected to getQuantityAvailable()
            if (inventory.getAvailableQuantity() < item.quantity()) {
                log.error("Insufficient stock for product: {}", inventory.getName());
                throw new RuntimeException("Out of stock for product: " + inventory.getName());
            }

            // Corrected to align with your entity's available/reserved logic
            inventory.setAvailableQuantity(inventory.getAvailableQuantity()- item.quantity());
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

        // Move stock from reserved back to available
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
                inventory.getAvailableQuantity(),
                inventory.getUnitPrice()
        );
    }

}
