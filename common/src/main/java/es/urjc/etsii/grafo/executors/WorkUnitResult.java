package es.urjc.etsii.grafo.executors;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.algorithms.EmptyAlgorithm;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.metrics.MetricsStorage;
import es.urjc.etsii.grafo.solution.Solution;

public record WorkUnitResult<S extends Solution<S, I>, I extends Instance>(boolean success, String experimentName, String instancePath, Algorithm<S,I> algorithm, String iteration, S solution, long executionTime, long timeToTarget, MetricsStorage metrics) {

    public static <S extends Solution<S, I>, I extends Instance> WorkUnitResult<S,I> ok(WorkUnit<S,I> workUnit, S solution, long executionTime, long timeToTarget, MetricsStorage metrics) {
        return new WorkUnitResult<>(true, workUnit.experimentName(), workUnit.instancePath(), workUnit.algorithm(), workUnit.i(), solution, executionTime, timeToTarget, metrics);
    }

    public static <S extends Solution<S, I>, I extends Instance> WorkUnitResult<S,I> failure(WorkUnit<S,I> workUnit, long executionTime, long timeToTarget) {
        return new WorkUnitResult<>(false, workUnit.experimentName(), workUnit.instancePath(), workUnit.algorithm(), workUnit.i(), null, executionTime, timeToTarget, null);
    }

    public static <S extends Solution<S, I>, I extends Instance> WorkUnitResult<S,I> copyBestAlg(WorkUnitResult<S,I> workUnit) {
        return new WorkUnitResult<>(workUnit.success(), workUnit.experimentName(), workUnit.instancePath(), workUnit.algorithm(), "bestiter", workUnit.solution(), workUnit.executionTime(), workUnit.timeToTarget(), workUnit.metrics());
    }

    public static <S extends Solution<S, I>, I extends Instance> WorkUnitResult<S,I> copyBestInstance(WorkUnitResult<S,I> workUnit) {
        return new WorkUnitResult<>(workUnit.success(), workUnit.experimentName(), workUnit.instancePath(), new EmptyAlgorithm<>("bestalg"), "bestiter", workUnit.solution(), workUnit.executionTime(), workUnit.timeToTarget(), workUnit.metrics());
    }

    public WorkUnitResult(boolean success, String experimentName, String instancePath, Algorithm<S,I> algorithm, int iteration, S solution, long executionTime, long timeToTarget, MetricsStorage metrics){
        this(success, experimentName, instancePath, algorithm, String.valueOf(iteration), solution, executionTime, timeToTarget, metrics);
    }


}
