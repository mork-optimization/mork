package es.urjc.etsii.grafo.tsptw.model;

import java.util.Arrays;
import java.util.Random;

final class TSPTWNewTestUtil {
    private TSPTWNewTestUtil() {}

    static TSPTWSolution generated(Random random, int n, boolean tight) {
        int[] x = new int[n], y = new int[n], starts = new int[n], ends = new int[n];
        int[] customers = new int[n - 1];
        for (int i = 0; i < n; i++) {
            x[i] = random.nextInt(40);
            y[i] = random.nextInt(40);
            starts[i] = i == 0 ? 0 : random.nextInt(70);
            if (i > 0) customers[i - 1] = i;
        }
        double[][] distances = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) distances[i][j] = Math.abs(x[i] - x[j]) + Math.abs(y[i] - y[j]);
        }
        Arrays.fill(ends, 100000);
        var broad = TSPTWTestUtil.tour(new TSPTWInstance("broad", n, distances, starts, ends), customers);
        if (tight) {
            for (int i = 1; i <= n; i++) ends[broad.permutation.get(i)] = (int) broad._makespan[i] + random.nextInt(35);
        }
        return TSPTWTestUtil.tour(new TSPTWInstance("generated", n, distances, starts, ends), customers);
    }

    static TSPTWSolution rebuild(TSPTWInstance instance, int[] route) {
        return TSPTWTestUtil.tour(instance, Arrays.copyOfRange(route, 1, route.length - 1));
    }

    static TSPTWSolution counterexample() {
        int[] x = {28, 24, 36, 23, 19, 33}, y = {4, 15, 30, 10, 1, 6};
        double[][] d = new double[6][6];
        for (int i = 0; i < 6; i++) {
            for (int j = 0; j < 6; j++) d[i][j] = Math.abs(x[i] - x[j]) + Math.abs(y[i] - y[j]);
        }
        return TSPTWTestUtil.tour(new TSPTWInstance("last-customer", 6, d, new int[6],
                new int[]{144, 25, 47, 91, 121, 117}), 1, 2, 3, 4, 5);
    }
}
