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

    @AutoconfigConstructor
    public VND(
            @ComponentParam(disallowed = {VND.class, Improver.SequentialImprover.class})
            List<Improver<S,I>> improvers
    ) {
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
