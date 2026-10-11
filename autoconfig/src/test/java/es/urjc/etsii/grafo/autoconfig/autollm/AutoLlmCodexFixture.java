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
