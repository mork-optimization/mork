package es.urjc.etsii.grafo.solver.services.events.types;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.algorithms.Algorithm;

import java.lang.ref.SoftReference;
import java.util.Optional;

/**
 * Event triggered each time an algorithm finishes creating a solution.
 * Contains information about the different solution characteristics.
 * This event is created and dispatched automatically.
 * @param <S> Solution type
 * @param <I> Instance type
 */
public class SolutionGeneratedEvent<S extends Solution<I>, I extends Instance> extends MorkEvent {
    private final String experimentName;
    private final String instanceName;
    private final Algorithm<S,I> algorithm;
    private final int iteration;
    private final double score;
    private final long executionTime;
    private final long timeToBest;
    private final SoftReference<S> solution;

    public SolutionGeneratedEvent(int iteration, S solution, String experimentName, Algorithm<S, I> algorithm, long executionTime, long timeToBest) {
        super();
        this.iteration = iteration;
        this.score = solution.getScore();
        this.instanceName = solution.getInstance().getName();
        this.solution = new SoftReference<>(solution);
        this.experimentName = experimentName;
        this.algorithm = algorithm;
        this.executionTime = executionTime;
        this.timeToBest = timeToBest;
    }

    /**
     * Which iteration this solution corresponds to
     * @return int
     */
    public int getIteration() {
        return iteration;
    }

    /**
     * Experiment name
     * @return String representing the experiment name
     */
    public String getExperimentName() {
        return experimentName;
    }

    /**
     * Algorithm that created this solution
     * @return Algorithm configuration
     */
    public Algorithm<S, I> getAlgorithm() {
        return algorithm;
    }

    /**
     * Instance used to create this solution
     * @return Instance name
     */
    public String getInstanceName() {
        return instanceName;
    }

    /**
     * Solution score when finished processing a solution.
     * @return solution score
     */
    public double getScore() {
        return score;
    }

    /**
     * Total execution time in nanoseconds used by the algorithm to create the solution.
     * @return time in nanos
     */
    public long getExecutionTime() {
        return executionTime;
    }

    /**
     * Execution time until last modification in nanoseconds
     * @return time in nanos
     */
    public long getTimeToBest() {
        return timeToBest;
    }

    /**
     * Get solution instance IF AVAILABLE
     * Storing all generated solutions is too memory expensive.
     * @return Empty optional if solution has been garbage collected, solution data if not.
     */
    public Optional<S> getSolution() {
        return Optional.ofNullable(this.solution.get());
    }
}
