package pe.edu.galaxy.training.java.sdd.catalog.application.port.in.query;

import pe.edu.galaxy.training.java.sdd.catalog.domain.model.ProductCategory;
import pe.edu.galaxy.training.java.sdd.catalog.domain.model.ProductStatus;

/** Input of SearchProductsUseCase. Null status or category means "no filter". Page starts at 0. */
public record SearchProductsQuery(ProductStatus status, ProductCategory category, int page, int size) {
}
