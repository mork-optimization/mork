package es.urjc.etsii.grafo.monitoring;

import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

/** Only files explicitly registered by successful exports can be downloaded. */
@Service
public class MonitoringArtifactRegistry {
    public record Artifact(String id, String experimentName, UUID resultId, String filename, long size, long modifiedAt) {}
    private record Registered(String id, String experiment, UUID resultId, Path path) {}
    private final Map<String, Registered> entries = new LinkedHashMap<>();
    private final Map<Path, String> ids = new HashMap<>();

    public synchronized void register(Path path, String experiment, UUID resultId) {
        try {
            if (!Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS)) return;
            var real = path.toRealPath();
            String id = ids.computeIfAbsent(real, ignored -> UUID.randomUUID().toString());
            entries.put(id, new Registered(id, experiment, resultId, real));
        } catch (IOException e) { throw new IllegalStateException("Cannot register exported artifact", e); }
    }

    public synchronized List<Artifact> manifest() {
        var result = new ArrayList<Artifact>();
        for (var entry : entries.values()) {
            var path = available(entry.id);
            if (path == null) continue;
            try { result.add(new Artifact(entry.id, entry.experiment, entry.resultId, path.getFileName().toString(), Files.size(path), Files.getLastModifiedTime(path).toMillis())); }
            catch (IOException ignored) { /* An export can disappear between manifest reads. */ }
        }
        return List.copyOf(result);
    }

    public synchronized Path available(String id) {
        var entry = entries.get(id);
        if (entry == null) return null;
        try { return Files.isRegularFile(entry.path, LinkOption.NOFOLLOW_LINKS) && entry.path.equals(entry.path.toRealPath()) ? entry.path : null; }
        catch (IOException e) { return null; }
    }
}
