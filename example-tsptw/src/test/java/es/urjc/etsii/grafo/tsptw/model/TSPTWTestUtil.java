package es.urjc.etsii.grafo.tsptw.model;

import es.urjc.etsii.grafo.metrics.Metrics;
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
}
