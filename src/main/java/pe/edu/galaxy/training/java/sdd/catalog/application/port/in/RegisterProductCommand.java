package pe.edu.galaxy.training.java.sdd.catalog.application.port.in;

import java.math.BigDecimal;

/** Input of RegisterProductUseCase. Price and category are optional (US01 CA-06); category is its code. */
public record RegisterProductCommand(String sku, String name, String description, BigDecimal price,
                                     String category) {
}
