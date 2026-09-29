package es.urjc.etsii.grafo.metrics.timing;

import es.urjc.etsii.grafo.config.SolverConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.dataformat.csv.CsvMapper;
import tools.jackson.dataformat.csv.CsvSchema;
import tools.jackson.databind.json.JsonMapper;

import java.nio.file.Files;
import java.nio.file.Path;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TimeStatsServiceTest {
    @TempDir Path directory;

    @Test
    void flushMakesHeadersAndRowsReadableBeforeClose() throws Exception {
        Path output = directory.resolve("recording");
        var execution = AsyncTimeStatsTest.metadata();
        try (var sink = new CsvTimeStatsSink(output, UUID.randomUUID(), 0, Instant.now())) {
            sink.flush();
            assertEquals("run_id,execution_id,thread_id,component_class,method_signature,start_ns,end_ns\n",
                    Files.readString(output.resolve("events.csv.partial")));
            assertEquals("run_id,execution_id,experiment,instance_path,instance_id,algorithm,repetition,seed\n",
                    Files.readString(output.resolve("executions.csv.partial")));
            sink.execution(execution);
            sink.flush();
            var rows = read(output.resolve("executions.csv.partial"));
            assertEquals(1, rows.size());
            assertEquals(execution.executionId().toString(), rows.getFirst().get("execution_id"));
        }
    }

    @Test
    void abandonedFinalizationLeavesPartialFiles() throws Exception {
        var id = UUID.randomUUID();
        Path output = directory.resolve(id.toString());
        try (var sink = new CsvTimeStatsSink(output, id, 0, Instant.now())) {
            sink.execution(AsyncTimeStatsTest.metadata());
            var summary = new TimeStatsSummary(id, "COMPLETE", 1, 0, 0, 0, 0, null);
            assertThrows(IOException.class, () -> sink.finish(summary, () -> false));
        }
        assertTrue(Files.exists(output.resolve("events.csv.partial")));
        assertTrue(Files.exists(output.resolve("executions.csv.partial")));
        assertFalse(Files.exists(output.resolve("manifest.json")));
        assertFalse(Files.exists(output.resolve("events.csv")));
    }

    @Test
    void streamsEscapedCsvAndPublishesManifestWithMatchingIdsAndRelativeClock() throws Exception {
        var solver = new SolverConfig();
        solver.setTimeStats(true);
        var config = new TimeStatsConfig();
        config.setOutputDirectory(directory);
        var service = new TimeStatsService(solver, config);
        var execution = new TimeStatsExecution(UUID.randomUUID(), "experiment,\"quoted\"", "path\r\nsecond line", "é", "algorithm", 3, 1237);
        service.start();
        Path output = service.outputDirectory().orElseThrow();
        try (var recorder = service.open(execution)) {
            long start = System.nanoTime();
            recorder.record(new TimeStatsMethod("Example", "method(int,String)"), start, start + 17);
            assertFalse(Files.exists(output.resolve("events.csv")));
        }
        service.open(AsyncTimeStatsTest.metadata()).close();
        service.close();
        service.close(); // Spring destruction and normal orchestration can both close.
        assertFalse(Files.exists(output.resolve("events.csv.partial")));
        List<Map<String, String>> events = read(output.resolve("events.csv"));
        var row = events.getFirst();
        assertEquals(1, events.size());
        assertEquals(execution.executionId().toString(), row.get("execution_id"));
        assertEquals("method(int,String)", row.get("method_signature"));
        assertTrue(Long.parseLong(row.get("start_ns")) >= 0);
        assertEquals(17, Long.parseLong(row.get("end_ns")) - Long.parseLong(row.get("start_ns")));
        var metadata = read(output.resolve("executions.csv"));
        assertEquals(2, metadata.size());
        var first = metadata.getFirst();
        assertEquals(execution.experiment(), first.get("experiment"));
        assertEquals(execution.instancePath(), first.get("instance_path"));
        assertEquals(execution.instanceId(), first.get("instance_id"));
        var manifest = JsonMapper.builder().build().readTree(output.resolve("manifest.json").toFile());
        assertEquals("COMPLETE", manifest.get("recording").get("status").asString());
        assertEquals(1, manifest.get("recording").get("flushedEvents").asLong());
    }

    @Test
    void disabledOrUnstartedServiceDoesNotCreateFiles() throws Exception {
        var config = new TimeStatsConfig();
        config.setOutputDirectory(directory);
        var solver = new SolverConfig();
        var service = new TimeStatsService(solver, config);
        service.start();
        assertNull(service.open(AsyncTimeStatsTest.metadata()));
        service.close();
        solver.setTimeStats(true);
        var unstarted = new TimeStatsService(solver, config); // Autoconfig never starts timing.
        assertNull(unstarted.open(AsyncTimeStatsTest.metadata()));
        unstarted.close();
        try (var files = Files.list(directory)) { assertEquals(0, files.count()); }
    }

    @Test
    void separateRunsCanBeConcatenatedWithoutIdCollisions() throws Exception {
        var ids = new java.util.HashSet<String>();
        for (int run = 0; run < 2; run++) {
            var solver = new SolverConfig();
            solver.setTimeStats(true);
            var config = new TimeStatsConfig();
            config.setOutputDirectory(directory);
            try (var service = new TimeStatsService(solver, config)) {
                service.start();
                service.open(AsyncTimeStatsTest.metadata()).close();
                service.close();
                var rows = read(service.outputDirectory().orElseThrow().resolve("executions.csv"));
                assertTrue(ids.add(rows.getFirst().get("execution_id")));
                assertTrue(ids.add(rows.getFirst().get("run_id")));
            }
        }
    }

    private List<Map<String, String>> read(Path file) {
        try (var rows = CsvMapper.builder().build().readerFor(Map.class)
                .with(CsvSchema.emptySchema().withHeader()).<Map<String, String>>readValues(file.toFile())) {
            return rows.readAll();
        }
    }
}
