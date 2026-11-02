package es.urjc.etsii.grafo.autoconfig.autollm;

import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import tools.jackson.databind.JsonNode;

/** Polling timestamps and elapsed time are deliberately absent from progress identity. */
public final class EvaluationProgress {
    public record Observation(long sequence, String lastMeaningfulChange, String digest, Map<String,JsonNode> counters) {}
    private EvaluationProgress() {}
    public static synchronized Observation observe(Path snapshot, JsonNode value) {
        var counters = new LinkedHashMap<String,JsonNode>();
        for (String key : List.of("state", "phase", "budget", "evaluations", "latestEvaluationRevision", "initialCompleted", "initialTotal", "confirmationsCompleted", "confirmationsTotal", "completed", "total"))
            if (value.has(key)) counters.put(key, value.path(key));
        Path file = snapshot.resolveSibling(snapshot.getFileName() + ".observation.json");
        var previous = Files.isRegularFile(file) ? AutoLlmFileUtil.read(file, Observation.class) : null;
        String digest = AutoLlmFileUtil.canonicalHash(counters);
        if (previous != null && previous.digest().equals(digest)) return previous;
        var result = new Observation(previous == null ? 1 : previous.sequence() + 1, counters.isEmpty() ? null : Instant.now().toString(), digest, counters);
        AutoLlmFileUtil.write(file, result); return result;
    }
}
