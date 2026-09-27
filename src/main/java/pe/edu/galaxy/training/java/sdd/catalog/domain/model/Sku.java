package pe.edu.galaxy.training.java.sdd.catalog.domain.model;

import pe.edu.galaxy.training.java.sdd.catalog.domain.exception.ProductValidationException;
import java.util.List;
import java.util.regex.Pattern;

/** Business code, format AAA-9999 (INV-01). Immutable (INV-03). */
public record Sku(String value) {

    private static final Pattern FORMAT = Pattern.compile("^[A-Z]{3}-\\d{4}$");

    public Sku {
        if (value == null || !FORMAT.matcher(value).matches()) {
            throw new ProductValidationException("Invalid SKU format",
                    List.of("sku: expected format AAA-9999 (e.g. ELE-0001)"));
        }
    }

    public static Sku of(String value) {
        return new Sku(value);
    }
}
