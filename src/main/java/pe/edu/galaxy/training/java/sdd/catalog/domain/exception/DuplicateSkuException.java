package pe.edu.galaxy.training.java.sdd.catalog.domain.exception;

import java.util.List;

/** ADR-0004: DUPLICATE_SKU (INV-02). */
public class DuplicateSkuException extends DomainException {

    public DuplicateSkuException(String message) {
        this(message, List.of());
    }

    public DuplicateSkuException(String message, List<String> details) {
        super(message, details);
    }
}
