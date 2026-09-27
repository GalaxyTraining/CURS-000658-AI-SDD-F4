package pe.edu.galaxy.training.java.sdd.catalog.application.port.in;

/** US03 - Activar producto (RF-03). */
public interface ActivateProductUseCase {

    ProductResult activate(ActivateProductCommand command);
}
