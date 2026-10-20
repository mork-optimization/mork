package es.urjc.etsii.grafo.autoconfig.autollm;

import org.springframework.stereotype.Service;
import org.springframework.core.env.Environment;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.util.*;
import java.util.function.BooleanSupplier;

@Service
public class CodexTaskRunner {
    public record Manifest(List<String> inventory, List<String> metaheuristics, List<String> components,
                           String blacklist, List<String> tests, List<String> limitations) {}
    public record Result(String threadId, Manifest manifest) {}
    private final AutoLlmProcessRunner processes;
    private final Path codexHome;
    public CodexTaskRunner(AutoLlmProcessRunner processes, Environment environment) {
        this.processes = processes;
        this.codexHome = Path.of(environment.getProperty("CODEX_HOME", Path.of(System.getProperty("user.home"), ".codex").toString()));
    }

    public CodexEvidenceUtil.Evidence archive(Path checkout, Path output, String threadId) {
        return CodexEvidenceUtil.collect(codexHome, checkout, output, threadId);
    }

    public Result run(AutoLlmConfig config, Path checkout, Path output, String prompt, String threadId, BooleanSupplier cancelled) {
        try {
            Files.createDirectories(output);
            try (var input = getClass().getResourceAsStream("/autollm/source-instructions.txt")) {
                prompt += "\n\n" + new String(Objects.requireNonNull(input).readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            }
            Files.writeString(output.resolve("prompt.txt"), prompt);
            var properties = new LinkedHashMap<String, Object>();
            for (String field : List.of("inventory", "metaheuristics", "components", "tests", "limitations")) properties.put(field, Map.of("type", "array", "items", Map.of("type", "string")));
            properties.put("blacklist", Map.of("type", List.of("string", "null")));
            AutoLlmFileUtil.write(output.resolve("schema.json"), Map.of("type", "object", "properties", properties, "required", properties.keySet(), "additionalProperties", false));
            var args = new ArrayList<String>(List.of(config.codex().executable(), "exec"));
            if (threadId != null) { args.add("resume"); args.add(threadId); }
            else { args.add("--sandbox"); args.add("workspace-write"); }
            args.addAll(List.of("--json", "--output-schema", output.resolve("schema.json").toString(), "--output-last-message", output.resolve("manifest.json").toString()));
            if (config.codex().model() != null && !config.codex().model().isBlank()) args.addAll(List.of("--model", config.codex().model()));
            if (config.codex().reasoningEffort() != null && !config.codex().reasoningEffort().isBlank()) args.addAll(List.of("-c", "model_reasoning_effort=\"" + config.codex().reasoningEffort() + "\""));
            args.add(prompt);
            CodexEvidenceUtil.begin(codexHome, checkout, output, threadId);
            AutoLlmFileUtil.write(output.resolve("codex-launch.json"), Map.of("startedAt", java.time.Instant.now().toString()));
            AutoLlmProcessRunner.Result result;
            RuntimeException processFailure = null;
            try {
                result = processes.run(args, checkout, output, config.execution().codexTimeoutMillis(), 0, config.execution().watchdogGraceMillis(), cancelled, false, config.execution().startupTimeoutMillis());
            } catch (RuntimeException e) { processFailure = e; throw e; }
            finally {
                try { archive(checkout, output, threadId); }
                catch (CodexEvidenceUtil.ArchiveException e) { if (processFailure != null) e.addSuppressed(processFailure); throw e; }
            }
            var events = CodexEvidenceUtil.cli(output.resolve("stdout.log"));
            String actualThread = events.threadId() == null ? threadId : events.threadId();
            if (actualThread != null) Files.writeString(output.resolve("thread-id.txt"), actualThread);
            if (threadId != null && !threadId.equals(actualThread)) throw AutoLlmFailureUtil.error(AutoLlmFailureUtil.Category.INTEGRITY, "CONVERSATION_CHANGED", "Codex resumed a different conversation", output);
            if (result.exitCode() != 0 || !events.completed() || actualThread == null || !Files.isRegularFile(output.resolve("manifest.json"))) throw AutoLlmFailureUtil.error(AutoLlmFailureUtil.Category.INFRASTRUCTURE, "CODEX_FAILED", "Codex task failed; inspect generation logs", output);
            var manifest = AutoLlmFileUtil.read(output.resolve("manifest.json"), Manifest.class);
            if (manifest.inventory() == null || manifest.metaheuristics() == null || manifest.components() == null || manifest.tests() == null || manifest.limitations() == null) throw new IllegalStateException("Incomplete Codex manifest");
            return new Result(actualThread, manifest);
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }
}
