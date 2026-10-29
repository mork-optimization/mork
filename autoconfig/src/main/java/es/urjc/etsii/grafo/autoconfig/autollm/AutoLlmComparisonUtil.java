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
import tools.jackson.core.type.TypeReference;

public final class AutoLlmComparisonUtil {
    private AutoLlmComparisonUtil() {}
    public static <S extends Solution<S,I>, I extends Instance> Algorithm<S,I> restart(String name, Algorithm<S,I> algorithm) {
        return new MultiStartAlgorithm<>(name, Context.getMainObjective(), algorithm, Integer.MAX_VALUE / 2, Integer.MAX_VALUE / 2, Integer.MAX_VALUE / 2);
    }
    private static final int MAX_EVENT_BYTES = 4 * 1024 * 1024;
    private record Location(long offset, int length) {}
    public record InstanceSummary(String instanceId, String instance, Map<String,Map<String,Integer>> counts) {}

    public static final class Cache {
        private final LinkedHashMap<Path,Progress> entries = new LinkedHashMap<>(8, .75f, true);

        private Progress refresh(Path file) throws IOException {
            var attributes = Files.readAttributes(file, BasicFileAttributes.class);
            var progress = entries.get(file);
            if (progress == null || progress.size > attributes.size() || !Objects.equals(progress.fileKey, attributes.fileKey())
                    || (progress.size == attributes.size() && !attributes.lastModifiedTime().equals(progress.modified))) {
                progress = new Progress(attributes.fileKey()); entries.put(file, progress);
                if (entries.size() > 8) entries.remove(entries.keySet().iterator().next());
            }
            // Snapshot the length: an actively growing journal must not keep this request running forever.
            try (var input = new BufferedInputStream(Files.newInputStream(file))) {
                input.skipNBytes(progress.cursor);
                var row = new ByteArrayOutputStream();
                long start = progress.cursor;
                for (long offset = start; offset < attributes.size(); offset++) {
                    int value = input.read();
                    if (value == -1) break;
                    if (value != '\n') {
                        if (row.size() <= MAX_EVENT_BYTES) row.write(value);
                        continue;
                    }
                    if (row.size() > MAX_EVENT_BYTES) progress.omitted++;
                    else {
                        Map<String,Object> event = null;
                        try { event = AutoLlmFileUtil.JSON.readValue(row.toByteArray(), new TypeReference<>() {}); }
                        catch (tools.jackson.core.JacksonException e) { progress.omitted++; }
                        if (event != null) progress.accept(event, new Location(start, row.size()));
                    }
                    progress.cursor = offset + 1; start = progress.cursor; row.reset();
                }
                // An incomplete trailing row stays uncommitted until its newline arrives.
            }
            progress.size = attributes.size(); progress.modified = attributes.lastModifiedTime();
            return progress;
        }

        public synchronized Object summary(Path file) {
            try {
                var progress = refresh(file);
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
                var instances = new ArrayList<InstanceSummary>();
                for (var entry : progress.instances.entrySet()) {
                    var instance = entry.getValue();
                    var instanceCounts = new TreeMap<String,Map<String,Integer>>();
                    for (var algorithm : instance.counts.entrySet()) instanceCounts.put(algorithm.getKey(), Map.copyOf(algorithm.getValue()));
                    instances.add(new InstanceSummary(entry.getKey(), instance.path, instanceCounts));
                }
                return Map.of("counts", counts, "objectives", objectives, "executionTimeMillis", times, "timeToBestMillis", best,
                        "instances", instances, "omittedEvents", progress.omitted);
            } catch (IOException e) { throw new UncheckedIOException(e); }
        }
        public synchronized Object curves(Path file, String instanceId) {
            try {
                var progress = refresh(file);
                var instance = progress.instances.get(instanceId);
                var rows = new ArrayList<Object>();
                if (instance != null) try (var input = new RandomAccessFile(file.toFile(), "r")) {
                    for (var location : instance.curves) {
                        byte[] bytes = new byte[location.length()];
                        input.seek(location.offset()); input.readFully(bytes);
                        Map<String,Object> event = AutoLlmFileUtil.JSON.readValue(bytes, new TypeReference<>() {});
                        rows.add(sample(event));
                    }
                }
                return Map.of("curves", rows, "sampled", true);
            } catch (IOException e) { throw new UncheckedIOException(e); }
        }
    }
    private static final class InstanceProgress {
        final String path;
        final Map<String,Map<String,Integer>> counts = new TreeMap<>();
        final List<Location> curves = new ArrayList<>();
        InstanceProgress(String path) { this.path = path; }
    }
    private static final class Progress {
        final Object fileKey;
        long cursor, size; int omitted;
        java.nio.file.attribute.FileTime modified;
        final Map<String,InstanceProgress> instances = new LinkedHashMap<>();
        final Map<String,Map<String,Integer>> counts = new TreeMap<>();
        final Map<String,Map<String,StatisticsUtil.OnlineAccumulator>> scores = new TreeMap<>();
        final Map<String,StatisticsUtil.OnlineAccumulator> timing = new TreeMap<>(), bestTime = new TreeMap<>();
        Progress(Object fileKey) { this.fileKey = fileKey; }
        void accept(Map<String,Object> event, Location location) {
            if (!"evaluation-result".equals(event.get("kind")) || !Boolean.TRUE.equals(event.get("measured"))) return;
            String algorithm = (String)event.get("algorithm");
            counts.computeIfAbsent(algorithm, _ -> new TreeMap<>()).merge((String)event.get("state"), 1, Integer::sum);
            var instance = instances.computeIfAbsent((String)event.get("instanceId"), _ -> new InstanceProgress((String)event.get("instance")));
            instance.counts.computeIfAbsent(algorithm, _ -> new TreeMap<>()).merge((String)event.get("state"), 1, Integer::sum);
            if (!"SUCCEEDED".equals(event.get("state"))) return;
            instance.curves.add(location);
            timing.computeIfAbsent(algorithm, _ -> new StatisticsUtil.OnlineAccumulator()).add(((Number)event.get("executionTimeNanos")).doubleValue() / 1_000_000);
            bestTime.computeIfAbsent(algorithm, _ -> new StatisticsUtil.OnlineAccumulator()).add(((Number)event.get("timeToBestNanos")).doubleValue() / 1_000_000);
            for (var entry : AutoLlmFileUtil.objectMap(event.get("objectives")).entrySet()) if (entry.getValue() instanceof Number value)
                scores.computeIfAbsent(algorithm, _ -> new TreeMap<>()).computeIfAbsent(entry.getKey(), _ -> new StatisticsUtil.OnlineAccumulator()).add(value.doubleValue());
        }
    }
    private static Object sample(Map<String,Object> event) {
        var curves = new LinkedHashMap<String,Object>();
        for (var entry : AutoLlmFileUtil.objectMap(event.get("curves")).entrySet()) if (entry.getValue() instanceof List<?> points) {
            var sampled = new ArrayList<Object>();
            int count = Math.min(256, points.size());
            for (int i = 0; i < count; i++) sampled.add(points.get(count == 1 ? 0 : (int)((long)i * (points.size() - 1) / (count - 1))));
            curves.put(entry.getKey(), sampled);
        }
        return Map.of("instance", event.get("instance"), "instanceId", event.get("instanceId"), "algorithm", event.get("algorithm"), "iteration", event.get("iteration"), "curves", curves);
    }
}
