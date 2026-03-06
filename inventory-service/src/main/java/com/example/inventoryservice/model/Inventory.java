package com.example.inventoryservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "inventory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String productCode;

    @Column(nullable = false)
    private String productName;

    @Column(length = 500)
    private String description;

    @Column(precision = 10, scale = 2)
    private BigDecimal unitPrice;

    @Builder.Default // Prevents nulls when building a new object
    @Column(nullable = false)
    private Integer quantityAvailable = 0;

    @Builder.Default
    @Column(nullable = false)
    private Integer quantityReserved = 0;

    @Version
    private Long version;
}

