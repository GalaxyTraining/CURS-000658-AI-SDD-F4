package pe.edu.galaxy.training.java.sdd.catalog.application.port.in;

/** US02 - Actualizar producto (RF-02). */
public interface UpdateProductUseCase {

    ProductResult update(UpdateProductCommand command);
}
