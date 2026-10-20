package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.autoconfig.autollm.execution.WorkloadProtocol;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Versioned JSON checkpoint. Mutations are serialized by the campaign service/store. */
public class AutoLlmCampaign {
    public int schemaVersion = 1;
    public String id;
    public AutoLlmConfig config;
    public String createdAt;
    public String status = "DRAFT";
    public String failure;
    public String reportFailure;
    public String reportUpdatedAt;
    public String baselineCommit;
    public String approvedScreeningPath;
    public WorkloadProtocol.Host executionHost;
    public Map<String,Object> remote;
    public String prompt;
    public Map<String, String> versions = Map.of();
    public CodexEvidenceUtil.Summary codexUsage;
    public List<Trial> trials = new ArrayList<>();
    public static class Trial {
        public int number;
        public long pilotSeed;
        public long tuningSeed;
        public long comparisonSeed;
        public String status = "PENDING";
        public String failure;
        public String threadId;
        public String blacklist;
        public String probeDirectory;
        public String generationDirectory;
        public String nextStep = "GENERATE";
        public boolean resumeTurn;
        public String winnerDirectory;
        public int repairTurns;
        public String preparedArtifact;
        public String mainDirectory;
        public String comparisonDirectory;
        public Object winner;
        public CodexEvidenceUtil.Summary codexUsage;
        public List<Attempt> attempts = new ArrayList<>();
    }
    public static class Attempt {
        public int number;
        public String stage;
        public String directory;
        public String status = "RUNNING";
        public String startedAt;
        public String finishedAt;
        public String failure;
        public CodexEvidenceUtil.Evidence codexEvidence;
    }
}
