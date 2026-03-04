package com.example.apigateway.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping("/orders")
    public ResponseEntity<Map<String, String>> orderFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("message", "Order Service is currently unavailable. Please try again later."));
    }

    @GetMapping("/payments")
    public ResponseEntity<Map<String, String>> paymentFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("message", "Payment Service is currently unavailable. Please try again later."));
    }

    @GetMapping("/inventory")
    public ResponseEntity<Map<String, String>> inventoryFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("message", "Inventory Service is currently unavailable. Please try again later."));
    }

    @GetMapping("/notifications")
    public ResponseEntity<Map<String, String>> notificationFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("message", "Notification Service is currently unavailable. Please try again later."));
    }
}

