package es.urjc.etsii.grafo.tsptw.model;

import es.urjc.etsii.grafo.metrics.Metrics;
import es.urjc.etsii.grafo.metrics.DeclaredObjective;
import es.urjc.etsii.grafo.tsptw.Main;
import es.urjc.etsii.grafo.util.Context;
import es.urjc.etsii.grafo.util.TimeControl;
import es.urjc.etsii.grafo.util.random.RandomType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public final class TSPTWTestUtil {
    private TSPTWTestUtil() {}

    public static void initialize() {
        Context.reset();
        TimeControl.remove();
        Context.Configurator.setObjectives(Main.OBJECTIVE);
        Context.Configurator.setValidator(new TSPTWSolutionValidator());
        Context.Configurator.resetRandom(RandomType.DEFAULT, 1);
    }

    public static void cleanup() {
        TimeControl.remove();
        Metrics.disableMetrics();
        Context.reset();
    }

    public static TSPTWInstance instance(String name) throws IOException {
        var resource = Objects.requireNonNull(TSPTWTestUtil.class.getResourceAsStream("/tsptw/" + name + ".txt"));
        try (var reader = new BufferedReader(new InputStreamReader(resource, StandardCharsets.UTF_8))) {
            return new TSPTWInstanceImporter().importInstance(reader, name);
        }
    }

    public static TSPTWSolution tour(TSPTWInstance instance, int... customers) {
        var solution = new TSPTWSolution(instance);
        solution.add(customers);
        solution.notifyUpdate();
        return solution;
    }

    public static void expireTimeLimit() {
        TimeControl.setMaxExecutionTime(-1, TimeUnit.NANOSECONDS);
        TimeControl.start();
    }

    public static void startTimeLimit() {
        TimeControl.setMaxExecutionTime(1, TimeUnit.DAYS);
        TimeControl.start();
    }

    public static void enableMetrics() {
        Metrics.register("Cost", time -> new DeclaredObjective("Cost", Main.OBJECTIVE.getFMode(), time));
        Metrics.enableMetrics();
        Metrics.resetMetrics();
    }

    /** Full independent evaluation, including tours that are valid permutations but violate time windows. */
    public static void assertConsistent(TSPTWSolution solution) {
        var instance = solution.getInstance();
        int n = instance.n();
        assertEquals(n + 1, solution.permutation.size());
        assertEquals(0, solution.permutation.getFirst());
        assertEquals(0, solution.permutation.getLast());
        boolean[] seen = new boolean[n];
        seen[0] = true;
        double cost = 0, time = 0, lateness = 0;
        int violations = 0;
        for (int i = 1; i <= n; i++) {
            int previous = solution.permutation.get(i - 1);
            int customer = solution.permutation.get(i);
            if (i < n) {
                assertFalse(seen[customer]);
                seen[customer] = true;
            }
            cost += instance.dist(previous, customer);
            time = Math.max(time + instance.dist(previous, customer), instance.getWindowStart(customer));
            assertEquals(time, solution._makespan[i], 1e-9);
            if (time > instance.getWindowEnd(customer)) {
                violations++;
                lateness += time - instance.getWindowEnd(customer);
            }
        }
        assertEquals(cost, solution.cost(), 1e-9);
        assertEquals(violations, solution.constraint_violations());
        assertEquals(lateness, solution.infeasibility(), 1e-9);
        assertEquals(0, solution.nodes_available);
        assertArrayEquals(seen, solution.node_assigned);
    }
}
