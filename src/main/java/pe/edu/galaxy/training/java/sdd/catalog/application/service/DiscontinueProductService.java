package pe.edu.galaxy.training.java.sdd.catalog.application.service;

import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.command.DiscontinueProductCommand;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.usecase.DiscontinueProductUseCase;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.result.ProductResult;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.out.ProductPersistencePort;
import pe.edu.galaxy.training.java.sdd.catalog.domain.exception.ProductNotFoundException;
import pe.edu.galaxy.training.java.sdd.catalog.domain.model.Product;
import pe.edu.galaxy.training.java.sdd.catalog.domain.model.ProductId;
import java.time.Clock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** US04 - Descontinuar producto. The rules (INV-06, INV-09) live in Product.discontinue; nothing is deleted (INV-10). */
@Service
public class DiscontinueProductService implements DiscontinueProductUseCase {

    private final ProductPersistencePort persistence;
    private final Clock clock;

    public DiscontinueProductService(ProductPersistencePort persistence, Clock clock) {
        this.persistence = persistence;
        this.clock = clock;
    }

    @Override
    @Transactional
    public ProductResult discontinue(DiscontinueProductCommand command) {
        Product product = persistence.findById(ProductId.of(command.productId()))
                .orElseThrow(() -> new ProductNotFoundException(command.productId()));
        product.discontinue(command.reason(), clock);
        return ProductResult.from(persistence.save(product));
    }
}
