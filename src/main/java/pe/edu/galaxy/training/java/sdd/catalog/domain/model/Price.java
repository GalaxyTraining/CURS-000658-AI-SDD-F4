package pe.edu.galaxy.training.java.sdd.catalog.domain.model;

import pe.edu.galaxy.training.java.sdd.catalog.domain.exception.ProductValidationException;
import java.math.BigDecimal;
import java.util.List;

/** Amount in PEN, greater than 0 and with at most 2 decimals ignoring trailing zeros (INV-05). */
public record Price(BigDecimal amount) {

    private static final int MAX_DECIMALS = 2;

    public Price {
        if (amount == null || amount.signum() <= 0) {
            throw new ProductValidationException("Invalid price", List.of("price: must be greater than 0"));
        }
        if (amount.stripTrailingZeros().scale() > MAX_DECIMALS) {
            throw new ProductValidationException("Invalid price", List.of("price: at most 2 decimals"));
        }
    }

    public static Price of(BigDecimal amount) {
        return new Price(amount);
    }
}
