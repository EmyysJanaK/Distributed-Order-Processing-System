package com.example.orderservice.service;

import com.example.orderservice.model.OutboxEvent;
import com.example.orderservice.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.example.orderservice.config.KafkaProducerConfig;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {
    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Scheduled(fixedDelay = 5000)
    @Transactional
    public void publishOutboxEvents() {
        List<OutboxEvent> events = outboxEventRepository.findByPublishedFalse();
        for (OutboxEvent event : events) {
            try {
                // Idempotency: check again if already published (in case of concurrent execution)
                if (event.isPublished()) continue;
                kafkaTemplate.send(KafkaProducerConfig.ORDER_CREATED_TOPIC, event.getPayload());
                event.setPublished(true);
                outboxEventRepository.save(event);
                log.info("Published outbox event {}", event.getId());
            } catch (Exception e) {
                log.error("Failed to publish outbox event {}", event.getId(), e);
            }
        }
    }
}
