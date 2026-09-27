package pe.edu.galaxy.training.java.sdd.catalog.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.ActivateProductCommand;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.ProductResult;
import pe.edu.galaxy.training.java.sdd.catalog.domain.exception.InvalidStateTransitionException;
import pe.edu.galaxy.training.java.sdd.catalog.domain.exception.ProductIncompleteException;
import pe.edu.galaxy.training.java.sdd.catalog.domain.exception.ProductNotFoundException;
import pe.edu.galaxy.training.java.sdd.catalog.domain.model.Price;
import pe.edu.galaxy.training.java.sdd.catalog.domain.model.Product;
import pe.edu.galaxy.training.java.sdd.catalog.domain.model.ProductCategory;
import pe.edu.galaxy.training.java.sdd.catalog.domain.model.ProductStatus;
import pe.edu.galaxy.training.java.sdd.catalog.domain.model.Sku;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** US03 - Activar producto, application layer (INV-06, INV-07). Plain JUnit with an in-memory port. */
class ActivateProductServiceTest {

    private static final Instant T0 = Instant.parse("2026-09-26T10:00:00Z");
    private static final Instant T1 = Instant.parse("2026-09-26T11:00:00Z");
    private static final Clock CLOCK_T0 = Clock.fixed(T0, ZoneOffset.UTC);

    private final InMemoryProductPersistence persistence = new InMemoryProductPersistence();
    private final ActivateProductService service =
            new ActivateProductService(persistence, Clock.fixed(T1, ZoneOffset.UTC));

    private Product saved(Price price, ProductCategory category) {
        return persistence.save(Product.register(Sku.of("ELE-0001"), "Wireless mouse", null, price, category,
                CLOCK_T0));
    }

    private Product completeDraft() {
        return saved(Price.of(new BigDecimal("49.90")), ProductCategory.ELECTRONICS);
    }

    private Product stored(Product product) {
        return persistence.findById(product.getId()).orElseThrow();
    }

    private ProductResult activate(Product product) {
        return service.activate(new ActivateProductCommand(product.getId().value()));
    }

    @Test
    void US03_CA01_completeDraft_becomesActiveAndIsSaved() {
        Product product = completeDraft();

        ProductResult result = activate(product);

        assertEquals("ACTIVE", result.status());
        assertEquals(T1, result.updatedAt());
        assertEquals(ProductStatus.ACTIVE, stored(product).getStatus());
    }

    @Test
    void US03_CA02_withoutPriceNorCategory_isRejectedIndicatingMissingAndUnchanged() {
        Product product = saved(null, null);

        ProductIncompleteException ex = assertThrows(ProductIncompleteException.class, () -> activate(product));

        assertEquals(List.of("price", "category"), ex.getDetails());
        assertEquals(ProductStatus.DRAFT, stored(product).getStatus());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void US03_CA03_activeOrDiscontinued_isRejectedAsInvalidTransition(boolean discontinued) {
        Product product = completeDraft();
        if (discontinued) {
            product.discontinue("The supplier stopped making it", CLOCK_T0);
        } else {
            product.activate(CLOCK_T0);
        }
        persistence.save(product);
        ProductStatus before = product.getStatus();

        assertThrows(InvalidStateTransitionException.class, () -> activate(product));
        assertEquals(before, stored(product).getStatus());
    }

    @Test
    void US03_unknownId_isRejectedAsNotFound() {
        assertThrows(ProductNotFoundException.class,
                () -> service.activate(new ActivateProductCommand(UUID.randomUUID())));
    }
}
