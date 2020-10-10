package es.urjc.etsii.grafo.solver.create.grasp;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.Solution;

import java.util.List;

/**
 * Creates and updates the candidate list when a movement is performed
 */
public abstract class GRASPListManager<M extends Move<S, I>, S extends Solution<I>, I extends Instance> {
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
     * Generate initial candidate list. The list will be sorted by the constructor.
     *
     * @param sol Current es.urjc.etsii.grafo.solution
     * @return an UNSORTED candidate list, where the best candidate is on the first position and the worst in the last
     */
    public abstract List<M> buildInitialCandidateList(S sol);

    /**
     * Update candidate list after each movement. The list will be sorted by the constructor.
     *
     * @param s     Current es.urjc.etsii.grafo.solution, move has been already applied
     * @param t     Chosen move
     * @param index index of the chosen move in the candidate list
     * @return an UNSORTED candidate list, where the best candidate is on the first position and the worst in the last
     */
    public abstract List<M> updateCandidateList(S s, M t, List<M> candidateList, int index);

}
