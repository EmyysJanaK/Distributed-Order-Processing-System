package com.example.orderservice.event;

import com.example.orderservice.model.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import com.example.orderservice.config.KafkaProducerConfig;

@Component
@RequiredArgsConstructor
public class OrderEventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishOrderCreatedEvent(OrderCreatedEvent event) {
        kafkaTemplate.send(KafkaProducerConfig.ORDER_CREATED_TOPIC, event);
        // Optionally add logging or error handling here
    }
}
