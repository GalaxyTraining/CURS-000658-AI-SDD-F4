package pe.edu.galaxy.training.java.sdd.catalog.domain.exception;

import java.util.List;

/** ADR-0004: INVALID_STATE_TRANSITION (INV-06, INV-08). */
public class InvalidStateTransitionException extends DomainException {

    public InvalidStateTransitionException(String message) {
        this(message, List.of());
    }

    public InvalidStateTransitionException(String message, List<String> details) {
        super(message, details);
    }
}
