package pe.edu.galaxy.training.java.sdd.catalog.application.port.in.usecase;

import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.query.GetProductQuery;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.result.ProductResult;

/** US05 - Consultar un producto (RF-05). */
public interface GetProductUseCase {

    ProductResult get(GetProductQuery query);
}
