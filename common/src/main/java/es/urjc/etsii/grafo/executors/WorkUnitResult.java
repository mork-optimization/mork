package es.urjc.etsii.grafo.executors;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.algorithms.EmptyAlgorithm;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.metrics.MetricsStorage;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.TimeStatsEvent;

import java.util.List;

public record WorkUnitResult<S extends Solution<S, I>, I extends Instance>(boolean success, String experimentName, String instancePath, String instanceId, Algorithm<S,I> algorithm, String iteration, S solution, long executionTime, long timeToTarget, MetricsStorage metrics, List<TimeStatsEvent> timeData) {

    public static <S extends Solution<S, I>, I extends Instance> WorkUnitResult<S,I> ok(WorkUnit<S,I> workUnit, String instanceId, S solution, long executionTime, long timeToTarget, MetricsStorage metrics, List<TimeStatsEvent> timeData) {
        return new WorkUnitResult<>(true, workUnit.experimentName(), workUnit.instancePath(), instanceId, workUnit.algorithm(), workUnit.i(), solution, executionTime, timeToTarget, metrics, timeData);
    }

    public static <S extends Solution<S, I>, I extends Instance> WorkUnitResult<S,I> failure(WorkUnit<S,I> workUnit, String instanceId, long executionTime, long timeToTarget, List<TimeStatsEvent> timeData) {
        return new WorkUnitResult<>(false, workUnit.experimentName(), workUnit.instancePath(), instanceId, workUnit.algorithm(), workUnit.i(), null, executionTime, timeToTarget, null, timeData);
    }

    public static <S extends Solution<S, I>, I extends Instance> WorkUnitResult<S,I> copyBestAlg(WorkUnitResult<S,I> workUnit) {
        return new WorkUnitResult<>(workUnit.success(), workUnit.experimentName(), workUnit.instancePath(), workUnit.instanceId(), workUnit.algorithm(), "bestiter", workUnit.solution(), workUnit.executionTime(), workUnit.timeToTarget(), workUnit.metrics(), workUnit.timeData());
    }

    public static <S extends Solution<S, I>, I extends Instance> WorkUnitResult<S,I> copyBestInstance(WorkUnitResult<S,I> workUnit) {
        return new WorkUnitResult<>(workUnit.success(), workUnit.experimentName(), workUnit.instancePath(), workUnit.instanceId(), new EmptyAlgorithm<>("bestalg"), "bestiter", workUnit.solution(), workUnit.executionTime(), workUnit.timeToTarget(), workUnit.metrics(), workUnit.timeData());
    }

    public WorkUnitResult(boolean success, String experimentName, String instancePath, String instanceId, Algorithm<S,I> algorithm, int iteration, S solution, long executionTime, long timeToTarget, MetricsStorage metrics, List<TimeStatsEvent> timeData){
        this(success, experimentName, instancePath, instanceId, algorithm, String.valueOf(iteration), solution, executionTime, timeToTarget, metrics, timeData);
    }
}
