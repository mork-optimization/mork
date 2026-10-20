package es.urjc.etsii.grafo.autoconfig.screening;

import es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmFileUtil;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;

public final class ScreeningProgressUtil {
    private ScreeningProgressUtil() {}
    public static void update(Path output, String phase, Map<String,?> counts) {
        Path file = output.resolve("screening-progress.json");
        var value = Files.exists(file) ? new LinkedHashMap<>(AutoLlmFileUtil.parseObject(read(file))) : new LinkedHashMap<String,Object>();
        value.putAll(counts); value.put("phase", phase); value.put("updatedAt", Instant.now().toString()); AutoLlmFileUtil.write(file, value);
    }
    private static String read(Path file) { try { return Files.readString(file); } catch (java.io.IOException e) { throw new java.io.UncheckedIOException(e); } }
}
