package pe.edu.galaxy.training.java.sdd.catalog.application.port.in.result;

import pe.edu.galaxy.training.java.sdd.catalog.domain.model.Product;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Output of the use cases. Uses JDK types only, so the product never leaves the application
 * (architecture.md §3). Category and status are their codes.
 */
public record ProductResult(UUID id, String sku, String name, String description, BigDecimal price,
                            String category, String status, String discontinuationReason,
                            Instant createdAt, Instant updatedAt) {

    public static ProductResult from(Product product) {
        return new ProductResult(
                product.getId().value(),
                product.getSku().value(),
                product.getName(),
                product.getDescription(),
                product.getPrice() == null ? null : product.getPrice().amount(),
                product.getCategory() == null ? null : product.getCategory().name(),
                product.getStatus().name(),
                product.getDiscontinuationReason(),
                product.getCreatedAt(),
                product.getUpdatedAt());
    }
}
