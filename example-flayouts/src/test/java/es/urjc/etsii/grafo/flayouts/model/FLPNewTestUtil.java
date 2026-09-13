package es.urjc.etsii.grafo.flayouts.model;

import es.urjc.etsii.grafo.flayouts.Main;
import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.solution.SolutionValidator;
import es.urjc.etsii.grafo.solution.ValidationResult;
import es.urjc.etsii.grafo.util.Context;
import es.urjc.etsii.grafo.util.TimeControl;
import es.urjc.etsii.grafo.util.random.RandomType;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

public final class FLPNewTestUtil {
    private FLPNewTestUtil() {}

    public static void initialize(long seed) {
        Context.reset();
        TimeControl.remove();
        Metrics.disableMetrics();
        Context.Configurator.setObjectives(Main.FLOW);
        Context.Configurator.resetRandom(RandomType.DEFAULT, seed);
        Context.Configurator.setValidator(new SolutionValidator<FLPSolution, FLPInstance>() {
            @Override
            public ValidationResult validate(FLPSolution solution) {
                assertState(solution, false);
                return ValidationResult.ok();
            }
        });
        Context.Configurator.enableValidation();
    }

    public static void cleanup() { Metrics.disableMetrics(); TimeControl.remove(); Context.reset(); }

    public static FLPInstance instance(int n, int rows) {
        int[] lengths = new int[n];
        int[][] flow = new int[n][n];
        for (int f = 0; f < n; f++) {
            lengths[f] = 1 + f % 7;
            for (int g = 0; g < n; g++) flow[f][g] = f == g ? 0 : (Math.min(f, g) * 31 + Math.max(f, g) * 17) % 13;
        }
        return new FLPInstance("fixture-" + n + "-" + rows, lengths, flow, rows, Map.of());
    }

    public static FLPSolution fixture(FLPInstance instance, int[]... rows) {
        var solution = new FLPSolution(instance);
        for (int r = 0; r < rows.length; r++) {
            double left = 0;
            for (int f : rows[r]) {
                solution.rows[r][solution.rowSize[r]++] = f;
                solution.assignedFacilities++;
                solution.notAssignedFacilities.remove(f);
                solution.center[f] = left + instance.length(f) / 2.0;
                left += instance.length(f);
            }
        }
        solution.cachedScore = independentCost(instance, rows);
        solution.notifyUpdate();
        return solution;
    }

    public static int[][] layout(FLPSolution s) {
        int[][] result = new int[s.nRows()][];
        for (int r = 0; r < result.length; r++) result[r] = Arrays.copyOf(s.rows[r], s.rowSize(r));
        return result;
    }

    // Independent oracle: accumulate widths directly from row order and count each assigned pair once.
    public static double independentCost(FLPInstance instance, int[][] rows) {
        double total = 0;
        var assigned = new ArrayList<Integer>();
        double[] centers = new double[instance.nFacilities()];
        for (int[] row : rows) {
            double length = 0;
            for (int f : row) {
                centers[f] = length + instance.length(f) / 2.0;
                length += instance.length(f);
                assigned.add(f);
            }
        }
        for (int i = 0; i < assigned.size(); i++) for (int j = i + 1; j < assigned.size(); j++) {
            int a = assigned.get(i), b = assigned.get(j);
            total += Math.abs(centers[a] - centers[b]) * (instance.flow(a, b) + (double) instance.flow(b, a)) / 2;
        }
        return total;
    }

    public static void assertState(FLPSolution s, boolean complete) {
        var instance = s.getInstance();
        var missing = new HashSet<Integer>();
        for (int f = 0; f < instance.nFacilities(); f++) missing.add(f);
        int count = 0;
        for (int r = 0; r < s.nRows(); r++) {
            double left = 0;
            for (int p = 0; p < s.rows[r].length; p++) {
                int f = s.rows[r][p];
                if (p >= s.rowSize(r)) { assertEquals(FLPSolution.FREE_SPACE, f); continue; }
                assertTrue(missing.remove(f), "Duplicate/invalid facility " + f);
                assertEquals(left + instance.length(f) / 2.0, s.center[f], 1e-8);
                left += instance.length(f);
                count++;
            }
        }
        for (int f : missing) assertEquals(FLPSolution.UNKNOWN_CENTER, s.center[f]);
        assertEquals(count, s.nAssigned());
        assertEquals(missing, s.getNotAssignedFacilities());
        assertTrue(Double.isFinite(s.getScore()));
        assertEquals(independentCost(instance, layout(s)), s.getScore(), 1e-7);
        assertEquals(s.recalculateScore(), s.getScore(), 1e-7);
        if (complete) assertTrue(missing.isEmpty(), "Incomplete solution");
    }

    public static void assertUnchanged(FLPSolution before, FLPSolution after) {
        assertTrue(Arrays.deepEquals(before.rows, after.rows));
        assertArrayEquals(before.center, after.center);
        assertArrayEquals(before.rowSize, after.rowSize);
        assertEquals(before.nAssigned(), after.nAssigned());
        assertEquals(before.getNotAssignedFacilities(), after.getNotAssignedFacilities());
        assertEquals(before.getScore(), after.getScore());
        assertEquals(before.getVersion(), after.getVersion());
        assertEquals(before.getLastModifiedTime(), after.getLastModifiedTime());
    }

    public static void checkMove(FLPSolution original, FLPNewMove move) {
        var before = original.cloneSolution();
        var result = original.cloneSolution();
        move.execute(result);
        assertState(result, false);
        assertEquals(independentCost(result.getInstance(), layout(result)) - independentCost(original.getInstance(), layout(original)), move.delta(), 1e-7);
        assertEquals(original.getVersion() + 1, result.getVersion());
        assertUnchanged(before, original);
    }
}
