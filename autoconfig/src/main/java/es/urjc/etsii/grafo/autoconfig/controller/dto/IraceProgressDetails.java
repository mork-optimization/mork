package es.urjc.etsii.grafo.autoconfig.controller.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Progress counters reported by IRACE after an iteration.
 *
 * <p>Unknown properties are ignored so adding a counter in IRACE does not break
 * existing Mork versions.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record IraceProgressDetails(
        int nbIterations,
        int maxExperiments,
        int experimentsUsed,
        int remainingBudget,
        boolean remainingBudgetEstimated,
        int currentBudget,
        int currentBudgetUsed,
        double maxTime,
        double timeUsed,
        Double remainingTime,
        Double boundEstimate
) {
}
