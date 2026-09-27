package pe.edu.galaxy.training.java.sdd.catalog.domain.model;

import pe.edu.galaxy.training.java.sdd.catalog.domain.exception.InvalidStateTransitionException;
import pe.edu.galaxy.training.java.sdd.catalog.domain.exception.ProductIncompleteException;
import pe.edu.galaxy.training.java.sdd.catalog.domain.exception.ProductValidationException;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate root (specs/architecture/domain-model.md). There is no way to change the SKU (INV-03)
 * nor to delete a product (INV-10).
 */
public class Product {

    private static final int NAME_MIN = 3;
    private static final int NAME_MAX = 100;
    private static final int DESCRIPTION_MAX = 500;
    private static final int REASON_MIN = 10;
    private static final int REASON_MAX = 500;

    private final ProductId id;
    private final Sku sku;
    private String name;
    private String description;
    private Price price;
    private ProductCategory category;
    private ProductStatus status;
    private String discontinuationReason;
    private final Instant createdAt;
    private Instant updatedAt;

    private Product(ProductId id, Sku sku, String name, String description, Price price,
                    ProductCategory category, ProductStatus status, String discontinuationReason,
                    Instant createdAt, Instant updatedAt) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.sku = Objects.requireNonNull(sku, "sku is required");
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.status = Objects.requireNonNull(status, "status is required");
        this.discontinuationReason = discontinuationReason;
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
        this.updatedAt = Objects.requireNonNull(updatedAt, "updatedAt is required");
    }

    /** RF-01: every new product starts in DRAFT; price and category are optional (US01 CA-06). */
    public static Product register(Sku sku, String name, String description, Price price,
                                   ProductCategory category, Clock clock) {
        Objects.requireNonNull(sku, "sku is required");
        name = strip(name);
        description = strip(description);
        List<String> errors = new ArrayList<>();
        validateName(name, errors);
        validateDescription(description, errors);
        throwIfAny(errors);
        Instant now = clock.instant();
        return new Product(ProductId.newId(), sku, name, description, price, category,
                ProductStatus.DRAFT, null, now, now);
    }

    /** Rebuilds an already stored product without applying transition rules (persistence only). */
    public static Product restore(ProductId id, Sku sku, String name, String description, Price price,
                                  ProductCategory category, ProductStatus status, String discontinuationReason,
                                  Instant createdAt, Instant updatedAt) {
        return new Product(id, sku, name, description, price, category, status, discontinuationReason,
                createdAt, updatedAt);
    }

    /**
     * RF-02: a null argument means "not sent" and keeps the current value, so price and category
     * can be replaced but never removed (INV-11). Status never changes.
     */
    public void update(String name, String description, Price price, ProductCategory category, Clock clock) {
        if (status == ProductStatus.DISCONTINUED) {
            throw new InvalidStateTransitionException("A discontinued product cannot be modified",
                    List.of("status: " + status));
        }
        name = strip(name);
        description = strip(description);
        List<String> errors = new ArrayList<>();
        if (name != null) {
            validateName(name, errors);
        }
        if (description != null) {
            validateDescription(description, errors);
        }
        throwIfAny(errors);

        if (name != null) {
            this.name = name;
        }
        if (description != null) {
            this.description = description;
        }
        if (price != null) {
            this.price = price;
        }
        if (category != null) {
            this.category = category;
        }
        this.updatedAt = clock.instant();
    }

    /** RF-03: only DRAFT -> ACTIVE (INV-06), and only with price and category (INV-07). */
    public void activate(Clock clock) {
        if (status != ProductStatus.DRAFT) {
            throw new InvalidStateTransitionException("Only a DRAFT product can be activated",
                    List.of("status: " + status));
        }
        List<String> missing = new ArrayList<>();
        if (price == null) {
            missing.add("price");
        }
        if (category == null) {
            missing.add("category");
        }
        if (!missing.isEmpty()) {
            throw new ProductIncompleteException(missing);
        }
        this.status = ProductStatus.ACTIVE;
        this.updatedAt = clock.instant();
    }

    /** RF-04: DRAFT or ACTIVE -> DISCONTINUED (INV-06), with a reason of 10 to 500 characters (INV-09). */
    public void discontinue(String reason, Clock clock) {
        if (status == ProductStatus.DISCONTINUED) {
            throw new InvalidStateTransitionException("The product is already discontinued",
                    List.of("status: " + status));
        }
        reason = strip(reason);
        int length = reason == null ? 0 : reason.length();
        if (length < REASON_MIN || length > REASON_MAX) {
            throw new ProductValidationException("Invalid discontinuation reason",
                    List.of("reason: between " + REASON_MIN + " and " + REASON_MAX + " characters"));
        }
        this.discontinuationReason = reason;
        this.status = ProductStatus.DISCONTINUED;
        this.updatedAt = clock.instant();
    }

    /** Name, description and reason are validated and stored without leading/trailing spaces. */
    private static String strip(String value) {
        return value == null ? null : value.strip();
    }

    private static void validateName(String name, List<String> errors) {
        int length = name == null ? 0 : name.length();
        if (length < NAME_MIN || length > NAME_MAX) {
            errors.add("name: between " + NAME_MIN + " and " + NAME_MAX + " characters");
        }
    }

    private static void validateDescription(String description, List<String> errors) {
        if (description != null && description.length() > DESCRIPTION_MAX) {
            errors.add("description: at most " + DESCRIPTION_MAX + " characters");
        }
    }

    private static void throwIfAny(List<String> errors) {
        if (!errors.isEmpty()) {
            throw new ProductValidationException("Invalid product data", errors);
        }
    }

    public ProductId getId() {
        return id;
    }

    public Sku getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Price getPrice() {
        return price;
    }

    public ProductCategory getCategory() {
        return category;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public String getDiscontinuationReason() {
        return discontinuationReason;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
