package es.urjc.etsii.grafo.autoconfig.irace;

import es.urjc.etsii.grafo.autoconfig.controller.dto.EliteConfiguration;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Reads the final elite configurations written by the bundled IRACE runner. */
public final class IraceFinalElitesUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private IraceFinalElitesUtil() {
    }

    public static List<EliteConfiguration> read(Path file) throws IOException {
        if (!Files.isRegularFile(file)) {
            throw new IllegalStateException("IRACE final elites file does not exist: " + file);
        }
        var parsed = OBJECT_MAPPER.readValue(Files.readString(file), FinalElitesFile.class);
        if (parsed.elites() == null || parsed.elites().isEmpty()) {
            throw new IllegalStateException("IRACE completed without final elite configurations in " + file);
        }
        return List.copyOf(parsed.elites());
    }

    private record FinalElitesFile(List<EliteConfiguration> elites) {
    }
}
