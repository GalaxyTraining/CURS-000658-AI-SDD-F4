package pe.edu.galaxy.training.java.sdd.catalog.adapter.out.persistence;

import pe.edu.galaxy.training.java.sdd.catalog.domain.model.Price;
import pe.edu.galaxy.training.java.sdd.catalog.domain.model.Product;
import pe.edu.galaxy.training.java.sdd.catalog.domain.model.ProductId;
import pe.edu.galaxy.training.java.sdd.catalog.domain.model.Sku;
import org.springframework.stereotype.Component;

/** Product <-> ProductJpaEntity. Rebuilds with Product.restore (domain-model.md §4). */
@Component
class ProductPersistenceMapper {

    ProductJpaEntity toEntity(Product product) {
        return new ProductJpaEntity(
                product.getId().value(),
                product.getSku().value(),
                product.getName(),
                product.getDescription(),
                product.getPrice() == null ? null : product.getPrice().amount(),
                product.getCategory(),
                product.getStatus(),
                product.getDiscontinuationReason(),
                product.getCreatedAt(),
                product.getUpdatedAt());
    }

    Product toDomain(ProductJpaEntity entity) {
        return Product.restore(
                ProductId.of(entity.getId()),
                Sku.of(entity.getSku()),
                entity.getName(),
                entity.getDescription(),
                entity.getPrice() == null ? null : Price.of(entity.getPrice()),
                entity.getCategory(),
                entity.getStatus(),
                entity.getDiscontinuationReason(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
