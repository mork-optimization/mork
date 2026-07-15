package es.urjc.etsii.grafo.autoconfig.r;

import java.nio.file.Path;
import java.time.Duration;

/**
 * Result of a completed R script process.
 *
 * @param exitCode process exit code
 * @param elapsed process execution duration
 * @param stdoutLog captured standard-output log
 * @param stderrLog captured standard-error log
 */
public record RExecutionResult(
        int exitCode,
        Duration elapsed,
        Path stdoutLog,
        Path stderrLog
) {
    /**
     * Whether the process completed successfully.
     *
     * @return true for exit code zero
     */
    public boolean successful() {
        return exitCode == 0;
    }
}
