package es.urjc.etsii.grafo.TSP.algorithms.neighborhood;

import es.urjc.etsii.grafo.TSP.model.TSPInstance;
import es.urjc.etsii.grafo.TSP.model.TSPSolution;
import es.urjc.etsii.grafo.solution.LazyMove;
import es.urjc.etsii.grafo.solution.neighborhood.Neighborhood;
import es.urjc.etsii.grafo.util.DoubleComparator;
import es.urjc.etsii.grafo.util.random.RandomManager;

import java.text.MessageFormat;
import java.util.Objects;

public class SwapNeighborhood extends Neighborhood<SwapNeighborhood.SwapMove, TSPSolution, TSPInstance> {


    @Override
    public ExploreResult<SwapNeighborhood.SwapMove, TSPSolution, TSPInstance> explore(TSPSolution solution) {
        int initialVertex = RandomManager.getRandom().nextInt(solution.getInstance().numberOfLocations());
        return new ExploreResult<>(solution, new SwapMove(solution, initialVertex, initialVertex, (initialVertex + 1) % solution.getInstance().numberOfLocations()));
    }

    public static class SwapMove extends LazyMove<TSPSolution, TSPInstance> {

        final int initialPi;
        final int nLocations;
        final int pi;
        final int pj;
        final double value;

        public SwapMove(TSPSolution solution, int initialPi, int pi, int pj) {
            super(solution);
            this.initialPi = initialPi;
            this.nLocations = solution.getInstance().numberOfLocations();
            this.pi = pi;
            this.pj = pj;
            this.value = calculateValue(solution);
//            System.out.println(initialPi + "-" +this);
        }

        private double calculateValue(TSPSolution solution) {
            var s = solution.cloneSolution();
            s.swapLocationOrder(pi, pj);
            return s.getScore() - solution.getScore();
        }


        @Override
        public LazyMove<TSPSolution, TSPInstance> next(TSPSolution solution) {
            var nextPj = (pj + 1) % nLocations;
            var nextPi = pi;
            if (nextPj == initialPi) {
                nextPi = (nextPi + 1) % nLocations;
                if (nextPi == (initialPi -1 + nLocations)/ + nLocations) {
                    return null;
                }
                nextPj = (nextPi + 1) % nLocations;
            }
            return new SwapMove(solution, initialPi, nextPi, nextPj);
        }

        @Override
        protected boolean _execute(TSPSolution solution) {
            solution.swapLocationOrder(pi, pj);
            return true;
        }

        @Override
        public double getValue() {
            return value;
        }

        @Override
        public boolean improves() {
            return DoubleComparator.isLessThan(this.getValue(), 0);
        }

        @Override
        public String toString() {
            return String.format("Swap %s <-> %s", this.pi, this.pj);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SwapMove swapMove = (SwapMove) o;
            return initialPi == swapMove.initialPi && pi == swapMove.pi && pj == swapMove.pj;
        }

        @Override
        public int hashCode() {
            return Objects.hash(initialPi, pi, pj);
        }
    }

}