package pe.edu.galaxy.training.java.sdd.catalog.application.port.in;

/** Input of SearchProductsUseCase. Null status or category means "no filter"; they are codes. Page starts at 0. */
public record SearchProductsQuery(String status, String category, int page, int size) {
}
