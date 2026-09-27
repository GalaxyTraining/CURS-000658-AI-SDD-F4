package pe.edu.galaxy.training.java.sdd.catalog.adapter.out.persistence;

import pe.edu.galaxy.training.java.sdd.catalog.domain.model.ProductCategory;
import pe.edu.galaxy.training.java.sdd.catalog.domain.model.ProductStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Table shape of a product (ADR-0005). The unique SKU constraint backs INV-02. */
@Entity
@Table(name = "products",
        uniqueConstraints = @UniqueConstraint(name = ProductJpaEntity.UNIQUE_SKU, columnNames = "sku"))
public class ProductJpaEntity {

    static final String UNIQUE_SKU = "uk_products_sku";

    @Id
    private UUID id;

    @Column(nullable = false, length = 8)
    private String sku;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(precision = 19, scale = 2)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ProductCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status;

    @Column(length = 500)
    private String discontinuationReason;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    protected ProductJpaEntity() {
    }

    ProductJpaEntity(UUID id, String sku, String name, String description, BigDecimal price,
                     ProductCategory category, ProductStatus status, String discontinuationReason,
                     Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.status = status;
        this.discontinuationReason = discontinuationReason;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    UUID getId() {
        return id;
    }

    String getSku() {
        return sku;
    }

    String getName() {
        return name;
    }

    String getDescription() {
        return description;
    }

    BigDecimal getPrice() {
        return price;
    }

    ProductCategory getCategory() {
        return category;
    }

    ProductStatus getStatus() {
        return status;
    }

    String getDiscontinuationReason() {
        return discontinuationReason;
    }

    Instant getCreatedAt() {
        return createdAt;
    }

    Instant getUpdatedAt() {
        return updatedAt;
    }
}
