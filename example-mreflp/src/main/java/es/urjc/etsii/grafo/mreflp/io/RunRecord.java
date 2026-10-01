package es.urjc.etsii.grafo.mreflp.io;

import java.util.Map;

public record RunRecord(int formatVersion, String caseId, String algorithm, String protocol, long seed,
                        double budgetSeconds, int maxRestarts, Map<String, Number> parameters, String randomType,
                        String sourceHash, String artifactHash, long cost, long runtimeNanos, long timeToBestNanos,
                        int[] assignments, String javaVersion, String javaVm, String os, String architecture,
                        int availableProcessors, boolean warmedUp, int warmupRepetitions, long warmupMillis) {}
