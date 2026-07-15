package es.urjc.etsii.grafo.autoconfig.irace;

import es.urjc.etsii.grafo.autoconfig.r.RLangRunner;
import es.urjc.etsii.grafo.autoconfig.r.RExecutionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import static es.urjc.etsii.grafo.util.IOUtil.copyWithSubstitutions;
import static es.urjc.etsii.grafo.util.IOUtil.getInputStreamForIrace;

/**
 * <p>IraceIntegration class.</p>
 *
 */
@Service
public class IraceIntegration {
    private static final Logger log = LoggerFactory.getLogger(IraceIntegration.class.getName());
    private static final String RUNNER_SCRIPT = "runner.R";

    private final RLangRunner runner;

    /**
     * <p>Constructor for IraceIntegration.</p>
     *
     * @param runner a {@link RLangRunner} object
     */
    public IraceIntegration(RLangRunner runner) {
        log.debug("Using R runner: {}", runner.getClass().getSimpleName());
        this.runner = runner;
    }

    /**
     * <p>runIrace.</p>
     *
     * @param isJAR a boolean.
     */
    public void runIrace(boolean isJAR){
        Path workingDirectory = Path.of("").toAbsolutePath().normalize();
        Path script = workingDirectory.resolve(RUNNER_SCRIPT);
        try {
            try (var inputStream = getInputStreamForIrace(RUNNER_SCRIPT, isJAR)) {
                copyWithSubstitutions(inputStream, script, Map.of());
            }
            var result = runner.execute(new RExecutionRequest(script, workingDirectory, Map.of()));
            if (!result.successful()) {
                throw new IllegalStateException(
                        "R execution failed with exit code %s. Review %s and %s"
                                .formatted(result.exitCode(), result.stdoutLog(), result.stderrLog())
                );
            }
        } catch (IOException e) {
            throw new IllegalStateException("Cannot prepare R execution", e);
        }
    }
}
