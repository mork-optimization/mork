package es.urjc.etsii.grafo.tsp.model.neighs;

import es.urjc.etsii.grafo.tsp.model.TSPBaseMove;
import es.urjc.etsii.grafo.tsp.model.TSPInstance;
import es.urjc.etsii.grafo.tsp.model.TSPSolution;
import es.urjc.etsii.grafo.solution.neighborhood.ExploreResult;
import es.urjc.etsii.grafo.solution.neighborhood.Neighborhood;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InsertNeighborhood extends Neighborhood<InsertNeighborhood.InsertMove, TSPSolution, TSPInstance> {

    @Override
    public ExploreResult<InsertMove, TSPSolution, TSPInstance> explore(TSPSolution solution) {
        List<InsertMove> list = new ArrayList<>();
        for (int i = 0; i < solution.getInstance().numberOfLocations(); i++) {
            for (int j = 0; j < solution.getInstance().numberOfLocations(); j++) {
                list.add(new InsertMove(solution, i, j));
            }
        }
        return ExploreResult.fromList(list);
    }

    public static class InsertMove extends TSPBaseMove {

        final int pi;
        final int pj;

        /**
         * Constructor on an insert move. Given a solution, an insert move consist in inserting the the location of a position pi, into a position pj.
         *
         *
         * @param solution current solution
         * @param pi position of the location is going to be inserted into pj
         * @param pj position where the location of pi is going to be inserted
         */
        public InsertMove(TSPSolution solution, int pi, int pj) {
            super(solution);
            this.pi = pi;
            this.pj = pj;
            this.distanceDelta = insertDelta(solution);
        }

        @Override
        protected TSPSolution _execute(TSPSolution solution) {
            solution.insertLocationAtPiInPj(pi, pj);
            return solution;
        }

        private double insertDelta(TSPSolution solution){
            var s = solution.cloneSolution();
            s.insertLocationAtPiInPj(pi, pj);
            return s.getDistance() - solution.getDistance();
        }

        @Override
        public String toString() {
            return String.format("Insert %s --> %s", this.pi, this.pj);
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            InsertMove that = (InsertMove) o;
            return pi == that.pi && pj == that.pj;
        }

        @Override
        public int hashCode() {
            return Objects.hash(pi, pj);
        }
    }

}