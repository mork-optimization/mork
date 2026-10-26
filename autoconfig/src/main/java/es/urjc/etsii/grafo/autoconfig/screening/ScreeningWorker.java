package es.urjc.etsii.grafo.autoconfig.screening;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmComparisonUtil;
import es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmFileUtil;
import es.urjc.etsii.grafo.autoconfig.builder.AlgorithmBuilderService;
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

/** One warmed JVM for both screening phases. All construction and validation are outside timing. */
final class ScreeningWorker<S extends Solution<S,I>, I extends Instance> {
    private final InstanceManager<I> instances;
    private final AlgorithmBuilderService algorithms;
    private final SolutionBuilder<S,I> solutions;
    private final SolutionValidator<S,I> validator;
    private boolean warmedUp;
    private int initialCompleted, initialTotal, confirmationsCompleted, confirmationsTotal;
    private String phase;
    ScreeningWorker(InstanceManager<I> instances, AlgorithmBuilderService algorithms, SolutionBuilder<S,I> solutions,
            SolutionValidator<S,I> validator) {
        this.instances = instances; this.algorithms = algorithms; this.solutions = solutions; this.validator = validator;
    }
    void run(Path jobFile) throws IOException {
        if (ScreeningWorker.class.desiredAssertionStatus() || Solution.class.desiredAssertionStatus()) {
            throw new IllegalStateException("Timing workers require assertions disabled (-da)");
        }
        screen(AutoLlmFileUtil.read(jobFile, Job.class));
    }
    void screen(Job job) throws IOException {
        initialCompleted = 0; initialTotal = 0; confirmationsCompleted = 0; confirmationsTotal = 0;
        var config = job.manifest().settings();
        Path output = Path.of(job.output());
        String sota = Files.readString(output.resolve("sota.json"));
        if (!job.manifest().sotaSha256().equals(AutoLlmFileUtil.hash(output.resolve("sota.json")))) {
            throw new IllegalStateException("Frozen SOTA changed before worker execution");
        }
        ExecutionJournalUtil.write("readiness.json", Map.of("startedAtMillis", System.currentTimeMillis(), "pid", ProcessHandle.current().pid()));
        Context.Configurator.setValidator(validator);
        Context.Configurator.enableValidation();
        Metrics.enableMetrics();
        initialTotal = job.manifest().inputs().size(); phase = "INITIAL";
        // Finish the complete initial pass before starting any confirmations.
        int completed = 0;
        for (Input input : job.manifest().inputs()) {
            runSeeds(job, input, List.of(job.manifest().seed()), sota);
            System.out.println("Initial measurements: " + ++completed + "/" + job.manifest().inputs().size());
            initialCompleted = completed; progress(job);
        }
        System.out.println("Initial pass complete; starting confirmations");
        var seeds = new ArrayList<Long>();
        for (int i = 1; i <= config.confirmationRuns(); i++) seeds.add(job.manifest().seed() + i);
        for (Input input : job.manifest().inputs()) if (ScreeningUtil.candidate(ScreeningFileUtil.readRun(output, job.manifest(), input, job.manifest().seed()), config, job.manifest().mode())) confirmationsTotal += seeds.size();
        phase = "CONFIRMATIONS"; progress(job);
        for (Input input : job.manifest().inputs()) {
            Run initial = ScreeningFileUtil.readRun(output, job.manifest(), input, job.manifest().seed());
            if (ScreeningUtil.candidate(initial, config, job.manifest().mode())) runSeeds(job, input, seeds, sota);
        }
        phase = "MEASUREMENTS_COMPLETE"; progress(job);
    }
    private void runSeeds(Job job, Input input, List<Long> seeds, String sota) {
        Path output = Path.of(job.output());
        boolean confirming = phase.equals("CONFIRMATIONS");
        Run initial = confirming ? ScreeningFileUtil.readRun(output, job.manifest(), input, job.manifest().seed()) : null;
        var confirmations = new ArrayList<Run>();
        var pending = new ArrayList<Long>();
        for (long seed : seeds) {
            Run cached = ScreeningFileUtil.readRun(output, job.manifest(), input, seed);
            if (cached == null) pending.add(seed);
            else if (confirming) { confirmations.add(cached); confirmationsCompleted++; }
        }
        if (confirming && ScreeningUtil.classify(initial, confirmations, job.manifest().settings(), job.manifest().mode()).classification().equals("RETAINED")) {
            confirmationsTotal -= pending.size(); progress(job);
            return;
        }
        if (pending.isEmpty()) return;
        if (!input.sha256().equals(AutoLlmFileUtil.inputHash(input.path()))) {
            throw new IllegalStateException("Frozen input changed before worker execution: " + input.path());
        }
        I instance;
        ExecutionJournalUtil.phase("load", "LOADING", 300_000, Map.of("instance", input.path()));
        try {
            instance = instances.getInstance(input.path());
        } catch (RuntimeException failure) {
            for (long seed : pending) ScreeningFileUtil.writeRun(output, new Run(job.manifest().fingerprint(),
                    input.path(), seed, false, null, 0, List.of(), "Loading failed: " + failure));
            if (phase.equals("CONFIRMATIONS")) confirmationsCompleted += pending.size(); progress(job);
            return;
        } finally {
            ExecutionJournalUtil.finished("load", Map.of("state", "FINISHED"));
        }
        if (!warmedUp) {
            String previous = phase; phase = "WARMUP"; progress(job);
            for (int i = 0; i < job.manifest().settings().warmupRepetitions(); i++) {
                Run result = measure(job.manifest(), input, instance, sota, job.manifest().seed() - 1000 - i,
                        job.manifest().settings().warmupMillis());
                if (!result.success()) throw new IllegalStateException("Warm-up failed: " + result.failure());
            }
            warmedUp = true;
            phase = previous;
        }
        int remaining = pending.size();
        for (long seed : pending) {
            Run result = measure(job.manifest(), input, instance, sota, seed, job.manifest().settings().horizonMillis());
            ScreeningFileUtil.writeRun(output, result);
            remaining--;
            if (confirming) {
                confirmationsCompleted++;
                confirmations.add(result);
                if (ScreeningUtil.classify(initial, confirmations, job.manifest().settings(), job.manifest().mode()).classification().equals("RETAINED")) {
                    confirmationsTotal -= remaining; progress(job);
                    break;
                }
            }
            progress(job);
        }
    }
    private void progress(Job job) {
        ScreeningProgressUtil.update(Path.of(job.output()), phase, Map.of("initialCompleted", initialCompleted, "initialTotal", initialTotal,
                "confirmationsCompleted", confirmationsCompleted, "confirmationsTotal", confirmationsTotal, "workers", 1));
    }
    @SuppressWarnings("unchecked")
    Run measure(Manifest manifest, Input input, I instance, String sota, long seed, long millis) {
        String id = UUID.randomUUID().toString();
        long start = 0, elapsed = 0;
        try {
            ExecutionJournalUtil.phase(id, "BUILDING", 300_000, Map.of("instance", input.path(), "seed", seed));
            Context.Configurator.resetRandom(es.urjc.etsii.grafo.util.random.RandomType.valueOf(manifest.randomType()), seed);
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
            return new Run(manifest.fingerprint(), input.path(), seed, true, value, elapsed, curve, null);
        } catch (RuntimeException | AssertionError failure) {
            if (start != 0 && elapsed == 0) elapsed = System.nanoTime() - start;
            return new Run(manifest.fingerprint(), input.path(), seed, false, null, elapsed, List.of(), failure.toString());
        } finally {
            TimeControl.remove();
            ExecutionJournalUtil.finished(id, Map.of("state", "FINISHED", "seed", seed));
        }
    }
}
