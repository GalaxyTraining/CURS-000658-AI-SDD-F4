package pe.edu.galaxy.training.java.sdd.catalog.adapter.in.web;

import pe.edu.galaxy.training.java.sdd.catalog.adapter.in.web.generated.ProductsApi;
import pe.edu.galaxy.training.java.sdd.catalog.adapter.in.web.generated.model.DiscontinueProductRequest;
import pe.edu.galaxy.training.java.sdd.catalog.adapter.in.web.generated.model.ProductCategory;
import pe.edu.galaxy.training.java.sdd.catalog.adapter.in.web.generated.model.ProductPageResponse;
import pe.edu.galaxy.training.java.sdd.catalog.adapter.in.web.generated.model.ProductResponse;
import pe.edu.galaxy.training.java.sdd.catalog.adapter.in.web.generated.model.ProductStatus;
import pe.edu.galaxy.training.java.sdd.catalog.adapter.in.web.generated.model.RegisterProductRequest;
import pe.edu.galaxy.training.java.sdd.catalog.adapter.in.web.generated.model.UpdateProductRequest;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.ProductResult;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.RegisterProductUseCase;
import java.net.URI;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RestController;

/** Implements the generated contract 1:1; only translates HTTP <-> Command/Result. */
@RestController
public class ProductController implements ProductsApi {

    private final RegisterProductUseCase registerProductUseCase;
    private final ProductWebMapper mapper;

    public ProductController(RegisterProductUseCase registerProductUseCase, ProductWebMapper mapper) {
        this.registerProductUseCase = registerProductUseCase;
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
        // TODO searchProducts
        throw new UnsupportedOperationException("searchProducts is not implemented yet");
    }

    @Override
    public ResponseEntity<ProductResponse> getProduct(UUID productId) {
        // TODO getProduct
        throw new UnsupportedOperationException("getProduct is not implemented yet");
    }

    @Override
    public ResponseEntity<ProductResponse> updateProduct(UUID productId, UpdateProductRequest updateProductRequest) {
        // TODO updateProduct
        throw new UnsupportedOperationException("updateProduct is not implemented yet");
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
