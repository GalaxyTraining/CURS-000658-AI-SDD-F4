package pe.edu.galaxy.training.java.sdd.catalog.application.port.in;

/** US05 - Consultar un producto (RF-05). */
public interface GetProductUseCase {

    ProductResult get(GetProductQuery query);
}
