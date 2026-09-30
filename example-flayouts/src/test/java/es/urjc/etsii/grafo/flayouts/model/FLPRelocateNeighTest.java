package es.urjc.etsii.grafo.flayouts.model;

import es.urjc.etsii.grafo.util.TimeControl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static es.urjc.etsii.grafo.flayouts.model.FLPNewTestUtil.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

class FLPRelocateNeighTest {
    @BeforeEach void setup() { initialize(1234); }
    @AfterEach void teardown() { cleanup(); }

    @Test
    void removalAndInsertionProduceExactRelocationsWithoutChangingTheSource() {
        var fixtures = List.of(
                fixture(instance(9, 2), new int[]{0, 1, 2, 3}, new int[]{4, 5, 6, 7, 8}),
                fixture(instance(9, 3), new int[]{8, 2, 6}, new int[]{4, 1}, new int[]{}),
                fixture(instance(4, 2), new int[]{0, 1, 2, 3}, new int[]{}),
                fixture(instance(4, 1), new int[]{3, 2, 1, 0}));
        for (var solution : fixtures) {
            var before = solution.cloneSolution();
            var expected = new HashMap<FLPRelocateNeigh.RelocateMove, int[][]>();
            for (int row = 0; row < solution.nRows(); row++) {
                for (int pos = 0; pos < solution.rowSize(row); pos++) {
                    var removed = layout(solution);
                    int facility = removed[row][pos];
                    var shortened = new int[removed[row].length - 1];
                    System.arraycopy(removed[row], 0, shortened, 0, pos);
                    System.arraycopy(removed[row], pos + 1, shortened, pos, shortened.length - pos);
                    removed[row] = shortened;
                    for (int target = 0; target < solution.nRows(); target++) {
                        for (int gap = 0; gap <= removed[target].length; gap++) {
                            if (target == row && gap == pos) continue;
                            var result = removed.clone();
                            var inserted = new int[removed[target].length + 1];
                            System.arraycopy(removed[target], 0, inserted, 0, gap);
                            inserted[gap] = facility;
                            System.arraycopy(removed[target], gap, inserted, gap + 1, removed[target].length - gap);
                            result[target] = inserted;
                            expected.put(new FLPRelocateNeigh.RelocateMove(solution, row, pos, target, gap, 0), result);
                        }
                    }
                }
            }
            for (int repetition = 0; repetition < 2; repetition++) {
                try (var stream = new FLPRelocateNeigh(true).explore(solution).moves()) {
                    var moves = stream.toList();
                    assertEquals(expected.keySet(), new HashSet<>(moves));
                    assertEquals(expected.size(), moves.size());
                    for (var move : moves) {
                        var result = solution.cloneSolution();
                        move.execute(result);
                        assertTrue(Arrays.deepEquals(expected.get(move), layout(result)), move.toString());
                        checkMove(solution, move);
                    }
                }
                assertUnchanged(before, solution);
            }
        }
    }

    @Test
    void failedRelocationEvaluationLeavesTheSourceUntouched() {
        var instance = spy(instance(9, 2));
        var solution = fixture(instance, new int[]{0, 1, 2}, new int[]{3, 4});
        var before = solution.cloneSolution();
        var failure = new IllegalStateException("Injected cost-evaluation failure");
        doThrow(failure).when(instance).flow(anyInt(), anyInt());
        assertSame(failure, assertThrows(IllegalStateException.class,
                () -> new FLPRelocateNeigh(true).explore(solution)));
        assertUnchanged(before, solution);
    }

    @Test
    void expiredDeadlineDoesNotExploreOrChangeTheSource() {
        var solution = fixture(instance(9, 2), new int[]{0, 1, 2}, new int[]{3, 4});
        var before = solution.cloneSolution();
        TimeControl.setMaxExecutionTime(0, TimeUnit.NANOSECONDS);
        TimeControl.start();
        try (var moves = new FLPRelocateNeigh(true).explore(solution).moves()) {
            assertEquals(0, moves.count());
        }
        assertUnchanged(before, solution);
    }
}
