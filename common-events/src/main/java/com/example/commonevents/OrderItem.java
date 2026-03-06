package com.example.commonevents;

import java.util.UUID;

public record OrderItem(UUID productId, Integer quantity) {
}
