package es.urjc.etsii.grafo.autoconfig.autollm.execution;

import es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmProcessRunner;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;

public interface AutoLlmWorkloadExecutor {
    record Workload(List<String> command, Path cwd, Path output, Map<String,Path> inputs, List<String> collect, List<String> reset,
                    long timeoutMillis, long plannedMillis, long graceMillis, boolean watchEvaluations, long startupMillis) {}
    AutoLlmProcessRunner.Result run(Workload workload, BooleanSupplier cancelled);
}
