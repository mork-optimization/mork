package es.urjc.etsii.grafo.autoconfig.autollm;

import org.springframework.stereotype.Service;
import org.springframework.core.env.Environment;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.*;
import java.util.*;
import java.util.function.BooleanSupplier;

@Service
public class GenerationTaskRunner {
    public record GeneratedComponent(String className, String role, String description) {}
    public record Manifest(List<String> inventory, List<String> metaheuristics, List<String> components,
                           String blacklist, List<String> tests, List<String> limitations, List<GeneratedComponent> generatedComponents) {}
    public record Result(String sessionId, Manifest manifest) {}
    private final AutoLlmProcessRunner processes;
    private final GenerationBackend codexBackend;
    private final GenerationBackend claudeBackend;
    public GenerationTaskRunner(AutoLlmProcessRunner processes, Environment environment) {
        this.processes = processes;
        this.codexBackend = new CodexBackend(environment);
        this.claudeBackend = new ClaudeCodeBackend(environment);
    }

    public GenerationEvidence.Evidence archive(Path checkout, Path output, String sessionId) {
        return backend(output).archive(checkout, output, sessionId);
    }

    public Result run(AutoLlmConfig config, Path checkout, Path output, String prompt, String sessionId, BooleanSupplier cancelled) {
        try {
            Files.createDirectories(output);
            try (var input = getClass().getResourceAsStream("/autollm/source-instructions.txt")) {
                prompt += "\n\n" + new String(Objects.requireNonNull(input).readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
            }
            Files.writeString(output.resolve("prompt.txt"), prompt);
            var properties = new LinkedHashMap<String, Object>();
            for (String field : List.of("inventory", "metaheuristics", "components", "tests", "limitations")) properties.put(field, Map.of("type", "array", "items", Map.of("type", "string")));
            properties.put("blacklist", Map.of("type", List.of("string", "null")));
            properties.put("generatedComponents", Map.of("type", "array", "items", Map.of("type", "object",
                    "properties", Map.of("className", Map.of("type", "string"), "role", Map.of("type", "string"), "description", Map.of("type", "string")),
                    "required", List.of("className", "role", "description"), "additionalProperties", false)));
            AutoLlmFileUtil.write(output.resolve("schema.json"), Map.of("type", "object", "properties", properties, "required", properties.keySet(), "additionalProperties", false));
            var startedAt = java.time.Instant.now();
            AutoLlmFileUtil.write(output.resolve("generation-launch.json"), Map.of("startedAt", startedAt.toString(), "agent", config.generation().agent()));
            var backend = backend(output);
            var launch = backend.prepare(config, checkout, output, prompt, sessionId);
            AutoLlmProcessRunner.Result result;
            RuntimeException processFailure = null;
            try {
                result = processes.run(launch.command(), checkout, output, config.execution().generationTimeoutMillis(), 0, config.execution().watchdogGraceMillis(), cancelled, false, config.execution().startupTimeoutMillis(), launch.environment(), launch.removeEnvironment(), () -> backend.refresh(output));
                AutoLlmFileUtil.write(output.resolve("process-result.json"), result);
            } catch (RuntimeException e) { processFailure = e; throw e; }
            finally {
                try { archive(checkout, output, sessionId); }
                catch (GenerationEvidence.ArchiveException e) { if (processFailure != null) e.addSuppressed(processFailure); throw e; }
            }
            return completedResult(output, sessionId, result, startedAt);
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }
    /** Read a completed turn after evidence recovery, without launching a new generation turn. */
    public Result recoveredResult(Path output, String sessionId) {
        Path result = output.resolve("process-result.json");
        if (!Files.isRegularFile(result)) return null;
        var process = AutoLlmFileUtil.read(result, AutoLlmProcessRunner.Result.class);
        if (process.exitCode() != 0) return null;
        return completedResult(output, sessionId, process, java.time.Instant.parse(
                AutoLlmFileUtil.JSON.readTree(read(output.resolve("generation-launch.json"))).path("startedAt").asString()));
    }
    private GenerationBackend backend(Path output) {
        if (!Files.isRegularFile(output.resolve("generation-launch.json"))) throw new GenerationEvidence.ArchiveException("Generation launch identity is missing", null);
        var agent = AutoLlmFileUtil.JSON.readTree(read(output.resolve("generation-launch.json"))).path("agent").asString();
        return switch (AutoLlmConfig.Agent.valueOf(agent)) { case CODEX -> codexBackend; case CLAUDE_CODE -> claudeBackend; };
    }
    private static String read(Path file) {
        try { return Files.readString(file); } catch (IOException e) { throw new UncheckedIOException(e); }
    }
    private Result completedResult(Path output, String sessionId, AutoLlmProcessRunner.Result result, java.time.Instant startedAt) {
        String actualThread = backend(output).completed(output, sessionId, result, startedAt);
        var manifest = AutoLlmFileUtil.read(output.resolve("manifest.json"), Manifest.class);
        if (manifest.inventory() == null || manifest.metaheuristics() == null || manifest.components() == null || manifest.tests() == null || manifest.limitations() == null) throw new IllegalStateException("Incomplete generation manifest");
        if (manifest.generatedComponents() == null) throw new IllegalStateException("Generated component catalog is missing");
        var names = new HashSet<String>();
        for (var component : manifest.generatedComponents()) {
            if (component.className() == null || component.className().isBlank() || component.role() == null || component.role().isBlank()
                    || component.description() == null || component.description().isBlank() || !names.add(component.className()))
                throw new IllegalStateException("Generated components require unique class names, roles and descriptions");
        }
        return new Result(actualThread, manifest);
    }
}
