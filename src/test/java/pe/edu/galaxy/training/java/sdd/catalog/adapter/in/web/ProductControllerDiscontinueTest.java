package pe.edu.galaxy.training.java.sdd.catalog.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.ActivateProductUseCase;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.DiscontinueProductCommand;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.DiscontinueProductUseCase;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.GetProductUseCase;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.ProductResult;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.RegisterProductUseCase;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.SearchProductsUseCase;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.UpdateProductUseCase;
import pe.edu.galaxy.training.java.sdd.catalog.domain.exception.InvalidStateTransitionException;
import pe.edu.galaxy.training.java.sdd.catalog.domain.exception.ProductNotFoundException;
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

/** US04 - Descontinuar producto, web layer: POST /api/v1/products/{productId}/discontinue (discontinueProduct). */
@WebMvcTest(ProductController.class)
@Import(ProductWebMapper.class)
class ProductControllerDiscontinueTest {

    private static final UUID ID = UUID.fromString("7c9e6679-7425-40de-944b-e07fc1f90ae7");
    private static final String PATH = "/api/v1/products/" + ID + "/discontinue";
    private static final Instant NOW = Instant.parse("2026-09-26T15:00:00Z");
    private static final String REASON = "The supplier stopped making it";

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

    private MvcTestResult post(String json) {
        return mvc.post().uri(PATH).contentType(MediaType.APPLICATION_JSON).content(json).exchange();
    }

    private void assertValidationErrorWithoutCallingUseCase(MvcTestResult result) {
        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("VALIDATION_ERROR");
        verify(discontinueProductUseCase, never()).discontinue(any());
    }

    @Test
    void US04_CA01_validReason_returns200DiscontinuedWithReason() {
        when(discontinueProductUseCase.discontinue(new DiscontinueProductCommand(ID, REASON)))
                .thenReturn(new ProductResult(ID, "ELE-0001", "Wireless mouse", null, new BigDecimal("49.90"),
                        "ELECTRONICS", "DISCONTINUED", REASON, NOW, NOW));

        MvcTestResult result = post("""
                {"reason":"%s"}
                """.formatted(REASON));

        assertThat(result).hasStatus(HttpStatus.OK);
        assertThat(result).bodyJson().extractingPath("$.status").isEqualTo("DISCONTINUED");
        assertThat(result).bodyJson().extractingPath("$.discontinuationReason").isEqualTo(REASON);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "Too short"})
    void US04_CA02_emptyOrShortReason_returns400(String reason) {
        assertValidationErrorWithoutCallingUseCase(post("""
                {"reason":"%s"}
                """.formatted(reason)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"reason\":null}"})
    void US04_CA02_missingOrNullReason_returns400(String body) {
        assertValidationErrorWithoutCallingUseCase(post(body));
    }

    @Test
    void US04_CA02_reasonLongerThan500_returns400() {
        assertValidationErrorWithoutCallingUseCase(post("""
                {"reason":"%s"}
                """.formatted("x".repeat(501))));
    }

    @Test
    void US04_CA03_alreadyDiscontinued_returns409InvalidStateTransition() {
        when(discontinueProductUseCase.discontinue(any())).thenThrow(new InvalidStateTransitionException(
                "The product is already discontinued", List.of("status: DISCONTINUED")));

        MvcTestResult result = post("""
                {"reason":"%s"}
                """.formatted(REASON));

        assertThat(result).hasStatus(HttpStatus.CONFLICT);
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("INVALID_STATE_TRANSITION");
    }

    @Test
    void US04_unknownId_returns404ProductNotFound() {
        when(discontinueProductUseCase.discontinue(any())).thenThrow(new ProductNotFoundException(ID));

        MvcTestResult result = post("""
                {"reason":"%s"}
                """.formatted(REASON));

        assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("PRODUCT_NOT_FOUND");
    }
}
