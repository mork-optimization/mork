package es.urjc.etsii.grafo.autoconfig.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Converts invalid autoconfig API query parameters into RFC 9457 problem details.
 */
@RestControllerAdvice(assignableTypes = AutoconfigController.class)
public class AutoconfigApiExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail invalidRequest(IllegalArgumentException exception) {
        var problem = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problem.setTitle("Invalid autoconfig request");
        return problem;
    }
}
