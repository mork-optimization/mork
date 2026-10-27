package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmCampaign.CampaignStatus;
import es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmCampaign.TargetStatus;
import es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmCampaign.TrialStatus;
import es.urjc.etsii.grafo.autoconfig.autollm.AutoLlmCampaign.AttemptStatus;

import es.urjc.etsii.grafo.autoconfig.builder.ComponentSpec;
import es.urjc.etsii.grafo.autoconfig.autollm.execution.*;
import es.urjc.etsii.grafo.autoconfig.builder.ComponentSpecJsonCodec;
import es.urjc.etsii.grafo.config.InstanceConfiguration;
import es.urjc.etsii.grafo.util.ExceptionUtil;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;
import java.io.*;
import java.nio.file.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

/** One serialized coordinator dispatches isolated target runners. */
@Service
public class AutoLlmCampaignService {
    private final AutoLlmConfig defaults;
    private final AutoLlmCampaignStore store;
    private final AutoLlmProcessRunner processes;
    private final CodexTaskRunner codex;
    private final AutoLlmReportService<?,?> reports;
    private final InstanceConfiguration instances;
    private final AutoLlmTargetProbeService targetProbes;
    public AutoLlmCampaignService(AutoLlmConfig defaults, AutoLlmCampaignStore store, AutoLlmProcessRunner processes,
            CodexTaskRunner codex, AutoLlmReportService<?,?> reports, InstanceConfiguration instances) {
        this.defaults = defaults; this.store = store; this.processes = processes; this.codex = codex; this.reports = reports; this.instances = instances;
        this.targetProbes = new AutoLlmTargetProbeService(store, processes);
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
        if (c.preparation().probeDurationMillis() <= 0 || c.preparation().pilotDurationMillis() <= 0 || c.tuning().evaluations() <= 0 || c.comparison().repetitions() <= 0) errors.add("Budgets and repetitions must be positive");
        if (c.tuning().ignoreInitialMillis() < 0 || c.tuning().intervalDurationMillis() <= 0 || c.comparison().timeLimitMillis() < 0) errors.add("Invalid algorithm time horizon");
        if (c.codex().maxConcurrentSessions() < 1) errors.add("Concurrent Codex sessions must be positive");
        errors.addAll(AutoLlmExecutionService.validate(c));
        if (!Path.of(c.workspace()).toAbsolutePath().normalize().equals(store.root())) errors.add("Workspace is fixed by backend configuration");
        Path repo = Path.of(c.project().repository()).toAbsolutePath().normalize();
        errors.addAll(AutoLlmConfigUtil.projectErrors(c));
        for (String value : Arrays.asList(c.sotaFile(), c.trainingPath(), c.comparisonPath())) if (value == null || value.isBlank()) errors.add("SOTA, training, and comparison paths are required");
        if (c.sotaFile() != null && !Files.isRegularFile(resolve(repo, c.sotaFile()))) errors.add("SOTA file does not exist");
        if (c.initialConfigurationsFile() != null && !c.initialConfigurationsFile().isBlank() && !Files.isRegularFile(resolve(repo, c.initialConfigurationsFile()))) errors.add("Initial configuration file does not exist");
        if (c.trainingPath() != null && !Files.exists(resolve(repo, c.trainingPath()))) errors.add("Training input does not exist");
        if (c.comparisonPath() != null && !Files.exists(resolve(repo, c.comparisonPath()))) errors.add("Comparison input does not exist");
        return errors;
    }
    private AutoLlmCampaign createInternal(AutoLlmConfig requested) {
        var errors = validate(requested); if (!errors.isEmpty()) throw new IllegalArgumentException(String.join("; ", errors));
        var c = new AutoLlmCampaign(); c.id = UUID.randomUUID().toString(); c.createdAt = Instant.now().toString();
        Path directory = store.directory(c.id); Path repo = Path.of(requested.project().repository()).toAbsolutePath().normalize();
        var codec = new ComponentSpecJsonCodec();
        try {
            var sota = codec.parse(Files.readString(resolve(repo, requested.sotaFile())));
            var seeds = new LinkedHashSet<ComponentSpec>(); seeds.add(sota);
            if (requested.initialConfigurationsFile() != null && !requested.initialConfigurationsFile().isBlank()) seeds.addAll(codec.parseList(Files.readString(resolve(repo, requested.initialConfigurationsFile()))));
            Files.createDirectories(directory.resolve("control"));
            var instancePaths = new LinkedHashMap<String,String>();
            String reference = instances.getPaths() == null ? null : instances.getPaths().get("reference");
            if (reference != null && !reference.isBlank()) instancePaths.put("reference", es.urjc.etsii.grafo.util.InstancePathUtil.resolve(repo, reference.trim()));
            if (!instancePaths.isEmpty()) AutoLlmFileUtil.write(directory.resolve("control/instance-paths.json"), instancePaths);
            var aliases = AutoLlmInputUtil.aliases(resolve(repo, requested.trainingPath()), resolve(repo, requested.comparisonPath()));
            if (!aliases.isEmpty()) AutoLlmFileUtil.write(directory.resolve("control/input-aliases.json"), aliases);
            Files.copy(resolve(repo, requested.sotaFile()), directory.resolve("control/sota.json"), StandardCopyOption.REPLACE_EXISTING);
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
            registerInitial(c); store.save(c); AutoLlmFileUtil.write(directory.resolve("control/config.json"), c.config); return c;
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }
    private Path resolve(Path base, String path) { return base.resolve(path).normalize().toAbsolutePath(); }
    private final ExecutorService coordinator = Executors.newSingleThreadExecutor(Thread.ofVirtual().name("autollm-coordinator").factory());
    private final ExecutorService workers = Executors.newVirtualThreadPerTaskExecutor();
    private final ExecutorService reporting = Executors.newSingleThreadExecutor(Thread.ofVirtual().name("autollm-reporting").factory());
    private final Map<String, Running> jobs = new HashMap<>();
    private record Running(String token, AutoLlmTrialRunner runner, Future<?> future) {}
    private volatile String active;
    private volatile boolean cancelled;
    private volatile boolean closing;
    private AutoLlmCampaign current;
    private AutoLlmCampaignStore.OperationLease ownership;
    private Semaphore sessions;
    private boolean checkingBaseline;
    private volatile long reportSequence;
    private final Set<String> reportedAttempts = new HashSet<>();

    private <T> T invoke(Callable<T> operation) {
        try { return coordinator.submit(operation).get(); }
        catch (InterruptedException e) { Thread.currentThread().interrupt(); throw new CancellationException(); }
        catch (ExecutionException e) {
            if (e.getCause() instanceof RuntimeException runtime) throw runtime;
            throw new IllegalStateException(e.getCause());
        }
    }
    private void enqueue(Runnable operation) { if (!closing) try { coordinator.execute(operation); } catch (RejectedExecutionException ignored) { /* Controller is shutting down. */ } }
    private void save() { store.save(current); }
    public AutoLlmCampaign create(AutoLlmConfig config) { return invoke(() -> createInternal(config)); }
    public String active() { return active; }
    private void register(AutoLlmCampaign c, AutoLlmConfig.Server server) {
        AutoLlmPoolUtil.requireId(server.id());
        var existing = c.targets.get(server.id());
        if (existing != null) {
            if (!existing.definition.equals(server)) throw new IllegalStateException("Target IDs are immutable; drain/remove and register a new ID");
            return;
        }
        if (c.config.execution().target() != AutoLlmConfig.Target.SSH) throw new IllegalStateException("Local campaigns have one fixed target");
        var errors = AutoLlmExecutionService.validate(c.config.forServer(server));
        if (!errors.isEmpty()) throw new IllegalArgumentException(String.join("; ", errors));
        var target = new AutoLlmCampaign.TargetState(); target.definition = server;
        target.directory = "targets/" + server.id(); target.revision = ++c.poolRevision;
        c.targets.put(server.id(), target);
        store.event(c.id, "target-registered", Map.of("targetId", server.id(), "revision", c.poolRevision));
    }
    private void registerInitial(AutoLlmCampaign c) {
        if (c.config.execution().target() == AutoLlmConfig.Target.SSH) {
            for (var server : c.config.execution().ssh().servers()) register(c, server);
        } else {
            var target = new AutoLlmCampaign.TargetState();
            target.definition = new AutoLlmConfig.Server("local", null, null, null, null, null, null, null);
            target.directory = "targets/local"; target.revision = ++c.poolRevision; c.targets.put("local", target);
        }
    }
    private void acquire(AutoLlmCampaign c) {
        if (active != null) throw new IllegalStateException("A campaign is already active");
        ownership = store.acquire(c.id, store.directory(c.id)); current = c; active = c.id; cancelled = false;
        sessions = new Semaphore(c.config.codex().maxConcurrentSessions(), true); reportedAttempts.clear();
    }
    private void release() {
        try { if (ownership != null) ownership.close(); }
        finally { ownership = null; active = null; current = null; }
    }
    private void pin(AutoLlmCampaign c) {
        Path root = store.directory(c.id);
        if (c.baselineCommit != null) { verifyCanonical(c); return; }
        Path repo = Path.of(c.config.project().repository()), evidence = root.resolve("canonical/ref");
        var result = processes.run(List.of("git", "rev-parse", "--verify", c.config.project().ref() + "^{commit}"), repo, evidence, 30000, 0, 1000, () -> false, false, 0);
        if (result.exitCode() != 0) throw new IllegalStateException("Cannot resolve committed baseline; inspect " + evidence);
        try {
            String commit = Files.readString(evidence.resolve("stdout.log")).strip();
            if (c.config.execution().target() == AutoLlmConfig.Target.SSH) {
                var prepared = es.urjc.etsii.grafo.autoconfig.screening.RemoteScreeningService.approved(c.config);
                if (!prepared.commit().equals(commit)) throw new IllegalStateException("Baseline differs from approved screening");
                c.approvedScreeningPath = Path.of(c.config.trainingPath()).getParent().getParent().toString();
            }
            c.config = c.config.withRef(commit);
            String train = AutoLlmInputUtil.snapshot(root, "training", es.urjc.etsii.grafo.util.InstancePathUtil.list(c.config.trainingPath()));
            String test = AutoLlmInputUtil.snapshot(root, "comparison", es.urjc.etsii.grafo.util.InstancePathUtil.list(c.config.comparisonPath()));
            c.config = c.config.withPaths(train, test); c.baselineCommit = commit;
            var hashes = new TreeMap<String,String>();
            for (String name : List.of("sota.json", "seeds.json")) hashes.put("control/" + name, AutoLlmFileUtil.hash(root.resolve("control/" + name)));
            AutoLlmInputUtil.addSettingsHashes(hashes, root);
            for (String name : List.of("training", "comparison")) AutoLlmInputUtil.addHashes(hashes, root, name);
            AutoLlmFileUtil.write(root.resolve("control/canonical-hashes.json"), hashes);
            AutoLlmFileUtil.write(root.resolve("control/config.json"), c.config); store.save(c);
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }
    private void verifyCanonical(AutoLlmCampaign c) {
        Path root = store.directory(c.id);
        try { AutoLlmFileUtil.verifySources(root, AutoLlmFileUtil.read(root.resolve("control/canonical-hashes.json"), new TypeReference<>() {})); }
        catch (RuntimeException e) { throw AutoLlmFailureUtil.error(AutoLlmFailureUtil.Category.INTEGRITY, "CANONICAL_INPUT_CHANGED", e.getMessage(), root.resolve("control/canonical-hashes.json")); }
    }
    public AutoLlmCampaign start(String id, boolean resume, Set<String> selectedTargets) {
        return invoke(() -> {
            var c = store.load(id);
            if (!resume && c.status != CampaignStatus.DRAFT) throw new IllegalStateException("Only draft campaigns can start");
            if (resume) recoveryInternal(c).resumeCampaign().requireAllowed();
            if (resume && !selectedTargets.isEmpty()) throw new IllegalArgumentException("Target selection is only supported when starting a draft");
            if (!resume) {
                for (String targetId : selectedTargets) {
                    var target = requireTarget(c, targetId);
                    if (target.problem != null || !(target.baselineReady || target.status == TargetStatus.VALIDATED))
                        throw new IllegalStateException("Selected target must pass the baseline check: " + targetId);
                }
                if (selectedTargets.isEmpty()) {
                    for (var target : c.targets.values()) if (!target.draining && target.problem != null)
                        throw new IllegalStateException("Some targets failed qualification; explicitly select qualified targets to start a subset");
                } else {
                    for (var entry : c.targets.entrySet()) entry.getValue().draining = !selectedTargets.contains(entry.getKey());
                }
            }
            acquire(c);
            try {
                pin(c); c.status = CampaignStatus.RUNNING; c.problem = null; c.failure = null;
                for (var target : c.targets.values()) if (target.status != TargetStatus.REMOVED) {
                    target.problem = null; target.assignedTrial = null;
                    target.status = target.draining ? TargetStatus.DRAINING : target.baselineReady ? TargetStatus.READY : TargetStatus.REGISTERED;
                }
                if (resume) for (var t : c.trials) if (!t.status.terminal()) {
                    t.queueForResume();
                }
                save(); dispatch(); return c.copy();
            } catch (RuntimeException e) { c.status = CampaignStatus.NEEDS_ATTENTION; c.problem = AutoLlmFailureUtil.describe(e, store.directory(id)); try { save(); } finally { release(); } throw e; }
        });
    }
    private AutoLlmExecutionContext executionModel(AutoLlmCampaign c, String targetId, AutoLlmCampaign.Trial trial) {
        var target = c.targets.get(targetId); Path root = store.directory(c.id).resolve(target.directory);
        AutoLlmFileUtil.write(root.resolve("execution-context.json"), AutoLlmPoolUtil.identity(c.id, targetId));
        var model = new AutoLlmExecutionContext(c.id, target.directory, store.directory(c.id), c.prompt);
        model.approvedScreeningPath = c.approvedScreeningPath;
        model.config = target.resolvedConfig != null ? target.resolvedConfig : c.config.execution().target() == AutoLlmConfig.Target.LOCAL ? c.config : c.config.forServer(target.definition);
        model.baselineCommit = target.baselineReady ? c.baselineCommit : null; model.executionHost = target.host; model.versions = target.versions;
        if (target.resolvedConfig == null) {
            try {
                Files.createDirectories(root.resolve("control"));
                for (String file : List.of("sota.json", "seeds.json")) Files.copy(store.directory(c.id).resolve("control/" + file), root.resolve("control/" + file), StandardCopyOption.REPLACE_EXISTING);
                AutoLlmInputUtil.copySettings(store.directory(c.id), root);
            } catch (IOException e) { throw new UncheckedIOException(e); }
            var cfg = model.config;
            model.config = new AutoLlmConfig(cfg.project(), cfg.workspace(), cfg.trials(), cfg.seed(), cfg.maxParameters(), root.resolve("control/sota.json").toString(), root.resolve("control/seeds.json").toString(), cfg.trainingPath(), cfg.comparisonPath(), cfg.codex(), cfg.preparation(), cfg.tuning(), cfg.comparison(), cfg.execution());
        }
        AutoLlmFileUtil.write(root.resolve("control/config.json"), model.config);
        if (trial != null) model.trial = trial.copy();
        return model;
    }
    protected AutoLlmTrialRunner runner(AutoLlmExecutionContext model, Path root, AutoLlmTrialRunner.Listener listener, java.util.function.BooleanSupplier stop, Semaphore permits) {
        return new AutoLlmTrialRunner(model, root, processes, codex, listener, stop, permits);
    }
    private AutoLlmTrialRunner newRunner(String targetId, String token, AutoLlmCampaign.Trial trial) {
        var c = current; var target = c.targets.get(targetId);
        return runner(executionModel(c, targetId, trial), store.directory(c.id).resolve(target.directory), new AutoLlmTrialRunner.Listener() {
            public void state(AutoLlmExecutionContext.Snapshot snapshot) { enqueue(() -> update(targetId, token, snapshot)); }
            public void event(String kind, Object detail) { enqueue(() -> { if (current != null && jobs.containsKey(targetId) && jobs.get(targetId).token.equals(token)) store.event(c.id, kind, Map.of("targetId", targetId, "detail", detail)); }); }
        }, () -> cancelled, sessions);
    }
    private void update(String targetId, String token, AutoLlmExecutionContext.Snapshot model) {
        var running = jobs.get(targetId); if (running == null || !running.token.equals(token) || current == null) return;
        apply(targetId, model); save();
        if (model.trial() != null) for (var attempt : model.trial().attempts) {
            if (attempt.codexEvidence != null && attempt.status != AttemptStatus.RUNNING && reportedAttempts.add(targetId + "/" + model.trial().number + "/" + attempt.number)) reportAsync(false);
        }
    }
    private void apply(String targetId, AutoLlmExecutionContext.Snapshot model) {
        var target = current.targets.get(targetId);
        target.resolvedConfig = model.config(); target.host = model.executionHost(); target.versions = model.versions();
        target.remote = AutoLlmPoolUtil.remote(model.remote(), target.directory);
        if (model.trial() != null) {
            var copy = model.trial().copy();
            for (int i = 0; i < current.trials.size(); i++) if (current.trials.get(i).number == copy.number) current.trials.set(i, copy);
        }
    }
    private void qualify(String targetId, boolean metadataOnly, CompletableFuture<Void> completed) {
        var target = current.targets.get(targetId); String token = UUID.randomUUID().toString();
        target.status = TargetStatus.QUALIFYING; target.problem = null;
        var taskRunner = newRunner(targetId, token, null);
        FutureTask<Void> future = new FutureTask<>(() -> {
            Throwable failure = null;
            try {
                String endpoint = taskRunner.endpoint();
                invoke(() -> { requireEndpoint(targetId, endpoint); return null; });
                taskRunner.prepare(metadataOnly);
                invoke(() -> { verifyQualification(targetId, taskRunner.snapshot()); return null; });
            } catch (Throwable e) { failure = e; }
            var model = taskRunner.snapshot(); var error = failure;
            enqueue(() -> {
                if (!owns(targetId, token)) { completed.complete(null); return; }
                apply(targetId, model); jobs.remove(targetId);
                if (error == null) { target.baselineReady = !metadataOnly; target.status = target.draining ? TargetStatus.DRAINING : metadataOnly ? TargetStatus.VALIDATED : TargetStatus.READY; target.problem = null; }
                else { target.status = TargetStatus.NEEDS_ATTENTION; target.problem = AutoLlmFailureUtil.describe(error, store.directory(current.id).resolve(target.directory)); }
                save(); completed.complete(null); if (!checkingBaseline) dispatch();
            }); return null;
        });
        jobs.put(targetId, new Running(token, taskRunner, future)); save(); workers.execute(future);
    }
    private void verifyQualification(String id, AutoLlmExecutionContext.Snapshot model) {
        Path root = store.directory(current.id), targetRoot = root.resolve(current.targets.get(id).directory);
        verifyCanonical(current);
        for (String kind : List.of("training", "comparison")) {
            String expected = kind.equals("training") ? current.config.trainingPath() : current.config.comparisonPath();
            String actual = kind.equals("training") ? model.config().trainingPath() : model.config().comparisonPath();
            var expectedHashes = new ArrayList<>(AutoLlmPoolUtil.inputIdentities(expected));
            var actualHashes = new ArrayList<>(AutoLlmPoolUtil.inputIdentities(actual));
            Collections.sort(expectedHashes); Collections.sort(actualHashes);
            if (!expectedHashes.equals(actualHashes)) throw new IllegalStateException("Qualified target changed the frozen " + kind + " selection");
        }
        var fingerprint = new LinkedHashMap<String,Object>();
        fingerprint.put("trainingOrder", AutoLlmPoolUtil.inputIdentities(model.config().trainingPath()));
        fingerprint.put("comparisonOrder", AutoLlmPoolUtil.inputIdentities(model.config().comparisonPath()));
        for (String file : List.of("baseline/components.json", "baseline-preflight/check/execution/search-space.json"))
            fingerprint.put(file, AutoLlmFileUtil.read(targetRoot.resolve(file), Object.class));
        Map<String,Object> dependencies = AutoLlmFileUtil.read(targetRoot.resolve("baseline/dependencies.json"), new TypeReference<>() {});
        fingerprint.put("dependencies", dependencies.getOrDefault("contentSha256", Map.of()));
        Path budgetFile = targetRoot.resolve("control/comparison-time-limits.json");
        if (Files.exists(budgetFile)) {
            Map<String,Long> budgets = AutoLlmFileUtil.read(budgetFile, new TypeReference<>() {});
            var orderedBudgets = new ArrayList<Long>();
            for (String input : es.urjc.etsii.grafo.util.InstancePathUtil.list(model.config().comparisonPath())) {
                Long budget = budgets.get(input);
                if (budget == null || budget <= 0) throw new IllegalStateException("Missing frozen comparison budget: " + input);
                orderedBudgets.add(budget);
            }
            fingerprint.put("comparisonLimits", orderedBudgets);
        }
        Path canonical = root.resolve("control/baseline-fingerprint.json");
        if (Files.exists(canonical)) {
            if (!AutoLlmFileUtil.JSON.readTree(AutoLlmFileUtil.JSON.writeValueAsBytes(fingerprint)).equals(AutoLlmFileUtil.JSON.readTree(read(canonical))))
                throw new IllegalStateException("Target baseline metadata or dependencies differ from the qualified pool");
        } else {
            AutoLlmFileUtil.write(canonical, fingerprint);
            Map<String,String> hashes = AutoLlmFileUtil.read(root.resolve("control/canonical-hashes.json"), new TypeReference<>() {});
            hashes.put("control/baseline-fingerprint.json", AutoLlmFileUtil.hash(canonical));
            AutoLlmFileUtil.write(root.resolve("control/canonical-hashes.json"), hashes);
        }
    }
    private void requireEndpoint(String id, String endpoint) {
        for (var entry : current.targets.entrySet()) if (!entry.getKey().equals(id) && entry.getValue().status != TargetStatus.REMOVED && endpoint.equals(entry.getValue().endpoint)) throw new IllegalStateException("Duplicate resolved SSH endpoint: " + entry.getKey());
        current.targets.get(id).endpoint = endpoint;
    }
    private boolean owns(String id, String token) { return current != null && jobs.containsKey(id) && jobs.get(id).token.equals(token); }
    private void dispatch() {
        if (current == null || checkingBaseline || closing) return;
        if (cancelled) { cancelJobs(); return; }
        try { verifyCanonical(current); }
        catch (RuntimeException e) { current.problem = AutoLlmFailureUtil.describe(e, store.directory(current.id)); current.failure = ExceptionUtil.stacktrace(e); cancelled = true; save(); cancelJobs(); return; }
        for (var entry : current.targets.entrySet()) {
            String id = entry.getKey(); var target = entry.getValue();
            if (jobs.containsKey(id) || target.draining || Set.of(TargetStatus.REMOVED, TargetStatus.NEEDS_ATTENTION).contains(target.status)) continue;
            AutoLlmCampaign.Trial trial = null;
            try {
                if (!target.baselineReady) { qualify(id, false, new CompletableFuture<>()); continue; }
                for (var candidate : current.trials) if (candidate.status == TrialStatus.PENDING && id.equals(candidate.targetId)) { trial = candidate; break; }
                if (trial == null) for (var candidate : current.trials) if (candidate.status == TrialStatus.PENDING && candidate.targetId == null) { trial = candidate; break; }
                if (trial != null) launch(id, trial);
            } catch (RuntimeException e) {
                target.status = TargetStatus.NEEDS_ATTENTION; target.problem = AutoLlmFailureUtil.describe(e, store.directory(current.id).resolve(target.directory));
                if (trial != null) { trial.status = TrialStatus.BLOCKED; trial.problem = target.problem; trial.failure = ExceptionUtil.stacktrace(e); }
                save();
            }
        }
        settle();
    }
    private void launch(String id, AutoLlmCampaign.Trial trial) {
        var target = current.targets.get(id);
        if (trial.targetId == null) { trial.targetId = id; trial.targetRevision = current.poolRevision; trial.directory = target.directory + "/trials/%03d".formatted(trial.number); }
        trial.status = TrialStatus.STARTING; target.assignedTrial = trial.number; target.status = TargetStatus.BUSY;
        String token = UUID.randomUUID().toString(); var taskRunner = newRunner(id, token, trial);
        FutureTask<Void> future = new FutureTask<>(() -> {
            Throwable failure = null;
            try { taskRunner.execute(); } catch (Throwable e) { failure = e; }
            var model = taskRunner.snapshot(); var error = failure;
            enqueue(() -> {
                if (!owns(id, token)) return;
                apply(id, model); jobs.remove(id); target.assignedTrial = null;
                var result = current.trials.get(trial.number - 1);
                if (error != null) { result.status = TrialStatus.BLOCKED; result.problem = AutoLlmFailureUtil.describe(error, store.directory(current.id).resolve(target.directory)); }
                boolean outstanding = AutoLlmCampaignStore.hasUncollectedJobs(store.directory(current.id).resolve(target.directory));
                boolean infrastructure = result.problem != null && result.problem.category() == AutoLlmFailureUtil.Category.INFRASTRUCTURE;
                if (outstanding || infrastructure || result.status == TrialStatus.BLOCKED) { target.status = TargetStatus.NEEDS_ATTENTION; target.problem = result.problem; }
                else target.status = target.draining ? TargetStatus.DRAINING : TargetStatus.READY;
                save(); reportAsync(false); dispatch();
            }); return null;
        });
        jobs.put(id, new Running(token, taskRunner, future)); save(); workers.execute(future);
    }
    private void settle() {
        if (!jobs.isEmpty()) { current.status = CampaignStatus.RUNNING; save(); return; }
        boolean pending = false, blocked = false, failed = false;
        for (var t : current.trials) { pending |= t.status == TrialStatus.PENDING; blocked |= t.status.recoverable(); failed |= t.status == TrialStatus.FAILED; }
        if (pending && !blocked) {
            boolean attention = false; for (var t : current.targets.values()) attention |= t.status == TargetStatus.NEEDS_ATTENTION;
            current.status = attention ? CampaignStatus.NEEDS_ATTENTION : CampaignStatus.WAITING_FOR_TARGETS;
        } else current.status = blocked || pending ? CampaignStatus.NEEDS_ATTENTION : failed ? CampaignStatus.COMPLETED_WITH_FAILURES : CampaignStatus.SUCCEEDED;
        save();
        if (current.status != CampaignStatus.WAITING_FOR_TARGETS) reportAsync(true);
    }
    private void reportAsync(boolean releaseAfter) {
        var snapshot = current.copy(); long sequence = ++reportSequence;
        reporting.execute(() -> {
            if (sequence != reportSequence) return;
            buildReport(snapshot);
            enqueue(() -> {
                if (current == null || !current.id.equals(snapshot.id)) return;
                if (sequence == reportSequence) {
                    current.reportFailure = snapshot.reportFailure; current.reportUpdatedAt = snapshot.reportUpdatedAt; current.codexUsage = snapshot.codexUsage;
                    for (int i = 0; i < current.trials.size(); i++) current.trials.get(i).codexUsage = snapshot.trials.get(i).codexUsage;
                    save();
                }
                if (releaseAfter && sequence == reportSequence && jobs.isEmpty() && current.status != CampaignStatus.WAITING_FOR_TARGETS) release();
            });
        });
    }
    private void buildReport(AutoLlmCampaign c) {
        Path root = store.directory(c.id);
        try { CodexEvidenceUtil.report(c, root); reports.report(c, root); c.reportFailure = null; }
        catch (RuntimeException e) { c.reportFailure = ExceptionUtil.stacktrace(e); store.event(c.id, "report-failed", c.reportFailure); }
        c.reportUpdatedAt = Instant.now().toString();
    }
    public AutoLlmCampaign report(String id) {
        return invoke(() -> {
            if (active != null) throw new IllegalStateException("Wait for active work before regenerating reports");
            try (var lock = store.lock()) { var c = store.load(id); buildReport(c); store.save(c); return c; }
        });
    }
    public record TargetCheck(Integer parameters, boolean qualified, boolean withinLimit, String directory, AutoLlmFailureUtil.Failure problem) {}
    public record BaselineCheck(int parameters, int maxParameters, boolean withinLimit, boolean ready,
            List<String> qualifiedTargets, List<String> failedTargets, Map<String,TargetCheck> targets) {}
    public BaselineCheck baselineCheck(String id) {
        var completions = invoke(() -> {
            var c = store.load(id); if (c.status != CampaignStatus.DRAFT) throw new IllegalStateException("Baseline checks require a draft");
            acquire(c); checkingBaseline = true;
            try {
                pin(c); var list = new ArrayList<CompletableFuture<Void>>();
                for (String target : c.targets.keySet()) { var done = new CompletableFuture<Void>(); list.add(done); qualify(target, true, done); }
                return list;
            } catch (RuntimeException e) { checkingBaseline = false; release(); throw e; }
        });
        try { CompletableFuture.allOf(completions.toArray(CompletableFuture[]::new)).join(); }
        finally { invoke(() -> {
            checkingBaseline = false;
            if (cancelled) cancelJobs();
            else { current.status = CampaignStatus.DRAFT; try { save(); } finally { release(); } }
            return null;
        }); }
        var c = store.load(id);
        var targets = new LinkedHashMap<String,TargetCheck>();
        var qualified = new ArrayList<String>(); var failed = new ArrayList<String>();
        int maximum = 0;
        for (var entry : c.targets.entrySet()) {
            var target = entry.getValue();
            Path path = store.directory(id).resolve(target.directory).resolve("baseline-preflight/check/execution/check.json");
            Integer parameters = Files.isRegularFile(path)
                    ? AutoLlmFileUtil.JSON.readTree(read(path)).path("parameters").asInt() : null;
            boolean withinLimit = parameters != null && parameters <= c.config.maxParameters();
            boolean passed = target.problem == null && (target.baselineReady || target.status == TargetStatus.VALIDATED) && withinLimit;
            if (parameters != null) maximum = Math.max(maximum, parameters);
            (passed ? qualified : failed).add(entry.getKey());
            targets.put(entry.getKey(), new TargetCheck(parameters, target.problem == null && (target.baselineReady || target.status == TargetStatus.VALIDATED), withinLimit, path.getParent().toString(), target.problem));
        }
        boolean ready = c.status == CampaignStatus.DRAFT && !qualified.isEmpty() && failed.isEmpty();
        boolean withinLimit = !targets.isEmpty();
        for (var target : targets.values()) withinLimit &= target.withinLimit();
        return new BaselineCheck(maximum, c.config.maxParameters(), withinLimit, ready, List.copyOf(qualified), List.copyOf(failed), Map.copyOf(targets));
    }

    private static String read(Path path) { try { return Files.readString(path); } catch (IOException e) { throw new UncheckedIOException(e); } }
    public AutoLlmTargetProbeService.Probe probeTarget(AutoLlmConfig config) { return targetProbes.start(config); }
    public AutoLlmTargetProbeService.Probe targetProbe(String id) { return targetProbes.load(id); }
    public AutoLlmCampaign addTarget(String id, String targetId, AutoLlmConfig.Server server) {
        return invoke(() -> {
            if (!targetId.equals(server.id())) throw new IllegalArgumentException("Target ID and definition differ");
            var c = id.equals(active) ? current : store.load(id);
            if (c.status.completed()) throw new IllegalStateException("Campaign has completed");
            register(c, server); store.save(c);
            if (c == current && !checkingBaseline && !cancelled) dispatch();
            return c.copy();
        });
    }
    public AutoLlmCampaign drainTarget(String id, String targetId, boolean remove) {
        return invoke(() -> {
            var c = id.equals(active) ? current : store.load(id); var target = requireTarget(c, targetId);
            if (remove && (jobs.containsKey(targetId) && c == current || AutoLlmCampaignStore.hasUncollectedJobs(store.directory(id).resolve(target.directory)))) throw new IllegalStateException("Drain and reconcile the busy target before removal");
            if (remove) for (var trial : c.trials) if (targetId.equals(trial.targetId) && !trial.status.terminal())
                throw new IllegalStateException("An unfinished trial is pinned to this target; reconcile it before removal");
            if (target.status == TargetStatus.REMOVED || target.draining && !remove) return c.copy();
            target.draining = true; target.status = remove ? TargetStatus.REMOVED : TargetStatus.DRAINING; target.revision = ++c.poolRevision;
            store.event(id, remove ? "target-removed" : "target-draining", Map.of("targetId", targetId, "revision", c.poolRevision)); store.save(c);
            if (c == current) dispatch(); return c.copy();
        });
    }
    private AutoLlmCampaign.TargetState requireTarget(AutoLlmCampaign c, String id) {
        var target = c.targets.get(id); if (target == null) throw new NoSuchElementException("Unknown target " + id); return target;
    }
    public AutoLlmCampaign resumeTarget(String id, String targetId) {
        return invoke(() -> {
            if (!id.equals(active)) throw new IllegalStateException("Resume the stopped campaign explicitly before recovering a target");
            requireTarget(current, targetId);
            recoveryInternal(current).targets().get(targetId).resume().requireAllowed();
            return resumeTargetInternal(id, targetId, false);
        });
    }
    public AutoLlmCampaign requalifyTarget(String id, String targetId) {
        return invoke(() -> {
            if (!id.equals(active)) throw new IllegalStateException("Resume the stopped campaign explicitly before recovering a target");
            requireTarget(current, targetId);
            recoveryInternal(current).targets().get(targetId).requalify().requireAllowed();
            return resumeTargetInternal(id, targetId, true);
        });
    }
    private AutoLlmCampaign resumeTargetInternal(String id, String targetId, boolean requalify) {
        var target = requireTarget(current, targetId);
        target.resume(requalify, ++current.poolRevision);
        store.event(id, requalify ? "target-requalification-requested" : "target-resumed", Map.of("targetId", targetId, "revision", current.poolRevision));
        for (var trial : current.trials) if (targetId.equals(trial.targetId) && trial.status.recoverable()) { trial.queueForResume(); }
        save(); dispatch(); return current.copy();
    }
    public AutoLlmRecoveryUtil.Recovery recovery(String id) {
        return invoke(() -> recoveryInternal(id.equals(active) ? current : store.load(id)));
    }
    private AutoLlmRecoveryUtil.Recovery recoveryInternal(AutoLlmCampaign campaign) {
        var busy = new HashSet<String>(); var uncollected = new HashSet<String>();
        for (var entry : campaign.targets.entrySet()) {
            if (campaign == current && jobs.containsKey(entry.getKey())) busy.add(entry.getKey());
            if (!busy.contains(entry.getKey()) && AutoLlmCampaignStore.hasUncollectedJobs(store.directory(campaign.id).resolve(entry.getValue().directory))) uncollected.add(entry.getKey());
        }
        String blocked = closing || campaign == current && (cancelled || checkingBaseline) ? "Controller is shutting down, cancelling, or checking the baseline" : null;
        try { store.requireReservation(campaign.id); } catch (IllegalStateException e) { blocked = e.getMessage(); }
        return AutoLlmRecoveryUtil.describe(campaign, active, busy, uncollected, blocked);
    }
    public AutoLlmCampaign resumeTrial(String id, int number) {
        return invoke(() -> {
            if (!id.equals(active)) throw new IllegalStateException("Resume the stopped campaign first");
            var actions = recoveryInternal(current).trials().get(number);
            if (actions == null) throw new NoSuchElementException("Unknown trial " + number);
            actions.resume().requireAllowed();
            AutoLlmCampaign.Trial trial = null;
            for (var candidate : current.trials) if (candidate.number == number) { trial = candidate; break; }
            var target = requireTarget(current, trial.targetId);
            trial.queueForResume();
            target.resume(false, ++current.poolRevision);
            store.event(id, "trial-resumed-by-operator", Map.of("trial", number, "targetId", trial.targetId));
            save(); dispatch(); return current.copy();
        });
    }
    public AutoLlmCampaign failTrial(String id, int number, String reason) {
        if (reason == null || reason.isBlank()) throw new IllegalArgumentException("A reason is required to fail a trial");
        return invoke(() -> {
            if (id.equals(active)) return failTrial(current, number, reason);
            if (active != null) throw new IllegalStateException("Another campaign is active");
            store.requireReservation(id);
            try (var lock = store.lock()) { return failTrial(store.load(id), number, reason); }
        });
    }
    private AutoLlmCampaign failTrial(AutoLlmCampaign c, int number, String reason) {
        AutoLlmCampaign.Trial trial = null;
        for (var candidate : c.trials) if (candidate.number == number) { trial = candidate; break; }
        if (trial == null) throw new NoSuchElementException("Unknown trial " + number);
        if (trial.status == TrialStatus.FAILED) return c.copy();
        recoveryInternal(c).trials().get(number).fail().requireAllowed();
        var target = requireTarget(c, trial.targetId);
        trial.status = TrialStatus.FAILED;
        target.assignedTrial = null; target.problem = null; target.status = target.draining ? TargetStatus.DRAINING : TargetStatus.READY;
        store.event(c.id, "trial-failed-by-operator", Map.of("trial", number, "targetId", trial.targetId, "reason", reason.strip()));
        if (c == current) { save(); reportAsync(false); dispatch(); }
        else store.save(c);
        return c.copy();
    }
    public AutoLlmCampaign cancel(String id) {
        return invoke(() -> {
            if (!id.equals(active)) acquire(store.load(id));
            cancelled = true; current.status = CampaignStatus.CANCELLATION_PENDING; save(); cancelJobs(); return current.copy();
        });
    }
    private void cancelJobs() {
        current.status = CampaignStatus.CANCELLATION_PENDING; save();
        if (!jobs.isEmpty()) return;
        boolean queued = false;
        for (var entry : current.targets.entrySet()) {
            String id = entry.getKey(); var target = entry.getValue(); Path root = store.directory(current.id).resolve(target.directory);
            if (!AutoLlmCampaignStore.hasUncollectedJobs(root)) continue;
            queued = true; String token = UUID.randomUUID().toString(); var taskRunner = newRunner(id, token, null);
            FutureTask<Void> future = new FutureTask<>(() -> {
                Throwable error = null; try { taskRunner.cancelOutstanding(); } catch (Throwable e) { error = e; }
                var failure = error; var snapshot = taskRunner.snapshot();
                enqueue(() -> { if (!owns(id, token)) return; apply(id, snapshot); jobs.remove(id); if (failure != null) { target.status = TargetStatus.NEEDS_ATTENTION; target.problem = AutoLlmFailureUtil.describe(failure, root); current.problem = target.problem; } if (jobs.isEmpty()) finishCancellation(); }); return null;
            });
            jobs.put(id, new Running(token, taskRunner, future)); workers.execute(future);
        }
        if (!queued) finishCancellation();
    }
    private void finishCancellation() {
        current.status = AutoLlmCampaignStore.hasUncollectedJobs(store.directory(current.id)) ? CampaignStatus.NEEDS_ATTENTION : current.problem == null ? CampaignStatus.CANCELLED : CampaignStatus.NEEDS_ATTENTION;
        save(); reportAsync(true);
    }
    public Object metadataCheck(String id, int number, String blacklist) {
        var taskRunner = invoke(() -> {
            if (!id.equals(active)) throw new IllegalStateException("Campaign is not active");
            for (var t : current.trials) if (t.number == number && t.targetId != null && jobs.containsKey(t.targetId)) return jobs.get(t.targetId).runner;
            throw new IllegalStateException("Trial is not running");
        });
        return taskRunner.metadataCheck(id, number, blacklist);
    }
    public void recover() {
        invoke(() -> {
            try (var lock = store.lock()) {
                for (var c : store.list()) if (c.status.inProgress()) {
                    c.status = CampaignStatus.INTERRUPTED;
                    for (var t : c.trials) if (t.status.inProgress()) t.status = TrialStatus.INTERRUPTED;
                    store.save(c);
                }
            } catch (IllegalStateException ignored) { /* Another controller owns this workspace. */ }
            return null;
        });
    }
    @PreDestroy public void close() {
        closing = true;
        try {
            coordinator.submit(() -> {
                if (current != null) {
                    current.status = CampaignStatus.INTERRUPTED;
                    for (var trial : current.trials) if (trial.status.inProgress()) trial.status = TrialStatus.INTERRUPTED;
                    save();
                }
            }).get(5, TimeUnit.SECONDS);
        } catch (Exception ignored) { /* OS ownership is released when the controller exits. */ }
        workers.shutdownNow(); reporting.shutdown(); coordinator.shutdown();
        var lock = ownership; ownership = null;
        Runnable relinquish = () -> {
            try { workers.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS); reporting.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); return; }
            if (lock != null) lock.detach();
        };
        try {
            if (workers.awaitTermination(5, TimeUnit.SECONDS) && reporting.awaitTermination(5, TimeUnit.SECONDS)) relinquish.run();
            else Thread.ofVirtual().name("autollm-shutdown").start(relinquish);
        } catch (InterruptedException e) { Thread.currentThread().interrupt(); Thread.ofVirtual().name("autollm-shutdown").start(relinquish); }
        targetProbes.close();
    }
}
