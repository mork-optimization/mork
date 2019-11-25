package solver.algorythms;

import io.Instance;
import solution.Solution;
import solver.create.Constructor;
import solver.improve.Improver;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

//TODO pass an executor to the algotythm, sequential or parallel
public class BestConstruction1LS extends Algorithm {

    private int executions;

    /**
     * Create a new MultiStartAlgorithm, @see algorithm
     * @param i Tries, the algorythm will be executed i times, returns the best.
     */
    @SafeVarargs
    public BestConstruction1LS(int i, Supplier<Constructor> constructorSupplier, Supplier<Improver>... improvers) {
        super(constructorSupplier, improvers);
        this.executions = i;
    }

    /**
     * Executes the algorythm for the given instance
     * @param ins Instance the algorithm will process
     * @return Best solution found
     */
    @Override
    public Solution algorithm(Instance ins, int hardCostLimit, int k, double[] weights) {
        Solution s = this.constructor.construct(ins,  hardCostLimit, k, weights);
        for (int i = 1; i < executions; i++) {
            Solution temp = this.constructor.construct(ins, hardCostLimit, k, weights);
            s = temp.getBetterSolution(s);
        }
        Solution javaPlis = s;
        improvers.forEach(i -> i.improve(javaPlis, 10, TimeUnit.MINUTES));
        return javaPlis;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "executions=" + executions +
                ", constructor=" + constructor +
                ", improver=" + improvers +
                '}';
    }
}
