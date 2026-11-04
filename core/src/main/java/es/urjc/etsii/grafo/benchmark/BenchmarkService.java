package es.urjc.etsii.grafo.benchmark;

import es.urjc.etsii.grafo.config.SolverConfig;
import jnt.scimark2.ScimarkAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.function.IntFunction;
import java.util.function.Supplier;

/** One benchmark per execution, run synchronously in the solver VM. No writer coordination. */
@Service
public class BenchmarkService {
    private static final Logger log = LoggerFactory.getLogger(BenchmarkService.class);
    static final JsonMapper JSON = JsonMapper.builder()
            .enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES)
            .enable(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES)
            .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
            .build();
    private final Path file;
    private final Supplier<Map<String, String>> environment;
    private final IntFunction<BenchmarkResult.Scores> measure;
    private volatile BenchmarkReport report = BenchmarkReport.notRecorded(Map.of());

    public BenchmarkService() {
        this(Path.of(".mork", ".benchmark"), BenchmarkEnvironment::current,
                seed -> BenchmarkResult.Scores.from(ScimarkAPI.runBenchmark(seed)));
    }

    BenchmarkService(Path file, Supplier<Map<String, String>> environment, IntFunction<BenchmarkResult.Scores> measure) {
        this.file = file;
        this.environment = environment;
        this.measure = measure;
    }

    public BenchmarkReport report() { return report; }

    public BenchmarkReport prepare(SolverConfig config) {
        report = new BenchmarkReport(BenchmarkReport.Status.DISABLED, BenchmarkEnvironment.runtime(), null, "Benchmark disabled");
        if (!config.isBenchmark()) return report;
        Map<String, String> current = report.environment();
        try {
            checkInterrupted();
            current = environment.get();
            report = new BenchmarkReport(BenchmarkReport.Status.UNAVAILABLE, current, null, "Benchmark did not complete");
            BenchmarkResult saved = load(config.getSeed(), current);
            if (saved != null) {
                report = new BenchmarkReport(BenchmarkReport.Status.REUSED, current, saved, "Reused saved measurement");
            } else {
                log.info("Running CPU benchmark in the current JVM...");
                var result = BenchmarkResult.measured(config.getSeed(), current, measure.apply((int) config.getSeed()));
                checkInterrupted();
                String detail;
                if (!BenchmarkEnvironment.identifiable(current)) {
                    detail = "Measured without persistence: could not identify the host/CPU";
                    log.warn(detail);
                } else {
                    detail = store(result) ? "Measured and saved" : "Measured; benchmark file could not be saved";
                }
                report = new BenchmarkReport(BenchmarkReport.Status.MEASURED, current, result, detail);
            }
            log.info("SciMark score: {} approximate MFLOPS ({}, measured {})", report.result().score(), report.status(), report.result().measuredAt());
        } catch (CancellationException e) {
            throw e;
        } catch (RuntimeException e) {
            checkInterrupted();
            log.warn("CPU benchmark unavailable: {}", e.toString());
            report = new BenchmarkReport(BenchmarkReport.Status.UNAVAILABLE, current, null, e.toString());
        }
        return report;
    }

    private BenchmarkResult load(long seed, Map<String, String> current) {
        if (!Files.exists(file)) return null;
        try {
            if (Files.size(file) > 65536) throw new IOException("Benchmark file is too large");
            var saved = JSON.readValue(Files.readString(file), BenchmarkResult.class);
            if (saved.matches(seed, current)) return saved;
            log.info("Benchmark environment or inputs changed; removing {}", file);
        } catch (IOException | RuntimeException e) {
            log.warn("Invalid benchmark file {}; removing it: {}", file, e.toString());
        }
        try { Files.deleteIfExists(file); }
        catch (IOException e) { log.warn("Could not remove invalid benchmark file {}: {}", file, e.toString()); }
        return null;
    }

    private boolean store(BenchmarkResult result) {
        Path temporary = null;
        try {
            Path directory = file.toAbsolutePath().getParent();
            Files.createDirectories(directory);
            temporary = Files.createTempFile(directory, ".benchmark-", ".part");
            Files.writeString(temporary, JSON.writerWithDefaultPrettyPrinter().writeValueAsString(result));
            Files.move(temporary, file.toAbsolutePath(), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException | RuntimeException e) {
            log.warn("Could not save benchmark file {}: {}", file, e.toString());
            return false;
        } finally {
            if (temporary != null) {
                try { Files.deleteIfExists(temporary); }
                catch (IOException e) { log.warn("Could not remove benchmark staging file {}: {}", temporary, e.toString()); }
            }
        }
    }

    private static void checkInterrupted() {
        if (Thread.currentThread().isInterrupted()) throw new CancellationException("Benchmark interrupted");
    }
}
