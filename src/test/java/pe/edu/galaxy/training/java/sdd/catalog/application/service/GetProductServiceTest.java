package pe.edu.galaxy.training.java.sdd.catalog.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.query.GetProductQuery;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.result.ProductResult;
import pe.edu.galaxy.training.java.sdd.catalog.domain.exception.ProductNotFoundException;
import pe.edu.galaxy.training.java.sdd.catalog.domain.model.Price;
import pe.edu.galaxy.training.java.sdd.catalog.domain.model.Product;
import pe.edu.galaxy.training.java.sdd.catalog.domain.model.ProductCategory;
import pe.edu.galaxy.training.java.sdd.catalog.domain.model.Sku;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** US05 - Consultar un producto, application layer (INV-10). Plain JUnit with an in-memory port. */
class GetProductServiceTest {

    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-09-26T15:00:00Z"), ZoneOffset.UTC);

    private final InMemoryProductPersistence persistence = new InMemoryProductPersistence();
    private final GetProductService service = new GetProductService(persistence);

    private Product savedProduct() {
        return persistence.save(Product.register(Sku.of("ELE-0001"), "Wireless mouse", "Ergonomic mouse",
                Price.of(new BigDecimal("49.90")), ProductCategory.ELECTRONICS, CLOCK));
    }

    @Test
    void US05_CA01_existingId_returnsAllDataAndStatus() {
        Product product = savedProduct();

        ProductResult result = service.get(new GetProductQuery(product.getId().value()));

        assertEquals(ProductResult.from(product), result);
        assertEquals("DRAFT", result.status());
    }

    @Test
    void US05_CA01_discontinuedProduct_isStillReturned() {
        Product product = savedProduct();
        product.discontinue("The supplier stopped making it", CLOCK);
        persistence.save(product);

        ProductResult result = service.get(new GetProductQuery(product.getId().value()));

        assertEquals("DISCONTINUED", result.status());
        assertEquals("The supplier stopped making it", result.discontinuationReason());
    }

    @Test
    void US05_CA02_unknownId_isRejectedAsNotFound() {
        savedProduct();

        assertThrows(ProductNotFoundException.class,
                () -> service.get(new GetProductQuery(UUID.randomUUID())));
    }
}
