package es.urjc.etsii.grafo.solver.algorithms;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.io.WorkingOnResult;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.create.SolutionBuilder;

public abstract class BaseAlgorithm<S extends Solution<I>, I extends Instance> implements Algorithm<S,I> {

    protected final SolutionBuilder<S,I> builder;

    protected BaseAlgorithm(SolutionBuilder<S, I> builder) {
        this.builder = builder;
    }

    /**
     * Executes an algorithm, repeating the execution N times.
     * @param ins Instance to use
     * @param repetitions How many times should we repeat the experiment to make it more robust.
     * @return Result of the execution
     */
    public WorkingOnResult execute(I ins, int repetitions) {
        WorkingOnResult result = new WorkingOnResult(repetitions, this.toString(), ins.getName());
        for (int i = 0; i < repetitions; i++) {
            long startTime = System.nanoTime();
            Solution<I> s = algorithm(ins);
            long ellapsedTime = System.nanoTime() - startTime;
            System.out.format("\t%s.\tOptimal Value: %.3f -- Time: %.3f (s)\n", i+1, s.getOptimalValue(), ellapsedTime / 1000_000_000D);
            result.addSolution(s, ellapsedTime);
        }

        return result;
    }

    protected abstract Solution<I> algorithm(I ins);

}
