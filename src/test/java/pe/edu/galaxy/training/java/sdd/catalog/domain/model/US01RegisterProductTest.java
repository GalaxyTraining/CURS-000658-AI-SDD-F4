package pe.edu.galaxy.training.java.sdd.catalog.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static pe.edu.galaxy.training.java.sdd.catalog.domain.model.ProductFixtures.CLOCK_T0;
import static pe.edu.galaxy.training.java.sdd.catalog.domain.model.ProductFixtures.T0;

import pe.edu.galaxy.training.java.sdd.catalog.domain.exception.ProductValidationException;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** US01 - Registrar producto. CA-02 (application) and CA-05 (web) are tested outside the domain. */
class US01RegisterProductTest {

    @Test
    void US01_CA01_validSkuAndName_createsDraftProduct() {
        Product product = Product.register(Sku.of("ELE-0001"), "Wireless mouse", null,
                Price.of(new BigDecimal("49.90")), ProductCategory.ELECTRONICS, CLOCK_T0);

        assertNotNull(product.getId());
        assertEquals("ELE-0001", product.getSku().value());
        assertEquals(ProductStatus.DRAFT, product.getStatus());
        assertEquals(T0, product.getCreatedAt());
        assertEquals(T0, product.getUpdatedAt());
    }

    @Test
    void US01_CA01_nameAndDescriptionWithSurroundingSpaces_areStoredStripped() {
        Product product = Product.register(Sku.of("ELE-0001"), "  Wireless mouse ", "  Ergonomic mouse  ",
                null, null, CLOCK_T0);

        assertEquals("Wireless mouse", product.getName());
        assertEquals("Ergonomic mouse", product.getDescription());
    }

    @ParameterizedTest
    @ValueSource(strings = {"  ab  ", "   ", "ab"})
    void US01_CA01_nameShorterThanThreeCharactersWithoutSpaces_isRejected(String name) {
        assertThrows(ProductValidationException.class,
                () -> Product.register(Sku.of("ELE-0001"), name, null, null, null, CLOCK_T0));
    }

    @Test
    void US01_CA01_nameLongerThanHundredCharacters_isRejected() {
        assertThrows(ProductValidationException.class,
                () -> Product.register(Sku.of("ELE-0001"), "a".repeat(101), null, null, null, CLOCK_T0));
    }

    @Test
    void US01_CA01_descriptionLongerThanFiveHundredCharacters_isRejected() {
        assertThrows(ProductValidationException.class,
                () -> Product.register(Sku.of("ELE-0001"), "Wireless mouse", "a".repeat(501), null, null, CLOCK_T0));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ele-01", "ele-0001", "EL-0001", "ELEC-0001", "ELE-001", "ELE0001", ""})
    void US01_CA03_skuWithInvalidFormat_isRejectedWithExpectedFormat(String sku) {
        ProductValidationException ex = assertThrows(ProductValidationException.class, () -> Sku.of(sku));
        assertTrue(ex.getDetails().getFirst().contains("AAA-9999"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "-0.01", "10.505", "0.001"})
    void US01_CA04_priceZeroNegativeOrWithMoreThanTwoDecimals_isRejected(String amount) {
        assertThrows(ProductValidationException.class, () -> Price.of(new BigDecimal(amount)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.01", "10", "10.5", "10.50", "10.500"})
    void US01_CA04_priceWithTrailingZeros_isAccepted(String amount) {
        assertEquals(new BigDecimal(amount), Price.of(new BigDecimal(amount)).amount());
    }

    @Test
    void US01_CA06_withoutPriceNorCategory_createsDraftProduct() {
        Product product = Product.register(Sku.of("ELE-0001"), "Wireless mouse", null, null, null, CLOCK_T0);

        assertEquals(ProductStatus.DRAFT, product.getStatus());
        assertNull(product.getPrice());
        assertNull(product.getCategory());
    }
}
