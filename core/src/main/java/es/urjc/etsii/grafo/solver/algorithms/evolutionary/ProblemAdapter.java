package es.urjc.etsii.grafo.solver.algorithms.evolutionary;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.create.Constructive;
import es.urjc.etsii.grafo.solver.create.builder.SolutionBuilder;
import org.uma.jmetal.problem.Problem;

/**
 * JMetal problem adapter
 * @param <S> Solution class
 * @param <I> Instance class
 */
public class ProblemAdapter<S extends Solution<S, I>, I extends Instance> implements Problem<S> {

    private final I instance;
    private final SolutionBuilder<S, I> builder;
    protected final Constructive<S,I> constructive;

    public ProblemAdapter(I instance, SolutionBuilder<S,I> builder, Constructive<S, I> constructive) {
        this.instance = instance;
        this.builder = builder;
        this.constructive = constructive;
    }

    @Override
    public int getNumberOfVariables() {
        return 0;
    }

    @Override
    public int getNumberOfObjectives() {
        return 1;
    }

    @Override
    public int getNumberOfConstraints() {
        return 0;
    }

    @Override
    public String getName() {
        return instance.getName();
    }

    @Override
    public S evaluate(S solution) {
        // TODO
        return null;
    }

    @Override
    public S createSolution() {
        S emptySolution = builder.initializeSolution(instance);
        S solution = this.constructive.construct(emptySolution);
        return solution;
    }
}
