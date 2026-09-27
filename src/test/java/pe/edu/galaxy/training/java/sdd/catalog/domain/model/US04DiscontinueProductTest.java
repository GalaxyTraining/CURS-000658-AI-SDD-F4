package pe.edu.galaxy.training.java.sdd.catalog.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static pe.edu.galaxy.training.java.sdd.catalog.domain.model.ProductFixtures.CLOCK_T1;
import static pe.edu.galaxy.training.java.sdd.catalog.domain.model.ProductFixtures.T1;
import static pe.edu.galaxy.training.java.sdd.catalog.domain.model.ProductFixtures.VALID_REASON;

import pe.edu.galaxy.training.java.sdd.catalog.domain.exception.InvalidStateTransitionException;
import pe.edu.galaxy.training.java.sdd.catalog.domain.exception.ProductValidationException;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

/** US04 - Descontinuar producto. CA-04 (never deleted) is tested in web and persistence. */
class US04DiscontinueProductTest {

    static Stream<Supplier<Product>> draftOrActive() {
        return Stream.of(ProductFixtures::completeDraft, ProductFixtures::active);
    }

    @ParameterizedTest
    @MethodSource("draftOrActive")
    void US04_CA01_draftOrActiveWithValidReason_becomesDiscontinued(Supplier<Product> fixture) {
        Product product = fixture.get();

        product.discontinue(VALID_REASON, CLOCK_T1);

        assertEquals(ProductStatus.DISCONTINUED, product.getStatus());
        assertEquals(VALID_REASON, product.getDiscontinuationReason());
        assertEquals(T1, product.getUpdatedAt());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"too short", "          ", "   short   "})
    void US04_CA02_emptyOrShorterThanTenCharactersReason_isRejected(String reason) {
        Product product = ProductFixtures.active();

        assertThrows(ProductValidationException.class, () -> product.discontinue(reason, CLOCK_T1));
        assertEquals(ProductStatus.ACTIVE, product.getStatus());
        assertNull(product.getDiscontinuationReason());
    }

    @Test
    void US04_CA02_reasonLongerThanFiveHundredCharacters_isRejected() {
        Product product = ProductFixtures.active();

        assertThrows(ProductValidationException.class, () -> product.discontinue("a".repeat(501), CLOCK_T1));
    }

    @Test
    void US04_CA03_discontinuedProduct_isRejectedAsInvalidTransition() {
        Product product = ProductFixtures.discontinued();

        assertThrows(InvalidStateTransitionException.class, () -> product.discontinue(VALID_REASON, CLOCK_T1));
        assertEquals(ProductStatus.DISCONTINUED, product.getStatus());
    }
}
