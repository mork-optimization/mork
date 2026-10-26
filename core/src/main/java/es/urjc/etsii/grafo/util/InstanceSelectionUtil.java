package es.urjc.etsii.grafo.util;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.io.InstanceManager;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.ToLongFunction;

/** Shared reference-instance selection and property export. */
public final class InstanceSelectionUtil {
    private InstanceSelectionUtil() {}

    /** Choose the fastest-loading candidate, breaking ties by absolute load path. */
    public static String selectFastestLoadingInstance(List<String> paths, ToLongFunction<String> loadTimeNanos) {
        if (paths.isEmpty()) throw new IllegalArgumentException("Cannot select a reference instance from empty inputs");
        String selected = null;
        long fastest = Long.MAX_VALUE;
        for (String path : paths) {
            long nanos = loadTimeNanos.applyAsLong(path);
            if (selected == null || nanos < fastest || nanos == fastest
                    && IOUtil.absoluteLoadPath(path).compareTo(IOUtil.absoluteLoadPath(selected)) < 0) {
                selected = path;
                fastest = nanos;
            }
        }
        return selected;
    }

    public static <I extends Instance> void writeProperties(Path target, List<String> paths, InstanceManager<I> manager) {
        var rows = new ArrayList<Map<String,Object>>();
        var keys = new TreeSet<String>();
        for (String path : paths) {
            I instance = manager.getInstance(path);
            var row = new TreeMap<String,Object>();
            row.put("id", instance.getId()); row.put("path", IOUtil.absoluteLoadPath(path));
            for (String key : Instance.getUniquePropertiesKeys()) {
                Object value = instance.getPropertyOrDefault(key, null);
                if (!key.equals(Instance.LOAD_TIME_NANOS) && value instanceof Number number && Double.isFinite(number.doubleValue())) {
                    keys.add(key); row.put(key, number);
                }
            }
            rows.add(row);
        }
        var columns = new ArrayList<>(List.of("id", "path")); columns.addAll(keys);
        try {
            Files.createDirectories(target.toAbsolutePath().getParent());
            try (var writer = Files.newBufferedWriter(target)) {
                writeRow(writer, columns);
                for (var row : rows) {
                    var values = new ArrayList<Object>();
                    for (String key : columns) values.add(row.getOrDefault(key, ""));
                    writeRow(writer, values);
                }
            }
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }
    public static void writeRow(Writer writer, List<?> values) throws IOException {
        boolean first = true;
        for (Object value : values) {
            if (!first) writer.write(','); first = false;
            writer.write('"'); writer.write(value == null ? "" : String.valueOf(value).replace("\"", "\"\"")); writer.write('"');
        }
        writer.write('\n');
    }
}
