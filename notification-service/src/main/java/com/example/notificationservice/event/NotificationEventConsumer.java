package com.example.notificationservice.event;

import com.example.commonevents.OrderCreatedEvent;
import com.example.commonevents.PaymentProcessedEvent;
import com.example.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventConsumer {

    private final NotificationService notificationService;

    @KafkaListener(
            topics = "order.created",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "orderKafkaListenerContainerFactory"
    )
    public void onOrderCreated(
            @Payload OrderCreatedEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            Acknowledgment acknowledgment) {

        log.info("Notification: received OrderCreatedEvent orderId={}", event.getOrderId());
        try {
            notificationService.notifyOrderCreated(event);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Failed to send order-created notification", e);
        }
    }

    @KafkaListener(
            topics = "payment.processed",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "paymentKafkaListenerContainerFactory"
    )
    public void onPaymentProcessed(
            @Payload PaymentProcessedEvent event,
            @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
            Acknowledgment acknowledgment) {

        log.info("Notification: received PaymentProcessedEvent orderId={} status={}",
                event.getOrderId(), event.getStatus());
        try {
            notificationService.notifyPaymentProcessed(event);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Failed to send payment-processed notification", e);
        }
    }
}

