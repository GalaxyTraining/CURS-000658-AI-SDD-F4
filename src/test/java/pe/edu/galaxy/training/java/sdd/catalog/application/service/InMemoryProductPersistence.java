package pe.edu.galaxy.training.java.sdd.catalog.application.service;

import pe.edu.galaxy.training.java.sdd.catalog.application.port.out.ProductPage;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.out.ProductPersistencePort;
import pe.edu.galaxy.training.java.sdd.catalog.domain.model.Product;
import pe.edu.galaxy.training.java.sdd.catalog.domain.model.ProductCategory;
import pe.edu.galaxy.training.java.sdd.catalog.domain.model.ProductId;
import pe.edu.galaxy.training.java.sdd.catalog.domain.model.ProductStatus;
import pe.edu.galaxy.training.java.sdd.catalog.domain.model.Sku;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Test double of the output port, shared by the service tests. */
class InMemoryProductPersistence implements ProductPersistencePort {

    private final Map<ProductId, Product> products = new LinkedHashMap<>();

    @Override
    public Product save(Product product) {
        products.put(product.getId(), product);
        return product;
    }

    @Override
    public Optional<Product> findById(ProductId id) {
        return Optional.ofNullable(products.get(id));
    }

    @Override
    public boolean existsBySku(Sku sku) {
        return products.values().stream().anyMatch(product -> product.getSku().equals(sku));
    }

    @Override
    public ProductPage search(ProductStatus status, ProductCategory category, int page, int size) {
        List<Product> matches = products.values().stream()
                .filter(product -> status == null || product.getStatus() == status)
                .filter(product -> category == null || product.getCategory() == category)
                .sorted(Comparator.comparing(Product::getCreatedAt)
                        .thenComparing(product -> product.getId().value()))
                .toList();
        List<Product> items = matches.stream().skip((long) page * size).limit(size).toList();
        return new ProductPage(items, matches.size());
    }

    int count() {
        return products.size();
    }
}
