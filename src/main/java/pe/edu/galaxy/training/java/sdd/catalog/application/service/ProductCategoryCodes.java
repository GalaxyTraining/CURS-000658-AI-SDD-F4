package pe.edu.galaxy.training.java.sdd.catalog.application.service;

import pe.edu.galaxy.training.java.sdd.catalog.domain.exception.ProductValidationException;
import pe.edu.galaxy.training.java.sdd.catalog.domain.model.ProductCategory;
import java.util.Arrays;
import java.util.List;

/** Converts a category code to the closed catalog (specs/domain/catalogs.md). */
final class ProductCategoryCodes {

    private ProductCategoryCodes() {
    }

    static ProductCategory toCategory(String code) {
        if (code == null) {
            return null;
        }
        return Arrays.stream(ProductCategory.values())
                .filter(category -> category.name().equals(code))
                .findFirst()
                .orElseThrow(() -> new ProductValidationException("Invalid category",
                        List.of("category: must be one of " + Arrays.toString(ProductCategory.values()))));
    }
}
