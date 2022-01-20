package es.urjc.etsii.grafo.TSP.algorithms.neighborhood;

import es.urjc.etsii.grafo.TSP.model.TSPInstance;
import es.urjc.etsii.grafo.TSP.model.TSPSolution;
import es.urjc.etsii.grafo.solution.LazyMove;
import es.urjc.etsii.grafo.solution.neighborhood.LazyNeighborhood;
import es.urjc.etsii.grafo.util.DoubleComparator;
import es.urjc.etsii.grafo.util.random.RandomManager;

import java.text.MessageFormat;
import java.util.Objects;
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
//            System.out.println(initialPi + "-" +this);
        }


        @Override
        public LazyMove<TSPSolution, TSPInstance> next() {
            var nextPj = (pj + 1) % s.getInstance().numberOfLocations();
            var nextPi = pi;
            if (nextPj == initialPi) {
                nextPi = (nextPi + 1) % s.getInstance().numberOfLocations();
                if (nextPi == (initialPi -1 + s.getInstance().numberOfLocations())/ + s.getInstance().numberOfLocations()) {
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
            var s = this.getSolution().cloneSolution();
            s.swapLocationOrder(pi, pj);
            var result = s.getScore() - this.getSolution().getScore();
            return result;
        }

        @Override
        public boolean improves() {
            return DoubleComparator.isLessThan(this.getValue(), 0);
        }

        @Override
        public String toString() {
            return MessageFormat.format("Swap {0}[{1}] <-> {2}[{3}]", this.pi, this.getSolution().getLocation(pi), this.pj, this.getSolution().getLocation(pj));
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