package com.example.inventoryservice.dto;

import java.math.BigDecimal;

public record UpdateInventoryRequest(
        String sku,
        String name,
        String description,
        Integer availableQuantity,
        BigDecimal unitPrice
) {
}
