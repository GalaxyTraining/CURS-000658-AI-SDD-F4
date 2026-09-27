package pe.edu.galaxy.training.java.sdd.catalog.application.service;

import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.ProductPageResult;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.ProductResult;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.SearchProductsQuery;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.SearchProductsUseCase;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.out.ProductPage;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.out.ProductPersistencePort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** US05 - Listar productos. Filtering, RF-05 ordering and paging are delegated to the output port. */
@Service
public class SearchProductsService implements SearchProductsUseCase {

    private final ProductPersistencePort persistence;

    public SearchProductsService(ProductPersistencePort persistence) {
        this.persistence = persistence;
    }

    @Override
    @Transactional(readOnly = true)
    public ProductPageResult search(SearchProductsQuery query) {
        ProductPage page = persistence.search(
                ProductStatusCodes.toStatus(query.status()),
                ProductCategoryCodes.toCategory(query.category()),
                query.page(), query.size());
        int totalPages = (int) ((page.totalElements() + query.size() - 1) / query.size());
        return new ProductPageResult(
                page.items().stream().map(ProductResult::from).toList(),
                query.page(), query.size(), page.totalElements(), totalPages);
    }
}
