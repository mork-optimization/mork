package es.urjc.etsii.grafo.metrics.timing;

import java.io.IOException;
import java.util.function.BooleanSupplier;

/** Used exclusively by the single writer thread. Also allows deterministic fault injection. */
interface TimeStatsSink extends AutoCloseable {
    void execution(TimeStatsExecution execution) throws IOException;
    void batch(AsyncTimeStats.Batch batch) throws IOException;
    void flush() throws IOException;
    void finish(TimeStatsSummary summary, BooleanSupplier mayPublish) throws IOException;
    void failure(TimeStatsSummary summary) throws IOException;
    @Override
    void close() throws IOException;
}
