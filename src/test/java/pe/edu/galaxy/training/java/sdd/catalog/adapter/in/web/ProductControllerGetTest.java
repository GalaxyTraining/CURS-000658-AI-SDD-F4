package pe.edu.galaxy.training.java.sdd.catalog.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.GetProductQuery;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.GetProductUseCase;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.ProductResult;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.RegisterProductUseCase;
import pe.edu.galaxy.training.java.sdd.catalog.domain.exception.ProductNotFoundException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

/** US05 - Consultar un producto, web layer: GET /api/v1/products/{productId} (getProduct). */
@WebMvcTest(ProductController.class)
@Import(ProductWebMapper.class)
class ProductControllerGetTest {

    private static final String PATH = "/api/v1/products/";
    private static final UUID ID = UUID.fromString("7c9e6679-7425-40de-944b-e07fc1f90ae7");
    private static final Instant NOW = Instant.parse("2026-09-26T15:00:00Z");

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private RegisterProductUseCase registerProductUseCase;

    @MockitoBean
    private GetProductUseCase getProductUseCase;

    private MvcTestResult get(String productId) {
        return mvc.get().uri(PATH + productId).exchange();
    }

    @Test
    void US05_CA01_existingId_returns200WithAllDataAndStatus() {
        when(getProductUseCase.get(new GetProductQuery(ID))).thenReturn(new ProductResult(ID, "ELE-0001",
                "Wireless mouse", "Ergonomic mouse", new BigDecimal("49.90"), "ELECTRONICS", "ACTIVE", null,
                NOW, NOW));

        MvcTestResult result = get(ID.toString());

        assertThat(result).hasStatus(HttpStatus.OK);
        assertThat(result).bodyJson().extractingPath("$.id").isEqualTo(ID.toString());
        assertThat(result).bodyJson().extractingPath("$.sku").isEqualTo("ELE-0001");
        assertThat(result).bodyJson().extractingPath("$.name").isEqualTo("Wireless mouse");
        assertThat(result).bodyJson().extractingPath("$.description").isEqualTo("Ergonomic mouse");
        assertThat(result).bodyJson().extractingPath("$.price").isEqualTo(49.90);
        assertThat(result).bodyJson().extractingPath("$.category").isEqualTo("ELECTRONICS");
        assertThat(result).bodyJson().extractingPath("$.status").isEqualTo("ACTIVE");
        assertThat(result).bodyJson().extractingPath("$.createdAt").isNotNull();
        assertThat(result).bodyJson().extractingPath("$.updatedAt").isNotNull();
    }

    @Test
    void US05_CA02_unknownId_returns404ProductNotFound() {
        when(getProductUseCase.get(any())).thenThrow(new ProductNotFoundException(ID));

        MvcTestResult result = get(ID.toString());

        assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("PRODUCT_NOT_FOUND");
    }

    @Test
    void US05_CA02_malformedId_returns400ValidationError() {
        MvcTestResult result = get("not-a-uuid");

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("VALIDATION_ERROR");
        verify(getProductUseCase, never()).get(any());
    }
}
