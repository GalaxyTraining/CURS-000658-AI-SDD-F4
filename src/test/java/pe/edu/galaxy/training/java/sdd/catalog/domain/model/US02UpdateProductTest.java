package pe.edu.galaxy.training.java.sdd.catalog.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static pe.edu.galaxy.training.java.sdd.catalog.domain.model.ProductFixtures.CLOCK_T1;
import static pe.edu.galaxy.training.java.sdd.catalog.domain.model.ProductFixtures.T0;
import static pe.edu.galaxy.training.java.sdd.catalog.domain.model.ProductFixtures.T1;

import pe.edu.galaxy.training.java.sdd.catalog.domain.exception.InvalidStateTransitionException;
import pe.edu.galaxy.training.java.sdd.catalog.domain.exception.ProductValidationException;
import java.math.BigDecimal;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

/** US02 - Actualizar producto. The out-of-catalog category of CA-04 is tested in web. */
class US02UpdateProductTest {

    static Stream<Supplier<Product>> draftOrActive() {
        return Stream.of(ProductFixtures::completeDraft, ProductFixtures::active);
    }

    @ParameterizedTest
    @MethodSource("draftOrActive")
    void US02_CA01_validData_isSavedAndStatusDoesNotChange(Supplier<Product> fixture) {
        Product product = fixture.get();
        ProductStatus before = product.getStatus();

        product.update("Gaming mouse", "RGB mouse", Price.of(new BigDecimal("59.90")), ProductCategory.OFFICE, CLOCK_T1);

        assertEquals("Gaming mouse", product.getName());
        assertEquals("RGB mouse", product.getDescription());
        assertEquals(new BigDecimal("59.90"), product.getPrice().amount());
        assertEquals(ProductCategory.OFFICE, product.getCategory());
        assertEquals(before, product.getStatus());
        assertEquals(T1, product.getUpdatedAt());
    }

    @Test
    void US02_CA01_onlySentFields_areModified() {
        Product product = ProductFixtures.completeDraft();

        product.update(null, null, Price.of(new BigDecimal("59.90")), null, CLOCK_T1);

        assertEquals("Wireless mouse", product.getName());
        assertEquals("Ergonomic mouse", product.getDescription());
        assertEquals(new BigDecimal("59.90"), product.getPrice().amount());
        assertEquals(ProductCategory.ELECTRONICS, product.getCategory());
    }

    @Test
    void US02_CA01_nameAndDescriptionWithSurroundingSpaces_areStoredStripped() {
        Product product = ProductFixtures.completeDraft();

        product.update("  Gaming mouse ", " RGB mouse  ", null, null, CLOCK_T1);

        assertEquals("Gaming mouse", product.getName());
        assertEquals("RGB mouse", product.getDescription());
    }

    @Test
    void US02_CA02_discontinuedProduct_isRejected() {
        Product product = ProductFixtures.discontinued();

        assertThrows(InvalidStateTransitionException.class,
                () -> product.update("Gaming mouse", null, null, null, CLOCK_T1));
        assertEquals("Wireless mouse", product.getName());
    }

    @Test
    void US02_CA03_update_neverChangesSku() {
        Product product = ProductFixtures.completeDraft();

        product.update("Gaming mouse", "RGB mouse", Price.of(new BigDecimal("59.90")), ProductCategory.OFFICE, CLOCK_T1);

        assertEquals("ELE-0001", product.getSku().value());
    }

    @Test
    void US02_CA04_invalidData_isRejectedAndProductIsUnchanged() {
        Product product = ProductFixtures.completeDraft();

        assertThrows(ProductValidationException.class,
                () -> product.update("ab", "a".repeat(501), null, ProductCategory.OFFICE, CLOCK_T1));

        assertEquals("Wireless mouse", product.getName());
        assertEquals("Ergonomic mouse", product.getDescription());
        assertEquals(ProductCategory.ELECTRONICS, product.getCategory());
        assertEquals(T0, product.getUpdatedAt());
    }

    @Test
    void US02_CA04_invalidPrice_isRejected() {
        assertThrows(ProductValidationException.class, () -> Price.of(new BigDecimal("-5")));
    }

    @Test
    void US02_CA05_emptyPriceAndCategory_keepCurrentValues() {
        Product product = ProductFixtures.completeDraft();

        product.update("Gaming mouse", null, null, null, CLOCK_T1);

        assertEquals(new BigDecimal("49.90"), product.getPrice().amount());
        assertEquals(ProductCategory.ELECTRONICS, product.getCategory());
    }
}
