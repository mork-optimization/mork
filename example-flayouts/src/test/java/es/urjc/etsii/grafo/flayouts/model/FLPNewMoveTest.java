package es.urjc.etsii.grafo.flayouts.model;

import es.urjc.etsii.grafo.util.TimeControl;
import org.junit.jupiter.api.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import static es.urjc.etsii.grafo.flayouts.model.FLPNewTestUtil.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FLPNewMoveTest {
    @BeforeEach void setup() { initialize(1234); }
    @AfterEach void teardown() { cleanup(); }

    @Test
    void exhaustiveSmallLayoutsHaveExactDeltasAndIdenticalFastNeighborhoods() {
        for (int a = 0; a < 4; a++) for (int b = 0; b < 4; b++) for (int c = 0; c < 4; c++) for (int d = 0; d < 4; d++) {
            if (a == b || a == c || a == d || b == c || b == d || c == d) continue;
            int[] permutation = {a, b, c, d};
            for (int split = 0; split <= 4; split++) {
                var s = fixture(instance(4, 2), Arrays.copyOfRange(permutation, 0, split), Arrays.copyOfRange(permutation, split, 4));
                compare(s, new FLPSwapNeighNew(false), new FLPSwapNeighNew());
                compare(s, new FLPRelocateNeighNew(false), new FLPRelocateNeighNew());
                compare(s, new FLPOptNeighNew(false), new FLPOptNeighNew());
                for (int length : new int[]{2, 3}) for (boolean reverse : new boolean[]{false, true}) {
                    var n = new FLPBlockRelocateNeighNew(length, reverse, FLPBlockRelocateNeighNew.Scope.ALL);
                    for (var m : n.explore(s).moves().toList()) checkMove(s, m);
                }
            }
        }
    }

    private void compare(FLPSolution s, FLPPreservingNeighNew baseline, FLPPreservingNeighNew fast) {
        var before = s.cloneSolution();
        var expected = baseline.explore(s).moves().toList();
        var actual = fast.explore(s).moves().toList();
        assertEquals(new HashSet<>(expected), new HashSet<>(actual));
        assertEquals(expected.size(), actual.size());
        assertEquals(expected.size(), new HashSet<>(expected).size());
        assertUnchanged(before, s);
        for (var m : expected) checkMove(s, m);
        for (var m : actual) checkMove(s, m);
    }

    @Test
    void addsAndRemovesHandlePartialFullAndEmptyRowsWithoutMutatingExploration() {
        for (int rows : new int[]{1, 2, 3}) for (int n = 1; n <= 6; n++) {
            var s = new FLPSolution(instance(n, rows));
            s.notifyUpdate();
            while (s.nAssigned() < n) {
                var before = s.cloneSolution();
                var baseline = new FLPAddNeigh(false).explore(s).moves().toList();
                var fast = new FLPAddNeigh().explore(s).moves().toList();
                assertEquals((n - s.nAssigned()) * (s.nAssigned() + rows), baseline.size());
                assertEquals(new HashSet<>(baseline), new HashSet<>(fast));
                assertUnchanged(before, s);
                for (var m : baseline) checkMove(s, m);
                for (var m : fast) checkMove(s, m);
                FLPNewUtil.add(s, n - s.nAssigned() - 1, 0, s.rowSize(0), true).execute(s);
            }
            assertState(s, true);
            while (s.nAssigned() > 0) {
                var before = s.cloneSolution();
                var removals = new FLPRemoveNeighNew().explore(s).moves().toList();
                var slowRemovals = new FLPRemoveNeighNew(false).explore(s).moves().toList();
                assertEquals(s.nAssigned(), removals.size());
                assertEquals(new HashSet<>(slowRemovals), new HashSet<>(removals));
                for (var m : slowRemovals) checkMove(s, m);
                for (var m : removals) checkMove(s, m);
                assertUnchanged(before, s);
                removals.getLast().execute(s);
            }
            assertState(s, false);
            assertEquals(0, s.getScore());
        }
    }

    @Test
    void refreshUsesFacilityIdentitiesAndInvalidatesMissingOrSplitBlocks() {
        var s = fixture(instance(6, 2), new int[]{0,1,2}, new int[]{3,4});
        var add = FLPNewUtil.add(s, 5, 0, 1, true);
        var swap = FLPNewUtil.swap(s, 0, 4, true);
        var block = FLPNewUtil.relocate(s, 0, 0, 2, 1, 0, false, true);
        var reverse = FLPNewUtil.reverse(s, 0, 2, true);
        FLPNewUtil.swap(s, 1, 3, true).execute(s);
        assertThrows(AssertionError.class, () -> swap.execute(s));
        checkMove(s, add.refresh(s).orElseThrow());
        checkMove(s, swap.refresh(s).orElseThrow());
        assertTrue(block.refresh(s).isEmpty());
        checkMove(s, reverse.refresh(s).orElseThrow());
        FLPNewUtil.remove(s, 0).execute(s);
        assertTrue(swap.refresh(s).isEmpty());
        assertTrue(reverse.refresh(s).isEmpty());
        add.refresh(s).orElseThrow().execute(s);
        assertTrue(add.refresh(s).isEmpty());
        assertState(s, false);
    }

    @Test
    void randomMovesIncludeTinyLayoutsAndReproduciblyPreserveState() {
        for (var neighborhood : List.of(new FLPSwapNeighNew(), new FLPRelocateNeighNew(), new FLPOptNeighNew(),
                new FLPBlockRelocateNeighNew(2, true, FLPBlockRelocateNeighNew.Scope.ALL),
                new FLPCandidateRelocateNeighNew(4, 5, FLPCandidateRelocateNeighNew.Relation.MIXED))) {
            var s = fixture(instance(9, 3), new int[]{0,1,2,3}, new int[]{4,5}, new int[]{6,7,8});
            for (int i = 0; i < 30; i++) {
                var before = s.cloneSolution();
                var move = neighborhood.getRandomMove(s).orElseThrow();
                assertUnchanged(before, s);
                move.execute(s);
                assertState(s, true);
            }
        }
        var tiny = fixture(instance(2, 2), new int[]{0,1}, new int[]{});
        assertEquals(1, new FLPSwapNeighNew().explore(tiny).moves().count());
        assertTrue(new FLPSwapNeighNew().getRandomMove(tiny).isPresent());
        assertEquals(4, new FLPRelocateNeighNew().explore(tiny).moves().count());
        assertTrue(new FLPRelocateNeighNew().getRandomMove(tiny).isPresent());
    }

    @Test
    void movesApplyTheirNamedTransformationsIncludingBackwardAndBlockRelocations() {
        var s = fixture(instance(7, 3), new int[]{0,1,2}, new int[]{3,4}, new int[]{5});
        checkLayout(s, FLPNewUtil.swap(s, 0, 4, true), new int[]{4,1,2}, new int[]{3,0}, new int[]{5});
        checkLayout(s, FLPNewUtil.reverse(s, 0, 2, true), new int[]{2,1,0}, new int[]{3,4}, new int[]{5});
        checkLayout(s, FLPNewUtil.relocate(s, 0, 2, 1, 0, 0, false, true), new int[]{2,0,1}, new int[]{3,4}, new int[]{5});
        checkLayout(s, FLPNewUtil.relocate(s, 0, 0, 2, 1, 1, true, true), new int[]{2}, new int[]{3,1,0,4}, new int[]{5});
        checkLayout(s, FLPNewUtil.add(s, 6, 1, 1, true), new int[]{0,1,2}, new int[]{3,6,4}, new int[]{5});
        checkLayout(s, FLPNewUtil.remove(s, 0, 4), new int[]{1,2}, new int[]{3}, new int[]{5});
    }

    private void checkLayout(FLPSolution s, FLPNewMove move, int[]... expected) {
        var copy = s.cloneSolution();
        move.execute(copy);
        assertTrue(Arrays.deepEquals(expected, layout(copy)), move.toString());
        assertState(copy, false);
    }

    @Test
    void lazyExplorationAndDeadlinesAvoidEvaluatingUnusedMoves() {
        var s = spy(fixture(instance(20, 2), new int[]{0,1,2,3,4,5,6,7,8,9}, new int[]{10,11,12,13,14,15,16,17,18,19}));
        var baseline = new FLPSwapNeighNew(false).explore(s);
        verify(s, times(190)).getScore();
        clearInvocations(s);
        try (var moves = new FLPSwapNeighNew().explore(s).moves()) { assertTrue(moves.findFirst().isPresent()); }
        verify(s, never()).getScore();
        baseline.moves().close();
        TimeControl.setMaxExecutionTime(0, TimeUnit.NANOSECONDS);
        TimeControl.start();
        assertEquals(0, new FLPRelocateNeighNew().explore(s).moves().count());
        assertTrue(new FLPRelocateNeighNew().getRandomMove(s).isEmpty());
    }
}
