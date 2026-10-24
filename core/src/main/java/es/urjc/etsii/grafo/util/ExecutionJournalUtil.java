package es.urjc.etsii.grafo.util;

import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

/** Synchronous, portable child-process records. Disabled unless a directory is configured. */
public final class ExecutionJournalUtil {
    public static final String DIRECTORY_PROPERTY = "mork.execution.directory";
    private static final JsonMapper JSON = new JsonMapper();
    private static final Map<String, Map<String, Object>> ACTIVE = new LinkedHashMap<>();
    private ExecutionJournalUtil() {}

    public static boolean enabled() { return System.getProperty(DIRECTORY_PROPERTY) != null; }
    public static Path directory() { return Path.of(System.getProperty(DIRECTORY_PROPERTY)); }

    public static synchronized void append(String kind, Map<String, ?> values) {
        if (!enabled()) return;
        var event = new LinkedHashMap<String, Object>(values);
        event.put("kind", kind);
        event.put("timestamp", Instant.now().toString());
        byte[] bytes = (JSON.writeValueAsString(event) + "\n").getBytes(java.nio.charset.StandardCharsets.UTF_8);
        try {
            Files.createDirectories(directory());
            try (var file = FileChannel.open(directory().resolve("events.jsonl"), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND)) {
                var buffer = ByteBuffer.wrap(bytes);
                while (buffer.hasRemaining()) file.write(buffer);
                file.force(false);
            }
        } catch (IOException e) { throw new UncheckedIOException("Cannot persist execution record", e); }
    }

    public static synchronized void phase(String id, String phase, long timeoutMillis, Map<String, ?> metadata) {
        if (!enabled()) return;
        var entry = new LinkedHashMap<>(ACTIVE.getOrDefault(id, Map.of()));
        entry.putAll(metadata);
        entry.put("id", id);
        entry.put("phase", phase);
        entry.put("deadline", System.currentTimeMillis() + timeoutMillis);
        ACTIVE.put(id, entry);
        write("active.json", ACTIVE);
        append("evaluation-phase", entry);
    }

    public static synchronized void finished(String id, Map<String, ?> result) {
        if (!enabled()) return;
        // Persist the result before removing its active deadline.
        append("evaluation-result", result);
        ACTIVE.remove(id);
        write("active.json", ACTIVE);
    }

    public static synchronized void write(String filename, Object value) {
        if (!enabled()) return;
        try {
            Files.createDirectories(directory());
            Path target = directory().resolve(filename);
            Path temporary = directory().resolve(filename + ".tmp");
            Files.writeString(temporary, JSON.writeValueAsString(value));
            try { Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING); }
            catch (AtomicMoveNotSupportedException e) { Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING); }
        } catch (IOException e) { throw new UncheckedIOException("Cannot persist " + filename, e); }
    }

    public static void terminal(String outcome) { write("terminal.json", Map.of("outcome", outcome, "finishedAt", Instant.now().toString())); }

    public static <S extends es.urjc.etsii.grafo.solution.Solution<S,I>, I extends es.urjc.etsii.grafo.io.Instance>
    es.urjc.etsii.grafo.executors.WorkUnitResult<S,I> withMetadata(es.urjc.etsii.grafo.executors.WorkUnitResult<S,I> result) {
        if (!enabled() || System.getProperty("mork.execution.campaign") == null) return result;
        var properties = new LinkedHashMap<>(result.solutionProperties());
        properties.put("AutoLLM campaign", System.getProperty("mork.execution.campaign"));
        properties.put("Execution target", System.getProperty("mork.execution.target"));
        properties.put("Pool revision", System.getProperty("mork.execution.pool-revision"));
        properties.put("AutoLLM trial", System.getProperty("mork.execution.trial"));
        properties.put("Tuning seed", System.getProperty("mork.execution.tuning-seed"));
        properties.put("Algorithm role", result.algorithm().getName());
        if (System.getProperty("mork.execution.artifact-sha256") != null) properties.put("Artifact SHA-256", System.getProperty("mork.execution.artifact-sha256"));
        return new es.urjc.etsii.grafo.executors.WorkUnitResult<>(result.resultId(), result.success(), result.experimentName(), result.instancePath(), result.instanceId(),
                result.algorithm(), result.iteration(), result.solution(), result.objectives(), properties, result.executionTime(), result.timeToTarget(), result.metrics());
    }

    public static Long timeLimit(String instancePath) {
        String file = System.getProperty("mork.execution.time-limits");
        if (file == null) return null;
        try {
            long limit = JSON.readTree(Files.readString(Path.of(file))).path(instancePath).longValue();
            if (limit <= 0) throw new IllegalStateException("Missing comparison time limit: " + instancePath);
            return limit;
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }

    public static void result(es.urjc.etsii.grafo.executors.WorkUnitResult<?, ?> result, boolean measured, long seed) {
        if (!enabled()) return;
        var row = new LinkedHashMap<String, Object>();
        row.put("id", result.resultId().toString());
        row.put("measured", measured);
        row.put("instance", result.instancePath());
        row.put("instanceId", result.instanceId());
        row.put("algorithm", result.algorithm().getName());
        row.put("iteration", result.iteration());
        row.put("seed", seed);
        row.put("state", result.success() ? "SUCCEEDED" : "FAILED");
        row.put("objectives", result.objectives());
        row.put("properties", result.solutionProperties());
        row.put("executionTimeNanos", result.executionTime());
        row.put("timeToBestNanos", result.timeToTarget());
        var curves = new LinkedHashMap<String, Object>();
        if (result.metrics() != null) for (var entry : result.metrics().getMetrics().entrySet()) curves.put(entry.getKey(), entry.getValue().getValues());
        row.put("curves", curves);
        finished(result.resultId().toString(), row);
    }
}
