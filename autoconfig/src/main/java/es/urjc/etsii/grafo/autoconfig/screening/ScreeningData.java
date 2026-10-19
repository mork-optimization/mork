package es.urjc.etsii.grafo.autoconfig.screening;

import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.metrics.TimeValue;
import java.util.List;
import java.util.Map;

/** Immutable evidence shared by the controller, workers and offline review. */
public final class ScreeningData {
    private ScreeningData() {}
    public record Input(String path, String sha256) {}
    public record Manifest(int version, String fingerprint, InstanceScreeningConfig settings, long seed, String randomType,
            double trainingFraction, String objective, FMode mode, Map<String,String> host,
            List<Input> inputs, String sotaSha256, String artifactSha256, List<String> runArguments) {}
    public record Run(String fingerprint, String instance, long seed, boolean success, Double objective,
            long runtimeNanos, List<TimeValue> curve, String failure) {}
    public record RunEvidence(String sha256, Run run) {}
    public record Decision(String instance, String classification, Double bestObserved, Long firstPassTtbNanos,
            int confirmations, int fastHits, int failures, String reason) {}
    public record Job(Manifest manifest, String output) {}
    public record Proposal(String fingerprint, String state, List<Decision> instances, Map<String,String> files,
            int trainingCount, int testingCount, String splitFailure) {}
}
