package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.autoconfig.autollm.execution.WorkloadProtocol;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Versioned JSON checkpoint. Mutations are serialized by the campaign service/store. */
public class AutoLlmCampaign {
    public static final int SCHEMA_VERSION = 4;
    public enum CampaignStatus {
        DRAFT, RUNNING, WAITING_FOR_REMOTE, WAITING_FOR_TARGETS, CANCELLATION_PENDING,
        INTERRUPTED, NEEDS_ATTENTION, CANCELLED, SUCCEEDED, COMPLETED_WITH_FAILURES;
        public boolean completed() { return this == SUCCEEDED || this == COMPLETED_WITH_FAILURES; }
        public boolean resumable() {
            return switch (this) {
                case RUNNING, WAITING_FOR_REMOTE, WAITING_FOR_TARGETS, INTERRUPTED, NEEDS_ATTENTION, CANCELLED -> true;
                case DRAFT, CANCELLATION_PENDING, SUCCEEDED, COMPLETED_WITH_FAILURES -> false;
            };
        }
        public boolean inProgress() { return this == RUNNING || this == WAITING_FOR_REMOTE || this == WAITING_FOR_TARGETS || this == CANCELLATION_PENDING; }
    }
    public enum TargetStatus { REGISTERED, VALIDATED, QUALIFYING, READY, BUSY, DRAINING, NEEDS_ATTENTION, REMOVED }
    public enum TrialStatus {
        PENDING, STARTING, WAITING_FOR_GENERATION, GENERATION, REPAIR, METADATA_RECONCILIATION,
        PREPARATION, TUNING, WINNER_EXPORT, COMPARISON, WAITING_FOR_QUOTA, WAITING_FOR_REMOTE,
        INTERRUPTED, BLOCKED, SUCCEEDED, FAILED;
        public boolean terminal() { return this == SUCCEEDED || this == FAILED; }
        public boolean recoverable() { return this == BLOCKED || this == INTERRUPTED; }
        public boolean inProgress() { return this != PENDING && !terminal(); }
    }
    public enum AttemptStatus { RUNNING, SUCCEEDED, FAILED, INTERRUPTED, QUOTA_LIMIT }
    public enum AttemptStage {
        GENERATION, REPAIR, PREPARATION, TUNING, WINNER_EXPORT, COMPARISON;
        public boolean generation() { return this == GENERATION || this == REPAIR; }
        public Stage committedStage() { return this == REPAIR ? Stage.GENERATION : Stage.valueOf(name()); }
        public TrialStatus trialStatus() { return TrialStatus.valueOf(name()); }
    }
    public enum PreparationStage { BUILD, DEPENDENCIES, METADATA, PROBES, PILOT }
    public int schemaVersion = SCHEMA_VERSION;
    public String id;
    public AutoLlmConfig config;
    public String createdAt;
    public CampaignStatus status = CampaignStatus.DRAFT;
    public String failure;
    public AutoLlmFailureUtil.Failure problem;
    public String reportFailure;
    public String reportUpdatedAt;
    public String baselineCommit;
    public String approvedScreeningPath;
    public WorkloadProtocol.Host executionHost;
    public Map<String,Object> remote;
    public String prompt;
    public Map<String, String> versions = Map.of();
    public CodexEvidenceUtil.Summary codexUsage;
    public long poolRevision;
    public Map<String, TargetState> targets = new java.util.LinkedHashMap<>();
    public List<Trial> trials = new ArrayList<>();

    public AutoLlmCampaign copy() {
        var result = new AutoLlmCampaign();
        result.schemaVersion = this.schemaVersion;
        result.id = this.id;
        result.config = this.config;
        result.createdAt = this.createdAt;
        result.status = this.status;
        result.failure = this.failure;
        result.problem = this.problem;
        result.reportFailure = this.reportFailure;
        result.reportUpdatedAt = this.reportUpdatedAt;
        result.baselineCommit = this.baselineCommit;
        result.approvedScreeningPath = this.approvedScreeningPath;
        result.executionHost = this.executionHost;
        result.remote = remote == null ? null : new java.util.LinkedHashMap<>(remote);
        result.prompt = this.prompt;
        result.versions = this.versions;
        result.codexUsage = this.codexUsage;
        result.poolRevision = this.poolRevision;
        for (var entry : targets.entrySet()) result.targets.put(entry.getKey(), entry.getValue().copy());
        for (var trial : trials) result.trials.add(trial.copy());
        return result;
    }
    /** Small polling projection; full evidence remains in campaign.json and the detail endpoint. */
    public AutoLlmCampaign statusSnapshot() {
        var result = copy();
        result.prompt = null; result.failure = null;
        for (var target : result.targets.values()) target.resolvedConfig = null;
        for (var trial : result.trials) {
            trial.winner = null; trial.blacklist = null; trial.failure = null;
            for (var attempt : trial.attempts) { attempt.failure = null; attempt.codexEvidence = null; }
        }
        return result;
    }
    public static class TargetState {
        public AutoLlmConfig.Server definition;
        public TargetStatus status = TargetStatus.REGISTERED;
        public String directory;
        public String endpoint;
        public long revision;
        public boolean draining;
        public boolean baselineReady;
        public Integer assignedTrial;
        public AutoLlmConfig resolvedConfig;
        public WorkloadProtocol.Host host;
        public Map<String, Object> remote;
        public Map<String, String> versions = Map.of();
        public AutoLlmFailureUtil.Failure problem;

        public void resume(boolean requalify, long revision) {
            if (requalify) baselineReady = false;
            draining = false; problem = null; this.revision = revision;
            status = baselineReady ? TargetStatus.READY : TargetStatus.REGISTERED;
        }
        public TargetState copy() {
            var result = new TargetState();
            result.definition = this.definition;
            result.status = this.status;
            result.directory = this.directory;
            result.endpoint = this.endpoint;
            result.revision = this.revision;
            result.draining = this.draining;
            result.baselineReady = this.baselineReady;
            result.assignedTrial = this.assignedTrial;
            result.resolvedConfig = this.resolvedConfig;
            result.host = this.host;
            result.remote = remote == null ? null : new java.util.LinkedHashMap<>(remote);
            result.versions = this.versions;
            result.problem = this.problem;
            return result;
        }
    }
    public enum Stage { GENERATION, PREPARATION, TUNING, WINNER_EXPORT, COMPARISON, COMPLETE }
    public record StageOutput(String directory, String artifact) {}
    public static class Trial {
        public int number;
        public String targetId;
        public String directory;
        public long targetRevision;
        public long pilotSeed;
        public long tuningSeed;
        public long comparisonSeed;
        public TrialStatus status = TrialStatus.PENDING;
        public String failure;
        public AutoLlmFailureUtil.Failure problem;
        public int infrastructureRetries;
        public Map<String,Integer> connectionRetries = new java.util.LinkedHashMap<>();
        public String threadId;
        public String blacklist;
        public Stage nextStage = Stage.GENERATION;
        public Map<Stage, StageOutput> outputs = new java.util.EnumMap<>(Stage.class);
        public boolean resumeTurn;
        public String quotaRetryAt;
        public boolean repairPending;
        public int repairTurns;
        public Object winner;
        public CodexEvidenceUtil.Summary codexUsage;
        public List<Attempt> attempts = new ArrayList<>();
        public String outputDirectory(Stage stage) { var output = outputs.get(stage); return output == null ? null : output.directory(); }
        public String latestDirectory(Stage stage) {
            for (int i = attempts.size() - 1; i >= 0; i--) if (attempts.get(i).stage.committedStage() == stage) return attempts.get(i).directory;
            return outputDirectory(stage);
        }
        public String artifact() { var output = outputs.get(Stage.PREPARATION); return output == null ? null : output.artifact(); }
        public void complete(Stage stage, String directory, String artifact) {
            outputs.put(stage, new StageOutput(directory, artifact));
            nextStage = Stage.values()[stage.ordinal() + 1];
        }

        public void queueForResume() {
            if (status.terminal()) throw new IllegalStateException("Completed trials cannot be resumed");
            status = TrialStatus.PENDING; infrastructureRetries = 0;
        }
        public Trial copy() {
            var result = new Trial();
            result.number = this.number;
            result.targetId = this.targetId;
            result.directory = this.directory;
            result.targetRevision = this.targetRevision;
            result.pilotSeed = this.pilotSeed;
            result.tuningSeed = this.tuningSeed;
            result.comparisonSeed = this.comparisonSeed;
            result.status = this.status;
            result.failure = this.failure;
            result.problem = this.problem;
            result.infrastructureRetries = this.infrastructureRetries;
            result.connectionRetries.putAll(connectionRetries);
            result.threadId = this.threadId;
            result.blacklist = this.blacklist;
            result.nextStage = this.nextStage;
            result.outputs.putAll(outputs);
            result.resumeTurn = this.resumeTurn;
            result.quotaRetryAt = this.quotaRetryAt;
            result.repairPending = this.repairPending;
            result.repairTurns = this.repairTurns;
            result.winner = this.winner;
            result.codexUsage = this.codexUsage;
            for (var attempt : attempts) result.attempts.add(attempt.copy());
            return result;
        }
    }
    public static class Attempt {
        public int number;
        public AttemptStage stage;
        public String directory;
        public AttemptStatus status = AttemptStatus.RUNNING;
        public String startedAt;
        public String finishedAt;
        public String failure;
        public AutoLlmFailureUtil.Failure problem;
        public PreparationStage substage;
        public CodexEvidenceUtil.Evidence codexEvidence;

        public Attempt copy() {
            var result = new Attempt();
            result.number = this.number;
            result.stage = this.stage;
            result.directory = this.directory;
            result.status = this.status;
            result.startedAt = this.startedAt;
            result.finishedAt = this.finishedAt;
            result.failure = this.failure;
            result.problem = this.problem;
            result.substage = this.substage;
            result.codexEvidence = this.codexEvidence;
            return result;
        }
    }
}
