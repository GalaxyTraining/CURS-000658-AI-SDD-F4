package pe.edu.galaxy.training.java.sdd.catalog.application.port.in.usecase;

import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.command.ActivateProductCommand;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.result.ProductResult;

/** US03 - Activar producto (RF-03). */
public interface ActivateProductUseCase {

    ProductResult activate(ActivateProductCommand command);
}
