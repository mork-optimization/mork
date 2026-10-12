package es.urjc.etsii.grafo.autoconfig.autollm;

import java.nio.file.*;
import java.util.*;

public class AutoLlmCodexFixture {
    public static void main(String[] args) throws Exception {
        if (args[0].equals("login")) { System.out.println("Authenticated fixture"); return; }
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
        if (Arrays.asList(args).contains("hang-fixture")) { System.out.flush(); Thread.sleep(60_000); }
        if (Arrays.asList(args).contains("fail-fixture")) System.exit(1);
        Files.createDirectories(Path.of("src/main/java")); Files.writeString(Path.of("src/main/java/FixtureUtil.java"), "// Added by fake Codex\n");
        if (Arrays.asList(args).contains("repair-fixture")) Files.writeString(Path.of("src/main/java/Original.java"), args.length > 2 && args[1].equals("resume") ? "// original\n" : "// forbidden edit\n");
        var manifest = new LinkedHashMap<String,Object>();
        for (String field : List.of("inventory", "metaheuristics", "components", "tests", "limitations")) manifest.put(field, List.of());
        manifest.put("blacklist", null);
        int position = Arrays.asList(args).indexOf("--output-last-message");
        AutoLlmFileUtil.write(Path.of(args[position + 1]), manifest);
        System.out.println("{\"type\":\"turn.completed\",\"usage\":{\"input_tokens\":10,\"output_tokens\":10}}");
    }
}
