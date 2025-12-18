package com.example.orderservice.service;

import com.example.orderservice.model.OrderCreatedEvent;
import com.example.orderservice.event.OrderEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

@Component
@RequiredArgsConstructor
public class OrderDomainEventPublisher {
    private final OrderEventPublisher orderEventPublisher;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        orderEventPublisher.publishOrderCreatedEvent(event);
    }
}
