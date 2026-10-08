package es.urjc.etsii.grafo.autoconfig.irace;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * This class is used to configure irace by the properties specified in the application.yml
 * {@see application.yml} section irace
 */
@Configuration
@ConfigurationProperties(prefix = "irace")
public class IraceConfig {
    public static final int DEFAULT_API_EVALUATION_HISTORY_LIMIT = 10_000;

    private boolean enabled;
    private boolean auc = false;
    private boolean timecontrol = false;
    private int apiEvaluationHistoryLimit = DEFAULT_API_EVALUATION_HISTORY_LIMIT;

    /**
     * Is irace enabled?
     *
     * @return true if irace is enabled, false otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Is irace enabled?
     *
     * @param enabled true to enable irace, false to disable
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Decide if we should use the objective function directly or if we should calculate the AUC over the objective function
     * @return trye if we should calculate the AUC, false to use the objective function applied to the solution
     */
    public boolean isAuc() {
        return auc;
    }

    /**
     * Decide if we should use the objective function directly or if we should calculate the AUC over the objective function
     * @param auc true to calculate the AUC, false to use the objective function applied to the solution
     */
    public void setAuc(boolean auc) {
        this.auc = auc;
    }

    /**
     * Is time control enabled?
     * @return true if enabled, false otherwise
     */
    public boolean isTimecontrol() {
        return timecontrol;
    }

    /**
     * Enable or disable time control.
     * @param timecontrol true to enable, false to disable
     */
    public void setTimecontrol(boolean timecontrol) {
        this.timecontrol = timecontrol;
    }

    /**
     * Maximum number of individual evaluations retained for the autoconfig REST API.
     *
     * @return evaluation history limit
     */
    public int getApiEvaluationHistoryLimit() {
        return apiEvaluationHistoryLimit;
    }

    /**
     * Configure the maximum number of evaluations retained by the autoconfig REST API.
     *
     * @param apiEvaluationHistoryLimit positive history limit
     */
    public void setApiEvaluationHistoryLimit(int apiEvaluationHistoryLimit) {
        if (apiEvaluationHistoryLimit < 1) {
            throw new IllegalArgumentException("irace.api-evaluation-history-limit must be positive");
        }
        this.apiEvaluationHistoryLimit = apiEvaluationHistoryLimit;
    }
}
