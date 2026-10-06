package es.urjc.etsii.grafo.events.types;

import java.util.UUID;

/** Actual repetition start, independent of the executor's result/export order. */
public record WorkUnitStartedEvent(UUID resultId, String experimentName, String instanceName,
                                   String algorithmName, int ordinal, long seed) implements MorkEvent {}
