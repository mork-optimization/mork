package es.urjc.etsii.grafo.flayouts.model;

import es.urjc.etsii.grafo.flayouts.constructives.grasp.FLPAddListManager;
import es.urjc.etsii.grafo.util.TimeControl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static es.urjc.etsii.grafo.flayouts.model.FLPNewTestUtil.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class FLPAddNeighTest {
    @BeforeEach void setup() { initialize(1234); }
    @AfterEach void teardown() { cleanup(); }

    @Test
    void insertionCandidatesAreCompleteExactAndLeaveSourceUntouched() {
        var instance = instance(9, 2);
        var solutions = List.of(
                fixture(instance, new int[]{0}, new int[]{}),
                fixture(instance, new int[]{}, new int[]{0}),
                fixture(instance, new int[]{0, 1, 2, 3}, new int[]{4, 5}),
                fixture(instance, new int[]{7, 6, 5}, new int[]{3, 2, 1}),
                fixture(instance, new int[]{0, 1, 2, 3, 4, 5, 6, 7}, new int[]{}),
                fixture(instance, new int[]{}, new int[]{0, 1, 2, 3, 4, 5, 6, 7}),
                fixture(instance, new int[]{}, new int[]{}),
                fixture(instance, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8}, new int[]{}),
                fixture(instance(1, 2), new int[]{}, new int[]{}),
                fixture(instance(4, 3), new int[]{2}, new int[]{}, new int[]{0}));
        for (var neighborhood : List.of(new FLPAddNeigh(), new FLPAddNeigh(false))) {
            for (var solution : solutions) {
                var before = solution.cloneSolution();
                var expected = new HashSet<FLPAddNeigh.AddMove>();
                for (int facility : solution.getNotAssignedFacilities()) {
                    for (int row = 0; row < solution.nRows(); row++) {
                        for (int pos = 0; pos <= solution.rowSize(row); pos++) {
                            expected.add(new FLPAddNeigh.AddMove(solution, row, pos, facility, 0));
                        }
                    }
                }
                for (int repetition = 0; repetition < 3; repetition++) {
                    var candidates = neighborhood.exploreList(solution);
                    assertEquals(expected, new HashSet<>(candidates));
                    assertEquals(expected.size(), candidates.size(), "Duplicate insertion candidates");
                    for (var candidate : candidates) {
                        // Build the expected layout directly, independently of insertion/swap cost helpers.
                        var rows = layout(solution);
                        int[] oldRow = rows[candidate.rowIdx()];
                        int[] newRow = new int[oldRow.length + 1];
                        System.arraycopy(oldRow, 0, newRow, 0, candidate.pos());
                        newRow[candidate.pos()] = candidate.facility();
                        System.arraycopy(oldRow, candidate.pos(), newRow, candidate.pos() + 1, oldRow.length - candidate.pos());
                        rows[candidate.rowIdx()] = newRow;
                        assertEquals(independentCost(solution.getInstance(), rows) - solution.getScore(),
                                candidate.delta(), 1e-7, candidate.toString());
                        checkMove(solution, candidate);
                    }
                    assertUnchanged(before, solution);
                    assertState(solution, false);
                }
            }
        }
    }

    @Test
    void directInsertionPreservesEvaluationStateAndExecutesAtEveryGap() {
        var solution = fixture(instance(9, 3), new int[]{0, 1, 2}, new int[]{3, 4}, new int[]{});
        var before = solution.cloneSolution();
        for (int facility : solution.getNotAssignedFacilities()) {
            for (int row = 0; row < solution.nRows(); row++) {
                for (int pos = 0; pos <= solution.rowSize(row); pos++) {
                    var move = new FLPAddNeigh.AddMove(solution, row, pos, facility);
                    assertUnchanged(before, solution);
                    checkMove(solution, move);
                }
            }
        }
        assertThrows(IllegalArgumentException.class, () -> new FLPAddNeigh.AddMove(solution, 0, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new FLPAddNeigh.AddMove(solution, 0, 4, 5));
        assertUnchanged(before, solution);
    }

    @Test
    void partialCenterUpdatesAndFullRowsRemainValid() {
        var solution = fixture(instance(3, 1), new int[]{0, 1});
        assertEquals(3.0, solution.left(0, 2));
        new FLPAddNeigh.AddMove(solution, 0, 2, 2).execute(solution);
        assertState(solution, true);
        assertTrue(solution.verifyCorrectSizes());
        assertEquals(0, new FLPAddNeigh().neighborhoodSize(solution));
        assertTrue(new FLPAddNeigh().getRandomMove(solution).isEmpty());
    }

    @Test
    void randomInsertionIsReproducibleAndDoesNotChangeTheSource() {
        var solution = fixture(instance(9, 3), new int[]{0, 1}, new int[]{2}, new int[]{});
        var before = solution.cloneSolution();
        var neighborhood = new FLPAddNeigh();
        assertEquals(36, neighborhood.neighborhoodSize(solution));
        var first = neighborhood.getRandomMove(solution).orElseThrow();
        initialize(1234);
        assertEquals(first, neighborhood.getRandomMove(solution).orElseThrow());
        assertUnchanged(before, solution);
        checkMove(solution, first);
    }

    @Test
    void defaultInsertionIsLazyAndStopsAtTheDeadline() {
        var instance = spy(instance(9, 2));
        var solution = fixture(instance, new int[]{0, 1, 2}, new int[]{});
        org.mockito.Mockito.clearInvocations(instance);
        var neighborhood = new FLPAddNeigh();
        try (var moves = neighborhood.explore(solution).moves()) {
            verify(instance, never()).flow(anyInt(), anyInt());
            TimeControl.setMaxExecutionTime(0, TimeUnit.NANOSECONDS);
            TimeControl.start();
            assertEquals(0, moves.count());
        }
        assertTrue(neighborhood.getRandomMove(solution).isEmpty());
        verify(instance, never()).flow(anyInt(), anyInt());
    }

    @Test
    void originalGraspBuildsAndUpdatesListsAtTheSourceVersion() {
        var solution = fixture(instance(9, 2), new int[]{}, new int[]{});
        var manager = new FLPAddListManager();
        var before = solution.cloneSolution();
        var candidates = manager.buildInitialCandidateList(solution);
        assertEquals(18, candidates.size());
        assertUnchanged(before, solution);

        for (int facility = 0; facility < 2; facility++) {
            int selected = -1;
            for (int index = 0; index < candidates.size(); index++) {
                var move = candidates.get(index);
                if (move.facility() == facility && move.rowIdx() == 0 && move.pos() == 0) {
                    selected = index;
                    break;
                }
            }
            assertNotEquals(-1, selected);
            var chosen = candidates.get(selected);
            long version = solution.getVersion();
            chosen.execute(solution);
            assertEquals(version + 1, solution.getVersion());
            assertState(solution, false);
            before = solution.cloneSolution();
            candidates = manager.updateCandidateList(solution, chosen, candidates, selected);
            // The first update is the reported nine-facility reproduction: [[0], []].
            assertEquals(facility == 0 ? 24 : 28, candidates.size());
            assertUnchanged(before, solution);
        }

        var partial = fixture(instance(9, 2), new int[]{0, 1, 2}, new int[]{3, 4});
        before = partial.cloneSolution();
        assertEquals(28, manager.buildInitialCandidateList(partial).size());
        assertUnchanged(before, partial);
    }

    @Test
    void failedExplorationLeavesSourceUntouched() {
        var instance = spy(instance(9, 2));
        var solution = fixture(instance, new int[]{0}, new int[]{});
        var before = solution.cloneSolution();
        var failure = new IllegalStateException("Injected cost-evaluation failure");
        doThrow(failure).when(instance).flow(anyInt(), anyInt());

        assertSame(failure, assertThrows(IllegalStateException.class,
                () -> new FLPAddListManager().buildInitialCandidateList(solution)));
        assertUnchanged(before, solution);
        assertSame(failure, assertThrows(IllegalStateException.class,
                () -> new FLPAddNeigh(false).exploreList(solution)));
        assertUnchanged(before, solution);
        assertSame(failure, assertThrows(IllegalStateException.class,
                () -> new FLPAddNeigh.AddMove(solution, 0, 1, 1)));
        assertUnchanged(before, solution);
    }
}
