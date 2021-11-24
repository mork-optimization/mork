package es.urjc.etsii.grafo.TSP.algorithms.neighborhood;

import es.urjc.etsii.grafo.TSP.model.TSPInstance;
import es.urjc.etsii.grafo.TSP.model.TSPSolution;
import es.urjc.etsii.grafo.solution.EagerMove;
import es.urjc.etsii.grafo.solution.neighborhood.EagerNeighborhood;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

public class InsertNeighborhood extends EagerNeighborhood<InsertNeighborhood.InsertMove, TSPSolution, TSPInstance> {


    @Override
    public List<InsertMove> getMovements(TSPSolution solution) {
        List<InsertMove> list = new ArrayList<>();
        for (int i = 0; i < solution.getInstance().numberOfLocations(); i++) {
            for (int j = 0; j < solution.getInstance().numberOfLocations(); j++) {
                list.add(new InsertMove(solution, i, j));
            }
        }
        return list;
    }

    public static class InsertMove extends EagerMove<TSPSolution, TSPInstance> {

        final int pi;
        final int pj;

        public InsertMove(TSPSolution solution, int pi, int pj) {
            super(solution);
            this.pi = pi;
            this.pj = pj;
//            System.out.println(this);
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        protected void _execute() {
            this.getSolution().insertLocationAtPiInPj(pi, pj);
        }

        @Override
        public double getValue() {
            var s = this.getSolution();
            if (pi == pj) return 0;
            if (pj == (pi + 1) % this.getSolution().getInstance().numberOfLocations()) {
                return getValueConsecutiveSwap(pi, pj);
            } else if (pi == (pj + 1) % this.getSolution().getInstance().numberOfLocations()) {
                return getValueConsecutiveSwap(pj, pi);
            } else if (pi < pj) {
                var contributionOfPi = s.getDistanceContribution(pi);
                var contributionOfPjNext = s.getDistanceContributionToNextLocation((pj));
                int posBeforePi = (pi - 1 + s.getInstance().numberOfLocations()) % s.getInstance().numberOfLocations();
                var newContributionOfPiPreviousWithNext = this.getSolution().getDistanceContributionToNextLocation(pi, s.getLocation(posBeforePi));
                var newContributionOfPiWithPrevious = this.getSolution().getDistanceContributionToPreviousLocation((pj+ 1) % s.getInstance().numberOfLocations(), s.getLocation(pi));
                var newContributionOfPiWithNext = this.getSolution().getDistanceContributionToNextLocation(pj , s.getLocation(pi));
                return newContributionOfPiWithPrevious + newContributionOfPiWithNext + newContributionOfPiPreviousWithNext - contributionOfPi - contributionOfPjNext;
            } else {
                var contributionOfPi = s.getDistanceContribution(pi);
                var contributionOfPjNext = s.getDistanceContributionToNextLocation((pj - 1 + s.getInstance().numberOfLocations()) % s.getInstance().numberOfLocations());
                int posBeforePi = (pi - 1 + s.getInstance().numberOfLocations()) % s.getInstance().numberOfLocations();
                var newContributionOfPiPreviousWithNext = this.getSolution().getDistanceContributionToNextLocation(pi, s.getLocation(posBeforePi));
                var newContributionOfPiWithPrevious = this.getSolution().getDistanceContributionToPreviousLocation(pj, s.getLocation(pi));
                var newContributionOfPiWithNext = this.getSolution().getDistanceContributionToNextLocation((pj- 1 + s.getInstance().numberOfLocations()) % s.getInstance().numberOfLocations(), s.getLocation(pi));
                return newContributionOfPiWithPrevious + newContributionOfPiWithNext + newContributionOfPiPreviousWithNext - contributionOfPi - contributionOfPjNext;
            }
        }

        private double getValueConsecutiveSwap(int pi, int pj) {
            var contributionOfPi = this.getSolution().getDistanceContributionToPreviousLocation(pi);
            var contributionOfPj = this.getSolution().getDistanceContributionToNextLocation(pj);

            var newContributionOfPi = this.getSolution().getDistanceContributionToNextLocation(pj, this.getSolution().getLocation(pi));
            var newContributionOfPj = this.getSolution().getDistanceContributionToPreviousLocation(pi, this.getSolution().getLocation(pj));

            return newContributionOfPi + newContributionOfPj - contributionOfPi - contributionOfPj;
        }

        @Override
        public String toString() {
            return MessageFormat.format("Insert {0}[{1}] --> {2}", this.pi, this.getSolution().getLocation(pi), this.pj);
        }

        @Override
        public boolean improves() {
            return this.getValue() < 0;
        }


        @Override
        public boolean equals(Object o) {
            InsertMove other = (InsertMove) o;
            return (pi == other.pi && pj == other.pj) || (pj == other.pi && pi == other.pj);
        }

        @Override
        public int hashCode() {
            return 0;
        }
    }

}