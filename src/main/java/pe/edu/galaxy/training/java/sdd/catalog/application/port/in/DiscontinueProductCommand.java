package pe.edu.galaxy.training.java.sdd.catalog.application.port.in;

import java.util.UUID;

/** Input of DiscontinueProductUseCase. The reason is validated by the domain (INV-09). */
public record DiscontinueProductCommand(UUID productId, String reason) {
}
