package es.urjc.etsii.grafo.autoconfig.autollm;

import java.nio.file.Files;
import java.nio.file.Path;

/** Shared scheduling/recovery rules; tuning ownership never moves between hosts. */
final class AutoLlmAssignmentPolicy {
    private AutoLlmAssignmentPolicy() {}
    static void refreshPins(AutoLlmCampaign campaign, Path root) {
        for (var trial : campaign.trials) for (var assignment : trial.assignments)
            if (Files.isRegularFile(root.resolve(assignment.directory).resolve("control/assignment-pinned.json"))) assignment.pinned = true;
    }
    static boolean movable(AutoLlmCampaign.Trial trial) {
        var assignment = trial.assignment();
        if (assignment == null || assignment.released || assignment.pinned
                || trial.nextStage.ordinal() < AutoLlmCampaign.Stage.PREPARATION.ordinal()
                || trial.nextStage.ordinal() > AutoLlmCampaign.Stage.TUNING.ordinal()) return false;
        for (var attempt : trial.attempts) if (assignment.id.equals(attempt.assignmentId)
                && attempt.stage.committedStage().ordinal() >= AutoLlmCampaign.Stage.TUNING.ordinal()) return false;
        return !trial.outputs.containsKey(AutoLlmCampaign.Stage.TUNING);
    }
    static boolean queued(AutoLlmCampaign.Trial trial) {
        return trial.status == AutoLlmCampaign.TrialStatus.PENDING || trial.status == AutoLlmCampaign.TrialStatus.WAITING_FOR_TARGET;
    }
    static boolean retained(AutoLlmCampaign.Trial trial) {
        return trial.assignment() != null && !movable(trial) && trial.nextStage.ordinal() >= AutoLlmCampaign.Stage.PREPARATION.ordinal();
    }
}
