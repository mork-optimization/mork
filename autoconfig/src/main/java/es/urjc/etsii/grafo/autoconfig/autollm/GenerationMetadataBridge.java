package es.urjc.etsii.grafo.autoconfig.autollm;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.function.Function;

/** File requests let a sandboxed local Codex turn ask the controller for a remote metadata check. */
final class GenerationMetadataBridge implements AutoCloseable {
    private final Path queue;
    private final Path durable;
    private final Function<Map<String,String>,Object> check;
    private final Thread thread;
    private volatile boolean closed;
    GenerationMetadataBridge(Path source, Path durable, Function<Map<String,String>,Object> check) {
        this.queue = source.resolve(".mork-metadata"); this.durable = durable; this.check = check;
        this.thread = Thread.ofVirtual().name("autollm-metadata").start(this::watch);
    }
    private void watch() {
        try {
            while (!closed && !Thread.currentThread().isInterrupted()) {
                Files.createDirectories(queue); Files.createDirectories(durable);
                try (var files = Files.newDirectoryStream(queue, "*.request.json")) {
                    for (Path file : files) {
                        String name = file.getFileName().toString();
                        if (!name.matches("[a-f0-9]{32}\\.request\\.json")) continue;
                        Path response = queue.resolve(name.replace(".request.json", ".result.json"));
                        if (Files.exists(response)) continue;
                        Path request = durable.resolve(name), saved = durable.resolve(name.replace(".request.json", ".result.json"));
                        if (!Files.exists(request)) Files.copy(file, request);
                        Object result;
                        if (Files.exists(saved)) result = AutoLlmFileUtil.read(saved, Object.class);
                        else {
                            var options = new java.util.HashMap<>(AutoLlmFileUtil.read(request, new tools.jackson.core.type.TypeReference<Map<String,String>>() {}));
                            options.put("requestId", name.substring(0, 32));
                            try { result = Map.of("result", check.apply(options)); }
                            catch (RuntimeException e) { result = Map.of("error", e.toString(), "failure", AutoLlmFailureUtil.describe(e, durable)); }
                            AutoLlmFileUtil.write(saved, result);
                        }
                        Files.createDirectories(queue);
                        AutoLlmFileUtil.write(response, result);
                        if (closed) break;
                    }
                } catch (NoSuchFileException ignored) { /* Editable mailbox was removed; durable requests/results remain owned. */ }
                Thread.sleep(100);
            }
        } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        catch (IOException e) { AutoLlmFileUtil.write(queue.resolve("bridge-error.json"), Map.of("error", e.toString())); }
    }
    @Override public void close() {
        closed = true;
        thread.interrupt();
        try { thread.join(2000); }
        catch (InterruptedException e) { thread.interrupt(); Thread.currentThread().interrupt(); throw new CancellationException("Metadata bridge detached during shutdown"); }
    }
}
