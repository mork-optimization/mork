package es.urjc.etsii.grafo.autoconfig.autollm;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class AutoLlmFileUtilTest {
    @TempDir Path root;

    @Test void concurrentPublicationUsesIndependentTemporaryFiles() throws Exception {
        var barrier = new CyclicBarrier(2); Path file = root.resolve("llm-usage.json");
        try (var executor = Executors.newFixedThreadPool(2)) {
            var first = executor.submit(() -> AutoLlmFileUtil.write(file, new ConcurrentValue(barrier, "first")));
            var second = executor.submit(() -> AutoLlmFileUtil.write(file, new ConcurrentValue(barrier, "second")));
            first.get(5, TimeUnit.SECONDS); second.get(5, TimeUnit.SECONDS);
        }
        var saved = AutoLlmFileUtil.read(file, Map.class);
        assertTrue(Set.of("first", "second").contains(saved.get("value")));
        try (var files = Files.list(root)) { assertEquals(List.of(file), files.toList()); }
    }

    /** Both writers have opened their temporary output before either can complete serialization. */
    public static final class ConcurrentValue {
        private final CyclicBarrier barrier;
        private final String value;
        ConcurrentValue(CyclicBarrier barrier, String value) { this.barrier = barrier; this.value = value; }
        public String getValue() {
            try { barrier.await(3, TimeUnit.SECONDS); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new IllegalStateException(e); }
            catch (BrokenBarrierException | TimeoutException e) { throw new IllegalStateException(e); }
            return value;
        }
    }
}
