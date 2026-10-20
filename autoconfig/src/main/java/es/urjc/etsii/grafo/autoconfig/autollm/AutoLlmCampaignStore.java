package es.urjc.etsii.grafo.autoconfig.autollm;

import org.springframework.stereotype.Service;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;

@Service
public class AutoLlmCampaignStore {
    private final Path root;
    private final Map<Path, CachedHash> hashes = new java.util.concurrent.ConcurrentHashMap<>();
    private record CachedHash(long size, long modified, String hash) {}
    public AutoLlmCampaignStore(AutoLlmConfig config) { this.root = Path.of(config.workspace()).toAbsolutePath().normalize(); }
    public Path root() { return root; }
    public synchronized void requireReservation(String id) {
        Path file = root.resolve("operation-reservation.json");
        if (Files.exists(file)) {
            String owner = (String) AutoLlmFileUtil.read(file, Map.class).get("id");
            if (!id.equals(owner)) throw new IllegalStateException("Reconcile or cancel saved operation " + owner + " before starting another workload");
        }
    }
    public synchronized void reserve(String id) { requireReservation(id); AutoLlmFileUtil.write(root.resolve("operation-reservation.json"), Map.of("id", id)); }
    public synchronized void releaseReservation(String id, Path directory) {
        requireReservation(id);
        if (hasUncollectedJobs(directory)) return;
        try { Files.deleteIfExists(root.resolve("operation-reservation.json")); } catch (IOException e) { throw new UncheckedIOException(e); }
    }
    public static boolean hasUncollectedJobs(Path directory) {
        for (Path path : AutoLlmFileUtil.ownedFiles(directory, "remote-job.json")) if (!Files.exists(path.getParent().resolve("collection-completed.json"))) return true;
        return false;
    }
    public Path directory(String id) {
        if (!id.matches("[a-f0-9-]{36}")) throw new IllegalArgumentException("Invalid campaign id");
        return root.resolve(id);
    }
    public synchronized void save(AutoLlmCampaign campaign) {
        AutoLlmFileUtil.write(directory(campaign.id).resolve("campaign.json"), campaign);
        var state = new LinkedHashMap<String,Object>(); state.put("campaignId", campaign.id); state.put("status", campaign.status);
        state.put("configurationPath", directory(campaign.id).resolve("control/config.json").toString()); state.put("remote", campaign.remote);
        state.put("updatedAt", Instant.now().toString()); AutoLlmFileUtil.write(root.resolve("campaign-session.json"), state);
    }
    public synchronized AutoLlmCampaign load(String id) {
        Path file = directory(id).resolve("campaign.json");
        if (!Files.exists(file)) throw new NoSuchElementException("Campaign does not exist: " + id);
        var campaign = AutoLlmFileUtil.read(file, AutoLlmCampaign.class);
        if (campaign.schemaVersion != AutoLlmCampaign.SCHEMA_VERSION) throw new IllegalStateException("Unsupported campaign schema " + campaign.schemaVersion + "; use a fresh workspace. Historical evidence remains available with the original controller.");
        return campaign;
    }
    public synchronized List<AutoLlmCampaign> list() {
        var campaigns = new ArrayList<AutoLlmCampaign>();
        if (!Files.isDirectory(root)) return campaigns;
        try (var dirs = Files.list(root)) {
            for (Path dir : dirs.toList()) if (Files.isRegularFile(dir.resolve("campaign.json"))) campaigns.add(load(dir.getFileName().toString()));
        } catch (IOException e) { throw new UncheckedIOException(e); }
        campaigns.sort(Comparator.comparing((AutoLlmCampaign c) -> c.createdAt).reversed());
        return campaigns;
    }
    public synchronized void event(String id, String kind, Object detail) {
        AutoLlmFileUtil.append(directory(id).resolve("campaign-events.jsonl"), Map.of("timestamp", Instant.now().toString(), "kind", kind, "detail", detail));
    }
    public record Artifact(String path, long size, String sha256) {}
    public List<Artifact> artifacts(String id) {
        Path base = directory(id);
        var artifacts = new ArrayList<Artifact>();
        try (var paths = Files.walk(base)) {
            for (Path path : paths.toList()) {
                String name = base.relativize(path).toString().replace('\\', '/');
                if (Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS) && downloadable(name)) {
                    long size = Files.size(path), modified = Files.getLastModifiedTime(path).toMillis();
                    var cached = hashes.get(path);
                    if (cached == null || cached.size != size || cached.modified != modified) {
                        cached = new CachedHash(size, modified, AutoLlmFileUtil.hash(path)); hashes.put(path, cached);
                    }
                    artifacts.add(new Artifact(name, size, cached.hash));
                }
            }
        } catch (IOException e) { throw new UncheckedIOException(e); }
        artifacts.sort(Comparator.comparing(Artifact::path));
        return artifacts;
    }
    private boolean downloadable(String name) {
        return !name.startsWith(".transport/") && !name.startsWith(".transfer/") && !name.startsWith(".source-bundles/")
                && !AutoLlmFileUtil.excluded(name) && !name.endsWith("owner.json") && !name.endsWith("readiness.json") && !name.endsWith("scenario.txt") && !name.endsWith("runner.R") && !name.endsWith(".lock") && !name.endsWith(".tmp");
    }
    public Path artifact(String id, String relative) {
        Path path = AutoLlmFileUtil.inside(directory(id), relative);
        if (!downloadable(relative) || !Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) throw new NoSuchElementException("Artifact unavailable");
        try { if (!path.toRealPath().startsWith(directory(id).toRealPath())) throw new IllegalArgumentException("Artifact escapes campaign directory"); }
        catch (IOException e) { throw new UncheckedIOException(e); }
        return path;
    }
    public ExecutionLock lock() {
        try {
            Files.createDirectories(root);
            var channel = FileChannel.open(root.resolve("execution.lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            FileLock lock;
            try { lock = channel.tryLock(); } catch (java.nio.channels.OverlappingFileLockException e) { channel.close(); throw new IllegalStateException("Another campaign is active", e); }
            if (lock == null) { channel.close(); throw new IllegalStateException("Another campaign is active"); }
            return new ExecutionLock(channel, lock);
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }
    public record ExecutionLock(FileChannel channel, FileLock lock) implements AutoCloseable {
        public void close() { try { lock.release(); channel.close(); } catch (IOException e) { throw new UncheckedIOException(e); } }
    }
}
