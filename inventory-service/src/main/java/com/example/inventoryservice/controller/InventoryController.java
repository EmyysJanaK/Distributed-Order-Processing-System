package com.example.inventoryservice.controller;

import com.example.inventoryservice.dto.InventoryResponse;
// import com.example.inventoryservice.dto.CreateInventoryRequest; // Uncomment if you created this DTO
import com.example.inventoryservice.model.Inventory;
import com.example.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping
    public ResponseEntity<List<InventoryResponse>> getAllInventory() {
        return ResponseEntity.ok(inventoryService.getAllInventory());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(inventoryService.getById(id));
    }

    // Best practice is to use a CreateInventoryRequest DTO here instead of the Inventory entity
    @PostMapping
    public ResponseEntity<InventoryResponse> createInventory(@RequestBody Inventory inventoryRequest) {
        return ResponseEntity.ok(inventoryService.createInventory(inventoryRequest));
    }

    @PutMapping("/{id}")
    public ResponseEntity<InventoryResponse> updateInventory(
            @PathVariable UUID id,
            @RequestBody Inventory updatedInventory) {
        return ResponseEntity.ok(inventoryService.updateInventory(id, updatedInventory));
    }
}