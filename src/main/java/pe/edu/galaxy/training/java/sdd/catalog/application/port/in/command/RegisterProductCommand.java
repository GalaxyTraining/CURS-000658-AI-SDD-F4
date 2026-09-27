package pe.edu.galaxy.training.java.sdd.catalog.application.port.in.command;

import pe.edu.galaxy.training.java.sdd.catalog.domain.model.ProductCategory;
import java.math.BigDecimal;

/** Input of RegisterProductUseCase. Price and category are optional (US01 CA-06). */
public record RegisterProductCommand(String sku, String name, String description, BigDecimal price,
                                     ProductCategory category) {
}
