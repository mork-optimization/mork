package es.urjc.etsii.grafo.autoconfig.autollm.execution;

import es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmFailureUtil;
import es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmConfig;
import es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmFileUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class SshTransportTest {
    @TempDir Path root;
    private AutoLlmConfig config(Path runner) {
        var server = new AutoLlmConfig.Server("a", "unused-host", "/worker", "java", Map.of(), null, null, null);
        return new AutoLlmConfig(new AutoLlmConfig.Project(root.toString(), null, null, null, null), root.toString(), null, null, null, null, null, null, null, null, null, null, null,
                new AutoLlmConfig.Execution(null, null, null, null, null, null, null, null, null, AutoLlmConfig.Target.SSH, new AutoLlmConfig.Ssh("a", runner.toString(), List.of(server), null)));
    }
    @Test void managedCampaignSharesOneFrozenRunnerAndPreservesItsIdentityAfterSourceChanges() throws Exception {
        Path original = root.resolve("built-runner.jar"); Files.writeString(original, "frozen runner");
        Path campaign = root.resolve("campaign"); AutoLlmFileUtil.write(campaign.resolve("control/storage-layout.json"), Map.of("version", 2));
        Path firstScratch = campaign.resolve("trials/001/.transport"), secondScratch = campaign.resolve("trials/002/.transport");
        var first = new SshTransport(config(original), firstScratch); var second = new SshTransport(config(original), secondScratch);
        Path firstJar = first.frozenRunner(), secondJar = second.frozenRunner();
        assertEquals(firstJar, secondJar); assertTrue(firstJar.startsWith(campaign.resolve(".store/artifacts")));
        assertFalse(Files.exists(firstScratch.resolve("runner.jar"))); assertFalse(Files.exists(secondScratch.resolve("runner.jar")));
        try (var files = Files.list(campaign.resolve(".store/artifacts"))) { assertEquals(1, files.count()); }
        Files.writeString(original, "newer runner"); assertEquals(firstJar, first.frozenRunner()); assertEquals("frozen runner", Files.readString(firstJar));
        CampaignAssetStore.releaseArtifact(campaign, campaign.resolve("unrelated-owner")); assertTrue(Files.isRegularFile(firstJar));
        Files.writeString(firstJar, "changed shared runner"); assertThrows(IllegalStateException.class, first::frozenRunner);
        assertThrows(IllegalStateException.class, first::storage, "Read-only capacity inspection must verify the shared runner before connecting");
    }
    @Test void legacyCampaignKeepsItsOriginalRunnerCopy() throws Exception {
        Path original = root.resolve("built-runner.jar"); Files.writeString(original, "legacy runner");
        Path scratch = root.resolve("legacy/.transport");
        assertEquals(scratch.resolve("runner.jar"), new SshTransport(config(original), scratch).frozenRunner());
        assertFalse(Files.exists(root.resolve("legacy/.store")));
    }
    @ParameterizedTest
    @ValueSource(strings = {
            "sign_and_send_pubkey: signing failed for RSA from agent: agent refused operation",
            "sign_and_send_pubkey: signing failed: agent communication failed",
            "ssh_fetch_identitylist: communication with agent failed"
    })
    void agentFailuresRemainRetryableDespiteFinalPermissionDenied(String diagnostic) {
        var error = SshTransport.connectionFailure(diagnostic + "\nuser@host: Permission denied (publickey).");
        assertInstanceOf(SshTransport.ConnectionException.class, error);
        var failure = AutoLlmFailureUtil.describe(error, null);
        assertEquals(AutoLlmFailureUtil.Category.TRANSIENT_INFRASTRUCTURE, failure.category());
        assertTrue(failure.retryable());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Host key verification failed.",
            "WARNING: REMOTE HOST IDENTIFICATION HAS CHANGED!",
            "Bad configuration option: invalid",
            "Could not resolve hostname missing: nodename nor servname provided"
    })
    void identityAndConfigurationFailuresOverrideAgentFailuresEvenOutsideMessageTail(String diagnostic) {
        var error = SshTransport.connectionFailure(diagnostic + "\n" + "x".repeat(4000)
                + "\nagent refused operation\nPermission denied (publickey).");
        assertInstanceOf(ExecutionTargetException.class, error);
        assertFalse(AutoLlmFailureUtil.describe(error, null).retryable());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Permission denied (publickey).", "Permission denied (publickey,password)."})
    void rejectedCredentialsRequireIntervention(String diagnostic) {
        assertInstanceOf(ExecutionTargetException.class, SshTransport.connectionFailure(diagnostic));
    }
}
