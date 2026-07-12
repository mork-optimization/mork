package es.urjc.etsii.grafo.events.types;

import es.urjc.etsii.grafo.executors.WorkUnitResult;

import java.util.Map;

/**
 * Lightweight event triggered each time an algorithm finishes creating a result.
 */
public class SolutionGeneratedEvent extends MorkEvent {
    private final String resultId;
    private final boolean success;
    private final String experimentName;
    private final String instanceName;
    private final String algorithmName;
    private final String iteration;
    private final Map<String, Double> objectives;
    private final double score;
    private final long executionTime;
    private final long timeToBest;

    /**
     * Create event from stored result data.
     *
     * @param result stored work-unit result
     */
    public SolutionGeneratedEvent(WorkUnitResult<?, ?> result) {
        this.resultId = result.getResultId();
        this.success = result.isSuccess();
        this.experimentName = result.getExperimentName();
        this.instanceName = result.getInstanceName();
        this.algorithmName = result.getAlgorithmName();
        this.iteration = result.getIteration();
        this.objectives = result.getObjectives();
        this.score = result.getScore();
        this.executionTime = result.getExecutionTime();
        this.timeToBest = result.getTimeToBest();
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

    public String getInstanceName() {
        return instanceName;
    }

    public String getAlgorithmName() {
        return algorithmName;
    }

    public String getIteration() {
        return iteration;
    }

    public Map<String, Double> getObjectives() {
        return objectives;
    }

    public double getScore() {
        return score;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public long getTimeToBest() {
        return timeToBest;
    }
}
