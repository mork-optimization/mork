package es.urjc.etsii.grafo.autoconfig.autollm;

import java.util.*;

/** The same availability rules are used by recovery requests and operator interfaces. */
public final class AutoLlmRecoveryUtil {
    public record Action(boolean allowed, String reason, String effect) {
        public void requireAllowed() { if (!allowed) throw new IllegalStateException(reason); }
    }
    public record TrialActions(Action resume, Action fail) {}
    public record TargetActions(Action resume, Action requalify) {}
    public record Recovery(Action resumeCampaign, Map<Integer, TrialActions> trials, Map<String, TargetActions> targets) {}
    private AutoLlmRecoveryUtil() {}
    public static Action action(String reason, String effect) { return new Action(reason == null, reason, effect); }

    public static Recovery describe(AutoLlmCampaign campaign, String active, Set<String> busy, Set<String> uncollected, String blocked) {
        boolean running = campaign.id.equals(active);
        String common = blocked;
        if (common == null && active != null && !running) common = "Another campaign is active";
        if (common == null && Set.of("DRAFT", "SUCCEEDED", "COMPLETED_WITH_FAILURES", "CANCELLATION_PENDING").contains(campaign.status)) common = "Campaign state " + campaign.status + " does not allow recovery";
        String resumeReason = common;
        if (resumeReason == null && running) resumeReason = "This campaign is already active; recover an individual trial or server";
        String activeReason = common;
        if (activeReason == null && !running) activeReason = "Resume the stopped campaign first";
        var targets = new LinkedHashMap<String, TargetActions>();
        for (var entry : campaign.targets.entrySet()) {
            String id = entry.getKey(); var target = entry.getValue();
            String reason = activeReason;
            if (reason == null && target.status.equals("REMOVED")) reason = "Removed server IDs cannot be reused";
            if (reason == null && busy.contains(id)) reason = "The server already has an active controller workload";
            String qualifyReason = reason;
            if (qualifyReason == null && uncollected.contains(id)) qualifyReason = "Resume existing work to reconcile remote jobs and collect evidence before requalifying";
            targets.put(id, new TargetActions(action(reason, "Resume this server and its blocked trials on their saved checkpoints; clear draining"),
                    action(qualifyReason, "Run server qualification before resuming its blocked trials; preserve seeds and checkpoints")));
        }
        var trials = new LinkedHashMap<Integer, TrialActions>();
        for (var trial : campaign.trials) {
            String reason = common;
            if (reason == null && !Set.of("BLOCKED", "INTERRUPTED").contains(trial.status)) reason = "Only blocked or interrupted trials can be recovered";
            var target = campaign.targets.get(trial.targetId);
            if (reason == null && (target == null || target.status.equals("REMOVED"))) reason = "The trial has no available assigned server";
            if (reason == null && busy.contains(trial.targetId)) reason = "The assigned server already has an active controller workload";
            String failReason = reason;
            if (failReason == null && !target.baselineReady) failReason = "The trial's server must remain qualified";
            if (failReason == null && uncollected.contains(trial.targetId)) failReason = "Reconcile remote jobs and collect their evidence before failing the trial";
            String trialResumeReason = reason;
            if (trialResumeReason == null) trialResumeReason = activeReason;
            if (trialResumeReason == null && !target.baselineReady) trialResumeReason = "Requalify the assigned server first";
            String failEffect = "Mark this trial terminally FAILED and preserve its error, evidence and seeds";
            if (!running) failEffect += "; dispatch stays stopped until campaign resume";
            else if (target != null && !target.draining) failEffect += "; release its server for pending work";
            if (target != null && target.draining) failEffect += "; the server remains drained";
            trials.put(trial.number, new TrialActions(action(trialResumeReason, "Resume only this trial on its original server using saved checkpoints and remote job identities; clear server draining"), action(failReason, failEffect)));
        }
        return new Recovery(action(resumeReason, "Resume the campaign and reconcile saved jobs; completed failed trials remain failed"), trials, targets);
    }
}
