package es.urjc.etsii.grafo.solver.algorithms;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.io.WorkingOnResult;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.create.SolutionBuilder;
import es.urjc.etsii.grafo.util.RandomManager;

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
        // Reset random state before each algorithm/instance pair execution
        RandomManager.reset();
        WorkingOnResult result = new WorkingOnResult(repetitions, this.toString(), ins.getName());
        for (int i = 0; i < repetitions; i++) {
            long startTime = System.nanoTime();
            Solution<I> s = algorithm(ins);
            long currentTime = System.nanoTime();
            long ellapsedTime = currentTime - startTime;
            long timeToTarget = s.getLastModifiedTime() - startTime;
            System.out.format("\t%s.\tTime: %.3f (s) \tTTT: %.3f (s) \t%s -- \n", i+1, ellapsedTime / 1000_000_000D, timeToTarget / 1000_000_000D, s);
            result.addSolution(s, ellapsedTime, timeToTarget);
        }

        return result;
    }

    protected abstract Solution<I> algorithm(I ins);

}
