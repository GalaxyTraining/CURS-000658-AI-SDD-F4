package pe.edu.galaxy.training.java.sdd.catalog.adapter.in.web;

import pe.edu.galaxy.training.java.sdd.catalog.adapter.in.web.generated.model.DiscontinueProductRequest;
import pe.edu.galaxy.training.java.sdd.catalog.adapter.in.web.generated.model.ProductCategory;
import pe.edu.galaxy.training.java.sdd.catalog.adapter.in.web.generated.model.ProductPageResponse;
import pe.edu.galaxy.training.java.sdd.catalog.adapter.in.web.generated.model.ProductResponse;
import pe.edu.galaxy.training.java.sdd.catalog.adapter.in.web.generated.model.ProductStatus;
import pe.edu.galaxy.training.java.sdd.catalog.adapter.in.web.generated.model.RegisterProductRequest;
import pe.edu.galaxy.training.java.sdd.catalog.adapter.in.web.generated.model.UpdateProductRequest;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.DiscontinueProductCommand;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.ProductPageResult;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.ProductResult;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.RegisterProductCommand;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.SearchProductsQuery;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.UpdateProductCommand;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

/** Generated web DTO <-> Commands and Results (architecture.md §3). */
@Component
public class ProductWebMapper {

    public RegisterProductCommand toCommand(RegisterProductRequest request) {
        ProductCategory category = request.getCategory();
        return new RegisterProductCommand(
                request.getSku(),
                request.getName(),
                request.getDescription(),
                request.getPrice(),
                category == null ? null : category.getValue());
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
                category == null ? null : category.getValue());
    }

    public DiscontinueProductCommand toCommand(UUID productId, DiscontinueProductRequest request) {
        return new DiscontinueProductCommand(productId, request.getReason());
    }

    public SearchProductsQuery toQuery(ProductStatus status, ProductCategory category, int page, int size) {
        return new SearchProductsQuery(
                status == null ? null : status.getValue(),
                category == null ? null : category.getValue(),
                page, size);
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
}
