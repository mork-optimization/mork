package es.urjc.etsii.grafo.solver.services.events;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.algorithms.Algorithm;

public class SolutionGeneratedEvent<S extends Solution<I>, I extends Instance> extends MorkEvent {
    private final int iteration;
    private final S solution;
    private final String experimentName;
    private final Algorithm<S,I> algorithm;

    public SolutionGeneratedEvent(int iteration, S solution, String experimentName, Algorithm<S, I> algorithm) {
        super();
        this.iteration = iteration;
        this.solution = solution;
        this.experimentName = experimentName;
        this.algorithm = algorithm;
    }

    public int getIteration() {
        return iteration;
    }

    public S getSolution() {
        return solution;
    }

    public String getExperimentName() {
        return experimentName;
    }

    public Algorithm<S, I> getAlgorithm() {
        return algorithm;
    }
}
