package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmCampaign.TargetStatus;

import java.util.*;

/** The same availability rules are used by recovery requests and operator interfaces. */
public final class AutoLlmRecoveryUtil {
    public record Action(boolean allowed, String reason, String effect) {
        public void requireAllowed() { if (!allowed) throw new IllegalStateException(reason); }
    }
    public record TrialActions(Action resume, Action fail, Action reconcileLocal) {}
    public record TargetActions(Action resume, Action requalify) {}
    public record Recovery(Action resumeCampaign, Map<Integer, TrialActions> trials, Map<String, TargetActions> targets) {}
    private AutoLlmRecoveryUtil() {}
    public static Action action(String reason, String effect) { return new Action(reason == null, reason, effect); }

    public static Recovery describe(AutoLlmCampaign campaign, String active, Set<String> busy, Set<String> uncollected, Set<String> unknownLocal, String blocked) {
        boolean running = campaign.id.equals(active);
        String common = blocked;
        if (common == null && active != null && !running) common = "Another campaign is active";
        if (common == null && !campaign.status.resumable()) common = "Campaign state " + campaign.status + " does not allow recovery";
        String resumeReason = common;
        if (resumeReason == null && running) resumeReason = "This campaign is already active; recover an individual trial or server";
        String activeReason = common;
        if (activeReason == null && !running) activeReason = "Resume the stopped campaign first";
        var targets = new LinkedHashMap<String, TargetActions>();
        for (var entry : campaign.targets.entrySet()) {
            String id = entry.getKey(); var target = entry.getValue();
            String reason = activeReason;
            if (reason == null && target.status == TargetStatus.REMOVED) reason = "Removed server IDs cannot be reused";
            if (reason == null && busy.contains(id)) reason = "The server already has an active controller workload";
            String qualifyReason = reason;
            if (qualifyReason == null && uncollected.contains(id)) qualifyReason = "Resume existing work to reconcile remote jobs and collect evidence before requalifying";
            targets.put(id, new TargetActions(action(reason, "Resume this server and its blocked trials on their saved checkpoints; clear draining"),
                    action(qualifyReason, "Run server qualification before resuming its blocked trials; preserve seeds and checkpoints")));
        }
        var trials = new LinkedHashMap<Integer, TrialActions>();
        for (var trial : campaign.trials) {
            String reason = common;
            if (reason == null && !trial.status.recoverable()) reason = "Only blocked or interrupted trials can be recovered";
            var target = campaign.targets.get(trial.targetId);
            boolean local = trial.targetId == null && trial.nextStage.ordinal() <= AutoLlmCampaign.Stage.PREPARATION.ordinal();
            if (reason == null && !local && (target == null || target.status == TargetStatus.REMOVED && !AutoLlmAssignmentPolicy.movable(trial))) reason = "The trial has no available assigned server";
            String failReason = reason;
            boolean ownBusy = busy.contains("$trial-" + trial.number) || busy.contains("$reconcile-" + trial.number)
                    || (trial.targetId != null && busy.contains(trial.targetId)) && target != null && (target.assignedTrial == null || Objects.equals(target.assignedTrial, trial.number));
            if (failReason == null && ownBusy) failReason = "This trial still has an active controller workload";
            if (reason == null && (ownBusy || (trial.targetId != null && busy.contains(trial.targetId)) && !AutoLlmAssignmentPolicy.movable(trial))) reason = "The assigned server already has an active controller workload";
            if (reason == null && unknownLocal.contains("$trial-" + trial.number)) reason = "Local process termination is unknown; inspect and reconcile the exact local job first";
            if (failReason == null && target != null && !target.baselineReady) failReason = "The trial's server must remain qualified";
            if (failReason == null && ((trial.targetId != null && uncollected.contains(trial.targetId)) || uncollected.contains("$trial-" + trial.number))) failReason = "Reconcile remote jobs and collect their evidence before failing the trial";
            String trialResumeReason = reason;
            if (trialResumeReason == null) trialResumeReason = activeReason;
            if (trialResumeReason == null && target != null && !target.baselineReady && !AutoLlmAssignmentPolicy.movable(trial)) trialResumeReason = "Requalify the assigned server first";
            String failEffect = "Mark this trial terminally FAILED and preserve its error, evidence and seeds";
            if (!running) failEffect += "; dispatch stays stopped until campaign resume";
            else if (target != null && !target.draining) failEffect += "; release its server for pending work";
            if (target != null && target.draining) failEffect += "; the server remains drained";
            String reconcileReason = common;
            if (reconcileReason == null && (ownBusy || !trial.status.recoverable())) reconcileReason = "Stop this trial's controller workload before reconciling a local job";
            if (reconcileReason == null && (!local || trial.nextStage.ordinal() >= AutoLlmCampaign.Stage.TUNING.ordinal())) reconcileReason = "Only local metadata work before tuning can be reconciled";
            if (reconcileReason == null && !unknownLocal.contains("$trial-" + trial.number)) reconcileReason = "No local job has unknown termination";
            trials.put(trial.number, new TrialActions(action(trialResumeReason, "Resume saved work; reconcile and revalidate unpinned preparation on an available approved server; retain tuning ownership"), action(failReason, failEffect),
                    action(reconcileReason, "Inspect exact local job/submission IDs, then record an explicit operator assertion with an audit reason; retire the interrupted metadata request")));
        }
        return new Recovery(action(resumeReason, "Resume the campaign and reconcile saved jobs; completed failed trials remain failed"), trials, targets);
    }
}
