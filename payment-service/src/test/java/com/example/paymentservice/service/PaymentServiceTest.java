package com.example.paymentservice.service;

import com.example.commonevents.OrderCreatedEvent;
import com.example.paymentservice.model.Payment;
import com.example.paymentservice.model.PaymentStatus;
import com.example.paymentservice.repository.PaymentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    @DisplayName("processPayment - saves payment with SUCCESS status and publishes event")
    void processPayment_savesPaymentAndPublishesEvent() {
        UUID orderId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        BigDecimal amount = BigDecimal.valueOf(150.00);

        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(orderId)
                .customerId(customerId)
                .totalAmount(amount)
                .timestamp(Instant.now())
                .build();

        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(paymentRepository.save(any(Payment.class))).thenAnswer(inv -> {
            Payment p = inv.getArgument(0);
            p = Payment.builder()
                    .id(UUID.randomUUID())
                    .orderId(p.getOrderId())
                    .customerId(p.getCustomerId())
                    .amount(p.getAmount())
                    .status(p.getStatus())
                    .build();
            return p;
        });

        paymentService.processPayment(event);

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());

        Payment saved = paymentCaptor.getValue();
        assertThat(saved.getOrderId()).isEqualTo(orderId);
        assertThat(saved.getCustomerId()).isEqualTo(customerId);
        assertThat(saved.getAmount()).isEqualByComparingTo(amount);
        assertThat(saved.getStatus()).isEqualTo(PaymentStatus.SUCCESS);

        verify(kafkaTemplate).send(eq("payment.processed"), eq(orderId.toString()), any());
    }

    @Test
    @DisplayName("processPayment - skips if already processed (idempotency)")
    void processPayment_skipsIfAlreadyProcessed() {
        UUID orderId = UUID.randomUUID();
        OrderCreatedEvent event = OrderCreatedEvent.builder()
                .orderId(orderId)
                .customerId(UUID.randomUUID())
                .totalAmount(BigDecimal.TEN)
                .timestamp(Instant.now())
                .build();

        when(paymentRepository.findByOrderId(orderId))
                .thenReturn(Optional.of(Payment.builder().orderId(orderId).build()));

        paymentService.processPayment(event);

        verify(paymentRepository, never()).save(any());
        verify(kafkaTemplate, never()).send(anyString(), anyString(), any());
    }
}

