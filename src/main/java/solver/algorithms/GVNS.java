package solver.algorithms;

import io.Instance;
import solution.Solution;
import solver.create.Constructor;
import solver.destructor.Shake;
import solver.improve.Improver;
import solver.improve.VND;
import util.DoubleComparator;

import java.util.function.Supplier;

public class GVNS extends Algorithm{

    private final int maxK;
    private final Shake shake;
    private final VND vnd;

    /**
     * Create a new MultiStartAlgorithm, @see algorithm
     */
    @SafeVarargs
    public GVNS(int maxK, Supplier<Constructor> constructorSupplier, Supplier<Shake> destructor, Supplier<Improver>... improvers) {
        super(constructorSupplier, improvers);
        this.maxK = maxK;
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

        Solution current = this.constructor.construct(ins);
        current = vnd.doIt(current, improvers);
        int currentK = 0;
        while(currentK < maxK){
            Solution cloned = current.clone();
            this.shake.iteration(cloned, currentK);
            this.vnd.doIt(cloned, this.improvers);
            if(DoubleComparator.isPositiveOrZero(current.getOptimalValue() - cloned.getOptimalValue())){
                currentK++;
            } else {
                current = cloned;
                currentK = 1;
            }
        }

        return current;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{" +
                ", maxK=" + maxK +
                ", constructor=" + constructor +
                ", destructor=" + shake +
                ", improver=" + improvers +
                '}';
    }
}
