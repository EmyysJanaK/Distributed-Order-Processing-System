package com.example.inventoryservice.dto;

public record StockDeductionRequest(
        Integer quantity,
        String orderId
) {
}
