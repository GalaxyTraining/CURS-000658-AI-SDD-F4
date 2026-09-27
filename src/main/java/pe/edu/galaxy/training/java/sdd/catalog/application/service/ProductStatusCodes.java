package pe.edu.galaxy.training.java.sdd.catalog.application.service;

import pe.edu.galaxy.training.java.sdd.catalog.domain.exception.ProductValidationException;
import pe.edu.galaxy.training.java.sdd.catalog.domain.model.ProductStatus;
import java.util.Arrays;
import java.util.List;

/** Converts a status code to the lifecycle states (specs/domain/product-lifecycle.md). */
final class ProductStatusCodes {

    private ProductStatusCodes() {
    }

    static ProductStatus toStatus(String code) {
        if (code == null) {
            return null;
        }
        return Arrays.stream(ProductStatus.values())
                .filter(status -> status.name().equals(code))
                .findFirst()
                .orElseThrow(() -> new ProductValidationException("Invalid status",
                        List.of("status: must be one of " + Arrays.toString(ProductStatus.values()))));
    }
}
