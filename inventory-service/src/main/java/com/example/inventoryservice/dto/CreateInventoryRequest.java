package com.example.inventoryservice.dto;

import java.math.BigDecimal;

public record CreateInventoryRequest(
        String sku,
        String name,
        String description,
        Integer initialQuantity,
        BigDecimal unitPrice) {

}
