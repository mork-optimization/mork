package es.urjc.etsii.grafo.autoconfig.autollm;

import java.nio.file.*;
import java.util.*;

public class AutoLlmCodexFixture {
    public static final Map<String, String> BASELINE_SOURCES = Map.of(
            "src/main/java/ProjectExplorationFilter.java", "class ProjectExplorationFilter { boolean reject(int depth) { return false; } }\n",
            "src/main/java/ProjectSolution.java", "class ProjectSolution {}\n");
    public static final Map<String, String> MODIFIED_SOURCES = Map.of(
            "src/main/java/ProjectExplorationFilter.java", "class ProjectExplorationFilter { boolean reject(int depth) { return depth > 2; } }\n",
            "src/main/java/ProjectSolution.java", "class ProjectSolution { int state; }\n");
    public static void main(String[] args) throws Exception {
        if (args[0].equals("login")) { System.err.println("Not logged in"); System.exit(1); }
        if (Arrays.asList(args).contains("--version")) { System.out.println("fake-codex-test"); return; }
        String thread = UUID.randomUUID().toString();
        if (args.length > 2 && args[1].equals("resume")) thread = args[2];
        System.out.println("{\"type\":\"thread.started\",\"thread_id\":\"" + thread + "\"}");
        if (!(Arrays.asList(args).contains("missing-archive-fixture") && Path.of("").toAbsolutePath().toString().contains("trials/001/"))) {
            Path session = Path.of(System.getenv("CODEX_HOME"), "sessions", "rollout-" + thread + ".jsonl");
            if (!Files.exists(session)) AutoLlmFileUtil.append(session, Map.of("type", "session_meta", "payload", Map.of("id", thread, "cwd", Path.of("").toAbsolutePath().toString(), "cli_version", "fixture-1")));
            AutoLlmFileUtil.append(session, Map.of("type", "turn_context", "payload", Map.of("model", "fixture-model")));
            AutoLlmFileUtil.append(session, Map.of("type", "response_item", "payload", Map.of("type", "message", "role", "user", "content", List.of(Map.of("type", "input_text", "text", args[args.length - 1])))));
            AutoLlmFileUtil.append(session, Map.of("type", "token_usage_record", "payload", Map.of("response_id", UUID.randomUUID().toString(), "usage", Map.of("input_tokens", 10, "cached_input_tokens", 2, "cache_write_input_tokens", 0, "output_tokens", 10, "reasoning_output_tokens", 4, "total_tokens", 20))));
        }
        System.out.println("{\"type\":\"fixture.ready\"}");
        var arguments = Arrays.asList(args);
        Path quotaMarker = Path.of(".quota-emitted");
        boolean repeatedQuota = arguments.contains("quota-repeat-fixture") && Path.of("").toAbsolutePath().toString().contains("trials/001/");
        int quotaCount = repeatedQuota && Files.exists(quotaMarker) ? Integer.parseInt(Files.readString(quotaMarker).strip()) : 0;
        boolean quotaFixture = arguments.contains("quota-fixture")
                || repeatedQuota && quotaCount < 4
                || (arguments.contains("quota-generation-fixture") && !args[1].equals("resume")
                    || arguments.contains("quota-repair-fixture") && args[1].equals("resume"))
                    && Path.of("").toAbsolutePath().toString().contains("trials/001/") && !Files.exists(Path.of(".quota-emitted"));
        if (quotaFixture) {
            Files.writeString(quotaMarker, Integer.toString(quotaCount + 1));
            AutoLlmFileUtil.append(Path.of(System.getenv("CODEX_HOME"), "sessions", "rollout-" + thread + ".jsonl"),
                    Map.of("type", "event_msg", "payload", Map.of("type", "token_count", "rate_limits",
                            Map.of("limit_id", "codex", "primary", Map.of("used_percent", 100,
                                    "resets_at", java.time.Instant.now().plusSeconds(3600).getEpochSecond())))));
            String message = "You've hit your usage limit.";
            System.out.println(AutoLlmFileUtil.JSON.writeValueAsString(Map.of("type", "error", "message", message)));
            System.out.println(AutoLlmFileUtil.JSON.writeValueAsString(Map.of("type", "turn.failed", "error", Map.of("message", message))));
            System.exit(1);
        }
        if (Arrays.asList(args).contains("hang-fixture")) { System.out.flush(); Thread.sleep(60_000); }
        if (Arrays.asList(args).contains("fail-fixture")) System.exit(1);
        Files.createDirectories(Path.of("src/main/java")); Files.writeString(Path.of("src/main/java/FixtureUtil.java"), "public final class FixtureUtil {}\n");
        if ((arguments.contains("repair-fixture") || arguments.contains("quota-repair-fixture")) && !args[1].equals("resume")) {
            Files.writeString(Path.of("src/main/java/FixtureUtil.java"), "public final class FixtureUtil { invalid }\n");
        }
        for (var entry : MODIFIED_SOURCES.entrySet()) {
            if (Files.exists(Path.of(entry.getKey()))) Files.writeString(Path.of(entry.getKey()), entry.getValue());
        }
        var manifest = new LinkedHashMap<String,Object>();
        for (String field : List.of("inventory", "metaheuristics", "components", "tests", "limitations")) manifest.put(field, List.of());
        manifest.put("blacklist", null);
        int position = Arrays.asList(args).indexOf("--output-last-message");
        AutoLlmFileUtil.write(Path.of(args[position + 1]), manifest);
        System.out.println("{\"type\":\"turn.completed\",\"usage\":{\"input_tokens\":10,\"output_tokens\":10}}");
    }
}
