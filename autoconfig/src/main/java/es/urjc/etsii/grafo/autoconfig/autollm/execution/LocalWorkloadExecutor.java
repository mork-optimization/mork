package es.urjc.etsii.grafo.autoconfig.autollm.execution;

import es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmProcessRunner;
import java.util.function.BooleanSupplier;

public final class LocalWorkloadExecutor implements AutoLlmWorkloadExecutor {
    private final AutoLlmProcessRunner processes;
    public LocalWorkloadExecutor(AutoLlmProcessRunner processes) { this.processes = processes; }
    @Override public AutoLlmProcessRunner.Result run(Workload work, BooleanSupplier cancelled) {
        if (cancelled.getAsBoolean()) throw new java.util.concurrent.CancellationException("Cancelled before starting a workload");
        return processes.run(work.command(), work.cwd(), work.output(), work.timeoutMillis(), work.plannedMillis(), work.graceMillis(), cancelled, work.watchEvaluations(), work.startupMillis());
    }
}
