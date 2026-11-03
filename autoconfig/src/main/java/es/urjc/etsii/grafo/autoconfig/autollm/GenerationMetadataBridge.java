package es.urjc.etsii.grafo.autoconfig.autollm;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.concurrent.TimeUnit;
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
        boolean interrupted = false;
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
                            catch (RuntimeException e) {
                                interrupted |= Thread.interrupted();
                                result = Map.of("error", e.toString(), "failure", AutoLlmFailureUtil.describe(e, durable));
                            }
                            AutoLlmFileUtil.write(saved, result);
                        }
                        Files.createDirectories(queue);
                        AutoLlmFileUtil.write(response, result);
                        if (closed || interrupted) break;
                    }
                } catch (NoSuchFileException ignored) { /* Editable mailbox was removed; durable requests/results remain owned. */ }
                if (interrupted) break;
                Thread.sleep(100);
            }
        } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        catch (IOException e) { AutoLlmFileUtil.write(queue.resolve("bridge-error.json"), Map.of("error", e.toString())); }
        finally { if (interrupted) Thread.currentThread().interrupt(); }
    }
    boolean owns(Thread candidate) { return thread == candidate; }
    void interrupt() { if (!closed) { closed = true; thread.interrupt(); } }
    boolean awaitStopped(long deadline) {
        // The caller is commonly interrupted already. Still allow the check to stop its
        // process tree and durably record the interrupted response before returning.
        boolean interrupted = Thread.interrupted();
        try {
            while (thread.isAlive() && System.nanoTime() < deadline) {
                try { thread.join(Math.max(1, TimeUnit.NANOSECONDS.toMillis(deadline - System.nanoTime()))); }
                catch (InterruptedException e) { interrupted = true; thread.interrupt(); }
            }
            return !thread.isAlive();
        } finally { if (interrupted) Thread.currentThread().interrupt(); }
    }
    @Override public void close() {
        interrupt();
        if (!awaitStopped(System.nanoTime() + TimeUnit.SECONDS.toNanos(15)))
            throw new IllegalStateException("Metadata check shutdown is unconfirmed; reconcile its saved local job");
    }
}
