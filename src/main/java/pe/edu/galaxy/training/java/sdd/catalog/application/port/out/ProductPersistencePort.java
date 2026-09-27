package pe.edu.galaxy.training.java.sdd.catalog.application.port.out;

import pe.edu.galaxy.training.java.sdd.catalog.domain.model.Product;
import pe.edu.galaxy.training.java.sdd.catalog.domain.model.ProductCategory;
import pe.edu.galaxy.training.java.sdd.catalog.domain.model.ProductId;
import pe.edu.galaxy.training.java.sdd.catalog.domain.model.ProductStatus;
import pe.edu.galaxy.training.java.sdd.catalog.domain.model.Sku;
import java.util.Optional;

/** What the application needs from storage (architecture.md §4). */
public interface ProductPersistencePort {

    /** Inserts or updates. A duplicated SKU is reported as DuplicateSkuException (INV-02). */
    Product save(Product product);

    Optional<Product> findById(ProductId id);

    boolean existsBySku(Sku sku);

    /** Null filters mean "no filter"; page starts at 0; ordered by createdAt, then id (RF-05). */
    ProductPage search(ProductStatus status, ProductCategory category, int page, int size);
}
