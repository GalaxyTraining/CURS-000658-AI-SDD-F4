package pe.edu.galaxy.training.java.sdd.catalog.application.port.in;

/** US04 - Descontinuar producto (RF-04). */
public interface DiscontinueProductUseCase {

    ProductResult discontinue(DiscontinueProductCommand command);
}
