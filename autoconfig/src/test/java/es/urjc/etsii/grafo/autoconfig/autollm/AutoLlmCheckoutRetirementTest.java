package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.autoconfig.autollm.execution.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class AutoLlmCheckoutRetirementTest {
    @TempDir Path temporary;
    private final AutoLlmProcessRunner processes = new AutoLlmProcessRunner();
    private final AutoLlmConfig config = AutoLlmConfig.defaults();
    private Path campaign, trial, source;
    private String commit;
    private final AutoLlmCampaign.Trial state = new AutoLlmCampaign.Trial();

    private void prepare() throws Exception {
        campaign = temporary.resolve("campaign"); trial = campaign.resolve("trials/001"); source = trial.resolve("source");
        Files.createDirectories(source); Files.writeString(source.resolve("file.txt"), "original");
        Files.writeString(source.resolve(".gitignore"), "target/\n*.input\n");
        git(source, "init"); git(source, "add", "."); git(source, "-c", "user.name=Test", "-c", "user.email=test@example.test", "commit", "-m", "baseline");
        commit = git(source, "rev-parse", "HEAD").strip();
        AutoLlmFileUtil.write(campaign.resolve("control/storage-layout.json"), Map.of("version", 1));
        state.status = AutoLlmCampaign.TrialStatus.FAILED;
    }
    private String git(Path cwd, String... arguments) throws Exception {
        var args = new ArrayList<String>(); args.add("git"); args.addAll(List.of(arguments));
        Path logs = temporary.resolve("commands/" + UUID.randomUUID()); command(args, cwd, logs);
        return Files.readString(logs.resolve("stdout.log"));
    }
    private void command(List<String> arguments, Path cwd, Path logs) {
        var result = processes.run(arguments, cwd, logs, 10000, 0, 100, () -> false, false, 0);
        assertEquals(0, result.exitCode(), () -> arguments + " " + AutoLlmFailureUtil.tail(logs.resolve("stderr.log"), 2000));
    }
    private void maintain() {
        AutoLlmCheckoutRetirement.maintainTrial(campaign, trial, state, config, commit, Map.of(), processes, () -> false);
    }
    private Path evidence() { return trial.resolve("storage/checkout"); }
    private void assertReleased() {
        var receipt = AutoLlmFileUtil.read(evidence().resolve("retention.json"), Map.class);
        assertEquals("RELEASED", receipt.get("status"), receipt.toString()); assertFalse(Files.exists(source));
    }

    @Test void preservesFailedSourcesDiagnosticsStagedDataAndUnreachableGitObjects() throws Exception {
        prepare();
        Files.writeString(source.resolve("file.txt"), "staged"); git(source, "add", "file.txt");
        String tree = git(source, "write-tree").strip();
        String dangling = git(source, "-c", "user.name=Test", "-c", "user.email=test@example.test", "commit-tree", tree, "-p", commit, "-m", "unreachable commit").strip();
        Files.writeString(source.resolve("file.txt"), "working tree");
        Files.writeString(source.resolve("build.input"), "ignored source input");
        Files.createDirectories(source.resolve("target/reports"));
        Files.writeString(source.resolve("target/reports/build.xml"), "unique diagnostic");
        Files.writeString(source.resolve("target/project.jar"), "unverified partial executable");
        Files.createDirectories(source.resolve("empty directory"));
        maintain(); assertReleased(); assertEquals(AutoLlmCampaign.TrialStatus.FAILED, state.status);
        Path restored = temporary.resolve("restored");
        AutoLlmCheckoutRetirement.restore(campaign, evidence(), restored, this::command);
        assertEquals("working tree", Files.readString(restored.resolve("file.txt")));
        assertEquals("staged", git(restored, "show", ":file.txt"));
        assertTrue(git(restored, "cat-file", "-p", dangling).contains("unreachable commit"));
        assertEquals("ignored source input", Files.readString(restored.resolve("build.input")));
        assertEquals("unique diagnostic", Files.readString(restored.resolve("target/reports/build.xml")));
        assertEquals("unverified partial executable", Files.readString(restored.resolve("target/project.jar")));
        assertTrue(Files.isDirectory(restored.resolve("empty directory")));
        Path exported = temporary.resolve("source-only");
        AutoLlmSourceService.restore(campaign, trial, exported, this::command);
        assertEquals("working tree", Files.readString(exported.resolve("file.txt")));
        assertFalse(Files.exists(exported.resolve("target")));
        maintain(); assertReleased();
    }

    @Test void retainsCheckoutWhenArtifactVerificationFailedOrWorkIsNotClosed() throws Exception {
        prepare();
        state.status = AutoLlmCampaign.TrialStatus.BLOCKED; maintain(); assertFalse(Files.exists(evidence()));
        state.status = AutoLlmCampaign.TrialStatus.FAILED;
        Path job = trial.resolve("attempts/remote/remote-job.json"); AutoLlmFileUtil.write(job, Map.of());
        maintain(); assertFalse(Files.exists(evidence()));
        AutoLlmFileUtil.write(job.resolveSibling("collection-completed.json"), Map.of());
        AutoLlmFileUtil.write(trial.resolve("frozen/executable-fingerprint.json"), Map.of());
        AutoLlmFileUtil.write(trial.resolve("storage/artifact-retention.json"), Map.of("status", "RETAINED"));
        maintain(); assertTrue(Files.exists(source.resolve(".git")));
        assertEquals("RETAINED", AutoLlmFileUtil.read(evidence().resolve("retention.json"), Map.class).get("status"));
        assertTrue(Files.exists(trial.resolve("frozen/source-snapshot.json")), "Failed source must still be preserved");
    }

    @Test void legacyAndCancelledCampaignsAreUntouched() throws Exception {
        prepare();
        AutoLlmCheckoutRetirement.maintainTrial(campaign, trial, state, config, commit, Map.of(), processes, () -> true);
        assertFalse(Files.exists(evidence()));
        Files.delete(campaign.resolve("control/storage-layout.json")); maintain();
        assertFalse(Files.exists(evidence())); assertTrue(Files.exists(source.resolve(".git")));
    }

    @Test void retriesPartialDeletionButRefusesChangedOrCorruptEvidence() throws Exception {
        prepare(); maintain(); assertReleased();
        Path restored = temporary.resolve("restored"); AutoLlmCheckoutRetirement.restore(campaign, evidence(), restored, this::command);
        Files.move(restored, source); Files.delete(source.resolve("file.txt")); maintain(); assertReleased();
        restored = temporary.resolve("changed"); AutoLlmCheckoutRetirement.restore(campaign, evidence(), restored, this::command);
        Files.move(restored, source); Files.writeString(source.resolve("file.txt"), "new evidence after retirement");
        maintain(); assertEquals("new evidence after retirement", Files.readString(source.resolve("file.txt")));
        assertEquals("RETAINED", AutoLlmFileUtil.read(evidence().resolve("retention.json"), Map.class).get("status"));
        Files.writeString(evidence().resolve("extras.zip"), "corrupt archive");
        assertThrows(java.io.IOException.class, () -> AutoLlmCheckoutRetirement.restore(campaign, evidence(), temporary.resolve("corrupt"), this::command));
        assertFalse(Files.exists(temporary.resolve("corrupt")));
    }

    @Test void retiresCampaignBaselinesOnlyAfterAllTrialsAreTerminalAndKeepsUnverifiedJars() throws Exception {
        prepare();
        var model = new AutoLlmCampaign(); model.config = config; model.baselineCommit = commit;
        model.status = AutoLlmCampaign.CampaignStatus.SUCCEEDED; model.trials.add(state);
        var target = new AutoLlmCampaign.TargetState(); target.directory = "targets/local"; model.targets.put("local", target);
        Path baseline = campaign.resolve("targets/local/baseline/source"); Files.createDirectories(baseline.getParent()); Files.move(source, baseline);
        Files.createDirectories(baseline.resolve("target")); Files.writeString(baseline.resolve("target/project.jar"), "retained baseline binary");
        Path replay = campaign.resolve("targets/local/baseline-replay/source");
        GitSourceStore.checkout(campaign.resolve(".store/git"), baseline, commit, replay, temporary.resolve("replay-clone"), this::command);
        Files.createDirectories(replay.resolve("target")); Files.copy(baseline.resolve("target/project.jar"), replay.resolve("target/project.jar"));
        state.status = AutoLlmCampaign.TrialStatus.BLOCKED;
        AutoLlmCheckoutRetirement.maintainCampaign(campaign, model, processes, () -> false); assertTrue(Files.exists(baseline));
        state.status = AutoLlmCampaign.TrialStatus.FAILED;
        AutoLlmCheckoutRetirement.maintainCampaign(campaign, model, processes, () -> false);
        Path evidence = baseline.getParent().resolve("storage/checkout");
        assertEquals("RELEASED", AutoLlmFileUtil.read(evidence.resolve("retention.json"), Map.class).get("status"));
        assertFalse(Files.exists(baseline));
        assertFalse(Files.exists(replay));
        try (var files = Files.list(campaign.resolve(".store/artifacts"))) { assertEquals(1, files.count(), "Identical unverified JARs share one retained asset"); }
        var first = AutoLlmFileUtil.read(evidence.resolve("checkout.json"), AutoLlmCheckoutRetirement.Checkout.class);
        var second = AutoLlmFileUtil.read(replay.getParent().resolve("storage/checkout/checkout.json"), AutoLlmCheckoutRetirement.Checkout.class);
        assertEquals(first.artifacts(), second.artifacts());
        assertFalse(WorkloadArchiveUtil.verify(evidence.resolve("extras.zip"), temporary.resolve("archive-check")).containsKey("target/project.jar"));
        Path restored = temporary.resolve("baseline-restored"); AutoLlmCheckoutRetirement.restore(campaign, evidence, restored, this::command);
        assertEquals("retained baseline binary", Files.readString(restored.resolve("target/project.jar")));
    }

    @Test void preservesSubmoduleLayoutAndDirtyIndex() throws Exception {
        prepare();
        Path child = temporary.resolve("child"); Files.createDirectories(child); Files.writeString(child.resolve("child.txt"), "child baseline");
        git(child, "init"); git(child, "add", "."); git(child, "-c", "user.name=Test", "-c", "user.email=test@example.test", "commit", "-m", "child");
        git(source, "-c", "protocol.file.allow=always", "submodule", "add", child.toString(), "child module");
        git(source, "-c", "user.name=Test", "-c", "user.email=test@example.test", "commit", "-am", "submodule");
        commit = git(source, "rev-parse", "HEAD").strip();
        Path submodule = source.resolve("child module"); Files.writeString(submodule.resolve("child.txt"), "staged child"); git(submodule, "add", "child.txt");
        Files.writeString(submodule.resolve("child.txt"), "working child");
        maintain(); assertReleased();
        Path restored = temporary.resolve("with-submodule"); AutoLlmCheckoutRetirement.restore(campaign, evidence(), restored, this::command);
        assertTrue(Files.isRegularFile(restored.resolve("child module/.git")));
        assertEquals("working child", Files.readString(restored.resolve("child module/child.txt")));
        assertEquals("staged child", git(restored.resolve("child module"), "show", ":child.txt"));
    }
}
