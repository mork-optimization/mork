package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmCampaign.TrialStatus;
import es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmCampaign.AttemptStatus;
import es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmCampaign.AttemptStage;
import es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmCampaign.PreparationStage;

import es.urjc.etsii.grafo.autoconfig.autollm.execution.*;
import static es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmCampaign.Stage.*;
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
        void state(AutoLlmExecutionContext.Snapshot snapshot);
        void event(String kind, Object detail);
    }
    private final AutoLlmExecutionContext model;
    private final Path executionRoot;
    private final AutoLlmProcessRunner processes;
    private final CodexTaskRunner codex;
    private final AutoLlmExecutionService workloads;
    private final Listener listener;
    private final java.util.function.BooleanSupplier stop;
    private final Semaphore sessions;
    private Semaphore buildPermits = new Semaphore(1);
    public void localBuildPermits(Semaphore permits) { buildPermits = permits; }
    private java.time.Clock capacityClock = java.time.Clock.systemUTC();
    private java.util.function.DoubleSupplier capacityRandom = () -> java.util.concurrent.ThreadLocalRandom.current().nextDouble();
    private java.util.function.LongConsumer capacitySleep = millis -> {
        try { Thread.sleep(millis); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new CancellationException("Capacity wait interrupted"); }
    };
    void capacityTiming(java.time.Clock clock, java.util.function.DoubleSupplier random, java.util.function.LongConsumer sleep) {
        capacityClock = clock; capacityRandom = random; capacitySleep = sleep;
    }
    private boolean localOnly;
    private volatile boolean shuttingDown;
    private volatile Thread metadataThread;
    private volatile GenerationMetadataBridge metadataBridge;
    public void prepareTrialLocally() { localOnly = true; execute(); }
    private final ReentrantLock metadataLock = new ReentrantLock();
    public AutoLlmTrialRunner(AutoLlmExecutionContext model, Path root, AutoLlmProcessRunner processes, CodexTaskRunner codex,
                              Listener listener, java.util.function.BooleanSupplier stop, Semaphore sessions) {
        this.model = model; this.executionRoot = root; this.processes = processes; this.codex = codex;
        this.workloads = new AutoLlmExecutionService(processes); this.listener = listener; this.stop = stop; this.sessions = sessions;
    }
    private boolean cancelled() { return shuttingDown || stop.getAsBoolean(); }
    public void shutdown() {
        shuttingDown = true;
        var bridge = metadataBridge; if (bridge != null) bridge.interrupt();
        var thread = metadataThread; if (thread != null && (bridge == null || !bridge.owns(thread))) thread.interrupt();
    }
    public boolean awaitShutdown(long deadline) {
        boolean interrupted = Thread.interrupted();
        try {
            boolean locked = false;
            while (!locked && System.nanoTime() < deadline) {
                try { locked = metadataLock.tryLock(Math.max(1, deadline - System.nanoTime()), TimeUnit.NANOSECONDS); }
                catch (InterruptedException e) { interrupted = true; }
            }
            if (!locked) return false;
            metadataLock.unlock();
            var bridge = metadataBridge;
            return bridge == null || bridge.awaitStopped(deadline);
        } finally { if (interrupted) Thread.currentThread().interrupt(); }
    }
    private synchronized void publish(AutoLlmExecutionContext campaign) { listener.state(campaign.snapshot()); }
    private void event(String kind, Object detail) { listener.event(kind, detail); }
    public AutoLlmExecutionContext.Snapshot snapshot() { return model.snapshot(); }
    public void prepare(boolean metadataOnly) {
        prepareBaseline(model, metadataOnly);
    }
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
    public void reconcilePreparation() {
        if (!AutoLlmAssignmentPolicy.movable(model.trial)) throw new IllegalStateException("Tuned assignments cannot be released");
        workloads.reconcilePreparation(executionRoot, this::cancelled, state -> remoteState(model, state));
    }
    public void reconcile() {
        processes.reconcile(executionRoot);
        if (model.trial != null) {
            var trial = model.trial;
            recoverTurn(model, trial);
            for (var a : trial.attempts) if (a.status == AttemptStatus.RUNNING) { a.status = AttemptStatus.INTERRUPTED; a.finishedAt = Instant.now().toString(); }
        }
    }
    public void execute() {
        var c = model; var t = c.trial;
        c.problem = null;
        try {
            if (!localOnly) ExecutionOwner.prepare(c.config, executionRoot);
            reconcile();
            if (c.config.execution().target() == AutoLlmConfig.Target.SSH) workloads.identity(c.config, executionRoot);
            if (t.problem != null && t.problem.code().equals("ArchiveException")) throw new AutoLlmFailureUtil.StageException(t.problem, null);
            while (true) {
                try { runTrial(c, t); break; }
                catch (RuntimeException e) {
                    if (e instanceof CancellationException) throw e;
                    if (e instanceof CodexCapacityUtil.CapacityException capacity) {
                        t.capacityWait = CodexCapacityUtil.schedule(t.capacityWait, capacity.getMessage(), capacityClock, capacityRandom);
                        t.problem = AutoLlmFailureUtil.describe(e, executionRoot); t.status = TrialStatus.WAITING_FOR_CAPACITY;
                        c.problem = null; publish(c); event("codex-capacity-wait", Map.of("trial", t.number, "wait", t.capacityWait));
                        continue;
                    }
                    if (e instanceof CodexQuotaUtil.QuotaLimitException quota) {
                        t.quotaRetryAt = quota.retryAt().toString(); t.problem = AutoLlmFailureUtil.describe(e, executionRoot);
                        t.status = TrialStatus.WAITING_FOR_QUOTA; c.problem = null; publish(c);
                        event("codex-quota-wait", Map.of("trial", t.number, "retryAt", t.quotaRetryAt, "failure", t.problem));
                        continue;
                    }
                    var problem = AutoLlmFailureUtil.describe(e, executionRoot);
                    if (!problem.retryable() || t.infrastructureRetries >= AutoLlmFailureUtil.MAX_RETRIES) throw e;
                    t.infrastructureRetries++; t.problem = problem; c.problem = null; publish(c);
                    event("infrastructure-retry", Map.of("trial", t.number, "retry", t.infrastructureRetries, "failure", problem));
                    try { Thread.sleep(1000L * t.infrastructureRetries); }
                    catch (InterruptedException interrupted) { Thread.currentThread().interrupt(); throw new CancellationException(); }
                }
            }
            t.status = localOnly || t.nextStage == GENERATION ? TrialStatus.WAITING_FOR_TARGET : TrialStatus.SUCCEEDED;
            if (t.nextStage != GENERATION) { t.failure = null; t.problem = null; }
        } catch (CancellationException e) { t.status = TrialStatus.INTERRUPTED; }
        catch (RuntimeException e) {
            t.problem = AutoLlmFailureUtil.describe(e, executionRoot); t.failure = ExceptionUtil.stacktrace(e);
            t.status = t.problem.category() == AutoLlmFailureUtil.Category.COMPONENT ? TrialStatus.FAILED : TrialStatus.BLOCKED;
            c.problem = t.problem;
        } finally {
            for (var a : t.attempts) if (a.status == AttemptStatus.RUNNING) { a.status = AttemptStatus.INTERRUPTED; a.finishedAt = Instant.now().toString(); }
            if (t.status.terminal() && !cancelled()) {
                try (var permit = es.urjc.etsii.grafo.util.ConcurrencyUtil.acquirePermit(buildPermits, this::cancelled)) {
                    if (!localOnly || t.assignments.isEmpty()) RemoteStorageMaintenance.maintain(c.campaignRoot, executionRoot, this::cancelled);
                    AutoLlmArtifactRebuild.maintain(c.campaignRoot, executionRoot, t, c.config, processes, this::cancelled);
                    AutoLlmCheckoutRetirement.maintainTrial(c.campaignRoot, executionRoot, t, c.config, c.baselineCommit, c.versions, processes, this::cancelled);
                    AutoLlmEvidenceFiles.compact(c.campaignRoot, executionRoot, t);
                } catch (CancellationException ignored) { /* Completed measurements remain completed; the executable is retained. */ }
                catch (IOException | RuntimeException failure) { event("storage-maintenance-failed", Map.of("trial", t.number, "reason", failure.toString())); }
            }
            publish(c);
        }
    }
    private void recoverTurn(AutoLlmExecutionContext c, AutoLlmCampaign.Trial trial) {
        if (trial.attempts.isEmpty()) return;
        var attempt = trial.attempts.getLast();
        if (!attempt.stage.generation() || !Set.of(AttemptStatus.RUNNING, AttemptStatus.INTERRUPTED, AttemptStatus.FAILED, AttemptStatus.QUOTA_LIMIT, AttemptStatus.CAPACITY_LIMIT).contains(attempt.status)) return;
        Path root = executionRoot;
        try {
            attempt.codexEvidence = codex.archive(root.resolve("source"), model.path(attempt.directory), trial.threadId);
            if (attempt.codexEvidence.threadId() != null) trial.threadId = attempt.codexEvidence.threadId();
            var recovered = codex.recoveredResult(model.path(attempt.directory), trial.threadId);
            if (recovered != null) {
                if (trial.repairPending) trial.repairTurns++;
                trial.repairPending = false;
                trial.capacityWait = null; trial.threadId = recovered.threadId(); trial.blacklist = recovered.manifest().blacklist();
                trial.complete(GENERATION, attempt.directory, null);
                trial.resumeTurn = false; trial.failure = null;
                finish(c, attempt, null);
            } else trial.resumeTurn = trial.threadId != null;
            trial.problem = null;
        } catch (CodexEvidenceUtil.ArchiveException e) {
            Path evidence = model.path(attempt.directory).resolve("evidence.json");
            if (Files.isRegularFile(evidence)) attempt.codexEvidence = AutoLlmFileUtil.read(evidence, CodexEvidenceUtil.Evidence.class);
            trial.status = TrialStatus.BLOCKED; trial.failure = ExceptionUtil.stacktrace(e); trial.resumeTurn = false;
            trial.problem = AutoLlmFailureUtil.describe(e, model.path(attempt.directory)); c.problem = trial.problem;
            attempt.status = AttemptStatus.FAILED; attempt.failure = trial.failure; attempt.finishedAt = Instant.now().toString();
        }
    }

    private void prepareBaseline(AutoLlmExecutionContext c, boolean metadataOnly) {
        Path root = executionRoot; Path repo = Path.of(c.config.project().repository());
        Path preflight = root.resolve("baseline-preflight");
        c.executionHost = workloads.probe(c.config, root);
        var versions = new LinkedHashMap<>(TargetPreflightUtil.versions(c.executionHost));
        c.versions = Map.copyOf(versions);
        if (c.config.execution().target() == AutoLlmConfig.Target.SSH) {
            if (c.screeningPath == null) c.screeningPath = Path.of(c.config.trainingPath()).getParent().getParent().toString();
            es.urjc.etsii.grafo.autoconfig.screening.RemoteScreeningService.screened(screenedConfig(c));
            AutoLlmFileUtil.write(root.resolve("control/config.json"), c.config); publish(c);
        }
        Path codexVersion = preflight.resolve("codex-version");
        runCommand(c, List.of(c.config.codex().executable(), "--version"), repo, codexVersion, 30_000, true);
        versions.put("codex", AutoLlmFailureUtil.tail(codexVersion.resolve("stdout.log"), 4000) + AutoLlmFailureUtil.tail(codexVersion.resolve("stderr.log"), 4000));
        runCommand(c, List.of("git", "rev-parse", "--verify", c.config.project().ref() + "^{commit}"), repo, preflight.resolve("ref"), 30_000, true);
        try {
            String commit = Files.readString(preflight.resolve("ref/stdout.log")).strip();
            c.config = c.config.withRef(commit);
            Path baseline = root.resolve("baseline/source"); cloneProject(c, commit, baseline, preflight.resolve("clone"));
            if (c.config.execution().target() == AutoLlmConfig.Target.SSH) {
                var prepared = es.urjc.etsii.grafo.autoconfig.screening.RemoteScreeningService.screened(screenedConfig(c));
                if (!commit.equals(prepared.commit())) throw new IllegalStateException("Baseline commit differs from approved screening");
                Path screeningRoot = Path.of(c.screeningPath);
                var manifest = AutoLlmFileUtil.read(screeningRoot.resolve("proposal/manifest.json"), es.urjc.etsii.grafo.autoconfig.screening.ScreeningData.Manifest.class);
                if (!"QUALIFIED_SERVER_POOL".equals(manifest.reusePolicy()) || !prepared.sotaSha256().equals(AutoLlmFileUtil.hash(Path.of(c.config.sotaFile())))) throw new IllegalStateException("Approved selection policy or SOTA changed");
                Path screened = screeningRoot.resolve(prepared.artifact());
                if (!prepared.artifactSha256().equals(AutoLlmFileUtil.hash(screened))) throw new IllegalStateException("Approved screening executable changed");
                Path destination = baseline.resolve(c.config.project().artifact()); Files.createDirectories(destination.getParent()); Files.copy(screened, destination, StandardCopyOption.REPLACE_EXISTING);
                AutoLlmFileUtil.write(preflight.resolve("build/adopted-screening.json"), Map.of("artifactSha256", prepared.artifactSha256(), "buildEvidence", screeningRoot.resolve(prepared.baseline().buildDirectory()).toString()));
            } else runCommand(c, c.config.project().buildCommand(), baseline, preflight.resolve("build"), c.config.execution().buildTimeoutMillis(), false);
            Path jar = AutoLlmFileUtil.inside(baseline, c.config.project().artifact()); if (!Files.isRegularFile(jar)) throw new IllegalStateException("Build did not produce configured JAR: " + jar);
            if (c.config.execution().target() == AutoLlmConfig.Target.SSH) c.config = workloads.stageInputs(c.config, root);
            child(c, null, jar, preflight.resolve("check"), "--autollm-check", 0, c.config.execution().searchSpaceTimeoutMillis(), List.of());
            var check = AutoLlmExecutionService.check(c.config, root, preflight.resolve("check/execution/check.json"));
            if (check.parameters() > c.config.maxParameters() && !metadataOnly) throw AutoLlmFailureUtil.error(AutoLlmFailureUtil.Category.INFRASTRUCTURE, "BASELINE_SEARCH_SPACE", "Baseline exceeds the configured parameter limit; prune while preserving all seeds before starting", preflight.resolve("check"));
            c.config = c.config.withPaths(AutoLlmInputUtil.snapshot(root, "training", check.training()), AutoLlmInputUtil.snapshot(root, "comparison", check.comparison()));
            Map<String,Long> originalBudgets = AutoLlmFileUtil.read(preflight.resolve("check/execution/comparison-time-limits.json"), new TypeReference<>() {});
            var localBudgets = new LinkedHashMap<String,Long>();
            var localPaths = AutoLlmExecutionService.localPaths(c.config, root);
            for (var budget : originalBudgets.entrySet()) localBudgets.put(localPaths.apply(budget.getKey()), budget.getValue());
            var frozenBudgets = new LinkedHashMap<String,Long>();
            Map<String,Map<String,String>> comparisonInputs = AutoLlmFileUtil.read(root.resolve("data/comparison-manifest.json"), new TypeReference<>() {});
            for (var entry : comparisonInputs.entrySet()) frozenBudgets.put(entry.getValue().get("path"), localBudgets.get(entry.getKey()));
            AutoLlmFileUtil.write(root.resolve("control/comparison-time-limits.json"), frozenBudgets);
            AutoLlmFileUtil.write(root.resolve("control/config.json"), c.config);
            if (!metadataOnly) child(c, null, jar, preflight.resolve("probe"), "--autollm-probe", 0, c.config.execution().startupTimeoutMillis() + c.config.preparation().probeDurationMillis(), List.of());
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
            c.versions = versions;
            var controls = new TreeMap<String,String>();
            for (String file : List.of("sota.json", "seeds.json", "comparison-time-limits.json")) controls.put("control/" + file, AutoLlmFileUtil.hash(root.resolve("control/" + file)));
            AutoLlmInputUtil.addSettingsHashes(controls, root);
            for (String name : List.of("training", "comparison")) AutoLlmInputUtil.addHashes(controls, root, name);
            AutoLlmFileUtil.write(root.resolve("control/frozen-hashes.json"), controls);
            AutoLlmFileUtil.write(root.resolve("control/config.json"), c.config); if (!metadataOnly) c.baselineCommit = commit; publish(c);
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }
    private void prepareAssignmentSource(AutoLlmExecutionContext c, AutoLlmCampaign.Trial trial) {
        if (trial.assignment() == null || !executionRoot.equals(c.path(trial.assignment().directory))) return;
        Path marker = executionRoot.resolve("clone/clone-completed.json");
        if (Files.exists(marker)) return;
        Path original = c.path(trial.directory).resolve("source"), pointer = executionRoot.resolve("input/source.json");
        GitSourceStore.Command command = (args, cwd, logs) -> runCommand(c, args, cwd, logs, c.config.execution().buildTimeoutMillis(), true);
        try {
            AutoLlmSourceService.Source record;
            if (Files.isRegularFile(pointer)) record = AutoLlmFileUtil.read(pointer, AutoLlmSourceService.Source.class);
            else {
                record = AutoLlmSourceService.capture(c.campaignRoot, original, executionRoot.resolve("input"), c.baselineCommit, c.config, c.versions, command);
                AutoLlmFileUtil.write(pointer, record);
            }
            Path snapshot = c.path(record.snapshot()), source = executionRoot.resolve("source");
            if (!AutoLlmFileUtil.hash(snapshot.resolve("snapshot.json")).equals(record.manifestSha256())) throw new IOException("Assignment source snapshot changed");
            // An interrupted restore is retained for diagnosis; only a complete fresh restore is published.
            if (Files.exists(source)) Files.move(source, executionRoot.resolve("input/interrupted-source-" + UUID.randomUUID()));
            Path staged = executionRoot.resolve("input/restoring-" + UUID.randomUUID());
            GitPatchSnapshot.restore(c.campaignRoot.resolve(".store/git/" + record.commit() + ".bundle"), snapshot, staged, command);
            Files.move(staged, source);
            AutoLlmFileUtil.write(marker, Map.of("commit", record.commit()));
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }
    private void cloneProject(AutoLlmExecutionContext c, String commit, Path destination, Path output) {
        if (!Files.exists(destination.resolve(".git"))) {
            try { DirectoryArchiveUtil.restore(destination); }
            catch (IOException e) { throw new UncheckedIOException(e); }
        }
        if (Files.exists(output.resolve("clone-completed.json"))) return;
        try {
            GitSourceStore.checkout(c.campaignRoot.resolve(".store/git"), Path.of(c.config.project().repository()), commit, destination, output,
                    (args, cwd, logs) -> runCommand(c, args, cwd, logs, c.config.execution().buildTimeoutMillis(), true));
        } catch (IOException e) { throw new UncheckedIOException(e); }
        AutoLlmFileUtil.write(output.resolve("clone-completed.json"), Map.of("commit", commit));
    }
    private AutoLlmCampaign.Attempt attempt(AutoLlmExecutionContext c, AutoLlmCampaign.Trial t, AttemptStage stage) {
        if (!t.attempts.isEmpty()) {
            var previous = t.attempts.getLast();
            if (previous.stage == stage && previous.status == AttemptStatus.INTERRUPTED
                    && model.path(previous.directory).startsWith(executionRoot)
                    && Objects.equals(previous.assignmentId, t.assignment() != null && executionRoot.equals(c.path(t.assignment().directory)) ? t.activeAssignmentId : null)) {
                Path directory = model.path(previous.directory);
                if (!AutoLlmFileUtil.ownedFiles(directory, "remote-job.json").isEmpty() || !AutoLlmFileUtil.ownedFiles(directory, "workload-result.json").isEmpty() || !AutoLlmFileUtil.ownedFiles(directory, "collection-failure.json").isEmpty()) { previous.status = AttemptStatus.RUNNING; previous.finishedAt = null; previous.failure = null; previous.problem = null; t.status = stage.trialStatus(); publish(c); event("attempt-rejoined", Map.of("directory", previous.directory)); return previous; }
            }
        }
        var a = new AutoLlmCampaign.Attempt(); a.number = t.attempts.size() + 1; a.stage = stage;
        a.assignmentId = t.assignment() != null && executionRoot.equals(c.path(t.assignment().directory)) ? t.activeAssignmentId : null;
        a.directory = model.relative(executionRoot.resolve("attempts/%03d-%s".formatted(a.number, stage.name().toLowerCase(java.util.Locale.ROOT)))); a.startedAt = Instant.now().toString();
        t.status = stage.trialStatus(); t.attempts.add(a); c.remote = null; publish(c); event("stage-started", Map.of("trial", t.number, "stage", stage, "attempt", a.number)); return a;
    }
    private void substage(AutoLlmExecutionContext campaign, AutoLlmCampaign.Attempt attempt, PreparationStage stage) {
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
    private void finish(AutoLlmExecutionContext c, AutoLlmCampaign.Attempt a, RuntimeException failure) {
        if (failure == null) a.status = AttemptStatus.SUCCEEDED;
        else if (failure instanceof CodexQuotaUtil.QuotaLimitException) a.status = AttemptStatus.QUOTA_LIMIT;
        else if (failure instanceof CodexCapacityUtil.CapacityException) a.status = AttemptStatus.CAPACITY_LIMIT;
        else if (failure instanceof CancellationException || AutoLlmCampaignStore.hasUncollectedJobs(model.path(a.directory)) || !AutoLlmFileUtil.ownedFiles(model.path(a.directory), "collection-failure.json").isEmpty()) a.status = AttemptStatus.INTERRUPTED;
        else a.status = AttemptStatus.FAILED;
        a.finishedAt = Instant.now().toString(); a.failure = failure == null ? null : ExceptionUtil.stacktrace(failure);
        a.problem = failure == null || failure instanceof CancellationException ? null : AutoLlmFailureUtil.describe(failure, model.path(a.directory));
        var event = new LinkedHashMap<String,Object>(); event.put("directory", a.directory); event.put("status", a.status); event.put("problem", a.problem); event.put("failure", a.failure);
        event("attempt-finished", event);
        publish(c);
        if (a.stage.generation()) {
            Path evidence = model.path(a.directory).resolve("evidence.json");
            if (Files.isRegularFile(evidence)) a.codexEvidence = AutoLlmFileUtil.read(evidence, CodexEvidenceUtil.Evidence.class);
            publish(c);
        }
    }
    private void runTrial(AutoLlmExecutionContext c, AutoLlmCampaign.Trial t) {
        awaitCapacity(c, t);
        awaitQuota(c, t);
        Path root = executionRoot; Path source = root.resolve("source");
        AutoLlmFileUtil.verifySources(root, AutoLlmFileUtil.read(root.resolve("control/frozen-hashes.json"), new TypeReference<>() {}));
        prepareAssignmentSource(c, t);
        cloneProject(c, c.baselineCommit, source, root.resolve("clone"));
        if (c.config.execution().target() == AutoLlmConfig.Target.SSH && t.resumeTurn) reconcileMetadata(c, t, source);
        while (t.nextStage.ordinal() < TUNING.ordinal()) {
            if (localOnly && t.nextStage.ordinal() >= PREPARATION.ordinal()) return;
            if (cancelled()) throw new CancellationException();
            if (t.nextStage == GENERATION) {
                boolean continuation = t.resumeTurn;
                if (t.threadId != null && !continuation && t.repairTurns >= c.config.preparation().maxRepairs()) throw AutoLlmFailureUtil.error(AutoLlmFailureUtil.Category.COMPONENT, "REPAIRS_EXHAUSTED", "Preparation exhausted repair limit: " + t.failure, root);
                t.status = TrialStatus.WAITING_FOR_GENERATION; publish(c);
                try (var permit = es.urjc.etsii.grafo.util.ConcurrencyUtil.acquirePermit(sessions, this::cancelled)) {
                if (t.capacityWait != null) CodexCapacityUtil.requireBudget(t.capacityWait, capacityClock);
                var a = attempt(c, t, t.threadId == null || continuation && t.outputDirectory(GENERATION) == null ? AttemptStage.GENERATION : AttemptStage.REPAIR); Path dir = model.path(a.directory);
                t.repairPending |= t.threadId != null && !continuation;
                t.resumeTurn = false; publish(c);
                try {
                    String prompt = c.prompt + "\nBuild/test command: " + c.config.project().buildCommand() + "\nFrozen seed descriptions: " + c.config.initialConfigurationsFile() + "\nTraining inputs: " + c.config.trainingPath();
                    prompt += "\nGeneration and initial preparation run locally; keep analysis and edits in this checkout. Use the controller metadata helper instead of local builds or experiments: " + metadataHelper(c, t) + ". It runs build/tests and --autollm-check in the preparation context and returns local evidence. Do not invoke pilots/tuning/comparisons yourself.";
                    if (t.failure != null && t.problem != null && t.problem.category() == AutoLlmFailureUtil.Category.COMPONENT) prompt += "\nRepair feedback:\n" + t.failure + "\nInspect the previous verification logs under " + root.resolve("attempts");
                    CodexTaskRunner.Result result;
                    try (var bridge = new GenerationMetadataBridge(source, root.resolve("metadata-requests"), options -> metadataCheck(c.id, t.number, options.get("blacklist"), options.get("requestId")))) {
                        metadataBridge = bridge;
                        result = codex.run(c.config, source, dir, prompt, t.threadId, () -> cancelled() || c.problem != null);
                    }
                    try {
                        metadataLock.lockInterruptibly();
                        try { if (t.repairPending) t.repairTurns++; t.repairPending = false; t.capacityWait = null; t.threadId = result.threadId(); t.blacklist = result.manifest().blacklist(); t.complete(GENERATION, a.directory, null); t.failure = null; t.problem = null; finish(c, a, null); }
                        finally { metadataLock.unlock(); }
                    } catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new CancellationException("Generation interrupted while awaiting a metadata check"); }
                } catch (RuntimeException e) {
                    if (c.problem != null && e instanceof CancellationException && !cancelled()) e = new AutoLlmFailureUtil.StageException(c.problem, e);
                    if (e instanceof CodexQuotaUtil.QuotaLimitException quota) t.quotaRetryAt = quota.retryAt().toString();
                    t.failure = ExceptionUtil.stacktrace(e); finish(c, a, e);
                    if (a.codexEvidence != null && a.codexEvidence.threadId() != null) t.threadId = a.codexEvidence.threadId();
                    t.resumeTurn = e instanceof CancellationException && t.threadId != null; publish(c);
                    if (e instanceof CancellationException) throw e;
                    t.resumeTurn = t.threadId != null; publish(c); throw e;
                }
                }
            }
            if (t.nextStage == LOCAL_PREPARATION) {
                try (var permit = es.urjc.etsii.grafo.util.ConcurrencyUtil.acquirePermit(buildPermits, this::cancelled)) { runStage(c, t, LOCAL_PREPARATION, (a, dir) -> {
                    runCommand(c, c.config.project().buildCommand(), source, dir.resolve("build"), c.config.execution().buildTimeoutMillis(), false);
                    Path jar = source.resolve(c.config.project().artifact());
                    verifyDependencies(baselineArtifact(c), jar, dir);
                    child(c, t, jar, dir.resolve("check"), "--autollm-check", 0, c.config.execution().searchSpaceTimeoutMillis(), List.of());
                    return null;
                }); } catch (RuntimeException e) {
                    var problem = AutoLlmFailureUtil.describe(e, root);
                    if (problem.category() != AutoLlmFailureUtil.Category.COMPONENT) throw e;
                    t.problem = problem; t.failure = ExceptionUtil.stacktrace(e); t.nextStage = GENERATION; publish(c); continue;
                }
                if (localOnly) return;
            }
            if (c.config.execution().target() == AutoLlmConfig.Target.SSH) {
                t.status = TrialStatus.METADATA_RECONCILIATION; publish(c); reconcileMetadata(c, t, source);
            }
            t.failure = null; t.problem = null;
            try { runStage(c, t, PREPARATION, (a, dir) -> {
                substage(c, a, PreparationStage.BUILD);
                runCommand(c, c.config.project().buildCommand(), source, dir.resolve("build"), c.config.execution().buildTimeoutMillis(), false);
                var generatedHashes = AutoLlmFileUtil.sourceHashes(source);
                Path jar = AutoLlmFileUtil.inside(source, c.config.project().artifact());
                var dependencies = AutoLlmFileUtil.dependencyHashes(jar);
                AutoLlmFileUtil.write(dir.resolve("dependencies.json"), dependencies);
                substage(c, a, PreparationStage.DEPENDENCIES);
                verifyDependencies(baselineArtifact(c), jar, dir);
                substage(c, a, PreparationStage.METADATA);
                child(c, t, jar, dir.resolve("check"), "--autollm-check", 0, c.config.execution().searchSpaceTimeoutMillis(), List.of());
                String localPreparation = t.outputDirectory(LOCAL_PREPARATION);
                if (localPreparation != null) {
                    var localMetadata = AutoLlmFileUtil.read(model.path(localPreparation).resolve("check/execution/search-space.json"), Object.class);
                    var remoteMetadata = AutoLlmFileUtil.read(dir.resolve("check/execution/search-space.json"), Object.class);
                    if (!AutoLlmFileUtil.canonicalHash(localMetadata).equals(AutoLlmFileUtil.canonicalHash(remoteMetadata)))
                        throw AutoLlmFailureUtil.error(AutoLlmFailureUtil.Category.INTEGRITY, "PREPARATION_METADATA_CHANGED", "Generated search-space metadata differs between local preparation and the assigned target", dir);
                }
                substage(c, a, PreparationStage.PROBES);
                child(c, t, jar, dir.resolve("probe"), "--autollm-probe", 0, c.config.preparation().probeDurationMillis() + c.config.execution().startupTimeoutMillis(), List.of());
                publish(c);
                substage(c, a, PreparationStage.PILOT);
                long pilotMillis = c.config.preparation().pilotDurationMillis();
                child(c, t, jar, dir.resolve("pilot"), "--autoconfig", pilotMillis, c.config.execution().startupTimeoutMillis() + pilotMillis + c.config.execution().phaseTimeoutMillis() + c.config.execution().watchdogGraceMillis(), List.of());
                boolean observed = false;
                for (var event : AutoLlmFileUtil.events(dir.resolve("pilot/execution/events.jsonl"))) if ("evaluation-result".equals(event.get("kind")) && "SUCCEEDED".equals(event.get("state"))) observed = true;
                if (!observed) throw AutoLlmFailureUtil.error(AutoLlmFailureUtil.Category.UNKNOWN, "EMPTY_PILOT", "Verification autoconfig produced no successful evaluations", dir);
                Path frozen = root.resolve("frozen"); Files.createDirectories(frozen);
                boolean managed = Files.exists(c.campaignRoot.resolve("control/storage-layout.json"));
                Path executable;
                if (managed) executable = CampaignAssetStore.putArtifact(c.campaignRoot, root, jar);
                else { executable = frozen.resolve("project.jar"); Files.copy(jar, executable, StandardCopyOption.REPLACE_EXISTING); }
                if (Files.exists(c.campaignRoot.resolve("control/storage-layout.json"))) {
                    AutoLlmSourceService.freeze(c.campaignRoot, root, source, dir, c.baselineCommit, c.config, c.versions,
                            (args, cwd, logs) -> runCommand(c, args, cwd, logs, c.config.execution().buildTimeoutMillis(), true));
                } else AutoLlmFileUtil.archiveSources(source, frozen.resolve("source.zip"), generatedHashes);
                AutoLlmFileUtil.write(frozen.resolve("source-hashes.json"), generatedHashes);
                Files.copy(dir.resolve("check/execution/search-space.json"), frozen.resolve("search-space.json"), StandardCopyOption.REPLACE_EXISTING);
                Files.copy(dir.resolve("check/execution/components.json"), frozen.resolve("components.json"), StandardCopyOption.REPLACE_EXISTING);
                var identity = new LinkedHashMap<String,String>(); identity.put("sha256", AutoLlmFileUtil.hash(executable)); identity.put("blacklist", t.blacklist == null ? "" : t.blacklist);
                if (managed) identity.put("asset", model.relative(executable).replace('\\', '/'));
                AutoLlmFileUtil.write(frozen.resolve("artifact.json"), identity);
                if (Files.exists(frozen.resolve("source-snapshot.json"))) AutoLlmFileUtil.write(frozen.resolve("executable-fingerprint.json"), AutoLlmExecutableFingerprint.read(executable));
                t.failure = null; return model.relative(executable);
            }); } catch (RuntimeException e) {
                if (e instanceof CancellationException) throw e;
                t.problem = AutoLlmFailureUtil.describe(e, model.path(t.attempts.getLast().directory)); t.failure = ExceptionUtil.stacktrace(e);
                if (t.problem.category() != AutoLlmFailureUtil.Category.COMPONENT) throw e;
                t.nextStage = GENERATION; publish(c);
                if (!localOnly) return;
            }
        }
        Path jar = model.path(t.artifact());
        var identity = AutoLlmFileUtil.read(executionRoot.resolve("frozen/artifact.json"), Map.class);
        if (!AutoLlmFileUtil.hash(jar).equals(identity.get("sha256"))) throw AutoLlmFailureUtil.error(AutoLlmFailureUtil.Category.INTEGRITY, "ARTIFACT_CHANGED", "Frozen artifact was modified", jar);
        if (t.nextStage == TUNING && t.assignment() != null && !t.assignment().pinned) {
            Path pin = executionRoot.resolve("control/assignment-pinned.json");
            AutoLlmFileUtil.write(pin, Map.of("assignmentId", t.assignment().id, "stage", "TUNING"));
            try { RemoteCampaignStorage.force(pin); } catch (IOException e) { throw new UncheckedIOException(e); }
            t.assignment().pinned = true; publish(c);
        }
        if (t.nextStage == TUNING) runStage(c, t, TUNING, (attempt, dir) -> {
            child(c, t, jar, dir, "--autoconfig", 0, c.config.execution().stageTimeoutMillis(), List.of());
            return null;
        });
        if (t.nextStage == WINNER_EXPORT) runStage(c, t, WINNER_EXPORT, (attempt, dir) -> {
            child(c, t, jar, dir, "--autollm-export", 0, c.config.execution().searchSpaceTimeoutMillis(), List.of("--autollm.child.elites-file=" + model.path(t.outputDirectory(TUNING)).resolve("autoconfig-final-elites.json")));
            t.winner = AutoLlmFileUtil.read(dir.resolve("execution/winner.json"), Object.class);
            return null;
        });
        if (t.nextStage == COMPARISON) runStage(c, t, COMPARISON, (attempt, dir) -> {
            child(c, t, jar, dir, "--default", 0, c.config.execution().stageTimeoutMillis(), List.of("--autollm.child.comparison-enabled=true", "--autollm.child.winner-file=" + model.path(t.outputDirectory(WINNER_EXPORT)).resolve("execution/winner.json"), "--solver.experiments=autollm-comparison", "--solver.repetitions=" + c.config.comparison().repetitions()));
            return null;
        });
    }
    @FunctionalInterface private interface StageOperation {
        String run(AutoLlmCampaign.Attempt attempt, Path directory) throws IOException;
    }
    private void runStage(AutoLlmExecutionContext context, AutoLlmCampaign.Trial trial, AutoLlmCampaign.Stage stage, StageOperation operation) {
        var attempt = attempt(context, trial, AttemptStage.valueOf(stage.name()));
        Path directory = model.path(attempt.directory);
        try {
            String artifact = operation.run(attempt, directory);
            Files.deleteIfExists(directory.resolve("collection-failure.json"));
            trial.complete(stage, attempt.directory, artifact);
            finish(context, attempt, null);
        } catch (IOException | UncheckedIOException e) {
            var failure = AutoLlmFailureUtil.error(AutoLlmFailureUtil.Category.INFRASTRUCTURE, "EVIDENCE_COLLECTION", "Retry completed stage collection: " + e, directory);
            AutoLlmFileUtil.write(directory.resolve("collection-failure.json"), failure.failure());
            finish(context, attempt, failure); throw failure;
        } catch (RuntimeException e) { finish(context, attempt, e); throw e; }
    }
    private void awaitCapacity(AutoLlmExecutionContext campaign, AutoLlmCampaign.Trial trial) {
        if (trial.capacityWait == null) return;
        trial.status = TrialStatus.WAITING_FOR_CAPACITY; publish(campaign);
        while (true) {
            if (cancelled() || Thread.currentThread().isInterrupted()) throw new CancellationException("Capacity wait cancelled");
            CodexCapacityUtil.requireBudget(trial.capacityWait, capacityClock);
            long remaining = java.time.Duration.between(capacityClock.instant(), Instant.parse(trial.capacityWait.nextRetryAt())).toMillis();
            if (remaining <= 0) break;
            capacitySleep.accept(Math.min(1000, remaining));
        }
        trial.problem = null; trial.failure = null; publish(campaign);
        event("codex-capacity-resumed", Map.of("trial", trial.number, "retry", trial.capacityWait.retries()));
    }
    private void awaitQuota(AutoLlmExecutionContext campaign, AutoLlmCampaign.Trial trial) {
        if (trial.quotaRetryAt == null) return;
        trial.status = TrialStatus.WAITING_FOR_QUOTA; publish(campaign);
        long reset = Instant.parse(trial.quotaRetryAt).toEpochMilli();
        while (true) {
            if (cancelled()) throw new CancellationException("Quota wait cancelled");
            long remaining = reset - System.currentTimeMillis();
            if (remaining <= 0) break;
            try { Thread.sleep(Math.min(1000, remaining)); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new CancellationException("Quota wait interrupted"); }
        }
        trial.quotaRetryAt = null; trial.problem = null; trial.failure = null; publish(campaign);
        event("codex-quota-resumed", Map.of("trial", trial.number));
    }
    private Path baselineArtifact(AutoLlmExecutionContext c) {
        Path identity = executionRoot.resolve("baseline/artifact.json");
        if (!Files.isRegularFile(identity)) return executionRoot.resolve("baseline/source").resolve(c.config.project().artifact());
        Map<String,String> entry = AutoLlmFileUtil.read(identity, new TypeReference<>() {});
        Path artifact = WorkloadPathUtil.inside(c.campaignRoot, entry.get("asset"));
        if (!AutoLlmFileUtil.hash(artifact).equals(entry.get("sha256"))) throw new IllegalStateException("Shared baseline artifact changed");
        return artifact;
    }
    private void runCommand(AutoLlmExecutionContext c, List<String> command, Path cwd, Path dir, long timeout, boolean local) {
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
            if (command.equals(c.config.project().buildCommand())) throw AutoLlmFailureUtil.buildFailure(cwd, c.baselineSource == null ? executionRoot.resolve("baseline/source") : c.baselineSource, dir, message, AutoLlmExecutionService.localPaths(c.config, executionRoot));
            throw AutoLlmFailureUtil.error(AutoLlmFailureUtil.Category.INFRASTRUCTURE, "PREREQUISITE_FAILED", message, dir);
        }
    }
    private AutoLlmProcessRunner.Result child(AutoLlmExecutionContext c, AutoLlmCampaign.Trial t, Path jar, Path output, String mode, long pilotMillis, long hardTimeout, List<String> extra) {
        Path root = executionRoot; Path controls = root.resolve("control/frozen-hashes.json");
        if (Files.exists(controls)) AutoLlmFileUtil.verifySources(root, AutoLlmFileUtil.read(controls, new TypeReference<>() {}));
        String artifactHash = AutoLlmFileUtil.hash(jar);
        if (t != null && t.artifact() != null && !artifactHash.equals(AutoLlmFileUtil.read(root.resolve("frozen/artifact.json"), Map.class).get("sha256"))) throw new IllegalStateException("Frozen artifact was modified");
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
                "--solver.parallel-executor=" + (workers > 1), "--solver.n-workers=" + workers, "--solver.progress-bar=false", "--solver.metrics=true", "--serializers.xlsx.enabled=true", "--serializers.xlsx.frequency=EXPERIMENT_END"));
        if (t != null) args.addAll(3, List.of("-Dmork.execution.assignment=" + t.activeAssignmentId, "-Dmork.execution.target=" + t.targetId, "-Dmork.execution.pool-revision=" + t.targetRevision, "-Dmork.execution.campaign=" + c.id, "-Dmork.execution.trial=" + t.number, "-Dmork.execution.tuning-seed=" + t.tuningSeed, "-Dmork.execution.artifact-sha256=" + artifactHash));
        if (t != null) { args.add("--autollm.child.baseline-components-file=" + executionRoot.resolve("baseline/components.json")); if (t.blacklist != null && !t.blacklist.isBlank()) args.add("--blacklist=" + t.blacklist); }
        boolean newFilter = t != null && t.blacklist != null && !t.blacklist.isBlank();
        boolean detailedTiming = false;
        for (String argument : c.config.project().runArguments()) {
            if (argument.split("=", 2)[0].replace("-", "").replace("_", "").equalsIgnoreCase("solver.timestats")) { detailedTiming = Boolean.parseBoolean(argument.split("=", 2)[1]); continue; }
            if (newFilter && (argument.startsWith("--blacklist=") || argument.startsWith("--whitelist="))) continue;
            args.add(argument);
        }
        args.add("--solver.time-stats=" + detailedTiming);
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
    private TrialStatus beforeWorkspaceWait;
    private void remoteState(AutoLlmExecutionContext campaign, Map<String,Object> state) {
        if (campaign.trial != null) {
            if ("WAITING_FOR_WORKSPACE".equals(state.get("status"))) {
                if (campaign.trial.status != TrialStatus.WAITING_FOR_WORKSPACE) beforeWorkspaceWait = campaign.trial.status;
                campaign.trial.status = TrialStatus.WAITING_FOR_WORKSPACE;
            } else if (campaign.trial.status == TrialStatus.WAITING_FOR_WORKSPACE && beforeWorkspaceWait != null) {
                campaign.trial.status = beforeWorkspaceWait;
                beforeWorkspaceWait = null;
            }
        }
        campaign.remote = state;
        if (campaign.trial != null && state.get("jobId") instanceof String job && state.get("connectionRetries") instanceof Number retries)
            campaign.trial.connectionRetries.merge(job, retries.intValue(), Math::max);
        publish(campaign);
    }
    private AutoLlmConfig screenedConfig(AutoLlmExecutionContext campaign) {
        Path proposal = Path.of(campaign.screeningPath).resolve("proposal");
        return campaign.config.withPaths(proposal.resolve("proposed-train.index").toString(), proposal.resolve("proposed-test.index").toString());
    }
    private void reconcileMetadata(AutoLlmExecutionContext campaign, AutoLlmCampaign.Trial trial, Path source) {
        Path root = executionRoot, directory = root.resolve("metadata");
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
    private String metadataHelper(AutoLlmExecutionContext campaign, AutoLlmCampaign.Trial trial) {
        Path root = executionRoot, helper = root.resolve("control/metadata-check-" + trial.number + ".py");
        Path queue = root.resolve("source/.mork-metadata");
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
            metadataThread = Thread.currentThread();
            if (shuttingDown) throw new CancellationException("Metadata check interrupted by controller shutdown");
            if (!id.equals(model.id)) throw new IllegalArgumentException("Campaign mismatch");
            var campaign = model;
            var trial = campaign.trial;
            if (trial != null && trial.number != number) throw new IllegalArgumentException("Trial mismatch");
            if (trial == null || !(Set.of(TrialStatus.GENERATION, TrialStatus.REPAIR).contains(trial.status) || trial.attempts.isEmpty() || trial.attempts.getLast().status != AttemptStatus.RUNNING)) throw new IllegalStateException("Metadata checks are available only during generation/repair");
            Path root = executionRoot, source = root.resolve("source");
            Path output = root.resolve("metadata/" + requestId);
            // The metadata bridge/API owns this check while the campaign thread waits for local Codex.
            try (var permit = es.urjc.etsii.grafo.util.ConcurrencyUtil.acquirePermit(buildPermits, this::cancelled)) {
                runCommand(campaign, campaign.config.project().buildCommand(), source, output.resolve("build"), campaign.config.execution().buildTimeoutMillis(), false);
                var probe = new AutoLlmCampaign.Trial(); probe.targetId = trial.targetId; probe.targetRevision = trial.targetRevision; probe.number = trial.number; probe.pilotSeed = trial.pilotSeed; probe.blacklist = blacklist == null ? trial.blacklist : blacklist;
                child(campaign, probe, source.resolve(campaign.config.project().artifact()), output.resolve("check"), "--autollm-check", 0, campaign.config.execution().searchSpaceTimeoutMillis(), List.of());
                return Map.of("directory", output.toString(), "check", AutoLlmFileUtil.read(output.resolve("check/execution/check.json"), Object.class));
            } catch (RuntimeException e) {
                if (e instanceof CancellationException) throw e;
                var problem = AutoLlmFailureUtil.describe(e, output);
                if (problem.category() != AutoLlmFailureUtil.Category.COMPONENT) { campaign.problem = problem; trial.problem = problem; publish(campaign); }
                throw e;
            }
        } finally { metadataThread = null; metadataLock.unlock(); }
    }
}
