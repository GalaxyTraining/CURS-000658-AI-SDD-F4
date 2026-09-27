package pe.edu.galaxy.training.java.sdd.catalog.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.DiscontinueProductCommand;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.GetProductQuery;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.ProductResult;
import pe.edu.galaxy.training.java.sdd.catalog.domain.exception.InvalidStateTransitionException;
import pe.edu.galaxy.training.java.sdd.catalog.domain.exception.ProductNotFoundException;
import pe.edu.galaxy.training.java.sdd.catalog.domain.exception.ProductValidationException;
import pe.edu.galaxy.training.java.sdd.catalog.domain.model.Price;
import pe.edu.galaxy.training.java.sdd.catalog.domain.model.Product;
import pe.edu.galaxy.training.java.sdd.catalog.domain.model.ProductCategory;
import pe.edu.galaxy.training.java.sdd.catalog.domain.model.ProductStatus;
import pe.edu.galaxy.training.java.sdd.catalog.domain.model.Sku;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** US04 - Descontinuar producto, application layer (INV-06, INV-09, INV-10). Plain JUnit with an in-memory port. */
class DiscontinueProductServiceTest {

    private static final Instant T0 = Instant.parse("2026-09-26T10:00:00Z");
    private static final Instant T1 = Instant.parse("2026-09-26T11:00:00Z");
    private static final Clock CLOCK_T0 = Clock.fixed(T0, ZoneOffset.UTC);
    private static final String REASON = "The supplier stopped making it";

    private final InMemoryProductPersistence persistence = new InMemoryProductPersistence();
    private final DiscontinueProductService service =
            new DiscontinueProductService(persistence, Clock.fixed(T1, ZoneOffset.UTC));

    private Product saved(boolean active) {
        Product product = Product.register(Sku.of("ELE-0001"), "Wireless mouse", null,
                Price.of(new BigDecimal("49.90")), ProductCategory.ELECTRONICS, CLOCK_T0);
        if (active) {
            product.activate(CLOCK_T0);
        }
        return persistence.save(product);
    }

    private Product stored(Product product) {
        return persistence.findById(product.getId()).orElseThrow();
    }

    private ProductResult discontinue(Product product, String reason) {
        return service.discontinue(new DiscontinueProductCommand(product.getId().value(), reason));
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void US04_CA01_draftOrActive_becomesDiscontinuedWithReason(boolean active) {
        Product product = saved(active);

        ProductResult result = discontinue(product, "  " + REASON + "  ");

        assertEquals("DISCONTINUED", result.status());
        assertEquals(REASON, result.discontinuationReason());
        assertEquals(T1, result.updatedAt());
        assertEquals(ProductStatus.DISCONTINUED, stored(product).getStatus());
    }

    @ParameterizedTest
    @ValueSource(strings = {"          ", "Too short"})
    void US04_CA02_blankOrShortReason_isRejectedAndUnchanged(String reason) {
        Product product = saved(false);

        assertThrows(ProductValidationException.class, () -> discontinue(product, reason));
        assertEquals(ProductStatus.DRAFT, stored(product).getStatus());
        assertNull(stored(product).getDiscontinuationReason());
    }

    @Test
    void US04_CA03_alreadyDiscontinued_isRejectedAsInvalidTransition() {
        Product product = saved(false);
        discontinue(product, REASON);

        assertThrows(InvalidStateTransitionException.class, () -> discontinue(product, "Another valid reason"));
        assertEquals(REASON, stored(product).getDiscontinuationReason());
    }

    @Test
    void US04_CA04_discontinued_isStillReturnedByGetProduct() {
        Product product = saved(true);
        discontinue(product, REASON);

        ProductResult found = new GetProductService(persistence).get(new GetProductQuery(product.getId().value()));

        assertEquals("DISCONTINUED", found.status());
        assertEquals(1, persistence.count());
    }

    @Test
    void US04_unknownId_isRejectedAsNotFound() {
        assertThrows(ProductNotFoundException.class,
                () -> service.discontinue(new DiscontinueProductCommand(UUID.randomUUID(), REASON)));
    }
}
