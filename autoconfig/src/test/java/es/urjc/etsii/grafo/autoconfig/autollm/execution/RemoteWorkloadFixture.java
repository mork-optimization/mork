package es.urjc.etsii.grafo.autoconfig.autollm.execution;

import es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmFileUtil;
import java.nio.file.*;
import java.util.Map;

public final class RemoteWorkloadFixture {
    private RemoteWorkloadFixture() {}
    public static void main(String[] args) throws Exception {
        Path output = Path.of(args[1]); Files.createDirectories(output.resolve("execution"));
        AutoLlmFileUtil.write(output.resolve("execution/readiness.json"), Map.of("startedAtMillis", System.currentTimeMillis()));
        Files.writeString(output.resolve("literal.txt"), args.length > 2 ? args[2] : "literal");
        if (args[0].equals("hang")) {
            AutoLlmFileUtil.write(output.resolve("execution/active.json"), Map.of("hung", Map.of("deadline", System.currentTimeMillis() + 100, "phase", "RUNNING")));
            while (true) Thread.sleep(100);
        }
        if (args[0].equals("wait")) Thread.sleep(20000);
        System.out.println("fixture complete");
        AutoLlmFileUtil.write(output.resolve("execution/terminal.json"), Map.of("outcome", "SUCCEEDED"));
    }
}
