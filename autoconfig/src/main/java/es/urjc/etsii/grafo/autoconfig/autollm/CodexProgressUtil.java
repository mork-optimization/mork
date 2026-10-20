package es.urjc.etsii.grafo.autoconfig.autollm;

import java.io.*;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;

/** Incremental CLI observations. Counters describe visible events, not hidden model iterations. */
public final class CodexProgressUtil {
    private static final int MAX_EVENT_BYTES = 8 * 1024 * 1024;
    public static class Progress {
        public long cursor;
        public boolean discarding;
        public String state = "LAUNCHING";
        public String threadId;
        public String lastActivityAt;
        public int turns;
        public int commandsStarted;
        public int commandsCompleted;
        public int edits;
        public int messages;
        public Object usage;
    }
    private CodexProgressUtil() {}
    public static synchronized Progress refresh(Path output) {
        Path snapshot = output.resolve("codex-progress.json"), log = output.resolve("stdout.log");
        var progress = Files.exists(snapshot) ? AutoLlmFileUtil.read(snapshot, Progress.class) : new Progress();
        if (!Files.isRegularFile(log)) return progress;
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
                    if (type.equals("thread.started")) {
                        progress.threadId = event.path("thread_id").asString(); progress.state = "SESSION_READY";
                        AutoLlmFileUtil.write(output.resolve("thread-id.json"), Map.of("threadId", progress.threadId));
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
                    else continue;
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
        AutoLlmFileUtil.append(output.resolve("activity.jsonl"), Map.of("timestamp", progress.lastActivityAt, "type", type,
                "text", text.length() > 4000 ? text.substring(0, 4000) + "\n… Full output is in stdout.log." : text));
    }
}
