package pe.edu.galaxy.training.java.sdd.catalog.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static pe.edu.galaxy.training.java.sdd.catalog.domain.model.ProductFixtures.CLOCK_T0;
import static pe.edu.galaxy.training.java.sdd.catalog.domain.model.ProductFixtures.CLOCK_T1;
import static pe.edu.galaxy.training.java.sdd.catalog.domain.model.ProductFixtures.T1;

import pe.edu.galaxy.training.java.sdd.catalog.domain.exception.InvalidStateTransitionException;
import pe.edu.galaxy.training.java.sdd.catalog.domain.exception.ProductIncompleteException;
import java.math.BigDecimal;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/** US03 - Activar producto. */
class US03ActivateProductTest {

    @Test
    void US03_CA01_draftWithPriceAndCategory_becomesActive() {
        Product product = ProductFixtures.completeDraft();

        product.activate(CLOCK_T1);

        assertEquals(ProductStatus.ACTIVE, product.getStatus());
        assertEquals(T1, product.getUpdatedAt());
    }

    @Test
    void US03_CA02_activateWithoutPrice_isRejectedIndicatingPrice() {
        Product product = Product.register(Sku.of("ELE-0001"), "Wireless mouse", null, null,
                ProductCategory.ELECTRONICS, CLOCK_T0);

        ProductIncompleteException ex = assertThrows(ProductIncompleteException.class, () -> product.activate(CLOCK_T1));

        assertEquals(List.of("price"), ex.getDetails());
        assertEquals(ProductStatus.DRAFT, product.getStatus());
    }

    @Test
    void US03_CA02_activateWithoutCategory_isRejectedIndicatingCategory() {
        Product product = Product.register(Sku.of("ELE-0001"), "Wireless mouse", null,
                Price.of(new BigDecimal("49.90")), null, CLOCK_T0);

        ProductIncompleteException ex = assertThrows(ProductIncompleteException.class, () -> product.activate(CLOCK_T1));

        assertEquals(List.of("category"), ex.getDetails());
    }

    @Test
    void US03_CA02_activateWithoutPriceNorCategory_isRejectedIndicatingBoth() {
        Product product = ProductFixtures.draftWithoutPriceNorCategory();

        ProductIncompleteException ex = assertThrows(ProductIncompleteException.class, () -> product.activate(CLOCK_T1));

        assertEquals(List.of("price", "category"), ex.getDetails());
    }

    static Stream<Supplier<Product>> activeOrDiscontinued() {
        return Stream.of(ProductFixtures::active, ProductFixtures::discontinued);
    }

    @ParameterizedTest
    @MethodSource("activeOrDiscontinued")
    void US03_CA03_activeOrDiscontinuedProduct_isRejectedAsInvalidTransition(Supplier<Product> fixture) {
        Product product = fixture.get();
        ProductStatus before = product.getStatus();

        assertThrows(InvalidStateTransitionException.class, () -> product.activate(CLOCK_T1));
        assertEquals(before, product.getStatus());
    }
}
