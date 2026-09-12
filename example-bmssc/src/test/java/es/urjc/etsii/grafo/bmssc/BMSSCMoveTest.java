package es.urjc.etsii.grafo.bmssc;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.urjc.etsii.grafo.bmssc.model.sol.*;
import es.urjc.etsii.grafo.bmssc.util.BMSSCUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Random;

import static es.urjc.etsii.grafo.bmssc.BMSSCTestUtil.*;
import static org.junit.jupiter.api.Assertions.*;

class BMSSCMoveTest {
    @BeforeEach
    void setup() { initialize(1234); }

    @AfterEach
    void teardown() { cleanup(); }

    @Test
    void exhaustivelyChecksSmallAssignmentsAndEveryAdmissibleMove() {
        for (int n = 1; n <= 5; n++) {
            for (int k = 1; k <= n; k++) {
                var instance = instance(n, k);
                int combinations = (int) Math.pow(k + 1, n);
                for (int code = 0; code < combinations; code++) {
                    int[] assignment = new int[n];
                    int[] counts = new int[k];
                    int remaining = code;
                    boolean valid = true;
                    for (int p = 0; p < n; p++) {
                        int c = remaining % (k + 1) - 1;
                        remaining /= k + 1;
                        assignment[p] = c;
                        if (c >= 0 && ++counts[c] > instance.getClusterSize(c)) valid = false;
                    }
                    if (!valid) continue;
                    var solution = assigned(instance, assignment);
                    assertCostAndCaches(solution);
                    for (int p = 0; p < n; p++) {
                        for (int c = 0; c < k; c++) {
                            if (solution.canAssign(p, c) || solution.canReassign(p, c)) {
                                var copy = solution.cloneSolution();
                                int[] expected = assignment.clone();
                                expected[p] = c;
                                BMSSCMove move = solution.isAssigned(p)
                                        ? new ReassignMove(copy, p, c) : new AssignMove(copy, p, c);
                                checkMove(copy, move, expected);
                            }
                        }
                        for (int q = p + 1; q < n; q++) {
                            if (!solution.canSwap(p, q)) continue;
                            var copy = solution.cloneSolution();
                            int[] expected = assignment.clone();
                            expected[p] = assignment[q];
                            expected[q] = assignment[p];
                            checkMove(copy, new SwapMove(copy, p, q), expected);
                        }
                    }
                }
            }
        }
    }

    @Test
    void maintainsCachesAcrossLongMixedMoveSequences() {
        var random = new Random(981);
        var solution = assigned(instance(15, 4), 0, 1, 2, 3, 0, 1, 2, 3, 0, 1, 2, 3, 0, 1, 2);
        solution.relaxClusterSizeConstraint(1);
        BMSSCUtil.withPartialSolution(() -> {
            for (int step = 0; step < 2000; step++) {
                int p = random.nextInt(15), q = random.nextInt(15), c = random.nextInt(4);
                if (step % 2 == 0 && solution.canReassign(p, c)) new ReassignMove(solution, p, c).execute(solution);
                else if (solution.canSwap(p, q)) new SwapMove(solution, p, q).execute(solution);
                assertCostAndCaches(solution);
            }
            return solution;
        });
    }

    @Test
    void clonePreservesMetadataAndOwnsAllMutableState() {
        var original = assigned(instance(6, 2), 0, 1, 0, 1, 0, 1);
        var copy = original.cloneSolution();
        assertSame(original.getInstance(), copy.getInstance());
        assertEquals(original.getVersion(), copy.getVersion());
        assertEquals(original.getLastModifiedTime(), copy.getLastModifiedTime());
        assertEquals(original.lastExecutesMoves(), copy.lastExecutesMoves());
        double oldCost = original.getCost();
        int[] oldAssignment = assignment(original);
        copy.relaxClusterSizeConstraint(0.75);
        BMSSCUtil.withPartialSolution(() -> new ReassignMove(copy, 0, 1).execute(copy));
        assertArrayEquals(oldAssignment, assignment(original));
        assertEquals(oldCost, original.getCost());
        assertEquals(3, original.getClusterCapacity(0));
        assertEquals(4, copy.getClusterSize(1));
        assertCostAndCaches(copy);
        assertFeasible(original);
        assertThrows(UnsupportedOperationException.class, () -> original.getCluster(0).clear());
        assertThrows(UnsupportedOperationException.class, () -> original.getNotAssignedPoints().add(0));
        original.getPointAssignments()[0] = -1;
        assertArrayEquals(oldAssignment, original.getPointAssignments());
    }

    @Test
    void jsonExportIncludesTheAssignmentAndCostWithoutSerializingTheInstance() throws Exception {
        var solution = assigned(instance(6, 2), 0, 1, 0, 1, 0, 1);
        var mapper = new ObjectMapper();
        var exported = mapper.readTree(mapper.writeValueAsString(solution));
        assertFalse(exported.has("instance"));
        assertEquals(solution.getCost(), exported.get("cost").asDouble());
        assertArrayEquals(assignment(solution), mapper.treeToValue(exported.get("pointAssignments"), int[].class));
    }

    @Test
    void staleMovesMustRefreshOrBeDiscarded() {
        var solution = assigned(instance(6, 2), 0, 0, 0, 1, 1, 1);
        var stillApplicable = new SwapMove(solution, 0, 3);
        var becomesInvalid = new SwapMove(solution, 0, 4);
        new SwapMove(solution, 0, 3).execute(solution);
        assertThrows(AssertionError.class, () -> stillApplicable.execute(solution));
        assertTrue(becomesInvalid.refresh(solution).isEmpty());
        var refreshed = stillApplicable.refresh(solution).orElseThrow();
        assertNotSame(stillApplicable, refreshed);
        refreshed.execute(solution);
        assertFeasible(solution);
        assertTrue(stillApplicable.refresh(new BMSSCSolution(solution.getInstance())).isEmpty());
    }

    @Test
    void validatorAccumulatesMembershipSizeAndCacheFailures() throws Exception {
        var partial = assigned(instance(4, 2), 0, -1, 1, -1);
        var result = new BMSSCSolutionValidator().validate(partial);
        assertTrue(result.getFailCount() >= 4);
        var solution = assigned(instance(4, 2), 0, 0, 1, 1);
        Field lookup = BMSSCSolution.class.getDeclaredField("clusterOfPoint");
        lookup.setAccessible(true);
        Arrays.fill((int[]) lookup.get(solution), -1);
        Field cost = BMSSCSolution.class.getDeclaredField("cost");
        cost.setAccessible(true);
        cost.setDouble(solution, Double.NaN);
        result = new BMSSCSolutionValidator().validate(solution);
        assertTrue(result.getFailCount() >= 5);
        assertTrue(result.getReasonFailed().contains("Cost"));
    }

    private void checkMove(BMSSCSolution solution, BMSSCMove move, int[] expected) {
        double expectedCost = cost(solution.getInstance(), expected);
        assertTrue(Double.isFinite(move.getCostDelta()));
        assertEquals(expectedCost - solution.getCost(), move.getCostDelta(), 1e-8);
        BMSSCUtil.withPartialSolution(() -> move.execute(solution));
        assertArrayEquals(expected, assignment(solution));
        assertCostAndCaches(solution);
    }
}
