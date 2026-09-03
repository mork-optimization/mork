package es.urjc.etsii.grafo.autoconfig.controller.dto;

import java.util.List;

/**
 * Authenticated iteration progress reported through IRACE's iteration callback.
 */
public class AutoconfigProgressRequest extends AuthenticatedExecuteRequest {

    private final String runId;
    private final int iteration;
    private final List<EliteConfiguration> elites;

    public AutoconfigProgressRequest(
            String key,
            String runId,
            int iteration,
            List<EliteConfiguration> elites
    ) {
        super(key);
        this.runId = runId;
        this.iteration = iteration;
        this.elites = elites;
    }

    public String getRunId() {
        return runId;
    }

    public int getIteration() {
        return iteration;
    }

    public List<EliteConfiguration> getElites() {
        return elites;
    }

    @Override
    public void checkValid(String key) {
        super.checkValid(key);
        if (runId == null) {
            throw new IllegalArgumentException("Run ID cannot be null");
        }
        if (runId.isBlank()) {
            throw new IllegalArgumentException("Run ID cannot be blank");
        }
        if (iteration < 1) {
            throw new IllegalArgumentException("IRACE iteration must be positive");
        }
        if (elites == null) {
            throw new IllegalArgumentException("Elites cannot be null");
        }
        for (var elite : elites) {
            if (elite == null) {
                throw new IllegalArgumentException("Elite configuration cannot be null");
            }
        }
    }
}
