package es.urjc.etsii.grafo.autoconfig.irace;

import es.urjc.etsii.grafo.autoconfig.controller.dto.EliteConfiguration;
import es.urjc.etsii.grafo.autoconfig.r.RLangRunner;
import es.urjc.etsii.grafo.autoconfig.r.RExecutionRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
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
    public static final String FINAL_ELITES_FILE = "autoconfig-final-elites.json";
    public static final String FINAL_ELITES_TEMP_FILE = FINAL_ELITES_FILE + ".tmp";

    private final RLangRunner runner;
    private final ObjectMapper objectMapper = new ObjectMapper();

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
    public List<EliteConfiguration> runIrace(boolean isJAR, Map<String, String> substitutions){
        Path workingDirectory = Path.of("").toAbsolutePath().normalize();
        Path script = workingDirectory.resolve(RUNNER_SCRIPT);
        Path finalElites = workingDirectory.resolve(FINAL_ELITES_FILE);
        Path temporaryFinalElites = workingDirectory.resolve(FINAL_ELITES_TEMP_FILE);
        try {
            Files.deleteIfExists(finalElites);
            Files.deleteIfExists(temporaryFinalElites);
            try (var inputStream = getInputStreamForIrace(RUNNER_SCRIPT, isJAR)) {
                copyWithSubstitutions(inputStream, script, substitutions);
            }
            var result = runner.execute(new RExecutionRequest(script, workingDirectory, Map.of()));
            if (!result.successful()) {
                throw new IllegalStateException(
                        "R execution failed with exit code %s. Review %s and %s"
                                .formatted(result.exitCode(), result.stdoutLog(), result.stderrLog())
                );
            }
            return readFinalElites(finalElites);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot prepare R execution", e);
        } finally {
            deleteGeneratedFile(finalElites);
            deleteGeneratedFile(temporaryFinalElites);
        }
    }

    private static void deleteGeneratedFile(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("Cannot delete generated IRACE file {}", path, e);
        }
    }

    List<EliteConfiguration> readFinalElites(Path finalElites) throws IOException {
        if (!Files.isRegularFile(finalElites)) {
            throw new IllegalStateException("IRACE completed without producing " + FINAL_ELITES_FILE);
        }
        var parsed = objectMapper.readValue(Files.readString(finalElites), FinalElitesFile.class);
        if (parsed.elites() == null || parsed.elites().isEmpty()) {
            throw new IllegalStateException("IRACE completed without final elite configurations");
        }
        return List.copyOf(parsed.elites());
    }

    private record FinalElitesFile(List<EliteConfiguration> elites) {
    }
}
