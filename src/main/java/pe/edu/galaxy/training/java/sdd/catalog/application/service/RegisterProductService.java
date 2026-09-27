package pe.edu.galaxy.training.java.sdd.catalog.application.service;

import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.result.ProductResult;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.command.RegisterProductCommand;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.usecase.RegisterProductUseCase;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.out.ProductPersistencePort;
import pe.edu.galaxy.training.java.sdd.catalog.domain.exception.DuplicateSkuException;
import pe.edu.galaxy.training.java.sdd.catalog.domain.model.Price;
import pe.edu.galaxy.training.java.sdd.catalog.domain.model.Product;
import pe.edu.galaxy.training.java.sdd.catalog.domain.model.Sku;
import java.time.Clock;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** US01 - Registrar producto. Checks SKU uniqueness (INV-02); the other rules live in the domain. */
@Service
public class RegisterProductService implements RegisterProductUseCase {

    private final ProductPersistencePort persistence;
    private final Clock clock;

    public RegisterProductService(ProductPersistencePort persistence, Clock clock) {
        this.persistence = persistence;
        this.clock = clock;
    }

    @Override
    @Transactional
    public ProductResult register(RegisterProductCommand command) {
        Sku sku = Sku.of(command.sku());
        if (persistence.existsBySku(sku)) {
            throw new DuplicateSkuException("A product with SKU " + sku.value() + " already exists");
        }
        Price price = command.price() == null ? null : Price.of(command.price());
        Product product = Product.register(sku, command.name(), command.description(), price,
                command.category(), clock);
        return ProductResult.from(persistence.save(product));
    }
}
