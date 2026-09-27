package pe.edu.galaxy.training.java.sdd.catalog.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.ActivateProductCommand;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.ActivateProductUseCase;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.DiscontinueProductUseCase;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.GetProductUseCase;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.ProductResult;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.RegisterProductUseCase;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.SearchProductsUseCase;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.UpdateProductUseCase;
import pe.edu.galaxy.training.java.sdd.catalog.domain.exception.InvalidStateTransitionException;
import pe.edu.galaxy.training.java.sdd.catalog.domain.exception.ProductIncompleteException;
import pe.edu.galaxy.training.java.sdd.catalog.domain.exception.ProductNotFoundException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

/** US03 - Activar producto, web layer: POST /api/v1/products/{productId}/activate (activateProduct). */
@WebMvcTest(ProductController.class)
@Import(ProductWebMapper.class)
class ProductControllerActivateTest {

    private static final UUID ID = UUID.fromString("7c9e6679-7425-40de-944b-e07fc1f90ae7");
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

    private MvcTestResult activate(String productId) {
        return mvc.post().uri("/api/v1/products/" + productId + "/activate").exchange();
    }

    @Test
    void US03_CA01_completeDraft_returns200Active() {
        when(activateProductUseCase.activate(new ActivateProductCommand(ID))).thenReturn(new ProductResult(ID,
                "ELE-0001", "Wireless mouse", null, new BigDecimal("49.90"), "ELECTRONICS", "ACTIVE", null,
                NOW, NOW));

        MvcTestResult result = activate(ID.toString());

        assertThat(result).hasStatus(HttpStatus.OK);
        assertThat(result).bodyJson().extractingPath("$.id").isEqualTo(ID.toString());
        assertThat(result).bodyJson().extractingPath("$.status").isEqualTo("ACTIVE");
    }

    @Test
    void US03_CA02_incomplete_returns422WithMissingFields() {
        when(activateProductUseCase.activate(any()))
                .thenThrow(new ProductIncompleteException(List.of("price", "category")));

        MvcTestResult result = activate(ID.toString());

        assertThat(result).hasStatus(HttpStatus.UNPROCESSABLE_CONTENT);
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("PRODUCT_INCOMPLETE");
        assertThat(result).bodyJson().extractingPath("$.details").isEqualTo(List.of("price", "category"));
    }

    @Test
    void US03_CA03_invalidTransition_returns409InvalidStateTransition() {
        when(activateProductUseCase.activate(any())).thenThrow(new InvalidStateTransitionException(
                "Only a DRAFT product can be activated", List.of("status: ACTIVE")));

        MvcTestResult result = activate(ID.toString());

        assertThat(result).hasStatus(HttpStatus.CONFLICT);
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("INVALID_STATE_TRANSITION");
    }

    @Test
    void US03_unknownId_returns404ProductNotFound() {
        when(activateProductUseCase.activate(any())).thenThrow(new ProductNotFoundException(ID));

        MvcTestResult result = activate(ID.toString());

        assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("PRODUCT_NOT_FOUND");
    }

    @Test
    void US03_malformedId_returns400ValidationError() {
        MvcTestResult result = activate("not-a-uuid");

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("VALIDATION_ERROR");
        verify(activateProductUseCase, never()).activate(any());
    }
}
