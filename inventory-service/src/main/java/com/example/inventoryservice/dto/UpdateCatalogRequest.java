package com.example.inventoryservice.dto;

import java.math.BigDecimal;

public record UpdateCatalogRequest(
        String name,
        String description,
        BigDecimal unitPrice
)
{}
