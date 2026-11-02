package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.autoconfig.autollm.execution.*;
import java.io.*;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.util.function.BooleanSupplier;

/** Rebuild verification is maintenance: failure retains the original and never changes experiment outcomes. */
public final class AutoLlmArtifactRebuild {
    private AutoLlmArtifactRebuild() {}
    public record Verification(int version, String originalSha256, String rebuiltSha256, String sourceManifestSha256,
                               String verifiedAt, Map<String,String> runtime, boolean verified) {}

    public static Verification rebuild(Path campaign, Path trial, AutoLlmConfig config, Path work, Path evidence,
                                       AutoLlmProcessRunner processes, BooleanSupplier cancelled) throws IOException {
        if (Files.exists(work, LinkOption.NOFOLLOW_LINKS)) throw new IOException("Rebuild requires a new output directory: " + work);
        var expected = AutoLlmFileUtil.read(trial.resolve("frozen/executable-fingerprint.json"), AutoLlmExecutableFingerprint.class);
        Map<String,String> artifact = AutoLlmFileUtil.read(trial.resolve("frozen/artifact.json"), new tools.jackson.core.type.TypeReference<>() {});
        if (!expected.sha256().equals(artifact.get("sha256"))) throw new IOException("Original executable fingerprint identity changed");
        Files.createDirectories(work.toAbsolutePath().getParent());
        Files.createDirectory(work);
        GitSourceStore.Command command = (args, cwd, logs) -> run(processes, args, cwd, logs, config.execution().buildTimeoutMillis(), cancelled);
        var source = AutoLlmSourceService.restore(campaign, trial, work.resolve("source"), command);
        var build = new ArrayList<>(source.buildCommand());
        if (System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("windows")) {
            if (build.getFirst().equals("./mvnw") || build.getFirst().equals("mvnw")) build.set(0, "mvnw.cmd");
            if (build.getFirst().equals("./gradlew") || build.getFirst().equals("gradlew")) build.set(0, "gradlew.bat");
        }
        run(processes, build, work.resolve("source"), evidence.resolve("build"), config.execution().buildTimeoutMillis(), cancelled);
        Path jar = WorkloadPathUtil.inside(work.resolve("source"), source.artifact());
        var rebuilt = AutoLlmExecutableFingerprint.read(jar);
        AutoLlmFileUtil.write(evidence.resolve("rebuilt-fingerprint.json"), rebuilt);
        if (!expected.matches(rebuilt)) throw new IOException("Rebuilt executable contents differ; retaining the measured executable");
        var args = new ArrayList<>(List.of(Path.of(System.getProperty("java.home"), "bin/java").toString(), "-da", "-Xmx" + config.preparation().localHeap(),
                "-Dmork.execution.directory=" + evidence.resolve("inventory/execution").toAbsolutePath(), "-jar", jar.toAbsolutePath().toString(),
                "--autollm-check", "--autollm-inventory", "--autollm.child.config-file=" + work.toAbsolutePath(),
                "--server.address=127.0.0.1", "--server.port=0", "--event.webserver.stop-on-execution-end=true", "--instances.preload=false"));
        // Older project commits know --autollm-check but not inventory mode. A directory as config-file makes
        // them fail before any probes/measurements, instead of falling back to the default orchestrator.
        String blacklist = artifact.getOrDefault("blacklist", "");
        for (String argument : config.project().runArguments()) {
            if (!blacklist.isBlank() && (argument.startsWith("--blacklist=") || argument.startsWith("--whitelist="))) continue;
            args.add(argument);
        }
        if (!blacklist.isBlank()) args.addAll(List.of("--blacklist=" + blacklist, "--whitelist="));
        run(processes, args, work.resolve("source"), evidence.resolve("inventory"), config.execution().searchSpaceTimeoutMillis(), cancelled);
        for (String name : List.of("components.json", "search-space.json")) {
            Object original = AutoLlmFileUtil.read(trial.resolve("frozen/" + name), Object.class);
            Object fresh = AutoLlmFileUtil.read(evidence.resolve("inventory/execution/" + name), Object.class);
            if (!AutoLlmFileUtil.canonicalHash(original).equals(AutoLlmFileUtil.canonicalHash(fresh))) throw new IOException("Rebuilt " + name + " differs from the measured executable");
        }
        if (cancelled.getAsBoolean()) throw new java.util.concurrent.CancellationException("Rebuild verification cancelled");
        Files.copy(jar, work.resolve("project.jar"));
        var verification = new Verification(1, expected.sha256(), rebuilt.sha256(), source.manifestSha256(), Instant.now().toString(),
                Map.of("java", System.getProperty("java.runtime.version"), "vm", System.getProperty("java.vm.name"), "os", System.getProperty("os.name")), true);
        AutoLlmFileUtil.write(evidence.resolve("verification.json"), verification);
        return verification;
    }
    private static void run(AutoLlmProcessRunner processes, List<String> command, Path cwd, Path logs, long timeout, BooleanSupplier cancelled) {
        if (cancelled.getAsBoolean()) throw new java.util.concurrent.CancellationException("Rebuild cancelled");
        var result = processes.run(command, cwd, logs, timeout, 0, 1000, cancelled, false, 0);
        if (result.exitCode() != 0) {
            String detail = AutoLlmFailureUtil.tail(logs.resolve("stderr.log"), 4000);
            if (detail.isBlank()) detail = AutoLlmFailureUtil.tail(logs.resolve("stdout.log"), 4000);
            throw new IllegalStateException("Rebuild command failed (" + result.exitCode() + "): " + detail);
        }
    }

    /** Caller retains the trial's scheduler ownership until this operation returns. */
    public static void maintain(Path campaign, Path trial, AutoLlmCampaign.Trial state, AutoLlmConfig config,
                                AutoLlmProcessRunner processes, BooleanSupplier cancelled) {
        Path pointer = trial.resolve("frozen/source-snapshot.json"), fingerprint = trial.resolve("frozen/executable-fingerprint.json");
        if (!Files.isRegularFile(pointer) || !Files.isRegularFile(fingerprint) || !state.status.terminal()) return;
        Path status = trial.resolve("storage/artifact-retention.json");
        Path temporary = null;
        var receipt = new LinkedHashMap<String,Object>();
        var released = new ArrayList<String>();
        try {
            if (Files.isRegularFile(status) && "RELEASED".equals(AutoLlmFileUtil.read(status, Map.class).get("status"))) return;
            if (cancelled.getAsBoolean() || AutoLlmCampaignStore.hasUncollectedJobs(trial)) return;
            temporary = Files.createTempDirectory(campaign.toAbsolutePath().getParent(), "artifact-rebuild-");
            Path evidence = trial.resolve("storage/rebuilds/" + UUID.randomUUID());
            receipt.put("evidence", trial.relativize(evidence).toString());
            var verified = rebuild(campaign, trial, config, temporary.resolve("work"), evidence, processes, cancelled);
            if (cancelled.getAsBoolean() || !state.status.terminal() || AutoLlmCampaignStore.hasUncollectedJobs(trial)) return;
            // Persist the proof before deleting anything. Resume can safely repeat a partially completed release.
            receipt.put("status", "VERIFIED"); receipt.put("verification", trial.relativize(evidence.resolve("verification.json")).toString());
            receipt.put("originalSha256", verified.originalSha256()); receipt.put("files", released);
            AutoLlmFileUtil.write(status, receipt);
            var expected = AutoLlmFileUtil.read(fingerprint, AutoLlmExecutableFingerprint.class);
            var source = AutoLlmFileUtil.read(pointer, AutoLlmSourceService.Source.class);
            Path frozen = CampaignAssetStore.artifact(campaign, trial);
            if (Files.exists(frozen) && !AutoLlmFileUtil.hash(frozen).equals(verified.originalSha256())) throw new IOException("Measured executable changed before release");
            var candidates = new ArrayList<Path>();
            if (frozen.toAbsolutePath().normalize().startsWith(trial.toAbsolutePath().normalize())) candidates.add(frozen);
            else {
                CampaignAssetStore.releaseArtifact(campaign, trial);
                receipt.put("releasedAssetReference", campaign.relativize(frozen).toString().replace('\\', '/'));
                AutoLlmFileUtil.write(status, receipt);
            }
            candidates.add(WorkloadPathUtil.inside(trial.resolve("source"), source.artifact()));
            for (Path file : candidates) {
                if (!Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)) continue;
                if (!expected.matches(AutoLlmExecutableFingerprint.read(file))) continue;
                Files.delete(file); released.add(trial.relativize(file).toString());
                AutoLlmFileUtil.write(status, receipt);
            }
            receipt.put("status", "RELEASED");
            AutoLlmFileUtil.write(status, receipt);
        } catch (Exception failure) {
            receipt.put("status", released.isEmpty() && !receipt.containsKey("releasedAssetReference") ? "RETAINED" : "PARTIALLY_RELEASED");
            receipt.put("reason", failure.toString()); receipt.put("observedAt", Instant.now().toString());
            AutoLlmFileUtil.write(status, receipt);
        } finally {
            if (temporary != null) try { WorkloadArchiveUtil.deleteTree(temporary); }
            catch (IOException failure) { AutoLlmFileUtil.write(trial.resolve("storage/cleanup-pending.json"), Map.of("path", temporary.toString(), "reason", failure.toString())); }
        }
    }
}
