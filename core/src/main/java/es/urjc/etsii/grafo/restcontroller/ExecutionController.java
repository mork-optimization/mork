package es.urjc.etsii.grafo.restcontroller;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.restcontroller.dto.ExecuteRequest;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.irace.IraceOrquestrator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.logging.Logger;

@RestController("/api/")
@ConditionalOnExpression("'${irace.enabled}'")
public class ExecutionController<S extends Solution<I>, I extends Instance> {

    private static final Logger log = Logger.getLogger(ExecutionController.class.getName());

    private final IraceOrquestrator<S,I> orquestrator;

    public ExecutionController(IraceOrquestrator<S, I> orquestrator) {
        this.orquestrator = orquestrator;
    }

    @PostMapping("/execute")
    public ResponseEntity<Double> execute(@RequestBody ExecuteRequest request) {
        log.finest("Execute request: " + request);
        // TODO review async possibilities
        var result = this.orquestrator.iraceCallback(request);
        return ResponseEntity.ok(result);
    }
}
