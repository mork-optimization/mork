package es.urjc.etsii.grafo.algorithms.scattersearch;

import es.urjc.etsii.grafo.annotations.AlgorithmComponent;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.TimeControl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@AlgorithmComponent
public abstract class SolutionCombinator<S extends Solution<S, I>, I extends Instance> {
    /**
     * Returns new reference set starting from current reference set
     *
     * @param currentSet   current reference set, DO NOT MODIFY
     * @param newSolutions solutions added to refset in last iteration
     * @return new reference set
     */
    public Set<S> newSet(S[] currentSet, Set<S> newSolutions) {
        var newsize = newSolutions.size() * currentSet.length;
        var newset = new HashSet<S>(newsize);
        for (var solution : newSolutions) {
            for (var refSolution : currentSet) {
                if(TimeControl.isTimeUp()){
                    return newset;
                }
                var combinedSolution = this.apply(solution, refSolution);
                newset.addAll(combinedSolution);
            }
        }
        return newset;
    }

    /**
     * Create a new solution combining left and right. If this method is not flexible enough,
     * leave an empty implementation (throw new UnsupportedOperationException)
     * and override method newSet
     * @param left origin solution, one of the recently added solutions to the refset
     * @param right target solution, one of the old solutions in the ref set
     * @return new solution, such as List.of(solution)
     * or list of solutions generated from the solutions given as parameters
     */
    protected abstract List<S> apply(S left, S right);

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "{}";
    }
}
