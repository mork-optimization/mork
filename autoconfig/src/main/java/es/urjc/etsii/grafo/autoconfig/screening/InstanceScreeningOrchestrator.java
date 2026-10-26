package es.urjc.etsii.grafo.autoconfig.screening;

import es.urjc.etsii.grafo.autoconfig.autollm.*;
import es.urjc.etsii.grafo.autoconfig.autollm.execution.DirectoryArchiveUtil;
import es.urjc.etsii.grafo.autoconfig.builder.AlgorithmBuilderService;
import es.urjc.etsii.grafo.config.*;
import es.urjc.etsii.grafo.create.builder.SolutionBuilder;
import es.urjc.etsii.grafo.io.*;
import es.urjc.etsii.grafo.monitoring.MonitorModeState.Mode;
import es.urjc.etsii.grafo.orchestrator.AbstractOrchestrator;
import es.urjc.etsii.grafo.services.ExecutionLifecycleCoordinator;
import es.urjc.etsii.grafo.solution.*;
import es.urjc.etsii.grafo.util.*;
import org.springframework.boot.ApplicationArguments;
import org.springframework.core.env.Environment;
import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.util.*;
import static es.urjc.etsii.grafo.autoconfig.screening.ScreeningData.*;
import static es.urjc.etsii.grafo.events.types.ExecutionEndedEvent.Outcome.*;

/** Explicit data preparation. Proposed indexes become usable only through the separate review operation. */
public class InstanceScreeningOrchestrator<S extends Solution<S,I>, I extends Instance> extends AbstractOrchestrator {
    private final InstanceScreeningConfig settings;
    private final InstanceConfiguration inputs;
    private final InstanceManager<I> instances;
    private final AlgorithmBuilderService algorithms;
    private final SolutionBuilder<S,I> solutions;
    private final Optional<SolutionValidator<S,I>> validator;
    private final SolverConfig solver;
    private final BlockConfig blocks;
    private final AutoLlmProcessRunner processes;
    private final AutoLlmConfig limits;
    private final ExecutionLifecycleCoordinator lifecycle;
    private final ApplicationArguments arguments;
    private final Environment environment;
    public InstanceScreeningOrchestrator(InstanceScreeningConfig settings, InstanceConfiguration inputs, InstanceManager<I> instances,
            AlgorithmBuilderService algorithms, SolutionBuilder<S,I> solutions, Optional<SolutionValidator<S,I>> validator,
            SolverConfig solver, BlockConfig blocks, AutoLlmProcessRunner processes, AutoLlmConfig limits,
            ExecutionLifecycleCoordinator lifecycle, ApplicationArguments arguments, Environment environment) {
        this.settings = settings; this.inputs = inputs; this.instances = instances; this.algorithms = algorithms;
        this.solutions = solutions; this.validator = validator; this.solver = solver; this.blocks = blocks;
        this.processes = processes; this.limits = limits; this.lifecycle = lifecycle; this.arguments = arguments; this.environment = environment;
    }
    @Override public List<String> getNames() { return List.of("instance-screening", "instance-screening-worker", "instance-screening-approve"); }
    @Override public Mode getMode() { return Mode.UTILITY; }
    @Override public void run(String... args) {
        long start = System.nanoTime(); var outcome = SUCCEEDED;
        try {
            Context.Configurator.setSolverConfig(solver);
            Context.Configurator.setBlockConfig(blocks);
            if (limits.execution().target() == AutoLlmConfig.Target.SSH && !arguments.containsOption("instance-screening-worker")) {
                Path output = Path.of(settings.output()).toAbsolutePath().normalize(); Files.createDirectories(output);
                try (var channel = FileChannel.open(output.resolve(".screening.lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE); var lock = channel.tryLock()) {
                    if (lock == null) throw new IllegalStateException("Screening output is already in use");
                    new RemoteScreeningService(processes, update -> {}, () -> Thread.currentThread().isInterrupted()).run(limits, settings, inputs, solver, arguments.containsOption("instance-screening-approve"),
                            environment.getProperty("instance-screening.review-sha256"), List.of(arguments.getSourceArgs()));
                }
                return;
            }
            if (arguments.containsOption("instance-screening-worker")) {
                new ScreeningWorker<>(instances, algorithms, solutions, validator.orElseThrow(() ->
                        new IllegalStateException("Screening requires a SolutionValidator"))).run(Path.of(settings.jobFile()));
                return;
            }
            Path output = Path.of(settings.output()).toAbsolutePath().normalize();
            Files.createDirectories(output);
            try (var channel = FileChannel.open(output.resolve(".screening.lock"), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
                 var lock = channel.tryLock()) {
                if (lock == null) throw new IllegalStateException("Screening output is already in use");
                String artifact = ScreeningFileUtil.artifactHash(instances.getUserImporterImplementation().getClass());
                if (arguments.containsOption("instance-screening-approve")) {
                    ScreeningFileUtil.approve(output, environment.getProperty("instance-screening.review-sha256"), artifact);
                    System.out.println("APPROVED: " + output.resolve("train.index") + " and " + output.resolve("test.index"));
                } else prepare(output, artifact);
            }
        } catch (IOException e) { outcome = FAILED; throw new UncheckedIOException(e); }
        catch (RuntimeException | Error e) { outcome = FAILED; throw e; }
        finally { lifecycle.complete(System.nanoTime() - start, outcome); }
    }
    private void prepare(Path output, String artifact) throws IOException {
        if (Files.exists(output.resolve("approved.json"))) throw new IllegalStateException("This selection is approved; use a new directory to rescreen");
        if (validator.isEmpty()) throw new IllegalStateException("Screening requires a SolutionValidator");
        if (settings.sotaFile() == null) throw new IllegalArgumentException("Provide --instance-screening.sota-file=<fixed SOTA JSON>");
        String python = settings.python();
        if (python.contains("/") || python.contains("\\")) python = Path.of(python).toAbsolutePath().normalize().toString();
        var config = new InstanceScreeningConfig(Path.of(settings.sotaFile()).toAbsolutePath().normalize().toString(), output.toString(),
                settings.thresholdMillis(), settings.candidateMultiplier(), settings.horizonMillis(), settings.confirmationRuns(),
                settings.fastFraction(), python, settings.warmupRepetitions(), settings.warmupMillis(), null);
        String sota = Files.readString(Path.of(config.sotaFile()));
        algorithms.buildAlgorithmFromJson(sota);
        double fraction = inputs.getPreliminarPercentage();
        if (!Double.isFinite(fraction) || fraction <= 0 || fraction >= 1) throw new IllegalArgumentException("Training fraction must be between zero and one");
        String inputPath = inputs.getForSelection();
        if (inputPath == null || inputPath.isBlank()) inputPath = inputs.getPath("default");
        inputPath = Path.of(inputPath).toAbsolutePath().normalize().toString();
        if (Files.isDirectory(Path.of(inputPath)) && output.startsWith(Path.of(inputPath))) {
            throw new IllegalArgumentException("Place screening output outside the input directory");
        }
        var paths = new LinkedHashMap<>(inputs.getPaths()); paths.put("instance-screening", inputPath); inputs.setPath(paths);
        var source = new TreeSet<String>();
        for (String path : instances.getInstanceSolveOrder("instance-screening", false)) source.add(IOUtil.absoluteLoadPath(path));
        if (source.isEmpty()) throw new IllegalArgumentException("No instances to screen");
        var all = new ArrayList<Input>();
        for (String path : source) all.add(new Input(path, AutoLlmFileUtil.inputHash(path)));
        var host = ScreeningFileUtil.host();
        String sotaHash = AutoLlmFileUtil.hash(Path.of(config.sotaFile()));
        String objective = Context.getMainObjective().getName(); var mode = Context.getMainObjective().getFMode();
        Math.addExact(solver.getSeed(), config.confirmationRuns());
        Math.subtractExact(solver.getSeed(), 1000L + config.warmupRepetitions());
        var runArguments = List.of(arguments.getSourceArgs());
        String sourceMetadata = environment.getProperty("instance-screening.provenance-file");
        String sourceHash = sourceMetadata == null ? "" : AutoLlmFileUtil.hash(Path.of(sourceMetadata));
        String fingerprint = ScreeningFileUtil.digest(List.of(config, solver.getSeed(), solver.getRandomType(), fraction, objective, mode, host, all, sotaHash, artifact, runArguments, sourceHash));
        var manifest = new Manifest(3, fingerprint, config, solver.getSeed(), solver.getRandomType().name(), fraction, objective, mode, host, all, sotaHash, artifact, runArguments, "QUALIFIED_SERVER_POOL");
        Path manifestFile = output.resolve("manifest.json");
        if (Files.exists(manifestFile)) {
            Manifest previous = AutoLlmFileUtil.read(manifestFile, Manifest.class);
            if (!previous.equals(manifest)) throw new IllegalStateException("Screening inputs, SOTA, executable, host or settings changed; use a new directory");
        } else {
            try (var files = Files.list(output)) {
                if (files.anyMatch(path -> !path.getFileName().toString().equals(".screening.lock"))) {
                    throw new IllegalStateException("Refusing to use a nonempty directory without a screening manifest");
                }
            }
            AutoLlmFileUtil.write(manifestFile, manifest);
        }
        ScreeningFileUtil.writeText(output.resolve("sota.json"), sota);
        processes.reconcile(output);
        var preflight = processes.run(List.of(python, "-c", "import numpy,pandas,sklearn,yellowbrick,matplotlib,seaborn; print('Instance selector dependencies available')"),
                output, output.resolve("python-check"), 30_000, 0, 0, () -> Thread.currentThread().isInterrupted(), false, 0);
        if (preflight.exitCode() != 0) throw new IllegalStateException("Selector Python dependencies are unavailable; see " + output.resolve("python-check/stderr.log") + "; configure --instance-screening.python=<prepared interpreter>");
        var decisions = new ArrayList<Decision>();
        System.out.println("Screening " + all.size() + " instances with fixed SOTA; threshold=" + config.thresholdMillis() + " ms, horizon=" + config.horizonMillis() + " ms");
        runJob(manifest, output);
        ScreeningProgressUtil.update(output, "SELECTION", Map.of());
        for (Input input : all) {
            Run initial = ScreeningFileUtil.readRun(output, manifest, input, manifest.seed());
            var confirmation = ScreeningUtil.candidate(initial, config, mode)
                    ? ScreeningFileUtil.readConfirmations(output, manifest, input) : List.<Run>of();
            Decision decision = ScreeningUtil.classify(initial, confirmation, config, mode);
            decisions.add(decision);
            System.out.println(decision.classification() + " " + input.path() + " (" + decision.fastHits() + "/" + decision.confirmations() + " completed confirmations are fast common-target hits; limit " + config.confirmationRuns() + ")");
        }
        writeProposal(output, manifest, decisions);
    }
    private List<String> workerCommand(Path output, Path jobFile) {
        var command = ScreeningFileUtil.javaCommand(instances.getUserImporterImplementation().getClass());
        command.add(2, "-Xmx" + limits.execution().heap());
        command.add(2, "-Dmork.execution.directory=" + output.resolve("execution"));
        for (String argument : arguments.getSourceArgs()) {
            String key = argument.split("=", 2)[0].replace("-", "").replace("_", "").toLowerCase();
            if (!argument.startsWith("--") || key.startsWith("instancescreening") || key.startsWith("server.")
                    || key.startsWith("instances.") || key.startsWith("event.webserver.") || key.startsWith("serializers.")
                    || Set.of("solver.metrics", "solver.parallelexecutor", "solver.nworkers", "solver.timestats", "solver.benchmark", "logging.level.root").contains(key)) continue;
            command.add(argument);
        }
        command.addAll(List.of("--instance-screening-worker", "--instance-screening.job-file=" + jobFile,
                "--server.address=127.0.0.1", "--server.port=0", "--event.webserver.stop-on-execution-end=true",
                "--instances.preload=false", "--solver.parallel-executor=false", "--solver.n-workers=1", "--solver.metrics=true",
                "--solver.time-stats=false", "--solver.benchmark=false", "--serializers.xlsx.enabled=false", "--logging.level.root=WARN"));
        return command;
    }
    private void runJob(Manifest manifest, Path output) {
        Path attempt = output.resolve("jobs").resolve("screening-" + UUID.randomUUID());
        Path jobFile = attempt.resolve("job.json");
        AutoLlmFileUtil.write(jobFile, new Job(manifest, output.toString()));
        try {
            long maximumRuns = (long) manifest.inputs().size() * (manifest.settings().confirmationRuns() + 1)
                    + manifest.settings().warmupRepetitions();
            long timeout = limits.execution().startupTimeoutMillis() + maximumRuns
                    * (2 * limits.execution().phaseTimeoutMillis() + manifest.settings().horizonMillis() + limits.execution().watchdogGraceMillis());
            var result = processes.run(workerCommand(attempt, jobFile), attempt, attempt, timeout, 0,
                    limits.execution().watchdogGraceMillis(), () -> Thread.currentThread().isInterrupted(), true, limits.execution().startupTimeoutMillis());
            Path terminal = attempt.resolve("execution/terminal.json");
            if (result.exitCode() != 0 || !Files.exists(terminal)
                    || !"SUCCEEDED".equals(AutoLlmFileUtil.JSON.readTree(Files.readString(terminal)).path("outcome").asString())) {
                throw new IllegalStateException("Screening worker failed; completed measurements are saved. Resume with the same command; inspect " + attempt);
            }
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }
    private void writeProposal(Path output, Manifest manifest, List<Decision> decisions) throws IOException {
        var retained = new ArrayList<String>();
        int trivial = 0, inconclusive = 0;
        for (Decision decision : decisions) {
            if (decision.classification().equals("TRIVIAL")) trivial++;
            else retained.add(decision.instance());
            if (decision.classification().equals("INCONCLUSIVE")) inconclusive++;
        }
        ScreeningFileUtil.index(output.resolve("proposed-retained.index"), retained);
        var training = new ArrayList<String>(); var testing = new ArrayList<String>();
        String splitFailure = null;
        if (retained.isEmpty()) splitFailure = "No retained instances; cannot create nonempty training and testing sets";
        else try {
            Path selector = output.resolve("selector.py");
            try (var resource = getClass().getResourceAsStream("/instance-selector/instance-selector.py")) {
                if (resource == null) throw new IllegalStateException("Bundled instance selector is missing");
                Files.copy(resource, selector, StandardCopyOption.REPLACE_EXISTING);
            }
            InstanceSelectionUtil.writeProperties(output.resolve("properties.csv"), retained, instances);
            Path selection = output.resolve("selection");
            var result = processes.run(List.of(manifest.settings().python(), selector.toString(), "--properties", output.resolve("properties.csv").toString(),
                            "--output", selection.toString(), "--size", Double.toString(manifest.trainingFraction()), "--seed", Long.toString(Math.floorMod(manifest.seed(), Integer.MAX_VALUE))),
                    output, output.resolve("selector-process"), limits.execution().buildTimeoutMillis(), 0, 0,
                    () -> Thread.currentThread().isInterrupted(), false, 0);
            if (result.exitCode() != 0) throw new IllegalStateException("Property-based selector failed; inspect selector-process logs");
            var selected = new LinkedHashSet<String>();
            for (String line : Files.readAllLines(selection.resolve("selected_instances.index"))) if (!line.isBlank()) selected.add(IOUtil.absoluteLoadPath(line));
            if (selected.size() != Math.ceil(manifest.trainingFraction() * retained.size()) || !retained.containsAll(selected)) {
                throw new IllegalStateException("Selector returned an unexpected training size or unknown instance");
            }
            for (String path : retained) if (selected.contains(path)) training.add(path);
            testing.addAll(retained);
            if (training.isEmpty() || testing.isEmpty()) throw new IllegalStateException("Selector produced an empty split");
        } catch (RuntimeException | IOException e) { splitFailure = e.toString(); training.clear(); testing.clear(); }
        ScreeningFileUtil.index(output.resolve("proposed-train.index"), training);
        ScreeningFileUtil.index(output.resolve("proposed-test.index"), testing);
        try (var writer = Files.newBufferedWriter(output.resolve("instances.csv"))) {
            InstanceSelectionUtil.writeRow(writer, List.of("instance", "classification", "bestObserved", "firstPassTtbNanos", "confirmations", "fastHits", "failures", "commonTargetAttainments", "confirmationTtbMedianNanos", "confirmationTtbP95Nanos", "confirmationTtbMaxNanos", "reason"));
            for (int i = 0; i < decisions.size(); i++) {
                Decision d = decisions.get(i);
                var runs = ScreeningFileUtil.readConfirmations(output, manifest, manifest.inputs().get(i));
                var ttb = ScreeningUtil.summarize(runs, d.bestObserved(), manifest.mode(), manifest.settings().horizonNanos());
                InstanceSelectionUtil.writeRow(writer, Arrays.asList(d.instance(), d.classification(), d.bestObserved(), d.firstPassTtbNanos(),
                        d.confirmations(), d.fastHits(), d.failures(), ttb.attained(), ttb.medianNanos(), ttb.p95Nanos(), ttb.maxNanos(), d.reason()));
            }
        }
        String report = "# Instance screening: PENDING REVIEW\n\n"
                + "Fixed SOTA; " + manifest.settings().horizonMillis() + " ms observation horizon; fast threshold " + manifest.settings().thresholdMillis() + " ms.\n\n"
                + "One assertions-disabled worker JVM is reused across the initial pass and all confirmations, with a single warm-up sequence. Seeds, metrics and time limits reset before every run.\n\n"
                + "| Input | Trivial/excluded | Retained (including inconclusive) | Inconclusive | Training | Testing |\n| ---: | ---: | ---: | ---: | ---: | ---: |\n"
                + "| " + decisions.size() + " | " + trivial + " | " + retained.size() + " | " + inconclusive + " | " + training.size() + " | " + testing.size() + " |\n\n"
                + "Exclude only when the first-pass TTB is at most " + manifest.settings().candidateMultiplier() + " times the fast threshold and strictly more than "
                + manifest.settings().fastFraction() * 100 + "% of " + manifest.settings().confirmationRuns() + " fresh valid runs reach the common observed best before the threshold.\n\n"
                + "Confirmations stop early and retain the instance when even all remaining runs reaching the current common target before the threshold cannot meet that fraction. The common target and completed fast hits are recomputed after every run. Counts and timing summaries include only completed confirmations; early stopping is recorded in the decision reason.\n\n"
                + "All decisions, reasons and confirmation TTB summaries: [instances.csv](instances.csv). Summaries include only runs attaining the common target; the attainment count shows how many reached it. Raw objective curves and seeds: runs/. Failed or inconsistent evidence is retained as inconclusive.\n\n"
                + "SOTA SHA-256: `" + manifest.sotaSha256() + "`. Executable SHA-256: `" + manifest.artifactSha256() + "`.\n\n"
                + "Reuse scope: QUALIFIED_SERVER_POOL. This proposal permits the same frozen selection on qualified execution servers. Measurements and exclusion timing remain evidence from the screening host only.\n\n"
                + "Objective: " + manifest.objective() + " (" + manifest.mode() + "). Host: " + manifest.host() + ".\n\n"
                + (splitFailure == null ? "Testing contains every retained instance, including all selected training instances. The training/testing overlap is intentional.\n\n" : "Split failure: " + splitFailure + ". No approved indexes can be published.\n\n")
                + "This is an empirical criterion for this SOTA/host within this observation horizon; it is not an optimality or statistical-confidence claim.\n\n"
                + "Show this report and the detailed lists to the user and obtain explicit agreement before approval or campaign configuration. Original instances and indexes are untouched.\n";
        ScreeningFileUtil.writeText(output.resolve("report.md"), report);
        String sourceMetadata = environment.getProperty("instance-screening.provenance-file");
        if (sourceMetadata != null) Files.copy(Path.of(sourceMetadata), output.resolve("source-metadata.json"), StandardCopyOption.REPLACE_EXISTING);
        var files = new LinkedHashMap<String,String>();
        for (String file : List.of("manifest.json", "sota.json", "report.md", "instances.csv", "proposed-retained.index", "proposed-train.index", "proposed-test.index")) {
            files.put(file, AutoLlmFileUtil.hash(output.resolve(file)));
        }
        if (sourceMetadata != null) files.put("source-metadata.json", AutoLlmFileUtil.hash(output.resolve("source-metadata.json")));
        var runHashes = DirectoryArchiveUtil.hashes(output.resolve("runs"));
        AutoLlmFileUtil.write(output.resolve("run-hashes.json"), runHashes);
        files.put("run-hashes.json", AutoLlmFileUtil.hash(output.resolve("run-hashes.json")));
        AutoLlmFileUtil.write(output.resolve("proposal.json"), new Proposal(manifest.fingerprint(), "PENDING_REVIEW", decisions, files, training.size(), testing.size(), splitFailure));
        ScreeningProgressUtil.update(output, "AWAITING_APPROVAL", Map.of("excluded", trivial, "retained", retained.size(), "inconclusive", inconclusive, "training", training.size(), "testing", testing.size()));
        System.out.println(report);
        System.out.println("Proposal SHA-256 for explicit user approval: " + AutoLlmFileUtil.hash(output.resolve("proposal.json")));
        System.out.println("Report: " + output.resolve("report.md"));
    }
}
