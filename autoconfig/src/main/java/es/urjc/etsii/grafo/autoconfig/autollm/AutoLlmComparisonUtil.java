package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.algorithms.multistart.MultiStartAlgorithm;
import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.util.Context;
import es.urjc.etsii.grafo.util.StatisticsUtil;
import es.urjc.etsii.grafo.algorithms.FMode;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.function.Consumer;
import tools.jackson.core.type.TypeReference;

public final class AutoLlmComparisonUtil {
    private AutoLlmComparisonUtil() {}
    public static <S extends Solution<S,I>, I extends Instance> Algorithm<S,I> restart(String name, Algorithm<S,I> algorithm) {
        return new MultiStartAlgorithm<>(name, Context.getMainObjective(), algorithm, Integer.MAX_VALUE / 2, Integer.MAX_VALUE / 2, Integer.MAX_VALUE / 2);
    }
    private static final int PAGE_BYTES = 4 * 1024 * 1024;
    public record Page(long nextCursor, boolean more, int omittedEvents) {}

    /** Only complete JSONL rows are committed. Large/malformed rows remain available in the raw journal. */
    public static Page events(Path file, long cursor, int maximumEvents, Consumer<Map<String,Object>> consumer) {
        if (cursor < 0) throw new IllegalArgumentException("Cursor must be nonnegative");
        try (var input = new RandomAccessFile(file.toFile(), "r")) {
            long size = input.length();
            if (cursor > size) cursor = 0;
            input.seek(cursor);
            byte[] batch = new byte[(int)Math.min(PAGE_BYTES, size - cursor)]; input.readFully(batch);
            int start = 0, events = 0, omitted = 0;
            for (int end = 0; end < batch.length; end++) if (batch[end] == '\n') {
                Map<String,Object> event = null;
                try { event = AutoLlmFileUtil.JSON.readValue(Arrays.copyOfRange(batch, start, end), new TypeReference<>() {}); }
                catch (tools.jackson.core.JacksonException e) { omitted++; }
                start = end + 1;
                if (event != null) consumer.accept(event);
                if (++events >= maximumEvents) break;
            }
            // Bound oversized events too, instead of repeatedly reading the same page forever.
            if (start == 0 && batch.length == PAGE_BYTES) { start = batch.length; omitted++; }
            long next = cursor + start;
            return new Page(next, next < size, omitted);
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }

    public static final class Cache {
        private final LinkedHashMap<Path,Progress> entries = new LinkedHashMap<>(8, .75f, true);
        public synchronized Object summary(Path file) {
            try {
                var attributes = Files.readAttributes(file, BasicFileAttributes.class);
                var progress = entries.get(file);
                if (progress == null || progress.cursor > attributes.size() || !Objects.equals(progress.fileKey, attributes.fileKey())) {
                    progress = new Progress(attributes.fileKey()); entries.put(file, progress);
                    if (entries.size() > 8) entries.remove(entries.keySet().iterator().next());
                }
                Page page = events(file, progress.cursor, Integer.MAX_VALUE, progress::accept);
                progress.cursor = page.nextCursor(); progress.omitted += page.omittedEvents();
                var objectives = new TreeMap<String,Object>();
                for (var algorithm : progress.scores.entrySet()) {
                    var scores = new TreeMap<String,Object>();
                    for (var objective : algorithm.getValue().entrySet()) {
                        var definition = Context.getObjectivesW().get(objective.getKey());
                        scores.put(objective.getKey(), objective.getValue().summary(definition == null ? FMode.MINIMIZE : definition.getFMode()));
                    }
                    objectives.put(algorithm.getKey(), scores);
                }
                var times = new TreeMap<String,Object>(); var best = new TreeMap<String,Object>();
                for (var entry : progress.timing.entrySet()) times.put(entry.getKey(), entry.getValue().summary(FMode.MINIMIZE));
                for (var entry : progress.bestTime.entrySet()) best.put(entry.getKey(), entry.getValue().summary(FMode.MINIMIZE));
                var counts = new TreeMap<String,Object>();
                for (var entry : progress.counts.entrySet()) counts.put(entry.getKey(), Map.copyOf(entry.getValue()));
                return Map.of("counts", counts, "objectives", objectives, "executionTimeMillis", times, "timeToBestMillis", best,
                        "cursor", progress.cursor, "more", page.more(), "omittedEvents", progress.omitted);
            } catch (IOException e) { throw new UncheckedIOException(e); }
        }
    }
    private static final class Progress {
        final Object fileKey;
        long cursor; int omitted;
        final Map<String,Map<String,Integer>> counts = new TreeMap<>();
        final Map<String,Map<String,StatisticsUtil.OnlineAccumulator>> scores = new TreeMap<>();
        final Map<String,StatisticsUtil.OnlineAccumulator> timing = new TreeMap<>(), bestTime = new TreeMap<>();
        Progress(Object fileKey) { this.fileKey = fileKey; }
        void accept(Map<String,Object> event) {
            if (!"evaluation-result".equals(event.get("kind")) || !Boolean.TRUE.equals(event.get("measured"))) return;
            String algorithm = (String)event.get("algorithm");
            counts.computeIfAbsent(algorithm, _ -> new TreeMap<>()).merge((String)event.get("state"), 1, Integer::sum);
            if (!"SUCCEEDED".equals(event.get("state"))) return;
            timing.computeIfAbsent(algorithm, _ -> new StatisticsUtil.OnlineAccumulator()).add(((Number)event.get("executionTimeNanos")).doubleValue() / 1_000_000);
            bestTime.computeIfAbsent(algorithm, _ -> new StatisticsUtil.OnlineAccumulator()).add(((Number)event.get("timeToBestNanos")).doubleValue() / 1_000_000);
            for (var entry : AutoLlmFileUtil.objectMap(event.get("objectives")).entrySet()) if (entry.getValue() instanceof Number value)
                scores.computeIfAbsent(algorithm, _ -> new TreeMap<>()).computeIfAbsent(entry.getKey(), _ -> new StatisticsUtil.OnlineAccumulator()).add(value.doubleValue());
        }
    }
    public static Object curves(Path file, long cursor) {
        var rows = new ArrayList<Object>();
        Page page = events(file, cursor, 16, event -> {
            if (!"evaluation-result".equals(event.get("kind")) || !Boolean.TRUE.equals(event.get("measured")) || !"SUCCEEDED".equals(event.get("state"))) return;
            var curves = new LinkedHashMap<String,Object>();
            for (var entry : AutoLlmFileUtil.objectMap(event.get("curves")).entrySet()) if (entry.getValue() instanceof List<?> points) {
                var sampled = new ArrayList<Object>();
                int count = Math.min(256, points.size());
                for (int i = 0; i < count; i++) sampled.add(points.get(count == 1 ? 0 : (int)((long)i * (points.size() - 1) / (count - 1))));
                curves.put(entry.getKey(), sampled);
            }
            rows.add(Map.of("instance", event.get("instance"), "instanceId", event.get("instanceId"), "algorithm", event.get("algorithm"), "iteration", event.get("iteration"), "curves", curves));
        });
        return Map.of("curves", rows, "nextCursor", page.nextCursor(), "more", page.more(), "omittedEvents", page.omittedEvents(), "sampled", true);
    }
}
