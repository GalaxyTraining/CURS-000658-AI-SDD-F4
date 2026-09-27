package pe.edu.galaxy.training.java.sdd.catalog.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import pe.edu.galaxy.training.java.sdd.catalog.application.port.out.ProductPage;
import pe.edu.galaxy.training.java.sdd.catalog.domain.exception.DuplicateSkuException;
import pe.edu.galaxy.training.java.sdd.catalog.domain.model.Price;
import pe.edu.galaxy.training.java.sdd.catalog.domain.model.Product;
import pe.edu.galaxy.training.java.sdd.catalog.domain.model.ProductCategory;
import pe.edu.galaxy.training.java.sdd.catalog.domain.model.ProductId;
import pe.edu.galaxy.training.java.sdd.catalog.domain.model.ProductStatus;
import pe.edu.galaxy.training.java.sdd.catalog.domain.model.Sku;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

/** ProductPersistencePort over H2 (ADR-0005). */
@DataJpaTest
@Import({ProductPersistenceAdapter.class, ProductPersistenceMapper.class})
class ProductPersistenceAdapterTest {

    private static final Instant T0 = Instant.parse("2026-09-26T10:00:00Z");

    @Autowired
    private ProductPersistenceAdapter adapter;

    private static Clock at(int minutes) {
        return Clock.fixed(T0.plusSeconds(60L * minutes), ZoneOffset.UTC);
    }

    private static Product draft(String sku, ProductCategory category, int minute) {
        Price price = category == null ? null : Price.of(new BigDecimal("49.90"));
        return Product.register(Sku.of(sku), "Product " + sku, null, price, category, at(minute));
    }

    private static Product active(String sku, ProductCategory category, int minute) {
        Product product = draft(sku, category, minute);
        product.activate(at(minute));
        return product;
    }

    @Test
    void save_thenFindById_restoresAllFields() {
        Product product = Product.register(Sku.of("ELE-0001"), "Wireless mouse", "Ergonomic mouse",
                Price.of(new BigDecimal("49.90")), ProductCategory.ELECTRONICS, at(0));

        adapter.save(product);
        Product found = adapter.findById(product.getId()).orElseThrow();

        assertThat(found.getId()).isEqualTo(product.getId());
        assertThat(found.getSku()).isEqualTo(product.getSku());
        assertThat(found.getName()).isEqualTo("Wireless mouse");
        assertThat(found.getDescription()).isEqualTo("Ergonomic mouse");
        assertThat(found.getPrice().amount()).isEqualByComparingTo("49.90");
        assertThat(found.getCategory()).isEqualTo(ProductCategory.ELECTRONICS);
        assertThat(found.getStatus()).isEqualTo(ProductStatus.DRAFT);
        assertThat(found.getDiscontinuationReason()).isNull();
        assertThat(found.getCreatedAt()).isEqualTo(product.getCreatedAt());
        assertThat(found.getUpdatedAt()).isEqualTo(product.getUpdatedAt());
    }

    @Test
    void findById_unknownId_isEmpty() {
        assertThat(adapter.findById(ProductId.of(UUID.randomUUID()))).isEmpty();
    }

    @Test
    void existsBySku_reflectsStoredProducts() {
        adapter.save(draft("ELE-0001", null, 0));

        assertThat(adapter.existsBySku(Sku.of("ELE-0001"))).isTrue();
        assertThat(adapter.existsBySku(Sku.of("ELE-0002"))).isFalse();
    }

    @Test
    void US01_CA02_duplicateSkuAtDatabase_isTranslatedToDuplicateSku() {
        adapter.save(draft("ELE-0001", null, 0));

        assertThatThrownBy(() -> adapter.save(draft("ELE-0001", null, 1)))
                .isInstanceOf(DuplicateSkuException.class);
    }

    @Test
    void US05_CA03_searchByStatusAndCategory_returnsOnlyMatches() {
        Product match = active("ELE-0001", ProductCategory.ELECTRONICS, 0);
        adapter.save(match);
        adapter.save(draft("ELE-0002", ProductCategory.ELECTRONICS, 1));
        adapter.save(active("HOM-0001", ProductCategory.HOME, 2));

        ProductPage both = adapter.search(ProductStatus.ACTIVE, ProductCategory.ELECTRONICS, 0, 20);
        ProductPage byStatus = adapter.search(ProductStatus.ACTIVE, null, 0, 20);
        ProductPage noFilter = adapter.search(null, null, 0, 20);

        assertThat(both.items()).extracting(Product::getId).containsExactly(match.getId());
        assertThat(byStatus.totalElements()).isEqualTo(2);
        assertThat(noFilter.totalElements()).isEqualTo(3);
    }

    @Test
    void US05_CA04_search_isPagedAndOrderedByCreatedAtThenId() {
        adapter.save(draft("ELE-0003", null, 2));
        adapter.save(draft("ELE-0001", null, 0));
        adapter.save(draft("ELE-0002", null, 1));

        ProductPage first = adapter.search(null, null, 0, 2);
        ProductPage second = adapter.search(null, null, 1, 2);

        assertThat(first.items()).extracting(product -> product.getSku().value())
                .containsExactly("ELE-0001", "ELE-0002");
        assertThat(second.items()).extracting(product -> product.getSku().value())
                .containsExactly("ELE-0003");
        assertThat(first.totalElements()).isEqualTo(3);
    }
}
