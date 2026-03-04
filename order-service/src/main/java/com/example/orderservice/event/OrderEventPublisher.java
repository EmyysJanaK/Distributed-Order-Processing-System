package com.example.orderservice.event;

import com.example.commonevents.OrderCreatedEvent;
import com.example.orderservice.config.KafkaProducerConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishOrderCreatedEvent(OrderCreatedEvent event) {
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(KafkaProducerConfig.ORDER_CREATED_TOPIC,
                        event.getOrderId().toString(), event);
        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish OrderCreatedEvent for orderId={}", event.getOrderId(), ex);
            } else {
                log.info("Published OrderCreatedEvent for orderId={} to partition={}",
                        event.getOrderId(), result.getRecordMetadata().partition());
            }
        });
    }
}
