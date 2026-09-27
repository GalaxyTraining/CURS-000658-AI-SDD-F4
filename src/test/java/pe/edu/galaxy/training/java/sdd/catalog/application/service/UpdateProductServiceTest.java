package pe.edu.galaxy.training.java.sdd.catalog.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.ProductResult;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.UpdateProductCommand;
import pe.edu.galaxy.training.java.sdd.catalog.domain.exception.InvalidStateTransitionException;
import pe.edu.galaxy.training.java.sdd.catalog.domain.exception.ProductNotFoundException;
import pe.edu.galaxy.training.java.sdd.catalog.domain.exception.ProductValidationException;
import pe.edu.galaxy.training.java.sdd.catalog.domain.model.Price;
import pe.edu.galaxy.training.java.sdd.catalog.domain.model.Product;
import pe.edu.galaxy.training.java.sdd.catalog.domain.model.ProductCategory;
import pe.edu.galaxy.training.java.sdd.catalog.domain.model.ProductId;
import pe.edu.galaxy.training.java.sdd.catalog.domain.model.Sku;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** US02 - Actualizar producto, application layer. Plain JUnit with an in-memory port. */
class UpdateProductServiceTest {

    private static final Instant T0 = Instant.parse("2026-09-26T10:00:00Z");
    private static final Instant T1 = Instant.parse("2026-09-26T11:00:00Z");
    private static final Clock CLOCK_T0 = Clock.fixed(T0, ZoneOffset.UTC);

    private final InMemoryProductPersistence persistence = new InMemoryProductPersistence();
    private final UpdateProductService service =
            new UpdateProductService(persistence, Clock.fixed(T1, ZoneOffset.UTC));

    private Product saved(boolean active) {
        Product product = Product.register(Sku.of("ELE-0001"), "Wireless mouse", "Ergonomic mouse",
                Price.of(new BigDecimal("49.90")), ProductCategory.ELECTRONICS, CLOCK_T0);
        if (active) {
            product.activate(CLOCK_T0);
        }
        return persistence.save(product);
    }

    private Product stored(Product product) {
        return persistence.findById(product.getId()).orElseThrow();
    }

    private static UpdateProductCommand command(ProductId id, String name, BigDecimal price, String category) {
        return new UpdateProductCommand(id.value(), name, null, price, category);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void US02_CA01_validData_isSavedAndStatusDoesNotChange(boolean active) {
        Product product = saved(active);
        String statusBefore = product.getStatus().name();

        ProductResult result = service.update(
                command(product.getId(), "Gaming mouse", new BigDecimal("59.90"), "OFFICE"));

        assertEquals("Gaming mouse", result.name());
        assertEquals(new BigDecimal("59.90"), result.price());
        assertEquals("OFFICE", result.category());
        assertEquals("Ergonomic mouse", result.description());
        assertEquals(statusBefore, result.status());
        assertEquals(T1, result.updatedAt());
        assertEquals("Gaming mouse", stored(product).getName());
    }

    @Test
    void US02_CA02_discontinued_isRejected() {
        Product product = saved(false);
        product.discontinue("The supplier stopped making it", CLOCK_T0);
        persistence.save(product);

        assertThrows(InvalidStateTransitionException.class,
                () -> service.update(command(product.getId(), "Gaming mouse", null, null)));
        assertEquals("Wireless mouse", stored(product).getName());
    }

    @Test
    void US02_CA03_skuIsNeverChanged() {
        Product product = saved(false);

        ProductResult result = service.update(command(product.getId(), "Gaming mouse", null, null));

        assertEquals("ELE-0001", result.sku());
    }

    @Test
    void US02_CA04_invalidPrice_isRejectedAndUnchanged() {
        Product product = saved(false);

        assertThrows(ProductValidationException.class,
                () -> service.update(command(product.getId(), "Gaming mouse", new BigDecimal("10.505"), null)));
        assertEquals("Wireless mouse", stored(product).getName());
        assertEquals(new BigDecimal("49.90"), stored(product).getPrice().amount());
        assertEquals(T0, stored(product).getUpdatedAt());
    }

    @Test
    void US02_CA04_categoryOutsideCatalog_isRejectedAndUnchanged() {
        Product product = saved(false);

        assertThrows(ProductValidationException.class,
                () -> service.update(command(product.getId(), null, null, "TOYS")));
        assertEquals(ProductCategory.ELECTRONICS, stored(product).getCategory());
    }

    @Test
    void US02_CA05_notSentPriceAndCategory_keepCurrentValues() {
        Product product = saved(false);

        ProductResult result = service.update(command(product.getId(), "Gaming mouse", null, null));

        assertEquals(new BigDecimal("49.90"), result.price());
        assertEquals("ELECTRONICS", result.category());
    }

    @Test
    void US02_unknownId_isRejectedAsNotFound() {
        assertThrows(ProductNotFoundException.class,
                () -> service.update(command(ProductId.of(UUID.randomUUID()), "Gaming mouse", null, null)));
    }
}
