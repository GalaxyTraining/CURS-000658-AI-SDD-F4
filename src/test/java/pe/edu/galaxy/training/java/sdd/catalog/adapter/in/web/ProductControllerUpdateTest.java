package pe.edu.galaxy.training.java.sdd.catalog.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.ActivateProductUseCase;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.DiscontinueProductUseCase;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.GetProductUseCase;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.ProductResult;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.RegisterProductUseCase;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.SearchProductsUseCase;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.UpdateProductCommand;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.UpdateProductUseCase;
import pe.edu.galaxy.training.java.sdd.catalog.domain.exception.InvalidStateTransitionException;
import pe.edu.galaxy.training.java.sdd.catalog.domain.exception.ProductNotFoundException;
import pe.edu.galaxy.training.java.sdd.catalog.domain.exception.ProductValidationException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

/** US02 - Actualizar producto, web layer: PATCH /api/v1/products/{productId} (updateProduct), ADR-0009. */
@WebMvcTest(ProductController.class)
@Import(ProductWebMapper.class)
class ProductControllerUpdateTest {

    private static final UUID ID = UUID.fromString("7c9e6679-7425-40de-944b-e07fc1f90ae7");
    private static final String PATH = "/api/v1/products/" + ID;
    private static final Instant NOW = Instant.parse("2026-09-26T15:00:00Z");

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private RegisterProductUseCase registerProductUseCase;

    @MockitoBean
    private GetProductUseCase getProductUseCase;

    @MockitoBean
    private SearchProductsUseCase searchProductsUseCase;

    @MockitoBean
    private UpdateProductUseCase updateProductUseCase;

    @MockitoBean
    private ActivateProductUseCase activateProductUseCase;

    @MockitoBean
    private DiscontinueProductUseCase discontinueProductUseCase;

    private MvcTestResult patch(String json) {
        return mvc.patch().uri(PATH).contentType(MediaType.APPLICATION_JSON).content(json).exchange();
    }

    private static ProductResult active(String name, BigDecimal price) {
        return new ProductResult(ID, "ELE-0001", name, null, price, "ELECTRONICS", "ACTIVE", null, NOW, NOW);
    }

    private void assertValidationErrorWithoutCallingUseCase(MvcTestResult result) {
        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("VALIDATION_ERROR");
        verify(updateProductUseCase, never()).update(any());
    }

    @Test
    void US02_CA01_validRequest_returns200AndStatusDoesNotChange() {
        when(updateProductUseCase.update(any())).thenReturn(active("Gaming mouse", new BigDecimal("59.90")));

        MvcTestResult result = patch("""
                {"name":"Gaming mouse","price":59.90}
                """);

        assertThat(result).hasStatus(HttpStatus.OK);
        assertThat(result).bodyJson().extractingPath("$.name").isEqualTo("Gaming mouse");
        assertThat(result).bodyJson().extractingPath("$.status").isEqualTo("ACTIVE");
        verify(updateProductUseCase).update(
                new UpdateProductCommand(ID, "Gaming mouse", null, new BigDecimal("59.90"), null));
    }

    @Test
    void US02_CA02_discontinued_returns409InvalidStateTransition() {
        when(updateProductUseCase.update(any())).thenThrow(
                new InvalidStateTransitionException("A discontinued product cannot be modified",
                        List.of("status: DISCONTINUED")));

        MvcTestResult result = patch("""
                {"name":"Gaming mouse"}
                """);

        assertThat(result).hasStatus(HttpStatus.CONFLICT);
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("INVALID_STATE_TRANSITION");
    }

    @Test
    void US02_CA03_skuInBody_isIgnored() {
        when(updateProductUseCase.update(any())).thenReturn(active("Gaming mouse", null));

        MvcTestResult result = patch("""
                {"sku":"XYZ-9999","name":"Gaming mouse"}
                """);

        assertThat(result).hasStatus(HttpStatus.OK);
        assertThat(result).bodyJson().extractingPath("$.sku").isEqualTo("ELE-0001");
        verify(updateProductUseCase).update(new UpdateProductCommand(ID, "Gaming mouse", null, null, null));
    }

    @Test
    void US02_CA03_onlySkuInBody_returns400() {
        assertValidationErrorWithoutCallingUseCase(patch("""
                {"sku":"XYZ-9999"}
                """));
    }

    @ParameterizedTest
    @ValueSource(strings = {"\"ab\"", "\"%s\""})
    void US02_CA04_invalidName_returns400(String name) {
        String value = name.formatted("x".repeat(101));

        assertValidationErrorWithoutCallingUseCase(patch("""
                {"name":%s}
                """.formatted(value)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1"})
    void US02_CA04_priceZeroOrNegative_returns400(String price) {
        assertValidationErrorWithoutCallingUseCase(patch("""
                {"price":%s}
                """.formatted(price)));
    }

    @Test
    void US02_CA04_priceWithThreeDecimals_returns400() {
        when(updateProductUseCase.update(any())).thenThrow(
                new ProductValidationException("Invalid price", List.of("price: at most 2 decimals")));

        MvcTestResult result = patch("""
                {"price":10.505}
                """);

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("VALIDATION_ERROR");
    }

    @Test
    void US02_CA04_categoryOutsideCatalog_returns400() {
        assertValidationErrorWithoutCallingUseCase(patch("""
                {"category":"TOYS"}
                """));
    }

    @ParameterizedTest
    @ValueSource(strings = {"price", "category"})
    void US02_CA05_explicitNullPriceOrCategory_returns400(String field) {
        assertValidationErrorWithoutCallingUseCase(patch("""
                {"name":"Gaming mouse","%s":null}
                """.formatted(field)));
    }

    @Test
    void US02_CA05_explicitNullOnly_returns400() {
        assertValidationErrorWithoutCallingUseCase(patch("""
                {"price":null}
                """));
    }

    @Test
    void US02_emptyBody_returns400() {
        assertValidationErrorWithoutCallingUseCase(patch("{}"));
    }

    @Test
    void US02_unknownId_returns404ProductNotFound() {
        when(updateProductUseCase.update(any())).thenThrow(new ProductNotFoundException(ID));

        MvcTestResult result = patch("""
                {"name":"Gaming mouse"}
                """);

        assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("PRODUCT_NOT_FOUND");
    }
}
