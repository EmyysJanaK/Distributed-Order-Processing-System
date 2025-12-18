package com.example.orderservice.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_event")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxEvent {
    @Id
    @GeneratedValue
    private UUID id;

    private String aggregateType;
    private UUID aggregateId;
    private String type;

    @Lob
    private String payload;

    private Instant createdAt;
    private boolean published;
}
