package es.urjc.etsii.grafo.solver.improve;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.DoubleComparator;

import java.util.List;

/**
 * <p>VND class.</p>
 *
 */
public class VND<S extends Solution<S,I>,I extends Instance> extends Improver<S,I>{

    private final List<Improver<S,I>> improvers;
    private final boolean maximize;

    /**
     * <p>Constructor for VND.</p>
     *
     * @param improvers a {@link java.util.List} object.
     * @param maximize a boolean.
     */
    public VND(List<Improver<S, I>> improvers, boolean maximize) {
        this.improvers = improvers;
        this.maximize = maximize;
    }

    /** {@inheritDoc} */
    @Override
    protected S _improve(S s) {
        int currentLS = 0;
        while(currentLS < improvers.size()){
            double prev = s.getScore();
            var ls = improvers.get(currentLS);
            s = ls.improve(s);
            if(currentLS == 0){
                // Why repeat when improver stops when
                // it cannot improve the current solution?
                currentLS++;
            } else if (this.maximize) {
                if (DoubleComparator.isGreaterOrEqualsThan(prev, s.getScore())) {
                    // prev >= current, no improvement
                    currentLS++;
                } else {
                    currentLS = 0;
                }
            } else {
                if (DoubleComparator.isLessOrEquals(prev, s.getScore())) {
                    // prev <= current, no improvement
                    currentLS++;
                } else {
                    currentLS = 0;
                }
            }
        }
        return s;
    }


    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "VND{" +
                "imprs=" + improvers +
                '}';
    }
}
