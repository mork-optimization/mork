package es.urjc.etsii.grafo.metrics.timing;

import tools.jackson.databind.SequenceWriter;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.dataformat.csv.CsvMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.UUID;
import java.util.function.BooleanSupplier;

/** Single writer, append-only CSV. The manifest is the final publication marker. */
final class CsvTimeStatsSink implements TimeStatsSink {
    private final Path directory;
    private final String runId;
    private final long origin;
    private final Instant startedAt;
    private final SequenceWriter events;
    private final SequenceWriter executions;
    private boolean closed;

    CsvTimeStatsSink(Path directory, UUID runId, long origin, Instant startedAt) throws IOException {
        this.directory = directory;
        this.runId = runId.toString();
        this.origin = origin;
        this.startedAt = startedAt;
        Files.createDirectories(directory.getParent());
        Files.createDirectory(directory); // Never replace another recording.
        var writer = CsvMapper.builder().build().writer()
                .without(SerializationFeature.FLUSH_AFTER_WRITE_VALUE);
        events = writer.writeValues(directory.resolve("events.csv.partial").toFile());
        try {
            executions = writer.writeValues(directory.resolve("executions.csv.partial").toFile());
        } catch (RuntimeException e) {
            events.close();
            throw e;
        }
        try {
            events.write(new String[]{"run_id", "execution_id", "thread_id", "component_class",
                    "method_signature", "start_ns", "end_ns"});
            executions.write(new String[]{"run_id", "execution_id", "experiment", "instance_path",
                    "instance_id", "algorithm", "repetition", "seed"});
        } catch (RuntimeException e) {
            close();
            throw e;
        }
    }

    @Override
    public void execution(TimeStatsExecution execution) throws IOException {
        executions.write(new Object[]{runId, execution.executionId(), execution.experiment(),
                execution.instancePath(), execution.instanceId(), execution.algorithm(), execution.repetition(), execution.seed()});
    }

    @Override
    public void batch(AsyncTimeStats.Batch batch) throws IOException {
        for (int i = 0; i < batch.size; i++) {
            var method = batch.methods[i];
            events.write(new Object[]{runId, batch.execution.metadata.executionId(), batch.threadId,
                    method.componentClass(), method.signature(), batch.enter[i], batch.exit[i]});
        }
    }

    @Override
    public void flush() throws IOException {
        executions.flush();
        events.flush();
    }

    private void manifest(TimeStatsSummary summary) throws IOException {
        var values = new LinkedHashMap<String, Object>();
        values.put("schemaVersion", 1);
        values.put("startedAt", startedAt.toString());
        values.put("monotonicOriginNanos", origin);
        values.put("timestampUnit", "nanoseconds since monotonicOriginNanos");
        values.put("recording", summary);
        JsonMapper.builder().build().writerWithDefaultPrettyPrinter()
                .writeValue(directory.resolve("manifest.json.partial").toFile(), values);
    }

    @Override
    public void finish(TimeStatsSummary summary, BooleanSupplier mayPublish) throws IOException {
        close();
        manifest(summary);
        for (String name : new String[]{"events.csv", "executions.csv", "manifest.json"}) {
            if (!mayPublish.getAsBoolean()) throw new IOException("Timing writer shutdown timed out before publication");
            Files.move(directory.resolve(name + ".partial"), directory.resolve(name));
        }
    }

    @Override
    public void failure(TimeStatsSummary summary) throws IOException {
        manifest(summary);
    }

    @Override
    public void close() throws IOException {
        if (closed) return;
        closed = true;
        try { events.close(); }
        finally { executions.close(); }
    }
}
