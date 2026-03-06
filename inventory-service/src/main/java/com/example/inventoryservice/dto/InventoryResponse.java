package com.example.inventoryservice.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record InventoryResponse(
    UUID id,
    String sku,
    String name,
    String description,
    BigDecimal unitPrice,
    Integer availableQuantity,
    Integer quantityReserved

){}
