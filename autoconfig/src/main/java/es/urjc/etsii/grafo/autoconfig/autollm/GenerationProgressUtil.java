package es.urjc.etsii.grafo.autoconfig.autollm;

import java.io.*;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;

/** Incremental CLI observations. Counters describe visible events, not hidden model iterations. */
public final class GenerationProgressUtil {
    private static final int MAX_EVENT_BYTES = 8 * 1024 * 1024;
    public static class Progress {
        public long cursor;
        public boolean discarding;
        public String state = "LAUNCHING";
        public String sessionId;
        public String lastActivityAt;
        public int turns;
        public int commandsStarted;
        public int commandsCompleted;
        public Set<String> pendingCommands = new HashSet<>();
        public int edits;
        public int messages;
        public Object usage;
    }
    private GenerationProgressUtil() {}
    public static synchronized Progress refresh(Path output) {
        var launch = output.resolve("generation-launch.json");
        return refresh(output, Files.isRegularFile(launch) && "CLAUDE_CODE".equals(AutoLlmFileUtil.read(launch, Map.class).get("agent")));
    }
    static synchronized Progress refresh(Path output, boolean claude) {
        Path snapshot = output.resolve("generation-progress.json"), log = output.resolve("stdout.log");
        var progress = Files.exists(snapshot) ? AutoLlmFileUtil.read(snapshot, Progress.class) : new Progress();
        if (AutoLlmEvidenceFiles.archived(log) || !Files.isRegularFile(log)) return progress;
        boolean changed = false;
        try (var input = new RandomAccessFile(log.toFile(), "r")) {
            if (progress.cursor > input.length()) throw new IllegalStateException("Codex event stream was truncated");
            input.seek(progress.cursor);
            byte[] batch = new byte[(int)Math.min(16L * 1024 * 1024, input.length() - progress.cursor)]; input.readFully(batch);
            long base = progress.cursor; int start = 0;
            for (int end = 0; end < batch.length; end++) {
                if (batch[end] != '\n') continue;
                byte[] bytes = end - start <= MAX_EVENT_BYTES ? Arrays.copyOfRange(batch, start, end) : null;
                start = end + 1; progress.cursor = base + start; changed = true;
                if (progress.discarding || bytes == null) {
                    progress.discarding = false; activity(output, "oversized-event", "Event exceeds the display limit; inspect the raw log.", progress); continue;
                }
                try {
                    var event = AutoLlmFileUtil.JSON.readTree(bytes);
                    String type = event.path("type").asString("");
                    String description = type;
                    description = claude ? ClaudeCodeBackend.observe(output, event, progress) : CodexBackend.observe(output, event, progress);
                    if (description == null) continue;
                    activity(output, type, description, progress);
                } catch (tools.jackson.core.JacksonException e) { activity(output, "unparsed-event", "Unrecognized event; inspect the raw log.", progress); }
            }
            if (progress.discarding || batch.length - start > MAX_EVENT_BYTES) {
                progress.discarding = true; progress.cursor = base + batch.length; changed = true;
            }
            if (changed) AutoLlmFileUtil.write(snapshot, progress);
            return progress;
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }
    private static void activity(Path output, String type, String text, Progress progress) {
        progress.lastActivityAt = Instant.now().toString();
        var event = new LinkedHashMap<String,Object>(); event.put("timestamp", progress.lastActivityAt); event.put("type", type); event.put("text", text);
        while (AutoLlmFileUtil.JSON.writeValueAsBytes(event).length > 4096) {
            text = text.substring(0, text.length() / 2); event.put("text", text + "\n[truncated; full output is in stdout.log]"); event.put("truncated", true);
        }
        AutoLlmFileUtil.append(output.resolve("activity.jsonl"), event);
    }
}
