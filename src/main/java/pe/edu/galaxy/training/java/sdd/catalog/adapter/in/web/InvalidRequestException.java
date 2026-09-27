package pe.edu.galaxy.training.java.sdd.catalog.adapter.in.web;

import java.util.List;

/** A contract constraint the generated code does not check, e.g. minProperties (ADR-0009). Answered as 400. */
class InvalidRequestException extends RuntimeException {

    private final List<String> details;

    InvalidRequestException(String message, List<String> details) {
        super(message);
        this.details = List.copyOf(details);
    }

    List<String> getDetails() {
        return details;
    }
}
