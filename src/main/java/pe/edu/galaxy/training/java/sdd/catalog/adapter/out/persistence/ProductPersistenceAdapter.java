package pe.edu.galaxy.training.java.sdd.catalog.adapter.out.persistence;

import pe.edu.galaxy.training.java.sdd.catalog.application.port.out.ProductPage;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.out.ProductPersistencePort;
import pe.edu.galaxy.training.java.sdd.catalog.domain.exception.DuplicateSkuException;
import pe.edu.galaxy.training.java.sdd.catalog.domain.model.Product;
import pe.edu.galaxy.training.java.sdd.catalog.domain.model.ProductCategory;
import pe.edu.galaxy.training.java.sdd.catalog.domain.model.ProductId;
import pe.edu.galaxy.training.java.sdd.catalog.domain.model.ProductStatus;
import pe.edu.galaxy.training.java.sdd.catalog.domain.model.Sku;
import java.util.Locale;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

/** Implements ProductPersistencePort with JPA over H2 (ADR-0005). */
@Component
class ProductPersistenceAdapter implements ProductPersistencePort {

    /** RF-05: stable paging, by creation date and then by id. */
    private static final Sort ORDER = Sort.by("createdAt").ascending().and(Sort.by("id").ascending());

    private final SpringDataProductJpa repository;
    private final ProductPersistenceMapper mapper;

    ProductPersistenceAdapter(SpringDataProductJpa repository, ProductPersistenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Product save(Product product) {
        try {
            return mapper.toDomain(repository.saveAndFlush(mapper.toEntity(product)));
        } catch (DataIntegrityViolationException e) {
            if (isUniqueSkuViolation(e)) {
                throw new DuplicateSkuException("A product with SKU " + product.getSku().value() + " already exists");
            }
            throw e;
        }
    }

    @Override
    public Optional<Product> findById(ProductId id) {
        return repository.findById(id.value()).map(mapper::toDomain);
    }

    @Override
    public boolean existsBySku(Sku sku) {
        return repository.existsBySku(sku.value());
    }

    @Override
    public ProductPage search(ProductStatus status, ProductCategory category, int page, int size) {
        Page<ProductJpaEntity> result = repository.search(status, category, PageRequest.of(page, size, ORDER));
        return new ProductPage(result.map(mapper::toDomain).getContent(), result.getTotalElements());
    }

    /** INV-02 under concurrency: the database constraint is the last guard (domain-model.md). */
    private static boolean isUniqueSkuViolation(DataIntegrityViolationException e) {
        String message = e.getMostSpecificCause().getMessage();
        return message != null && message.toLowerCase(Locale.ROOT).contains(ProductJpaEntity.UNIQUE_SKU);
    }
}
