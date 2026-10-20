package es.urjc.etsii.grafo.util;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Shared input discovery for applications, screening and workload staging. */
public final class InstancePathUtil {
    private InstancePathUtil() {}

    public static String resolve(Path parent, String token) {
        int split = token.indexOf('!');
        String container = split < 0 ? token : token.substring(0, split);
        return parent.resolve(container).toAbsolutePath().normalize() + (split < 0 ? "" : token.substring(split));
    }

    public static List<String> list(String input) {
        Path path = Path.of(input).toAbsolutePath().normalize();
        var result = new ArrayList<String>();
        if (Files.isRegularFile(path) && input.endsWith(".index")) {
            try {
                for (String line : Files.readAllLines(path, StandardCharsets.UTF_8)) {
                    line = line.replaceFirst("^\\uFEFF", "").trim();
                    if (!line.isBlank() && !line.startsWith("#")) result.add(IOUtil.checkLoadPathExists(resolve(path.getParent(), line)));
                }
            } catch (IOException e) { throw new UncheckedIOException(e); }
        } else {
            for (String token : IOUtil.iterate(path)) if (!token.endsWith(".index")) result.add(token);
        }
        return result;
    }
}
