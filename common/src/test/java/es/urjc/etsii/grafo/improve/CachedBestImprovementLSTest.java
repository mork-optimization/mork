package es.urjc.etsii.grafo.improve;

import es.urjc.etsii.grafo.improve.ls.LocalSearchCachedBestImprovement;
import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.solution.Move;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.solution.RefreshableMove;
import es.urjc.etsii.grafo.solution.neighborhood.ExploreResult;
import es.urjc.etsii.grafo.solution.neighborhood.Neighborhood;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestSolution;
import es.urjc.etsii.grafo.util.Context;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

class CachedBestImprovementLSTest {

    private TestSolution solution;
    private Objective<CachedMove, TestSolution, TestInstance> objective;

    @BeforeAll
    static void init() {
        Metrics.disableMetrics();
    }

    @BeforeEach
    void setup() {
        this.solution = new TestSolution(new TestInstance("Fake Instance"));
        this.objective = Objective.ofMinimizing("Test", TestSolution::getScore, CachedMove::getScoreChange);
        Context.Configurator.setObjectives(this.objective);
    }

    @Test
    void usesCachedMoveBeforeReexploringNeighborhood() {
        var neighborhood = new CountingNeighborhood(
                Map.of(2, -4.0, 3, -1.0),
                List.of(
                        new MoveSpec(1, -10),
                        new MoveSpec(2, -5),
                        new MoveSpec(3, -1)
                )
        );
        var ls = new LocalSearchCachedBestImprovement<>(this.objective, neighborhood, 3);

        var first = ls.getMove(this.solution);
        Assertions.assertEquals(1, first.id());
        Assertions.assertEquals(1, neighborhood.exploreCalls());
        first.execute(this.solution);

        var second = ls.getMove(this.solution);
        Assertions.assertEquals(2, second.id());
        Assertions.assertEquals(-4, second.getScoreChange());
        Assertions.assertEquals(1, neighborhood.exploreCalls());
        Assertions.assertEquals(1, neighborhood.refreshCalls());
        Assertions.assertDoesNotThrow(() -> second.execute(this.solution));
    }

    @Test
    void reexploresWhenCachedMovesDoNotImproveAfterRefresh() {
        var neighborhood = new CountingNeighborhood(
                Map.of(2, 1.0),
                List.of(
                        new MoveSpec(1, -10),
                        new MoveSpec(2, -5)
                ),
                List.of(
                        new MoveSpec(3, -2)
                )
        );
        var ls = new LocalSearchCachedBestImprovement<>(this.objective, neighborhood, 2);

        var first = ls.getMove(this.solution);
        Assertions.assertEquals(1, first.id());
        first.execute(this.solution);

        var second = ls.getMove(this.solution);
        Assertions.assertEquals(3, second.id());
        Assertions.assertEquals(2, neighborhood.exploreCalls());
        Assertions.assertEquals(1, neighborhood.refreshCalls());
    }

    @Test
    void returnsNullWhenNoMoveImprovesAfterFullExploration() {
        var neighborhood = new CountingNeighborhood(
                Map.of(),
                List.of(
                        new MoveSpec(1, 0),
                        new MoveSpec(2, 5)
                )
        );
        var ls = new LocalSearchCachedBestImprovement<>(this.objective, neighborhood, 2);

        Assertions.assertNull(ls.getMove(this.solution));
        Assertions.assertEquals(1, neighborhood.exploreCalls());
    }

    @Test
    void rejectsInvalidCacheSize() {
        var neighborhood = new CountingNeighborhood(Map.of(), List.of());

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new LocalSearchCachedBestImprovement<>(this.objective, neighborhood, 0)
        );
    }

    private record MoveSpec(int id, double scoreChange) {}

    private static class CountingNeighborhood extends Neighborhood<CachedMove, TestSolution, TestInstance> {

        private final Map<Integer, Double> refreshedScores;
        private final List<List<MoveSpec>> explorations;
        private final AtomicInteger refreshCalls;
        private int exploreCalls;

        @SafeVarargs
        CountingNeighborhood(Map<Integer, Double> refreshedScores, List<MoveSpec>... explorations) {
            this.refreshedScores = refreshedScores;
            this.explorations = List.of(explorations);
            this.refreshCalls = new AtomicInteger();
        }

        @Override
        public ExploreResult<CachedMove, TestSolution, TestInstance> explore(TestSolution solution) {
            var index = Math.min(this.exploreCalls, this.explorations.size() - 1);
            this.exploreCalls++;

            var moves = new ArrayList<CachedMove>();
            for (var spec : this.explorations.get(index)) {
                moves.add(new CachedMove(
                        solution,
                        spec.id(),
                        spec.scoreChange(),
                        this.refreshedScores,
                        this.refreshCalls
                ));
            }
            return ExploreResult.fromList(moves);
        }

        int exploreCalls() {
            return this.exploreCalls;
        }

        int refreshCalls() {
            return this.refreshCalls.get();
        }
    }

    private static class CachedMove extends Move<TestSolution, TestInstance>
            implements RefreshableMove<CachedMove, TestSolution, TestInstance> {

        private final int id;
        private final double scoreChange;
        private final Map<Integer, Double> refreshedScores;
        private final AtomicInteger refreshCalls;

        CachedMove(
                TestSolution solution,
                int id,
                double scoreChange,
                Map<Integer, Double> refreshedScores,
                AtomicInteger refreshCalls
        ) {
            super(solution);
            this.id = id;
            this.scoreChange = scoreChange;
            this.refreshedScores = refreshedScores;
            this.refreshCalls = refreshCalls;
        }

        @Override
        public Optional<CachedMove> refresh(TestSolution solution) {
            this.refreshCalls.incrementAndGet();
            var refreshedScore = this.refreshedScores.get(this.id);
            if (refreshedScore == null) {
                return Optional.empty();
            }

            return Optional.of(new CachedMove(
                    solution,
                    this.id,
                    refreshedScore,
                    this.refreshedScores,
                    this.refreshCalls
            ));
        }

        @Override
        protected TestSolution _execute(TestSolution solution) {
            solution.setScore(solution.getScore() + this.scoreChange);
            return solution;
        }

        int id() {
            return this.id;
        }

        double getScoreChange() {
            return this.scoreChange;
        }

        @Override
        public String toString() {
            return "CachedMove{" +
                    "id=" + id +
                    ", scoreChange=" + scoreChange +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof CachedMove that)) {
                return false;
            }
            return this.id == that.id
                    && Double.compare(this.scoreChange, that.scoreChange) == 0;
        }

        @Override
        public int hashCode() {
            int result = Integer.hashCode(this.id);
            result = 31 * result + Double.hashCode(this.scoreChange);
            return result;
        }
    }
}
