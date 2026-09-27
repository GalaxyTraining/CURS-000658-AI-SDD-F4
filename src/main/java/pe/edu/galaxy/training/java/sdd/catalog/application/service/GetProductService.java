package pe.edu.galaxy.training.java.sdd.catalog.application.service;

import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.query.GetProductQuery;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.usecase.GetProductUseCase;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.result.ProductResult;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.out.ProductPersistencePort;
import pe.edu.galaxy.training.java.sdd.catalog.domain.exception.ProductNotFoundException;
import pe.edu.galaxy.training.java.sdd.catalog.domain.model.ProductId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** US05 - Consultar un producto. Discontinued products are returned too: nothing is deleted (INV-10). */
@Service
public class GetProductService implements GetProductUseCase {

    private final ProductPersistencePort persistence;

    public GetProductService(ProductPersistencePort persistence) {
        this.persistence = persistence;
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResult get(GetProductQuery query) {
        return persistence.findById(ProductId.of(query.productId()))
                .map(ProductResult::from)
                .orElseThrow(() -> new ProductNotFoundException(query.productId()));
    }
}
