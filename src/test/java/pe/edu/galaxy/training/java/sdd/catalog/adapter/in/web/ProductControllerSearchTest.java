package pe.edu.galaxy.training.java.sdd.catalog.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.GetProductUseCase;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.ProductPageResult;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.ProductResult;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.RegisterProductUseCase;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.SearchProductsQuery;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.SearchProductsUseCase;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

/** US05 - Listar productos, web layer: GET /api/v1/products (searchProducts). */
@WebMvcTest(ProductController.class)
@Import(ProductWebMapper.class)
class ProductControllerSearchTest {

    private static final String PATH = "/api/v1/products";
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

    private MvcTestResult get(String queryString) {
        return mvc.get().uri(PATH + queryString).exchange();
    }

    private static ProductResult activeElectronics() {
        return new ProductResult(ID, "ELE-0001", "Wireless mouse", null, new BigDecimal("49.90"), "ELECTRONICS",
                "ACTIVE", null, NOW, NOW);
    }

    @Test
    void US05_CA03_filters_arePassedToQueryAndReturn200() {
        when(searchProductsUseCase.search(new SearchProductsQuery("ACTIVE", "ELECTRONICS", 0, 20)))
                .thenReturn(new ProductPageResult(List.of(activeElectronics()), 0, 20, 1, 1));

        MvcTestResult result = get("?status=ACTIVE&category=ELECTRONICS");

        assertThat(result).hasStatus(HttpStatus.OK);
        assertThat(result).bodyJson().extractingPath("$.items.length()").isEqualTo(1);
        assertThat(result).bodyJson().extractingPath("$.items[0].id").isEqualTo(ID.toString());
        assertThat(result).bodyJson().extractingPath("$.items[0].status").isEqualTo("ACTIVE");
        assertThat(result).bodyJson().extractingPath("$.items[0].category").isEqualTo("ELECTRONICS");
    }

    @Test
    void US05_CA04_defaultPageAndSize_areApplied() {
        when(searchProductsUseCase.search(any())).thenReturn(new ProductPageResult(List.of(), 0, 20, 0, 0));

        MvcTestResult result = get("");

        assertThat(result).hasStatus(HttpStatus.OK);
        verify(searchProductsUseCase).search(new SearchProductsQuery(null, null, 0, 20));
    }

    @Test
    void US05_CA04_pageMetadata_isReturned() {
        when(searchProductsUseCase.search(new SearchProductsQuery(null, null, 1, 3)))
                .thenReturn(new ProductPageResult(List.of(activeElectronics()), 1, 3, 4, 2));

        MvcTestResult result = get("?page=1&size=3");

        assertThat(result).hasStatus(HttpStatus.OK);
        assertThat(result).bodyJson().extractingPath("$.page").isEqualTo(1);
        assertThat(result).bodyJson().extractingPath("$.size").isEqualTo(3);
        assertThat(result).bodyJson().extractingPath("$.totalElements").isEqualTo(4);
        assertThat(result).bodyJson().extractingPath("$.totalPages").isEqualTo(2);
    }

    @Test
    void US05_CA03_unknownStatus_returns400ValidationError() {
        MvcTestResult result = get("?status=ARCHIVED");

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("VALIDATION_ERROR");
        verify(searchProductsUseCase, never()).search(any());
    }

    @Test
    void US05_CA03_unknownCategory_returns400ValidationError() {
        MvcTestResult result = get("?category=TOYS");

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("VALIDATION_ERROR");
        verify(searchProductsUseCase, never()).search(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "101"})
    void US05_CA04_sizeOutOfRange_returns400ValidationError(String size) {
        MvcTestResult result = get("?size=" + size);

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("VALIDATION_ERROR");
        verify(searchProductsUseCase, never()).search(any());
    }

    @Test
    void US05_CA04_negativePage_returns400ValidationError() {
        MvcTestResult result = get("?page=-1");

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("VALIDATION_ERROR");
        assertThat(result).bodyJson().extractingPath("$.details[0]").asString().startsWith("page:");
        verify(searchProductsUseCase, never()).search(any());
    }
}
