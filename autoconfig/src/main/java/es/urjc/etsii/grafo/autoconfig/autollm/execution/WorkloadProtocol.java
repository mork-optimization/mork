package es.urjc.etsii.grafo.autoconfig.autollm.execution;

import es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmProcessRunner;
import java.util.List;
import java.util.Map;

/** Versioned, file-based protocol; workload arguments never pass through a shell. */
public final class WorkloadProtocol {
    public static final int VERSION = 4;
    private WorkloadProtocol() {}
    public record Job(int version, String id, String root, String inputSha256, List<String> layers, List<String> command, String cwd, String output,
                      List<String> collect, List<String> reset, Map<String,String> environment, Map<String,String> expectedHost,
                      long timeoutMillis, long plannedMillis, long graceMillis, boolean watchEvaluations, long startupMillis) {}
    public enum JobStatus {
        NOT_FOUND, SUBMITTED, RUNNING, COLLECTING, SUCCEEDED, FAILED, CANCELLED, HOST_CHANGED, INTERRUPTED;
        public boolean inProgress() { return this == SUBMITTED || this == RUNNING || this == COLLECTING; }
    }
    public record State(String id, JobStatus status, AutoLlmProcessRunner.Result result, String failure, String archiveSha256) {}
    public record Host(int version, Map<String,String> identity, int requestedWorkers, int effectiveWorkers, long maxMemoryBytes,
                       long totalMemoryBytes, long usableDiskBytes, List<TargetPreflightUtil.Prerequisite> prerequisites) {}
    public record Chunk(long offset, String base64) {}
    public record Poll(State state, Map<String,Chunk> chunks, Map<String,String> snapshots) {}
}
