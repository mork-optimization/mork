package es.urjc.etsii.grafo.autoconfig.autollm.execution;

import es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmFileUtil;
import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.function.Function;
import tools.jackson.core.type.TypeReference;

/** Workspace reference ledger. Deletion authority is a recorded identity, never a path prefix alone. */
public final class OwnershipManifest {
    public record Resource(String path, String token, String fileKey, String kind, String sessionId, String sessionCwd, Set<String> references) {
        public Resource { references = Collections.unmodifiableSortedSet(new TreeSet<>(references)); }
    }
    public record Dependency(String workspace, String path, String token, String reference) {}
    private static final String MARKER = ".mork-owned-v8.json";
    private final Path workspace;
    public OwnershipManifest(Path workspace) { this.workspace = workspace; }
    private Path handoffPath() {
        Path root = es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmNormalization.localPath(workspace);
        return root.getParent().resolve(".mork-cleanup/" + AutoLlmFileUtil.canonicalHash(root.toString()) + "/ownership-handoff");
    }
    private Path metadata() {
        return Files.isRegularFile(handoffPath().resolve("resources.json")) ? handoffPath()
                : es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmNormalization.localPath(workspace).resolve(".mork");
    }
    public void handoff() {
        transaction(resources -> {
            if (metadata().equals(handoffPath())) return null;
            Path staging = handoffPath().resolveSibling("ownership-handoff.pending");
            try {
                Files.createDirectories(staging);
                AutoLlmFileUtil.write(staging.resolve("resources.json"), resources);
                Path deleted = metadata().resolve("deleted");
                if (Files.isDirectory(deleted)) {
                    Files.createDirectories(staging.resolve("deleted"));
                    try (var paths = Files.list(deleted)) {
                        for (var file : paths.toList()) Files.copy(file, staging.resolve("deleted").resolve(file.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                    }
                }
                Files.move(staging, handoffPath(), StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException e) { throw new UncheckedIOException(e); }
            return null;
        });
    }
    public void finishHandoff() {
        if (!Files.isDirectory(handoffPath())) return;
        try (var paths = Files.walk(handoffPath())) {
            var ordered = new ArrayList<Path>(); for (var it = paths.iterator(); it.hasNext();) ordered.add(it.next());
            ordered.sort(Comparator.reverseOrder()); for (Path path : ordered) Files.delete(path);
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }
    private <T> T transaction(Function<Map<String,Resource>,T> operation) { synchronized (OwnershipManifest.class) {
        Path root = metadata();
        try { Files.createDirectories(root); } catch (IOException e) { throw new UncheckedIOException(e); }
        try (var channel = FileChannel.open(root.resolve("resources.lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE); var lock = channel.lock()) {
            if (!root.equals(metadata())) return transaction(operation);
            Path file = root.resolve("resources.json");
            Map<String,Resource> resources = Files.isRegularFile(file) ? AutoLlmFileUtil.read(file, new TypeReference<>() {}) : new LinkedHashMap<>();
            T value = operation.apply(resources); AutoLlmFileUtil.write(file, resources); return value;
        } catch (IOException e) { throw new UncheckedIOException(e); }
    } }
    private void requireOpen() {
        if (metadata().equals(handoffPath())) throw new IllegalStateException("Workspace cleanup is in progress");
    }
    public Resource directory(Path directory, String kind, String reference) {
        Path path = es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmNormalization.localPath(directory);
        return transaction(resources -> {
            requireOpen();
            Resource existing = resources.get(path.toString());
            try {
                if (existing == null) {
                    Files.createDirectories(path); boundary(path);
                    if (Files.exists(path.resolve(MARKER))) throw new IllegalStateException("Unregistered ownership marker: " + path);
                    String token = UUID.randomUUID().toString();
                    AutoLlmFileUtil.write(path.resolve(MARKER), Map.of("token", token, "workspace", es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmNormalization.localPath(workspace).toString()));
                    existing = new Resource(path.toString(), token, key(path), kind, null, null, Set.of());
                } else { if (!deletionPhase(existing).equals("OWNED")) throw new IllegalStateException("Resource deletion is in progress"); verify(existing); }
                var refs = new LinkedHashSet<>(existing.references()); refs.add(reference);
                var value = new Resource(existing.path(), existing.token(), existing.fileKey(), existing.kind(), null, null, refs);
                resources.put(path.toString(), value); return value;
            } catch (IOException e) { throw new UncheckedIOException(e); }
        });
    }
    public void transcript(Path file, String sessionId, Path cwd, String reference) {
        if (!es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmNormalization.localPath(cwd).startsWith(
                es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmNormalization.localPath(workspace)))
            throw new IllegalStateException("Native session belongs to a different workspace");
        transaction(resources -> {
            requireOpen();
            try {
                Path path = es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmNormalization.localPath(file); boundary(path); verifySession(path, sessionId, cwd.toString());
                var previous = resources.get(path.toString()); var refs = new LinkedHashSet<String>();
                if (previous != null) { verify(previous); refs.addAll(previous.references()); }
                refs.add(reference);
                resources.put(path.toString(), new Resource(path.toString(), sessionId, key(path), "transcript", sessionId, cwd.toString(), refs));
                return null;
            } catch (IOException e) { throw new UncheckedIOException(e); }
        });
    }
    public void removeReferences(Set<String> selected) {
        transaction(resources -> {
            for (var entry : resources.entrySet()) {
                var r = entry.getValue(); var refs = new LinkedHashSet<>(r.references()); refs.removeAll(selected);
                entry.setValue(new Resource(r.path(), r.token(), r.fileKey(), r.kind(), r.sessionId(), r.sessionCwd(), refs));
            }
            return null;
        });
    }
    public List<Resource> resources() { return Files.isRegularFile(metadata().resolve("resources.json")) ? transaction(resources -> List.copyOf(resources.values())) : List.of(); }
    public void reference(Path resource, String reference) {
        transaction(resources -> {
            requireOpen();
            var previous = resources.get(es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmNormalization.localPath(resource).toString());
            if (previous == null) throw new IllegalStateException("Dependency has no ownership record: " + resource);
            verify(previous); var refs = new LinkedHashSet<>(previous.references()); refs.add(reference);
            resources.put(previous.path(), new Resource(previous.path(), previous.token(), previous.fileKey(), previous.kind(), previous.sessionId(), previous.sessionCwd(), refs)); return null;
        });
    }
    /** Register with the resource's own workspace; no host-global catalog is needed. */
    public void screeningDependency(Path source, Path consumer) {
        source = es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmNormalization.localPath(source);
        for (Path parent = source; parent != null; parent = parent.getParent()) {
            Path marker = parent.resolve(MARKER);
            if (!Files.isRegularFile(marker)) continue;
            var identity = AutoLlmFileUtil.read(marker, Map.class);
            if (!(identity.get("workspace") instanceof String ownerWorkspace)) throw new IllegalStateException("Missing dependency ownership: " + marker);
            var owner = new OwnershipManifest(Path.of(ownerWorkspace));
            for (var resource : owner.resources()) if (resource.kind().equals("screening") && source.startsWith(Path.of(resource.path()))) {
                verify(resource);
                if (!ownerWorkspace.equals(es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmNormalization.localPath(workspace).toString())) {
                    var dependency = new Dependency(ownerWorkspace, resource.path(), resource.token(), consumer.toString());
                    Path journal = consumer.resolve("external-dependencies.json");
                    transaction(resources -> {
                        requireOpen();
                        var dependencies = new ArrayList<>(dependencies(journal));
                        if (!dependencies.contains(dependency)) dependencies.add(dependency);
                        AutoLlmFileUtil.write(journal, dependencies); return null;
                    });
                }
                owner.reference(Path.of(resource.path()), consumer.toString());
                return;
            }
        }
    }
    public static List<Dependency> dependencies(Path file) {
        return Files.isRegularFile(file) ? AutoLlmFileUtil.read(file, new TypeReference<>() {}) : List.of();
    }
    public static void releaseDependency(Dependency dependency) {
        var owner = new OwnershipManifest(Path.of(dependency.workspace()));
        if (!Files.isRegularFile(owner.metadata().resolve("resources.json"))) return;
        owner.transaction(resources -> {
            var resource = resources.get(dependency.path());
            if (resource == null) return null; // A previous release and owner cleanup already completed.
            if (!resource.token().equals(dependency.token())) throw new IllegalStateException("Dependency ownership changed: " + dependency.path());
            verify(resource);
            var references = new LinkedHashSet<>(resource.references()); references.remove(dependency.reference());
            resources.put(resource.path(), new Resource(resource.path(), resource.token(), resource.fileKey(), resource.kind(), resource.sessionId(), resource.sessionCwd(), references));
            return null;
        });
    }
    private static String key(Path path) throws IOException {
        var key = Files.readAttributes(path, BasicFileAttributes.class, LinkOption.NOFOLLOW_LINKS).fileKey();
        if (key == null) throw new IOException("Filesystem does not expose a stable file identity: " + path);
        return key.toString();
    }
    private static void boundary(Path path) throws IOException {
        for (Path p = path; p != null; p = p.getParent()) if (Files.isSymbolicLink(p)) throw new IOException("Symlink in ownership boundary: " + p);
        if (!path.equals(path.toRealPath())) throw new IOException("Ownership path is not canonical: " + path);
        if (Files.isDirectory(path.resolve(".git")) || Files.isRegularFile(path.resolve(".git"))) throw new IOException("Repository cannot be a deletion root: " + path);
    }
    private static void verifySession(Path path, String id, String cwd) throws IOException {
        try (var lines = Files.newBufferedReader(path)) {
            String line = lines.readLine();
            if (line == null || line.length() > 65536) throw new IOException("Missing session identity");
            var event = AutoLlmFileUtil.JSON.readTree(line);
            if (!event.path("type").asString("").equals("session_meta")) {
                es.urjc.etsii.grafo.autoconfig.autollm.ClaudeCodeBackend.verifyTranscript(path, id, cwd);
                return;
            }
            if (!event.path("type").asString().equals("session_meta") || !id.equals(event.path("payload").path("id").asString())
                    || !cwd.equals(event.path("payload").path("cwd").asString())) throw new IOException("Native session ownership is ambiguous: " + path);
        }
    }
    public static void verify(Resource resource) {
        try {
            Path path = Path.of(resource.path()); boundary(path);
            if (!resource.fileKey().equals(key(path))) throw new IOException("Resource path was replaced: " + path);
            if (resource.sessionId() != null) verifySession(path, resource.sessionId(), resource.sessionCwd());
            else if (!resource.token().equals(AutoLlmFileUtil.JSON.readTree(Files.readString(path.resolve(MARKER))).path("token").asString()))
                throw new IOException("Ownership marker changed: " + path);
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }
    public String deletionPhase(Resource resource) {
        Path file = metadata().resolve("deleted/" + AutoLlmFileUtil.canonicalHash(List.of(resource.path(), resource.token())) + ".json");
        if (Files.isRegularFile(file)) return "DELETED";
        if (Files.isRegularFile(file.resolveSibling(file.getFileName() + ".intent"))) return "DELETING";
        return "OWNED";
    }
    public void acknowledgeDeletion(Resource resource) {
        transaction(resources -> {
            Path file = metadata().resolve("deleted/" + AutoLlmFileUtil.canonicalHash(List.of(resource.path(), resource.token())) + ".json");
            try { Files.deleteIfExists(file); Files.deleteIfExists(file.resolveSibling(file.getFileName() + ".intent")); }
            catch (IOException e) { throw new UncheckedIOException(e); }
            return null;
        });
    }
    public void delete(Resource reviewed, Set<String> selected) {
        transaction(resources -> {
            var current = resources.get(reviewed.path());
            Path tombstone = metadata().resolve("deleted/" + AutoLlmFileUtil.canonicalHash(List.of(reviewed.path(), reviewed.token())) + ".json");
            if (current == null && Files.isRegularFile(tombstone)) return null;
            if (current == null || !current.equals(reviewed) || !selected.containsAll(current.references())) throw new IllegalStateException("Cleanup scope changed; create a new plan");
            Path intent = tombstone.resolveSibling(tombstone.getFileName() + ".intent");
            try {
                Path root = Path.of(current.path());
                if (!Files.exists(intent)) { verify(current); AutoLlmFileUtil.write(intent, current); }
                else if (Files.exists(root)) {
                    boundary(root); if (!current.fileKey().equals(key(root))) throw new IOException("Deletion boundary was replaced");
                }
                if (!Files.exists(root)) { AutoLlmFileUtil.write(tombstone, current); resources.remove(current.path()); return null; }
                if (current.sessionId() != null) Files.delete(root);
                else {
                    // Keep the boundary marker until last, so interrupted deletion remains verifiable.
                    try (var paths = Files.walk(root)) {
                        var entries = new ArrayList<>(paths.toList()); entries.sort(Comparator.reverseOrder());
                        for (Path p : entries) if (!p.equals(root) && !p.equals(root.resolve(MARKER))) {
                            if (!current.fileKey().equals(key(root))) throw new IOException("Deletion boundary changed");
                            for (Path parent = p.getParent(); parent != null && parent.startsWith(root); parent = parent.getParent())
                                if (Files.isSymbolicLink(parent)) throw new IOException("Symlink appeared inside deletion boundary: " + parent);
                            Files.deleteIfExists(p);
                        }
                    }
                    Files.deleteIfExists(root.resolve(MARKER)); Files.delete(root);
                }
                AutoLlmFileUtil.write(tombstone, current);
                resources.remove(current.path()); return null;
            } catch (IOException e) { throw new UncheckedIOException(e); }
        });
    }
}
