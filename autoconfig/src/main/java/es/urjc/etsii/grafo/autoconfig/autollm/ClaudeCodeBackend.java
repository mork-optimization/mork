package es.urjc.etsii.grafo.autoconfig.autollm;

import org.springframework.core.env.Environment;
import tools.jackson.databind.JsonNode;
import java.io.*;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import static es.urjc.etsii.grafo.autoconfig.autollm.GenerationEvidence.*;

/** Claude's print-mode protocol. Credentials stay in the existing CLI configuration. */
public final class ClaudeCodeBackend implements GenerationBackend {
    private final Path home;
    private final Map<String,String> environmentOverrides;
    ClaudeCodeBackend(Environment environment) {
        String configuredHome = environment.getProperty("CLAUDE_CONFIG_DIR");
        environmentOverrides = configuredHome == null ? Map.of() : Map.of("CLAUDE_CONFIG_DIR", configuredHome);
        home = AutoLlmNormalization.localPath(Path.of(environment.getProperty("CLAUDE_CONFIG_DIR", Path.of(System.getProperty("user.home"), ".claude").toString())));
    }
    public Launch prepare(AutoLlmConfig config, Path checkout, Path output, String prompt, String sessionId) {
        try {
            if (sessionId != null) UUID.fromString(sessionId);
            var settings = Map.of("sandbox", Map.of("enabled", true, "autoAllowBashIfSandboxed", true, "allowUnsandboxedCommands", false));
            AutoLlmFileUtil.write(output.resolve("claude-settings.json"), settings);
            var args = new ArrayList<>(List.of(config.generation().executable(), "-p", "--verbose", "--output-format", "stream-json",
                    "--model", config.generation().model(), "--json-schema", Files.readString(output.resolve("schema.json")),
                    "--permission-mode", "acceptEdits", "--settings", output.resolve("claude-settings.json").toString()));
            if (sessionId != null) args.addAll(List.of("--resume", sessionId));
            if (config.generation().reasoningEffort() != null) args.addAll(List.of("--effort", config.generation().reasoningEffort()));
            args.add(prompt);
            var known = new LinkedHashSet<String>();
            if (sessionId != null) for (Path file : transcripts(home, checkout, sessionId)) known.addAll(responses(file).keySet());
            AutoLlmFileUtil.write(output.resolve("claude-before.json"), Map.of("responseIds", known, "home", home.toAbsolutePath().toString()));
            var selection = new LinkedHashMap<String,Object>();
            selection.put("requestedModel", config.generation().model()); selection.put("launchModel", config.generation().model());
            selection.put("resolvedModel", null); selection.put("resolvedProvider", null); selection.put("observation", "UNAVAILABLE");
            AutoLlmFileUtil.write(output.resolve("model-selection.json"), selection);
            return new Launch(args, environmentOverrides, Set.of("CLAUDECODE", "CLAUDE_CODE_CHILD_SESSION"));
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }
    public void refresh(Path output) { progress(output); }
    static GenerationProgressUtil.Progress progress(Path output) {
        return GenerationProgressUtil.refresh(output, true);
    }
    private static JsonNode result(Path output) {
        JsonNode result = null;
        try (var reader = AutoLlmEvidenceFiles.reader(output.resolve("stdout.log"))) {
            for (String line; (line = reader.readLine()) != null;) {
                JsonNode event;
                try { event = AutoLlmFileUtil.JSON.readTree(line); } catch (RuntimeException ignored) { continue; }
                if (event.path("type").asString("").equals("result")) result = event;
            }
        } catch (IOException e) { throw new UncheckedIOException(e); }
        return result;
    }
    public String completed(Path output, String expected, AutoLlmProcessRunner.Result process, Instant startedAt) {
        var progress = progress(output);
        if (expected != null && !expected.equals(progress.sessionId)) throw AutoLlmFailureUtil.error(AutoLlmFailureUtil.Category.INTEGRITY,
                "CONVERSATION_CHANGED", "Claude resumed a different session", output);
        var result = result(output);
        if (process.exitCode() != 0 || result == null || result.path("is_error").asBoolean() || !"success".equals(result.path("subtype").asString())
                || progress.sessionId == null || !result.hasNonNull("structured_output")) {
            String error = result == null ? "Claude exited without a terminal result" : result.toString();
            if (error.contains("rate_limit_error") || error.contains("usage_limit_reached") || error.toLowerCase(Locale.ROOT).contains("you've hit your limit")) throw new GenerationQuotaUtil.QuotaLimitException(error, Instant.now().plusSeconds(300));
            if (error.contains("overloaded_error")) throw new GenerationCapacityUtil.CapacityException(error);
            throw AutoLlmFailureUtil.error(AutoLlmFailureUtil.Category.INFRASTRUCTURE, "CLAUDE_FAILED", error, output);
        }
        if (!progress.sessionId.equals(result.path("session_id").asString())) throw new IllegalStateException("Claude result session differs from initialization");
        AutoLlmFileUtil.write(output.resolve("manifest.json"), result.get("structured_output"));
        return progress.sessionId;
    }
    public Evidence archive(Path checkout, Path output, String expected) {
        try {
            Path marker = output.resolve("evidence.json");
            if (Files.isRegularFile(marker) && "FAILED".equals(AutoLlmFileUtil.read(marker, Evidence.class).status())) {
                Path previous = output.resolve("collection-failures/" + UUID.randomUUID()); Files.createDirectories(previous);
                for (String name : List.of("evidence.json", "usage.json", "conversations")) {
                    Path file = output.resolve(name); if (Files.exists(file)) Files.move(file, previous.resolve(name));
                }
            }
            if (Files.isRegularFile(marker)) {
                var saved = AutoLlmFileUtil.read(marker, Evidence.class);
                if ("ARCHIVED".equals(saved.status())) {
                    if (expected != null && !expected.equals(saved.sessionId())) throw new IllegalStateException("Archived session differs from requested session");
                    for (var conversation : saved.conversations()) if (!conversation.sha256().equals(AutoLlmFileUtil.hash(AutoLlmFileUtil.inside(output, conversation.path())))) throw new IllegalStateException("Archived conversation changed");
                    return saved;
                }
            }
            var progress = progress(output);
            String id = progress.sessionId == null ? expected : progress.sessionId;
            if (expected != null && !expected.equals(id)) throw new IllegalStateException("Claude resumed a different session");
            if (id != null) Files.writeString(output.resolve("session-id.txt"), id);
            var conversations = new ArrayList<Conversation>(); var entries = new LinkedHashMap<String,Entry>();
            var before = AutoLlmFileUtil.read(output.resolve("claude-before.json"), JsonNode.class);
            var known = new HashSet<String>(); for (var response : before.path("responseIds")) known.add(response.asString());
            if (id != null) for (Path file : transcripts(Path.of(before.path("home").asString()), checkout, id)) {
                String name = file.getFileName().toString();
                String conversationId = name.substring(0, name.length() - 6);
                Path copy = output.resolve("conversations/" + name); Files.createDirectories(copy.getParent());
                String hash = AutoLlmFileUtil.hash(file);
                Path temporary = copy.resolveSibling(name + ".tmp"); Files.copy(file, temporary, StandardCopyOption.REPLACE_EXISTING);
                if (!hash.equals(AutoLlmFileUtil.hash(temporary)) || !hash.equals(AutoLlmFileUtil.hash(file))) throw new IllegalStateException("Claude transcript changed during archival");
                Files.move(temporary, copy, StandardCopyOption.REPLACE_EXISTING);
                var responses = responses(copy);
                for (var response : responses.entrySet()) if (!known.contains(response.getKey())) entries.put(response.getKey(), new Entry("claude:" + id + ":" + response.getKey(), conversationId, "assistant.message.usage", response.getValue()));
                conversations.add(new Conversation(conversationId, conversationId.equals(id) ? null : id, output.relativize(copy).toString(), hash, null, List.of()));
                Path operation = es.urjc.etsii.grafo.autoconfig.autollm.execution.CampaignAssetStore.campaign(checkout);
                if (operation != null && Files.exists(operation.resolve(".mork-owned-v8.json")))
                    new es.urjc.etsii.grafo.autoconfig.autollm.execution.OwnershipManifest(operation.getParent()).transcript(file, id, checkout, operation.toString());
            }
            var summary = GenerationEvidence.summary(entries.values(), false);
            var nativeResult = Files.isRegularFile(output.resolve("stdout.log")) ? result(output) : null;
            List<Map<String,Object>> reported = nativeResult == null ? List.of() : List.of(AutoLlmFileUtil.parseObject(nativeResult.toString()));
            AutoLlmFileUtil.write(output.resolve("usage.json"), new Usage(summary, List.copyOf(entries.values()), conversations, reported,
                    List.of("Reasoning token counts are unavailable. CLI cost is an estimate, not billing; resumed-session totals are not summed.")));
            var evidence = new Evidence(id == null ? "UNAVAILABLE" : "ARCHIVED", id, conversations, summary, null);
            AutoLlmFileUtil.write(marker, evidence); return evidence;
        } catch (RuntimeException | IOException e) {
            var failure = new ArchiveException("Cannot archive Claude session: " + e.getMessage(), e);
            AutoLlmFileUtil.write(output.resolve("evidence.json"), new Evidence("FAILED", expected, List.of(), GenerationEvidence.summary(List.of(), false), failure.getMessage()));
            throw failure;
        }
    }
    private List<Path> transcripts(Path nativeHome, Path checkout, String id) throws IOException {
        UUID.fromString(id);
        var matches = new ArrayList<Path>(); Path projects = nativeHome.resolve("projects");
        if (Files.isDirectory(projects)) try (var dirs = Files.newDirectoryStream(projects)) {
            for (Path dir : dirs) {
                Path file = dir.resolve(id + ".jsonl");
                if (Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)) matches.add(file);
            }
        }
        if (matches.size() != 1) throw new IOException("Expected one native transcript for exact Claude session " + id);
        Path main = matches.getFirst();
        Path agents = main.getParent().resolve(id).resolve("subagents");
        if (Files.isDirectory(agents)) try (var files = Files.newDirectoryStream(agents, "agent-*.jsonl")) { for (Path file : files) matches.add(file); }
        for (Path file : matches) {
            for (Path p = file; p != null; p = p.getParent()) if (Files.isSymbolicLink(p)) throw new IOException("Symlink in transcript path");
            verifyTranscript(file, id, checkout.toString());
        }
        return matches;
    }
    public static void verifyTranscript(Path file, String id, String cwd) throws IOException {
        boolean found = false;
        try (var reader = Files.newBufferedReader(file)) {
            for (String line; (line = reader.readLine()) != null;) {
                var event = AutoLlmFileUtil.JSON.readTree(line);
                if (!event.hasNonNull("sessionId")) continue;
                if (!id.equals(event.path("sessionId").asString()) || (event.hasNonNull("cwd") && !Path.of(cwd).toRealPath().equals(Path.of(event.path("cwd").asString()).toRealPath()))) throw new IOException("Claude transcript ownership mismatch");
                found |= event.hasNonNull("cwd");
            }
        }
        if (!found) throw new IOException("Claude transcript lacks session/workspace identity");
    }
    private static Map<String,Tokens> responses(Path file) throws IOException {
        var responses = new LinkedHashMap<String,Tokens>();
        try (var reader = Files.newBufferedReader(file)) {
            for (String line; (line = reader.readLine()) != null;) {
                var message = AutoLlmFileUtil.JSON.readTree(line).path("message");
                if (!message.hasNonNull("id") || !message.hasNonNull("usage")) continue;
                var usage = message.path("usage");
                Long input = count(usage, "input_tokens"), output = count(usage, "output_tokens"), cached = count(usage, "cache_read_input_tokens"), write = count(usage, "cache_creation_input_tokens");
                Long total = input == null || output == null || cached == null || write == null ? null : Math.addExact(Math.addExact(input, output), Math.addExact(cached, write));
                responses.put(message.path("id").asString(), new Tokens(input, cached, write, output, null, total));
            }
        }
        return responses;
    }
    private static Long count(JsonNode usage, String key) {
        var value = usage.get(key); if (value == null || value.isNull()) return null;
        if (!value.isIntegralNumber() || !value.canConvertToLong() || value.longValue() < 0) throw new IllegalStateException("Invalid token count");
        return value.longValue();
    }
    static String observe(Path output, JsonNode event, GenerationProgressUtil.Progress progress) {
        String type = event.path("type").asString("");
        if (event.hasNonNull("session_id")) {
            String id = event.path("session_id").asString();
            if (progress.sessionId != null && !progress.sessionId.equals(id)) throw new IllegalStateException("Claude emitted multiple session IDs");
            progress.sessionId = id;
            AutoLlmFileUtil.write(output.resolve("session-id.json"), Map.of("sessionId", id));
        }
        if (type.equals("system") && event.path("subtype").asString("").equals("init")) {
            progress.state = "SESSION_READY"; progress.turns++;
            Path selectionFile = output.resolve("model-selection.json");
            if (Files.isRegularFile(selectionFile)) {
                var selection = AutoLlmFileUtil.read(selectionFile, Map.class);
                selection.put("resolvedModel", event.path("model").stringValue());
                selection.put("observation", event.hasNonNull("model") ? "PARTIAL" : "UNAVAILABLE");
                AutoLlmFileUtil.write(selectionFile, selection);
            }
            AutoLlmFileUtil.write(output.resolve("execution/readiness.json"), Map.of("startedAtMillis", System.currentTimeMillis()));
        } else if (type.equals("assistant")) {
            progress.messages++; progress.state = "ACTIVE";
            for (var block : event.path("message").path("content")) if ("tool_use".equals(block.path("type").asString(""))) {
                String name = block.path("name").asString("");
                if (name.equals("Bash") && progress.pendingCommands.add(block.path("id").asString())) progress.commandsStarted++;
                if (name.equals("Edit") || name.equals("Write") || name.equals("NotebookEdit")) progress.edits++;
            }
        } else if (type.equals("user")) {
            for (var block : event.path("message").path("content")) if ("tool_result".equals(block.path("type").asString(""))
                    && progress.pendingCommands.remove(block.path("tool_use_id").asString())) progress.commandsCompleted++;
        }
        else if (type.equals("result")) { progress.state = event.path("is_error").asBoolean() ? "FAILED" : "COMPLETED"; progress.usage = event.get("usage"); }
        else if (!type.equals("user")) return null;
        return type.equals("assistant") ? event.path("message").path("content").toString() : type;
    }
}
