package es.urjc.etsii.grafo.autoconfig.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.urjc.etsii.grafo.autoconfig.controller.dto.ExecuteRequest;
import es.urjc.etsii.grafo.autoconfig.controller.dto.ExecuteResponse;
import es.urjc.etsii.grafo.autoconfig.controller.dto.IraceExecuteConfig;
import es.urjc.etsii.grafo.autoconfig.irace.IraceOrchestrator;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

/**
 * API endpoints related to experiment and run execution.
 * Currently, used for IRACE integration using the middleware.sh
 *
 * @param <S> Solution class
 * @param <I> Instance class
 */
@RestController
@Profile({"irace", "autoconfig"})
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
        this.json = new ObjectMapper().configure(FAIL_ON_UNKNOWN_PROPERTIES, false); // ignore unmapped properties such as bounds
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
        log.trace("Execute request: {}", request);
        var decoded = validateAndPrepare(request, this.orquestrator.getIntegrationKey());
        var config = IraceUtil.toIraceRuntimeConfig(decoded);
        var result = this.orquestrator.iraceSingleCallback(config);
        return ResponseEntity.ok(result.toIraceResultString());
    }

    /**
     * Execute and return the results for the given IRACE configuration
     *
     * @param request integration key and run configuration
     * @return run result
     */
    @PostMapping("/batchExecute")
    public ResponseEntity<List<ExecuteResponse>> batchExecute(@RequestBody ExecuteRequest request) throws JsonProcessingException {
        log.trace("Batch execute request: {}", request);
        var decoded = validateAndPrepare(request, this.orquestrator.getIntegrationKey());
        List<IraceExecuteConfig> configs = json.readValue(decoded, new TypeReference<>() {});
        var results = this.orquestrator.iraceMultiCallback(configs);
        return ResponseEntity.ok(results);
    }

    public static String validateAndPrepare(ExecuteRequest request, String integrationKey) {
        if (!request.isValid()) {
            throw new IllegalArgumentException("ExecuteRequest failed validation");
        }
        if (!request.getKey().equals(integrationKey)) {
            throw new IllegalArgumentException(String.format("Invalid integration key, got %s", request.getKey()));
        }
        String decoded = StringUtil.b64decode(request.getConfig());
        if (decoded.isBlank()) {
            throw new IllegalArgumentException("Empty decoded config");
        }
        return decoded;
    }
}
