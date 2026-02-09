package es.urjc.etsii.grafo.autoconfig.controller;

import es.urjc.etsii.grafo.autoconfig.controller.dto.MultiExecuteRequest;
import es.urjc.etsii.grafo.autoconfig.controller.dto.SingleExecuteRequest;
import es.urjc.etsii.grafo.autoconfig.controller.dto.ExecuteResponse;
import es.urjc.etsii.grafo.autoconfig.irace.IraceOrchestrator;
import es.urjc.etsii.grafo.autoconfig.irace.IraceRuntimeConfiguration;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerErrorException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

/**
 * API endpoints related to experiment and run execution.
 * Currently, used for IRACE integration using the middleware.sh
 *
 * @param <S> Solution class
 * @param <I> Instance class
 */
@RestController
public class ExecutionController<S extends Solution<S, I>, I extends Instance> {

    private static final Logger log = LoggerFactory.getLogger(ExecutionController.class);

    private final IraceOrchestrator<S, I> orquestrator;

    private final ObjectMapper json;

    /**
     * Create a new execution controller
     *
     * @param orquestrator Irace orquestrator
     */
    public ExecutionController(IraceOrchestrator<S, I> orquestrator) {
        this.orquestrator = orquestrator;
        this.json = new ObjectMapper();
    }

    /**
     * Execute and return the results for the given IRACE configuration
     *
     * @param request integration key and run configuration
     * @return run result
     */
    @PostMapping("/execute")
    public ResponseEntity<String> execute(@RequestBody SingleExecuteRequest request) {
        log.trace("Execute request: {}", request);
        request.checkValid(this.orquestrator.getIntegrationKey());

        var exp = request.getExperiment();
        try {
            var config = new IraceRuntimeConfiguration(exp);
            var result = this.orquestrator.iraceSingleCallback(config);
            return ResponseEntity.ok(result.toIraceResultString());
        } catch (Exception e) {
            String errorMessage = String.format("Error executing single request: %s", exp);
            log.error(errorMessage, e);
            throw new ServerErrorException(errorMessage, e);
        }
    }

    /**
     * Execute and return the results for the given IRACE configuration
     *
     * @param request integration key and run configuration
     * @return run result
     */
    @PostMapping("/batchExecute")
    public ResponseEntity<List<ExecuteResponse>> batchExecute(@RequestBody MultiExecuteRequest request) {
        log.trace("Batch execute request: {}", request);
        request.checkValid(this.orquestrator.getIntegrationKey());

        try {
            var results = this.orquestrator.iraceMultiCallback(request.getExperiments());
            return ResponseEntity.ok(results);
        } catch (Exception e){
            String formattedMsg = String.format("Error executing batch request. Exps:  %s", request.getExperiments());
            log.error(formattedMsg, e);
            throw new ServerErrorException(formattedMsg, e);
        }
    }
}
