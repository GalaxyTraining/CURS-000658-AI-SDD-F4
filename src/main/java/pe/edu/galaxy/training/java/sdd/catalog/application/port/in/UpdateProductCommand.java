package pe.edu.galaxy.training.java.sdd.catalog.application.port.in;

import java.math.BigDecimal;
import java.util.UUID;

/** Input of UpdateProductUseCase. A null field means "not sent" and keeps its value (INV-11); category is its code. */
public record UpdateProductCommand(UUID productId, String name, String description, BigDecimal price,
                                   String category) {
}
