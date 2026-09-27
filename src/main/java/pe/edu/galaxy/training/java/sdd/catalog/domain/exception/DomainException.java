package pe.edu.galaxy.training.java.sdd.catalog.domain.exception;

import java.util.List;

public abstract class DomainException extends RuntimeException {

    private final List<String> details;

    protected DomainException(String message, List<String> details) {
        super(message);
        this.details = List.copyOf(details);
    }

    public List<String> getDetails() {
        return details;
    }
}
