package pe.edu.galaxy.training.java.sdd.catalog.application.service;

import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.ProductResult;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.UpdateProductCommand;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.UpdateProductUseCase;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.out.ProductPersistencePort;
import pe.edu.galaxy.training.java.sdd.catalog.domain.exception.ProductNotFoundException;
import pe.edu.galaxy.training.java.sdd.catalog.domain.model.Price;
import pe.edu.galaxy.training.java.sdd.catalog.domain.model.Product;
import pe.edu.galaxy.training.java.sdd.catalog.domain.model.ProductId;
import java.time.Clock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** US02 - Actualizar producto. The rules (INV-03, INV-04, INV-05, INV-08, INV-11) live in the domain. */
@Service
public class UpdateProductService implements UpdateProductUseCase {

    private final ProductPersistencePort persistence;
    private final Clock clock;

    public UpdateProductService(ProductPersistencePort persistence, Clock clock) {
        this.persistence = persistence;
        this.clock = clock;
    }

    @Override
    @Transactional
    public ProductResult update(UpdateProductCommand command) {
        Product product = persistence.findById(ProductId.of(command.productId()))
                .orElseThrow(() -> new ProductNotFoundException(command.productId()));
        Price price = command.price() == null ? null : Price.of(command.price());
        product.update(command.name(), command.description(), price,
                ProductCategoryCodes.toCategory(command.category()), clock);
        return ProductResult.from(persistence.save(product));
    }
}
