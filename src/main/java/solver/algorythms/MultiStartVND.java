package solver.algorythms;

import io.Instance;
import solution.Solution;
import solver.create.Constructor;
import solver.improve.Improver;
import solver.improve.VND;

import java.util.function.Supplier;

public class MultiStartVND extends Algorithm {

    private int executions;
    private VND vnd;
    /**
     * Create a new MultiStartAlgorithm, @see algorithm
     * @param nStarts Tries, the algorythm will be executed i times, returns the best.
     *          If -1, calculate automatically the number of iterations
     */
    @SafeVarargs
    public MultiStartVND(int nStarts, Supplier<Constructor> constructorSupplier, Supplier<Improver>... improvers) {
        super(constructorSupplier, improvers);
        this.executions = nStarts;
        vnd = new VND();
    }

    /**
     * Executes the algorythm for the given instance
     * @param ins Instance the algorithm will process
     * @return Best solution found
     */
    @Override
    public Solution algorithm(Instance ins) {

        Solution best = null;
        for (int i = 1; i <= executions; i++) {
            Solution current = this.constructor.construct(ins);
            current = vnd.doIt(current, improvers);
            best = current.getBetterSolution(best);
        }

        return best;
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
