package es.urjc.etsii.grafo.autoconfig.autollm;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import static org.junit.jupiter.api.Assertions.*;

class GenerationMetadataBridgeTest {
    @TempDir Path source;
    @Test void servicesSandboxWritableRequestsOnceAndReturnsDiagnostics() throws Exception {
        var calls = new AtomicInteger();
        try (var bridge = new GenerationMetadataBridge(source, ignored -> { calls.incrementAndGet(); return Map.of("parameters", 32); })) {
            Path queue = source.resolve(".mork-metadata"); Files.createDirectories(queue);
            String key = UUID.randomUUID().toString().replace("-", "");
            Files.writeString(queue.resolve(key + ".request.json"), "{}");
            Path response = queue.resolve(key + ".result.json"); await(response);
            assertEquals(32, AutoLlmFileUtil.JSON.readTree(Files.readString(response)).path("result").path("parameters").asInt());
            Thread.sleep(200); assertEquals(1, calls.get());
        }
    }
    @Test void checkFailuresAreReadableByTheLocalCodexTurn() throws Exception {
        try (var bridge = new GenerationMetadataBridge(source, ignored -> { throw new IllegalStateException("remote build diagnostics"); })) {
            Path queue = source.resolve(".mork-metadata"); Files.createDirectories(queue);
            String key = UUID.randomUUID().toString().replace("-", ""); Files.writeString(queue.resolve(key + ".request.json"), "{}");
            Path response = queue.resolve(key + ".result.json"); await(response);
            assertTrue(Files.readString(response).contains("remote build diagnostics"));
        }
    }
    private static void await(Path file) throws Exception {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
        while (!Files.exists(file) && System.nanoTime() < deadline) Thread.sleep(10);
        assertTrue(Files.exists(file));
    }
}
