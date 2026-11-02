package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.autoconfig.autollm.execution.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import java.time.Instant;
import java.util.*;
import java.util.function.BooleanSupplier;

/** Retire closed checkouts only after reconstructing their sources, diagnostics and unique Git objects. */
public final class AutoLlmCheckoutRetirement {
    private AutoLlmCheckoutRetirement() {}
    public record FileIdentity(String sha256, String link) {}
    public record GitObject(String type, String path) {}
    public record Repository(String path, String gitDirectory, String commit, Map<String,GitObject> objects) {}
    public record Artifact(String path, String sha256, boolean executable) {}
    public record Checkout(int version, AutoLlmSourceService.Source source, String extrasSha256,
                           List<Repository> repositories, List<String> directories, Map<String,FileIdentity> files,
                           Map<String,Artifact> artifacts) {}

    public static void maintainTrial(Path campaign, Path trial, AutoLlmCampaign.Trial state, AutoLlmConfig config,
                                     String commit, Map<String,String> versions, AutoLlmProcessRunner processes, BooleanSupplier cancelled) {
        if (!eligible(campaign, trial, state.status.terminal(), cancelled)) return;
        Path source = trial.resolve("source"), evidence = trial.resolve("storage/checkout");
        try {
            // Capture failed preparation sources too, even when no executable could be frozen or rebuilt.
            if (Files.isDirectory(source) && !Files.exists(trial.resolve("frozen/source-snapshot.json"))) {
                AutoLlmSourceService.freeze(campaign, trial, source, evidence.resolve("failed-source"), commit, config, versions, command(processes, config, cancelled));
            }
            if (Files.exists(trial.resolve("frozen/executable-fingerprint.json"))) {
                Path retained = trial.resolve("storage/artifact-retention.json");
                if (!Files.isRegularFile(retained) || !"RELEASED".equals(AutoLlmFileUtil.read(retained, Map.class).get("status"))) {
                    retained(evidence, "Executable rebuild verification has not completed successfully"); return;
                }
            }
            retire(campaign, source, evidence, commit, config, versions, processes, cancelled);
            if (!cancelled.getAsBoolean()) DirectoryArchiveUtil.archive(trial.resolve("baseline/source"));
        } catch (Exception failure) { retained(evidence, failure.toString()); }
    }

    /** Run only after the coordinator has finished all campaign jobs, before releasing campaign ownership. */
    public static void maintainCampaign(Path root, AutoLlmCampaign campaign, AutoLlmProcessRunner processes, BooleanSupplier cancelled) {
        if (!campaign.status.completed() || !eligible(root, root, true, cancelled)) return;
        for (var trial : campaign.trials) if (!trial.status.terminal()) return;
        var targets = new ArrayList<>(campaign.targets.values());
        if (campaign.localPreparation != null) targets.add(campaign.localPreparation);
        for (var target : targets) {
            if (target.directory == null) continue;
            var config = target.resolvedConfig == null ? campaign.config : target.resolvedConfig;
            for (String name : List.of("baseline", "baseline-replay")) {
                Path source = WorkloadPathUtil.inside(root, target.directory).resolve(name + "/source");
                Path evidence = source.getParent().resolve("storage/checkout");
                try { retire(root, source, evidence, campaign.baselineCommit, config, target.versions, processes, cancelled); }
                catch (Exception failure) { retained(evidence, failure.toString()); }
            }
        }
    }

    private static boolean eligible(Path campaign, Path owner, boolean terminal, BooleanSupplier cancelled) {
        return terminal && Files.isRegularFile(campaign.resolve("control/storage-layout.json"))
                && !cancelled.getAsBoolean() && !AutoLlmCampaignStore.hasUncollectedJobs(owner);
    }
    private static void retained(Path evidence, String reason) {
        AutoLlmFileUtil.write(evidence.resolve("retention.json"), Map.of("status", "RETAINED", "reason", reason, "observedAt", Instant.now().toString()));
    }
    private static GitSourceStore.Command command(AutoLlmProcessRunner processes, AutoLlmConfig config, BooleanSupplier cancelled) {
        return (args, cwd, logs) -> {
            if (cancelled.getAsBoolean()) throw new java.util.concurrent.CancellationException("Checkout retirement cancelled");
            var result = processes.run(args, cwd, logs, config.execution().buildTimeoutMillis(), 0, 1000, cancelled, false, 0);
            if (result.exitCode() != 0) throw new IllegalStateException("Checkout preservation command failed: " + AutoLlmFailureUtil.tail(logs.resolve("stderr.log"), 4000));
        };
    }

    private static void retire(Path campaign, Path checkout, Path evidence, String commit, AutoLlmConfig config,
                               Map<String,String> versions, AutoLlmProcessRunner processes, BooleanSupplier cancelled) throws IOException {
        if (!Files.isDirectory(checkout, LinkOption.NOFOLLOW_LINKS) || cancelled.getAsBoolean()) return;
        Path source = checkout.toRealPath();
        if (!source.startsWith(campaign.toRealPath())) throw new IOException("Checkout is outside the campaign");
        var command = command(processes, config, cancelled);
        Path descriptor = evidence.resolve("checkout.json");
        Checkout record;
        if (Files.isRegularFile(descriptor)) record = AutoLlmFileUtil.read(descriptor, Checkout.class);
        else {
            var snapshot = AutoLlmSourceService.capture(campaign, source, evidence.resolve("sources"), commit, config, versions, command);
            var repositories = new ArrayList<Repository>();
            repositories(campaign, source, source, WorkloadPathUtil.inside(campaign, snapshot.snapshot()), evidence.resolve("git"), repositories, command);
            var files = files(source, repositories);
            var snapshotFiles = new HashSet<String>(); snapshotFiles(WorkloadPathUtil.inside(campaign, snapshot.snapshot()), "", snapshotFiles);
            var extras = new TreeMap<String,Path>();
            var artifacts = new TreeMap<String,Artifact>();
            for (var file : files.entrySet()) if (!snapshotFiles.contains(file.getKey())) {
                Path path = WorkloadPathUtil.inside(source, file.getKey());
                if (file.getValue().link() == null && file.getKey().toLowerCase(Locale.ROOT).endsWith(".jar")) {
                    Path shared = CampaignAssetStore.putArtifact(campaign, evidence, path);
                    artifacts.put(file.getKey(), new Artifact(relative(campaign, shared), file.getValue().sha256(), Files.isExecutable(path)));
                } else extras.put(file.getKey(), path);
            }
            Path archive = evidence.resolve("extras.zip");
            WorkloadArchiveUtil.pack(archive, extras); WorkloadArchiveUtil.verify(archive, source);
            record = new Checkout(1, snapshot, AutoLlmFileUtil.hash(archive), repositories, directories(source, repositories), files, artifacts);
            AutoLlmFileUtil.write(descriptor, record);
        }
        // Restoration is repeated on retries, so interrupted deletion never bypasses archive verification.
        Path temporary = Files.createTempDirectory(campaign.toAbsolutePath().getParent(), "checkout-verification-");
        try {
            Path restored = temporary.resolve("source"); restore(campaign, evidence, restored, command);
            if (!record.files().equals(files(restored, record.repositories()))) throw new IOException("Restored checkout differs from its retained manifest");
            for (var item : files(source, record.repositories()).entrySet()) {
                if (!Objects.equals(record.files().get(item.getKey()), item.getValue())) throw new IOException("Checkout changed after preservation: " + item.getKey());
            }
            verifyRemainingObjects(source, record.repositories(), evidence.resolve("git-recheck"), command);
            if (cancelled.getAsBoolean()) return;
            AutoLlmFileUtil.write(evidence.resolve("retention.json"), Map.of("status", "VERIFIED", "manifestSha256", AutoLlmFileUtil.hash(descriptor)));
            WorkloadArchiveUtil.deleteTree(source);
            AutoLlmFileUtil.write(evidence.resolve("retention.json"), Map.of("status", "RELEASED", "manifestSha256", AutoLlmFileUtil.hash(descriptor), "observedAt", Instant.now().toString()));
        } finally { WorkloadArchiveUtil.deleteTree(temporary); }
    }

    /** Restore the complete retired checkout, including diagnostics and original index/refs, into a new directory. */
    public static void restore(Path campaign, Path evidence, Path destination, GitSourceStore.Command command) throws IOException {
        var record = AutoLlmFileUtil.read(evidence.resolve("checkout.json"), Checkout.class);
        if (record.version() != 1 || !AutoLlmFileUtil.hash(evidence.resolve("extras.zip")).equals(record.extrasSha256())) throw new IOException("Retired checkout archive changed");
        AutoLlmSourceService.restore(campaign, record.source(), destination, command);
        // Snapshot restoration creates independent submodule repositories. Recreate the original owned layout
        // before restoring Git metadata, so its .git pointer files and index keep their original meaning.
        for (var repository : record.repositories()) {
            Path generated = WorkloadPathUtil.inside(destination, repository.path()).resolve(".git");
            Path original = WorkloadPathUtil.inside(destination, repository.gitDirectory());
            if (!generated.equals(original)) { Files.createDirectories(original.getParent()); Files.move(generated, original); }
        }
        var metadata = new HashSet<Path>();
        for (var repository : record.repositories()) {
            Path git = WorkloadPathUtil.inside(destination, repository.gitDirectory());
            Files.walkFileTree(git, new SimpleFileVisitor<>() {
                @Override public FileVisitResult preVisitDirectory(Path path, java.nio.file.attribute.BasicFileAttributes attributes) {
                    return objectsDirectory(destination, path, record.repositories()) ? FileVisitResult.SKIP_SUBTREE : FileVisitResult.CONTINUE;
                }
                @Override public FileVisitResult visitFile(Path path, java.nio.file.attribute.BasicFileAttributes attributes) { metadata.add(path); return FileVisitResult.CONTINUE; }
            });
        }
        for (Path file : metadata) Files.delete(file);
        for (String directory : record.directories()) Files.createDirectories(WorkloadPathUtil.inside(destination, directory));
        WorkloadArchiveUtil.unpack(evidence.resolve("extras.zip"), destination);
        for (var artifact : record.artifacts().entrySet()) {
            Path retained = WorkloadPathUtil.inside(campaign, artifact.getValue().path());
            if (!AutoLlmFileUtil.hash(retained).equals(artifact.getValue().sha256())) throw new IOException("Retained checkout executable changed: " + artifact.getKey());
            Path file = WorkloadPathUtil.inside(destination, artifact.getKey()); Files.createDirectories(file.getParent());
            Files.copy(retained, file);
            if (Files.isExecutable(file) != artifact.getValue().executable()
                    && !file.toFile().setExecutable(artifact.getValue().executable(), false)) throw new IOException("Cannot restore executable permission: " + file);
        }
        for (var repository : record.repositories()) {
            Path cwd = WorkloadPathUtil.inside(destination, repository.path()), git = WorkloadPathUtil.inside(destination, repository.gitDirectory());
            for (var object : repository.objects().entrySet()) {
                Path stored = WorkloadPathUtil.inside(campaign, object.getValue().path());
                verifyObject(stored, object.getValue().type(), object.getKey());
                Path log = evidence.resolve("restore-objects/" + UUID.randomUUID());
                command.run(List.of("git", "--git-dir=" + git.toAbsolutePath(), "hash-object", "-t", object.getValue().type(), "-w", stored.toAbsolutePath().toString()), cwd, log);
                if (!Files.readString(log.resolve("stdout.log")).strip().equals(object.getKey())) throw new IOException("Restored Git object changed");
            }
        }
    }

    private static void repositories(Path campaign, Path root, Path source, Path snapshot, Path logs, List<Repository> result, GitSourceStore.Command command) throws IOException {
        var manifest = AutoLlmFileUtil.read(snapshot.resolve("snapshot.json"), GitPatchSnapshot.Manifest.class);
        command.run(List.of("git", "rev-parse", "--absolute-git-dir"), source, logs.resolve("directory"));
        Path directory = Path.of(Files.readString(logs.resolve("directory/stdout.log")).strip()).toRealPath();
        if (!directory.startsWith(root) || Files.exists(directory.resolve("objects/info/alternates"))) throw new IOException("Checkout uses external Git objects; retaining it");
        var baseline = baselineObjects(source, manifest.commit(), logs.resolve("baseline"), command);
        var objects = objects(source, logs.resolve("objects"), command);
        var saved = new TreeMap<String,GitObject>();
        for (var entry : objects.entrySet()) if (!baseline.contains(entry.getKey())) {
            Path file = campaign.resolve(".store/git-objects/" + entry.getKey());
            if (!Files.isRegularFile(file)) {
                Path objectLogs = logs.resolve("export/" + entry.getKey());
                command.run(List.of("git", "cat-file", entry.getValue(), entry.getKey()), source, objectLogs);
                Path bytes = objectLogs.resolve("stdout.log"); verifyObject(bytes, entry.getValue(), entry.getKey());
                Files.createDirectories(file.getParent());
                try { Files.move(bytes, file); } catch (FileAlreadyExistsException ignored) { /* Another completed checkout preserved it. */ }
            }
            verifyObject(file, entry.getValue(), entry.getKey());
            saved.put(entry.getKey(), new GitObject(entry.getValue(), campaign.relativize(file).toString().replace('\\', '/')));
        }
        result.add(new Repository(relative(root, source), relative(root, directory), manifest.commit(), saved));
        for (var child : manifest.submodules().entrySet()) repositories(campaign, root, WorkloadPathUtil.inside(source, child.getKey()),
                WorkloadPathUtil.inside(snapshot, child.getValue().snapshot()), logs.resolve("submodule-" + result.size()), result, command);
    }
    private static Set<String> baselineObjects(Path source, String commit, Path logs, GitSourceStore.Command command) throws IOException {
        command.run(List.of("git", "rev-list", "--objects", commit), source, logs);
        var result = new HashSet<String>();
        for (String line : Files.readAllLines(logs.resolve("stdout.log"))) if (!line.isBlank()) result.add(line.split(" ", 2)[0]);
        return result;
    }
    private static Map<String,String> objects(Path source, Path logs, GitSourceStore.Command command) throws IOException {
        command.run(List.of("git", "cat-file", "--batch-all-objects", "--batch-check=%(objectname) %(objecttype)"), source, logs);
        var result = new TreeMap<String,String>();
        for (String line : Files.readAllLines(logs.resolve("stdout.log"))) if (!line.isBlank()) {
            var fields = line.split(" ");
            if (fields.length != 2 || !fields[0].matches("[a-f0-9]{40,64}") || !Set.of("blob", "tree", "commit", "tag").contains(fields[1])) throw new IOException("Invalid Git object inventory");
            result.put(fields[0], fields[1]);
        }
        return result;
    }
    private static void verifyRemainingObjects(Path source, List<Repository> repositories, Path logs, GitSourceStore.Command command) throws IOException {
        for (var repository : repositories) {
            Path cwd = WorkloadPathUtil.inside(source, repository.path());
            if (!Files.exists(cwd.resolve(".git"))) continue;
            Path output = logs.resolve(UUID.randomUUID().toString());
            var baseline = baselineObjects(cwd, repository.commit(), output.resolve("baseline"), command);
            for (var object : objects(cwd, output.resolve("objects"), command).keySet()) {
                if (!baseline.contains(object) && !repository.objects().containsKey(object)) throw new IOException("New Git object appeared after preservation: " + object);
            }
        }
    }
    private static void verifyObject(Path file, String type, String expected) throws IOException {
        try {
            var digest = MessageDigest.getInstance(expected.length() == 40 ? "SHA-1" : "SHA-256");
            digest.update((type + " " + Files.size(file) + "\0").getBytes(StandardCharsets.UTF_8));
            try (var input = Files.newInputStream(file)) {
                byte[] bytes = new byte[65536]; for (int size; (size = input.read(bytes)) != -1;) digest.update(bytes, 0, size);
            }
            if (!HexFormat.of().formatHex(digest.digest()).equals(expected)) throw new IOException("Retained Git object changed: " + expected);
        } catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
    private static void snapshotFiles(Path snapshot, String prefix, Set<String> files) {
        var manifest = AutoLlmFileUtil.read(snapshot.resolve("snapshot.json"), GitPatchSnapshot.Manifest.class);
        for (String name : manifest.files().keySet()) files.add(prefix + name);
        for (var child : manifest.submodules().entrySet()) snapshotFiles(WorkloadPathUtil.inside(snapshot, child.getValue().snapshot()), prefix + child.getKey() + "/", files);
    }
    private static boolean objectsDirectory(Path root, Path path, List<Repository> repositories) {
        for (var repository : repositories) if (path.startsWith(WorkloadPathUtil.inside(root, repository.gitDirectory()).resolve("objects"))) return true;
        return false;
    }
    private static Map<String,FileIdentity> files(Path root, List<Repository> repositories) throws IOException {
        var result = new TreeMap<String,FileIdentity>();
        Files.walkFileTree(root, new SimpleFileVisitor<>() {
            @Override public FileVisitResult preVisitDirectory(Path path, java.nio.file.attribute.BasicFileAttributes attributes) { return objectsDirectory(root, path, repositories) ? FileVisitResult.SKIP_SUBTREE : FileVisitResult.CONTINUE; }
            @Override public FileVisitResult visitFile(Path path, java.nio.file.attribute.BasicFileAttributes attributes) throws IOException {
                if (!attributes.isRegularFile() && !attributes.isSymbolicLink()) throw new IOException("Unsupported checkout file: " + path);
                String link = attributes.isSymbolicLink() ? Files.readSymbolicLink(path).toString() : null;
                result.put(relative(root, path), new FileIdentity(link == null ? AutoLlmFileUtil.hash(path) : null, link)); return FileVisitResult.CONTINUE;
            }
        });
        return result;
    }
    private static List<String> directories(Path root, List<Repository> repositories) throws IOException {
        var result = new ArrayList<String>();
        Files.walkFileTree(root, new SimpleFileVisitor<>() {
            @Override public FileVisitResult preVisitDirectory(Path path, java.nio.file.attribute.BasicFileAttributes attributes) {
                if (objectsDirectory(root, path, repositories)) return FileVisitResult.SKIP_SUBTREE;
                if (!path.equals(root)) result.add(relative(root, path)); return FileVisitResult.CONTINUE;
            }
        });
        return result;
    }
    private static String relative(Path root, Path path) { return root.relativize(path).toString().replace('\\', '/'); }
}
