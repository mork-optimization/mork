package es.urjc.etsii.grafo.metrics.timing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.*;

class AsyncTimeStatsTest {
    private static final TimeStatsMethod METHOD = new TimeStatsMethod("Component", "method()");

    static TimeStatsExecution metadata() {
        return new TimeStatsExecution(UUID.randomUUID(), "experiment", "path", "instance", "algorithm", 1, 1235);
    }

    @ParameterizedTest
    @ValueSource(strings = {"batch", "finish", "close"})
    void snapshotRemainsDrainingUntilWriterFinishes(String blockedPhase) throws Exception {
        var entered = new CountDownLatch(1);
        var release = new CountDownLatch(1);
        var sink = new Sink() {
            private void block(String phase) {
                if (!blockedPhase.equals(phase)) return;
                entered.countDown();
                try {
                    if (!release.await(5, TimeUnit.SECONDS)) throw new IllegalStateException("Test writer was not released");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(e);
                }
            }
            @Override public void batch(AsyncTimeStats.Batch batch) throws IOException {
                block("batch");
                super.batch(batch);
            }
            @Override public void finish(TimeStatsSummary summary, BooleanSupplier mayPublish) {
                block("finish");
                super.finish(summary, mayPublish);
            }
            @Override public void close() {
                block("close");
                super.close();
            }
        };
        var config = new TimeStatsConfig();
        config.setBatchSize(1);
        var engine = engine(config, sink);
        try (var recorder = engine.open(metadata())) {
            recorder.record(METHOD, 1, 2);
        }
        var closer = new Thread(engine::close);
        closer.start();
        try {
            assertTrue(entered.await(2, TimeUnit.SECONDS));
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(2);
            while (engine.snapshot().status().equals("RECORDING") && System.nanoTime() < deadline) {
                Thread.sleep(1);
            }
            var snapshot = engine.snapshot();
            assertEquals("DRAINING", snapshot.status());
            assertEquals(1, snapshot.observedEvents());
            assertEquals(blockedPhase.equals("batch") ? 0 : 1, snapshot.flushedEvents());
        } finally {
            release.countDown();
            assertTrue(closer.join(Duration.ofSeconds(5)));
        }
        assertEquals("COMPLETE", engine.snapshot().status());
        assertEquals("COMPLETE", sink.summary.status());
        assertEquals(1, engine.snapshot().flushedEvents());
    }

    @Test
    void closingIdleWriterDoesNotWaitForFlushInterval() throws Exception {
        var config = new TimeStatsConfig();
        config.setFlushInterval(Duration.ofMinutes(1));
        config.setShutdownTimeout(Duration.ofSeconds(1));
        var sink = new Sink();
        var opened = new CountDownLatch(1);
        var engine = new AsyncTimeStats(UUID.randomUUID(), 0, config, () -> {
            opened.countDown();
            return sink;
        });
        assertTrue(opened.await(2, TimeUnit.SECONDS));
        engine.close();
        assertTrue(sink.finished);
        assertEquals("COMPLETE", engine.snapshot().status());
    }

    @Test
    void fullAndPartialBatchesAreDrainedIncludingEmptyExecutions() {
        var config = new TimeStatsConfig();
        config.setBatchSize(3);
        var sink = new Sink();
        var engine = engine(config, sink);
        try (var recorder = engine.open(metadata())) {
            for (int i = 0; i < 7; i++) recorder.record(METHOD, i, i + 1);
        }
        engine.open(metadata()).close();
        engine.close();
        assertEquals(7, sink.calls.size());
        assertEquals(2, sink.metadata.size());
        assertEquals("COMPLETE", engine.snapshot().status());
        assertEquals(7, engine.snapshot().flushedEvents());
        assertEquals(0, engine.snapshot().activeExecutions());
        assertTrue(sink.finished);
        assertTrue(sink.closed);
        assertNull(engine.open(metadata()));
    }

    @Test
    void overflowingQueueDoesNotWaitOrCorruptOwnedBatches() throws Exception {
        var config = new TimeStatsConfig();
        config.setBatchSize(2);
        config.setQueueCapacity(1);
        config.setPoolCapacity(1);
        var sink = new BlockingSink();
        var engine = engine(config, sink);
        try {
            try (var recorder = engine.open(metadata())) {
                recorder.record(METHOD, 1, 2);
                recorder.record(METHOD, 2, 3);
                assertTrue(sink.entered.await(2, TimeUnit.SECONDS));
                assertTimeout(Duration.ofSeconds(1), () -> {
                    for (int i = 3; i <= 100_000; i++) recorder.record(METHOD, i, i + 1);
                });
            }
            assertEquals(99_996, engine.snapshot().droppedEvents());
        } finally {
            sink.release.countDown();
            engine.close();
        }
        assertEquals(4, sink.calls.size());
        for (int i = 0; i < 4; i++) assertEquals(i + 1, sink.calls.get(i).enter);
        assertEquals(100_000, engine.snapshot().observedEvents());
        assertEquals("INCOMPLETE", sink.summary.status());
    }

    @Test
    void intervalSubmitsPartialBatchAndWriterFlushesWhileProducerIsIdle() throws Exception {
        var config = new TimeStatsConfig();
        config.setFlushInterval(Duration.ofMillis(5));
        var sink = new Sink();
        var engine = engine(config, sink);
        try (var recorder = engine.open(metadata())) {
            long now = System.nanoTime();
            recorder.record(METHOD, now, now + TimeUnit.SECONDS.toNanos(1));
            assertTrue(sink.flushed.await(2, TimeUnit.SECONDS));
        } finally { engine.close(); }
        assertEquals(1, sink.calls.size());
    }

    @Test
    void diskFailureDisablesRecordingWithoutThrowingToProducer() throws Exception {
        var config = new TimeStatsConfig();
        config.setBatchSize(1);
        var failed = new CountDownLatch(1);
        var sink = new Sink() {
            @Override public void batch(AsyncTimeStats.Batch batch) throws IOException { throw new IOException("disk full"); }
            @Override public void failure(TimeStatsSummary summary) { failed.countDown(); }
        };
        var engine = engine(config, sink);
        try (var recorder = engine.open(metadata())) {
            assertDoesNotThrow(() -> recorder.record(METHOD, 1, 2));
            assertTrue(failed.await(2, TimeUnit.SECONDS));
            assertFalse(recorder.isActive());
            assertNull(engine.open(metadata()));
        } finally { engine.close(); }
        assertEquals("FAILED", engine.snapshot().status());
        assertTrue(engine.snapshot().error().contains("disk full"));
        assertEquals(0, engine.snapshot().flushedEvents());
        assertFalse(sink.finished);
    }

    @Test
    void shutdownTimeoutDoesNotPublishLaterWhenBlockedWriteReturns() throws Exception {
        var config = new TimeStatsConfig();
        config.setBatchSize(1);
        config.setShutdownTimeout(Duration.ofMillis(20));
        var sink = new BlockingSink();
        var engine = engine(config, sink);
        try (var recorder = engine.open(metadata())) {
            recorder.record(METHOD, 1, 2);
        }
        assertTrue(sink.entered.await(2, TimeUnit.SECONDS));
        try {
            assertTimeout(Duration.ofSeconds(1), engine::close);
            assertEquals("FAILED", engine.snapshot().status());
            assertFalse(sink.finished);
        } finally {
            sink.release.countDown();
            assertTrue(sink.closedLatch.await(2, TimeUnit.SECONDS));
            engine.close();
        }
        assertFalse(sink.finished);
    }

    @Test
    void concurrentProducersKeepExecutionIdsAndAccountForEveryEvent() throws Exception {
        var config = new TimeStatsConfig();
        config.setBatchSize(31);
        config.setQueueCapacity(4);
        var sink = new Sink();
        var engine = engine(config, sink);
        var ids = new HashSet<UUID>();
        try (var workers = Executors.newFixedThreadPool(8)) {
            var futures = new ArrayList<java.util.concurrent.Future<?>>();
            for (int worker = 0; worker < 8; worker++) {
                var metadata = metadata();
                ids.add(metadata.executionId());
                futures.add(workers.submit(() -> {
                    try (var recorder = engine.open(metadata)) {
                        for (int i = 0; i < 20_000; i++) recorder.record(METHOD, i, i + 1);
                    }
                }));
            }
            for (var future : futures) future.get(10, TimeUnit.SECONDS);
        } finally { engine.close(); }
        assertEquals(160_000, engine.snapshot().observedEvents());
        assertEquals(160_000, sink.calls.size() + engine.snapshot().droppedEvents());
        assertEquals(ids, sink.metadata);
        for (var call : sink.calls) {
            assertTrue(ids.contains(call.execution));
            assertSame(METHOD, call.method);
            assertEquals(call.enter + 1, call.exit);
        }
    }

    private AsyncTimeStats engine(TimeStatsConfig config, Sink sink) {
        return new AsyncTimeStats(UUID.randomUUID(), 0, config, () -> sink);
    }

    record Call(UUID execution, TimeStatsMethod method, long enter, long exit) {}
    static class Sink implements TimeStatsSink {
        final List<Call> calls = new ArrayList<>();
        final HashSet<UUID> metadata = new HashSet<>();
        final CountDownLatch flushed = new CountDownLatch(1);
        final CountDownLatch closedLatch = new CountDownLatch(1);
        volatile boolean finished;
        boolean closed;
        TimeStatsSummary summary;
        public void execution(TimeStatsExecution execution) { metadata.add(execution.executionId()); }
        public void batch(AsyncTimeStats.Batch batch) throws IOException {
            assertTrue(metadata.contains(batch.execution.metadata.executionId()));
            for (int i = 0; i < batch.size; i++) calls.add(new Call(batch.execution.metadata.executionId(), batch.methods[i], batch.enter[i], batch.exit[i]));
        }
        public void flush() { if (!calls.isEmpty()) flushed.countDown(); }
        public void finish(TimeStatsSummary summary, BooleanSupplier mayPublish) {
            this.summary = summary;
            finished = mayPublish.getAsBoolean();
        }
        public void failure(TimeStatsSummary summary) {}
        public void close() { closed = true; closedLatch.countDown(); }
    }

    static class BlockingSink extends Sink {
        final CountDownLatch entered = new CountDownLatch(1);
        final CountDownLatch release = new CountDownLatch(1);
        public void batch(AsyncTimeStats.Batch batch) throws IOException {
            entered.countDown();
            boolean released = false;
            while (!released) {
                try { released = release.await(10, TimeUnit.SECONDS); }
                catch (InterruptedException ignored) { /* Simulate non-interruptible storage. */ }
            }
            super.batch(batch);
        }
    }
}
