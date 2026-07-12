package es.urjc.etsii.grafo.executors;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.algorithms.EmptyAlgorithm;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.metrics.MetricsStorage;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.Context;
import es.urjc.etsii.grafo.util.TimeStatsEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE, isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public record WorkUnitResult<S extends Solution<S, I>, I extends Instance>(
        String resultId,
        boolean success,
        String experimentName,
        String instancePath,
        String instanceId,
        Algorithm<S,I> algorithm,
        String iteration,
        S solution,
        Map<String, Object> solutionProperties,
        long executionTime,
        long timeToTarget,
        MetricsStorage metrics,
        List<TimeStatsEvent> timeData
) {

    public static final String BEST_ALGORITHM = "bestalg";
    public static final String BEST_ITERATION = "bestiter";

    public static <S extends Solution<S, I>, I extends Instance> WorkUnitResult<S,I> ok(WorkUnit<S,I> workUnit, String instanceId, S solution, long executionTime, long timeToTarget, MetricsStorage metrics, List<TimeStatsEvent> timeData) {
        return new WorkUnitResult<>(true, workUnit.experimentName(), workUnit.instancePath(), instanceId, workUnit.algorithm(), workUnit.i(), solution, executionTime, timeToTarget, metrics, timeData);
    }

    public static <S extends Solution<S, I>, I extends Instance> WorkUnitResult<S,I> failure(WorkUnit<S,I> workUnit, String instanceId, long executionTime, long timeToTarget, List<TimeStatsEvent> timeData) {
        return new WorkUnitResult<>(false, workUnit.experimentName(), workUnit.instancePath(), instanceId, workUnit.algorithm(), workUnit.i(), null, executionTime, timeToTarget, null, timeData);
    }

    public static <S extends Solution<S, I>, I extends Instance> WorkUnitResult<S,I> copyBestAlg(WorkUnitResult<S,I> workUnit) {
        return new WorkUnitResult<>(
                workUnit.resultId(),
                workUnit.success(),
                workUnit.experimentName(),
                workUnit.instancePath(),
                workUnit.instanceId(),
                workUnit.algorithm(),
                BEST_ITERATION,
                workUnit.solution(),
                workUnit.solutionProperties(),
                workUnit.executionTime(),
                workUnit.timeToTarget(),
                workUnit.metrics(),
                workUnit.timeData()
        );
    }

    public static <S extends Solution<S, I>, I extends Instance> WorkUnitResult<S,I> copyBestInstance(WorkUnitResult<S,I> workUnit) {
        return new WorkUnitResult<>(
                workUnit.resultId(),
                workUnit.success(),
                workUnit.experimentName(),
                workUnit.instancePath(),
                workUnit.instanceId(),
                new EmptyAlgorithm<>(BEST_ALGORITHM),
                BEST_ITERATION,
                workUnit.solution(),
                workUnit.solutionProperties(),
                workUnit.executionTime(),
                workUnit.timeToTarget(),
                workUnit.metrics(),
                workUnit.timeData()
        );
    }

    public WorkUnitResult {
        if (resultId == null || resultId.isBlank()) {
            throw new IllegalArgumentException("resultId cannot be null or blank");
        }
        solutionProperties = solutionProperties == null ? Map.of() : Map.copyOf(solutionProperties);
        timeData = timeData == null ? List.of() : List.copyOf(timeData);
    }

    public WorkUnitResult(boolean success, String experimentName, String instancePath, String instanceId, Algorithm<S,I> algorithm, int iteration, S solution, long executionTime, long timeToTarget, MetricsStorage metrics, List<TimeStatsEvent> timeData){
        this(success, experimentName, instancePath, instanceId, algorithm, String.valueOf(iteration), solution, executionTime, timeToTarget, metrics, timeData);
    }

    public WorkUnitResult(boolean success, String experimentName, String instancePath, String instanceId, Algorithm<S,I> algorithm, String iteration, S solution, long executionTime, long timeToTarget, MetricsStorage metrics, List<TimeStatsEvent> timeData){
        this(UUID.randomUUID().toString(), success, experimentName, instancePath, instanceId, algorithm, iteration, solution, computeSolutionProperties(solution), executionTime, timeToTarget, metrics, timeData);
    }

    public WorkUnitResult(boolean success, String experimentName, String instancePath, String instanceId, Algorithm<S,I> algorithm, String iteration, S solution, Map<String, Object> solutionProperties, long executionTime, long timeToTarget, MetricsStorage metrics, List<TimeStatsEvent> timeData){
        this(UUID.randomUUID().toString(), success, experimentName, instancePath, instanceId, algorithm, iteration, solution, solutionProperties, executionTime, timeToTarget, metrics, timeData);
    }

    public static <S extends Solution<S,I>, I extends Instance> Map<String, Object> computeSolutionProperties(S solution) {
        if(solution == null){
            return Map.of();
        }
        var generators = solution.customProperties();
        if(generators == null || generators.isEmpty()){
            return Map.of();
        }
        Map<String, Object> properties = HashMap.newHashMap(generators.size());
        for(var e: generators.entrySet()){
            properties.put(e.getKey(), e.getValue().apply(solution));
        }
        return properties;
    }

    public String getResultId() {
        return resultId;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getExperimentName() {
        return experimentName;
    }

    public String getInstancePath() {
        return instancePath;
    }

    public String getInstanceName() {
        return instanceId;
    }

    public String getAlgorithmName() {
        return algorithm.getName();
    }

    public String getIteration() {
        return iteration;
    }

    public Map<String, Double> getObjectives() {
        return solution == null ? Map.of() : Context.evalSolution(solution);
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public long getTimeToBest() {
        return timeToTarget;
    }

    public MetricsStorage getMetrics() {
        return metrics;
    }

    public List<TimeStatsEvent> getTimeStatsEvents() {
        return timeData;
    }

    public Optional<S> getSolution() {
        return Optional.ofNullable(solution);
    }

    public Map<String, Object> getSolutionProperties() {
        return solutionProperties;
    }

    public double getScore() {
        var objectives = getObjectives();
        if (objectives.isEmpty()) {
            return Double.NaN;
        }
        return objectives.values().iterator().next();
    }
}
