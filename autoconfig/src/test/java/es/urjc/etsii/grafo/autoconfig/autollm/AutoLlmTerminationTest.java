package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.autoconfig.autollm.execution.OwnershipManifest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AutoLlmTerminationTest {
    @TempDir Path temporary;
    private static final String OLD_START = "1970-01-01T00:00:00Z";
    private static final String SUBMISSION = "completed-submission";

    private void owner(Path output, long pid, String start, String target) {
        AutoLlmFileUtil.write(output.resolve("owner.json"), Map.of("pid", pid, "start", start, "executionTarget", target));
        AutoLlmFileUtil.write(output.resolve("process-intent.json"), Map.of("submission", SUBMISSION));
    }
    private void proof(Path output, long pid, String submission) {
        AutoLlmFileUtil.write(output.resolve("process-terminated.json"), Map.of("pid", pid, "submission", submission));
    }
    private String currentStart() { return ProcessHandle.current().info().startInstant().orElseThrow().toString(); }

    @Test void reusedParentPidRequiresTheMatchingTerminationReceipt() throws Exception {
        long pid = ProcessHandle.current().pid();
        assertNotEquals(OLD_START, currentStart());
        owner(temporary, pid, OLD_START, "LOCAL");
        assertThrows(IllegalStateException.class, () -> AutoLlmProcessRunner.requireTerminated(temporary));
        proof(temporary, pid, "another-submission");
        assertThrows(IllegalStateException.class, () -> AutoLlmProcessRunner.requireTerminated(temporary));
        proof(temporary, Long.MAX_VALUE, SUBMISSION);
        assertThrows(IllegalStateException.class, () -> AutoLlmProcessRunner.requireTerminated(temporary));
        proof(temporary, pid, SUBMISSION);
        assertDoesNotThrow(() -> AutoLlmProcessRunner.requireTerminated(temporary));
        assertTrue(ProcessHandle.current().isAlive());
    }

    @Test void matchingLiveParentCannotBeOverriddenByAReceipt() {
        long pid = ProcessHandle.current().pid();
        owner(temporary, pid, currentStart(), "LOCAL");
        proof(temporary, pid, SUBMISSION);
        assertThrows(IllegalStateException.class, () -> AutoLlmProcessRunner.requireTerminated(temporary));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " "})
    void unidentifiedLiveParentStillBlocksCompletion(String start) {
        long pid = ProcessHandle.current().pid();
        owner(temporary, pid, start, "LOCAL");
        proof(temporary, pid, SUBMISSION);
        assertThrows(IllegalStateException.class, () -> AutoLlmProcessRunner.requireTerminated(temporary));
    }

    @Test void recordedChildrenMustBeStoppedEvenWhenTheParentHasATerminationReceipt() {
        owner(temporary, Long.MAX_VALUE, OLD_START, "LOCAL");
        proof(temporary, Long.MAX_VALUE, SUBMISSION);
        Path children = temporary.resolve("process-children.json");
        AutoLlmFileUtil.write(children, Map.of("submission", SUBMISSION, "children",
                List.of(Map.of("pid", ProcessHandle.current().pid(), "start", currentStart()))));
        assertThrows(IllegalStateException.class, () -> AutoLlmProcessRunner.requireTerminated(temporary));
        AutoLlmFileUtil.write(children, Map.of("submission", SUBMISSION, "children",
                List.of(Map.of("pid", ProcessHandle.current().pid(), "start", OLD_START))));
        assertDoesNotThrow(() -> AutoLlmProcessRunner.requireTerminated(temporary));
        AutoLlmFileUtil.write(children, Map.of("submission", SUBMISSION, "children",
                List.of(Map.of("pid", ProcessHandle.current().pid(), "start", ""))));
        assertThrows(IllegalStateException.class, () -> AutoLlmProcessRunner.requireTerminated(temporary));
        AutoLlmFileUtil.write(children, Map.of("submission", "other-submission", "children", List.of()));
        assertThrows(IllegalStateException.class, () -> AutoLlmProcessRunner.requireTerminated(temporary));
    }

    @Test void cleanupWaitsForProofThenDeletesArtifactsDespiteAReusedPid() throws Exception {
        Path workspace = temporary.toRealPath().resolve("workspace");
        var config = AutoLlmFileUtil.JSON.convertValue(Map.of("workspace", workspace.toString()), AutoLlmConfig.class);
        var store = new AutoLlmCampaignStore(config);
        String id = UUID.randomUUID().toString();
        Path campaign = store.directory(id), output = campaign.resolve("capture/git");
        new OwnershipManifest(workspace).directory(campaign, "campaign", campaign.toString());
        owner(output, ProcessHandle.current().pid(), OLD_START, "LOCAL");
        var cleanup = new AutoLlmCleanup(store);
        var plan = cleanup.plan(new AutoLlmCleanup.Selection("campaign", id, null, false, true));
        assertEquals("PENDING", cleanup.apply(plan.digest(), true).status());
        assertTrue(Files.exists(campaign));
        proof(output, ProcessHandle.current().pid(), SUBMISSION);
        var receipt = cleanup.apply(plan.digest(), true);
        assertEquals("SUCCEEDED", receipt.status(), receipt.toString());
        assertFalse(Files.exists(campaign));
        assertTrue(ProcessHandle.current().isAlive());
    }

    @Test void cleanupRetainsArtifactsWhileARecordedChildIsLive() throws Exception {
        Path workspace = temporary.toRealPath().resolve("workspace");
        var config = AutoLlmFileUtil.JSON.convertValue(Map.of("workspace", workspace.toString()), AutoLlmConfig.class);
        var store = new AutoLlmCampaignStore(config);
        String id = UUID.randomUUID().toString();
        Path campaign = store.directory(id), output = campaign.resolve("build");
        new OwnershipManifest(workspace).directory(campaign, "campaign", campaign.toString());
        owner(output, Long.MAX_VALUE, OLD_START, "LOCAL");
        proof(output, Long.MAX_VALUE, SUBMISSION);
        AutoLlmFileUtil.write(output.resolve("process-children.json"), Map.of("submission", SUBMISSION, "children",
                List.of(Map.of("pid", ProcessHandle.current().pid(), "start", currentStart()))));
        var cleanup = new AutoLlmCleanup(store);
        var plan = cleanup.plan(new AutoLlmCleanup.Selection("campaign", id, null, false, true));
        assertEquals("PENDING", cleanup.apply(plan.digest(), true).status());
        assertTrue(Files.exists(campaign));
        assertTrue(ProcessHandle.current().isAlive());
    }

    @Test void collectedRemoteProcessIdsAreNotCheckedAgainstTheController() {
        owner(temporary, ProcessHandle.current().pid(), currentStart(), "SSH");
        AutoLlmFileUtil.write(temporary.resolve("process-children.json"), Map.of("submission", SUBMISSION, "children",
                List.of(Map.of("pid", ProcessHandle.current().pid(), "start", currentStart()))));
        assertDoesNotThrow(() -> AutoLlmProcessRunner.requireTerminated(temporary, true));
        assertThrows(IllegalStateException.class, () -> AutoLlmProcessRunner.requireTerminated(temporary),
                "The worker must still require termination evidence for its own processes");
    }
}
