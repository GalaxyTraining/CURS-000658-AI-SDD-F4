package pe.edu.galaxy.training.java.sdd.catalog.domain.exception;

import java.util.List;

/** ADR-0004: VALIDATION_ERROR (INV-01, INV-04, INV-05, INV-09). */
public class ProductValidationException extends DomainException {

    public ProductValidationException(String message) {
        this(message, List.of());
    }

    public ProductValidationException(String message, List<String> details) {
        super(message, details);
    }
}
