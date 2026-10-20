package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.autoconfig.autollm.execution.WorkloadProtocol;
import java.util.*;

public final class AutoLlmSummaryUtil {
    private AutoLlmSummaryUtil() {}
    public static Map<String,Object> summarize(AutoLlmConfig config, WorkloadProtocol.Host host, Map<String,Long> comparisonLimits) {
        var result = new LinkedHashMap<String,Object>();
        double evaluationMillis = (double)config.tuning().ignoreInitialMillis() + config.tuning().intervalDurationMillis();
        double tuning = evaluationMillis * config.tuning().evaluations() * config.trials();
        double comparison = 0; for (long limit : comparisonLimits.values()) comparison += limit;
        comparison *= 2.0 * config.comparison().repetitions() * config.trials();
        result.put("trials", config.trials()); result.put("componentRepairsPerTrial", config.preparation().maxRepairs());
        result.put("infrastructureRetries", AutoLlmFailureUtil.MAX_RETRIES); result.put("pilotMillisPerTrial", config.preparation().pilotDurationMillis());
        result.put("tuningEvaluationsPerTrial", config.tuning().evaluations()); result.put("tuningMillisPerEvaluation", evaluationMillis);
        result.put("comparisonRepetitions", config.comparison().repetitions()); result.put("comparisonTimeLimitMillis", config.comparison().timeLimitMillis());
        result.put("comparisonTimePolicy", config.comparison().timeLimitMillis() > 0 ? "EXPLICIT" : "APPLICATION_CALCULATOR");
        result.put("comparisonLimitsResolved", !comparisonLimits.isEmpty()); result.put("comparisonInstanceCount", comparisonLimits.size());
        result.put("comparisonLimitMillis", new TreeSet<>(comparisonLimits.values()));
        result.put("requestedWorkers", config.execution().workers()); result.put("effectiveWorkers", host == null ? null : host.effectiveWorkers());
        result.put("tuningEvaluationHours", tuning / 3_600_000);
        result.put("comparisonEvaluationHours", comparisonLimits.isEmpty() ? null : comparison / 3_600_000);
        result.put("idealTuningWallHours", host == null ? null : tuning / host.effectiveWorkers() / 3_600_000);
        result.put("estimateNote", "Evaluation-time estimates assume full configured horizons. Wall time also depends on racing, restarts, load and overhead; generation, builds, screening and repairs are excluded.");
        return result;
    }
}
