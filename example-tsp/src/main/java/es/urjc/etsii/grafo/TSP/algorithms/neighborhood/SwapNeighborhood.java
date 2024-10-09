package es.urjc.etsii.grafo.TSP.algorithms.neighborhood;

import es.urjc.etsii.grafo.TSP.model.TSPBaseMove;
import es.urjc.etsii.grafo.TSP.model.TSPInstance;
import es.urjc.etsii.grafo.TSP.model.TSPSolution;
import es.urjc.etsii.grafo.solution.neighborhood.ExploreResult;
import es.urjc.etsii.grafo.solution.neighborhood.Neighborhood;

import java.util.Objects;
import java.util.stream.IntStream;

public class SwapNeighborhood extends Neighborhood<SwapNeighborhood.SwapMove, TSPSolution, TSPInstance> {


    @Override
    public ExploreResult<SwapMove, TSPSolution, TSPInstance> explore(TSPSolution solution) {
        // Instead of using a double for loop like the insert, we are going to generate the movements lazily
//        List<SwapMove> swapMoves = new ArrayList<>();
//        for (int i = 0; i < solution.getInstance().numberOfLocations(); i++) {
//            for (int j = i + 1; j < solution.getInstance().numberOfLocations(); j++) {
//                swapMoves.add(new SwapMove(solution, i, j));
//            }
//        }
//        return ExploreResult.fromList(swapMoves);

        int nLocations = solution.getInstance().numberOfLocations();
        var stream =
                // Generate all possible swap origin point, from 0 to n-1
                IntStream.range(0, nLocations-1).boxed()
                // For each origin point, generate all possible swap destination points: from starting point + 1 to n
                .flatMap(i -> IntStream.range(i + 1, nLocations).mapToObj(j -> new SwapMove(solution, i, j)));

        int streamSize = nLocations * (nLocations - 1) / 2;
        return ExploreResult.fromStream(stream, streamSize);
    }

    public static class SwapMove extends TSPBaseMove {

        final int nLocations;
        final int pi;
        final int pj;

        public SwapMove(TSPSolution solution, int pi, int pj) {
            super(solution);
            this.nLocations = solution.getInstance().numberOfLocations();
            this.pi = pi;
            this.pj = pj;
            this.distanceDelta = calculateValue(solution);
        }

        private double calculateValue(TSPSolution solution) {
            var s = solution.cloneSolution();
            s.swapLocationOrder(pi, pj);
            return s.getDistance() - solution.getDistance();
        }

        @Override
        protected TSPSolution _execute(TSPSolution solution) {
            solution.swapLocationOrder(pi, pj);
            return solution;
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
            return pi == swapMove.pi && pj == swapMove.pj;
        }

        @Override
        public int hashCode() {
            return Objects.hash(pi, pj);
        }
    }

}