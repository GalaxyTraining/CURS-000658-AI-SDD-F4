package pe.edu.galaxy.training.java.sdd.catalog.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;

import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.usecase.ActivateProductUseCase;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.usecase.DiscontinueProductUseCase;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.usecase.GetProductUseCase;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.usecase.RegisterProductUseCase;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.usecase.SearchProductsUseCase;
import pe.edu.galaxy.training.java.sdd.catalog.application.port.in.usecase.UpdateProductUseCase;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

/** ADR-0010: Spring MVC's own errors keep their HTTP status and never become 500 INTERNAL_ERROR. */
@WebMvcTest(ProductController.class)
@Import(ProductWebMapper.class)
class WebFrameworkErrorsTest {

    private static final String PRODUCT_PATH = "/api/v1/products/" + UUID.randomUUID();

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

    @Test
    void ADR0010_unknownRoute_returns404() {
        assertKeepsStatus(mvc.get().uri("/api/v1/unknown").exchange(), 404);
    }

    @Test
    void ADR0010_methodNotAllowed_returns405() {
        assertKeepsStatus(mvc.delete().uri(PRODUCT_PATH).exchange(), 405);
    }

    /** Even a 500 ErrorResponse could not be written as XML, so this case cannot become 500 by design. */
    @Test
    void ADR0010_notAcceptableMediaType_returns406() {
        assertKeepsStatus(mvc.get().uri(PRODUCT_PATH).accept(MediaType.APPLICATION_XML).exchange(), 406);
    }

    @Test
    void ADR0010_unsupportedContentType_returns415() {
        assertKeepsStatus(mvc.post().uri("/api/v1/products").contentType(MediaType.TEXT_PLAIN)
                .content("sku=ELE-0001").exchange(), 415);
    }

    private static void assertKeepsStatus(MvcTestResult result, int status) {
        assertThat(result).hasStatus(status);
        assertThat(result).body().asString().doesNotContain("INTERNAL_ERROR");
    }
}
