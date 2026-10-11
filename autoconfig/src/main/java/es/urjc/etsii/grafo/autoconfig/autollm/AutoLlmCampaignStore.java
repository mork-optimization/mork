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
    public Path directory(String id) {
        if (!id.matches("[a-f0-9-]{36}")) throw new IllegalArgumentException("Invalid campaign id");
        return root.resolve(id);
    }
    public synchronized void save(AutoLlmCampaign campaign) { AutoLlmFileUtil.write(directory(campaign.id).resolve("campaign.json"), campaign); }
    public synchronized AutoLlmCampaign load(String id) {
        Path file = directory(id).resolve("campaign.json");
        if (!Files.exists(file)) throw new NoSuchElementException("Campaign does not exist: " + id);
        var campaign = AutoLlmFileUtil.read(file, AutoLlmCampaign.class);
        if (campaign.schemaVersion != 1) throw new IllegalStateException("Unsupported campaign schema");
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
        return !AutoLlmFileUtil.excluded(name) && !name.endsWith("owner.json") && !name.endsWith("readiness.json") && !name.endsWith("scenario.txt") && !name.endsWith("runner.R") && !name.endsWith(".lock") && !name.endsWith(".tmp");
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
