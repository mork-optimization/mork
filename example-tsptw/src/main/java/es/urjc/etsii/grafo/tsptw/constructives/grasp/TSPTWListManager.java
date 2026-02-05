package es.urjc.etsii.grafo.tsptw.constructives.grasp;

import es.urjc.etsii.grafo.tsptw.model.TSPTWBaseMove;
import es.urjc.etsii.grafo.tsptw.model.TSPTWInstance;
import es.urjc.etsii.grafo.tsptw.model.TSPTWSolution;
import es.urjc.etsii.grafo.create.grasp.GRASPListManager;

import java.util.ArrayList;
import java.util.List;

public class TSPTWListManager extends GRASPListManager<TSPTWListManager.TSPTWGRASPMove, TSPTWSolution, TSPTWInstance> {

    /**
     * Generate initial candidate list.
     * @param solution Current solution
     * @return an UNSORTED candidate list
     */
    @Override
    public List<TSPTWGRASPMove> buildInitialCandidateList(TSPTWSolution solution) {
        var list = new ArrayList<TSPTWGRASPMove>();

        // TODO Generate a list with all valid movements for current solution
        // GRASP constructive ends when CL is empty

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
    public List<TSPTWGRASPMove> updateCandidateList(TSPTWSolution solution, TSPTWGRASPMove move, List<TSPTWGRASPMove> candidateList, int index) {
        // List can be partially updated / modified if required for performance
        // Recalculating from scratch is OK and can be optimized later if necessary
        // Do NOT prematurely optimize, split the code in small methods and profile first to see if this is necessary
        return buildInitialCandidateList(solution);
    }

    public static class TSPTWGRASPMove extends TSPTWBaseMove {
        public TSPTWGRASPMove(TSPTWSolution solution) {
            super(solution);
        }

        @Override
        protected TSPTWSolution _execute(TSPTWSolution solution) {
            // TODO Apply changes to solution when this method is called
            // Return the modified solutions.
            // It is up to the implementation to decide if the original solution is modified
            // in place or a new one is created by cloning the original solution and then applying the changes.
            // NOTE: Calling this method multiple times with a solution and its clones must return the same result
            throw new UnsupportedOperationException("_execute() in TSPTWListManager not implemented yet");
        }

        public double getScoreChange() {
            // TODO How much does o.f. value change if we apply this movement?
            throw new UnsupportedOperationException("getValue() in TSPTWListManager not implemented yet");
        }

        @Override
        public String toString() {
            // TODO Use IDE to generate this method after all properties are defined
            throw new UnsupportedOperationException("toString() in TSPTW not implemented yet");
        }

        @Override
        public boolean equals(Object o) {
            // TODO Use IDE to generate this method after all properties are defined
            throw new UnsupportedOperationException("equals() in TSPTW not implemented yet");
        }

        @Override
        public int hashCode() {
            // TODO Use IDE to generate this method after all properties are defined
            throw new UnsupportedOperationException("hashCode() in TSPTW not implemented yet");
        }
    }
}
