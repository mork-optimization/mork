package es.urjc.etsii.grafo.events.types;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.metrics.MetricsStorage;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.Context;
import es.urjc.etsii.grafo.util.TimeStatsEvent;

import java.lang.ref.SoftReference;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Event triggered each time an algorithm finishes creating a solution.
 * Contains information about the different solution characteristics.
 * This event is created and dispatched automatically.
 *
 * @param <S> Solution type
 * @param <I> Instance type
 */
public class SolutionGeneratedEvent<S extends Solution<S,I>, I extends Instance> extends MorkEvent {
    private final String experimentName;
    private final String instanceName;
    private final String algorithmName;
    private final MetricsStorage metrics;
    private final List<TimeStatsEvent> timeStatsEvents;
    private final boolean success;
    private final String iteration;
    private final String instancePath;
    private final Map<String, Double> objectives;
    private final long executionTime;
    private final long timeToBest;
    private final Algorithm<S,I> algorithm;
    private final SoftReference<S> solution;

    /**
     * Create a new SolutionGeneratedEvent
     *
     * @param iteration       solution iteration
     * @param solution        generated solution
     * @param experimentName  experiment name
     * @param algorithm       algorithm that generated this solution
     * @param executionTime   time used to generate this solution
     * @param timeToBest      time needed ot reach the best solution. timeToBest = totalTime - timeSinceLastModification
     * @param metrics         both framework calculated and user defined metrics
     * @param timeStatsEvents
     */
    public SolutionGeneratedEvent(boolean success, String iteration, String instancePath, S solution, String experimentName, Algorithm<S, I> algorithm, long executionTime, long timeToBest, MetricsStorage metrics, List<TimeStatsEvent> timeStatsEvents) {
        super();
        this.success = success;
        this.iteration = iteration;
        this.instancePath = instancePath;
        this.solution = new SoftReference<>(solution);
        this.experimentName = experimentName;
        this.algorithm = algorithm;
        this.executionTime = executionTime;
        this.timeToBest = timeToBest;
        this.algorithmName = algorithm.getName();
        this.metrics = metrics;
        this.timeStatsEvents = timeStatsEvents;
        if(solution != null){
            this.objectives = Context.evalSolution(solution);
            this.instanceName = solution.getInstance().getId();
        } else {
            this.instanceName = "Unknown";
            this.objectives = Map.of();
        }
    }

    /**
     * Which iteration this solution corresponds to
     *
     * @return iteration
     */
    public String getIteration() {
        return iteration;
    }

    /**
     * Experiment name
     *
     * @return String representing the experiment name
     */
    public String getExperimentName() {
        return experimentName;
    }

    /**
     * Algorithm that created this solution
     *
     * @return Algorithm configuration
     */
    public Algorithm<S, I> getAlgorithm() {
        return algorithm;
    }

    /**
     * Instance used to create this solution
     *
     * @return Instance name
     */
    public String getInstanceName() {
        return instanceName;
    }

    /**
     * Solution score when finished processing a solution.
     *
     * @return solution score
     */
    public Map<String, Double> getObjectives() {
        return objectives;
    }

    /**
     * Total execution time in nanoseconds used by the algorithm to create the solution.
     *
     * @return time in nanos
     */
    public long getExecutionTime() {
        return executionTime;
    }

    /**
     * Execution time until last modification in nanoseconds
     *
     * @return time in nanos
     */
    public long getTimeToBest() {
        return timeToBest;
    }

    /**
     * Get short algorithm name that generated this solution
     *
     * @return Short string representing the algorithm that generated this solution
     */
    public String getAlgorithmName() {
        return algorithmName;
    }

    /**
     * Get solution instance IF AVAILABLE
     * Storing all generated solutions is too memory expensive.
     *
     * @return Empty optional if solution has been garbage collected, solution data if not.
     */
    public Optional<S> getSolution() {
        return Optional.ofNullable(this.solution.get());
    }

    /**
     * Get both framework calculated and user defined properties
     * @return Map where key is metric name, value property value as calculated by the user provided function
     */
    public MetricsStorage getMetrics() {
        return metrics;
    }

    /**
     * Was the solution generated successfully?
     * @return true if the solution was generated successfully, false otherwise
     */
    public boolean isSuccess() {
        return success;
    }
}
