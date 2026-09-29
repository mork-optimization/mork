package es.urjc.etsii.grafo.metrics.timing;

import java.util.UUID;

/** Metadata only: never retain an algorithm, solution, or instance object. */
public record TimeStatsExecution(UUID executionId, String experiment, String instancePath,
                                 String instanceId, String algorithm, int repetition, long seed) {}
