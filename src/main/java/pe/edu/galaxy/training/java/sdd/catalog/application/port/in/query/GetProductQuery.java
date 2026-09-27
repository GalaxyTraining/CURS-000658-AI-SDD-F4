package pe.edu.galaxy.training.java.sdd.catalog.application.port.in.query;

import java.util.UUID;

/** Input of GetProductUseCase. */
public record GetProductQuery(UUID productId) {
}
