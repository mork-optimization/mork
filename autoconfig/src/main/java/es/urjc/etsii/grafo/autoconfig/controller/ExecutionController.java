package es.urjc.etsii.grafo.autoconfig.controller;

import es.urjc.etsii.grafo.autoconfig.controller.dto.ExecuteResponse;
import es.urjc.etsii.grafo.autoconfig.controller.dto.AutoconfigProgressRequest;
import es.urjc.etsii.grafo.autoconfig.controller.dto.MultiExecuteRequest;
import es.urjc.etsii.grafo.autoconfig.irace.IraceOrchestrator;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import org.springframework.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerErrorException;
import org.springframework.web.server.ResponseStatusException;

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
    private static final String BATCH_ENDPOINT = "/internal/autoconfig/irace/evaluations";

    private final IraceOrchestrator<S, I> orchestrator;

    /**
     * Create a new execution controller
     *
     * @param orchestrator Irace orchestrator
     */
    public ExecutionController(IraceOrchestrator<S, I> orchestrator) {
        this.orchestrator = orchestrator;
    }

    /**
     * Execute and return the results for the given IRACE configuration
     *
     * @param request integration key and run configuration
     * @return run result
     */
    @PostMapping(BATCH_ENDPOINT)
    public ResponseEntity<List<ExecuteResponse>> batchExecute(@RequestBody MultiExecuteRequest request) {
        request.checkValid(this.orchestrator.getIntegrationKey());
        log.trace("IRACE batch request with {} experiments", request.getExperiments().size());

        try {
            var results = this.orchestrator.iraceMultiCallback(
                    request.getExperiments(),
                    !request.isPreflight()
            );
            return ResponseEntity.ok(results);
        } catch (Exception e){
            String formattedMsg = String.format("Error executing batch request. Exps:  %s", request.getExperiments());
            log.error(formattedMsg, e);
            throw new ServerErrorException(formattedMsg, e);
        }
    }

    /**
     * Receive the elite set and progress counters produced after an IRACE iteration.
     *
     * @param request authenticated progress snapshot
     * @return empty successful response
     */
    @PostMapping("/internal/autoconfig/irace/progress")
    public ResponseEntity<Void> progress(@RequestBody AutoconfigProgressRequest request) {
        request.checkValid(this.orchestrator.getIntegrationKey());
        try {
            this.orchestrator.iraceProgressCallback(
                    request.getRunId(),
                    request.getIteration(),
                    request.getElites(),
                    request.getProgress()
            );
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, e.getMessage(), e);
        }
    }
}
