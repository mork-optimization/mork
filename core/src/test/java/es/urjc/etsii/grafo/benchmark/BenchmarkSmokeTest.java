package es.urjc.etsii.grafo.benchmark;

import jnt.scimark2.ScimarkAPI;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/** Explicit opt-in: exercises the unchanged, real SciMark measurement rather than a fake score. */
@EnabledIfSystemProperty(named = "mork.benchmark.smoke", matches = "true")
class BenchmarkSmokeTest {
    @Test
    void measuresAndReuses(@TempDir Path temp) {
        Path file = temp.resolve(".mork/.benchmark");
        var environment = BenchmarkEnvironment.current();
        assertTrue(BenchmarkEnvironment.identifiable(environment), "Smoke host must expose CPU and hostname");
        Thread callingThread = Thread.currentThread();
        var service = new BenchmarkService(file, () -> environment, seed -> {
            assertSame(callingThread, Thread.currentThread());
            return BenchmarkResult.Scores.from(ScimarkAPI.runBenchmark(seed));
        });
        long start = System.nanoTime();
        var measured = service.prepare(BenchmarkServiceTest.configuration());
        assertEquals(BenchmarkReport.Status.MEASURED, measured.status());
        assertTrue(Files.isRegularFile(file));
        assertTrue(Double.isFinite(measured.result().score()));
        assertTrue(measured.result().score() > 0);
        System.out.printf("Real SciMark completed in %.3f seconds; score %.3f MFLOPS%n", (System.nanoTime() - start) / 1e9, measured.result().score());
        var loaded = new BenchmarkService(file, () -> environment, seed -> fail("Expected reuse"));
        assertEquals(measured.result(), loaded.prepare(BenchmarkServiceTest.configuration()).result());
        assertEquals(BenchmarkReport.Status.REUSED, loaded.report().status());
    }
}
