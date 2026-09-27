package pe.edu.galaxy.training.java.sdd.catalog.adapter.in.web;

import pe.edu.galaxy.training.java.sdd.catalog.adapter.in.web.generated.model.DiscontinueProductRequest;
import pe.edu.galaxy.training.java.sdd.catalog.adapter.in.web.generated.model.ProductCategory;
import pe.edu.galaxy.training.java.sdd.catalog.adapter.in.web.generated.model.ProductPageResponse;
import pe.edu.galaxy.training.java.sdd.catalog.adapter.in.web.generated.model.ProductResponse;
import pe.edu.galaxy.training.java.sdd.catalog.adapter.in.web.generated.model.ProductStatus;
import pe.edu.galaxy.training.java.sdd.catalog.adapter.in.web.generated.model.RegisterProductRequest;
import pe.edu.galaxy.training.java.sdd.catalog.adapter.in.web.generated.model.UpdateProductRequest;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.command.DiscontinueProductCommand;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.result.ProductPageResult;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.result.ProductResult;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.command.RegisterProductCommand;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.query.SearchProductsQuery;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.command.UpdateProductCommand;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Generated web DTO <-> Commands and Results (architecture.md §3). */
@Component
public class ProductWebMapper {

    public RegisterProductCommand toCommand(RegisterProductRequest request) {
        return new RegisterProductCommand(
                request.getSku(),
                request.getName(),
                request.getDescription(),
                request.getPrice(),
                toDomain(request.getCategory()));
    }

    /** ADR-0009: minProperties 1; properties outside the schema (e.g. sku) are ignored and do not count. */
    public UpdateProductCommand toCommand(UUID productId, UpdateProductRequest request) {
        ProductCategory category = request.getCategory();
        if (request.getName() == null && request.getDescription() == null && request.getPrice() == null
                && category == null) {
            throw new InvalidRequestException("Invalid request",
                    List.of("body: at least one of name, description, price, category is required"));
        }
        return new UpdateProductCommand(
                productId,
                request.getName(),
                request.getDescription(),
                request.getPrice(),
                toDomain(category));
    }

    public DiscontinueProductCommand toCommand(UUID productId, DiscontinueProductRequest request) {
        return new DiscontinueProductCommand(productId, request.getReason());
    }

    public SearchProductsQuery toQuery(ProductStatus status, ProductCategory category, int page, int size) {
        return new SearchProductsQuery(toDomain(status), toDomain(category), page, size);
    }

    public ProductPageResponse toPageResponse(ProductPageResult result) {
        return new ProductPageResponse(
                result.items().stream().map(this::toResponse).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages());
    }

    public ProductResponse toResponse(ProductResult result) {
        return new ProductResponse(
                result.id(),
                result.sku(),
                result.name(),
                ProductStatus.fromValue(result.status()),
                result.createdAt().atOffset(ZoneOffset.UTC),
                result.updatedAt().atOffset(ZoneOffset.UTC))
                .description(result.description())
                .price(result.price())
                .category(result.category() == null ? null : ProductCategory.fromValue(result.category()))
                .discontinuationReason(result.discontinuationReason());
    }

    /** Safe because spec_lint.py guarantees both enums have the same values (architecture.md §3). */
    private static pe.edu.galaxy.training.java.sdd.catalog.domain.model.ProductCategory toDomain(
            ProductCategory category) {
        return category == null ? null
                : pe.edu.galaxy.training.java.sdd.catalog.domain.model.ProductCategory.valueOf(category.name());
    }

    private static pe.edu.galaxy.training.java.sdd.catalog.domain.model.ProductStatus toDomain(
            ProductStatus status) {
        return status == null ? null
                : pe.edu.galaxy.training.java.sdd.catalog.domain.model.ProductStatus.valueOf(status.name());
    }
}
