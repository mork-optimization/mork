package es.urjc.etsii.grafo.benchmark;

import es.urjc.etsii.grafo.config.SolverConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.atomic.AtomicInteger;

import static es.urjc.etsii.grafo.benchmark.BenchmarkReport.Status.*;
import static org.junit.jupiter.api.Assertions.*;

class BenchmarkServiceTest {
    @TempDir Path temp;
    private static final BenchmarkResult.Scores SCORES = new BenchmarkResult.Scores(10, 20, 30, 40, 50);

    static Map<String, String> environment() {
        var environment = new HashMap<>(BenchmarkEnvironment.runtime());
        environment.put("hostname", "test-host");
        environment.put("cpu", "test-cpu");
        return Map.copyOf(environment);
    }

    static SolverConfig configuration() {
        var config = new SolverConfig();
        config.setBenchmark(true);
        config.setSeed(1234);
        return config;
    }

    @Test
    void measuresInCallingThreadThenReusesAcrossServiceInstances() throws IOException {
        var calls = new AtomicInteger();
        var thread = Thread.currentThread();
        Path file = temp.resolve(".mork/.benchmark");
        Files.writeString(temp.resolve(".benchmark"), "legacy file must not be used");
        var service = new BenchmarkService(file, BenchmarkServiceTest::environment, seed -> {
            assertSame(thread, Thread.currentThread());
            assertEquals(1234, seed);
            calls.incrementAndGet();
            return SCORES;
        });
        var measured = service.prepare(configuration());
        assertEquals(MEASURED, measured.status());
        assertEquals(30, measured.result().score());
        assertEquals(2, measured.result().protocol().minimumSeconds());
        assertEquals(1024, measured.result().protocol().fftSize());
        assertEquals(SCORES, measured.result().scores());
        assertTrue(Files.isRegularFile(file));
        try (var children = Files.list(file.getParent())) {
            assertEquals(1, children.count(), "No lock, cache/tmp folders, or staging files remain");
        }
        var freshService = new BenchmarkService(file, BenchmarkServiceTest::environment, seed -> fail("Must reuse saved score"));
        var reused = freshService.prepare(configuration());
        assertEquals(REUSED, reused.status());
        assertEquals(measured.result(), reused.result());
        assertEquals(1, calls.get());
        assertEquals("legacy file must not be used", Files.readString(temp.resolve(".benchmark")));
    }

    @Test
    void disabledDoesNotReadFilesProbeHardwareOrMeasureAndClearsPreviousResult() throws IOException {
        Path file = temp.resolve(".benchmark");
        var service = new BenchmarkService(file, BenchmarkServiceTest::environment, seed -> SCORES);
        service.prepare(configuration());
        Files.writeString(file, "invalid data remains untouched when disabled");
        var disabled = new SolverConfig();
        var report = service.prepare(disabled);
        assertEquals(DISABLED, report.status());
        assertNull(report.result());
        assertFalse(report.environment().get("java.runtime.version").isBlank());
        assertEquals("invalid data remains untouched when disabled", Files.readString(file));
        new BenchmarkService(temp.resolve("absent/.benchmark"), () -> fail("No hardware probe"), seed -> fail("No measurement"))
                .prepare(disabled);
        assertFalse(Files.exists(temp.resolve("absent")));
    }

    @Test
    void malformedMissingUnsupportedAndNonfiniteDataIsRemovedBeforeRemeasurement() throws IOException {
        Path file = temp.resolve(".benchmark");
        var valid = BenchmarkResult.measured(1234, environment(), SCORES);
        String json = BenchmarkService.JSON.writeValueAsString(valid);
        for (String invalid : new String[]{"4\nvm\njava\n12.5\n", "{", "null", json + " {}",
                json.replace("\"version\":1", "\"version\":99"), json.replace("\"version\":1,", ""),
                json.replace("\"score\":30.0", "\"score\":null"),
                json.replace("\"score\":30.0", "\"score\":\"Infinity\""),
                json.replace("\"score\":30.0", "\"score\":31.0"),
                json.replace("\"fft\":10.0", "\"fft\":-1.0")}) {
            Files.writeString(file, invalid);
            var service = new BenchmarkService(file, BenchmarkServiceTest::environment, seed -> {
                assertFalse(Files.exists(file), "Invalid data must be removed before running SciMark");
                return SCORES;
            });
            assertEquals(MEASURED, service.prepare(configuration()).status(), invalid);
            assertEquals(valid.protocol(), BenchmarkService.JSON.readValue(Files.readString(file), BenchmarkResult.class).protocol());
        }
    }

    @Test
    void everyRecordedHostOrRuntimeParameterAndSeedInvalidatesTheFile() throws IOException {
        Path file = temp.resolve(".benchmark");
        var valid = BenchmarkResult.measured(1234, environment(), SCORES);
        for (String key : environment().keySet()) {
            Files.writeString(file, BenchmarkService.JSON.writeValueAsString(valid));
            var changed = new HashMap<>(environment());
            changed.put(key, changed.get(key) + "-changed");
            var service = new BenchmarkService(file, () -> changed, seed -> {
                assertFalse(Files.exists(file));
                return SCORES;
            });
            assertEquals(MEASURED, service.prepare(configuration()).status(), key);
        }
        Files.writeString(file, BenchmarkService.JSON.writeValueAsString(valid));
        var config = configuration();
        config.setSeed(9876);
        var service = new BenchmarkService(file, BenchmarkServiceTest::environment, seed -> {
            assertEquals(9876, seed);
            assertFalse(Files.exists(file));
            return SCORES;
        });
        assertEquals(MEASURED, service.prepare(config).status());
        String saved = Files.readString(file).replace("scimark-2.2-small", "different-protocol");
        Files.writeString(file, saved);
        assertEquals(MEASURED, service.prepare(config).status());
    }

    @Test
    void unidentifiedHostDoesNotReuseOrSave() throws IOException {
        Path file = temp.resolve(".benchmark");
        Files.writeString(file, BenchmarkService.JSON.writeValueAsString(BenchmarkResult.measured(1234, environment(), SCORES)));
        var unknown = new HashMap<>(environment());
        unknown.put("cpu", "");
        var service = new BenchmarkService(file, () -> unknown, seed -> SCORES);
        var report = service.prepare(configuration());
        assertEquals(MEASURED, report.status());
        assertEquals(30, report.result().score());
        assertFalse(Files.exists(file));
        assertTrue(report.detail().contains("without persistence"));
    }

    @Test
    void unwritableStorageStillReturnsMeasurement() throws IOException {
        Path blocker = temp.resolve(".mork");
        Files.writeString(blocker, "A regular file prevents creating the benchmark directory");
        var service = new BenchmarkService(blocker.resolve(".benchmark"), BenchmarkServiceTest::environment, seed -> SCORES);
        var report = service.prepare(configuration());
        assertEquals(MEASURED, report.status());
        assertEquals(30, report.result().score());
        assertTrue(report.detail().contains("could not be saved"));
        assertTrue(Files.isRegularFile(blocker));
    }

    @Test
    void failedAtomicPublicationRemovesOnlyItsStagingFile() throws IOException {
        Path directory = Files.createDirectory(temp.resolve(".mork"));
        Path invalidDestination = Files.createDirectory(directory.resolve(".benchmark"));
        Files.writeString(invalidDestination.resolve("existing"), "Do not delete unrelated contents");
        var service = new BenchmarkService(invalidDestination, BenchmarkServiceTest::environment, seed -> SCORES);
        var report = service.prepare(configuration());
        assertEquals(MEASURED, report.status());
        assertTrue(report.detail().contains("could not be saved"));
        assertEquals("Do not delete unrelated contents", Files.readString(invalidDestination.resolve("existing")));
        try (var files = Files.list(directory)) {
            assertEquals(1, files.count(), "Failed publication must not leave a staging file behind");
        }
    }

    @Test
    void failedMeasurementDoesNotPublishOrFallBackToInvalidData() throws IOException {
        Path file = temp.resolve(".benchmark");
        Files.writeString(file, "corrupt");
        var service = new BenchmarkService(file, BenchmarkServiceTest::environment, seed -> { throw new IllegalStateException("kernel failed"); });
        var report = service.prepare(configuration());
        assertEquals(UNAVAILABLE, report.status());
        assertNull(report.result());
        assertTrue(report.detail().contains("kernel failed"));
        assertFalse(Files.exists(file));
        for (double score : new double[]{0, -1, Double.NaN, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY}) {
            assertThrows(IllegalArgumentException.class, () -> new BenchmarkResult.Scores(score, 1, 1, 1, 1));
        }
    }

    @Test
    void interruptionIsPreservedAndNeverSavesAResult() {
        Path file = temp.resolve(".benchmark");
        var service = new BenchmarkService(file, BenchmarkServiceTest::environment, seed -> {
            Thread.currentThread().interrupt();
            return SCORES;
        });
        try {
            assertThrows(CancellationException.class, () -> service.prepare(configuration()));
            assertTrue(Thread.currentThread().isInterrupted());
            assertFalse(Files.exists(file));
        } finally { Thread.interrupted(); }
    }
}
