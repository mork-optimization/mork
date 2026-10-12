package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.autoconfig.builder.ComponentSpec;
import es.urjc.etsii.grafo.autoconfig.builder.ComponentSpecJsonCodec;
import es.urjc.etsii.grafo.config.InstanceConfiguration;
import es.urjc.etsii.grafo.util.IOUtil;
import es.urjc.etsii.grafo.util.ExceptionUtil;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;
import java.io.*;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

/** Sequential, checkpointed lifecycle. Only this service mutates a running campaign. */
@Service
public class AutoLlmCampaignService {
    private final AutoLlmConfig defaults;
    private final AutoLlmCampaignStore store;
    private final AutoLlmProcessRunner processes;
    private final CodexTaskRunner codex;
    private final AutoLlmReportService<?,?> reports;
    private final InstanceConfiguration instances;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(Thread.ofVirtual().name("autollm-campaign").factory());
    private volatile String active;
    private volatile boolean cancelled;
    private Future<?> task;

    public AutoLlmCampaignService(AutoLlmConfig defaults, AutoLlmCampaignStore store, AutoLlmProcessRunner processes,
                                  CodexTaskRunner codex, AutoLlmReportService<?,?> reports, InstanceConfiguration instances) {
        this.defaults = defaults; this.store = store; this.processes = processes; this.codex = codex; this.reports = reports; this.instances = instances;
    }
    public AutoLlmConfig defaults() {
        return defaults.withPaths(defaults.trainingPath() == null ? instances.getPath("irace") : defaults.trainingPath(),
                defaults.comparisonPath() == null ? instances.getPath("autollm-comparison") : defaults.comparisonPath());
    }
    public List<String> validate(AutoLlmConfig c) {
        var errors = new ArrayList<String>();
        if (c.trials() < 1 || c.trials() > 10000) errors.add("Trials must be between 1 and 10000");
        if (c.maxParameters() < 1 || c.maxParameters() >= 300) errors.add("Parameter cap must be between 1 and 299");
        if (c.preparation().maxRepairs() < 0 || c.preparation().maxRepairs() > 100) errors.add("Repair limit must be between 0 and 100");
        if (c.preparation().pilotDurationMillis() <= 0 || c.tuning().evaluations() <= 0 || c.comparison().repetitions() <= 0) errors.add("Budgets and repetitions must be positive");
        if (c.tuning().ignoreInitialMillis() < 0 || c.tuning().intervalDurationMillis() <= 0 || c.comparison().timeLimitMillis() < 0) errors.add("Invalid algorithm time horizon");
        if (c.execution().workers() < 1 || c.execution().workers() > 256) errors.add("Worker count must be between 1 and 256");
        for (Long timeout : List.of(c.execution().watchdogGraceMillis(), c.execution().codexTimeoutMillis(), c.execution().buildTimeoutMillis(), c.execution().startupTimeoutMillis(), c.execution().searchSpaceTimeoutMillis(), c.execution().phaseTimeoutMillis(), c.execution().stageTimeoutMillis())) if (timeout <= 0) errors.add("All operational timeouts must be positive");
        if (!c.execution().heap().matches("[1-9][0-9]*[kKmMgG]")) errors.add("Heap must be a JVM size such as 2g");
        if (!Path.of(c.workspace()).toAbsolutePath().normalize().equals(store.root())) errors.add("Workspace is fixed by backend configuration");
        Path repo = Path.of(c.project().repository()).toAbsolutePath().normalize();
        if (!Files.exists(repo.resolve(".git"))) errors.add("Project must be a local Git repository");
        if (c.project().buildCommand().isEmpty() || c.project().buildCommand().getFirst().isBlank()) errors.add("Build command must be an argument array");
        for (String argument : c.project().runArguments()) {
            String key = argument.split("=", 2)[0].replace("-", "").replace("_", "").toLowerCase();
            if (!argument.startsWith("--") || !argument.contains("=") || key.startsWith("autollm") || key.startsWith("server.") || key.startsWith("instances.") || key.startsWith("irace.") || key.startsWith("event.webserver.")
                    || List.of("solver.seed", "solver.autorestart", "solver.experimentsperparameter", "solver.minimumnumberofexperiments", "solver.ignoreinitialmillis", "solver.intervaldurationmillis", "solver.logscalearea", "solver.parallelexecutor", "solver.nworkers", "solver.metrics", "solver.experiments", "solver.repetitions").contains(key)) errors.add("Run argument overrides campaign-owned setting: " + argument);
        }
        if (Path.of(c.project().artifact()).isAbsolute() || c.project().artifact().contains("..")) errors.add("Artifact must be a relative project path");
        for (String value : Arrays.asList(c.sotaFile(), c.trainingPath(), c.comparisonPath())) if (value == null || value.isBlank()) errors.add("SOTA, training, and comparison paths are required");
        if (c.sotaFile() != null && !Files.isRegularFile(resolve(repo, c.sotaFile()))) errors.add("SOTA file does not exist");
        if (c.initialConfigurationsFile() != null && !c.initialConfigurationsFile().isBlank() && !Files.isRegularFile(resolve(repo, c.initialConfigurationsFile()))) errors.add("Initial configuration file does not exist");
        if (c.trainingPath() != null && !Files.exists(resolve(repo, c.trainingPath()))) errors.add("Training input does not exist");
        if (c.comparisonPath() != null && !Files.exists(resolve(repo, c.comparisonPath()))) errors.add("Comparison input does not exist");
        return errors;
    }
    public synchronized AutoLlmCampaign create(AutoLlmConfig requested) {
        var errors = validate(requested); if (!errors.isEmpty()) throw new IllegalArgumentException(String.join("; ", errors));
        var c = new AutoLlmCampaign(); c.id = UUID.randomUUID().toString(); c.createdAt = Instant.now().toString();
        Path directory = store.directory(c.id); Path repo = Path.of(requested.project().repository()).toAbsolutePath().normalize();
        var codec = new ComponentSpecJsonCodec();
        try {
            var sota = codec.parse(Files.readString(resolve(repo, requested.sotaFile())));
            var seeds = new LinkedHashSet<ComponentSpec>(); seeds.add(sota);
            if (requested.initialConfigurationsFile() != null && !requested.initialConfigurationsFile().isBlank()) seeds.addAll(codec.parseList(Files.readString(resolve(repo, requested.initialConfigurationsFile()))));
            AutoLlmFileUtil.write(directory.resolve("control/sota.json"), codec.toJsonTree(sota));
            var seedJson = new ArrayList<Object>(); for (var seed : seeds) seedJson.add(codec.toJsonTree(seed));
            AutoLlmFileUtil.write(directory.resolve("control/seeds.json"), seedJson);
            c.config = new AutoLlmConfig(new AutoLlmConfig.Project(repo.toString(), requested.project().ref(), requested.project().buildCommand(), requested.project().artifact(), requested.project().runArguments()), requested.workspace(), requested.trials(), requested.seed(), requested.maxParameters(),
                    directory.resolve("control/sota.json").toString(), directory.resolve("control/seeds.json").toString(), resolve(repo, requested.trainingPath()).toString(), resolve(repo, requested.comparisonPath()).toString(), requested.codex(), requested.preparation(), requested.tuning(), requested.comparison(), requested.execution());
            String prompt = requested.codex().prompt();
            if (prompt == null || prompt.isBlank()) {
                if (requested.codex().promptFile() != null && !requested.codex().promptFile().isBlank()) prompt = Files.readString(resolve(repo, requested.codex().promptFile()));
                else try (var input = getClass().getResourceAsStream("/autollm/prompt.txt")) { prompt = new String(Objects.requireNonNull(input).readAllBytes(), java.nio.charset.StandardCharsets.UTF_8); }
            }
            c.prompt = prompt.replace("{{ProjectName}}", repo.getFileName().toString()).replace("{{MaxParameters}}", c.config.maxParameters().toString());
            var random = new SplittableRandom(c.config.seed()); long comparisonSeed = random.nextLong(1, Integer.MAX_VALUE);
            var assigned = new HashSet<Long>(); assigned.add(comparisonSeed);
            for (int i = 1; i <= c.config.trials(); i++) {
                var t = new AutoLlmCampaign.Trial(); t.number = i;
                do { t.pilotSeed = random.nextLong(1, Integer.MAX_VALUE); } while (!assigned.add(t.pilotSeed));
                do { t.tuningSeed = random.nextLong(1, Integer.MAX_VALUE); } while (!assigned.add(t.tuningSeed));
                t.comparisonSeed = comparisonSeed; c.trials.add(t);
            }
            store.save(c); AutoLlmFileUtil.write(directory.resolve("control/config.json"), c.config); return c;
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }
    private Path resolve(Path base, String path) { return base.resolve(path).normalize().toAbsolutePath(); }
    public synchronized AutoLlmCampaign start(String id, boolean resume) {
        if (active != null) throw new IllegalStateException("A campaign is already active");
        var c = store.load(id);
        if (!resume && !c.status.equals("DRAFT")) throw new IllegalStateException("Only draft campaigns can start");
        if (resume && !List.of("RUNNING", "INTERRUPTED", "CANCELLED").contains(c.status)) throw new IllegalStateException("Campaign has no interrupted work to resume");
        var lock = store.lock(); active = id; cancelled = false;
        try {
            if (resume) {
                processes.reconcile(store.directory(id));
                for (var t : c.trials) if (!List.of("SUCCEEDED", "FAILED", "PENDING").contains(t.status)) {
                    recoverTurn(c, t);
                    if (!t.status.equals("FAILED")) t.status = "PENDING";
                    for (var a : t.attempts) if (a.status.equals("RUNNING")) { a.status = "INTERRUPTED"; a.finishedAt = Instant.now().toString(); }
                }
            }
            c.status = "RUNNING"; c.failure = null; store.save(c);
            task = executor.submit(() -> { try { run(c); } finally { synchronized (this) { try { lock.close(); } finally { active = null; } } } });
            return c;
        } catch (RuntimeException e) { active = null; lock.close(); throw e; }
    }
    public synchronized AutoLlmCampaign cancel(String id) {
        if (!id.equals(active)) throw new IllegalStateException("Campaign is not active");
        cancelled = true; return store.load(id);
    }
    public String active() { return active; }
    public synchronized AutoLlmCampaign report(String id) {
        if (active != null) throw new IllegalStateException("Wait for the active campaign before regenerating reports");
        try (var lock = store.lock()) { var campaign = store.load(id); report(campaign, store.directory(id)); return campaign; }
    }
    private void report(AutoLlmCampaign campaign, Path directory) {
        try { CodexEvidenceUtil.report(campaign, directory); reports.report(campaign, directory); campaign.reportFailure = null; }
        catch (RuntimeException e) { campaign.reportFailure = ExceptionUtil.stacktrace(e); store.event(campaign.id, "report-failed", campaign.reportFailure); }
        campaign.reportUpdatedAt = Instant.now().toString(); store.save(campaign);
    }
    public void recover() {
        AutoLlmCampaignStore.ExecutionLock ownership;
        try { ownership = store.lock(); }
        catch (IllegalStateException e) { return; /* Another parent owns the workspace. */ }
        try (var lock = ownership) {
            for (var c : store.list()) if (c.status.equals("RUNNING")) {
                processes.reconcile(store.directory(c.id)); c.status = "INTERRUPTED";
                for (var t : c.trials) if (!List.of("SUCCEEDED", "FAILED").contains(t.status)) recoverTurn(c, t);
                for (var t : c.trials) for (var a : t.attempts) if (a.status.equals("RUNNING")) { a.status = "INTERRUPTED"; a.finishedAt = Instant.now().toString(); }
                CodexEvidenceUtil.report(c, store.directory(c.id));
                store.save(c);
            }
        }
    }
    @PreDestroy public void close() { cancelled = true; if (task != null) task.cancel(true); executor.shutdownNow(); }

    private void recoverTurn(AutoLlmCampaign c, AutoLlmCampaign.Trial trial) {
        if (trial.attempts.isEmpty()) return;
        var attempt = trial.attempts.getLast();
        if (!List.of("GENERATION", "REPAIR").contains(attempt.stage) || !List.of("RUNNING", "INTERRUPTED").contains(attempt.status)) return;
        Path root = store.directory(c.id);
        try {
            attempt.codexEvidence = codex.archive(root.resolve("trials/%03d/source".formatted(trial.number)), root.resolve(attempt.directory), trial.threadId);
            if (attempt.codexEvidence.threadId() != null) trial.threadId = attempt.codexEvidence.threadId();
            trial.resumeTurn = trial.threadId != null;
        } catch (CodexEvidenceUtil.ArchiveException e) {
            trial.status = "FAILED"; trial.failure = ExceptionUtil.stacktrace(e); trial.resumeTurn = false;
            attempt.status = "FAILED"; attempt.failure = trial.failure; attempt.finishedAt = Instant.now().toString();
        }
    }

    private void run(AutoLlmCampaign c) {
        Path root = store.directory(c.id);
        try {
            if (c.baselineCommit == null) prepareBaseline(c);
            for (var t : c.trials) {
                if (cancelled) throw new CancellationException();
                if (t.status.equals("SUCCEEDED") || t.status.equals("FAILED")) continue;
                try { runTrial(c, t); t.status = "SUCCEEDED"; t.failure = null; }
                catch (CancellationException e) { t.status = "INTERRUPTED"; throw e; }
                catch (RuntimeException e) { t.status = "FAILED"; t.failure = ExceptionUtil.stacktrace(e); store.event(c.id, "trial-failed", Map.of("trial", t.number, "failure", t.failure)); }
                store.save(c);
                report(c, root);
            }
            boolean failed = false; for (var t : c.trials) if (t.status.equals("FAILED")) failed = true;
            c.status = failed ? "COMPLETED_WITH_FAILURES" : "SUCCEEDED";
        } catch (CancellationException e) { c.status = "CANCELLED"; }
        catch (RuntimeException e) { c.status = "FAILED"; c.failure = ExceptionUtil.stacktrace(e); }
        finally {
            for (var t : c.trials) for (var a : t.attempts) if (a.status.equals("RUNNING")) { a.status = "INTERRUPTED"; a.finishedAt = Instant.now().toString(); }
            store.save(c);
            report(c, root);
        }
    }
    private void prepareBaseline(AutoLlmCampaign c) {
        Path root = store.directory(c.id); Path repo = Path.of(c.config.project().repository());
        Path preflight = root.resolve("baseline-preflight");
        var versions = new LinkedHashMap<String, String>();
        for (var tool : List.of(List.of("git", "--version"), List.of("java", "-version"), List.of("Rscript", "--version"), List.of(c.config.codex().executable(), "--version"))) {
            var dir = preflight.resolve("tool-" + versions.size()); runCommand(c, tool, repo, dir, 30_000);
            try { versions.put(tool.getFirst(), Files.readString(dir.resolve("stdout.log")) + Files.readString(dir.resolve("stderr.log"))); } catch (IOException e) { throw new UncheckedIOException(e); }
        }
        runCommand(c, List.of("Rscript", "-e", "stopifnot(requireNamespace('irace',quietly=TRUE),requireNamespace('httr',quietly=TRUE),requireNamespace('jsonlite',quietly=TRUE));cat(as.character(packageVersion('irace')))"), repo, preflight.resolve("r-packages"), 30_000);
        runCommand(c, List.of(c.config.codex().executable(), "login", "status"), repo, preflight.resolve("codex-auth"), 30_000);
        runCommand(c, List.of("git", "rev-parse", "--verify", c.config.project().ref() + "^{commit}"), repo, preflight.resolve("ref"), 30_000);
        try {
            String commit = Files.readString(preflight.resolve("ref/stdout.log")).strip();
            Path baseline = root.resolve("baseline/source"); cloneProject(c, commit, baseline, preflight.resolve("clone"));
            runCommand(c, c.config.project().buildCommand(), baseline, preflight.resolve("build"), c.config.execution().buildTimeoutMillis());
            Path jar = AutoLlmFileUtil.inside(baseline, c.config.project().artifact()); if (!Files.isRegularFile(jar)) throw new IllegalStateException("Build did not produce configured JAR: " + jar);
            child(c, null, jar, preflight.resolve("check"), "--autollm-check", 0, c.config.execution().searchSpaceTimeoutMillis(), List.of());
            var check = AutoLlmFileUtil.read(preflight.resolve("check/execution/check.json"), Map.class);
            c.config = c.config.withPaths(snapshotInputs(root, "training", (List<String>) check.get("training")), snapshotInputs(root, "comparison", (List<String>) check.get("comparison")));
            var originalBudgets = AutoLlmFileUtil.read(preflight.resolve("check/execution/comparison-time-limits.json"), Map.class);
            var frozenBudgets = new LinkedHashMap<String,Object>();
            Map<String,Map<String,Object>> comparisonInputs = AutoLlmFileUtil.read(root.resolve("data/comparison-manifest.json"), Map.class);
            for (var entry : comparisonInputs.entrySet()) frozenBudgets.put(((Map<?,?>) entry.getValue()).get("path").toString(), originalBudgets.get(entry.getKey()));
            AutoLlmFileUtil.write(root.resolve("control/comparison-time-limits.json"), frozenBudgets);
            AutoLlmFileUtil.write(root.resolve("control/config.json"), c.config);
            child(c, null, jar, preflight.resolve("probe"), "--autollm-probe", 0, c.config.execution().startupTimeoutMillis() + c.config.execution().phaseTimeoutMillis(), List.of());
            var trainSet = new HashSet<>((List<String>) check.get("training")); trainSet.retainAll((List<String>) check.get("comparison"));
            AutoLlmFileUtil.write(root.resolve("baseline/instance-overlap.json"), trainSet);
            Files.copy(preflight.resolve("check/execution/components.json"), root.resolve("baseline/components.json"), StandardCopyOption.REPLACE_EXISTING);
            AutoLlmFileUtil.write(root.resolve("baseline/source-hashes.json"), AutoLlmFileUtil.sourceHashes(baseline));
            AutoLlmFileUtil.write(root.resolve("baseline/dependencies.json"), AutoLlmFileUtil.dependencyHashes(jar));
            versions.put("irace", Files.readString(preflight.resolve("r-packages/stdout.log"))); c.versions = versions;
            var controls = new TreeMap<String,String>();
            for (String file : List.of("sota.json", "seeds.json", "comparison-time-limits.json")) controls.put("control/" + file, AutoLlmFileUtil.hash(root.resolve("control/" + file)));
            for (String name : List.of("training", "comparison")) {
                controls.put("data/" + name + ".index", AutoLlmFileUtil.hash(root.resolve("data/" + name + ".index")));
                for (var value : AutoLlmFileUtil.read(root.resolve("data/" + name + "-manifest.json"), Map.class).values()) {
                    var input = (Map<String,String>) value; controls.put(root.relativize(Path.of(input.get("path"))).toString(), input.get("sha256"));
                }
            }
            AutoLlmFileUtil.write(root.resolve("control/frozen-hashes.json"), controls);
            AutoLlmFileUtil.write(root.resolve("control/config.json"), c.config); c.baselineCommit = commit; store.save(c);
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }
    private String snapshotInputs(Path root, String name, List<String> source) throws IOException {
        var index = new ArrayList<String>(); var hashes = new LinkedHashMap<String, Object>();
        Path directory = root.resolve("data/" + name); Files.createDirectories(directory);
        for (int i = 0; i < source.size(); i++) {
            String original = source.get(i); String basename = original.substring(original.lastIndexOf('!') + 1); basename = Path.of(basename).getFileName().toString();
            Path copy = directory.resolve(Integer.toString(i)).resolve(basename); Files.createDirectories(copy.getParent());
            try (var input = IOUtil.getInputStream(original)) { Files.copy(input, copy, StandardCopyOption.REPLACE_EXISTING); }
            index.add(copy.toString()); hashes.put(original, Map.of("path", copy.toString(), "sha256", AutoLlmFileUtil.hash(copy)));
        }
        Path file = root.resolve("data/" + name + ".index"); Files.write(file, index); AutoLlmFileUtil.write(root.resolve("data/" + name + "-manifest.json"), hashes); return file.toString();
    }
    private void cloneProject(AutoLlmCampaign c, String commit, Path destination, Path output) {
        if (Files.exists(output.resolve("clone-completed.json"))) return;
        if (!Files.exists(destination.resolve(".git"))) runCommand(c, List.of("git", "clone", "--no-hardlinks", "--no-checkout", c.config.project().repository(), destination.toString()), Path.of(c.config.project().repository()), output, c.config.execution().buildTimeoutMillis());
        runCommand(c, List.of("git", "checkout", "--detach", commit), destination, output.resolve("checkout"), 30_000);
        runCommand(c, List.of("git", "submodule", "update", "--init", "--recursive"), destination, output.resolve("submodules"), c.config.execution().buildTimeoutMillis());
        AutoLlmFileUtil.write(output.resolve("clone-completed.json"), Map.of("commit", commit));
    }
    private AutoLlmCampaign.Attempt attempt(AutoLlmCampaign c, AutoLlmCampaign.Trial t, String stage) {
        var a = new AutoLlmCampaign.Attempt(); a.number = t.attempts.size() + 1; a.stage = stage;
        a.directory = "trials/%03d/attempts/%03d-%s".formatted(t.number, a.number, stage.toLowerCase()); a.startedAt = Instant.now().toString();
        t.status = stage; t.attempts.add(a); store.save(c); store.event(c.id, "stage-started", Map.of("trial", t.number, "stage", stage, "attempt", a.number)); return a;
    }
    private void finish(AutoLlmCampaign c, AutoLlmCampaign.Attempt a, RuntimeException failure) {
        a.status = failure == null ? "SUCCEEDED" : failure instanceof CancellationException ? "INTERRUPTED" : "FAILED";
        a.finishedAt = Instant.now().toString(); a.failure = failure == null ? null : ExceptionUtil.stacktrace(failure); store.save(c);
        if (List.of("GENERATION", "REPAIR").contains(a.stage)) {
            Path evidence = store.directory(c.id).resolve(a.directory).resolve("evidence.json");
            if (Files.isRegularFile(evidence)) a.codexEvidence = AutoLlmFileUtil.read(evidence, CodexEvidenceUtil.Evidence.class);
            try { CodexEvidenceUtil.report(c, store.directory(c.id)); }
            catch (RuntimeException e) { c.reportFailure = ExceptionUtil.stacktrace(e); }
            store.save(c);
        }
    }
    private void runTrial(AutoLlmCampaign c, AutoLlmCampaign.Trial t) {
        Path root = store.directory(c.id); Path source = root.resolve("trials/%03d/source".formatted(t.number));
        AutoLlmFileUtil.verifySources(root, AutoLlmFileUtil.read(root.resolve("control/frozen-hashes.json"), Map.class));
        cloneProject(c, c.baselineCommit, source, root.resolve("trials/%03d/clone".formatted(t.number)));
        Map<String,String> preserved = AutoLlmFileUtil.read(root.resolve("baseline/source-hashes.json"), Map.class);
        while (t.preparedArtifact == null) {
            if (cancelled) throw new CancellationException();
            if (t.nextStep.equals("GENERATE")) {
                boolean continuation = t.resumeTurn;
                if (t.threadId != null && !continuation && t.repairTurns >= c.config.preparation().maxRepairs()) throw new IllegalStateException("Preparation exhausted repair limit: " + t.failure);
                var a = attempt(c, t, t.threadId == null || continuation && t.generationDirectory == null ? "GENERATION" : "REPAIR"); Path dir = root.resolve(a.directory);
                if (t.threadId != null && !continuation) t.repairTurns++;
                t.resumeTurn = false; store.save(c);
                try {
                    String prompt = c.prompt + "\nBuild/test command: " + c.config.project().buildCommand() + "\nFrozen seed descriptions: " + c.config.initialConfigurationsFile() + "\nTraining inputs: " + c.config.trainingPath();
                    if (t.failure != null) prompt += "\nRepair feedback:\n" + t.failure + "\nInspect the previous verification logs under " + root.resolve("trials/%03d/attempts".formatted(t.number));
                    var result = codex.run(c.config, source, dir, prompt, t.threadId, () -> cancelled);
                    t.threadId = result.threadId(); t.blacklist = result.manifest().blacklist(); t.generationDirectory = a.directory; t.nextStep = "VERIFY"; finish(c, a, null);
                } catch (RuntimeException e) {
                    t.failure = ExceptionUtil.stacktrace(e); finish(c, a, e);
                    if (a.codexEvidence != null && a.codexEvidence.threadId() != null) t.threadId = a.codexEvidence.threadId();
                    t.resumeTurn = e instanceof CancellationException && t.threadId != null; store.save(c);
                    if (e instanceof CancellationException || e instanceof CodexEvidenceUtil.ArchiveException) throw e;
                    if (t.threadId == null) throw new IllegalStateException("Codex generation failed before creating a conversation", e);
                    continue;
                }
            }
            var a = attempt(c, t, "PREPARATION"); Path dir = root.resolve(a.directory);
            try {
                AutoLlmFileUtil.verifySources(source, preserved);
                runCommand(c, c.config.project().buildCommand(), source, dir.resolve("build"), c.config.execution().buildTimeoutMillis());
                AutoLlmFileUtil.verifySources(source, preserved);
                var generatedHashes = AutoLlmFileUtil.sourceHashes(source);
                Path jar = AutoLlmFileUtil.inside(source, c.config.project().artifact());
                var dependencies = AutoLlmFileUtil.dependencyHashes(jar);
                AutoLlmFileUtil.write(dir.resolve("dependencies.json"), dependencies);
                if (!dependencies.contentSha256().equals(AutoLlmFileUtil.read(root.resolve("baseline/dependencies.json"), AutoLlmFileUtil.Dependencies.class).contentSha256())) throw new IllegalStateException("Build dependencies changed from frozen baseline");
                child(c, t, jar, dir.resolve("check"), "--autollm-check", 0, c.config.execution().searchSpaceTimeoutMillis(), List.of());
                long begin = System.currentTimeMillis();
                child(c, t, jar, dir.resolve("probe"), "--autollm-probe", 0, c.config.preparation().pilotDurationMillis() + c.config.execution().startupTimeoutMillis(), List.of());
                t.probeDirectory = a.directory + "/probe"; store.save(c);
                long remaining = c.config.preparation().pilotDurationMillis() - (System.currentTimeMillis() - begin);
                if (remaining <= 0) throw new IllegalStateException("Pilot budget exhausted by probes; increase the verification window");
                var pilot = child(c, t, jar, dir.resolve("pilot"), "--autoconfig", remaining, c.config.execution().startupTimeoutMillis() + remaining + c.config.execution().phaseTimeoutMillis() + c.config.execution().watchdogGraceMillis(), List.of());
                boolean observed = false;
                for (var event : AutoLlmFileUtil.events(dir.resolve("pilot/execution/events.jsonl"))) if ("evaluation-result".equals(event.get("kind")) && "SUCCEEDED".equals(event.get("state"))) observed = true;
                if (!observed) throw new IllegalStateException("Verification autoconfig produced no successful evaluations");
                Path frozen = root.resolve("trials/%03d/frozen".formatted(t.number)); Files.createDirectories(frozen);
                Files.copy(jar, frozen.resolve("project.jar"), StandardCopyOption.REPLACE_EXISTING);
                AutoLlmFileUtil.archiveSources(source, frozen.resolve("source.zip"), generatedHashes);
                AutoLlmFileUtil.write(frozen.resolve("source-hashes.json"), generatedHashes);
                Files.copy(dir.resolve("check/execution/search-space.json"), frozen.resolve("search-space.json"), StandardCopyOption.REPLACE_EXISTING);
                Files.copy(dir.resolve("check/execution/components.json"), frozen.resolve("components.json"), StandardCopyOption.REPLACE_EXISTING);
                AutoLlmFileUtil.write(frozen.resolve("artifact.json"), Map.of("sha256", AutoLlmFileUtil.hash(frozen.resolve("project.jar")), "blacklist", t.blacklist == null ? "" : t.blacklist));
                t.preparedArtifact = root.relativize(frozen.resolve("project.jar")).toString(); t.failure = null; finish(c, a, null);
            } catch (IOException e) { throw new UncheckedIOException(e); }
            catch (RuntimeException e) { finish(c, a, e); if (e instanceof CancellationException) throw e; t.failure = ExceptionUtil.stacktrace(e); t.nextStep = "GENERATE"; store.save(c); }
        }
        Path jar = root.resolve(t.preparedArtifact);
        var identity = AutoLlmFileUtil.read(jar.getParent().resolve("artifact.json"), Map.class);
        if (!AutoLlmFileUtil.hash(jar).equals(identity.get("sha256"))) throw new IllegalStateException("Frozen artifact was modified");
        if (t.mainDirectory == null) {
            var a = attempt(c, t, "TUNING");
            try { child(c, t, jar, root.resolve(a.directory), "--autoconfig", 0, c.config.execution().stageTimeoutMillis(), List.of()); t.mainDirectory = a.directory; finish(c, a, null); }
            catch (RuntimeException e) { finish(c, a, e); throw e; }
        }
        if (t.winnerDirectory == null) {
            var a = attempt(c, t, "WINNER_EXPORT");
            try { child(c, t, jar, root.resolve(a.directory), "--autollm-export", 0, c.config.execution().searchSpaceTimeoutMillis(), List.of("--autollm.child.elites-file=" + root.resolve(t.mainDirectory).resolve("autoconfig-final-elites.json")));
                t.winner = AutoLlmFileUtil.read(root.resolve(a.directory).resolve("execution/winner.json"), Object.class); t.winnerDirectory = a.directory; finish(c, a, null); }
            catch (RuntimeException e) { finish(c, a, e); throw e; }
        }
        var a = attempt(c, t, "COMPARISON"); t.comparisonDirectory = a.directory; store.save(c);
        try { child(c, t, jar, root.resolve(a.directory), "--default", 0, c.config.execution().stageTimeoutMillis(), List.of("--autollm.child.comparison-enabled=true", "--autollm.child.winner-file=" + root.resolve(t.winnerDirectory).resolve("execution/winner.json"), "--solver.experiments=autollm-comparison", "--solver.repetitions=" + c.config.comparison().repetitions())); finish(c, a, null); }
        catch (RuntimeException e) { finish(c, a, e); throw e; }
    }
    private void runCommand(AutoLlmCampaign c, List<String> command, Path cwd, Path dir, long timeout) {
        var result = processes.run(command, cwd, dir, timeout, 0, c.config.execution().watchdogGraceMillis(), () -> cancelled, false, 0);
        if (result.exitCode() != 0) throw new IllegalStateException("Command failed (" + result.exitCode() + "): " + command + "; logs: " + dir);
    }
    private AutoLlmProcessRunner.Result child(AutoLlmCampaign c, AutoLlmCampaign.Trial t, Path jar, Path output, String mode, long pilotMillis, long hardTimeout, List<String> extra) {
        Path root = store.directory(c.id); Path controls = root.resolve("control/frozen-hashes.json");
        if (Files.exists(controls)) AutoLlmFileUtil.verifySources(root, AutoLlmFileUtil.read(controls, Map.class));
        String artifactHash = AutoLlmFileUtil.hash(jar);
        if (t != null && t.preparedArtifact != null && !artifactHash.equals(AutoLlmFileUtil.read(jar.getParent().resolve("artifact.json"), Map.class).get("sha256"))) throw new IllegalStateException("Frozen artifact was modified");
        long seed = t == null ? c.config.seed() : mode.equals("--default") ? t.comparisonSeed : mode.equals("--autoconfig") && pilotMillis == 0 ? t.tuningSeed : t.pilotSeed;
        var args = new ArrayList<>(List.of(Path.of(System.getProperty("java.home"), "bin/java").toString(), "-ea", "-Xmx" + c.config.execution().heap(),
                "-Dmork.execution.directory=" + output.resolve("execution"), "-Dmork.execution.phase-timeout=" + c.config.execution().phaseTimeoutMillis(), "-Dmork.execution.watchdog-grace=" + c.config.execution().watchdogGraceMillis(),
                "-jar", jar.toString(), mode, "--server.address=127.0.0.1", "--server.port=0", "--event.webserver.stop-on-execution-end=true",
                "--instances.path.irace=" + c.config.trainingPath(), "--instances.path.autollm-comparison=" + c.config.comparisonPath(), "--instances.path.default=" + c.config.trainingPath(), "--instances.preload=false",
                "--autollm.child.config-file=" + store.directory(c.id).resolve("control/config.json"), "--autollm.child.time-limits-file=" + store.directory(c.id).resolve("control/comparison-time-limits.json"), "--irace.initial-configurations-file=" + c.config.initialConfigurationsFile(), "--irace.ablation=false", "--irace.reject-execution-errors=true",
                "--solver.autorestart=true", "--solver.seed=" + seed, "--solver.experiments-per-parameter=0", "--solver.minimum-number-of-experiments=" + c.config.tuning().evaluations(),
                "--solver.ignore-initial-millis=" + c.config.tuning().ignoreInitialMillis(), "--solver.interval-duration-millis=" + c.config.tuning().intervalDurationMillis(), "--solver.log-scale-area=" + c.config.tuning().logScaleArea(),
                "--solver.parallel-executor=" + (c.config.execution().workers() > 1), "--solver.n-workers=" + c.config.execution().workers(), "--solver.metrics=true", "--serializers.xlsx.enabled=true", "--serializers.xlsx.frequency=EXPERIMENT_END"));
        if (t != null) args.addAll(3, List.of("-Dmork.execution.campaign=" + c.id, "-Dmork.execution.trial=" + t.number, "-Dmork.execution.tuning-seed=" + t.tuningSeed, "-Dmork.execution.artifact-sha256=" + artifactHash));
        if (t != null) { args.add("--autollm.child.baseline-components-file=" + store.directory(c.id).resolve("baseline/components.json")); if (t.blacklist != null && !t.blacklist.isBlank()) args.add("--blacklist=" + t.blacklist); }
        boolean newFilter = t != null && t.blacklist != null && !t.blacklist.isBlank();
        for (String argument : c.config.project().runArguments()) {
            if (newFilter && (argument.startsWith("--blacklist=") || argument.startsWith("--whitelist="))) continue;
            args.add(argument);
        }
        if (newFilter) args.add("--whitelist=");
        args.addAll(extra);
        AutoLlmFileUtil.write(output.resolve("command.json"), args);
        var result = processes.run(args, output, output, hardTimeout, pilotMillis, c.config.execution().watchdogGraceMillis(), () -> cancelled, true, c.config.execution().startupTimeoutMillis());
        if (result.plannedDeadline()) return result;
        Path terminal = output.resolve("execution/terminal.json");
        if (result.exitCode() != 0 || !Files.isRegularFile(terminal)) throw new IllegalStateException("Child failed or did not persist completion; logs: " + output);
        String outcome = (String) AutoLlmFileUtil.read(terminal, Map.class).get("outcome");
        if (!outcome.equals("SUCCEEDED")) throw new IllegalStateException("Child outcome " + outcome + "; logs: " + output);
        return result;
    }
}
