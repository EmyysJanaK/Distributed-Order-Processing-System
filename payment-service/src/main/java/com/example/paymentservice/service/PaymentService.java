package com.example.paymentservice.service;

import com.example.commonevents.OrderCreatedEvent;
import com.example.commonevents.PaymentProcessedEvent;
import com.example.paymentservice.model.Payment;
import com.example.paymentservice.model.PaymentStatus;
import com.example.paymentservice.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private static final String PAYMENT_PROCESSED_TOPIC = "payment.processed";

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional
    public void processPayment(OrderCreatedEvent event) {
        // Idempotency: skip if already processed
        if (paymentRepository.findByOrderId(event.getOrderId()).isPresent()) {
            log.warn("Payment already processed for orderId={}", event.getOrderId());
            return;
        }

        // Simulate payment processing (replace with real payment gateway)
        PaymentStatus status = PaymentStatus.SUCCESS;

        Payment payment = Payment.builder()
                .orderId(event.getOrderId())
                .customerId(event.getCustomerId())
                .amount(event.getTotalAmount())
                .status(status)
                .processedAt(Instant.now())
                .build();
        paymentRepository.save(payment);
        log.info("Payment {} for orderId={}", status, event.getOrderId());

        PaymentProcessedEvent processedEvent = PaymentProcessedEvent.builder()
                .paymentId(payment.getId())
                .orderId(event.getOrderId())
                .customerId(event.getCustomerId())
                .amount(event.getTotalAmount())
                .status(PaymentProcessedEvent.PaymentStatus.valueOf(status.name()))
                .timestamp(Instant.now())
                .build();

        kafkaTemplate.send(PAYMENT_PROCESSED_TOPIC,
                event.getOrderId().toString(), processedEvent);
    }
}

