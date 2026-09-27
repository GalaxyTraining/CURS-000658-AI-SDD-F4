package pe.edu.galaxy.training.java.sdd.catalog.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.GetProductUseCase;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.ProductResult;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.RegisterProductCommand;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.RegisterProductUseCase;
import pe.edu.galaxy.training.java.sdd.catalog.domain.exception.DuplicateSkuException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

/** US01 - Registrar producto, web layer: POST /api/v1/products (registerProduct). */
@WebMvcTest(ProductController.class)
@Import(ProductWebMapper.class)
class ProductControllerRegisterTest {

    private static final String PATH = "/api/v1/products";
    private static final UUID ID = UUID.fromString("7c9e6679-7425-40de-944b-e07fc1f90ae7");
    private static final Instant NOW = Instant.parse("2026-09-26T15:00:00Z");

    @Autowired
    private MockMvcTester mvc;

    @MockitoBean
    private RegisterProductUseCase registerProductUseCase;

    @MockitoBean
    private GetProductUseCase getProductUseCase;

    private MvcTestResult post(String json) {
        return mvc.post().uri(PATH).contentType(MediaType.APPLICATION_JSON).content(json).exchange();
    }

    private static ProductResult draft(BigDecimal price, String category) {
        return new ProductResult(ID, "ELE-0001", "Wireless mouse", null, price, category, "DRAFT", null, NOW, NOW);
    }

    @Test
    void US01_CA01_validRequest_returns201WithLocationAndBody() {
        when(registerProductUseCase.register(any())).thenReturn(draft(new BigDecimal("49.90"), "ELECTRONICS"));

        MvcTestResult result = post("""
                {"sku":"ELE-0001","name":"Wireless mouse","price":49.90,"category":"ELECTRONICS"}
                """);

        assertThat(result).hasStatus(HttpStatus.CREATED);
        assertThat(result).headers().hasValue("Location", PATH + "/" + ID);
        assertThat(result).bodyJson().extractingPath("$.id").isEqualTo(ID.toString());
        assertThat(result).bodyJson().extractingPath("$.status").isEqualTo("DRAFT");
        assertThat(result).bodyJson().extractingPath("$.category").isEqualTo("ELECTRONICS");

        ArgumentCaptor<RegisterProductCommand> command = ArgumentCaptor.forClass(RegisterProductCommand.class);
        verify(registerProductUseCase).register(command.capture());
        assertThat(command.getValue()).isEqualTo(new RegisterProductCommand(
                "ELE-0001", "Wireless mouse", null, new BigDecimal("49.90"), "ELECTRONICS"));
    }

    @Test
    void US01_CA02_duplicateSku_returns409DuplicateSku() {
        when(registerProductUseCase.register(any()))
                .thenThrow(new DuplicateSkuException("A product with SKU ELE-0001 already exists"));

        MvcTestResult result = post("""
                {"sku":"ELE-0001","name":"Wireless mouse"}
                """);

        assertThat(result).hasStatus(HttpStatus.CONFLICT);
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("DUPLICATE_SKU");
    }

    @ParameterizedTest
    @ValueSource(strings = {"ele-01", "EL-0001", "ELE0001", "ELE-00001"})
    void US01_CA03_invalidSkuFormat_returns400ValidationError(String sku) {
        MvcTestResult result = post("""
                {"sku":"%s","name":"Wireless mouse"}
                """.formatted(sku));

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("VALIDATION_ERROR");
        assertThat(result).bodyJson().extractingPath("$.details[0]").asString().startsWith("sku:");
        verify(registerProductUseCase, never()).register(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-1", "0.00"})
    void US01_CA04_priceZeroOrNegative_returns400ValidationError(String price) {
        MvcTestResult result = post("""
                {"sku":"ELE-0001","name":"Wireless mouse","price":%s}
                """.formatted(price));

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("VALIDATION_ERROR");
        verify(registerProductUseCase, never()).register(any());
    }

    @Test
    void US01_CA05_categoryOutsideCatalog_returns400ValidationError() {
        MvcTestResult result = post("""
                {"sku":"ELE-0001","name":"Wireless mouse","category":"TOYS"}
                """);

        assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("VALIDATION_ERROR");
        verify(registerProductUseCase, never()).register(any());
    }

    @Test
    void US01_CA06_withoutPriceNorCategory_returns201() {
        when(registerProductUseCase.register(any())).thenReturn(draft(null, null));

        MvcTestResult result = post("""
                {"sku":"ELE-0001","name":"Wireless mouse"}
                """);

        assertThat(result).hasStatus(HttpStatus.CREATED);
        assertThat(result).bodyJson().extractingPath("$.status").isEqualTo("DRAFT");
        assertThat(result).bodyJson().extractingPath("$.price").isNull();
        assertThat(result).bodyJson().extractingPath("$.category").isNull();
    }
}
