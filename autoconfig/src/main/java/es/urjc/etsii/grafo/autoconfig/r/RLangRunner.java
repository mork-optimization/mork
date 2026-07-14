package es.urjc.etsii.grafo.autoconfig.r;

/**
 * Executes an R script.
 *
 * <p>Applications may provide their own Spring bean implementing this
 * interface. Mork supplies {@link RScriptRunner} when no custom runner is
 * configured.</p>
 */
@FunctionalInterface
public interface RLangRunner {

    /**
     * Execute the requested R script and wait for it to finish.
     *
     * @param request execution parameters
     * @return completed process information and output log locations
     */
    RExecutionResult execute(RExecutionRequest request);
}
