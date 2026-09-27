package pe.edu.galaxy.training.java.sdd.catalog.domain.exception;

import java.util.List;

/** ADR-0004: PRODUCT_INCOMPLETE (INV-07). The details list the missing data (US03 CA-02). */
public class ProductIncompleteException extends DomainException {

    public ProductIncompleteException(List<String> missingFields) {
        super("Product cannot be activated, missing: " + String.join(", ", missingFields), missingFields);
    }
}
