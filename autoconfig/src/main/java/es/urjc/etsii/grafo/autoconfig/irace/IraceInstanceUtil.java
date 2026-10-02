package es.urjc.etsii.grafo.autoconfig.irace;

import es.urjc.etsii.grafo.util.IOUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Writes instance load paths for irace's native training manifest parser. */
public final class IraceInstanceUtil {

    private IraceInstanceUtil() {
    }

    /**
     * Write absolute load paths without copying or importing the instance data.
     *
     * @param manifest destination file
     * @param loadPaths selected regular or archive-entry load paths
     * @throws IOException if the manifest cannot be written
     * @throws IllegalArgumentException if the selection is empty or a path cannot be represented by irace
     */
    public static void write(Path manifest, List<String> loadPaths) throws IOException {
        if (loadPaths.isEmpty()) {
            throw new IllegalArgumentException("No training instances configured for irace");
        }
        var absolutePaths = new ArrayList<String>(loadPaths.size());
        for (String loadPath : loadPaths) {
            String absolutePath = IOUtil.absoluteLoadPath(loadPath);
            if (absolutePath.indexOf('#') >= 0 || absolutePath.indexOf('\r') >= 0 || absolutePath.indexOf('\n') >= 0) {
                throw new IllegalArgumentException(
                        "Irace training instance paths cannot contain '#' or line breaks: " + absolutePath);
            }
            absolutePaths.add(absolutePath);
        }
        Files.write(manifest, absolutePaths, StandardCharsets.UTF_8);
    }
}
