package es.urjc.etsii.grafo.metrics.timing;

import es.urjc.etsii.grafo.config.SolverConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Service;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/** Timing lifecycle for regular experiments. Autoconfig does not start this service. */
@Service
public class TimeStatsService implements DisposableBean, AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(TimeStatsService.class);
    private final SolverConfig solverConfig;
    private final TimeStatsConfig config;
    private volatile AsyncTimeStats recording;
    private Path directory;

    public TimeStatsService(SolverConfig solverConfig, TimeStatsConfig config) {
        this.solverConfig = solverConfig;
        this.config = config;
    }

    public synchronized void start() {
        if (!solverConfig.isTimeStats() || recording != null) return;
        var id = UUID.randomUUID();
        var wallTime = Instant.now();
        long origin = System.nanoTime();
        directory = config.getOutputDirectory().toAbsolutePath().resolve(id.toString());
        recording = new AsyncTimeStats(id, origin, config, () -> new CsvTimeStatsSink(directory, id, origin, wallTime));
        log.info("Streaming TimeStats to {} (bounded buffers; incomplete recordings are reported)", directory);
    }

    public TimeStatsRecorder open(TimeStatsExecution execution) {
        var current = recording;
        return current == null ? null : current.open(execution);
    }

    public Optional<TimeStatsSummary> snapshot() {
        var current = recording;
        return current == null ? Optional.empty() : Optional.of(current.snapshot());
    }

    public Optional<Path> outputDirectory() { return Optional.ofNullable(directory); }

    @Override
    public void close() {
        var current = recording;
        if (current != null) current.close();
    }

    @Override
    public void destroy() { close(); }
}
