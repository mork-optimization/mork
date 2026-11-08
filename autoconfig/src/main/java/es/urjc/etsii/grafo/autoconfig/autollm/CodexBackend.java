package es.urjc.etsii.grafo.autoconfig.autollm;

import org.springframework.core.env.Environment;
import java.io.*;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;

final class CodexBackend implements GenerationBackend {
    private final Path codexHome;
    CodexBackend(Environment environment) {
        codexHome = Path.of(environment.getProperty("CODEX_HOME", Path.of(System.getProperty("user.home"), ".codex").toString()));
    }
    public Launch prepare(AutoLlmConfig config, Path checkout, Path output, String prompt, String sessionId) {
            var previousSelection = CodexEvidenceUtil.selection(codexHome, checkout, sessionId);
            String requestedModel = config.generation().model();
            String launchModel = requestedModel == null || requestedModel.isBlank() ? previousSelection.model() : requestedModel;
            var selection = new LinkedHashMap<String,Object>();
            selection.put("requestedModel", requestedModel); selection.put("launchModel", launchModel);
            selection.put("launchProvider", previousSelection.provider());
            selection.put("resolvedModel", null); selection.put("resolvedProvider", null);
            selection.put("observation", "UNAVAILABLE");
            AutoLlmFileUtil.write(output.resolve("model-selection.json"), selection);
            var args = new ArrayList<String>(List.of(config.generation().executable(), "exec"));
            if (sessionId != null) { args.add("resume"); args.add(sessionId); }
            else { args.add("--sandbox"); args.add("workspace-write"); }
            args.addAll(List.of("--json", "--output-schema", output.resolve("schema.json").toString(), "--output-last-message", output.resolve("manifest.json").toString()));
            if (launchModel != null && !launchModel.isBlank()) args.addAll(List.of("--model", launchModel));
            if (previousSelection.provider() != null) args.addAll(List.of("-c", "model_provider=" + AutoLlmFileUtil.JSON.writeValueAsString(previousSelection.provider())));
            if (config.generation().reasoningEffort() != null && !config.generation().reasoningEffort().isBlank()) args.addAll(List.of("-c", "model_reasoning_effort=\"" + config.generation().reasoningEffort() + "\""));
            args.add(prompt);
            CodexEvidenceUtil.begin(codexHome, checkout, output, sessionId);
        return new Launch(args, Map.of(), Set.of());
    }
    public void refresh(Path output) { GenerationProgressUtil.refresh(output); }
    public GenerationEvidence.Evidence archive(Path checkout, Path output, String sessionId) {
        var evidence = CodexEvidenceUtil.collect(codexHome, checkout, output, sessionId);
        try {
            var selection = Files.isRegularFile(output.resolve("model-selection.json")) ? AutoLlmFileUtil.read(output.resolve("model-selection.json"), Map.class) : new LinkedHashMap<String,Object>();
            for (var conversation : evidence.conversations()) if (conversation.sessionId().equals(evidence.sessionId())) {
                var observed = CodexEvidenceUtil.selection(AutoLlmFileUtil.inside(output, conversation.path()));
                selection.put("resolvedModel", observed.model()); selection.put("resolvedProvider", observed.provider());
                selection.put("observation", observed.model() != null && observed.provider() != null ? "COMPLETE" : "PARTIAL");
            }
            AutoLlmFileUtil.write(output.resolve("model-selection.json"), selection);
        } catch (IOException e) { throw new UncheckedIOException(e); }
        return evidence;
    }
    public String completed(Path output, String sessionId, AutoLlmProcessRunner.Result result, Instant startedAt) {
        try {
            var events = CodexEvidenceUtil.cli(output.resolve("stdout.log"));
            String actualThread = events.sessionId() == null ? sessionId : events.sessionId();
            if (actualThread != null) Files.writeString(output.resolve("session-id.txt"), actualThread);
            if (sessionId != null && !sessionId.equals(actualThread)) throw AutoLlmFailureUtil.error(AutoLlmFailureUtil.Category.INTEGRITY, "CONVERSATION_CHANGED", "Codex resumed a different conversation", output);
            if (result.exitCode() != 0 || !events.completed() || actualThread == null || !Files.isRegularFile(output.resolve("manifest.json"))) {
                var quota = GenerationQuotaUtil.detect(output, startedAt, java.time.Instant.now(), java.time.ZoneId.systemDefault());
                if (quota != null) throw quota;
                var capacity = GenerationCapacityUtil.detect(output);
                if (capacity != null) throw capacity;
                throw AutoLlmFailureUtil.error(AutoLlmFailureUtil.Category.INFRASTRUCTURE, "CODEX_FAILED", "Codex task failed; inspect generation logs", output);
            }
            return actualThread;
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }
    static String observe(Path output, tools.jackson.databind.JsonNode event, GenerationProgressUtil.Progress progress) {
        String type = event.path("type").asString(""); String description = type;
        if (type.equals("thread.started")) {
            progress.sessionId = event.path("thread_id").asString(); progress.state = "SESSION_READY";
            AutoLlmFileUtil.write(output.resolve("session-id.json"), Map.of("sessionId", progress.sessionId));
            AutoLlmFileUtil.write(output.resolve("execution/readiness.json"), Map.of("startedAtMillis", System.currentTimeMillis()));
        } else if (type.equals("turn.started")) { progress.turns++; progress.state = "ACTIVE"; }
        else if (type.startsWith("item.")) {
            progress.state = "ACTIVE";
            var item = event.path("item"); String kind = item.path("type").asString("");
            description = kind;
            if (kind.equals("command_execution")) {
                if (type.equals("item.started")) progress.commandsStarted++;
                if (type.equals("item.completed")) progress.commandsCompleted++;
                description += ": " + item.path("command").asString("") + "\n" + item.path("aggregated_output").asString("");
            } else if (kind.equals("agent_message")) {
                if (type.equals("item.completed")) progress.messages++;
                description = item.path("text").asString("");
            } else if (kind.equals("file_change")) {
                if (type.equals("item.completed")) progress.edits++;
                description += ": " + item.path("changes");
            } else description += ": " + item;
        } else if (type.equals("turn.completed")) { progress.state = "COMPLETED"; progress.usage = event.get("usage"); }
        else if (type.equals("turn.failed") || type.equals("error")) { progress.state = "FAILED"; description = event.toString(); }
        else return null;
        return description;
    }
}
