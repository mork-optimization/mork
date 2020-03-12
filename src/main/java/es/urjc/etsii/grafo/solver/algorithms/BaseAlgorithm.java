package es.urjc.etsii.grafo.solver.algorithms;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.io.Result;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.solver.create.Constructor;
import es.urjc.etsii.grafo.solver.improve.Improver;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class BaseAlgorithm<S extends Solution<I>, I extends Instance> implements Algorithm<S,I>{

    List<Improver<S,I>> improvers;
    Constructor<S,I> constructor;

    @SafeVarargs
    BaseAlgorithm(Supplier<Constructor<S,I>> constructorSupplier, Supplier<Improver<S,I>>... improvers){
        this.constructor = constructorSupplier.get();
        this.improvers = Arrays.stream(improvers).map(Supplier::get).collect(Collectors.toList());
    }

    /**
     * Executes an algorithm, repeating the execution N times.
     * @param ins Instance to use
     * @param repetitions How many times should we repeat the experiment to make it more robust.
     * @return Result of the execution
     */
    public Result execute(I ins, int repetitions) {
        Result result = new Result(repetitions, this.toString(), ins.getName());
        for (int i = 0; i < repetitions; i++) {
            long startTime = System.nanoTime();
            Solution<I> s = algorithm(ins);
            long ellapsedTime = System.nanoTime() - startTime;
            System.out.format("\t%s.\tOptimal Value: %.3f -- Time: %.3f\n", i+1, s.getOptimalValue(), ellapsedTime / 1000000000D);
            result.addSolution(s, ellapsedTime);
        }

        return result;
    }

    /**
     * Algorithm implementation
     * @param ins Instance the algorithm will process
     * @return Proposed es.urjc.etsii.grafo.solution
     */
    protected abstract Solution<I> algorithm(I ins);

    /**
     * Current algorithm short name, must be unique per execution
     * @return Alg. name, can include parameter configuration
     */
    public String getShortName(){
        return this.getClass().getSimpleName();
    }

}
