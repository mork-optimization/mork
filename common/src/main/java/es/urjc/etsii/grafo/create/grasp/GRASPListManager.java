package es.urjc.etsii.grafo.create.grasp;

import es.urjc.etsii.grafo.annotations.AlgorithmComponent;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.Solution;

import java.util.ArrayList;
import java.util.List;

/**
 * Creates and updates the candidate list when a movement is performed
 */
@AlgorithmComponent
public abstract class GRASPListManager<M extends Move<S, I>, S extends Solution<S,I>, I extends Instance> {
    /**
     * Initialize solution before GRASP algorithm is run
     * F.e: In the case of clustering algorithms, usually each cluster needs to have at least one point,
     * different solutions types may require different initialization
     *
     * @param solution Solution to initialize before running the GRASP constructive method
     */
    public void beforeGRASP(S solution){

    }

    /**
     * Do any kind of post-processing after there are no more valid GRASP moves
     *
     * @param solution Solution to modify after the GRASP constructive method has finished
     */
    public void afterGRASP(S solution){

    }


    /**
     * Generate initial candidate list.
     * @param solution Current solution
     * @return an UNSORTED candidate list
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

    /**
     * Create a no operation GRASPListManager method
     * Returns empty lists
     * @param <M> Move class
     * @param <S> Solution class
     * @param <I> Instance class
     * @return Null GRASPListManager method
     */
    public static <M extends Move<S,I>, S extends Solution<S,I>, I extends Instance> GRASPListManager<M, S,I> nul(){
        return new NullGraspListManager<>();
    }

    /**
     * Do nothing GRASPListManager
     *
     * @param <M> Move class
     * @param <S> Solution class
     * @param <I> Instance class
     */
    public static class NullGraspListManager<M extends Move<S,I>, S extends Solution<S,I>,I extends Instance> extends GRASPListManager<M,S,I> {

        public NullGraspListManager() {}

        @Override
        public List<M> buildInitialCandidateList(S solution) {
            return new ArrayList<>();
        }

        @Override
        public List<M> updateCandidateList(S solution, M move, List<M> candidateList, int index) {
            return buildInitialCandidateList(solution);
        }
    }
}
