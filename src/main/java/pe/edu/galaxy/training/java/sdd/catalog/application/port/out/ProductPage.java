package pe.edu.galaxy.training.java.sdd.catalog.application.port.out;

import pe.edu.galaxy.training.java.sdd.catalog.domain.model.Product;
import java.util.List;

/** One page of products returned by ProductPersistencePort.search (architecture.md §4). */
public record ProductPage(List<Product> items, long totalElements) {

    public ProductPage {
        items = List.copyOf(items);
    }
}
