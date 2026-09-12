package es.urjc.etsii.grafo.bmssc;

import es.urjc.etsii.grafo.bmssc.model.BMSSCInstance;
import es.urjc.etsii.grafo.bmssc.model.sol.AssignMove;
import es.urjc.etsii.grafo.bmssc.model.sol.BMSSCSolution;
import es.urjc.etsii.grafo.bmssc.model.sol.BMSSCSolutionValidator;
import es.urjc.etsii.grafo.bmssc.util.BMSSCUtil;
import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.util.Context;
import es.urjc.etsii.grafo.util.TimeControl;
import es.urjc.etsii.grafo.util.random.RandomType;

import static org.junit.jupiter.api.Assertions.*;

final class BMSSCTestUtil {
    private BMSSCTestUtil() {}

    static void initialize(long seed) {
        Context.reset();
        TimeControl.remove();
        Context.Configurator.setObjectives(Main.OBJ);
        Context.Configurator.setValidator(new BMSSCSolutionValidator());
        Context.Configurator.resetRandom(RandomType.DEFAULT, seed);
    }

    static void cleanup() {
        Metrics.disableMetrics();
        TimeControl.remove();
        Context.reset();
    }

    static BMSSCInstance instance(int n, int k) {
        double[][] points = new double[n][3];
        for (int p = 0; p < n; p++) {
            points[p][0] = (p * 7) % 13;
            points[p][1] = (p * 3) % 11;
            points[p][2] = p / 3.0;
        }
        return new BMSSCInstance("fixture.csv", n, 3, k, points);
    }

    static BMSSCSolution assigned(BMSSCInstance instance, int... clusters) {
        var solution = new BMSSCSolution(instance);
        BMSSCUtil.withPartialSolution(() -> {
            for (int p = 0; p < clusters.length; p++) {
                if (clusters[p] >= 0) new AssignMove(solution, p, clusters[p]).execute(solution);
            }
            return solution;
        });
        return solution;
    }

    // Independent oracle: recompute squared distances from coordinates, never consult caches or move formulas.
    static double cost(BMSSCInstance instance, int[] assignment) {
        double total = 0;
        for (int c = 0; c < instance.k; c++) {
            int count = 0;
            double sum = 0;
            for (int p = 0; p < instance.n; p++) {
                if (assignment[p] != c) continue;
                count++;
                for (int q = p + 1; q < instance.n; q++) {
                    if (assignment[q] != c) continue;
                    double[] a = instance.getPoint(p), b = instance.getPoint(q);
                    for (int dimension = 0; dimension < instance.d; dimension++) {
                        double difference = a[dimension] - b[dimension];
                        sum += difference * difference;
                    }
                }
            }
            if (count > 0) total += sum / count;
        }
        return total;
    }

    static int[] assignment(BMSSCSolution solution) {
        int[] assignment = new int[solution.getInstance().n];
        for (int p = 0; p < assignment.length; p++) assignment[p] = solution.clusterOf(p);
        return assignment;
    }

    static void assertCostAndCaches(BMSSCSolution solution) {
        assertTrue(Double.isFinite(solution.getCost()));
        assertEquals(cost(solution.getInstance(), assignment(solution)), solution.getCost(), 1e-7);
        assertTrue(solution.cachesValid());
    }

    static void assertFeasible(BMSSCSolution solution) {
        var validation = new BMSSCSolutionValidator().validate(solution);
        assertTrue(validation.isValid(), () -> validation.getReasonFailed());
        assertCostAndCaches(solution);
    }
}
