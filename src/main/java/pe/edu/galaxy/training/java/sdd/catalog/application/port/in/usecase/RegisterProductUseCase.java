package pe.edu.galaxy.training.java.sdd.catalog.application.port.in.usecase;

import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.command.RegisterProductCommand;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.result.ProductResult;

/** US01 - Registrar producto (RF-01). */
public interface RegisterProductUseCase {

    ProductResult register(RegisterProductCommand command);
}
