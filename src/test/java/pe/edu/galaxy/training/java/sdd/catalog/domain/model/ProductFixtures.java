package pe.edu.galaxy.training.java.sdd.catalog.domain.model;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

final class ProductFixtures {

    static final Instant T0 = Instant.parse("2026-09-26T10:00:00Z");
    static final Instant T1 = Instant.parse("2026-09-26T11:00:00Z");
    static final Clock CLOCK_T0 = Clock.fixed(T0, ZoneOffset.UTC);
    static final Clock CLOCK_T1 = Clock.fixed(T1, ZoneOffset.UTC);

    static final String VALID_REASON = "The supplier stopped making it";

    private ProductFixtures() {
    }

    static Product draftWithoutPriceNorCategory() {
        return Product.register(Sku.of("ELE-0001"), "Wireless mouse", null, null, null, CLOCK_T0);
    }

    static Product completeDraft() {
        return Product.register(Sku.of("ELE-0001"), "Wireless mouse", "Ergonomic mouse",
                Price.of(new BigDecimal("49.90")), ProductCategory.ELECTRONICS, CLOCK_T0);
    }

    static Product active() {
        Product product = completeDraft();
        product.activate(CLOCK_T0);
        return product;
    }

    static Product discontinued() {
        Product product = completeDraft();
        product.discontinue(VALID_REASON, CLOCK_T0);
        return product;
    }
}
