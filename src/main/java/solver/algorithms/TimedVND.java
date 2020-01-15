package solver.algorithms;

import io.Instance;
import solution.Solution;
import solver.create.Constructor;
import solver.improve.Improver;
import util.DoubleComparator;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class TimedVND extends Algorithm{

    private long limit;

    /**
     * Create a new MultiStartAlgorithm, @see algorithm
     * @param timeLimit maximum time for the algorithm, will be respected in a best effort manner
     * @param unit in which unit is the previous time
     */
    @SafeVarargs
    public TimedVND(int timeLimit, TimeUnit unit, Supplier<Constructor> constructorSupplier, Supplier<Improver>... improvers) {
        super(constructorSupplier, improvers);
        this.limit = unit.toNanos(timeLimit);
    }

    /**
     * Executes the algorythm for the given instance
     * @param ins Instance the algorithm will process
     * @return Best solution found
     */
    @Override
    public Solution algorithm(Instance ins) {
        Solution best = this.constructor.construct(ins);
        long nanos = System.nanoTime();
        while (System.nanoTime() - nanos < limit) {
            Solution current = this.constructor.construct(ins);
            int currentLS = 0;
            while(System.nanoTime() - nanos < limit && currentLS < this.improvers.size()){
                double prev = current.getOptimalValue();
                current = this.improvers.get(currentLS).improve(current, limit - (System.nanoTime() - nanos), TimeUnit.NANOSECONDS);
                if (DoubleComparator.isGreaterOrEqualsThan(prev, current.getOptimalValue())) {
                    currentLS++;
                } else {
                    currentLS = 0;
                }
            }

            best = current.getBetterSolution(best);
        }

        return best;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                ", timLimit in ns=" + this.limit +
                ", constructor=" + constructor +
                ", improver=" + improvers +
                '}';
    }
}
