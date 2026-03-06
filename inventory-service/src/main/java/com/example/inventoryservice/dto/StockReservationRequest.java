package com.example.inventoryservice.dto;

public record StockReservationRequest(
        Integer quantity,
        String orderId
) {}
