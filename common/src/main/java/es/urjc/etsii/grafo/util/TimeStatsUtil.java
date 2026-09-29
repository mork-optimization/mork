package es.urjc.etsii.grafo.util;

import es.urjc.etsii.grafo.metrics.timing.TimeStatsRecorder;

/** Explicit, non-inheritable timing scopes. Timing is disabled outside an execution scope. */
public final class TimeStatsUtil {
    private static final ThreadLocal<TimeStatsRecorder> CURRENT = new ThreadLocal<>();

    private TimeStatsUtil() {}

    public static TimeStatsRecorder current() {
        return CURRENT.get();
    }

    public static Scope bind(TimeStatsRecorder recorder) {
        return new Scope(recorder);
    }

    public static final class Scope implements AutoCloseable {
        private final Thread owner = Thread.currentThread();
        private final TimeStatsRecorder previous = CURRENT.get();
        private final TimeStatsRecorder recorder;
        private boolean closed;

        private Scope(TimeStatsRecorder recorder) {
            this.recorder = recorder;
            if (recorder == null) CURRENT.remove();
            else CURRENT.set(recorder);
        }

        @Override
        public void close() {
            if (Thread.currentThread() != owner) throw new IllegalStateException("Timing scope closed by another thread");
            if (closed) return;
            closed = true;
            try {
                if (recorder != null) recorder.close();
            } finally {
                if (previous == null) CURRENT.remove();
                else CURRENT.set(previous);
            }
        }
    }
}
