package pe.edu.galaxy.training.java.sdd.catalog.application.service;

import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.command.ActivateProductCommand;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.usecase.ActivateProductUseCase;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.result.ProductResult;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.out.ProductPersistencePort;
import pe.edu.galaxy.training.java.sdd.catalog.domain.exception.ProductNotFoundException;
import pe.edu.galaxy.training.java.sdd.catalog.domain.model.Product;
import pe.edu.galaxy.training.java.sdd.catalog.domain.model.ProductId;
import java.time.Clock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** US03 - Activar producto. The rules (INV-06, INV-07) live in Product.activate (architecture.md §6). */
@Service
public class ActivateProductService implements ActivateProductUseCase {

    private final ProductPersistencePort persistence;
    private final Clock clock;

    public ActivateProductService(ProductPersistencePort persistence, Clock clock) {
        this.persistence = persistence;
        this.clock = clock;
    }

    @Override
    @Transactional
    public ProductResult activate(ActivateProductCommand command) {
        Product product = persistence.findById(ProductId.of(command.productId()))
                .orElseThrow(() -> new ProductNotFoundException(command.productId()));
        product.activate(clock);
        return ProductResult.from(persistence.save(product));
    }
}
