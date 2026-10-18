package es.urjc.etsii.grafo.autoconfig.screening;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmComparisonUtil;
import es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmFileUtil;
import es.urjc.etsii.grafo.autoconfig.builder.AlgorithmBuilderService;
import es.urjc.etsii.grafo.config.SolverConfig;
import es.urjc.etsii.grafo.create.builder.SolutionBuilder;
import es.urjc.etsii.grafo.io.*;
import es.urjc.etsii.grafo.metrics.*;
import es.urjc.etsii.grafo.solution.*;
import es.urjc.etsii.grafo.util.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import static es.urjc.etsii.grafo.autoconfig.screening.ScreeningData.*;

/** One warmed, sequential JVM for one instance and phase. All construction and validation are outside timing. */
final class ScreeningWorker<S extends Solution<S,I>, I extends Instance> {
    private final InstanceManager<I> instances;
    private final AlgorithmBuilderService algorithms;
    private final SolutionBuilder<S,I> solutions;
    private final SolutionValidator<S,I> validator;
    private final SolverConfig solver;
    ScreeningWorker(InstanceManager<I> instances, AlgorithmBuilderService algorithms, SolutionBuilder<S,I> solutions,
            SolutionValidator<S,I> validator, SolverConfig solver) {
        this.instances = instances; this.algorithms = algorithms; this.solutions = solutions; this.validator = validator; this.solver = solver;
    }
    void run(Path jobFile) throws IOException {
        if (ScreeningWorker.class.desiredAssertionStatus() || Solution.class.desiredAssertionStatus()) {
            throw new IllegalStateException("Timing workers require assertions disabled (-da)");
        }
        Job job = AutoLlmFileUtil.read(jobFile, Job.class);
        var config = job.manifest().settings();
        Path output = Path.of(job.output());
        String sota = Files.readString(output.resolve("sota.json"));
        if (!job.manifest().sotaSha256().equals(AutoLlmFileUtil.hash(output.resolve("sota.json")))
                || !job.input().sha256().equals(ScreeningFileUtil.inputHash(job.input().path()))) {
            throw new IllegalStateException("Frozen SOTA or input changed before worker execution");
        }
        ExecutionJournalUtil.write("readiness.json", Map.of("startedAtMillis", System.currentTimeMillis(), "pid", ProcessHandle.current().pid()));
        ExecutionJournalUtil.phase("load", "LOADING", 300_000, Map.of("instance", job.input().path()));
        I instance = instances.getInstance(job.input().path());
        ExecutionJournalUtil.finished("load", Map.of("state", "SUCCEEDED"));
        Context.Configurator.setValidator(validator);
        Context.Configurator.enableValidation();
        Metrics.enableMetrics();
        for (int i = 0; i < config.warmupRepetitions(); i++) {
            Run result = measure(job, instance, sota, job.manifest().seed() - 1000 - i, config.warmupMillis());
            if (!result.success()) throw new IllegalStateException("Warm-up failed: " + result.failure());
        }
        for (long seed : job.seeds()) {
            if (ScreeningFileUtil.readRun(output, job.manifest(), job.input(), seed) != null) continue;
            Run result = measure(job, instance, sota, seed, config.horizonMillis());
            ScreeningFileUtil.writeRun(output, result);
        }
    }
    @SuppressWarnings("unchecked")
    private Run measure(Job job, I instance, String sota, long seed, long millis) {
        String id = UUID.randomUUID().toString();
        long start = 0, elapsed = 0;
        try {
            ExecutionJournalUtil.phase(id, "BUILDING", 300_000, Map.of("instance", job.input().path(), "seed", seed));
            Context.Configurator.resetRandom(es.urjc.etsii.grafo.util.random.RandomType.valueOf(job.manifest().randomType()), seed);
            Algorithm<S,I> algorithm = (Algorithm<S,I>) algorithms.buildAlgorithmFromJson(sota);
            algorithm = AutoLlmComparisonUtil.restart("screening-SOTA", algorithm);
            algorithm.setBuilder(solutions);
            TimeControl.setMaxExecutionTime(millis, TimeUnit.MILLISECONDS);
            ExecutionJournalUtil.phase(id, "RUNNING", millis, Map.of());
            start = System.nanoTime();
            Metrics.resetMetrics(start);
            TimeControl.start();
            S solution = algorithm.algorithm(instance);
            elapsed = System.nanoTime() - start;
            ExecutionJournalUtil.phase(id, "VALIDATING", 300_000, Map.of());
            validator.validate(solution).throwIfFail();
            Objective<?,S,I> objective = Context.getMainObjective();
            double value = objective.evalSol(solution);
            if (!Double.isFinite(value)) throw new IllegalStateException("Non-finite objective");
            var metric = Metrics.getCurrentThreadMetrics().getMetrics().get(Context.getMainObjective().getName());
            List<TimeValue> curve = metric == null ? List.of() : List.copyOf(metric.getValues());
            if (elapsed > TimeUnit.MILLISECONDS.toNanos(millis + 30_000)) throw new IllegalStateException("Run exceeded deadline plus grace");
            return new Run(job.manifest().fingerprint(), job.input().path(), seed, true, value, elapsed, curve, null);
        } catch (RuntimeException | AssertionError failure) {
            if (start != 0 && elapsed == 0) elapsed = System.nanoTime() - start;
            return new Run(job.manifest().fingerprint(), job.input().path(), seed, false, null, elapsed, List.of(), failure.toString());
        } finally {
            TimeControl.remove();
            ExecutionJournalUtil.finished(id, Map.of("state", "FINISHED", "seed", seed));
        }
    }
}
