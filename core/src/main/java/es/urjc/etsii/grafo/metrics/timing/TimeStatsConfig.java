package es.urjc.etsii.grafo.metrics.timing;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Path;
import java.time.Duration;
import java.util.Objects;

@Configuration
@ConfigurationProperties(prefix = "time-stats")
public class TimeStatsConfig {
    private Path outputDirectory = Path.of("time-stats");
    private int batchSize = 4096;
    private int queueCapacity = 64;
    private int poolCapacity = 64;
    private Duration flushInterval = Duration.ofSeconds(1);
    private Duration shutdownTimeout = Duration.ofSeconds(5);

    public Path getOutputDirectory() { return outputDirectory; }
    public void setOutputDirectory(Path value) { outputDirectory = Objects.requireNonNull(value); }
    public int getBatchSize() { return batchSize; }
    public void setBatchSize(int value) {
        if (value <= 0) throw new IllegalArgumentException("time-stats.batch-size must be positive");
        batchSize = value;
    }
    public int getQueueCapacity() { return queueCapacity; }
    public void setQueueCapacity(int value) {
        if (value <= 0) throw new IllegalArgumentException("time-stats.queue-capacity must be positive");
        queueCapacity = value;
    }
    public int getPoolCapacity() { return poolCapacity; }
    public void setPoolCapacity(int value) {
        if (value <= 0) throw new IllegalArgumentException("time-stats.pool-capacity must be positive");
        poolCapacity = value;
    }
    public Duration getFlushInterval() { return flushInterval; }
    public void setFlushInterval(Duration value) {
        if (value == null || value.toMillis() <= 0) throw new IllegalArgumentException("time-stats.flush-interval must be at least 1 ms");
        flushInterval = value;
    }
    public Duration getShutdownTimeout() { return shutdownTimeout; }
    public void setShutdownTimeout(Duration value) {
        if (value == null || value.toMillis() <= 0) throw new IllegalArgumentException("time-stats.shutdown-timeout must be at least 1 ms");
        shutdownTimeout = value;
    }
}
