package es.urjc.etsii.grafo.TSP.algorithms.constructives.grasp;

import es.urjc.etsii.grafo.TSP.model.TSPInstance;
import es.urjc.etsii.grafo.TSP.model.TSPSolution;
import es.urjc.etsii.grafo.solution.EagerMove;
import es.urjc.etsii.grafo.solver.create.grasp.GRASPListManager;

import java.util.ArrayList;
import java.util.List;

public class TSPListManager extends GRASPListManager<TSPListManager.TSPGRASPMove, TSPSolution, TSPInstance> {

    /**
     * Generate initial candidate list. The list will be sorted if necessary by the constructive method.
     * @param solution Current solution
     * @return a candidate list
     */
    @Override
    public List<TSPGRASPMove> buildInitialCandidateList(TSPSolution solution) {
        var list = new ArrayList<TSPGRASPMove>();

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
    public List<TSPGRASPMove> updateCandidateList(TSPSolution solution, TSPGRASPMove move, List<TSPGRASPMove> candidateList, int index) {
        // List can be partially updated / modified
        // recalculating from scratch is an ok start and can be optimized later if necessary
        return buildInitialCandidateList(solution);
    }

    public static class TSPGRASPMove extends EagerMove<TSPSolution, TSPInstance> {
        public TSPGRASPMove(TSPSolution solution) {
            super(solution);
        }

        @Override
        protected void _execute() {
            // TODO Apply changes to solution if movement is executed
            // this.s --> reference to current solution
            throw new UnsupportedOperationException("_execute() in TSPListManager not implemented yet");

        }

        @Override
        public double getValue() {
            // TODO How much does o.f. value change if we apply this movement?
            throw new UnsupportedOperationException("getValue() in TSPListManager not implemented yet");
        }

        @Override
        public boolean improves() {
            // TODO: Choose implementation
            // If maximizing: return DoubleComparator.isPositive(this.score)
            // If minimizing: return DoubleComparator.isNegative(this.score)
            throw new UnsupportedOperationException("improves() in TSPListManager not implemented yet");
        }

        @Override
        public String toString() {
            // TODO Use IDE to generate this method after all properties are defined
            throw new UnsupportedOperationException("toString() in TSP not implemented yet");
        }

        @Override
        public boolean equals(Object o) {
            // TODO Use IDE to generate this method after all properties are defined
            throw new UnsupportedOperationException("equals() in TSP not implemented yet");
        }

        @Override
        public int hashCode() {
            // TODO Use IDE to generate this method after all properties are defined
            throw new UnsupportedOperationException("hashCode() in TSP not implemented yet");
        }

        @Override
        public boolean isValid() {
            // A move is not valid if it leaves the solution in an unfeasible state
            // If the movement is not valid, it should probably not be in the candidate list.
            return true;
        }
    }
}
