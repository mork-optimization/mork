package es.urjc.etsii.grafo.executors;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.algorithms.EmptyAlgorithm;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.metrics.MetricsStorage;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.Context;
import es.urjc.etsii.grafo.util.TimeStatsEvent;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Result of executing one work unit.
 *
 * <p>Objective values and custom properties are evaluated when the result is
 * created. They therefore describe the solution at the end of the work unit,
 * even if the solution is later mutated or released from memory.</p>
 */
public record WorkUnitResult<S extends Solution<S, I>, I extends Instance>(
        UUID resultId,
        boolean success,
        String experimentName,
        String instancePath,
        String instanceId,
        Algorithm<S, I> algorithm,
        String iteration,
        S solution,
        Map<String, Double> objectives,
        Map<String, Object> solutionProperties,
        long executionTime,
        long timeToTarget,
        MetricsStorage metrics,
        List<TimeStatsEvent> timeData
) {

    public static final String BEST_ALGORITHM = "bestalg";
    public static final String BEST_ITERATION = "bestiter";

    public WorkUnitResult {
        Objects.requireNonNull(resultId, "resultId cannot be null");
        objectives = immutableOrderedMap(objectives, "objective");
        solutionProperties = immutableOrderedMap(solutionProperties, "custom property");
        timeData = timeData == null ? List.of() : List.copyOf(timeData);
    }

    public static <S extends Solution<S, I>, I extends Instance> WorkUnitResult<S, I> ok(
            WorkUnit<S, I> workUnit,
            String instanceId,
            S solution,
            long executionTime,
            long timeToTarget,
            MetricsStorage metrics,
            List<TimeStatsEvent> timeData
    ) {
        return new WorkUnitResult<>(
                UUID.randomUUID(),
                true,
                workUnit.experimentName(),
                workUnit.instancePath(),
                instanceId,
                workUnit.algorithm(),
                String.valueOf(workUnit.i()),
                solution,
                computeObjectives(solution),
                computeSolutionProperties(solution),
                executionTime,
                timeToTarget,
                metrics,
                timeData
        );
    }

    public static <S extends Solution<S, I>, I extends Instance> WorkUnitResult<S, I> failure(
            WorkUnit<S, I> workUnit,
            String instanceId,
            long executionTime,
            long timeToTarget,
            List<TimeStatsEvent> timeData
    ) {
        return new WorkUnitResult<>(
                UUID.randomUUID(),
                false,
                workUnit.experimentName(),
                workUnit.instancePath(),
                instanceId,
                workUnit.algorithm(),
                String.valueOf(workUnit.i()),
                null,
                Map.of(),
                Map.of(),
                executionTime,
                timeToTarget,
                null,
                timeData
        );
    }

    public static <S extends Solution<S, I>, I extends Instance> WorkUnitResult<S, I> copyBestAlg(
            WorkUnitResult<S, I> workUnit
    ) {
        return new WorkUnitResult<>(
                workUnit.resultId(),
                workUnit.success(),
                workUnit.experimentName(),
                workUnit.instancePath(),
                workUnit.instanceId(),
                workUnit.algorithm(),
                BEST_ITERATION,
                workUnit.solution(),
                workUnit.objectives(),
                workUnit.solutionProperties(),
                workUnit.executionTime(),
                workUnit.timeToTarget(),
                workUnit.metrics(),
                workUnit.timeData()
        );
    }

    public static <S extends Solution<S, I>, I extends Instance> WorkUnitResult<S, I> copyBestInstance(
            WorkUnitResult<S, I> workUnit
    ) {
        return new WorkUnitResult<>(
                workUnit.resultId(),
                workUnit.success(),
                workUnit.experimentName(),
                workUnit.instancePath(),
                workUnit.instanceId(),
                new EmptyAlgorithm<>(BEST_ALGORITHM),
                BEST_ITERATION,
                workUnit.solution(),
                workUnit.objectives(),
                workUnit.solutionProperties(),
                workUnit.executionTime(),
                workUnit.timeToTarget(),
                workUnit.metrics(),
                workUnit.timeData()
        );
    }

    /**
     * Evaluate all configured objectives once, retaining their declaration order.
     */
    public static <S extends Solution<S, I>, I extends Instance> Map<String, Double> computeObjectives(S solution) {
        if (solution == null) {
            return Map.of();
        }

        var configuredObjectives = Context.<S, I>getObjectives();
        if (configuredObjectives == null || configuredObjectives.isEmpty()) {
            return Map.of();
        }
        var values = new LinkedHashMap<String, Double>(configuredObjectives.size());
        for (var entry : configuredObjectives.entrySet()) {
            values.put(entry.getKey(), entry.getValue().evalSol(solution));
        }
        return Collections.unmodifiableMap(values);
    }

    /**
     * Evaluate all solution-defined custom properties once.
     */
    public static <S extends Solution<S, I>, I extends Instance> Map<String, Object> computeSolutionProperties(S solution) {
        if (solution == null) {
            return Map.of();
        }
        var generators = solution.customProperties();
        if (generators == null || generators.isEmpty()) {
            return Map.of();
        }

        var properties = new LinkedHashMap<String, Object>(generators.size());
        for (var entry : generators.entrySet()) {
            String name = Objects.requireNonNull(entry.getKey(), "Custom property name cannot be null");
            var generator = Objects.requireNonNull(
                    entry.getValue(),
                    "Custom property generator '%s' cannot be null".formatted(name)
            );
            Object value = generator.apply(solution);
            if (value == null) {
                throw new IllegalArgumentException(
                        "Custom property '%s' evaluated to null; custom property values must be non-null".formatted(name)
                );
            }
            properties.put(name, value);
        }
        return Collections.unmodifiableMap(properties);
    }

    /**
     * Value of the first declared objective, or {@link Double#NaN} when no
     * objective value is available.
     */
    public double mainObjectiveValue() {
        return objectives.isEmpty() ? Double.NaN : objectives.values().iterator().next();
    }

    /**
     * Return a copy that no longer strongly references its solution.
     */
    public WorkUnitResult<S, I> withoutSolution() {
        if (solution == null) {
            return this;
        }
        return new WorkUnitResult<>(
                resultId,
                success,
                experimentName,
                instancePath,
                instanceId,
                algorithm,
                iteration,
                null,
                objectives,
                solutionProperties,
                executionTime,
                timeToTarget,
                metrics,
                timeData
        );
    }

    private static <K, V> Map<K, V> immutableOrderedMap(Map<K, V> source, String valueDescription) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }

        var copy = new LinkedHashMap<K, V>(source.size());
        for (var entry : source.entrySet()) {
            K key = Objects.requireNonNull(
                    entry.getKey(),
                    "%s name cannot be null".formatted(capitalize(valueDescription))
            );
            if (entry.getValue() == null) {
                throw new IllegalArgumentException(
                        "%s '%s' cannot have a null value".formatted(capitalize(valueDescription), key)
                );
            }
            copy.put(key, entry.getValue());
        }
        return Collections.unmodifiableMap(copy);
    }

    private static String capitalize(String value) {
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }
}
