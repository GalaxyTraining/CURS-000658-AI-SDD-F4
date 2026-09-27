package pe.edu.galaxy.training.java.sdd.catalog.adapter.in.web;

import pe.edu.galaxy.training.java.sdd.catalog.adapter.in.web.generated.ProductsApi;
import pe.edu.galaxy.training.java.sdd.catalog.adapter.in.web.generated.model.DiscontinueProductRequest;
import pe.edu.galaxy.training.java.sdd.catalog.adapter.in.web.generated.model.ProductCategory;
import pe.edu.galaxy.training.java.sdd.catalog.adapter.in.web.generated.model.ProductPageResponse;
import pe.edu.galaxy.training.java.sdd.catalog.adapter.in.web.generated.model.ProductResponse;
import pe.edu.galaxy.training.java.sdd.catalog.adapter.in.web.generated.model.ProductStatus;
import pe.edu.galaxy.training.java.sdd.catalog.adapter.in.web.generated.model.RegisterProductRequest;
import pe.edu.galaxy.training.java.sdd.catalog.adapter.in.web.generated.model.UpdateProductRequest;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.GetProductQuery;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.GetProductUseCase;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.ProductPageResult;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.ProductResult;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.RegisterProductUseCase;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.SearchProductsUseCase;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.UpdateProductUseCase;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RestController;

/** Implements the generated contract 1:1; only translates HTTP <-> Command/Result. */
@RestController
public class ProductController implements ProductsApi {

    private final RegisterProductUseCase registerProductUseCase;
    private final GetProductUseCase getProductUseCase;
    private final SearchProductsUseCase searchProductsUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final ProductWebMapper mapper;

    public ProductController(RegisterProductUseCase registerProductUseCase, GetProductUseCase getProductUseCase,
                             SearchProductsUseCase searchProductsUseCase, UpdateProductUseCase updateProductUseCase,
                             ProductWebMapper mapper) {
        this.registerProductUseCase = registerProductUseCase;
        this.getProductUseCase = getProductUseCase;
        this.searchProductsUseCase = searchProductsUseCase;
        this.updateProductUseCase = updateProductUseCase;
        this.mapper = mapper;
    }

    @Override
    public ResponseEntity<ProductResponse> registerProduct(RegisterProductRequest registerProductRequest) {
        ProductResult result = registerProductUseCase.register(mapper.toCommand(registerProductRequest));
        return ResponseEntity.created(URI.create(PATH_REGISTER_PRODUCT + "/" + result.id()))
                .body(mapper.toResponse(result));
    }

    @Override
    public ResponseEntity<ProductPageResponse> searchProducts(@Nullable ProductStatus status,
                                                              @Nullable ProductCategory category,
                                                              Integer page, Integer size) {
        ProductPageResult result = searchProductsUseCase.search(mapper.toQuery(status, category, page, size));
        return ResponseEntity.ok(mapper.toPageResponse(result));
    }

    @Override
    public ResponseEntity<ProductResponse> getProduct(UUID productId) {
        return ResponseEntity.ok(mapper.toResponse(getProductUseCase.get(new GetProductQuery(productId))));
    }

    @Override
    public ResponseEntity<ProductResponse> updateProduct(UUID productId, UpdateProductRequest updateProductRequest) {
        ProductResult result = updateProductUseCase.update(mapper.toCommand(productId, updateProductRequest));
        return ResponseEntity.ok(mapper.toResponse(result));
    }

    @Override
    public ResponseEntity<ProductResponse> activateProduct(UUID productId) {
        // TODO activateProduct
        throw new UnsupportedOperationException("activateProduct is not implemented yet");
    }

    @Override
    public ResponseEntity<ProductResponse> discontinueProduct(UUID productId,
                                                              DiscontinueProductRequest discontinueProductRequest) {
        // TODO discontinueProduct
        throw new UnsupportedOperationException("discontinueProduct is not implemented yet");
    }
}
