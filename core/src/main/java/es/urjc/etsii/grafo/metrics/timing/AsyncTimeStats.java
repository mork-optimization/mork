package es.urjc.etsii.grafo.metrics.timing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/** Bounded batch transport. Only producers touch active batches; only the writer touches queued batches. */
final class AsyncTimeStats implements AutoCloseable {
    private static final Logger log = LoggerFactory.getLogger(AsyncTimeStats.class);
    interface SinkFactory { TimeStatsSink open() throws IOException; }

    final UUID runId;
    final long origin;
    private final int batchSize;
    private final long flushNanos;
    private final long shutdownMillis;
    private final Object gate = new Object();
    private final ArrayBlockingQueue<Batch> queue;
    private final ArrayBlockingQueue<Batch> pool;
    private final ConcurrentHashMap<UUID, Execution> executions = new ConcurrentHashMap<>();
    private final AtomicLong observed = new AtomicLong();
    private final AtomicLong dropped = new AtomicLong();
    private final Thread writer;
    private volatile boolean accepting = true;
    private volatile boolean abandoned;
    private volatile String error;
    private volatile long flushed;
    private volatile TimeStatsSummary finalSummary;
    private boolean closeRequested;

    AsyncTimeStats(UUID runId, long origin, TimeStatsConfig config, SinkFactory factory) {
        this.runId = runId;
        this.origin = origin;
        batchSize = config.getBatchSize();
        flushNanos = config.getFlushInterval().toNanos();
        shutdownMillis = config.getShutdownTimeout().toMillis();
        queue = new ArrayBlockingQueue<>(config.getQueueCapacity());
        pool = new ArrayBlockingQueue<>(config.getPoolCapacity());
        writer = new Thread(() -> write(factory), "mork-time-stats-writer");
        writer.setDaemon(true); // A broken filesystem must not prevent JVM termination.
        writer.start();
    }

    TimeStatsRecorder open(TimeStatsExecution metadata) {
        synchronized (gate) {
            if (!accepting) return null;
            var execution = new Execution(metadata);
            if (executions.putIfAbsent(metadata.executionId(), execution) != null) {
                throw new IllegalArgumentException("Duplicate timing execution ID " + metadata.executionId());
            }
            return new Recorder(execution);
        }
    }

    TimeStatsSummary snapshot() {
        var finished = finalSummary;
        return finished != null ? finished : summary(false);
    }

    private TimeStatsSummary summary(boolean terminal) {
        int active = 0;
        for (var execution : executions.values()) if (!execution.finished) active++;
        String status;
        if (error != null) status = "FAILED";
        else if (!terminal) status = accepting ? "RECORDING" : "DRAINING";
        else status = abandoned || active > 0 || dropped.get() > 0 ? "INCOMPLETE" : "COMPLETE";
        return new TimeStatsSummary(runId, status, executions.size(), active,
                observed.get(), dropped.get(), flushed, error);
    }

    private void recycle(Batch batch) {
        batch.clear();
        if (accepting) pool.offer(batch);
    }

    private void write(SinkFactory factory) {
        TimeStatsSink sink = null;
        long written = 0;
        long nextFlush = System.nanoTime() + flushNanos;
        long nextLossWarning = 0;
        long reportedLoss = 0;
        try {
            sink = factory.open();
            while (!abandoned && (accepting || !queue.isEmpty())) {
                long wait = Math.max(1, nextFlush - System.nanoTime());
                var batch = queue.poll(wait, TimeUnit.NANOSECONDS);
                if (batch != null && batch.size > 0) {
                    try {
                        writeMetadata(sink, batch.execution);
                        sink.batch(batch);
                        written += batch.size;
                    } finally {
                        recycle(batch);
                    }
                }
                if (System.nanoTime() >= nextFlush) {
                    sink.flush();
                    flushed = written;
                    nextFlush = System.nanoTime() + flushNanos;
                    long lost = dropped.get();
                    if (lost > reportedLoss && System.nanoTime() >= nextLossWarning) {
                        log.warn("TimeStats writer cannot keep up: {} events dropped so far; recording is incomplete", lost);
                        reportedLoss = lost;
                        nextLossWarning = System.nanoTime() + TimeUnit.SECONDS.toNanos(30);
                    }
                }
            }
            if (abandoned) throw new IOException("Timing writer shutdown timed out");
            // Include failed or empty executions, even if every event was dropped.
            for (var execution : executions.values()) writeMetadata(sink, execution);
            sink.flush();
            flushed = written;
            // Prepare the manifest's terminal status, but keep live snapshots draining until cleanup finishes.
            sink.finish(summary(true), () -> !abandoned);
        } catch (Exception failure) {
            synchronized (gate) {
                accepting = false;
                if (error == null) error = failure.toString();
            }
            log.warn("TimeStats recording failed; algorithms will continue and timing output is incomplete: {}", error);
            discardQueued();
            if (sink != null) {
                try { sink.failure(snapshot()); }
                catch (Exception secondary) { log.warn("Could not write TimeStats failure manifest: {}", secondary.toString()); }
            }
        } finally {
            synchronized (gate) { accepting = false; }
            discardQueued();
            pool.clear();
            if (sink != null) {
                try { sink.close(); }
                catch (Exception failure) {
                    if (error == null) error = failure.toString();
                    log.warn("Could not close TimeStats output: {}", failure.toString());
                }
            }
            finalSummary = summary(true);
            executions.clear();
            if (finalSummary.droppedEvents() > 0) {
                log.warn("TimeStats output is incomplete: {} events dropped; {} events acknowledged by flush",
                        finalSummary.droppedEvents(), finalSummary.flushedEvents());
            }
        }
    }

    private void writeMetadata(TimeStatsSink sink, Execution execution) throws IOException {
        if (!execution.metadataWritten) {
            sink.execution(execution.metadata);
            execution.metadataWritten = true;
        }
    }

    private void discardQueued() {
        Batch batch;
        while ((batch = queue.poll()) != null) {
            dropped.addAndGet(batch.size);
            batch.clear();
        }
    }

    @Override
    public void close() {
        synchronized (gate) {
            accepting = false;
            if (closeRequested) return;
            closeRequested = true;
            // Wake an idle poll without interrupting an in-progress disk write.
            queue.offer(new Batch(0));
        }
        if (Thread.currentThread() == writer) return;
        try {
            writer.join(shutdownMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        if (writer.isAlive()) {
            abandoned = true;
            error = "Timing writer did not finish within the shutdown timeout";
            writer.interrupt();
            discardQueued();
            pool.clear();
            log.warn("{}; leaving partial files", error);
        }
    }

    static final class Execution {
        final TimeStatsExecution metadata;
        volatile boolean finished;
        boolean metadataWritten; // writer only
        Execution(TimeStatsExecution metadata) { this.metadata = metadata; }
    }

    static final class Batch {
        final TimeStatsMethod[] methods;
        final long[] enter;
        final long[] exit;
        Execution execution;
        long threadId;
        int size;
        Batch(int capacity) {
            methods = new TimeStatsMethod[capacity];
            enter = new long[capacity];
            exit = new long[capacity];
        }
        void clear() {
            Arrays.fill(methods, 0, size, null);
            size = 0;
            execution = null;
        }
    }

    private final class Recorder implements TimeStatsRecorder {
        private final Execution execution;
        private final long threadId = Thread.currentThread().threadId();
        private Batch batch;
        private long lastSubmission = System.nanoTime();
        private boolean closed;

        Recorder(Execution execution) { this.execution = execution; }
        @Override
        public boolean isActive() { return accepting && !closed; }

        @Override
        public void record(TimeStatsMethod method, long enter, long exit) {
            if (closed) return;
            if (batch == null) {
                batch = pool.poll();
                if (batch == null) batch = new Batch(batchSize);
                batch.execution = execution;
                batch.threadId = threadId;
            }
            int index = batch.size++;
            batch.methods[index] = method;
            batch.enter[index] = enter - origin;
            batch.exit[index] = exit - origin;
            if (batch.size == batchSize || exit - lastSubmission >= flushNanos) {
                submit();
                lastSubmission = exit;
            }
        }

        private void submit() {
            if (batch == null || batch.size == 0) return;
            observed.addAndGet(batch.size);
            synchronized (gate) {
                if (accepting && queue.offer(batch)) {
                    batch = null;
                    return;
                }
            }
            dropped.addAndGet(batch.size);
            batch.clear();
            batch.execution = execution;
        }

        @Override
        public void close() {
            if (closed) return;
            closed = true;
            submit();
            if (batch != null) { recycle(batch); batch = null; }
            execution.finished = true;
        }
    }
}
