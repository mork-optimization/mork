package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.autoconfig.autollm.execution.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class AutoLlmLocalRecoveryTest {
    @TempDir Path trial;
    private static final String REQUEST = "0123456789abcdef0123456789abcdef";
    private Path unknown() throws Exception {
        trial = trial.toRealPath();
        Path output = trial.resolve("metadata/" + REQUEST + "/build");
        var owner = new ExecutionOwner(8, "campaign", null, trial.toString(), trial.toString(), AutoLlmConfig.defaults(), "controller", "controller");
        AutoLlmFileUtil.write(output.resolve("local-job.json"), new AutoLlmExecutionService.LocalJob("job", owner, List.of("./mvnw", "verify"), trial.resolve("source").toString()));
        AutoLlmFileUtil.write(output.resolve("process-intent.json"), Map.of("submission", "submission"));
        AutoLlmFileUtil.write(output.resolve("owner.json"), Map.of("pid", Long.MAX_VALUE, "start", "old", "executionTarget", "LOCAL"));
        return output;
    }
    private AutoLlmLocalRecovery.Confirmation confirmation(String submission) {
        return new AutoLlmLocalRecovery.Confirmation(submission, true, "Inspected exact owner, submission, logs and host process inventory; no surviving build or descendants");
    }
    @Test void unknownSubmissionIsUnresolvedAndRecoveryPreservesEvidenceWithoutFabricatingAResult() throws Exception {
        Path output = unknown();
        Path response = trial.resolve("metadata-requests/" + REQUEST + ".result.json");
        AutoLlmFileUtil.write(response, Map.of("error", "original failure"));
        assertTrue(AutoLlmCampaignStore.hasUncollectedJobs(trial));
        assertTrue(AutoLlmLocalRecovery.hasUnconfirmed(trial));
        assertEquals("TERMINATION_UNKNOWN", AutoLlmLocalRecovery.inspect(trial).getFirst().state());
        assertThrows(IllegalStateException.class, () -> AutoLlmLocalRecovery.confirm(trial, "job", confirmation("wrong")));
        var receipt = AutoLlmLocalRecovery.confirm(trial, "job", confirmation("submission"));
        assertEquals(receipt, AutoLlmLocalRecovery.confirm(trial, "job", confirmation("submission")));
        assertEquals("OPERATOR_ASSERTION_AFTER_HOST_INSPECTION", receipt.basis());
        assertFalse(AutoLlmLocalRecovery.hasUnconfirmed(trial));
        assertFalse(AutoLlmCampaignStore.hasUncollectedJobs(trial));
        assertDoesNotThrow(() -> AutoLlmProcessRunner.requireTerminated(trial));
        assertFalse(Files.exists(output.resolve("process-terminated.json")));
        assertFalse(Files.exists(output.resolve("workload-result.json")));
        assertTrue(Files.readString(response).contains("INTERRUPTED"));
        assertTrue(Files.readString(response.resolveSibling(REQUEST + ".before-recovery.result.json")).contains("original failure"));
        AutoLlmFileUtil.write(output.resolve("process-intent.json"), Map.of("submission", "different"));
        assertTrue(AutoLlmLocalRecovery.hasUnconfirmed(trial));
    }
    @Test void liveOrReusedPidsAndMissingConfirmationBlockReconciliation() throws Exception {
        Path output = unknown();
        assertThrows(IllegalArgumentException.class, () -> AutoLlmLocalRecovery.confirm(trial, "job", new AutoLlmLocalRecovery.Confirmation("submission", false, "reason")));
        assertThrows(IllegalArgumentException.class, () -> AutoLlmLocalRecovery.confirm(trial, "job", new AutoLlmLocalRecovery.Confirmation("submission", true, " ")));
        AutoLlmFileUtil.write(output.resolve("owner.json"), Map.of("pid", ProcessHandle.current().pid(), "start", "different", "executionTarget", "LOCAL"));
        assertFalse(AutoLlmLocalRecovery.inspect(trial).getFirst().confirmationAllowed());
        assertThrows(IllegalStateException.class, () -> AutoLlmLocalRecovery.confirm(trial, "job", confirmation("submission")));
    }
    @Test void pendingCollectionHasDifferentEligibilityAndCannotBeManuallyDiscarded() throws Exception {
        Path output = unknown();
        AutoLlmFileUtil.write(output.resolve("process-terminated.json"), Map.of("pid", Long.MAX_VALUE, "submission", "submission"));
        AutoLlmFileUtil.write(output.resolve("workload-result.json"), Map.of("result", "saved"));
        assertFalse(AutoLlmLocalRecovery.hasUnconfirmed(trial)); assertTrue(AutoLlmCampaignStore.hasUncollectedJobs(trial));
        assertEquals("PENDING_COLLECTION", AutoLlmLocalRecovery.inspect(trial).getFirst().state());
        assertThrows(IllegalStateException.class, () -> AutoLlmLocalRecovery.confirm(trial, "job", confirmation("submission")));
    }
    @Test void collectedHistoricalWorkDoesNotReopenWhenItsOldPidIsReused() throws Exception {
        Path output = unknown();
        AutoLlmFileUtil.write(output.resolve("owner.json"), Map.of("pid", ProcessHandle.current().pid(), "start", "old", "executionTarget", "LOCAL"));
        AutoLlmFileUtil.write(output.resolve("collection-completed.json"), Map.of("completed", true));
        assertFalse(AutoLlmLocalRecovery.hasUnconfirmed(trial));
        assertFalse(AutoLlmCampaignStore.hasUncollectedJobs(trial));
    }
    @Test void recoveryIsScopedToMetadataAndRejectsKnownSurvivingChildren() throws Exception {
        Path output = unknown();
        AutoLlmFileUtil.write(output.resolve("process-children.json"), Map.of("submission", "submission", "children", List.of(Map.of("pid", ProcessHandle.current().pid()))));
        assertThrows(IllegalStateException.class, () -> AutoLlmLocalRecovery.confirm(trial, "job", confirmation("submission")));
        Path outside = trial.resolve("tuning"); Files.move(output, outside);
        assertFalse(AutoLlmLocalRecovery.inspect(trial).getFirst().confirmationAllowed());
    }
}
