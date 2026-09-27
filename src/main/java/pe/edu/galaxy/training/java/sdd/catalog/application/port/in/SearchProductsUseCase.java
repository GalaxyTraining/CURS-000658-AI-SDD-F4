package pe.edu.galaxy.training.java.sdd.catalog.application.port.in;

/** US05 - Listar productos con filtros y paginación (RF-05). */
public interface SearchProductsUseCase {

    ProductPageResult search(SearchProductsQuery query);
}
