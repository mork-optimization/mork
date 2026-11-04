package es.urjc.etsii.grafo.benchmark;

import java.util.Map;
import java.util.Objects;

/** Execution metadata, distinct from the reusable measurement on disk. */
public record BenchmarkReport(Status status, Map<String, String> environment, BenchmarkResult result, String detail) {
    public enum Status { NOT_RECORDED, DISABLED, REUSED, MEASURED, UNAVAILABLE }

    public BenchmarkReport {
        Objects.requireNonNull(status);
        environment = Map.copyOf(environment);
        Objects.requireNonNull(detail);
        if ((status == Status.REUSED || status == Status.MEASURED) != (result != null)) {
            throw new IllegalArgumentException("Benchmark status and result disagree");
        }
    }

    public static BenchmarkReport notRecorded(Map<String, String> environment) {
        return new BenchmarkReport(Status.NOT_RECORDED, environment, null, "Not recorded");
    }
}
