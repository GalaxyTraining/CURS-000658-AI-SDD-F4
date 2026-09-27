package pe.edu.galaxy.training.java.sdd.catalog.adapter.in.web;

import pe.edu.galaxy.training.java.sdd.catalog.adapter.in.web.generated.model.ProductCategory;
import pe.edu.galaxy.training.java.sdd.catalog.adapter.in.web.generated.model.ProductResponse;
import pe.edu.galaxy.training.java.sdd.catalog.adapter.in.web.generated.model.ProductStatus;
import pe.edu.galaxy.training.java.sdd.catalog.adapter.in.web.generated.model.RegisterProductRequest;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.ProductResult;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.RegisterProductCommand;
import java.time.ZoneOffset;
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
