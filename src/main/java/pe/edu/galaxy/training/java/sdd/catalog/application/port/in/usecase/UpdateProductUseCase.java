package pe.edu.galaxy.training.java.sdd.catalog.application.port.in.usecase;

import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.command.UpdateProductCommand;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.result.ProductResult;

/** US02 - Actualizar producto (RF-02). */
public interface UpdateProductUseCase {

    ProductResult update(UpdateProductCommand command);
}
