package es.urjc.etsii.grafo.metrics.timing;

/** Cached description of a timed method, shared by all its invocations. */
public record TimeStatsMethod(String componentClass, String signature) {}
