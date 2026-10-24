package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.autoconfig.autollm.execution.*;
import es.urjc.etsii.grafo.util.IOUtil;
import es.urjc.etsii.grafo.util.ExceptionUtil;
import tools.jackson.core.type.TypeReference;
import java.io.*;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;

/** Owns one target's execution model. Only immutable snapshots cross into the coordinator. */
public final class AutoLlmTrialRunner {
    public interface Listener {
        void state(AutoLlmCampaign snapshot);
        void event(String kind, Object detail);
    }
    private final AutoLlmCampaign model;
    private final Path executionRoot;
    private final AutoLlmProcessRunner processes;
    private final CodexTaskRunner codex;
    private final AutoLlmExecutionService workloads;
    private final Listener listener;
    private final java.util.function.BooleanSupplier stop;
    private final Semaphore sessions;
    private final ReentrantLock metadataLock = new ReentrantLock();
    private record PreflightTool(List<String> command, boolean local) {}
    public AutoLlmTrialRunner(AutoLlmCampaign model, Path root, AutoLlmProcessRunner processes, CodexTaskRunner codex,
                              Listener listener, java.util.function.BooleanSupplier stop, Semaphore sessions) {
        this.model = model; this.executionRoot = root; this.processes = processes; this.codex = codex;
        this.workloads = new AutoLlmExecutionService(processes); this.listener = listener; this.stop = stop; this.sessions = sessions;
    }
    private boolean cancelled() { return stop.getAsBoolean(); }
    private synchronized void publish(AutoLlmCampaign campaign) { listener.state(AutoLlmPoolUtil.copy(campaign, AutoLlmCampaign.class)); }
    private void event(String kind, Object detail) { listener.event(kind, detail); }
    public AutoLlmCampaign snapshot() { return AutoLlmPoolUtil.copy(model, AutoLlmCampaign.class); }
    public void prepare(boolean metadataOnly) { prepareBaseline(model, metadataOnly); }
    public String endpoint() {
        if (model.config.execution().target() == AutoLlmConfig.Target.LOCAL) return "local";
        var server = model.config.execution().ssh().onlyServer();
        var command = new ArrayList<>(List.of("ssh", "-G", "-oBatchMode=yes"));
        if (server.configFile() != null) command.addAll(List.of("-F", server.configFile()));
        command.addAll(List.of("--", server.host()));
        Path output = executionRoot.resolve("endpoint");
        var result = processes.run(command, executionRoot, output, 30000, 0, 1000, stop, false, 0);
        if (result.exitCode() != 0) throw new IllegalStateException("Cannot resolve SSH endpoint; inspect " + output);
        try {
            String host = null, port = null;
            for (String line : Files.readAllLines(output.resolve("stdout.log"))) {
                if (line.startsWith("hostname ")) host = line.substring(9).strip();
                if (line.startsWith("port ")) port = line.substring(5).strip();
            }
            if (host == null || port == null) throw new IllegalStateException("SSH did not resolve hostname and port");
            return java.net.InetAddress.getByName(host).getHostAddress() + ":" + port;
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }
    public void cancelOutstanding() { workloads.cancelOutstanding(model.config, executionRoot, state -> remoteState(model, state)); }
    public void reconcile() {
        processes.reconcile(executionRoot);
        for (var trial : model.trials) {
            recoverTurn(model, trial);
            for (var a : trial.attempts) if (a.status.equals("RUNNING")) { a.status = "INTERRUPTED"; a.finishedAt = Instant.now().toString(); }
        }
    }
    public void execute() {
        var c = model; var t = c.trials.getFirst();
        c.problem = null; c.failure = null; c.status = "RUNNING";
        try {
            reconcile();
            if (c.config.execution().target() == AutoLlmConfig.Target.SSH) workloads.probe(c.config, executionRoot);
            if (t.problem != null && t.problem.code().equals("ArchiveException")) throw new AutoLlmFailureUtil.StageException(t.problem, null);
            while (true) {
                try { runTrial(c, t); break; }
                catch (RuntimeException e) {
                    if (e instanceof CancellationException) throw e;
                    var problem = AutoLlmFailureUtil.describe(e, executionRoot.resolve("trials/%03d".formatted(t.number)));
                    if (!problem.retryable() || t.infrastructureRetries >= AutoLlmFailureUtil.MAX_RETRIES) throw e;
                    t.infrastructureRetries++; t.problem = problem; c.problem = null; c.failure = null; publish(c);
                    event("infrastructure-retry", Map.of("trial", t.number, "retry", t.infrastructureRetries, "failure", problem));
                    try { Thread.sleep(1000L * t.infrastructureRetries); }
                    catch (InterruptedException interrupted) { Thread.currentThread().interrupt(); throw new CancellationException(); }
                }
            }
            t.status = "SUCCEEDED"; t.failure = null; t.problem = null; c.status = "SUCCEEDED";
        } catch (CancellationException e) { t.status = "INTERRUPTED"; c.status = "INTERRUPTED"; }
        catch (RuntimeException e) {
            t.problem = AutoLlmFailureUtil.describe(e, executionRoot); t.failure = ExceptionUtil.stacktrace(e);
            t.status = t.problem.category() == AutoLlmFailureUtil.Category.COMPONENT ? "FAILED" : "BLOCKED";
            c.problem = t.problem; c.failure = t.failure; c.status = t.status.equals("FAILED") ? "COMPLETED_WITH_FAILURES" : "NEEDS_ATTENTION";
        } finally {
            for (var a : t.attempts) if (a.status.equals("RUNNING")) { a.status = "INTERRUPTED"; a.finishedAt = Instant.now().toString(); }
            publish(c);
        }
    }
    private void recoverTurn(AutoLlmCampaign c, AutoLlmCampaign.Trial trial) {
        if (trial.attempts.isEmpty()) return;
        var attempt = trial.attempts.getLast();
        if (!List.of("GENERATION", "REPAIR").contains(attempt.stage) || !List.of("RUNNING", "INTERRUPTED", "FAILED").contains(attempt.status)) return;
        Path root = executionRoot;
        try {
            attempt.codexEvidence = codex.archive(root.resolve("trials/%03d/source".formatted(trial.number)), root.resolve(attempt.directory), trial.threadId);
            if (attempt.codexEvidence.threadId() != null) trial.threadId = attempt.codexEvidence.threadId();
            trial.resumeTurn = trial.threadId != null;
            trial.problem = null;
        } catch (CodexEvidenceUtil.ArchiveException e) {
            Path evidence = root.resolve(attempt.directory).resolve("evidence.json");
            if (Files.isRegularFile(evidence)) attempt.codexEvidence = AutoLlmFileUtil.read(evidence, CodexEvidenceUtil.Evidence.class);
            trial.status = "BLOCKED"; trial.failure = ExceptionUtil.stacktrace(e); trial.resumeTurn = false;
            trial.problem = AutoLlmFailureUtil.describe(e, root.resolve(attempt.directory)); c.problem = trial.problem; c.status = "NEEDS_ATTENTION";
            attempt.status = "FAILED"; attempt.failure = trial.failure; attempt.finishedAt = Instant.now().toString();
        }
    }

    private void prepareBaseline(AutoLlmCampaign c, boolean metadataOnly) {
        Path root = executionRoot; Path repo = Path.of(c.config.project().repository());
        Path preflight = root.resolve("baseline-preflight");
        var versions = new LinkedHashMap<String, String>();
        if (c.config.execution().target() == AutoLlmConfig.Target.LOCAL) {
            try { Files.createDirectories(root); c.executionHost = TargetPreflightUtil.identity(c.config, root, List.of()); }
            catch (IOException e) { throw new UncheckedIOException(e); }
        }
        if (c.config.execution().target() == AutoLlmConfig.Target.SSH) {
            var host = workloads.probe(c.config, root); c.executionHost = host;
            if (c.approvedScreeningPath == null) c.approvedScreeningPath = Path.of(c.config.trainingPath()).getParent().getParent().toString();
            es.urjc.etsii.grafo.autoconfig.screening.RemoteScreeningService.approved(screenedConfig(c));
            AutoLlmFileUtil.write(root.resolve("control/config.json"), c.config); publish(c);
        }
        for (var tool : List.of(new PreflightTool(List.of("git", "--version"), true), new PreflightTool(List.of("java", "-version"), false),
                new PreflightTool(List.of("Rscript", "--version"), false), new PreflightTool(List.of(c.config.codex().executable(), "--version"), true))) {
            var dir = preflight.resolve("tool-" + versions.size()); runCommand(c, tool.command(), repo, dir, 30_000, tool.local());
            try { versions.put(tool.command().getFirst(), Files.readString(dir.resolve("stdout.log")) + Files.readString(dir.resolve("stderr.log"))); } catch (IOException e) { throw new UncheckedIOException(e); }
        }
        runCommand(c, List.of("Rscript", "-e", "stopifnot(requireNamespace('irace',quietly=TRUE),requireNamespace('httr',quietly=TRUE),requireNamespace('jsonlite',quietly=TRUE));cat(as.character(packageVersion('irace')))"), repo, preflight.resolve("r-packages"), 30_000, false);
        runCommand(c, List.of(c.config.codex().executable(), "login", "status"), repo, preflight.resolve("codex-auth"), 30_000, true);
        runCommand(c, List.of("git", "rev-parse", "--verify", c.config.project().ref() + "^{commit}"), repo, preflight.resolve("ref"), 30_000, true);
        try {
            String commit = Files.readString(preflight.resolve("ref/stdout.log")).strip();
            c.config = c.config.withRef(commit);
            Path baseline = root.resolve("baseline/source"); cloneProject(c, commit, baseline, preflight.resolve("clone"));
            if (c.config.execution().target() == AutoLlmConfig.Target.SSH) {
                var prepared = es.urjc.etsii.grafo.autoconfig.screening.RemoteScreeningService.approved(screenedConfig(c));
                if (!commit.equals(prepared.commit())) throw new IllegalStateException("Baseline commit differs from approved screening");
                Path screeningRoot = Path.of(c.approvedScreeningPath);
                var manifest = AutoLlmFileUtil.read(screeningRoot.resolve("proposal/manifest.json"), es.urjc.etsii.grafo.autoconfig.screening.ScreeningData.Manifest.class);
                if (!"QUALIFIED_SERVER_POOL".equals(manifest.reusePolicy()) || !prepared.sotaSha256().equals(AutoLlmFileUtil.hash(Path.of(c.config.sotaFile())))) throw new IllegalStateException("Approved selection policy or SOTA changed");
                Path screened = screeningRoot.resolve(prepared.artifact());
                if (!prepared.artifactSha256().equals(AutoLlmFileUtil.hash(screened))) throw new IllegalStateException("Approved screening executable changed");
                Path destination = baseline.resolve(c.config.project().artifact()); Files.createDirectories(destination.getParent()); Files.copy(screened, destination, StandardCopyOption.REPLACE_EXISTING);
                AutoLlmFileUtil.write(preflight.resolve("build/adopted-screening.json"), Map.of("artifactSha256", prepared.artifactSha256(), "buildEvidence", screeningRoot.resolve("build").toString()));
            } else runCommand(c, c.config.project().buildCommand(), baseline, preflight.resolve("build"), c.config.execution().buildTimeoutMillis(), false);
            Path jar = AutoLlmFileUtil.inside(baseline, c.config.project().artifact()); if (!Files.isRegularFile(jar)) throw new IllegalStateException("Build did not produce configured JAR: " + jar);
            if (c.config.execution().target() == AutoLlmConfig.Target.SSH) c.config = workloads.stageInputs(c.config, root);
            child(c, null, jar, preflight.resolve("check"), "--autollm-check", 0, c.config.execution().searchSpaceTimeoutMillis(), List.of());
            var check = AutoLlmExecutionService.check(c.config, root, preflight.resolve("check/execution/check.json"));
            if (check.parameters() > c.config.maxParameters() && !metadataOnly) throw AutoLlmFailureUtil.error(AutoLlmFailureUtil.Category.INFRASTRUCTURE, "BASELINE_SEARCH_SPACE", "Baseline exceeds the configured parameter limit; prune while preserving all seeds before starting", preflight.resolve("check"));
            c.config = c.config.withPaths(snapshotInputs(root, "training", check.training()), snapshotInputs(root, "comparison", check.comparison()));
            Map<String,Long> originalBudgets = AutoLlmFileUtil.read(preflight.resolve("check/execution/comparison-time-limits.json"), new TypeReference<>() {});
            var localBudgets = new LinkedHashMap<String,Long>();
            for (var budget : originalBudgets.entrySet()) localBudgets.put(AutoLlmExecutionService.localPath(c.config, root, budget.getKey()), budget.getValue());
            var frozenBudgets = new LinkedHashMap<String,Long>();
            Map<String,Map<String,String>> comparisonInputs = AutoLlmFileUtil.read(root.resolve("data/comparison-manifest.json"), new TypeReference<>() {});
            for (var entry : comparisonInputs.entrySet()) frozenBudgets.put(entry.getValue().get("path"), localBudgets.get(entry.getKey()));
            AutoLlmFileUtil.write(root.resolve("control/comparison-time-limits.json"), frozenBudgets);
            AutoLlmFileUtil.write(root.resolve("control/config.json"), c.config);
            if (!metadataOnly) child(c, null, jar, preflight.resolve("probe"), "--autollm-probe", 0, c.config.execution().startupTimeoutMillis() + c.config.execution().phaseTimeoutMillis(), List.of());
            var trainSet = new HashSet<>(check.training()); trainSet.retainAll(check.comparison());
            AutoLlmFileUtil.write(root.resolve("baseline/instance-overlap.json"), trainSet);
            Files.copy(preflight.resolve("check/execution/components.json"), root.resolve("baseline/components.json"), StandardCopyOption.REPLACE_EXISTING);
            AutoLlmFileUtil.write(root.resolve("baseline/source-hashes.json"), AutoLlmFileUtil.sourceHashes(baseline));
            AutoLlmFileUtil.write(root.resolve("baseline/dependencies.json"), AutoLlmFileUtil.dependencyHashes(jar));
            if (!Files.exists(preflight.resolve("reproducibility/verified.json"))) {
                Path replay = root.resolve("baseline-replay/source"), evidence = preflight.resolve("reproducibility");
                cloneProject(c, commit, replay, evidence.resolve("clone"));
                runCommand(c, c.config.project().buildCommand(), replay, evidence.resolve("build"), c.config.execution().buildTimeoutMillis(), false);
                verifyDependencies(jar, replay.resolve(c.config.project().artifact()), evidence);
                child(c, null, replay.resolve(c.config.project().artifact()), evidence.resolve("check"), "--autollm-check", 0, c.config.execution().searchSpaceTimeoutMillis(), List.of());
                var repeated = AutoLlmExecutionService.check(c.config, root, evidence.resolve("check/execution/check.json"));
                if (repeated.parameters() != check.parameters() || !Arrays.equals(AutoLlmFileUtil.read(root.resolve("baseline/components.json"), String[].class), AutoLlmFileUtil.read(evidence.resolve("check/execution/components.json"), String[].class)))
                    throw AutoLlmFailureUtil.error(AutoLlmFailureUtil.Category.INTEGRITY, "BASELINE_METADATA_CHANGED", "Unchanged baseline emitted different metadata after relocation", evidence);
                AutoLlmFileUtil.write(evidence.resolve("verified.json"), Map.of("commit", commit, "fingerprintVersion", 2));
            }
            versions.put("irace", Files.readString(preflight.resolve("r-packages/stdout.log"))); c.versions = versions;
            var controls = new TreeMap<String,String>();
            for (String file : List.of("sota.json", "seeds.json", "comparison-time-limits.json")) controls.put("control/" + file, AutoLlmFileUtil.hash(root.resolve("control/" + file)));
            for (String name : List.of("training", "comparison")) {
                controls.put("data/" + name + ".index", AutoLlmFileUtil.hash(root.resolve("data/" + name + ".index")));
                Map<String,Map<String,String>> manifest = AutoLlmFileUtil.read(root.resolve("data/" + name + "-manifest.json"), new TypeReference<>() {});
                for (var input : manifest.values()) {
                    controls.put(root.relativize(Path.of(input.get("path"))).toString(), input.get("sha256"));
                }
            }
            AutoLlmFileUtil.write(root.resolve("control/frozen-hashes.json"), controls);
            AutoLlmFileUtil.write(root.resolve("control/config.json"), c.config); if (!metadataOnly) c.baselineCommit = commit; publish(c);
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }
    private String snapshotInputs(Path root, String name, List<String> source) throws IOException {
        var index = new ArrayList<String>(); var hashes = new LinkedHashMap<String, Map<String, String>>();
        Path directory = root.resolve("data/" + name); Files.createDirectories(directory);
        for (int i = 0; i < source.size(); i++) {
            String original = source.get(i); String basename = original.substring(original.lastIndexOf('!') + 1); basename = Path.of(basename).getFileName().toString();
            Path copy = directory.resolve("%08d".formatted(i)).resolve(basename); Files.createDirectories(copy.getParent());
            if (original.contains("!") || !Path.of(original).toAbsolutePath().normalize().equals(copy.toAbsolutePath().normalize())) {
                try (var input = IOUtil.getInputStream(original)) { Files.copy(input, copy, StandardCopyOption.REPLACE_EXISTING); }
            }
            index.add(copy.toString()); hashes.put(original, Map.of("path", copy.toString(), "sha256", AutoLlmFileUtil.hash(copy)));
        }
        Path file = root.resolve("data/" + name + ".index"); Files.write(file, index); AutoLlmFileUtil.write(root.resolve("data/" + name + "-manifest.json"), hashes); return file.toString();
    }
    private void cloneProject(AutoLlmCampaign c, String commit, Path destination, Path output) {
        if (Files.exists(output.resolve("clone-completed.json"))) return;
        if (!Files.exists(destination.resolve(".git"))) runCommand(c, List.of("git", "clone", "--no-hardlinks", "--no-checkout", c.config.project().repository(), destination.toString()), Path.of(c.config.project().repository()), output, c.config.execution().buildTimeoutMillis(), true);
        runCommand(c, List.of("git", "checkout", "--detach", commit), destination, output.resolve("checkout"), 30_000, true);
        runCommand(c, List.of("git", "submodule", "update", "--init", "--recursive"), destination, output.resolve("submodules"), c.config.execution().buildTimeoutMillis(), true);
        AutoLlmFileUtil.write(output.resolve("clone-completed.json"), Map.of("commit", commit));
    }
    private AutoLlmCampaign.Attempt attempt(AutoLlmCampaign c, AutoLlmCampaign.Trial t, String stage) {
        if (c.config.execution().target() == AutoLlmConfig.Target.SSH && !t.attempts.isEmpty()) {
            var previous = t.attempts.getLast();
            if (previous.stage.equals(stage) && previous.status.equals("INTERRUPTED")) {
                Path directory = executionRoot.resolve(previous.directory);
                if (!AutoLlmFileUtil.ownedFiles(directory, "remote-job.json").isEmpty()) { previous.status = "RUNNING"; previous.finishedAt = null; previous.failure = null; previous.problem = null; t.status = stage; publish(c); event("attempt-rejoined", Map.of("directory", previous.directory)); return previous; }
            }
        }
        var a = new AutoLlmCampaign.Attempt(); a.number = t.attempts.size() + 1; a.stage = stage;
        a.directory = "trials/%03d/attempts/%03d-%s".formatted(t.number, a.number, stage.toLowerCase()); a.startedAt = Instant.now().toString();
        t.status = stage; t.attempts.add(a); c.remote = null; publish(c); event("stage-started", Map.of("trial", t.number, "stage", stage, "attempt", a.number)); return a;
    }
    private void substage(AutoLlmCampaign campaign, AutoLlmCampaign.Attempt attempt, String stage) {
        attempt.substage = stage; publish(campaign); event("preparation-stage", Map.of("attempt", attempt.directory, "stage", stage));
    }
    private void verifyDependencies(Path baseline, Path generated, Path evidence) {
        var original = AutoLlmFileUtil.dependencyHashes(baseline); var current = AutoLlmFileUtil.dependencyHashes(generated);
        AutoLlmFileUtil.write(evidence.resolve("dependencies.json"), current);
        if (!original.contentSha256().equals(current.contentSha256())) {
            var differences = new TreeSet<String>(); differences.addAll(original.contentSha256().keySet()); differences.addAll(current.contentSha256().keySet());
            differences.removeIf(name -> Objects.equals(original.contentSha256().get(name), current.contentSha256().get(name)));
            AutoLlmFileUtil.write(evidence.resolve("dependency-differences.json"), Map.of("libraries", differences, "entries", AutoLlmFileUtil.dependencyDifferences(baseline, generated)));
            throw AutoLlmFailureUtil.error(AutoLlmFailureUtil.Category.INTEGRITY, "DEPENDENCIES_CHANGED", "Build dependencies changed from frozen baseline: " + differences, evidence.resolve("dependency-differences.json"));
        }
    }
    private void finish(AutoLlmCampaign c, AutoLlmCampaign.Attempt a, RuntimeException failure) {
        a.status = failure == null ? "SUCCEEDED" : failure instanceof CancellationException || AutoLlmCampaignStore.hasUncollectedJobs(executionRoot.resolve(a.directory)) ? "INTERRUPTED" : "FAILED";
        a.finishedAt = Instant.now().toString(); a.failure = failure == null ? null : ExceptionUtil.stacktrace(failure);
        a.problem = failure == null || failure instanceof CancellationException ? null : AutoLlmFailureUtil.describe(failure, executionRoot.resolve(a.directory));
        var event = new LinkedHashMap<String,Object>(); event.put("directory", a.directory); event.put("status", a.status); event.put("problem", a.problem); event.put("failure", a.failure);
        event("attempt-finished", event);
        publish(c);
        if (List.of("GENERATION", "REPAIR").contains(a.stage)) {
            Path evidence = executionRoot.resolve(a.directory).resolve("evidence.json");
            if (Files.isRegularFile(evidence)) a.codexEvidence = AutoLlmFileUtil.read(evidence, CodexEvidenceUtil.Evidence.class);
            publish(c);
        }
    }
    private void runTrial(AutoLlmCampaign c, AutoLlmCampaign.Trial t) {
        Path root = executionRoot; Path source = root.resolve("trials/%03d/source".formatted(t.number));
        AutoLlmFileUtil.verifySources(root, AutoLlmFileUtil.read(root.resolve("control/frozen-hashes.json"), new TypeReference<>() {}));
        cloneProject(c, c.baselineCommit, source, root.resolve("trials/%03d/clone".formatted(t.number)));
        if (c.config.execution().target() == AutoLlmConfig.Target.SSH && t.resumeTurn) reconcileMetadata(c, t, source);
        while (t.preparedArtifact == null) {
            if (cancelled()) throw new CancellationException();
            if (t.nextStep.equals("GENERATE")) {
                boolean continuation = t.resumeTurn;
                if (t.threadId != null && !continuation && t.repairTurns >= c.config.preparation().maxRepairs()) throw AutoLlmFailureUtil.error(AutoLlmFailureUtil.Category.COMPONENT, "REPAIRS_EXHAUSTED", "Preparation exhausted repair limit: " + t.failure, root.resolve("trials/%03d".formatted(t.number)));
                t.status = "WAITING_FOR_GENERATION"; publish(c);
                try { sessions.acquire(); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new CancellationException("Queued generation interrupted"); }
                if (cancelled()) { sessions.release(); throw new CancellationException(); }
                var a = attempt(c, t, t.threadId == null || continuation && t.generationDirectory == null ? "GENERATION" : "REPAIR"); Path dir = root.resolve(a.directory);
                boolean chargeRepair = t.threadId != null && !continuation;
                t.resumeTurn = false; publish(c);
                try {
                    String prompt = c.prompt + "\nBuild/test command: " + c.config.project().buildCommand() + "\nFrozen seed descriptions: " + c.config.initialConfigurationsFile() + "\nTraining inputs: " + c.config.trainingPath();
                    if (c.config.execution().target() == AutoLlmConfig.Target.SSH) prompt += "\nWorkloads run on SSH; keep analysis and edits local. Use the controller metadata helper instead of local builds or experiments: " + metadataHelper(c, t) + ". It runs build/tests and --autollm-check remotely and returns local evidence. Do not invoke pilots/tuning/comparisons yourself.";
                    if (t.failure != null && t.problem != null && t.problem.category() == AutoLlmFailureUtil.Category.COMPONENT) prompt += "\nRepair feedback:\n" + t.failure + "\nInspect the previous verification logs under " + root.resolve("trials/%03d/attempts".formatted(t.number));
                    CodexTaskRunner.Result result;
                    try (var bridge = c.config.execution().target() == AutoLlmConfig.Target.SSH ? new GenerationMetadataBridge(source, root.resolve("trials/%03d/metadata-requests".formatted(t.number)), options -> metadataCheck(c.id, t.number, options.get("blacklist"), options.get("requestId"))) : null) {
                        result = codex.run(c.config, source, dir, prompt, t.threadId, () -> cancelled() || c.problem != null);
                    }
                    try {
                        metadataLock.lockInterruptibly();
                        try { if (chargeRepair) t.repairTurns++; t.threadId = result.threadId(); t.blacklist = result.manifest().blacklist(); t.generationDirectory = a.directory; t.nextStep = "VERIFY"; t.failure = null; t.problem = null; finish(c, a, null); }
                        finally { metadataLock.unlock(); }
                    } catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new CancellationException("Generation interrupted while awaiting a metadata check"); }
                } catch (RuntimeException e) {
                    if (c.problem != null && e instanceof CancellationException && !cancelled()) e = new AutoLlmFailureUtil.StageException(c.problem, e);
                    t.failure = ExceptionUtil.stacktrace(e); finish(c, a, e);
                    if (a.codexEvidence != null && a.codexEvidence.threadId() != null) t.threadId = a.codexEvidence.threadId();
                    t.resumeTurn = e instanceof CancellationException && t.threadId != null; publish(c);
                    if (e instanceof CancellationException) throw e;
                    t.resumeTurn = t.threadId != null; publish(c); throw e;
                } finally { sessions.release(); }
            }
            if (c.config.execution().target() == AutoLlmConfig.Target.SSH) {
                t.status = "METADATA_RECONCILIATION"; publish(c); reconcileMetadata(c, t, source);
            }
            var a = attempt(c, t, "PREPARATION"); Path dir = root.resolve(a.directory);
            t.failure = null; t.problem = null;
            try {
                substage(c, a, "BUILD");
                runCommand(c, c.config.project().buildCommand(), source, dir.resolve("build"), c.config.execution().buildTimeoutMillis(), false);
                var generatedHashes = AutoLlmFileUtil.sourceHashes(source);
                Path jar = AutoLlmFileUtil.inside(source, c.config.project().artifact());
                var dependencies = AutoLlmFileUtil.dependencyHashes(jar);
                AutoLlmFileUtil.write(dir.resolve("dependencies.json"), dependencies);
                substage(c, a, "DEPENDENCIES");
                verifyDependencies(root.resolve("baseline/source").resolve(c.config.project().artifact()), jar, dir);
                substage(c, a, "METADATA");
                child(c, t, jar, dir.resolve("check"), "--autollm-check", 0, c.config.execution().searchSpaceTimeoutMillis(), List.of());
                long begin = System.currentTimeMillis();
                substage(c, a, "PROBES");
                child(c, t, jar, dir.resolve("probe"), "--autollm-probe", 0, c.config.preparation().pilotDurationMillis() + c.config.execution().startupTimeoutMillis(), List.of());
                t.probeDirectory = a.directory + "/probe"; publish(c);
                long remaining = c.config.preparation().pilotDurationMillis() - (System.currentTimeMillis() - begin);
                if (remaining <= 0) throw AutoLlmFailureUtil.error(AutoLlmFailureUtil.Category.INFRASTRUCTURE, "PILOT_WINDOW", "Pilot budget exhausted by probes; increase the verification window", dir);
                substage(c, a, "PILOT");
                var pilot = child(c, t, jar, dir.resolve("pilot"), "--autoconfig", remaining, c.config.execution().startupTimeoutMillis() + remaining + c.config.execution().phaseTimeoutMillis() + c.config.execution().watchdogGraceMillis(), List.of());
                boolean observed = false;
                for (var event : AutoLlmFileUtil.events(dir.resolve("pilot/execution/events.jsonl"))) if ("evaluation-result".equals(event.get("kind")) && "SUCCEEDED".equals(event.get("state"))) observed = true;
                if (!observed) throw AutoLlmFailureUtil.error(AutoLlmFailureUtil.Category.UNKNOWN, "EMPTY_PILOT", "Verification autoconfig produced no successful evaluations", dir);
                Path frozen = root.resolve("trials/%03d/frozen".formatted(t.number)); Files.createDirectories(frozen);
                Files.copy(jar, frozen.resolve("project.jar"), StandardCopyOption.REPLACE_EXISTING);
                AutoLlmFileUtil.archiveSources(source, frozen.resolve("source.zip"), generatedHashes);
                AutoLlmFileUtil.write(frozen.resolve("source-hashes.json"), generatedHashes);
                Files.copy(dir.resolve("check/execution/search-space.json"), frozen.resolve("search-space.json"), StandardCopyOption.REPLACE_EXISTING);
                Files.copy(dir.resolve("check/execution/components.json"), frozen.resolve("components.json"), StandardCopyOption.REPLACE_EXISTING);
                AutoLlmFileUtil.write(frozen.resolve("artifact.json"), Map.of("sha256", AutoLlmFileUtil.hash(frozen.resolve("project.jar")), "blacklist", t.blacklist == null ? "" : t.blacklist));
                t.preparedArtifact = root.relativize(frozen.resolve("project.jar")).toString(); t.failure = null; finish(c, a, null);
            } catch (IOException e) { throw new UncheckedIOException(e); }
            catch (RuntimeException e) {
                finish(c, a, e); if (e instanceof CancellationException) throw e;
                t.problem = AutoLlmFailureUtil.describe(e, dir); t.failure = ExceptionUtil.stacktrace(e);
                if (t.problem.category() != AutoLlmFailureUtil.Category.COMPONENT) throw e;
                t.nextStep = "GENERATE"; publish(c);
            }
        }
        Path jar = root.resolve(t.preparedArtifact);
        var identity = AutoLlmFileUtil.read(jar.getParent().resolve("artifact.json"), Map.class);
        if (!AutoLlmFileUtil.hash(jar).equals(identity.get("sha256"))) throw AutoLlmFailureUtil.error(AutoLlmFailureUtil.Category.INTEGRITY, "ARTIFACT_CHANGED", "Frozen artifact was modified", jar);
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
        for (var previous : t.attempts) if (previous.stage.equals("COMPARISON") && previous.status.equals("SUCCEEDED")) return;
        var a = attempt(c, t, "COMPARISON"); t.comparisonDirectory = a.directory; publish(c);
        try { child(c, t, jar, root.resolve(a.directory), "--default", 0, c.config.execution().stageTimeoutMillis(), List.of("--autollm.child.comparison-enabled=true", "--autollm.child.winner-file=" + root.resolve(t.winnerDirectory).resolve("execution/winner.json"), "--solver.experiments=autollm-comparison", "--solver.repetitions=" + c.config.comparison().repetitions())); finish(c, a, null); }
        catch (RuntimeException e) { finish(c, a, e); throw e; }
    }
    private void runCommand(AutoLlmCampaign c, List<String> command, Path cwd, Path dir, long timeout, boolean local) {
        AutoLlmProcessRunner.Result result;
        if (local) result = processes.run(command, cwd, dir, timeout, 0, c.config.execution().watchdogGraceMillis(), () -> cancelled(), false, 0);
        else {
            Path root = executionRoot; boolean build = command.equals(c.config.project().buildCommand());
            if (build && c.config.execution().target() == AutoLlmConfig.Target.LOCAL && (command.getFirst().endsWith("/mvnw") || command.getFirst().endsWith("/gradlew"))) {
                var wrapper = processes.run(List.of(command.getFirst(), "--version"), cwd, dir.resolve("wrapper-preflight"), timeout, 0, c.config.execution().watchdogGraceMillis(), () -> cancelled(), false, 0);
                if (wrapper.exitCode() != 0) throw AutoLlmFailureUtil.error(AutoLlmFailureUtil.Category.INFRASTRUCTURE, "BUILD_WRAPPER", "Committed build wrapper did not run; inspect its version diagnostics", dir.resolve("wrapper-preflight"));
            }
            result = workloads.run(c.config, root, command, cwd.startsWith(root) ? cwd : root, dir, build ? cwd : null,
                    build ? List.of(cwd.resolve(c.config.project().artifact())) : List.of(), timeout, 0, false, 0, () -> cancelled(), state -> remoteState(c, state));
        }
        if (result.exitCode() != 0) {
            String message = "Command failed (" + result.exitCode() + "): " + command + "; logs: " + dir;
            if (command.equals(c.config.project().buildCommand())) throw AutoLlmFailureUtil.buildFailure(cwd, executionRoot.resolve("baseline/source"), dir, message, path -> AutoLlmExecutionService.localPath(c.config, executionRoot, path));
            throw AutoLlmFailureUtil.error(AutoLlmFailureUtil.Category.INFRASTRUCTURE, "PREREQUISITE_FAILED", message, dir);
        }
    }
    private AutoLlmProcessRunner.Result child(AutoLlmCampaign c, AutoLlmCampaign.Trial t, Path jar, Path output, String mode, long pilotMillis, long hardTimeout, List<String> extra) {
        Path root = executionRoot; Path controls = root.resolve("control/frozen-hashes.json");
        if (Files.exists(controls)) AutoLlmFileUtil.verifySources(root, AutoLlmFileUtil.read(controls, new TypeReference<>() {}));
        String artifactHash = AutoLlmFileUtil.hash(jar);
        if (t != null && t.preparedArtifact != null && !artifactHash.equals(AutoLlmFileUtil.read(jar.getParent().resolve("artifact.json"), Map.class).get("sha256"))) throw new IllegalStateException("Frozen artifact was modified");
        long seed = t == null ? c.config.seed() : mode.equals("--default") ? t.comparisonSeed : mode.equals("--autoconfig") && pilotMillis == 0 ? t.tuningSeed : t.pilotSeed;
        int workers = workloads.workers(c.config, root);
        boolean validation = mode.equals("--autollm-probe") || mode.equals("--autoconfig") && pilotMillis > 0;
        var args = new ArrayList<>(List.of(Path.of(System.getProperty("java.home"), "bin/java").toString(), validation ? "-ea" : "-da", "-Xmx" + c.config.execution().heap(),
                "-Dmork.execution.directory=" + output.resolve("execution"), "-Dmork.execution.expected-workers=" + workers, "-Dmork.execution.phase-timeout=" + c.config.execution().phaseTimeoutMillis(), "-Dmork.execution.watchdog-grace=" + c.config.execution().watchdogGraceMillis(),
                "-jar", jar.toString(), mode, "--server.address=127.0.0.1", "--server.port=0", "--event.webserver.stop-on-execution-end=true",
                "--instances.path.irace=" + c.config.trainingPath(), "--instances.path.autollm-comparison=" + c.config.comparisonPath(), "--instances.path.default=" + c.config.trainingPath(), "--instances.preload=false",
                "--autollm.child.config-file=" + executionRoot.resolve("control/config.json"), "--autollm.child.time-limits-file=" + executionRoot.resolve("control/comparison-time-limits.json"), "--irace.initial-configurations-file=" + c.config.initialConfigurationsFile(), "--irace.ablation=false", "--irace.reject-execution-errors=true",
                "--solver.autorestart=true", "--solver.seed=" + seed, "--solver.experiments-per-parameter=0", "--solver.minimum-number-of-experiments=" + c.config.tuning().evaluations(),
                "--solver.ignore-initial-millis=" + c.config.tuning().ignoreInitialMillis(), "--solver.interval-duration-millis=" + c.config.tuning().intervalDurationMillis(), "--solver.log-scale-area=" + c.config.tuning().logScaleArea(),
                "--solver.parallel-executor=" + (workers > 1), "--solver.n-workers=" + workers, "--solver.metrics=true", "--serializers.xlsx.enabled=true", "--serializers.xlsx.frequency=EXPERIMENT_END"));
        if (t != null) args.addAll(3, List.of("-Dmork.execution.target=" + t.targetId, "-Dmork.execution.pool-revision=" + t.targetRevision, "-Dmork.execution.campaign=" + c.id, "-Dmork.execution.trial=" + t.number, "-Dmork.execution.tuning-seed=" + t.tuningSeed, "-Dmork.execution.artifact-sha256=" + artifactHash));
        if (t != null) { args.add("--autollm.child.baseline-components-file=" + executionRoot.resolve("baseline/components.json")); if (t.blacklist != null && !t.blacklist.isBlank()) args.add("--blacklist=" + t.blacklist); }
        boolean newFilter = t != null && t.blacklist != null && !t.blacklist.isBlank();
        for (String argument : c.config.project().runArguments()) {
            if (newFilter && (argument.startsWith("--blacklist=") || argument.startsWith("--whitelist="))) continue;
            args.add(argument);
        }
        if (newFilter) args.add("--whitelist=");
        args.addAll(extra);
        AutoLlmFileUtil.write(output.resolve("command.json"), args);
        if (c.executionHost != null) args.add(2, "-Dmork.execution.expected-processors=" + c.executionHost.identity().get("processors"));
        var result = workloads.run(c.config, root, args, output, output, null, List.of(), hardTimeout, pilotMillis, true,
                c.config.execution().startupTimeoutMillis(), () -> cancelled(), state -> remoteState(c, state));
        if (result.plannedDeadline()) return result;
        Path terminal = output.resolve("execution/terminal.json");
        if (result.exitCode() != 0 || !Files.isRegularFile(terminal)) throw childFailure(output, "Child failed or did not persist completion; logs: " + output);
        String outcome = (String) AutoLlmFileUtil.read(terminal, Map.class).get("outcome");
        if (!outcome.equals("SUCCEEDED")) throw childFailure(output, "Child outcome " + outcome + "; logs: " + output);
        return result;
    }
    private RuntimeException childFailure(Path output, String message) {
        Path failure = output.resolve("execution/failure.json");
        if (Files.exists(failure)) return new AutoLlmFailureUtil.StageException(AutoLlmFileUtil.read(failure, AutoLlmFailureUtil.Failure.class), null);
        return AutoLlmFailureUtil.error(AutoLlmFailureUtil.Category.UNKNOWN, "CHILD_FAILED", message, output);
    }
    private void remoteState(AutoLlmCampaign campaign, Map<String,Object> state) {
        campaign.remote = state;
        campaign.status = Set.of("WAITING_FOR_REMOTE", "CANCELLATION_PENDING").contains(state.get("status")) ? (String) state.get("status") : "RUNNING";
        publish(campaign);
    }
    private AutoLlmConfig screenedConfig(AutoLlmCampaign campaign) {
        Path proposal = Path.of(campaign.approvedScreeningPath).resolve("proposal");
        return campaign.config.withPaths(proposal.resolve("train.index").toString(), proposal.resolve("test.index").toString());
    }
    private void reconcileMetadata(AutoLlmCampaign campaign, AutoLlmCampaign.Trial trial, Path source) {
        Path root = executionRoot, directory = root.resolve("trials/%03d/metadata".formatted(trial.number));
        if (Files.isDirectory(directory)) try (var files = Files.walk(directory)) {
            for (Path file : files.toList()) if (file.getFileName().toString().equals("remote-job.json")) {
                Path output = file.getParent();
                if (Files.exists(output.resolve("collection-completed.json"))) continue;
                var job = AutoLlmFileUtil.read(file, WorkloadProtocol.Job.class);
                try { workloads.run(campaign.config, root, List.of("reattach"), root.resolve(job.cwd()), output, null, List.of(), job.timeoutMillis(), job.plannedMillis(), job.watchEvaluations(), job.startupMillis(), () -> cancelled(), update -> remoteState(campaign, update)); }
                catch (CancellationException e) { if (cancelled() || Thread.currentThread().isInterrupted()) throw e; }
                catch (ExecutionTargetException e) { throw e; }
                catch (IllegalStateException e) { if (!Files.exists(output.resolve("collection-completed.json"))) throw e; /* Completed auxiliary-check diagnostics remain mirrored. */ }
            }
        } catch (IOException e) { throw new UncheckedIOException(e); }
        Path queue = source.resolve(".mork-metadata");
        if (Files.isDirectory(queue)) try (var files = Files.newDirectoryStream(queue, "*.request.json")) {
            for (Path file : files) {
                Path response = queue.resolve(file.getFileName().toString().replace(".request.json", ".result.json"));
                if (!Files.exists(response)) AutoLlmFileUtil.write(response, Map.of("error", "Controller interrupted this check. Inspect mirrored metadata evidence and request a fresh count if needed."));
            }
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }
    private String metadataHelper(AutoLlmCampaign campaign, AutoLlmCampaign.Trial trial) {
        Path root = executionRoot, helper = root.resolve("control/metadata-check-" + trial.number + ".py");
        Path queue = root.resolve("trials/%03d/source/.mork-metadata".formatted(trial.number));
        long timeoutSeconds = (campaign.config.execution().buildTimeoutMillis() + campaign.config.execution().searchSpaceTimeoutMillis() + campaign.config.execution().phaseTimeoutMillis()) / 1000 + 30;
        String body = "import json,time,uuid,sys\nfrom pathlib import Path\nqueue=Path(" + AutoLlmFileUtil.JSON.writeValueAsString(queue.toString()) + ")\nqueue.mkdir(exist_ok=True)\nkey=uuid.uuid4().hex\npending=queue/(key+'.pending')\npending.write_text(json.dumps({'blacklist':sys.argv[1]} if len(sys.argv)>1 else {}))\npending.rename(queue/(key+'.request.json'))\nresponse=queue/(key+'.result.json')\ndeadline=time.monotonic()+" + timeoutSeconds + "\nwhile not response.exists():\n if time.monotonic()>deadline: raise SystemExit('Metadata request '+key+' timed out; inspect controller diagnostics before retrying')\n time.sleep(0.2)\nresult=json.loads(response.read_text())\nif 'error' in result: raise SystemExit(json.dumps(result,indent=2))\nprint(json.dumps(result['result'],indent=2))\n";
        try { Files.writeString(helper, body); } catch (IOException e) { throw new UncheckedIOException(e); }
        return "python3 " + WorkloadPathUtil.quote(helper.toString());
    }
    public Object metadataCheck(String id, int number, String blacklist) {
        return metadataCheck(id, number, blacklist, UUID.randomUUID().toString().replace("-", ""));
    }
    private Object metadataCheck(String id, int number, String blacklist, String requestId) {
        if (!requestId.matches("[a-f0-9]{32}")) throw new IllegalArgumentException("Invalid metadata request id");
        if (blacklist != null && !blacklist.matches("[A-Za-z_$][A-Za-z0-9_.$]*")) throw new IllegalArgumentException("Invalid blacklist component name");
        if (!metadataLock.tryLock()) throw new IllegalStateException("A metadata check is already running");
        try {
            if (!id.equals(model.id)) throw new IllegalArgumentException("Campaign mismatch");
            var campaign = model;
            AutoLlmCampaign.Trial trial = null;
            for (var candidate : campaign.trials) if (candidate.number == number) trial = candidate;
            if (trial == null || !Set.of("GENERATION", "REPAIR").contains(trial.status) || trial.attempts.isEmpty() || !trial.attempts.getLast().status.equals("RUNNING")) throw new IllegalStateException("Metadata checks are available only during generation/repair");
            Path root = executionRoot, source = root.resolve("trials/%03d/source".formatted(number));
            Path output = root.resolve("trials/%03d/metadata/%s".formatted(number, requestId));
            // The metadata bridge/API owns this check while the campaign thread waits for local Codex.
            try {
                runCommand(campaign, campaign.config.project().buildCommand(), source, output.resolve("build"), campaign.config.execution().buildTimeoutMillis(), false);
                var probe = new AutoLlmCampaign.Trial(); probe.targetId = trial.targetId; probe.targetRevision = trial.targetRevision; probe.number = trial.number; probe.pilotSeed = trial.pilotSeed; probe.blacklist = blacklist == null ? trial.blacklist : blacklist;
                child(campaign, probe, source.resolve(campaign.config.project().artifact()), output.resolve("check"), "--autollm-check", 0, campaign.config.execution().searchSpaceTimeoutMillis(), List.of());
                return Map.of("directory", output.toString(), "check", AutoLlmFileUtil.read(output.resolve("check/execution/check.json"), Object.class));
            } catch (RuntimeException e) {
                var problem = AutoLlmFailureUtil.describe(e, output);
                if (!(e instanceof CancellationException) && problem.category() != AutoLlmFailureUtil.Category.COMPONENT) { campaign.problem = problem; trial.problem = problem; publish(campaign); }
                throw e;
            }
        } finally { metadataLock.unlock(); }
    }
}
