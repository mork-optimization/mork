package es.urjc.etsii.grafo.autoconfig.autollm;

import java.nio.file.Path;
import java.util.*;
import java.util.function.UnaryOperator;

/** Snapshot and path operations shared by the coordinator, runners, and reporting. */
public final class AutoLlmPoolUtil {
    private AutoLlmPoolUtil() {}
    public static Map<String, Object> remote(Map<String, Object> value, String prefix) {
        if (value == null) return null;
        var result = new LinkedHashMap<>(value);
        if (result.get("stageDirectory") instanceof String directory) result.put("stageDirectory", prefix + "/" + directory);
        return result;
    }
    public static UnaryOperator<String> localInstances(AutoLlmCampaign campaign, Path root, AutoLlmCampaign.Trial trial) {
        var target = campaign.targets.get(trial.targetId);
        if (target == null) throw new IllegalStateException("Trial target unavailable");
        Path executionRoot = root.resolve(target.directory);
        return es.urjc.etsii.grafo.autoconfig.autollm.execution.AutoLlmExecutionService.localPaths(target.resolvedConfig, executionRoot);
    }
    public static Map<String, Object> identity(String campaignId, String targetId) {
        return Map.of("campaignId", campaignId, "targetId", targetId);
    }
    public static List<String> inputIdentities(String index) {
        var result = new ArrayList<String>();
        for (String input : es.urjc.etsii.grafo.util.InstancePathUtil.list(index)) {
            try (var stream = es.urjc.etsii.grafo.util.IOUtil.getInputStream(input)) {
                result.add(AutoLlmFileUtil.hash(stream));
            } catch (java.io.IOException e) { throw new java.io.UncheckedIOException(e); }
        }
        return result;
    }
    public static void requireId(String id) {
        if (id == null || !id.matches("[A-Za-z0-9][A-Za-z0-9_-]{0,63}")) throw new IllegalArgumentException("Target ID must contain 1–64 letters, numbers, underscores or hyphens");
    }
}
