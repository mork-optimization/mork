package solver.algorythms;

import io.Instance;
import solution.Solution;
import solver.create.Constructor;
import solver.destructor.Shake;
import solver.improve.Improver;
import solver.improve.VND;
import util.ConcurrencyUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import static util.ConcurrencyUtil.await;

public class MultiStartILS extends Algorithm{

    private final int nShakes;
    private final int k;
    private final int n;
    private final Shake shake;
    private final VND vnd;

    /**
     * Create a new MultiStartAlgorithm, @see algorithm
     * @param nShakes number of times we are going to shake the solution
     *          If -1, calculate automatically the number of iterations
     * @param n number of different solutions to evaluate
     * @param k destructor strength
     */
    @SafeVarargs
    public MultiStartILS(int nShakes, int k, int n, Supplier<Constructor> constructorSupplier, Supplier<Shake> destructor, Supplier<Improver>... improvers) {
        super(constructorSupplier, improvers);
        this.nShakes = nShakes;
        this.k = k;
        this.n = n;
        this.shake = destructor.get();
        vnd = new VND();
    }

    /**
     * Executes the algorythm for the given instance
     * @param ins Instance the algorithm will process
     * @return Best solution found
     */
    @Override
    public Solution algorithm(Instance ins) {
        int nThreads = ConcurrencyUtil.getNThreads();
        var executor = Executors.newFixedThreadPool(nThreads);
        var futures = Collections.synchronizedList(new ArrayList<Future<Solution>>());
        for (int i = 0; i < n; i++) {
            futures.add(executor.submit(() -> getSolution(ins)));
        }
        Solution best = null;
        for (var future : futures) {
            Solution s = await(future);
            if(best == null || s.getOptimalValue() > best.getOptimalValue()){
                best = s;
            }
        }

        return best;
    }

    private Solution getSolution(Instance ins) {
        Solution best = this.constructor.construct(ins);
        best = vnd.doIt(best, improvers);

        for (int i = 1; i <= this.nShakes; i++) {
            var copy = best.clone();
            this.shake.iteration(copy, k);
            copy = this.vnd.doIt(copy, this.improvers);
            best = copy.getBetterSolution(best);
        }

        return best;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                "iterations=" + (nShakes ==-1? "AUTO": nShakes) +
                ", n=" + n +
                ", k=" + k +
                ", constructor=" + constructor +
                ", destructor=" + shake +
                ", improver=" + improvers +
                '}';
    }
}
