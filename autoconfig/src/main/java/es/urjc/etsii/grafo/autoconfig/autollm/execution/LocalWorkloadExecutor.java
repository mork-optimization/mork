package es.urjc.etsii.grafo.autoconfig.autollm.execution;

import es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmProcessRunner;
import es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmFileUtil;
import es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmFailureUtil;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;

public final class LocalWorkloadExecutor implements AutoLlmWorkloadExecutor {
    private final AutoLlmProcessRunner processes;
    public record Completed(List<String> command, String cwd, AutoLlmProcessRunner.Result result) {}
    public LocalWorkloadExecutor(AutoLlmProcessRunner processes) { this.processes = processes; }
    @Override public AutoLlmProcessRunner.Result run(Workload work, BooleanSupplier cancelled) {
        if (cancelled.getAsBoolean()) throw new java.util.concurrent.CancellationException("Cancelled before starting a workload");
        var checkpoint = work.output().resolve("workload-result.json");
        AutoLlmProcessRunner.Result result = null;
        if (Files.isRegularFile(checkpoint)) {
            var completed = AutoLlmFileUtil.read(checkpoint, Completed.class);
            boolean collected = Files.isRegularFile(work.output().resolve("collection-completed.json"));
            boolean sameInvocation = completed.command().equals(work.command()) && completed.cwd().equals(work.cwd().toString());
            boolean succeeded = completed.result().exitCode() == 0 || completed.result().plannedDeadline();
            if (sameInvocation && (!collected || succeeded)) result = completed.result();
            else {
                // Baseline metadata-only/full checks share an output directory. A fully collected
                // invocation may be replaced; pending evidence always retains its original identity.
                if (!collected)
                    throw new IllegalStateException("Uncollected workload inputs changed: " + work.output());
                try {
                    Files.delete(checkpoint);
                    Files.delete(work.output().resolve("collection-completed.json"));
                } catch (IOException e) { throw new java.io.UncheckedIOException(e); }
            }
        }
        if (result == null) {
            result = processes.run(work.command(), work.cwd(), work.output(), work.timeoutMillis(), work.plannedMillis(), work.graceMillis(), cancelled, work.watchEvaluations(), work.startupMillis());
            AutoLlmFileUtil.write(checkpoint, new Completed(work.command(), work.cwd().toString(), result));
        }
        try {
            DirectoryArchiveUtil.archive(work.output().resolve("solutions"));
            AutoLlmFileUtil.write(work.output().resolve("collection-completed.json"), Map.of("completed", true));
            Files.deleteIfExists(work.output().resolve("collection-failure.json"));
        } catch (IOException | RuntimeException e) {
            var failure = AutoLlmFailureUtil.error(AutoLlmFailureUtil.Category.INFRASTRUCTURE,
                    "EVIDENCE_COLLECTION", "Workload completed; retry evidence collection: " + e, work.output());
            AutoLlmFileUtil.write(work.output().resolve("collection-failure.json"), failure.failure());
            throw failure;
        }
        return result;
    }
}
