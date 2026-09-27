package pe.edu.galaxy.training.java.sdd.catalog.application.port.in.command;

import java.util.UUID;

/** Input of ActivateProductUseCase. */
public record ActivateProductCommand(UUID productId) {
}
