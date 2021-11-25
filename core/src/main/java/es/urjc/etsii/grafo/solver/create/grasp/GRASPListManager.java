package es.urjc.etsii.grafo.solver.create.grasp;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.Solution;

import java.util.List;

/**
 * Creates and updates the candidate list when a movement is performed
 */
public abstract class GRASPListManager<M extends Move<S, I>, S extends Solution<S,I>, I extends Instance> {
    /**
     * Initialize solution before GRASP algorithm is run
     * F.e: In the case of clustering algorithms, usually each cluster needs to have at least one point,
     * different solutions types may require different initialization
     *
     * @param s Solution to initialize before running the GRASP constructive method
     */
    public void beforeGRASP(S s){

    }


    /**
     * Generate initial candidate list. The list will be sorted if necessary by the constructive method.
     *
     * @param solution Current solution
     * @return a candidate list
     */
    public abstract List<M> buildInitialCandidateList(S solution);

    /**
     * Update candidate list after each movement. The list will be sorted by the constructor.
     *
     * @param solution Current solution, move has been already applied
     * @param move     Chosen move
     * @param index index of the chosen move in the candidate list
     * @param candidateList original candidate list
     * @return an UNSORTED candidate list, where the best candidate is on the first position and the worst in the last
     */
    public abstract List<M> updateCandidateList(S solution, M move, List<M> candidateList, int index);

    /**
     * {@inheritDoc}
     *
     * Return string representation of the current list manager.
     * Defaults to "classname{}", override if custom parameters should appear in the string.
     */
    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{}";
    }
}
