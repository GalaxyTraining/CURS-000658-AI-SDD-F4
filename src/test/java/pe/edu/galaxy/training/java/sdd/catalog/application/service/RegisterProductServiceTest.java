package pe.edu.galaxy.training.java.sdd.catalog.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.ProductResult;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.RegisterProductCommand;
import pe.edu.galaxy.training.java.sdd.catalog.domain.exception.DuplicateSkuException;
import pe.edu.galaxy.training.java.sdd.catalog.domain.exception.ProductValidationException;
import pe.edu.galaxy.training.java.sdd.catalog.domain.model.ProductId;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

/** US01 - Registrar producto, application layer (INV-02). Plain JUnit with an in-memory port. */
class RegisterProductServiceTest {

    private static final Instant NOW = Instant.parse("2026-09-26T15:00:00Z");

    private final InMemoryProductPersistence persistence = new InMemoryProductPersistence();
    private final RegisterProductService service =
            new RegisterProductService(persistence, Clock.fixed(NOW, ZoneOffset.UTC));

    @Test
    void US01_CA01_validCommand_isSavedAsDraftWithClockTime() {
        ProductResult result = service.register(new RegisterProductCommand(
                "ELE-0001", "Wireless mouse", "Ergonomic mouse", new BigDecimal("49.90"), "ELECTRONICS"));

        assertEquals("DRAFT", result.status());
        assertEquals("ELE-0001", result.sku());
        assertEquals("ELECTRONICS", result.category());
        assertEquals(NOW, result.createdAt());
        assertEquals(NOW, result.updatedAt());
        assertTrue(persistence.findById(ProductId.of(result.id())).isPresent());
    }

    @Test
    void US01_CA02_existingSku_isRejectedAsDuplicate() {
        service.register(new RegisterProductCommand("ELE-0001", "Wireless mouse", null, null, null));

        assertThrows(DuplicateSkuException.class,
                () -> service.register(new RegisterProductCommand("ELE-0001", "Another mouse", null, null, null)));
        assertEquals(1, persistence.count());
    }

    @Test
    void US01_CA05_categoryOutsideCatalog_isRejected() {
        assertThrows(ProductValidationException.class,
                () -> service.register(new RegisterProductCommand("ELE-0001", "Wireless mouse", null, null, "TOYS")));
        assertEquals(0, persistence.count());
    }

    @Test
    void US01_CA06_withoutPriceNorCategory_isSavedAsDraft() {
        ProductResult result = service.register(new RegisterProductCommand("OFF-0010", "Stapler", null, null, null));

        assertEquals("DRAFT", result.status());
        assertNull(result.price());
        assertNull(result.category());
        assertEquals(1, persistence.count());
    }
}
