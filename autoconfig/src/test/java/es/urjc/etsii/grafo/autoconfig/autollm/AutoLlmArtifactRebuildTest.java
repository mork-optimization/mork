package es.urjc.etsii.grafo.autoconfig.autollm;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import java.util.zip.*;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;

class AutoLlmArtifactRebuildTest {
    @TempDir Path root;
    private final AutoLlmProcessRunner git = new AutoLlmProcessRunner();
    private Path campaign, trial, source;
    private AutoLlmConfig config;
    private final AutoLlmCampaign.Trial state = new AutoLlmCampaign.Trial();
    private final AtomicBoolean cancelled = new AtomicBoolean();
    private String failure = "";
    private int builds;
    private final AutoLlmProcessRunner processes = new AutoLlmProcessRunner() {
        @Override public Result run(List<String> args, Path cwd, Path logs, long timeout, long planned, long grace, BooleanSupplier cancel, boolean watch, long startup) {
            if (args.getFirst().equals("git")) return git.run(args, cwd, logs, timeout, planned, grace, cancel, watch, startup);
            try {
                if (args.equals(config.project().buildCommand()) || args.getFirst().equals("mvnw.cmd")) {
                    builds++;
                    if (failure.equals("build")) return new Result(1, false, 0);
                    Files.createDirectories(cwd.resolve("target"));
                    Files.write(cwd.resolve(config.project().artifact()), jar(failure.equals("contents") ? "changed" : Files.readString(cwd.resolve("source.txt")), "second build"));
                    if (failure.equals("cancel")) cancelled.set(true);
                } else {
                    assertTrue(args.contains("--autollm-inventory"));
                    assertTrue(args.contains("--autollm-check"));
                    String configArgument = "--autollm.child.config-file=" + cwd.getParent().toAbsolutePath();
                    assertTrue(args.contains(configArgument));
                    assertTrue(Files.isDirectory(cwd.getParent()), "Older check modes must fail to read a configuration");
                    if (failure.equals("old-mode")) return new Result(1, false, 0);
                    AutoLlmFileUtil.write(logs.resolve("execution/components.json"), List.of("ComponentNew"));
                    AutoLlmFileUtil.write(logs.resolve("execution/search-space.json"), Map.of("parameters", failure.equals("metadata") ? 3 : 2));
                }
                return new Result(0, false, 0);
            } catch (IOException e) { throw new UncheckedIOException(e); }
        }
    };

    private void prepare() throws Exception {
        campaign = root.resolve("campaign"); trial = campaign.resolve("trials/001"); source = trial.resolve("source");
        Files.createDirectories(source);
        config = AutoLlmConfig.defaults();
        Files.writeString(source.resolve("source.txt"), "compiled application");
        command(List.of("git", "init"), source, root.resolve("init"));
        command(List.of("git", "add", "."), source, root.resolve("add"));
        command(List.of("git", "-c", "user.name=Test", "-c", "user.email=test@example.test", "commit", "-m", "baseline"), source, root.resolve("commit"));
        command(List.of("git", "rev-parse", "HEAD"), source, root.resolve("head"));
        String commit = Files.readString(root.resolve("head/stdout.log")).strip();
        Path original = trial.resolve("frozen/project.jar"); Files.createDirectories(original.getParent());
        Files.write(original, jar("compiled application", "original build"));
        Files.createDirectories(source.resolve("target")); Files.copy(original, source.resolve(config.project().artifact()));
        AutoLlmSourceService.freeze(campaign, trial, source, trial.resolve("attempts/preparation"), commit, config, Map.of(), this::command);
        AutoLlmFileUtil.write(trial.resolve("frozen/artifact.json"), Map.of("sha256", AutoLlmFileUtil.hash(original)));
        AutoLlmFileUtil.write(trial.resolve("frozen/executable-fingerprint.json"), AutoLlmExecutableFingerprint.read(original));
        AutoLlmFileUtil.write(trial.resolve("frozen/components.json"), List.of("ComponentNew"));
        AutoLlmFileUtil.write(trial.resolve("frozen/search-space.json"), Map.of("parameters", 2));
        state.status = AutoLlmCampaign.TrialStatus.SUCCEEDED;
    }
    private void command(List<String> args, Path cwd, Path logs) {
        assertEquals(0, git.run(args, cwd, logs, 10000, 0, 100, () -> false, false, 0).exitCode(), args.toString());
    }
    private void maintain() { AutoLlmArtifactRebuild.maintain(campaign, trial, state, config, processes, cancelled::get); }

    @Test void releasesOnlyVerifiedCopiesAndCanRebuildAgainAfterRelocation() throws Exception {
        prepare();
        Files.writeString(source.resolve("target/other-evidence.txt"), "keep");
        maintain();
        assertFalse(Files.exists(trial.resolve("frozen/project.jar")));
        assertFalse(Files.exists(source.resolve(config.project().artifact())));
        assertTrue(Files.exists(source.resolve("target/other-evidence.txt")));
        var receipt = AutoLlmFileUtil.read(trial.resolve("storage/artifact-retention.json"), Map.class);
        assertEquals("RELEASED", receipt.get("status"), receipt.toString());
        assertEquals(2, ((List<?>) receipt.get("files")).size());
        assertTrue(Files.exists(trial.resolve((String) receipt.get("verification"))));
        assertEquals("RELEASED", ((Map<?,?>) AutoLlmStorageReport.inspect(campaign).artifactRetention().get(Path.of("trials", "001").toString())).get("status"));
        maintain(); assertEquals(1, builds, "Release is idempotent");
        Path moved = root.resolve("relocated campaign"); Files.move(campaign, moved); campaign = moved; trial = moved.resolve("trials/001");
        Path exported = root.resolve("export");
        var verification = AutoLlmArtifactRebuild.rebuild(campaign, trial, config, exported, exported.resolve("verification"), processes, () -> false);
        assertTrue(verification.verified()); assertTrue(Files.isRegularFile(exported.resolve("project.jar")));
        assertNotEquals(verification.originalSha256(), verification.rebuiltSha256(), "ZIP/build metadata may differ while executable contents match");
        assertThrows(IOException.class, () -> AutoLlmArtifactRebuild.rebuild(campaign, trial, config, exported, exported.resolve("verification"), processes, () -> false));
        try (var files = Files.list(root)) { assertFalse(files.anyMatch(p -> p.getFileName().toString().startsWith("artifact-rebuild-"))); }
    }

    @ParameterizedTest @ValueSource(strings = {"build", "contents", "metadata", "cancel", "snapshot", "original", "old-mode"})
    void failedVerificationRetainsOriginalsAndTrialOutcome(String mode) throws Exception {
        prepare(); failure = mode;
        if (mode.equals("snapshot")) {
            var pointer = AutoLlmFileUtil.read(trial.resolve("frozen/source-snapshot.json"), AutoLlmSourceService.Source.class);
            Files.writeString(campaign.resolve(pointer.snapshot()).resolve("snapshot.json"), "corrupt");
        }
        if (mode.equals("original")) Files.write(trial.resolve("frozen/project.jar"), jar("tampered", "metadata"));
        maintain();
        assertTrue(Files.exists(trial.resolve("frozen/project.jar")));
        assertTrue(Files.exists(source.resolve(config.project().artifact())));
        assertEquals(AutoLlmCampaign.TrialStatus.SUCCEEDED, state.status);
        assertEquals("RETAINED", AutoLlmFileUtil.read(trial.resolve("storage/artifact-retention.json"), Map.class).get("status"));
    }

    @Test void unfinishedWorkAndCancellationPreventRelease() throws Exception {
        prepare();
        state.status = AutoLlmCampaign.TrialStatus.BLOCKED; maintain(); assertEquals(0, builds);
        state.status = AutoLlmCampaign.TrialStatus.SUCCEEDED;
        cancelled.set(true); maintain(); assertEquals(0, builds); cancelled.set(false);
        Path job = trial.resolve("attempts/remote/remote-job.json"); AutoLlmFileUtil.write(job, Map.of());
        maintain(); assertEquals(0, builds); assertTrue(Files.exists(trial.resolve("frozen/project.jar")));
        AutoLlmFileUtil.write(job.resolveSibling("collection-completed.json"), Map.of());
        maintain(); assertEquals(1, builds); assertFalse(Files.exists(trial.resolve("frozen/project.jar")));
    }

    @Test void fingerprintChecksApplicationDependenciesAndResourcesButIgnoresZipLayout() throws Exception {
        var entries = new LinkedHashMap<String,byte[]>();
        entries.put("BOOT-INF/classes/App.class", "code".getBytes(UTF_8));
        entries.put("META-INF/MANIFEST.MF", "Main-Class: App\n".getBytes(UTF_8));
        entries.put("BOOT-INF/classes/settings.yml", "resource".getBytes(UTF_8));
        entries.put("BOOT-INF/lib/dependency.jar", zip(Map.of("Dependency.class", "library".getBytes(UTF_8)), 0));
        Path first = root.resolve("first.jar"), second = root.resolve("second.jar");
        Files.write(first, zip(entries, 0)); var expected = AutoLlmExecutableFingerprint.read(first);
        var reversed = new LinkedHashMap<String,byte[]>();
        for (String name : new ArrayList<>(entries.keySet()).reversed()) reversed.put(name, entries.get(name));
        Files.write(second, zip(reversed, 2000000000000L));
        assertTrue(expected.matches(AutoLlmExecutableFingerprint.read(second)));
        for (String name : entries.keySet()) {
            var changed = new LinkedHashMap<>(entries);
            changed.put(name, name.endsWith(".jar") ? zip(Map.of("Dependency.class", "changed".getBytes(UTF_8)), 0) : "changed".getBytes(UTF_8));
            Files.write(second, zip(changed, 0));
            assertFalse(expected.matches(AutoLlmExecutableFingerprint.read(second)), name);
        }
        Files.write(second, zip(Map.of("invalid.jar", "not a jar".getBytes(UTF_8)), 0));
        assertThrows(IOException.class, () -> AutoLlmExecutableFingerprint.read(second));
    }
    @Test void ignoresOnlyRecognizedBuildMetadataAndChecksNestedEntryContents() throws Exception {
        Path first = root.resolve("first.jar"), second = root.resolve("second.jar");
        var dependency = new LinkedHashMap<String,byte[]>();
        dependency.put("Library.class", "code".getBytes(UTF_8)); dependency.put("builddef.lst", "original path".getBytes(UTF_8));
        var reordered = new LinkedHashMap<String,byte[]>();
        reordered.put("builddef.lst", "relocated path".getBytes(UTF_8)); reordered.put("Library.class", "code".getBytes(UTF_8));
        for (String name : List.of("mork-1.jar", "mork-common-1.jar", "other-1.jar")) {
            Files.write(first, zip(Map.of("BOOT-INF/lib/" + name, zip(dependency, 0)), 0));
            Files.write(second, zip(Map.of("BOOT-INF/lib/" + name, zip(reordered, 2000000000000L)), 0));
            var original = AutoLlmExecutableFingerprint.read(first); var rebuilt = AutoLlmExecutableFingerprint.read(second);
            assertEquals(name.startsWith("mork-"), original.matches(rebuilt), name);
            if (name.startsWith("mork-")) assertNotEquals(original.buildMetadata(), rebuilt.buildMetadata());
        }
    }
    private static byte[] jar(String code, String metadata) throws IOException {
        return zip(Map.of("BOOT-INF/classes/App.class", code.getBytes(UTF_8), "BOOT-INF/classes/builddef.lst", metadata.getBytes(UTF_8)), 0);
    }
    private static byte[] zip(Map<String,byte[]> entries, long time) throws IOException {
        var buffer = new ByteArrayOutputStream();
        try (var output = new ZipOutputStream(buffer)) {
            for (var item : entries.entrySet()) {
                var entry = new ZipEntry(item.getKey()); entry.setTime(time); output.putNextEntry(entry); output.write(item.getValue()); output.closeEntry();
            }
        }
        return buffer.toByteArray();
    }
}
