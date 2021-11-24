package es.urjc.etsii.grafo.TSP.algorithms.neighborhood;

import es.urjc.etsii.grafo.TSP.model.TSPInstance;
import es.urjc.etsii.grafo.TSP.model.TSPSolution;
import es.urjc.etsii.grafo.solution.LazyMove;
import es.urjc.etsii.grafo.solution.neighborhood.LazyNeighborhood;
import es.urjc.etsii.grafo.util.random.RandomManager;

import java.text.MessageFormat;
import java.util.stream.Stream;

public class SwapNeighborhood extends LazyNeighborhood<SwapNeighborhood.SwapMove, TSPSolution, TSPInstance> {


    @Override
    public Stream<SwapMove> stream(TSPSolution solution) {
        int initialVertex = RandomManager.getRandom().nextInt(solution.getInstance().numberOfLocations());
        return buildStream(new SwapMove(solution, initialVertex, initialVertex, (initialVertex + 1) % solution.getInstance().numberOfLocations()));
    }

    public static class SwapMove extends LazyMove<TSPSolution, TSPInstance> {

        final int initialPi;
        final int pi;
        final int pj;

        public SwapMove(TSPSolution solution, int initialPi, int pi, int pj) {
            super(solution);
            this.initialPi = initialPi;
            this.pi = pi;
            this.pj = pj;
//            System.out.println(this);
        }


        @Override
        public LazyMove<TSPSolution, TSPInstance> next() {
            var nextPj = (pj + 1) % s.getInstance().numberOfLocations();
            var nextPi = pi;
            if (nextPj == initialPi) {
                nextPi = (nextPi + 1) % s.getInstance().numberOfLocations();
                if (nextPi == initialPi -1) {
                    return null;
                }
                nextPj = (nextPi + 1) % s.getInstance().numberOfLocations();
            }
            return new SwapMove(s, initialPi, nextPi, nextPj);
        }

        @Override
        public boolean isValid() {
            return true;
        }

        @Override
        protected void _execute() {
            this.getSolution().swapLocationOrder(pi, pj);
        }

        @Override
        public double getValue() {
            if (pj == (pi + 1) % this.getSolution().getInstance().numberOfLocations()) {
                return getValueConsecutiveSwap(pi, pj);
            } else if (pi == (pj + 1) % this.getSolution().getInstance().numberOfLocations()) {
                return getValueConsecutiveSwap(pj, pi);
            } else {
                var contributionOfPi = this.getSolution().getDistanceContribution(pi);
                var contributionOfPj = this.getSolution().getDistanceContribution(pj);

                var newContributionOfPi = this.getSolution().getDistanceContribution(pj, this.getSolution().getLocation(pi));
                var newContributionOfPj = this.getSolution().getDistanceContribution(pi, this.getSolution().getLocation(pj));

                return newContributionOfPi + newContributionOfPj - contributionOfPi - contributionOfPj;
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
        public boolean improves() {
            return this.getValue() < 0;
        }

        @Override
        public String toString() {
            return MessageFormat.format("Swap {0}[{1}] <-> {2}[{3}]", this.pi, this.getSolution().getLocation(pi), this.pj, this.getSolution().getLocation(pj));
        }

        @Override
        public boolean equals(Object o) {
            SwapMove other = (SwapMove) o;
            return (pi == other.pi && pj == other.pj) || (pj == other.pi && pi == other.pj);
        }

        @Override
        public int hashCode() {
            return 0;
        }
    }

}