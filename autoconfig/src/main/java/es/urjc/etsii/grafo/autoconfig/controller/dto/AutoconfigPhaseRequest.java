package es.urjc.etsii.grafo.autoconfig.controller.dto;

import es.urjc.etsii.grafo.autoconfig.service.AutoconfigRunState;

/** Authenticated lifecycle phase reported by the bundled IRACE process. */
public class AutoconfigPhaseRequest extends AuthenticatedExecuteRequest {

    private final String runId;
    private final AutoconfigRunState.RunPhase phase;

    public AutoconfigPhaseRequest(
            String key,
            String runId,
            AutoconfigRunState.RunPhase phase
    ) {
        super(key);
        this.runId = runId;
        this.phase = phase;
    }

    public String getRunId() {
        return runId;
    }

    public AutoconfigRunState.RunPhase getPhase() {
        return phase;
    }

    @Override
    public void checkValid(String key) {
        super.checkValid(key);
        if (runId == null || runId.isBlank()) {
            throw new IllegalArgumentException("Run ID cannot be blank");
        }
        if (phase == null) {
            throw new IllegalArgumentException("Run phase cannot be null");
        }
        if (phase != AutoconfigRunState.RunPhase.RACING
                && phase != AutoconfigRunState.RunPhase.POSTPROCESSING) {
            throw new IllegalArgumentException("IRACE may only report RACING or POSTPROCESSING");
        }
    }
}
