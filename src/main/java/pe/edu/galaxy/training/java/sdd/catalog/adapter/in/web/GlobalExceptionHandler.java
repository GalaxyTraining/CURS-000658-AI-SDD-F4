package pe.edu.galaxy.training.java.sdd.catalog.adapter.in.web;

import pe.edu.galaxy.training.java.sdd.catalog.adapter.in.web.generated.model.ErrorCode;
import pe.edu.galaxy.training.java.sdd.catalog.adapter.in.web.generated.model.ErrorResponse;
import pe.edu.galaxy.training.java.sdd.catalog.domain.exception.DomainException;
import pe.edu.galaxy.training.java.sdd.catalog.domain.exception.DuplicateSkuException;
import pe.edu.galaxy.training.java.sdd.catalog.domain.exception.InvalidStateTransitionException;
import pe.edu.galaxy.training.java.sdd.catalog.domain.exception.ProductIncompleteException;
import pe.edu.galaxy.training.java.sdd.catalog.domain.exception.ProductNotFoundException;
import pe.edu.galaxy.training.java.sdd.catalog.domain.exception.ProductValidationException;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/** Maps every error to ErrorResponse, one row of ADR-0004 each, plus INTERNAL_ERROR (ADR-0007). */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ProductValidationException.class)
    ResponseEntity<ErrorResponse> handleValidation(ProductValidationException e) {
        return error(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR, e);
    }

    @ExceptionHandler(ProductNotFoundException.class)
    ResponseEntity<ErrorResponse> handleNotFound(ProductNotFoundException e) {
        return error(HttpStatus.NOT_FOUND, ErrorCode.PRODUCT_NOT_FOUND, e);
    }

    @ExceptionHandler(DuplicateSkuException.class)
    ResponseEntity<ErrorResponse> handleDuplicateSku(DuplicateSkuException e) {
        return error(HttpStatus.CONFLICT, ErrorCode.DUPLICATE_SKU, e);
    }

    @ExceptionHandler(InvalidStateTransitionException.class)
    ResponseEntity<ErrorResponse> handleInvalidTransition(InvalidStateTransitionException e) {
        return error(HttpStatus.CONFLICT, ErrorCode.INVALID_STATE_TRANSITION, e);
    }

    @ExceptionHandler(ProductIncompleteException.class)
    ResponseEntity<ErrorResponse> handleIncomplete(ProductIncompleteException e) {
        return error(HttpStatus.UNPROCESSABLE_CONTENT, ErrorCode.PRODUCT_INCOMPLETE, e);
    }

    /** Contract constraints on the request body (pattern, length, minimum...). */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> handleInvalidBody(MethodArgumentNotValidException e) {
        List<String> details = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + ": " + fieldError.getDefaultMessage())
                .toList();
        return error(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR, "Invalid request", details);
    }

    /** Malformed JSON or a value outside a closed catalog, e.g. an unknown category (US01 CA-05). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ErrorResponse> handleUnreadableBody(HttpMessageNotReadableException e) {
        return error(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR, "Invalid request",
                List.of("body: malformed JSON or value not allowed"));
    }

    /** Contract constraints on path and query parameters. */
    @ExceptionHandler(HandlerMethodValidationException.class)
    ResponseEntity<ErrorResponse> handleInvalidParameters(HandlerMethodValidationException e) {
        List<String> details = e.getParameterValidationResults().stream()
                .flatMap(result -> result.getResolvableErrors().stream()
                        .map(error -> result.getMethodParameter().getParameterName() + ": "
                                + ((MessageSourceResolvable) error).getDefaultMessage()))
                .toList();
        return error(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR, "Invalid request", details);
    }

    /**
     * Contract constraints on query parameters (e.g. page, size) checked by method validation,
     * since the generated ProductsApi is annotated with @Validated.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException e) {
        List<String> details = e.getConstraintViolations().stream()
                .map(violation -> lastNode(violation.getPropertyPath()) + ": " + violation.getMessage())
                .sorted()
                .toList();
        return error(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR, "Invalid request", details);
    }

    /** A path or query value of the wrong type, e.g. a malformed UUID or an unknown status. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return error(HttpStatus.BAD_REQUEST, ErrorCode.VALIDATION_ERROR, "Invalid request",
                List.of(e.getName() + ": invalid value"));
    }

    /**
     * ADR-0007: any unexpected exception, without internal details. Spring MVC's own errors
     * (405, 415, unknown route...) are rethrown so Spring keeps answering them as usual.
     */
    @ExceptionHandler(Exception.class)
    ResponseEntity<ErrorResponse> handleUnexpected(Exception e) throws Exception {
        if (e instanceof org.springframework.web.ErrorResponse) {
            throw e;
        }
        log.error("Unexpected error", e);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR, "Unexpected error", List.of());
    }

    /** "searchProducts.page" -> "page". */
    private static String lastNode(Path path) {
        String name = null;
        for (Path.Node node : path) {
            name = node.getName();
        }
        return name;
    }

    private static ResponseEntity<ErrorResponse> error(HttpStatus status, ErrorCode code, DomainException e) {
        return error(status, code, e.getMessage(), e.getDetails());
    }

    private static ResponseEntity<ErrorResponse> error(HttpStatus status, ErrorCode code, String message,
                                                       List<String> details) {
        return ResponseEntity.status(status).body(new ErrorResponse(code, message, details));
    }
}
