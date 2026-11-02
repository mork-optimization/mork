package es.urjc.etsii.grafo.autoconfig.screening;

import es.urjc.etsii.grafo.autoconfig.autollm.*;
import es.urjc.etsii.grafo.autoconfig.autollm.execution.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/** Immutable baseline selection and isolated build attempts, independent of a user's working tree. */
public final class CommittedBaseline {
    private CommittedBaseline() {}
    public record Selection(int version, String commit, AutoLlmConfig.Project project) {}
    public record Attempt(String directory, String commit, String tree, String submodules, Map<String,String> sources) {}
    public record Built(AutoLlmProcessRunner.Result result, String artifactSha256) {}
    public record Receipt(int version, String commit, String tree, String submodules, String sourceSha256,
                          List<String> command, String buildDirectory, Map<String,String> evidence,
                          Object host, String artifactSha256) {}

    public static Attempt prepare(Path root, AutoLlmConfig config, GitSourceStore.Command command) throws IOException {
        Path repository = Path.of(config.project().repository()).toAbsolutePath().normalize();
        Path selectionFile = root.resolve("baseline-selection.json");
        Selection selection;
        if (Files.exists(selectionFile)) {
            selection = AutoLlmFileUtil.read(selectionFile, Selection.class);
            if (selection.version() != 1 || !selection.project().equals(config.project())) throw new IllegalStateException("Baseline settings changed; create a new screening job");
        } else {
            command.run(List.of("git", "rev-parse", "--verify", config.project().ref() + "^{commit}"), repository, root.resolve("ref"));
            selection = new Selection(1, Files.readString(root.resolve("ref/stdout.log")).strip(), config.project());
            AutoLlmFileUtil.write(selectionFile, selection);
        }
        Path active = root.resolve("baseline-attempt.json");
        if (Files.exists(active)) {
            Attempt previous = AutoLlmFileUtil.read(active, Attempt.class);
            Path build = root.resolve(previous.directory()).resolve("build");
            // An uncertain remote result must be collected with its original payload and job identity.
            if (AutoLlmCampaignStore.hasUncollectedJobs(build)) return previous;
            Path result = build.resolve("baseline-result.json");
            if (Files.exists(result)) {
                var built = AutoLlmFileUtil.read(result, Built.class);
                if (built.result().exitCode() == 0) {
                    Path artifact = root.resolve(previous.directory()).resolve("source").resolve(config.project().artifact());
                    if (!Objects.equals(built.artifactSha256(), AutoLlmFileUtil.hash(artifact))) throw new IllegalStateException("Sealed baseline executable changed");
                    return previous;
                }
            }
            // Fully collected but unsealed output cannot establish artifact provenance after a crash.
            // Build a new pristine attempt rather than trust mutable files from that incomplete receipt.

        }
        String directory = "baseline/attempts/" + UUID.randomUUID();
        Path attemptRoot = root.resolve(directory), source = attemptRoot.resolve("source");
        GitSourceStore.checkout(root.resolve(".store/git"), repository, selection.commit(), source, attemptRoot.resolve("clone"), command);
        command.run(List.of("git", "status", "--porcelain", "--untracked-files=all", "--ignored"), source, attemptRoot.resolve("pristine"));
        if (!Files.readString(attemptRoot.resolve("pristine/stdout.log")).isBlank()) throw new IllegalStateException("Committed baseline checkout is not pristine: " + source);
        command.run(List.of("git", "rev-parse", "HEAD^{tree}"), source, attemptRoot.resolve("tree"));
        command.run(List.of("git", "submodule", "status", "--recursive"), source, attemptRoot.resolve("submodules"));
        String submodules = Files.readString(attemptRoot.resolve("submodules/stdout.log"));
        for (String line : submodules.lines().toList()) if (!line.isEmpty() && line.charAt(0) != ' ') throw new IllegalStateException("Unpinned baseline submodule: " + line);
        var attempt = new Attempt(directory, selection.commit(), Files.readString(attemptRoot.resolve("tree/stdout.log")).strip(), submodules, sourceIdentity(source));
        AutoLlmFileUtil.write(active, attempt);
        AutoLlmFileUtil.write(attemptRoot.resolve("inputs.json"), attempt);
        return attempt;
    }

    private static Map<String,String> sourceIdentity(Path source) throws IOException {
        var result = new TreeMap<String,String>();
        Files.walkFileTree(source, new SimpleFileVisitor<>() {
            @Override public FileVisitResult preVisitDirectory(Path directory, java.nio.file.attribute.BasicFileAttributes attributes) {
                return directory.getFileName().toString().equals(".git") ? FileVisitResult.SKIP_SUBTREE : FileVisitResult.CONTINUE;
            }
            @Override public FileVisitResult visitFile(Path file, java.nio.file.attribute.BasicFileAttributes attributes) throws IOException {
                if (!file.getFileName().toString().equals(".git")) result.put(source.relativize(file).toString().replace('\\','/'),
                        attributes.isSymbolicLink() ? "link:" + Files.readSymbolicLink(file) : AutoLlmFileUtil.hash(file));
                return FileVisitResult.CONTINUE;
            }
        });
        return result;
    }
    public static void verifyBeforeSubmission(Path root, Attempt attempt) throws IOException {
        Path directory = root.resolve(attempt.directory()), build = directory.resolve("build");
        if (Files.exists(build.resolve("remote-job.json")) || Files.exists(build.resolve("workload-result.json"))) return;
        if (!attempt.sources().equals(sourceIdentity(directory.resolve("source")))) throw new IllegalStateException("Committed baseline source changed before submission; retry from a fresh attempt");
    }
    public static Receipt completed(Path root, Attempt attempt, AutoLlmConfig config, Path artifact) throws IOException {
        Path directory = root.resolve(attempt.directory()), build = directory.resolve("build");
        if (!Files.isRegularFile(build.resolve("collection-completed.json")) ||
                AutoLlmFileUtil.read(build.resolve("baseline-result.json"), Built.class).result().exitCode() != 0)
            throw new IllegalStateException("Baseline build is not successfully collected");
        var evidence = new TreeMap<String,String>();
        for (Path file : List.of(directory.resolve("inputs.json"), build.resolve("baseline-result.json"), build.resolve("collection-completed.json"), build.resolve("stdout.log"), build.resolve("stderr.log"))) {
            if (Files.isRegularFile(file)) evidence.put(root.relativize(file).toString().replace('\\','/'), AutoLlmFileUtil.hash(file));
        }
        var built = AutoLlmFileUtil.read(build.resolve("baseline-result.json"), Built.class);
        if (!Objects.equals(built.artifactSha256(), AutoLlmFileUtil.hash(artifact))) throw new IllegalStateException("Sealed baseline executable changed");
        Object host = AutoLlmFileUtil.read(root.resolve("target-preflight.json"), Object.class);
        return new Receipt(1, attempt.commit(), attempt.tree(), attempt.submodules(), AutoLlmFileUtil.canonicalHash(attempt.sources()), config.project().buildCommand(),
                root.relativize(build).toString().replace('\\','/'), evidence, host, AutoLlmFileUtil.hash(artifact));
    }
    public static void verify(Path root, RemoteScreeningService.Preparation preparation) {
        Receipt receipt = preparation.baseline();
        if (receipt == null || receipt.version() != 1 || !receipt.commit().equals(preparation.commit()) || !receipt.command().equals(preparation.config().project().buildCommand())
                || !receipt.artifactSha256().equals(preparation.artifactSha256())) throw new IllegalStateException("Missing or inconsistent committed baseline receipt");
        Path build = AutoLlmFileUtil.inside(root, receipt.buildDirectory());
        Path input = build.getParent().resolve("inputs.json");
        for (Path required : List.of(input, build.resolve("baseline-result.json"), build.resolve("collection-completed.json"))) {
            if (!receipt.evidence().containsKey(root.relativize(required).toString().replace('\\','/'))) throw new IllegalStateException("Incomplete baseline receipt");
        }
        var attempt = AutoLlmFileUtil.read(input, Attempt.class);
        if (!receipt.commit().equals(attempt.commit()) || !receipt.tree().equals(attempt.tree()) || !receipt.submodules().equals(attempt.submodules()) ||
                !receipt.sourceSha256().equals(AutoLlmFileUtil.canonicalHash(attempt.sources())) || !root.resolve(attempt.directory()).resolve("build").equals(build) ||
                AutoLlmFileUtil.read(build.resolve("baseline-result.json"), Built.class).result().exitCode() != 0)
            throw new IllegalStateException("Baseline receipt does not match its committed inputs and successful build");
        if (!Objects.equals(receipt.artifactSha256(), AutoLlmFileUtil.read(build.resolve("baseline-result.json"), Built.class).artifactSha256())) throw new IllegalStateException("Baseline result artifact identity changed");
        for (var entry : receipt.evidence().entrySet()) if (!entry.getValue().equals(AutoLlmFileUtil.hash(AutoLlmFileUtil.inside(root, entry.getKey())))) throw new IllegalStateException("Baseline build evidence changed: " + entry.getKey());
        if (!receipt.artifactSha256().equals(AutoLlmFileUtil.hash(AutoLlmFileUtil.inside(root, preparation.artifact())))) throw new IllegalStateException("Baseline artifact changed");
    }
}
