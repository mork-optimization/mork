package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.autoconfig.autollm.execution.WorkloadProtocol;
import java.nio.file.Path;
import java.util.Map;

/** Runner-owned state for one target. It never contains the coordinator's campaign or pool. */
public final class AutoLlmExecutionContext {
    public final String id;
    public final String targetDirectory;
    public final Path campaignRoot;
    public final String prompt;
    public AutoLlmConfig config;
    public String approvedScreeningPath;
    public String baselineCommit;
    public WorkloadProtocol.Host executionHost;
    public Map<String,String> versions = Map.of();
    public volatile Map<String,Object> remote;
    public volatile AutoLlmFailureUtil.Failure problem;
    public AutoLlmCampaign.Trial trial;

    public AutoLlmExecutionContext(String id, String targetDirectory, Path campaignRoot, String prompt) {
        this.id = id; this.targetDirectory = targetDirectory; this.campaignRoot = campaignRoot; this.prompt = prompt;
    }
    public Path path(String relative) { return AutoLlmFileUtil.inside(campaignRoot, relative); }
    public String relative(Path path) { return campaignRoot.relativize(path).toString(); }
    public Snapshot snapshot() {
        return new Snapshot(config, executionHost, versions, remote == null ? null : java.util.Collections.unmodifiableMap(new java.util.LinkedHashMap<>(remote)), trial == null ? null : trial.copy());
    }
    public record Snapshot(AutoLlmConfig config, WorkloadProtocol.Host executionHost, Map<String,String> versions,
                           Map<String,Object> remote, AutoLlmCampaign.Trial trial) {}
}
