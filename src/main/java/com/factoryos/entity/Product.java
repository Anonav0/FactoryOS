package com.factoryos.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        name = "products",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_product_sku", columnNames = "sku")
        },
        indexes = {
                @Index(name = "idx_product_sku", columnList = "sku"),
                @Index(name = "idx_product_name", columnList = "name"),
                @Index(name = "idx_product_active", columnList = "active")
        }
)
@org.hibernate.annotations.Check(constraints = "unit_price >= 0 AND reorder_level >= 0")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sku", nullable = false, unique = true, updatable = false, length = 50)
    private String sku;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "category", nullable = false, length = 100)
    private String category;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "reorder_level", nullable = false)
    private Integer reorderLevel;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        if (sku != null) {
            sku = sku.trim().toUpperCase();
        }
        if (active == null) {
            active = true;
        }
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}

