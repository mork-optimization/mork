package es.urjc.etsii.grafo.autoconfig.autollm;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import static es.urjc.etsii.grafo.autoconfig.autollm.GenerationEvidence.*;

/** Archives only the selected native Codex session and its explicitly linked descendants. */
public final class CodexEvidenceUtil {
    private CodexEvidenceUtil() {}

    public record Before(String codexHome, boolean known, Map<String, Set<String>> responseIds, Map<String,Tokens> counters) {}
    public record Cli(String sessionId, boolean completed, List<Map<String,Object>> usage) {}
    private record Session(Path path, String id, String parent, String cwd, String version) {}
    private static class Parsed {
        final Map<String, Tokens> responses = new LinkedHashMap<>();
        final Set<String> models = new LinkedHashSet<>();
        final List<String> notes = new ArrayList<>();
        Tokens counter;
        boolean counterInvalid;
    }
    public static void begin(Path home, Path checkout, Path output, String sessionId) {
        var responses = new LinkedHashMap<String, Set<String>>();
        var counters = new LinkedHashMap<String, Tokens>();
        try {
            if (sessionId != null) for (var session : sessions(home, checkout, sessionId)) {
                var parsed = parse(session.path());
                responses.put(session.id(), parsed.responses.keySet());
                if (parsed.counter != null && !parsed.counterInvalid) counters.put(session.id(), parsed.counter);
            }
            AutoLlmFileUtil.write(output.resolve("evidence-before.json"), new Before(home.toAbsolutePath().normalize().toString(), true, responses, counters));
        } catch (RuntimeException | IOException e) { throw failure(output, sessionId, List.of(), "Cannot establish Codex conversation archive baseline", e); }
    }

    /** Called after the owned process tree stops, including during interrupted-attempt recovery. */
    public static Evidence collect(Path home, Path checkout, Path output, String expectedThread) {
        Path marker = output.resolve("evidence.json");
        if (Files.isRegularFile(marker) && !"ARCHIVED".equals(AutoLlmFileUtil.read(marker, Evidence.class).status())) {
            // A failed collection is evidence of an attempt, not a sealed archive.
            Path previous = output.resolve("collection-failures/" + UUID.randomUUID());
            try {
                Files.createDirectories(previous);
                for (String name : List.of("evidence.json", "usage.json", "conversations")) {
                    Path file = output.resolve(name);
                    if (Files.exists(file)) Files.move(file, previous.resolve(name));
                }
            } catch (IOException e) { throw new ArchiveException("Cannot preserve previous collection failure", e); }
        }
        if (Files.isRegularFile(marker)) {
            try {
                var evidence = AutoLlmFileUtil.read(marker, Evidence.class);
                if (expectedThread != null && !expectedThread.equals(evidence.sessionId())) throw new ArchiveException("Archived Codex thread differs from the requested thread", null);
                for (var conversation : evidence.conversations()) {
                    if (!conversation.sha256().equals(AutoLlmFileUtil.hash(AutoLlmFileUtil.inside(output, conversation.path())))) throw new ArchiveException("Archived Codex conversation changed", null);
                }
                return evidence;
            } catch (RuntimeException e) { throw new ArchiveException("Cannot verify completed Codex archive", e); }
        }
        String thread = expectedThread;
        var cli = new ArrayList<Map<String,Object>>();
        try {
            Files.createDirectories(output);
            var reported = cli(output.resolve("stdout.log")); cli.addAll(reported.usage());
            if (reported.sessionId() != null) {
                if (expectedThread != null && !expectedThread.equals(reported.sessionId())) throw new IllegalStateException("Codex resumed a different conversation");
                thread = reported.sessionId();
            }
            // A CLI failure before creating a conversation has no native conversation to archive.
            if (thread == null) {
                var usage = new Usage(GenerationEvidence.summary(List.of(), false), List.of(), List.of(), cli, List.of("Codex did not report a conversation ID"));
                AutoLlmFileUtil.write(output.resolve("usage.json"), usage);
                var evidence = new Evidence("UNAVAILABLE", null, List.of(), usage.summary(), null);
                AutoLlmFileUtil.write(marker, evidence); return evidence;
            }
            Files.writeString(output.resolve("session-id.txt"), thread);
            var before = Files.isRegularFile(output.resolve("evidence-before.json")) ? AutoLlmFileUtil.read(output.resolve("evidence-before.json"), Before.class)
                    : new Before(home.toString(), false, Map.of(), Map.of());
            var conversations = new ArrayList<Conversation>();
            var entries = new LinkedHashMap<String,Entry>();
            var notes = new ArrayList<String>();
            var knownResponses = new HashSet<String>();
            for (var ids : before.responseIds().values()) knownResponses.addAll(ids);
            boolean complete = before.known();
            if (!before.known()) notes.add("Pre-attempt baseline is absent; token deltas are unavailable");
            for (var session : sessions(Path.of(before.codexHome()), checkout, thread)) {
                Path operation = es.urjc.etsii.grafo.autoconfig.autollm.execution.CampaignAssetStore.campaign(checkout);
                if (operation != null && Files.exists(operation.resolve(".mork-owned-v8.json"))) {
                    if (session.cwd() == null || !Path.of(session.cwd()).toAbsolutePath().normalize().startsWith(operation)) throw new ArchiveException("Native session workspace ownership is ambiguous", null);
                    new es.urjc.etsii.grafo.autoconfig.autollm.execution.OwnershipManifest(operation.getParent()).transcript(session.path(), session.id(), Path.of(session.cwd()), operation.toString());
                }
                Path copy = output.resolve("conversations/" + session.id() + ".jsonl");
                Files.createDirectories(copy.getParent());
                String hash = AutoLlmFileUtil.hash(session.path());
                if (!Files.exists(copy)) {
                    Path temporary = copy.resolveSibling(copy.getFileName() + ".tmp");
                    Files.copy(session.path(), temporary, StandardCopyOption.REPLACE_EXISTING);
                    if (!hash.equals(AutoLlmFileUtil.hash(temporary)) || !hash.equals(AutoLlmFileUtil.hash(session.path()))) throw new IllegalStateException("Codex session changed while archiving: " + session.id());
                    Files.move(temporary, copy);
                }
                if (!hash.equals(AutoLlmFileUtil.hash(copy))) throw new IllegalStateException("Incomplete existing Codex archive: " + session.id());
                var parsed = parse(copy);
                conversations.add(new Conversation(session.id(), session.parent(), output.relativize(copy).toString(), hash, session.version(), List.copyOf(parsed.models)));
                notes.addAll(parsed.notes); complete &= parsed.notes.isEmpty();
                if (!before.known()) continue;
                boolean newResponses = false;
                if (!parsed.responses.isEmpty()) {
                    for (var response : parsed.responses.entrySet()) {
                        if (knownResponses.contains(response.getKey())) continue;
                        newResponses = true;
                        String key = "response:" + response.getKey();
                        var entry = new Entry(key, session.id(), "token_usage_record.usage", response.getValue());
                        var previous = entries.putIfAbsent(key, entry);
                        if (previous != null && !previous.tokens().equals(entry.tokens())) { complete = false; notes.add("Conflicting usage for " + key); }
                    }
                }
                if (!newResponses && parsed.counter != null && !parsed.counterInvalid) {
                    var delta = delta(parsed.counter, before.counters().get(session.id()));
                    if (delta != null) entries.put("counter:" + session.id() + ":" + parsed.counter, new Entry("counter:" + session.id() + ":" + parsed.counter, session.id(), "event_msg.token_count delta", delta));
                    else { complete = false; notes.add("Non-monotonic counters: " + session.id()); }
                } else if (!newResponses && parsed.responses.isEmpty()) { complete = false; notes.add("Native usage is unavailable: " + session.id()); }
            }
            var usage = new Usage(GenerationEvidence.summary(entries.values(), complete), List.copyOf(entries.values()), conversations, cli, notes);
            AutoLlmFileUtil.write(output.resolve("usage.json"), usage);
            var evidence = new Evidence("ARCHIVED", thread, conversations, usage.summary(), null);
            AutoLlmFileUtil.write(marker, evidence); return evidence;
        } catch (RuntimeException | IOException e) {
            throw failure(output, thread, cli, "Required Codex conversation could not be archived: " + e.getMessage(), e);
        }
    }
    /** CLI tool events can be large; retain only control events while streaming the log. */
    public static Cli cli(Path file) {
        String thread = null; boolean completed = false;
        var usage = new ArrayList<Map<String,Object>>();
        if (!AutoLlmEvidenceFiles.exists(file)) return new Cli(null, false, usage);
        try (var reader = AutoLlmEvidenceFiles.reader(file)) {
            for (String line; (line = reader.readLine()) != null;) {
                Map<String,Object> event;
                try { event = AutoLlmFileUtil.parseObject(line); } catch (RuntimeException e) { continue; /* Interrupted output remains in the raw log. */ }
                if ("thread.started".equals(event.get("type")) && event.get("thread_id") instanceof String actual) {
                    if (thread != null && !thread.equals(actual)) throw new IllegalStateException("Codex emitted multiple conversation IDs");
                    thread = actual;
                }
                if ("turn.completed".equals(event.get("type"))) {
                    completed = true;
                    if (event.get("usage") instanceof Map<?,?> reported) usage.add(AutoLlmFileUtil.objectMap(reported));
                }
            }
            return new Cli(thread, completed, usage);
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }
    private static ArchiveException failure(Path output, String thread, List<Map<String,Object>> cli, String message, Throwable cause) {
        var error = new ArchiveException(message, cause);
        try {
            var usage = new Usage(GenerationEvidence.summary(List.of(), false), List.of(), List.of(), cli, List.of(message));
            AutoLlmFileUtil.write(output.resolve("usage.json"), usage);
            AutoLlmFileUtil.write(output.resolve("evidence.json"), new Evidence("FAILED", thread, List.of(), usage.summary(), message));
        } catch (RuntimeException e) { error.addSuppressed(e); }
        return error;
    }

    private static List<Session> sessions(Path home, Path checkout, String sessionId) throws IOException {
        if (!UUID.fromString(sessionId).toString().equalsIgnoreCase(sessionId)) throw new IllegalStateException("Invalid Codex thread ID");
        var all = new LinkedHashMap<String,Session>();
        var duplicates = new HashSet<String>();
        for (String directory : List.of("sessions", "archived_sessions")) {
            Path root = home.resolve(directory);
            if (!Files.isDirectory(root)) continue;
            try (var paths = Files.walk(root)) {
                for (var iterator = paths.iterator(); iterator.hasNext();) {
                    Path file = iterator.next();
                    if (Files.isSymbolicLink(file) || !Files.isRegularFile(file) || !file.getFileName().toString().endsWith(".jsonl")) continue;
                    Session session = metadata(file);
                    if (session != null) {
                        var prior = all.putIfAbsent(session.id(), session);
                        if (prior != null) duplicates.add(session.id());
                    }
                }
            }
        }
        var main = all.get(sessionId);
        if (main == null) throw new IllegalStateException("Native JSONL session not found for exact thread " + sessionId);
        if (main.cwd() == null || !sameDirectory(Path.of(main.cwd()), checkout)) throw new IllegalStateException("Codex session workspace does not match the trial");
        var selected = new LinkedHashMap<String, Session>(); selected.put(sessionId, main);
        boolean added;
        do {
            added = false;
            for (var session : all.values()) if (!selected.containsKey(session.id()) && selected.containsKey(session.parent())) { selected.put(session.id(), session); added = true; }
        } while (added);
        for (String id : selected.keySet()) if (duplicates.contains(id)) throw new IllegalStateException("Ambiguous native session ID: " + id);
        return List.copyOf(selected.values());
    }
    private static boolean sameDirectory(Path first, Path second) throws IOException {
        return first.toAbsolutePath().normalize().equals(second.toAbsolutePath().normalize()) || Files.exists(first) && Files.exists(second) && Files.isSameFile(first, second);
    }
    private static Session metadata(Path file) throws IOException {
        try (var reader = AutoLlmEvidenceFiles.reader(file)) {
            String line = reader.readLine(); if (line == null) return null;
            Map<String,Object> event;
            try { event = AutoLlmFileUtil.parseObject(line); } catch (RuntimeException e) { return null; }
            if (!"session_meta".equals(event.get("type")) || !(event.get("payload") instanceof Map<?,?> payload) || !(payload.get("id") instanceof String id)) return null;
            try { if (!UUID.fromString(id).toString().equalsIgnoreCase(id)) return null; } catch (IllegalArgumentException e) { return null; }
            return new Session(file, id, parent(payload.get("source")), (String) payload.get("cwd"), (String) payload.get("cli_version"));
        }
    }
    public record Selection(String model, String provider) {}
    public static Selection selection(Path home, Path checkout, String thread) {
        if (thread == null) return new Selection(null, null);
        try {
            for (var session : sessions(home, checkout, thread)) if (session.id().equals(thread)) return selection(session.path());
            return new Selection(null, null);
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }
    static Selection selection(Path transcript) throws IOException {
        String model = null, provider = null;
        try (var reader = AutoLlmEvidenceFiles.reader(transcript)) {
            for (String line; (line = reader.readLine()) != null;) {
                Map<String,Object> event;
                try { event = AutoLlmFileUtil.parseObject(line); } catch (RuntimeException ignored) { continue; }
                if (!(event.get("payload") instanceof Map<?,?> payload)) continue;
                if ("session_meta".equals(event.get("type")) && payload.get("model_provider") instanceof String value) provider = value;
                if ("turn_context".equals(event.get("type"))) {
                    if (payload.get("model") instanceof String value) model = value;
                    if (payload.get("model_provider") instanceof String value) provider = value;
                }
            }
        }
        return new Selection(model, provider);
    }
    private static String parent(Object source) {
        if (source instanceof Map<?,?> map) {
            for (String key : List.of("parent_thread_id", "parentSessionId")) if (map.get(key) instanceof String id) return id;
            for (var value : map.values()) { String id = parent(value); if (id != null) return id; }
        }
        return null;
    }
    private static Parsed parse(Path file) throws IOException {
        var parsed = new Parsed();
        try (var reader = AutoLlmEvidenceFiles.reader(file)) {
            int number = 0;
            for (String line; (line = reader.readLine()) != null;) {
                number++;
                try {
                    Map<String,Object> event = AutoLlmFileUtil.parseObject(line);
                    if (!(event.get("payload") instanceof Map<?,?> payload)) continue;
                    if ("turn_context".equals(event.get("type")) && payload.get("model") instanceof String model) parsed.models.add(model);
                    if ("token_usage_record".equals(event.get("type"))) {
                        if (!(payload.get("response_id") instanceof String id) || !(payload.get("usage") instanceof Map<?,?> usage)) { parsed.notes.add("Unsupported usage record at line " + number); continue; }
                        Tokens counts = tokens(usage);
                        var previous = parsed.responses.putIfAbsent(id, counts);
                        if (previous != null && !previous.equals(counts)) parsed.notes.add("Conflicting response usage at line " + number);
                    }
                    if ("event_msg".equals(event.get("type")) && "token_count".equals(payload.get("type")) && payload.get("info") instanceof Map<?,?> info && info.get("total_token_usage") instanceof Map<?,?> total) {
                        var counts = tokens(total);
                        if (parsed.counter != null && delta(counts, parsed.counter) == null) parsed.counterInvalid = true;
                        parsed.counter = counts;
                    }
                } catch (RuntimeException e) { parsed.notes.add("Unreadable native record at line " + number); }
            }
        }
        if (parsed.counterInvalid) parsed.notes.add("Native cumulative usage counters decreased");
        return parsed;
    }
    private static Tokens tokens(Map<?,?> usage) {
        return new Tokens(count(usage, "input_tokens"), count(usage, "cached_input_tokens"), count(usage, "cache_write_input_tokens"), count(usage, "output_tokens"), count(usage, "reasoning_output_tokens"), count(usage, "total_tokens"));
    }
    private static Long count(Map<?,?> usage, String key) {
        Object value = usage.get(key);
        if (value == null) return null;
        if (!(value instanceof Number number) || number.longValue() < 0 || number.doubleValue() != number.longValue()) throw new IllegalStateException("Invalid token counter: " + key);
        return number.longValue();
    }
    private static List<Long> fields(Tokens counts) { return Arrays.asList(counts.inputTokens(), counts.cachedInputTokens(), counts.cacheWriteInputTokens(), counts.outputTokens(), counts.reasoningOutputTokens(), counts.totalTokens()); }
    private static Tokens from(List<Long> values) { return new Tokens(values.get(0), values.get(1), values.get(2), values.get(3), values.get(4), values.get(5)); }
    private static Tokens delta(Tokens after, Tokens before) {
        var end = fields(after); var start = before == null ? Arrays.asList(0L,0L,0L,0L,0L,0L) : fields(before);
        var result = new ArrayList<Long>();
        for (int i = 0; i < end.size(); i++) {
            if (end.get(i) == null || start.get(i) == null) result.add(null);
            else { if (end.get(i) < start.get(i)) return null; result.add(end.get(i) - start.get(i)); }
        }
        return from(result);
    }
}
