package es.urjc.etsii.grafo.autoconfig.controller;

import es.urjc.etsii.grafo.autoconfig.exception.InvalidIntegrationKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

/**
 * Converts invalid autoconfig API query parameters into RFC 9457 problem details.
 */
@RestControllerAdvice(assignableTypes = {AutoconfigController.class, ExecutionController.class})
public class AutoconfigApiExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ProblemDetail responseStatus(ResponseStatusException exception) {
        return exception.getBody();
    }

    @ExceptionHandler(InvalidIntegrationKeyException.class)
    public ProblemDetail invalidIntegrationKey(InvalidIntegrationKeyException exception) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.UNAUTHORIZED, exception.getMessage());
        problem.setTitle("Invalid IRACE authentication");
        return problem;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail invalidRequest(IllegalArgumentException exception) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problem.setTitle("Invalid autoconfig request");
        return problem;
    }
}
