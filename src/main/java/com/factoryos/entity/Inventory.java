package com.factoryos.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(
        name = "inventory",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_inventory_product_id", columnNames = "product_id")
        },
        indexes = {
                @Index(name = "idx_inventory_product_id", columnList = "product_id")
        }
)
@org.hibernate.annotations.Check(constraints = "quantity_available >= 0 AND reserved_quantity >= 0")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    @Column(name = "quantity_available", nullable = false)
    @Builder.Default
    private Integer quantityAvailable = 0;

    @Column(name = "reserved_quantity", nullable = false)
    @Builder.Default
    private Integer reservedQuantity = 0;

    @Version
    @Column(name = "version")
    private Long version;

    @Column(name = "last_updated", nullable = false)
    private Instant lastUpdated;

    @PrePersist
    protected void onCreate() {
        if (quantityAvailable == null) {
            quantityAvailable = 0;
        }
        if (reservedQuantity == null) {
            reservedQuantity = 0;
        }
        lastUpdated = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdated = Instant.now();
    }
}

