package pe.edu.galaxy.training.java.sdd.catalog.application.port.in;

/** US01 - Registrar producto (RF-01). */
public interface RegisterProductUseCase {

    ProductResult register(RegisterProductCommand command);
}
