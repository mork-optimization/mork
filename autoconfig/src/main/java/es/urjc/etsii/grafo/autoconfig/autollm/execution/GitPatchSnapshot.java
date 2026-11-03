package es.urjc.etsii.grafo.autoconfig.autollm.execution;

import es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmFileUtil;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.*;
import java.util.*;

/** Portable commit-plus-patch snapshots, including recursive submodules and raw worktree bytes. */
public final class GitPatchSnapshot {
    private GitPatchSnapshot() {}
    private static final Set<String> CACHES = Set.of(".git", "target", "node_modules", ".codex", ".mork-metadata", ".DS_Store");
    public record FileIdentity(String mode, String object, String sha256, String link, boolean raw) {}
    public record Submodule(String snapshot, String commit, String manifestSha256) {}
    public record Manifest(int version, String commit, String tree, String bundleSha256, String patchSha256,
                           Map<String,FileIdentity> files, Map<String,Submodule> submodules) {}

    public static Manifest capture(Path bundle, String commit, Path source, Path output, GitSourceStore.Command command) throws IOException {
        if (Files.exists(output)) throw new IOException("Snapshot destination already exists: " + output);
        Files.createDirectories(output.toAbsolutePath().getParent());
        Path temporary = Files.createTempDirectory(output.toAbsolutePath().getParent(), "source-snapshot-");
        try {
            var manifest = captureTree(bundle, commit, source.toAbsolutePath(), temporary, command);
            Files.move(temporary, output);
            return manifest;
        } finally { WorkloadArchiveUtil.deleteTree(temporary); }
    }

    private static Manifest captureTree(Path bundle, String commit, Path source, Path output, GitSourceStore.Command command) throws IOException {
        Files.createDirectories(output);
        Path scratch = Files.createTempDirectory(output, "patch-index-");
        Path logs = output.resolve("capture");
        try {
            command.run(List.of("git", "init", "--bare", scratch.toString()), output, logs.resolve("init"));
            command.run(List.of("git", "fetch", "--no-tags", bundle.toAbsolutePath().toString(), "refs/heads/baseline"), scratch, logs.resolve("fetch"));
            var prefix = List.of("git", "--git-dir=" + scratch.toAbsolutePath(), "--work-tree=" + source, "-c", "core.autocrlf=false", "-c", "filter.lfs.clean=", "-c", "filter.lfs.smudge=", "-c", "filter.lfs.required=false");
            run(prefix, List.of("read-tree", commit), source, logs.resolve("read"), command);
            // Force-add ignored inputs as well, but never cache trees or the checkout's Git metadata.
            var paths = new ByteArrayOutputStream();
            Files.walkFileTree(source, new SimpleFileVisitor<>() {
                private void include(Path path) throws IOException {
                    paths.write(source.relativize(path).toString().replace('\\', '/').getBytes(StandardCharsets.UTF_8)); paths.write(0);
                }
                @Override public FileVisitResult preVisitDirectory(Path path, java.nio.file.attribute.BasicFileAttributes attributes) throws IOException {
                    if (path.equals(source)) return FileVisitResult.CONTINUE;
                    if (CACHES.contains(path.getFileName().toString())) return FileVisitResult.SKIP_SUBTREE;
                    if (Files.exists(path.resolve(".git"))) { include(path); return FileVisitResult.SKIP_SUBTREE; }
                    return FileVisitResult.CONTINUE;
                }
                @Override public FileVisitResult visitFile(Path path, java.nio.file.attribute.BasicFileAttributes attributes) throws IOException {
                    if (!CACHES.contains(path.getFileName().toString()) && (attributes.isRegularFile() || attributes.isSymbolicLink())) include(path);
                    return FileVisitResult.CONTINUE;
                }
            });
            // Update tracked deletions and gitlinks first; new files are added explicitly below.
            run(prefix, List.of("add", "-u", "--", "."), source, logs.resolve("update"), command);
            Path list = scratch.resolve("paths"); Files.write(list, paths.toByteArray());
            if (paths.size() > 0) run(prefix, List.of("--literal-pathspecs", "add", "-f", "--pathspec-from-file=" + list, "--pathspec-file-nul"), source, logs.resolve("add"), command);
            run(prefix, List.of("ls-files", "--stage", "-z"), source, logs.resolve("files"), command);
            var files = new TreeMap<String,FileIdentity>(); var submodules = new TreeMap<String,Submodule>();
            for (String entry : Files.readString(logs.resolve("files/stdout.log")).split("\0")) {
                if (entry.isEmpty()) continue;
                int tab = entry.indexOf('\t'); String[] fields = entry.substring(0, tab).split(" ");
                String mode = fields[0], object = fields[1], name = entry.substring(tab + 1);
                Path file = WorkloadPathUtil.inside(source, name);
                if (mode.equals("160000")) {
                    if (!Files.exists(file.resolve(".git"))) throw new IOException("Submodule is not initialized: " + name);
                    Path childBundle = GitSourceStore.bundle(bundle.toAbsolutePath().getParent(), file, object, logs.resolve("submodule-" + submodules.size()), command);
                    String child = "submodules/" + submodules.size();
                    captureTree(childBundle, object, file, output.resolve(child), command);
                    submodules.put(name, new Submodule(child, object, AutoLlmFileUtil.hash(output.resolve(child).resolve("snapshot.json"))));
                } else {
                    boolean link = mode.equals("120000");
                    if (link != Files.isSymbolicLink(file)) throw new IOException("Unsupported source file mode: " + name);
                    String target = link ? Files.readSymbolicLink(file).toString() : null;
                    if (link) checkLink(source, file, target);
                    String sha = link ? AutoLlmFileUtil.hash(new ByteArrayInputStream(target.getBytes(StandardCharsets.UTF_8))) : AutoLlmFileUtil.hash(file);
                    boolean raw = !link && !blobHash(file, object.length()).equals(object);
                    if (raw) { Path saved = output.resolve("raw/" + sha); Files.createDirectories(saved.getParent()); if (!Files.exists(saved)) Files.copy(file, saved); }
                    files.put(name, new FileIdentity(mode, object, sha, target, raw));
                }
            }
            run(prefix, List.of("write-tree"), source, logs.resolve("tree"), command);
            String tree = Files.readString(logs.resolve("tree/stdout.log")).strip();
            Path patch = output.resolve("source.patch");
            run(prefix, List.of("diff", "--cached", "--binary", "--full-index", "--no-ext-diff", "--no-textconv", "--src-prefix=a/", "--dst-prefix=b/", "--output=" + patch.toAbsolutePath(), commit, "--"), source, logs.resolve("diff"), command);
            var manifest = new Manifest(2, commit, tree, AutoLlmFileUtil.hash(bundle), AutoLlmFileUtil.hash(patch), files, submodules);
            AutoLlmFileUtil.write(output.resolve("snapshot.json"), manifest);
            verifyFiles(source, manifest);
            return manifest;
        } finally { WorkloadArchiveUtil.deleteTree(scratch); }
    }

    public static void restore(Path bundle, Path snapshot, Path destination, GitSourceStore.Command command) throws IOException {
        if (Files.exists(destination, LinkOption.NOFOLLOW_LINKS)) throw new IOException("Source restoration requires a new destination: " + destination);
        Files.createDirectories(destination.toAbsolutePath().getParent());
        Path staging = Files.createTempDirectory(destination.toAbsolutePath().getParent(), "source-restore-");
        try {
            restoreTree(bundle, snapshot, staging.resolve("source"), staging.resolve("logs"), command);
            Files.move(staging.resolve("source"), destination);
        } finally { WorkloadArchiveUtil.deleteTree(staging); }
    }

    private static void restoreTree(Path bundle, Path snapshot, Path destination, Path logs, GitSourceStore.Command command) throws IOException {
        var manifest = AutoLlmFileUtil.read(snapshot.resolve("snapshot.json"), Manifest.class);
        Path patch = snapshot.resolve("source.patch");
        if (manifest.version() != 2 || !manifest.commit().matches("[a-f0-9]{40,64}") || !AutoLlmFileUtil.hash(bundle).equals(manifest.bundleSha256()) || !AutoLlmFileUtil.hash(patch).equals(manifest.patchSha256())) throw new IOException("Source snapshot identity mismatch");
        command.run(List.of("git", "clone", "--no-checkout", bundle.toAbsolutePath().toString(), destination.toAbsolutePath().toString()), destination.toAbsolutePath().getParent(), logs.resolve("clone"));
        command.run(List.of("git", "-c", "core.autocrlf=false", "-c", "filter.lfs.smudge=", "-c", "filter.lfs.required=false", "checkout", "--detach", manifest.commit()), destination, logs.resolve("checkout"));
        if (Files.size(patch) > 0) command.run(List.of("git", "-c", "core.autocrlf=false", "-c", "filter.lfs.clean=", "-c", "filter.lfs.smudge=", "-c", "filter.lfs.required=false", "apply", "--index", "--binary", patch.toAbsolutePath().toString()), destination, logs.resolve("apply"));
        command.run(List.of("git", "write-tree"), destination, logs.resolve("tree"));
        if (!Files.readString(logs.resolve("tree/stdout.log")).strip().equals(manifest.tree())) throw new IOException("Restored source tree differs from snapshot");
        for (var entry : manifest.files().entrySet()) {
            var identity = entry.getValue(); Path file = WorkloadPathUtil.inside(destination, entry.getKey());
            if (identity.link() != null) { checkLink(destination, file, identity.link()); continue; }
            if (Files.isSymbolicLink(file)) throw new IOException("Unexpected restored symbolic link: " + file);
            if (!Files.isRegularFile(file) || !AutoLlmFileUtil.hash(file).equals(identity.sha256())) {
                Path bytes;
                if (identity.raw()) bytes = WorkloadPathUtil.inside(snapshot, "raw/" + identity.sha256());
                else {
                    if (!identity.object().matches("[a-f0-9]{40,64}")) throw new IOException("Invalid Git object identity");
                    Path evidence = logs.resolve("raw-" + UUID.randomUUID());
                    command.run(List.of("git", "cat-file", "blob", identity.object()), destination, evidence);
                    bytes = evidence.resolve("stdout.log");
                }
                if (!AutoLlmFileUtil.hash(bytes).equals(identity.sha256())) throw new IOException("Restored file identity mismatch: " + entry.getKey());
                Files.copy(bytes, file, StandardCopyOption.REPLACE_EXISTING);
                if (identity.mode().equals("100755")) file.toFile().setExecutable(true);
            }
        }
        for (var entry : manifest.submodules().entrySet()) {
            var child = entry.getValue();
            if (!child.commit().matches("[a-f0-9]{40,64}")) throw new IOException("Invalid submodule commit");
            Path childSnapshot = WorkloadPathUtil.inside(snapshot, child.snapshot());
            if (!AutoLlmFileUtil.hash(childSnapshot.resolve("snapshot.json")).equals(child.manifestSha256())) throw new IOException("Submodule snapshot changed: " + entry.getKey());
            Path directory = WorkloadPathUtil.inside(destination, entry.getKey());
            if (Files.exists(directory)) Files.delete(directory); // Only the empty gitlink directory from this fresh checkout.
            Files.createDirectories(directory.getParent());
            restoreTree(bundle.resolveSibling(child.commit() + ".bundle"), childSnapshot, directory, logs.resolve("submodule-" + UUID.randomUUID()), command);
        }
        verifyFiles(destination, manifest);
    }
    private static void verifyFiles(Path source, Manifest manifest) throws IOException {
        for (var entry : manifest.files().entrySet()) {
            // Older snapshots can contain Finder metadata, which is not a build input.
            if (Path.of(entry.getKey()).getFileName().toString().equals(".DS_Store")) continue;
            Path file = WorkloadPathUtil.inside(source, entry.getKey()); var identity = entry.getValue();
            if (identity.link() != null) {
                if (!Files.isSymbolicLink(file) || !Files.readSymbolicLink(file).toString().equals(identity.link())) throw new IOException("Source link mismatch: " + entry.getKey());
            } else {
                if (Files.isSymbolicLink(file) || !AutoLlmFileUtil.hash(file).equals(identity.sha256())) throw new IOException("Source bytes changed: " + entry.getKey());
                if (Files.getFileStore(file).supportsFileAttributeView("posix")) {
                    boolean executable = Files.getPosixFilePermissions(file).contains(java.nio.file.attribute.PosixFilePermission.OWNER_EXECUTE);
                    if (executable != identity.mode().equals("100755")) throw new IOException("Source executable mode changed: " + entry.getKey());
                }
            }
        }
    }
    private static void checkLink(Path root, Path file, String target) throws IOException {
        Path link = Path.of(target);
        if (link.isAbsolute() || !file.getParent().resolve(link).normalize().startsWith(root)) throw new IOException("Source link escapes checkout: " + file);
    }
    private static String blobHash(Path file, int length) throws IOException {
        try {
            MessageDigest digest = MessageDigest.getInstance(length == 64 ? "SHA-256" : "SHA-1");
            digest.update(("blob " + Files.size(file) + "\0").getBytes(StandardCharsets.UTF_8));
            try (var input = Files.newInputStream(file)) { byte[] buffer = new byte[65536]; for (int n; (n = input.read(buffer)) >= 0;) digest.update(buffer, 0, n); }
            return HexFormat.of().formatHex(digest.digest());
        } catch (NoSuchAlgorithmException e) { throw new IllegalStateException(e); }
    }
    private static void run(List<String> prefix, List<String> args, Path cwd, Path logs, GitSourceStore.Command command) {
        var full = new ArrayList<>(prefix); full.addAll(args); command.run(full, cwd, logs);
    }
}
