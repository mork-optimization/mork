package es.urjc.etsii.grafo.autoconfig.autollm.execution;

import es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmFileUtil;
import es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmCheckOrchestrator;
import es.urjc.etsii.grafo.util.InstancePathUtil;
import java.nio.file.*;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

public final class RemoteWorkloadFixture {
    private RemoteWorkloadFixture() {}
    public static void main(String[] args) throws Exception {
        Path output = Path.of(args[1]); Files.createDirectories(output.resolve("execution"));
        AutoLlmFileUtil.write(output.resolve("execution/readiness.json"), Map.of("startedAtMillis", System.currentTimeMillis()));
        Files.writeString(output.resolve("literal.txt"), args.length > 2 ? args[2] : "literal");
        if (args[0].equals("solutions")) {
            Files.createDirectories(output.resolve("solutions"));
            Files.writeString(output.resolve("solutions/first.json"), "{\"cost\":1}");
            Files.writeString(output.resolve("solutions/second.json"), "{\"cost\":2}");
        }
        if (args[0].equals("inputs")) {
            var tokens = InstancePathUtil.list(args[2]);
            if (args.length > 4) {
                var instances = new es.urjc.etsii.grafo.config.InstanceConfiguration();
                instances.setPath(Map.of("reference", args[3]));
                es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmInputUtil.configureReference(instances, Path.of(args[4]));
                String reference = instances.getPaths().get("reference");
                if (!tokens.contains(reference)) throw new IllegalStateException("Reference was not mapped to a configured input");
                AutoLlmFileUtil.write(output.resolve("reference.json"), Map.of("instance", reference));
            }
            var hashes = new LinkedHashMap<String,String>();
            for (String token : tokens) hashes.put(token, AutoLlmFileUtil.inputHash(token));
            AutoLlmFileUtil.write(output.resolve("input-hashes.json"), hashes);
            AutoLlmFileUtil.write(output.resolve("check.json"), new AutoLlmCheckOrchestrator.Check(1, List.of(), tokens, tokens));
        }
        if (args[0].equals("hang")) {
            AutoLlmFileUtil.write(output.resolve("execution/active.json"), Map.of("hung", Map.of("deadline", System.currentTimeMillis() + 100, "phase", "RUNNING")));
            while (true) Thread.sleep(100);
        }
        if (args[0].equals("wait")) Thread.sleep(20000);
        System.out.println("fixture complete");
        AutoLlmFileUtil.write(output.resolve("execution/terminal.json"), Map.of("outcome", "SUCCEEDED"));
    }
}
