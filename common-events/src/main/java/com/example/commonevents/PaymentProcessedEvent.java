package com.example.commonevents;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Event published by payment-service after processing a payment.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentProcessedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private UUID paymentId;
    private UUID orderId;
    private UUID customerId;
    private BigDecimal amount;
    private PaymentStatus status;
    private Instant timestamp;

    public enum PaymentStatus {
        SUCCESS, FAILED, PENDING
    }
}

