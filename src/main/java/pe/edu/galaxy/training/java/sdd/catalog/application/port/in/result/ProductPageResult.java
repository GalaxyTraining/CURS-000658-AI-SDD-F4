package pe.edu.galaxy.training.java.sdd.catalog.application.port.in.result;

import java.util.List;

/** Output of SearchProductsUseCase: one page of products plus its metadata. */
public record ProductPageResult(List<ProductResult> items, int page, int size, long totalElements,
                                int totalPages) {

    public ProductPageResult {
        items = List.copyOf(items);
    }
}
