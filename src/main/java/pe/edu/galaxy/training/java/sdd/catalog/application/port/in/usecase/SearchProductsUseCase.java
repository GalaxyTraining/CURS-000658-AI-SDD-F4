package pe.edu.galaxy.training.java.sdd.catalog.application.port.in.usecase;

import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.query.SearchProductsQuery;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.result.ProductPageResult;

/** US05 - Listar productos con filtros y paginación (RF-05). */
public interface SearchProductsUseCase {

    ProductPageResult search(SearchProductsQuery query);
}
