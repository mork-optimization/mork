package es.urjc.etsii.grafo.autoconfig.r;

import java.nio.file.Path;
import java.util.Map;

/**
 * Parameters for one R script execution.
 *
 * @param script R script to execute
 * @param workingDirectory process working directory
 * @param environment environment variables added to the child process
 */
public record RExecutionRequest(
        Path script,
        Path workingDirectory,
        Map<String, String> environment
) {
    /**
     * Create a validated, immutable execution request.
     */
    public RExecutionRequest {
        if (script == null) {
            throw new IllegalArgumentException("R script path cannot be null");
        }
        if (workingDirectory == null) {
            throw new IllegalArgumentException("R working directory cannot be null");
        }
        if (environment == null) {
            throw new IllegalArgumentException("R process environment cannot be null");
        }
        workingDirectory = workingDirectory.toAbsolutePath().normalize();
        script = script.isAbsolute()
                ? script.normalize()
                : workingDirectory.resolve(script).normalize();
        environment = Map.copyOf(environment);
    }
}
