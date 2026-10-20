package es.urjc.etsii.grafo.autoconfig.autollm;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.function.Function;

/** File requests let a sandboxed local Codex turn ask the controller for a remote metadata check. */
final class GenerationMetadataBridge implements AutoCloseable {
    private final Path queue;
    private final Function<Map<String,String>,Object> check;
    private final Thread thread;
    private volatile boolean closed;
    GenerationMetadataBridge(Path source, Function<Map<String,String>,Object> check) {
        this.queue = source.resolve(".mork-metadata"); this.check = check;
        this.thread = Thread.ofVirtual().name("autollm-metadata").start(this::watch);
    }
    private void watch() {
        try {
            Files.createDirectories(queue);
            while (!closed && !Thread.currentThread().isInterrupted()) {
                try (var files = Files.newDirectoryStream(queue, "*.request.json")) {
                    for (Path file : files) {
                        String name = file.getFileName().toString();
                        if (!name.matches("[a-f0-9]{32}\\.request\\.json")) continue;
                        Path response = queue.resolve(name.replace(".request.json", ".result.json"));
                        if (Files.exists(response)) continue;
                        Object result;
                        try { result = Map.of("result", check.apply(AutoLlmFileUtil.read(file, new tools.jackson.core.type.TypeReference<Map<String,String>>() {}))); }
                        catch (RuntimeException e) { result = Map.of("error", e.toString()); }
                        AutoLlmFileUtil.write(response, result);
                        if (closed) break;
                    }
                }
                Thread.sleep(100);
            }
        } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        catch (IOException e) { AutoLlmFileUtil.write(queue.resolve("bridge-error.json"), Map.of("error", e.toString())); }
    }
    @Override public void close() {
        closed = true;
        try { thread.join(); }
        catch (InterruptedException e) { thread.interrupt(); Thread.currentThread().interrupt(); throw new CancellationException("Metadata bridge detached during shutdown"); }
    }
}
