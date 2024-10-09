package es.urjc.etsii.grafo.__RNAME__.constructives.grasp;

import es.urjc.etsii.grafo.__RNAME__.model.__RNAME__BaseMove;
import es.urjc.etsii.grafo.__RNAME__.model.__RNAME__Instance;
import es.urjc.etsii.grafo.__RNAME__.model.__RNAME__Solution;
import es.urjc.etsii.grafo.create.grasp.GRASPListManager;

import java.util.ArrayList;
import java.util.List;

public class __RNAME__ListManager extends GRASPListManager<__RNAME__ListManager.__RNAME__GRASPMove, __RNAME__Solution, __RNAME__Instance> {

    /**
     * Generate initial candidate list. The list will be sorted if necessary by the constructive method.
     * @param solution Current solution
     * @return a candidate list
     */
    @Override
    public List<__RNAME__GRASPMove> buildInitialCandidateList(__RNAME__Solution solution) {
        var list = new ArrayList<__RNAME__GRASPMove>();

        // TODO Generate a list with all valid movements for current solution
        // GRASP ends when CL is empty

        return list;
    }

    /**
     * Update candidate list after each movement. The list will be sorted by the constructor.
     * @param solution Current solution, move has been already applied
     * @param move     Chosen move
     * @param index index of the chosen move in the candidate list
     * @param candidateList original candidate list
     * @return an UNSORTED candidate list, where the best candidate is on the first position and the worst in the last
     */
    @Override
    public List<__RNAME__GRASPMove> updateCandidateList(__RNAME__Solution solution, __RNAME__GRASPMove move, List<__RNAME__GRASPMove> candidateList, int index) {
        // List can be partially updated / modified
        // recalculating from scratch is an ok start and can be optimized later if necessary
        // Do not prematurely micro optimize, split the code in small methods and profile first
        return buildInitialCandidateList(solution);
    }

    public static class __RNAME__GRASPMove extends __RNAME__BaseMove {
        public __RNAME__GRASPMove(__RNAME__Solution solution) {
            super(solution);
        }

        @Override
        protected __RNAME__Solution _execute(__RNAME__Solution solution) {
            // TODO Apply changes to solution when this method is called
            // Return the modified solutions.
            // It is up to the implementation to decide if the original solution is modified
            // in place or a new one is created by cloning the original solution and then applying the changes.
            // This method should be idempotent, i.e. calling it multiple times with the same solution
            // should return the same result
            throw new UnsupportedOperationException("_execute() in __RNAME__ListManager not implemented yet");
        }

        public double getScoreChange() {
            // TODO How much does o.f. value change if we apply this movement?
            throw new UnsupportedOperationException("getValue() in __RNAME__ListManager not implemented yet");
        }

        @Override
        public String toString() {
            // TODO Use IDE to generate this method after all properties are defined
            throw new UnsupportedOperationException("toString() in __RNAME__ not implemented yet");
        }

        @Override
        public boolean equals(Object o) {
            // TODO Use IDE to generate this method after all properties are defined
            throw new UnsupportedOperationException("equals() in __RNAME__ not implemented yet");
        }

        @Override
        public int hashCode() {
            // TODO Use IDE to generate this method after all properties are defined
            throw new UnsupportedOperationException("hashCode() in __RNAME__ not implemented yet");
        }
    }
}
