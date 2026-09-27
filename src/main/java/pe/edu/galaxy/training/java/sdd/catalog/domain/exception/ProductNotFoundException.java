package pe.edu.galaxy.training.java.sdd.catalog.domain.exception;

import java.util.List;
import java.util.UUID;

/** ADR-0004: PRODUCT_NOT_FOUND. */
public class ProductNotFoundException extends DomainException {

    public ProductNotFoundException(UUID productId) {
        super("Product not found: " + productId, List.of());
    }
}
