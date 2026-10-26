package es.urjc.etsii.grafo.autoconfig.autollm.execution;

import es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmProcessRunner;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.BooleanSupplier;

public final class LocalWorkloadExecutor implements AutoLlmWorkloadExecutor {
    private final AutoLlmProcessRunner processes;
    public LocalWorkloadExecutor(AutoLlmProcessRunner processes) { this.processes = processes; }
    @Override public AutoLlmProcessRunner.Result run(Workload work, BooleanSupplier cancelled) {
        if (cancelled.getAsBoolean()) throw new java.util.concurrent.CancellationException("Cancelled before starting a workload");
        var result = processes.run(work.command(), work.cwd(), work.output(), work.timeoutMillis(), work.plannedMillis(), work.graceMillis(), cancelled, work.watchEvaluations(), work.startupMillis());
        try { DirectoryArchiveUtil.archive(work.output().resolve("solutions")); }
        catch (IOException e) { throw new UncheckedIOException(e); }
        return result;
    }
}
