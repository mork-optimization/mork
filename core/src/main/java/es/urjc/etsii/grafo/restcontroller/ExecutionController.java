package es.urjc.etsii.grafo.restcontroller;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.restcontroller.dto.ExecuteRequest;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.irace.IraceOrchestrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * API endpoints related to experiment and run execution.
 * Currently, used for IRACE integration using the middleware.sh
 *
 * @param <S> Solution class
 * @param <I> Instance class
 */
@RestController
@ConditionalOnExpression(value = "${irace.enabled}")
public class ExecutionController<S extends Solution<S,I>, I extends Instance> {

    private static final Logger log = LoggerFactory.getLogger(ExecutionController.class);

    private final IraceOrchestrator<S,I> orquestrator;

    /**
     * Create a new execution controller
     *
     * @param orquestrator Irace orquestrator
     */
    public ExecutionController(IraceOrchestrator<S, I> orquestrator) {
        this.orquestrator = orquestrator;
        log.info("Execution controller enabled");
    }

    /**
     * Execute and return the results for the given IRACE configuration
     *
     * @param request integration key and run configuration
     * @return run result
     */
    @PostMapping("/execute")
    public ResponseEntity<String> execute(@RequestBody ExecuteRequest request) {
        log.debug("Execute request: {}", request);
        // TODO review async possibilities
        var result = this.orquestrator.iraceCallback(request);
        return ResponseEntity.ok(result);
    }
}
