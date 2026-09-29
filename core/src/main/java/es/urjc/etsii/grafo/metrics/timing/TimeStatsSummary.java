package es.urjc.etsii.grafo.metrics.timing;

import java.util.UUID;

/** Flushed counts acknowledge successful Writer.flush(), not fsync or crash durability. */
public record TimeStatsSummary(UUID runId, String status, int executions, int activeExecutions,
                               long observedEvents, long droppedEvents, long flushedEvents, String error) {}
