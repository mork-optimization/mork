package es.urjc.etsii.grafo.autoconfig.autollm;

import java.nio.file.*;
import java.util.*;

public class AutoLlmClaudeFixture {
    public static void main(String[] args) throws Exception {
        var arguments = Arrays.asList(args);
        String mode = arguments.get(arguments.indexOf("--model") + 1);
        String id = arguments.contains("--resume") ? arguments.get(arguments.indexOf("--resume") + 1) : UUID.randomUUID().toString();
        Path cwd = Path.of("").toRealPath();
        Path transcript = Path.of(System.getenv("CLAUDE_CONFIG_DIR"), "projects", "fixture", id + ".jsonl");
        Files.createDirectories(transcript.getParent());
        AutoLlmFileUtil.append(transcript, Map.of("type", "assistant", "sessionId", id, "cwd", cwd.toString(), "message",
                Map.of("id", UUID.randomUUID().toString(), "usage", Map.of("input_tokens", 10, "output_tokens", 5, "cache_read_input_tokens", 2, "cache_creation_input_tokens", 3))));
        System.out.println(AutoLlmFileUtil.JSON.writeValueAsString(Map.of("type", "system", "subtype", "init", "session_id", id, "model", mode)));
        System.out.flush();
        if (mode.equals("hang")) { Thread.sleep(60000); return; }
        var result = new LinkedHashMap<String,Object>(); result.put("type", "result"); result.put("subtype", "success"); result.put("is_error", false); result.put("session_id", id);
        result.put("structured_output", new GenerationTaskRunner.Manifest(List.of(), List.of(), List.of(), null, List.of(), List.of(), List.of()));
        if (mode.equals("missing")) result.remove("structured_output");
        if (mode.equals("mismatch")) result.put("session_id", UUID.randomUUID().toString());
        if (mode.equals("quota") || mode.equals("capacity")) { result.put("is_error", true); result.put("errors", List.of(mode.equals("quota") ? "rate_limit_error" : "overloaded_error")); }
        System.out.println(AutoLlmFileUtil.JSON.writeValueAsString(result));
    }
}
