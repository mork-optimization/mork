package solver.algorythms;

import io.Instance;
import io.Result;
import solution.Solution;
import solver.create.Constructor;
import solver.improve.Improver;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class Algorithm {

    List<Improver> improvers;
    Constructor constructor;

    @SafeVarargs
    Algorithm(Supplier<Constructor> constructorSupplier, Supplier<Improver>... improvers){
        this.constructor = constructorSupplier.get();
        this.improvers = Arrays.stream(improvers).map(Supplier::get).collect(Collectors.toList());
    }

    /**
     * Executes an algorithm, repeating the execution N times.
     * @param ins Instance to use
     * @param repetitions How many times should we repeat the experiment to make it more robust.
     * @return Result of the execution
     */
    public Result execute(Instance ins, int repetitions, int hardCostLimit, int k, double[] weights) {
        Result result = new Result(repetitions, this.toString(), ins.getName());
        for (int i = 0; i < repetitions; i++) {
            long startTime = System.nanoTime();
            Solution s = algorithm(ins, hardCostLimit, k, weights);
            long ellapsedTime = System.nanoTime() - startTime;
            System.out.format("\t%s.\tBenefit: %.3f -- Time: %.3f\n", i+1, s.getOptimalValue(), ellapsedTime / 1000000000D);
            result.addSolution(s, ellapsedTime);
        }

        return result;
    }

    /**
     * Algorithm implementation
     * @param ins Instance the algorithm will process
     * @return Proposed solution
     */
    protected abstract Solution algorithm(Instance ins, int hardCostLimit, int k, double[] weights);

    /**
     * Current algorithm short name, must be unique per execution
     * @return Alg. name, can include parameter configuration
     */
    public String getShortName(){
        return this.getClass().getSimpleName();
    }

}
