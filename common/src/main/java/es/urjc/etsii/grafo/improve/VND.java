package es.urjc.etsii.grafo.improve;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.DoubleComparator;

import java.util.List;

/**
 * <p>VND class.</p>
 *
 */
public class VND<S extends Solution<S,I>,I extends Instance> extends Improver<S,I> {

    private final List<Improver<S,I>> improvers;

    /**
     * <p>Constructor for VND.</p>
     *
     * @param improvers a {@link List} object.
     * @param maximize a boolean.
     */
    public VND(List<Improver<S, I>> improvers, boolean maximize) {
        super(maximize);
        this.improvers = improvers;
    }

    /**
     * <p>Constructor for VND.</p>
     * TODO: properly handle list of types that we are able to resolve
     * @param improver1 improver1
     * @param improver2 improver2
     * @param improver3 improver3
     * @param maximize a boolean.
     */
    @AutoconfigConstructor
    public VND(boolean maximize, Improver<S, I> improver1, Improver<S, I> improver2, Improver<S, I> improver3) {
        super(maximize);
        this.improvers = List.of(improver1, improver2, improver3);
    }

    /** {@inheritDoc} */
    @Override
    protected S _improve(S solution) {
        int currentLS = 0;
        while(currentLS < improvers.size()){
            double prev = solution.getScore();
            var ls = improvers.get(currentLS);
            solution = ls.improve(solution);
            if(currentLS == 0){
                // Why repeat when improver stops when
                // it cannot improve the current solution?
                currentLS++;
            } else if (this.ofMaximize) {
                if (DoubleComparator.isGreaterOrEquals(prev, solution.getScore())) {
                    // prev >= current, no improvement
                    currentLS++;
                } else {
                    currentLS = 0;
                }
            } else {
                if (DoubleComparator.isLessOrEquals(prev, solution.getScore())) {
                    // prev <= current, no improvement
                    currentLS++;
                } else {
                    currentLS = 0;
                }
            }
        }
        return solution;
    }


    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "VND{" +
                "imprs=" + improvers +
                '}';
    }
}
