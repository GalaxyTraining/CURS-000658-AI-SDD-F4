package pe.edu.galaxy.training.java.sdd.catalog.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.GetProductUseCase;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.RegisterProductUseCase;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.SearchProductsUseCase;
import pe.edu.galaxy.training.java.sdd.catalog.domain.exception.DuplicateSkuException;
import pe.edu.galaxy.training.java.sdd.catalog.domain.exception.InvalidStateTransitionException;
import pe.edu.galaxy.training.java.sdd.catalog.domain.exception.ProductIncompleteException;
import pe.edu.galaxy.training.java.sdd.catalog.domain.exception.ProductNotFoundException;
import pe.edu.galaxy.training.java.sdd.catalog.domain.exception.ProductValidationException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

/** Every row of ADR-0004 and ADR-0007: HTTP status and code, using registerProduct as the entry point. */
@WebMvcTest(ProductController.class)
@Import(ProductWebMapper.class)
class GlobalExceptionHandlerTest {

    private static final String VALID_REQUEST = """
            {"sku":"ELE-0001","name":"Wireless mouse"}
            """;

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private RegisterProductUseCase registerProductUseCase;

    @MockitoBean
    private GetProductUseCase getProductUseCase;

    @MockitoBean
    private SearchProductsUseCase searchProductsUseCase;

    static Stream<Arguments> domainExceptions() {
        return Stream.of(
                Arguments.of(new ProductValidationException("Invalid price", List.of("price: at most 2 decimals")),
                        400, "VALIDATION_ERROR"),
                Arguments.of(new ProductNotFoundException(UUID.randomUUID()), 404, "PRODUCT_NOT_FOUND"),
                Arguments.of(new DuplicateSkuException("Duplicated SKU"), 409, "DUPLICATE_SKU"),
                Arguments.of(new InvalidStateTransitionException("Invalid transition"), 409, "INVALID_STATE_TRANSITION"),
                Arguments.of(new ProductIncompleteException(List.of("price", "category")), 422, "PRODUCT_INCOMPLETE"));
    }

    private MvcTestResult register() {
        return mvc.post().uri("/api/v1/products").contentType(MediaType.APPLICATION_JSON)
                .content(VALID_REQUEST).exchange();
    }

    @ParameterizedTest
    @MethodSource("domainExceptions")
    void ADR0004_domainException_mapsToHttpAndCode(RuntimeException exception, int status, String code) {
        when(registerProductUseCase.register(any())).thenThrow(exception);

        MvcTestResult result = register();

        assertThat(result).hasStatus(status);
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo(code);
        assertThat(result).bodyJson().extractingPath("$.message").isNotNull();
        assertThat(result).bodyJson().extractingPath("$.details").isNotNull();
    }

    @Test
    void ADR0004_productIncomplete_listsMissingDataInDetails() {
        when(registerProductUseCase.register(any())).thenThrow(new ProductIncompleteException(List.of("price")));

        assertThat(register()).bodyJson().extractingPath("$.details").asArray().containsExactly("price");
    }

    @Test
    void ADR0007_unexpectedException_returns500WithoutInternalDetails() {
        when(registerProductUseCase.register(any()))
                .thenThrow(new IllegalStateException("secret connection string jdbc:h2:mem:internal"));

        MvcTestResult result = register();

        assertThat(result).hasStatus(500);
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("INTERNAL_ERROR");
        assertThat(result).bodyJson().extractingPath("$.details").asArray().isEmpty();
        assertThat(result).body().asString().doesNotContain("secret", "jdbc");
    }
}
