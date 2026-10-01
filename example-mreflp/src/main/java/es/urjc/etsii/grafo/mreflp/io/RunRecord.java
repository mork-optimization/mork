package es.urjc.etsii.grafo.mreflp.io;

import es.urjc.etsii.grafo.mreflp.alg.LMLSParameters;

public record RunRecord(int formatVersion, String caseId, String algorithm, String protocol, long seed,
                        double budgetSeconds, int maxRestarts, LMLSParameters parameters, String randomType,
                        String sourceHash, String artifactHash, long cost, long runtimeNanos, long timeToBestNanos,
                        int[] assignments, String javaVersion, String javaVm, String os, String architecture,
                        int availableProcessors, boolean warmedUp, int warmupRepetitions, long warmupMillis) {}
