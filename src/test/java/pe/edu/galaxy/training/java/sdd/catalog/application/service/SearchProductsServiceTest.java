package pe.edu.galaxy.training.java.sdd.catalog.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.ProductPageResult;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.ProductResult;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.SearchProductsQuery;
import pe.edu.galaxy.training.java.sdd.catalog.domain.exception.ProductValidationException;
import pe.edu.galaxy.training.java.sdd.catalog.domain.model.Price;
import pe.edu.galaxy.training.java.sdd.catalog.domain.model.Product;
import pe.edu.galaxy.training.java.sdd.catalog.domain.model.ProductCategory;
import pe.edu.galaxy.training.java.sdd.catalog.domain.model.Sku;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** US05 - Listar productos, application layer (RF-05). Plain JUnit with an in-memory port. */
class SearchProductsServiceTest {

    private static final Instant T0 = Instant.parse("2026-09-26T10:00:00Z");

    private final InMemoryProductPersistence persistence = new InMemoryProductPersistence();
    private final SearchProductsService service = new SearchProductsService(persistence);

    /** Saved in reverse creation order, to check that the result follows createdAt (RF-05). */
    @BeforeEach
    void catalog() {
        save("OFF-0003", ProductCategory.OFFICE, 3, false);
        save("ELE-0002", ProductCategory.ELECTRONICS, 2, false);
        save("ELE-0001", ProductCategory.ELECTRONICS, 1, true);
        save("HOM-0000", ProductCategory.HOME, 0, true);
    }

    private void save(String sku, ProductCategory category, int minutes, boolean active) {
        Clock clock = Clock.fixed(T0.plusSeconds(60L * minutes), ZoneOffset.UTC);
        Product product = Product.register(Sku.of(sku), "Product " + sku, null,
                Price.of(new BigDecimal("10.00")), category, clock);
        if (active) {
            product.activate(clock);
        }
        persistence.save(product);
    }

    private static List<String> skus(ProductPageResult result) {
        return result.items().stream().map(ProductResult::sku).toList();
    }

    @Test
    void US05_CA03_filterByStatus_returnsOnlyMatching() {
        ProductPageResult result = service.search(new SearchProductsQuery("ACTIVE", null, 0, 20));

        assertEquals(List.of("HOM-0000", "ELE-0001"), skus(result));
        assertEquals(2, result.totalElements());
    }

    @Test
    void US05_CA03_filterByCategory_returnsOnlyMatching() {
        ProductPageResult result = service.search(new SearchProductsQuery(null, "ELECTRONICS", 0, 20));

        assertEquals(List.of("ELE-0001", "ELE-0002"), skus(result));
    }

    @Test
    void US05_CA03_filterByStatusAndCategory_returnsOnlyMatching() {
        ProductPageResult result = service.search(new SearchProductsQuery("DRAFT", "ELECTRONICS", 0, 20));

        assertEquals(List.of("ELE-0002"), skus(result));
        assertEquals(1, result.totalElements());
    }

    @Test
    void US05_CA03_withoutFilters_returnsAllOrderedByCreation() {
        ProductPageResult result = service.search(new SearchProductsQuery(null, null, 0, 20));

        assertEquals(List.of("HOM-0000", "ELE-0001", "ELE-0002", "OFF-0003"), skus(result));
        assertEquals(1, result.totalPages());
    }

    @Test
    void US05_CA03_unknownStatusCode_isRejected() {
        assertThrows(ProductValidationException.class,
                () -> service.search(new SearchProductsQuery("ARCHIVED", null, 0, 20)));
    }

    @Test
    void US05_CA04_moreResultsThanPageSize_returnsPaginated() {
        ProductPageResult first = service.search(new SearchProductsQuery(null, null, 0, 3));
        ProductPageResult second = service.search(new SearchProductsQuery(null, null, 1, 3));

        assertEquals(List.of("HOM-0000", "ELE-0001", "ELE-0002"), skus(first));
        assertEquals(List.of("OFF-0003"), skus(second));
        assertEquals(1, second.page());
        assertEquals(3, second.size());
        assertEquals(4, second.totalElements());
        assertEquals(2, second.totalPages());
    }

    @Test
    void US05_CA04_pageBeyondLast_isEmpty() {
        ProductPageResult result = service.search(new SearchProductsQuery(null, null, 5, 3));

        assertEquals(List.of(), result.items());
        assertEquals(4, result.totalElements());
        assertEquals(2, result.totalPages());
    }

    @Test
    void US05_CA04_noMatches_hasZeroPages() {
        ProductPageResult result = service.search(new SearchProductsQuery("DISCONTINUED", null, 0, 20));

        assertEquals(0, result.totalElements());
        assertEquals(0, result.totalPages());
    }
}
