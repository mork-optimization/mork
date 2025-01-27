package es.urjc.etsii.grafo.improve;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.annotations.ComponentParam;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.Context;

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
     * @param objective objective to optimize
     */
    public VND(List<Improver<S, I>> improvers, Objective<?,S,I> objective) {
        super(objective);
        this.improvers = improvers;
    }

    /**
     * <p>Constructor for VND.</p>
     * TODO: properly handle list of types that we are able to resolve
     * @param improver1 improver1
     * @param improver2 improver2
     * @param improver3 improver3
     */
    @AutoconfigConstructor
    public VND(
            @ComponentParam(disallowed = {VND.class}) Improver<S,I> improver1,
            @ComponentParam(disallowed = {VND.class}) Improver<S,I> improver2,
            @ComponentParam(disallowed = {VND.class}) Improver<S,I> improver3
    ) {
        this(List.of(improver1, improver2, improver3));
    }

    public VND(List<Improver<S,I>> improvers) {
        this(improvers, Context.getMainObjective());
    }

    /** {@inheritDoc} */
    @Override
    public S improve(S solution) {
        int index = 0;
        while(index < improvers.size()){
            double scoreBeforeImprover = objective.evalSol(solution);
            var improver = improvers.get(index);
            solution = improver.improve(solution);

            if(objective.isBetter(solution,  scoreBeforeImprover)){
                index = 0;
            } else {
                index++;
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
