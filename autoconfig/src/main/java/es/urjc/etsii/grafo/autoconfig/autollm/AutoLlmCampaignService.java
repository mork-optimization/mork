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
        return AutoLlmNormalization.normalize(defaults.withPaths(defaults.trainingPath() == null ? instances.getPath("irace") : defaults.trainingPath(),
                defaults.comparisonPath() == null ? instances.getPath("autollm-comparison") : defaults.comparisonPath()));
    }
    public List<String> validate(AutoLlmConfig c) {
        c = AutoLlmNormalization.normalize(c);
        var errors = new ArrayList<String>();
        if (c.trials() < 1 || c.trials() > 10000) errors.add("Trials must be between 1 and 10000");
        if (c.maxParameters() < 1 || c.maxParameters() >= 300) errors.add("Parameter cap must be between 1 and 299");
        if (c.preparation().maxRepairs() < 0 || c.preparation().maxRepairs() > 100) errors.add("Repair limit must be between 0 and 100");
        if (c.preparation().probeDurationMillis() <= 0 || c.preparation().pilotDurationMillis() <= 0 || c.tuning().evaluations() <= 0 || c.comparison().repetitions() <= 0) errors.add("Budgets and repetitions must be positive");
        if (c.tuning().ignoreInitialMillis() < 0 || c.tuning().intervalDurationMillis() <= 0 || c.comparison().timeLimitMillis() < 0) errors.add("Invalid algorithm time horizon");
        if (c.codex().maxConcurrentSessions() < 1) errors.add("Concurrent Codex sessions must be positive");
        errors.addAll(AutoLlmExecutionService.validate(c));
        if (!AutoLlmNormalization.localPath(Path.of(c.workspace())).equals(store.root())) errors.add("Workspace is fixed by backend configuration");
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
        if (AutoLlmCleanup.requested(store.root())) throw new IllegalStateException("Workspace cleanup is pending");
        requested = AutoLlmNormalization.normalize(requested);
        var errors = validate(requested); if (!errors.isEmpty()) throw new IllegalArgumentException(String.join("; ", errors));
        var c = new AutoLlmCampaign(); c.id = UUID.randomUUID().toString(); c.createdAt = Instant.now().toString();
        Path directory = store.directory(c.id); Path repo = Path.of(requested.project().repository()).toAbsolutePath().normalize();
        var codec = new ComponentSpecJsonCodec();
        try {
            var sota = codec.parse(Files.readString(resolve(repo, requested.sotaFile())));
            var seeds = new LinkedHashSet<ComponentSpec>(); seeds.add(sota);
            if (requested.initialConfigurationsFile() != null && !requested.initialConfigurationsFile().isBlank()) seeds.addAll(codec.parseList(Files.readString(resolve(repo, requested.initialConfigurationsFile()))));
            new OwnershipManifest(store.root()).directory(store.root(), "workspace", store.root().toString());
            new OwnershipManifest(store.root()).directory(directory, "campaign", directory.toString());
            Files.createDirectories(directory.resolve("control"));
            AutoLlmFileUtil.write(directory.resolve("control/storage-layout.json"), Map.of("version", 1, "sources", "commit-and-patch"));
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
                var t = new AutoLlmCampaign.Trial(); t.number = i; t.directory = "trials/%03d".formatted(i);
                do { t.pilotSeed = random.nextLong(1, Integer.MAX_VALUE); } while (!assigned.add(t.pilotSeed));
                do { t.tuningSeed = random.nextLong(1, Integer.MAX_VALUE); } while (!assigned.add(t.tuningSeed));
                t.comparisonSeed = comparisonSeed; c.trials.add(t);
            }
            c.localPreparation.directory = "preparation";
            c.localPreparation.definition = new AutoLlmConfig.Server("preparation", null, null, null, null, null, null, null);
            Path proposal = resolve(repo, requested.trainingPath()).getParent();
            if (Files.isRegularFile(proposal.resolve("proposal.json")) && Files.isRegularFile(proposal.getParent().resolve("remote-screening.json")))
                c.screeningPath = proposal.getParent().toString();
            if (c.screeningPath != null) {
                Path dependency = Path.of(c.screeningPath);
                new OwnershipManifest(store.root()).screeningDependency(dependency, directory);
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
    private Semaphore localBuilds;
    private static final String PREPARATION_TARGET = "$preparation";
    private AutoLlmCampaign.TargetState target(AutoLlmCampaign c, String id) { return PREPARATION_TARGET.equals(id) ? c.localPreparation : c.targets.get(id); }
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
        server = AutoLlmNormalization.normalize(c.config.forServer(server)).execution().ssh().onlyServer();
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
        c.targets.put(server.id(), target); c.reviewedTargets.add(server.id());
        store.event(c.id, "target-registered", Map.of("targetId", server.id(), "revision", c.poolRevision));
    }
    private void registerInitial(AutoLlmCampaign c) {
        if (c.config.execution().target() == AutoLlmConfig.Target.SSH) {
            for (var server : c.config.execution().ssh().servers()) register(c, server);
        } else {
            var target = new AutoLlmCampaign.TargetState();
            target.definition = new AutoLlmConfig.Server("local", null, null, null, null, null, null, null);
            target.directory = "targets/local"; target.revision = ++c.poolRevision; c.targets.put("local", target); c.reviewedTargets.add("local");
        }
    }
    private void acquire(AutoLlmCampaign c) {
        if (active != null) throw new IllegalStateException("A campaign is already active");
        ownership = store.acquire(c.id, store.directory(c.id)); current = c; active = c.id; cancelled = false;
        sessions = new Semaphore(c.config.codex().maxConcurrentSessions(), true);
        localBuilds = new Semaphore(c.config.preparation().localBuilds(), true); reportedAttempts.clear();
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
            if (c.config.execution().target() == AutoLlmConfig.Target.SSH || c.screeningPath != null) {
                var screeningConfig = c.config;
                if (c.screeningPath != null) {
                    Path proposal = Path.of(c.screeningPath).resolve("proposal");
                    screeningConfig = c.config.withPaths(proposal.resolve("proposed-train.index").toString(), proposal.resolve("proposed-test.index").toString());
                }
                var prepared = es.urjc.etsii.grafo.autoconfig.screening.RemoteScreeningService.screened(screeningConfig);
                if (!prepared.commit().equals(commit)) throw new IllegalStateException("Baseline differs from approved screening");
                if (c.screeningPath == null) c.screeningPath = Path.of(c.config.trainingPath()).getParent().getParent().toString();
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
    public record GenerationSettings(String model, String reasoningEffort, boolean ready, String approvalLine) {
        public static GenerationSettings from(AutoLlmConfig config) {
            String model = AutoLlmNormalization.optional(config.codex().model());
            String effort = AutoLlmNormalization.optional(config.codex().reasoningEffort());
            String line = "Generation and repairs: " + (model == null ? "model NOT SELECTED; set codex.model before approval" : "model " + model + " (campaign setting)")
                    + "; reasoning effort " + (effort == null ? "Codex default (not pinned)" : effort + " (campaign setting)")
                    + ". Provider comes from Codex configuration; resumed sessions retain their observed provider. No automatic model fallback.";
            return new GenerationSettings(model, effort, model != null, line);
        }
        public void requireReady() {
            if (!ready) throw new IllegalStateException("Set codex.model explicitly and review the revised proposal before approval or launch; the Codex CLI default is not pinned by the campaign.");
        }
    }
    public record Proposal(String digest, AutoLlmConfig configuration, List<String> training, List<String> testing,
                           Object screening, Map<String,Object> targets, String approvedDigest, AutoLlmStorageEstimate.Estimate storage, GenerationSettings generation) {}
    private Proposal proposalInternal(AutoLlmCampaign c) {
        if (c.baselineCommit == null) throw new IllegalStateException("Run the baseline check before reviewing the campaign");
        verifyCanonical(c);
        var training = es.urjc.etsii.grafo.util.InstancePathUtil.list(c.config.trainingPath());
        var testing = es.urjc.etsii.grafo.util.InstancePathUtil.list(c.config.comparisonPath());
        if (!new HashSet<>(testing).containsAll(training)) throw new IllegalStateException("Testing must include training");
        var targets = new TreeMap<String,Object>();
        for (var e : c.targets.entrySet()) if (e.getValue().status != TargetStatus.REMOVED) {
            var t = e.getValue();
            targets.put(e.getKey(), Map.of("definition", t.definition, "selected", c.reviewedTargets.contains(e.getKey()),
                    "ready", t.baselineReady || t.status == TargetStatus.VALIDATED, "draining", t.draining));
        }
        Object screening = Map.of();
        if (c.screeningPath != null) {
            Path source = Path.of(c.screeningPath);
            var original = es.urjc.etsii.grafo.util.InstancePathUtil.list(source.resolve("control/original.index").toString());
            var preparation = AutoLlmFileUtil.read(source.resolve("remote-screening.json"), es.urjc.etsii.grafo.autoconfig.screening.RemoteScreeningService.Preparation.class);
            var localPaths = AutoLlmExecutionService.localPaths(preparation.config(), source);
            var proposal = AutoLlmFileUtil.read(source.resolve("proposal/proposal.json"), es.urjc.etsii.grafo.autoconfig.screening.ScreeningData.Proposal.class);
            var decisions = new ArrayList<Object>(); var excluded = new TreeMap<String,String>();
            for (var d : proposal.instances()) {
                String path = localPaths.apply(d.instance());
                decisions.add(new es.urjc.etsii.grafo.autoconfig.screening.ScreeningData.Decision(path, d.classification(), d.bestObserved(), d.firstPassTtbNanos(), d.confirmations(), d.fastHits(), d.failures(), d.reason()));
                if (!testing.contains(path)) excluded.put(path, d.reason());
            }
            screening = Map.of("originalPool", original, "decisions", decisions, "excluded", excluded,
                    "proposalSha256", AutoLlmFileUtil.hash(source.resolve("proposal/proposal.json")));
        }
        Path selection = store.directory(c.id).resolve("control/dataset-decisions.json");
        if (Files.isRegularFile(selection)) screening = Map.of("screening", screening, "selection", AutoLlmFileUtil.read(selection, Object.class));
        var evidence = new TreeMap<String,Object>();
        var scope = new TreeMap<String,Object>();
        for (var e : c.targets.entrySet()) if (e.getValue().status != TargetStatus.REMOVED) scope.put(e.getKey(), Map.of("definition", e.getValue().definition, "selected", c.reviewedTargets.contains(e.getKey()), "artifactSha256", e.getValue().baselineArtifactSha256 == null ? "" : e.getValue().baselineArtifactSha256, "allocation", e.getValue().host == null ? Map.of() : Map.of("identity", e.getValue().host.identity(), "workers", e.getValue().host.effectiveWorkers())));
        var proposedConfig = AutoLlmNormalization.normalize(c.proposedTrials == null ? c.config : c.config.withTrials(c.proposedTrials));
        evidence.put("configuration", proposedConfig); evidence.put("targets", scope); evidence.put("screening", screening);
        evidence.put("canonical", AutoLlmFileUtil.read(store.directory(c.id).resolve("control/canonical-hashes.json"), Object.class));
        evidence.put("prompt", c.prompt); evidence.put("training", training); evidence.put("testing", testing);
        evidence.put("source", c.baselineCommit);
        Path seeds = store.directory(c.id).resolve("control/seeds.json");
        if (Files.isRegularFile(seeds)) evidence.put("seeds", AutoLlmFileUtil.read(seeds, Object.class));
        String digest = AutoLlmFileUtil.canonicalHash(evidence);
        Path snapshot = store.directory(c.id).resolve("control/proposals/" + digest + ".json");
        if (!Files.exists(snapshot)) AutoLlmFileUtil.write(snapshot, evidence);
        var proposal = new Proposal(digest, proposedConfig, training, testing, screening, targets, c.approvedProposal, AutoLlmStorageEstimate.estimate(c, store.directory(c.id), proposedConfig), GenerationSettings.from(proposedConfig));
        AutoLlmFileUtil.write(store.directory(c.id).resolve("control/campaign-proposal.json"), proposal);
        return proposal;
    }
    public Proposal proposal(String id) { return invoke(() -> proposalInternal(id.equals(active) ? current : store.load(id))); }
    public Proposal amendTrials(String id, int count) {
        return invoke(() -> {
            if (!id.equals(active)) throw new IllegalStateException("Resume the campaign before proposing an amendment");
            if (current.status.completed()) throw new IllegalStateException("Completed campaigns cannot be extended after storage retirement; create a new campaign");
            if (count < current.trials.size() || count > 10000) throw new IllegalArgumentException("An amendment can only increase the trial count, up to 10000");
            current.proposedTrials = count; save(); return proposalInternal(current);
        });
    }
    private void applyTrialAmendment(AutoLlmCampaign c) {
        if (c.proposedTrials == null) return;
        var random = new SplittableRandom(c.config.seed()); long comparisonSeed = random.nextLong(1, Integer.MAX_VALUE);
        var used = new HashSet<Long>(); used.add(comparisonSeed);
        for (int number = 1; number <= c.proposedTrials; number++) {
            long pilot, tuning;
            do { pilot = random.nextLong(1, Integer.MAX_VALUE); } while (!used.add(pilot));
            do { tuning = random.nextLong(1, Integer.MAX_VALUE); } while (!used.add(tuning));
            if (number <= c.trials.size()) continue;
            var trial = new AutoLlmCampaign.Trial(); trial.number = number; trial.directory = "trials/%03d".formatted(number);
            trial.pilotSeed = pilot; trial.tuningSeed = tuning; trial.comparisonSeed = comparisonSeed; c.trials.add(trial);
        }
        c.config = c.config.withTrials(c.proposedTrials); c.proposedTrials = null;
        AutoLlmFileUtil.write(store.directory(c.id).resolve("control/config.json"), c.config);
    }
    public Proposal selectProposal(String id, Set<String> selected) {
        return invoke(() -> {
            var c = id.equals(active) ? current : store.load(id);
            if (c.status != CampaignStatus.DRAFT && !id.equals(active)) throw new IllegalStateException("Review the stopped campaign through an explicit amendment");
            for (String target : selected) requireTarget(c, target);
            c.reviewedTargets.clear(); c.reviewedTargets.addAll(selected.isEmpty() ? c.targets.keySet() : selected);
            store.save(c); return proposalInternal(c);
        });
    }
    public Proposal approve(String id, String digest) {
        return invoke(() -> {
            var c = id.equals(active) ? current : store.load(id);
            if (c.status != CampaignStatus.DRAFT && !id.equals(active)) throw new IllegalStateException("Campaign approval requires an idle draft or an active amendment");
            if (digest == null || !digest.matches("[a-f0-9]{64}") || !Files.isRegularFile(store.directory(id).resolve("control/proposals/" + digest + ".json")))
                throw new AutoLlmNormalization.ApprovalMismatch("UNKNOWN_PROPOSAL_DIGEST", List.of());
            var p = proposalInternal(c);
            if (!p.digest().equals(digest)) throw new AutoLlmNormalization.ApprovalMismatch("PROPOSAL_CHANGED", AutoLlmNormalization.diff(
                    AutoLlmFileUtil.read(store.directory(id).resolve("control/proposals/" + digest + ".json"), Object.class),
                    AutoLlmFileUtil.read(store.directory(id).resolve("control/proposals/" + p.digest() + ".json"), Object.class)));
            p.generation().requireReady();
            if (c.config.execution().target() == AutoLlmConfig.Target.SSH && !c.localPreparation.baselineReady) throw new IllegalStateException("Local preparation prerequisites are not qualified; inspect the baseline check");
            if (c.reviewedTargets.isEmpty()) throw new IllegalStateException("Select at least one server");
            for (String target : c.reviewedTargets) {
                var t = requireTarget(c, target);
                if (!t.baselineReady || t.problem != null) throw new IllegalStateException("Selected server is not qualified: " + target);
            }
            applyTrialAmendment(c);
            c.approvedTargets.clear(); c.approvedTargets.addAll(c.reviewedTargets);
            c.approvedProposal = digest; c.approvedAt = Instant.now().toString();
            store.event(id, "campaign-approved", p); store.save(c); if (c == current) dispatch(); return proposalInternal(c);
        });
    }
    public record DatasetSelection(List<String> training, List<String> testing, Map<String,String> excluded) {}
    public AutoLlmCampaign reviseDataset(String id, DatasetSelection selection) {
        return invoke(() -> {
            if (active != null) throw new IllegalStateException("Wait for active operations before editing a draft");
            var old = store.load(id);
            if (old.status != CampaignStatus.DRAFT) throw new IllegalStateException("Only draft datasets can be edited");
            if (selection.training() == null || selection.testing() == null || selection.excluded() == null) throw new IllegalArgumentException("Provide training, testing and exclusions with reasons");
            var train = new LinkedHashSet<>(selection.training()); var test = new LinkedHashSet<>(selection.testing());
            if (train.isEmpty() || test.isEmpty() || train.size() != selection.training().size() || test.size() != selection.testing().size() || !test.containsAll(train))
                throw new IllegalArgumentException("Training/testing must be nonempty, contain no duplicates, and testing must include training");
            Path root = store.directory(id);
            Path originalPool = root.resolve("control/original-pool.json");
            var original = new LinkedHashSet<>(Files.isRegularFile(originalPool)
                    ? AutoLlmFileUtil.read(originalPool, new TypeReference<List<String>>() {})
                    : es.urjc.etsii.grafo.util.InstancePathUtil.list(old.screeningPath == null ? old.config.comparisonPath() : Path.of(old.screeningPath).resolve("control/original.index").toString()));
            var accounted = new HashSet<>(test);
            for (var e : selection.excluded().entrySet()) {
                if (e.getValue() == null || e.getValue().isBlank() || !accounted.add(e.getKey())) throw new IllegalArgumentException("Every excluded input needs a reason and cannot also be in testing");
            }
            if (!accounted.equals(original)) throw new IllegalArgumentException("Testing plus exclusions must account for the complete original input pool");
            try {
                Path revision = root.resolve("dataset-revisions/" + UUID.randomUUID()); Files.createDirectories(revision);
                Files.write(revision.resolve("training.index"), train); Files.write(revision.resolve("testing.index"), test);
                var replacement = createInternal(old.config.withPaths(revision.resolve("training.index").toString(), revision.resolve("testing.index").toString()));
                replacement.screeningPath = old.screeningPath;
                AutoLlmInputUtil.copySettings(root, store.directory(replacement.id));
                if (old.screeningPath != null) {
                    Map<String,Map<String,String>> originalInputs = AutoLlmFileUtil.read(Path.of(old.screeningPath).resolve("data/original-inputs.json"), new TypeReference<>() {});
                    var aliases = new LinkedHashMap<String,String>();
                    for (var e : originalInputs.entrySet()) if (test.contains(e.getValue().get("path"))) aliases.put(e.getKey(), e.getValue().get("path"));
                    AutoLlmFileUtil.write(store.directory(replacement.id).resolve("control/input-aliases.json"), aliases);
                }
                AutoLlmFileUtil.write(store.directory(replacement.id).resolve("control/dataset-decisions.json"), selection);
                AutoLlmFileUtil.write(store.directory(replacement.id).resolve("control/original-pool.json"), original);
                store.save(replacement); store.event(id, "dataset-revised", Map.of("campaignId", replacement.id)); return replacement;
            } catch (IOException e) { throw new UncheckedIOException(e); }
        });
    }
    public AutoLlmCampaign editDraft(String id, AutoLlmConfig configuration) {
        return invoke(() -> {
            if (active != null) throw new IllegalStateException("Wait for active operations before editing the draft");
            var old = store.load(id);
            if (old.status != CampaignStatus.DRAFT) throw new IllegalStateException("Only drafts can be edited");
            var errors = validate(configuration);
            if (!errors.isEmpty()) throw new IllegalArgumentException(String.join("; ", errors));
            var previous = old.config;
            boolean sameInputs = AutoLlmPoolUtil.inputIdentities(previous.trainingPath()).equals(AutoLlmPoolUtil.inputIdentities(configuration.trainingPath()))
                    && AutoLlmPoolUtil.inputIdentities(previous.comparisonPath()).equals(AutoLlmPoolUtil.inputIdentities(configuration.comparisonPath()));
            if (!sameInputs && old.screeningPath != null) throw new IllegalArgumentException("Edit the dataset with explicit exclusions before changing the screened instance selection");
            boolean reusable = sameInputs && previous.project().buildCommand().equals(configuration.project().buildCommand())
                    && previous.project().repository().equals(Path.of(configuration.project().repository()).toAbsolutePath().normalize().toString())
                    && previous.project().runArguments().equals(configuration.project().runArguments())
                    && previous.project().artifact().equals(configuration.project().artifact())
                    && previous.project().ref().equals(configuration.project().ref())
                    && previous.maxParameters().equals(configuration.maxParameters()) && previous.comparison().equals(configuration.comparison())
                    && previous.codex().equals(configuration.codex()) && previous.preparation().equals(configuration.preparation()) && previous.tuning().equals(configuration.tuning())
                    && AutoLlmFileUtil.hash(Path.of(previous.sotaFile())).equals(AutoLlmFileUtil.hash(resolve(Path.of(configuration.project().repository()), configuration.sotaFile())))
                    && previous.execution().equals(configuration.execution())
                    && (configuration.initialConfigurationsFile() == null || AutoLlmFileUtil.hash(Path.of(previous.initialConfigurationsFile())).equals(AutoLlmFileUtil.hash(resolve(Path.of(configuration.project().repository()), configuration.initialConfigurationsFile()))));
            if (!reusable) {
                var replacement = createInternal(configuration);
                if (sameInputs) try {
                    replacement.screeningPath = old.screeningPath;
                    Path source = store.directory(id), destination = store.directory(replacement.id);
                    AutoLlmInputUtil.copySettings(source, destination);
                    for (String file : List.of("dataset-decisions.json", "original-pool.json")) if (Files.isRegularFile(source.resolve("control/" + file)))
                        Files.copy(source.resolve("control/" + file), destination.resolve("control/" + file), StandardCopyOption.REPLACE_EXISTING);
                    store.save(replacement);
                } catch (IOException e) { throw new UncheckedIOException(e); }
                store.event(id, "superseded-by-draft", Map.of("campaignId", replacement.id)); return replacement;
            }
            store.event(id, "draft-revised", proposalInternal(old));
            old.approvedProposal = null; old.approvedAt = null;
            old.config = new AutoLlmConfig(previous.project(), previous.workspace(), configuration.trials(), configuration.seed(), previous.maxParameters(),
                    previous.sotaFile(), previous.initialConfigurationsFile(), previous.trainingPath(), previous.comparisonPath(), previous.codex(), previous.preparation(), previous.tuning(), previous.comparison(), previous.execution());
            old.trials.clear(); var random = new SplittableRandom(configuration.seed()); long comparisonSeed = random.nextLong(1, Integer.MAX_VALUE);
            var seeds = new HashSet<Long>(); seeds.add(comparisonSeed);
            for (int i = 1; i <= configuration.trials(); i++) {
                var t = new AutoLlmCampaign.Trial(); t.number = i; t.directory = "trials/%03d".formatted(i);
                do { t.pilotSeed = random.nextLong(1, Integer.MAX_VALUE); } while (!seeds.add(t.pilotSeed));
                do { t.tuningSeed = random.nextLong(1, Integer.MAX_VALUE); } while (!seeds.add(t.tuningSeed));
                t.comparisonSeed = comparisonSeed; old.trials.add(t);
            }
            AutoLlmFileUtil.write(store.directory(id).resolve("control/config.json"), old.config); store.save(old); return old.copy();
        });
    }
    public AutoLlmCampaign start(String id, boolean resume, Set<String> selectedTargets) {
        return invoke(() -> {
            var c = store.load(id);
            if (!resume && c.status != CampaignStatus.DRAFT) throw new IllegalStateException("Only draft campaigns can start");
            if (resume) recoveryInternal(c).resumeCampaign().requireAllowed();
            if (resume && !selectedTargets.isEmpty()) throw new IllegalArgumentException("Target selection is only supported when starting a draft");
            if (!resume) {
                if (!selectedTargets.isEmpty() && !selectedTargets.equals(c.approvedTargets)) throw new IllegalStateException("Target selection differs from approved proposal");
                for (String targetId : c.approvedTargets) {
                    var target = requireTarget(c, targetId);
                    if (target.problem != null || !target.baselineReady)
                        throw new IllegalStateException("Selected target must pass the baseline check: " + targetId);
                }
            }
            var approved = proposalInternal(c);
            approved.generation().requireReady();
            if (c.approvedProposal == null || !c.approvedProposal.equals(approved.digest()))
                throw new IllegalStateException("Review and approve the current campaign proposal before launch/resume");
            if (!resume) for (var e : c.targets.entrySet()) e.getValue().draining = !c.approvedTargets.contains(e.getKey());
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
        var target = target(c, targetId); Path root = store.directory(c.id).resolve(target.directory);
        AutoLlmFileUtil.write(root.resolve("execution-context.json"), AutoLlmPoolUtil.identity(c.id, targetId));
        var model = new AutoLlmExecutionContext(c.id, target.directory, store.directory(c.id), c.prompt);
        model.screeningPath = c.screeningPath;
        model.config = target.resolvedConfig != null ? target.resolvedConfig : PREPARATION_TARGET.equals(targetId) ? c.config.localPreparation() : c.config.execution().target() == AutoLlmConfig.Target.LOCAL ? c.config : c.config.forServer(target.definition);
        model.baselineSource = root.resolve("baseline/source");
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
        if (trial != null) {
            if (trial.assignment() != null && Files.isRegularFile(store.directory(c.id).resolve(trial.assignment().directory).resolve("control/assignment-pinned.json"))) trial.assignment().pinned = true;
            model.trial = trial.copy();
            Path trialRoot = store.directory(c.id).resolve(PREPARATION_TARGET.equals(targetId) ? trial.directory : trial.executionDirectory());
            if (!PREPARATION_TARGET.equals(targetId) && trial.assignment() != null && trial.assignment().config != null) {
                model.config = trial.assignment().config;
                model.executionHost = trial.assignment().host;
                return model;
            }
            if (AutoLlmCampaignStore.hasUncollectedJobs(trialRoot)) {
                model.config = AutoLlmFileUtil.read(trialRoot.resolve("control/config.json"), AutoLlmConfig.class);
                return model;
            }
            try {
                Files.createDirectories(trialRoot.resolve("control"));
                for (String file : List.of("sota.json", "seeds.json")) Files.copy(root.resolve("control/" + file), trialRoot.resolve("control/" + file), StandardCopyOption.REPLACE_EXISTING);
                AutoLlmInputUtil.copySettings(root, trialRoot);
                var cfg = model.config;
                String train = AutoLlmInputUtil.snapshot(trialRoot, "training", es.urjc.etsii.grafo.util.InstancePathUtil.list(cfg.trainingPath()));
                String test = AutoLlmInputUtil.snapshot(trialRoot, "comparison", es.urjc.etsii.grafo.util.InstancePathUtil.list(cfg.comparisonPath()));
                model.config = new AutoLlmConfig(cfg.project(), cfg.workspace(), cfg.trials(), cfg.seed(), cfg.maxParameters(), trialRoot.resolve("control/sota.json").toString(), trialRoot.resolve("control/seeds.json").toString(), train, test, cfg.codex(), cfg.preparation(), cfg.tuning(), cfg.comparison(), cfg.execution());
                if (!PREPARATION_TARGET.equals(targetId)) model.config = new AutoLlmExecutionService(processes).stageInputs(model.config, trialRoot);
                Files.copy(root.resolve("control/comparison-time-limits.json"), trialRoot.resolve("control/comparison-time-limits.json"), StandardCopyOption.REPLACE_EXISTING);
                Files.createDirectories(trialRoot.resolve("baseline"));
                Files.copy(root.resolve("baseline/components.json"), trialRoot.resolve("baseline/components.json"), StandardCopyOption.REPLACE_EXISTING);
                Path originalJar = root.resolve("baseline/source").resolve(cfg.project().artifact());
                if (Files.exists(model.campaignRoot.resolve("control/storage-layout.json"))) {
                    Path asset = CampaignAssetStore.putArtifact(model.campaignRoot, trialRoot.resolve("baseline"), originalJar);
                    AutoLlmFileUtil.write(trialRoot.resolve("baseline/artifact.json"), Map.of("asset", model.relative(asset).replace('\\', '/'), "sha256", AutoLlmFileUtil.hash(asset)));
                } else {
                    Path jar = trialRoot.resolve("baseline/source").resolve(cfg.project().artifact()); Files.createDirectories(jar.getParent());
                    Files.copy(originalJar, jar, StandardCopyOption.REPLACE_EXISTING);
                }
                var hashes = new TreeMap<String,String>();
                for (String file : List.of("sota.json", "seeds.json", "comparison-time-limits.json")) hashes.put("control/" + file, AutoLlmFileUtil.hash(trialRoot.resolve("control/" + file)));
                AutoLlmInputUtil.addSettingsHashes(hashes, trialRoot);
                for (String name : List.of("training", "comparison")) AutoLlmInputUtil.addHashes(hashes, trialRoot, name);
                AutoLlmFileUtil.write(trialRoot.resolve("control/frozen-hashes.json"), hashes);
                AutoLlmFileUtil.write(trialRoot.resolve("control/config.json"), model.config);
                var identity = new LinkedHashMap<>(AutoLlmPoolUtil.identity(c.id, targetId)); identity.put("trial", trial.number);
                if (!PREPARATION_TARGET.equals(targetId) && trial.assignment() != null) identity.put("assignment", trial.assignment().id);
                AutoLlmFileUtil.write(trialRoot.resolve("execution-context.json"), identity);
                if (!PREPARATION_TARGET.equals(targetId) && trial.assignment() != null) {
                    trial.assignment().config = model.config; trial.assignment().host = target.host;
                    if (target.host != null) AutoLlmFileUtil.write(trialRoot.resolve("execution-target.json"), target.host);
                    model.trial = trial.copy();
                }
            } catch (IOException e) { throw new UncheckedIOException(e); }
        }
        return model;
    }
    protected AutoLlmTrialRunner runner(AutoLlmExecutionContext model, Path root, AutoLlmTrialRunner.Listener listener, java.util.function.BooleanSupplier stop, Semaphore permits) {
        return new AutoLlmTrialRunner(model, root, processes, codex, listener, stop, permits);
    }
    private AutoLlmTrialRunner newRunner(String targetId, String token, AutoLlmCampaign.Trial trial) {
        var c = current; var target = target(c, targetId);
        var taskRunner = runner(executionModel(c, targetId, trial), store.directory(c.id).resolve(trial == null ? target.directory : trial.executionDirectory()), new AutoLlmTrialRunner.Listener() {
            public void state(AutoLlmExecutionContext.Snapshot snapshot) { enqueue(() -> update(targetId, token, snapshot)); }
            public void event(String kind, Object detail) { enqueue(() -> { if (current != null && jobs.containsKey(targetId) && jobs.get(targetId).token.equals(token)) store.event(c.id, kind, Map.of("targetId", targetId, "detail", detail)); }); }
        }, () -> cancelled, sessions);
        taskRunner.localBuildPermits(localBuilds);
        return taskRunner;
    }
    private void update(String targetId, String token, AutoLlmExecutionContext.Snapshot model) {
        var running = jobs.get(targetId); if (running == null || !running.token.equals(token) || current == null) return;
        apply(targetId, model); save();
        if (model.trial() != null) for (var attempt : model.trial().attempts) {
            if (attempt.codexEvidence != null && attempt.status != AttemptStatus.RUNNING && reportedAttempts.add(targetId + "/" + model.trial().number + "/" + attempt.number)) reportAsync(false);
        }
    }
    private void apply(String targetId, AutoLlmExecutionContext.Snapshot model) {
        var target = target(current, targetId);
        if (model.trial() == null) target.resolvedConfig = model.config(); target.host = model.executionHost(); target.versions = model.versions();
        target.remote = AutoLlmPoolUtil.remote(model.remote(), model.trial() == null ? target.directory : model.trial().executionDirectory());
        if (model.trial() != null) {
            var copy = model.trial().copy();
            for (int i = 0; i < current.trials.size(); i++) if (current.trials.get(i).number == copy.number) current.trials.set(i, copy);
        }
    }
    private void qualify(String targetId, boolean metadataOnly, CompletableFuture<Void> completed) {
        var target = target(current, targetId); String token = UUID.randomUUID().toString();
        target.status = TargetStatus.QUALIFYING; target.problem = null;
        var taskRunner = newRunner(targetId, token, null);
        FutureTask<Void> future = new FutureTask<>(() -> {
            Throwable failure = null; String artifactHash = null;
            try {
                String endpoint = taskRunner.endpoint();
                if (!PREPARATION_TARGET.equals(targetId)) invoke(() -> { requireEndpoint(targetId, endpoint); return null; });
                taskRunner.prepare(metadataOnly);
                Path artifact = store.directory(current.id).resolve(target.directory).resolve("baseline/source").resolve(taskRunner.snapshot().config().project().artifact());
                if (Files.isRegularFile(artifact)) artifactHash = AutoLlmFileUtil.hash(artifact);
                if (!PREPARATION_TARGET.equals(targetId)) invoke(() -> { verifyQualification(targetId, taskRunner.snapshot()); return null; });
            } catch (Throwable e) { failure = e; }
            var model = taskRunner.snapshot(); var error = failure; var qualifiedArtifact = artifactHash;
            enqueue(() -> {
                if (!owns(targetId, token)) { completed.complete(null); return; }
                apply(targetId, model); jobs.remove(targetId);
                if (error == null) { target.baselineArtifactSha256 = qualifiedArtifact; target.baselineReady = !metadataOnly || PREPARATION_TARGET.equals(targetId); target.status = target.draining ? TargetStatus.DRAINING : metadataOnly ? TargetStatus.VALIDATED : TargetStatus.READY; target.problem = null; }
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
        if (current.config.execution().target() == AutoLlmConfig.Target.LOCAL) current.localPreparation = current.targets.get("local");
        if (!current.localPreparation.baselineReady) {
            if (current.config.execution().target() != AutoLlmConfig.Target.LOCAL && !jobs.containsKey(PREPARATION_TARGET) && current.localPreparation.problem == null) qualify(PREPARATION_TARGET, false, new CompletableFuture<>());
        }
        int available = 0;
        for (var e : current.targets.entrySet()) if (current.approvedTargets.contains(e.getKey()) && !jobs.containsKey(e.getKey()) && !e.getValue().draining && e.getValue().baselineReady && e.getValue().problem == null) available++;
        int preparing = 0, waiting = 0;
        for (var job : jobs.keySet()) if (job.startsWith("$trial-")) preparing++;
        for (var t : current.trials) if (t.status == TrialStatus.WAITING_FOR_TARGET && t.nextStage == AutoLlmCampaign.Stage.PREPARATION) waiting++;
        if (current.localPreparation.baselineReady) for (var t : current.trials) {
            if (preparing >= current.config.codex().maxConcurrentSessions() + current.config.preparation().localBuilds() || preparing + waiting >= current.config.preparation().readyQueueSize() + available) break;
            if (!AutoLlmCampaignStore.hasUncollectedJobs(store.directory(current.id).resolve(t.directory)) && !jobs.containsKey("$trial-" + t.number) && (t.status == TrialStatus.PENDING || t.status == TrialStatus.WAITING_FOR_TARGET) && t.nextStage.ordinal() < AutoLlmCampaign.Stage.PREPARATION.ordinal()) { try { launchLocal(t); preparing++; } catch (RuntimeException e) { t.status = TrialStatus.BLOCKED; t.problem = AutoLlmFailureUtil.describe(e, store.directory(current.id).resolve(t.directory)); save(); } }
        }
        for (var entry : current.targets.entrySet()) {
            String id = entry.getKey(); var target = entry.getValue();
            if (jobs.containsKey(id) || target.draining || Set.of(TargetStatus.REMOVED, TargetStatus.NEEDS_ATTENTION).contains(target.status)) continue;
            AutoLlmCampaign.Trial trial = null;
            try {
                if (!target.baselineReady) { qualify(id, false, new CompletableFuture<>()); continue; }
                for (var candidate : current.trials) if ((candidate.status == TrialStatus.PENDING || candidate.status == TrialStatus.WAITING_FOR_TARGET) && candidate.nextStage.ordinal() >= AutoLlmCampaign.Stage.PREPARATION.ordinal() && id.equals(candidate.targetId)) { trial = candidate; break; }
                if (trial == null) for (var candidate : current.trials) if ((candidate.status == TrialStatus.WAITING_FOR_TARGET || candidate.status == TrialStatus.PENDING) && candidate.nextStage == AutoLlmCampaign.Stage.PREPARATION && candidate.targetId == null) { trial = candidate; break; }
                if (trial != null && current.approvedTargets.contains(id)) launch(id, trial);
            } catch (RuntimeException e) {
                target.status = TargetStatus.NEEDS_ATTENTION; target.problem = AutoLlmFailureUtil.describe(e, store.directory(current.id).resolve(target.directory));
                if (trial != null) { trial.status = TrialStatus.BLOCKED; trial.problem = target.problem; trial.failure = ExceptionUtil.stacktrace(e); }
                save();
            }
        }
        settle();
    }
    private void launchLocal(AutoLlmCampaign.Trial trial) {
        trial.status = TrialStatus.STARTING;
        String key = "$trial-" + trial.number, token = UUID.randomUUID().toString();
        var c = current;
        var runner = runner(executionModel(c, PREPARATION_TARGET, trial), store.directory(c.id).resolve(trial.directory), new AutoLlmTrialRunner.Listener() {
            public void state(AutoLlmExecutionContext.Snapshot snapshot) { enqueue(() -> { if (owns(key, token)) { apply(PREPARATION_TARGET, snapshot); save(); } }); }
            public void event(String kind, Object detail) { enqueue(() -> { if (owns(key, token)) store.event(c.id, kind, detail); }); }
        }, () -> cancelled, sessions);
        runner.localBuildPermits(localBuilds);
        var future = new FutureTask<Void>(() -> {
            Throwable failure = null; try { runner.prepareTrialLocally(); } catch (Throwable e) { failure = e; }
            var snapshot = runner.snapshot(); var error = failure;
            enqueue(() -> {
                if (!owns(key, token)) return;
                apply(PREPARATION_TARGET, snapshot); jobs.remove(key);
                if (error != null) { var t = current.trials.get(trial.number - 1); t.status = TrialStatus.BLOCKED; t.problem = AutoLlmFailureUtil.describe(error, store.directory(c.id).resolve(trial.directory)); }
                save(); dispatch();
            }); return null;
        });
        jobs.put(key, new Running(token, runner, future)); save(); workers.execute(future);
    }
    private void launch(String id, AutoLlmCampaign.Trial trial) {
        var target = current.targets.get(id);
        if (trial.targetId == null) { trial.targetId = id; trial.targetRevision = current.poolRevision; trial.directory = "trials/%03d".formatted(trial.number); }
        if (trial.assignment() == null) {
            var assignment = new AutoLlmCampaign.Assignment(); assignment.id = UUID.randomUUID().toString();
            assignment.targetId = id; assignment.poolRevision = current.poolRevision;
            assignment.directory = trial.directory + "/assignments/" + assignment.id;
            trial.assignments.add(assignment); trial.activeAssignmentId = assignment.id;
        } else if (!id.equals(trial.assignment().targetId)) throw new IllegalStateException("Assignment is pinned to another target");
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
                boolean outstanding = AutoLlmCampaignStore.hasUncollectedJobs(store.directory(current.id).resolve(result.directory));
                if (!outstanding && result.nextStage == AutoLlmCampaign.Stage.GENERATION && result.status == TrialStatus.WAITING_FOR_TARGET) {
                    if (result.assignment().pinned) {
                        result.status = TrialStatus.BLOCKED;
                        result.problem = AutoLlmFailureUtil.describe(new ExecutionTargetException("Cannot release a tuned assignment"), store.directory(current.id).resolve(result.executionDirectory()));
                    } else {
                        result.assignment().released = true; result.activeAssignmentId = null; result.targetId = null;
                    }
                }
                boolean infrastructure = result.problem != null && result.problem.category() == AutoLlmFailureUtil.Category.INFRASTRUCTURE;
                if (outstanding || infrastructure) { target.status = TargetStatus.NEEDS_ATTENTION; target.problem = result.problem; }
                else target.status = target.draining ? TargetStatus.DRAINING : TargetStatus.READY;
                save(); reportAsync(false); dispatch();
            }); return null;
        });
        jobs.put(id, new Running(token, taskRunner, future)); save(); workers.execute(future);
    }
    private void settle() {
        if (!jobs.isEmpty()) { current.status = CampaignStatus.RUNNING; save(); return; }
        boolean pending = false, blocked = false, failed = false;
        for (var t : current.trials) { pending |= t.status == TrialStatus.PENDING || t.status == TrialStatus.WAITING_FOR_TARGET; blocked |= t.status.recoverable(); failed |= t.status == TrialStatus.FAILED; }
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
            if (releaseAfter && snapshot.status.completed()) maintainStorage(snapshot, false);
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
    private void maintainStorage(AutoLlmCampaign campaign, boolean retryTrials) {
        Path root = store.directory(campaign.id);
        if (!campaign.status.completed() || !Files.exists(root.resolve("control/storage-layout.json"))) return;
        try {
            for (var trial : campaign.trials) {
                if (!trial.status.terminal()) continue;
                for (var assignment : trial.assignments) {
                    if (assignment.config == null) continue;
                    Path directory = root.resolve(assignment.directory);
                    var config = assignment.config;
                    RemoteStorageMaintenance.maintain(root, directory, () -> false);
                    if (retryTrials) AutoLlmArtifactRebuild.maintain(root, directory, trial, config, processes, () -> false);
                    AutoLlmCheckoutRetirement.maintainTrial(root, directory, trial, config, campaign.baselineCommit, campaign.versions, processes, () -> false);
                    AutoLlmEvidenceFiles.compact(root, directory, trial);
                }
                Path directory = root.resolve(trial.directory);
                var config = campaign.config.localPreparation();
                if (trial.assignments.isEmpty() && retryTrials) AutoLlmArtifactRebuild.maintain(root, directory, trial, config, processes, () -> false);
                AutoLlmCheckoutRetirement.maintainTrial(root, directory, trial, config, campaign.baselineCommit, campaign.versions, processes, () -> false);
                AutoLlmEvidenceFiles.compact(root, directory, trial);
            }
            for (var target : campaign.targets.values()) if (target.directory != null) {
                var config = target.resolvedConfig == null ? campaign.config : target.resolvedConfig;
                RemoteStorageMaintenance.maintain(root, root.resolve(target.directory), () -> false);
            }
            AutoLlmCheckoutRetirement.maintainCampaign(root, campaign, processes, () -> false);
        } catch (IOException | RuntimeException failure) { store.event(campaign.id, "storage-maintenance-failed", Map.of("reason", failure.toString())); }
    }
    public AutoLlmStorageReport.Report maintainStorage(String id) {
        return invoke(() -> {
            if (active != null) throw new IllegalStateException("Wait for active work before storage maintenance");
            try (var ownership = store.acquire(id, store.directory(id))) {
                var campaign = store.load(id);
                if (!campaign.status.completed()) throw new IllegalStateException("Storage maintenance requires a completed campaign");
                maintainStorage(campaign, true);
                return AutoLlmStorageReport.inspect(store.directory(id));
            }
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
                if (c.config.execution().target() == AutoLlmConfig.Target.SSH && !c.localPreparation.baselineReady) { var localDone = new CompletableFuture<Void>(); list.add(localDone); qualify(PREPARATION_TARGET, true, localDone); }
                for (String target : c.targets.keySet()) { var done = new CompletableFuture<Void>(); list.add(done); if (c.targets.get(target).baselineReady && c.targets.get(target).problem == null) done.complete(null); else qualify(target, false, done); }
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
        if (c.config.execution().target() == AutoLlmConfig.Target.SSH && !c.localPreparation.baselineReady) failed.add(PREPARATION_TARGET);
        boolean ready = c.status == CampaignStatus.DRAFT && !qualified.isEmpty() && failed.isEmpty();
        boolean withinLimit = !targets.isEmpty();
        for (var target : targets.values()) withinLimit &= target.withinLimit();
        return new BaselineCheck(maximum, c.config.maxParameters(), withinLimit, ready, List.copyOf(qualified), List.copyOf(failed), Map.copyOf(targets));
    }

    private static String read(Path path) { try { return Files.readString(path); } catch (IOException e) { throw new UncheckedIOException(e); } }
    public AutoLlmTargetProbeService.Probe probeTarget(AutoLlmConfig config) { return targetProbes.start(config); }
    public boolean hasSelectedTargetProbes() { return targetProbes.selectedActive(); }
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
        var busy = new HashSet<String>(); if (campaign == current) busy.addAll(jobs.keySet()); var uncollected = new HashSet<String>();
        for (var entry : campaign.targets.entrySet()) {
            if (campaign == current && jobs.containsKey(entry.getKey())) busy.add(entry.getKey());
            if (!busy.contains(entry.getKey()) && AutoLlmCampaignStore.hasUncollectedJobs(store.directory(campaign.id).resolve(entry.getValue().directory))) uncollected.add(entry.getKey());
        }
        for (var t : campaign.trials) if (t.directory != null && AutoLlmCampaignStore.hasUncollectedJobs(store.directory(campaign.id).resolve(t.directory))) {
            uncollected.add("$trial-" + t.number); if (t.targetId != null) uncollected.add(t.targetId);
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
            var target = trial.targetId == null ? null : requireTarget(current, trial.targetId);
            trial.queueForResume();
            if (target != null) target.resume(false, ++current.poolRevision);
            store.event(id, "trial-resumed-by-operator", Map.of("trial", number, "targetId", trial.targetId == null ? "local-preparation" : trial.targetId));
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
        var target = trial.targetId == null ? null : requireTarget(c, trial.targetId);
        trial.status = TrialStatus.FAILED;
        if (target != null && (target.assignedTrial == null || target.assignedTrial == number)) { target.assignedTrial = null; target.problem = null; target.status = target.draining ? TargetStatus.DRAINING : TargetStatus.READY; }
        store.event(c.id, "trial-failed-by-operator", Map.of("trial", number, "targetId", trial.targetId == null ? "local-preparation" : trial.targetId, "reason", reason.strip()));
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
        for (var trial : current.trials) for (var assignment : trial.assignments) {
            if (!AutoLlmCampaignStore.hasUncollectedJobs(store.directory(current.id).resolve(assignment.directory))) continue;
            String key = "$cancel-" + trial.number + "-" + assignment.id, token = UUID.randomUUID().toString();
            Path root = store.directory(current.id).resolve(assignment.directory);
            var model = new AutoLlmExecutionContext(current.id, assignment.directory, store.directory(current.id), current.prompt);
            model.config = assignment.config; model.trial = trial.copy();
            var runner = runner(model, root, new AutoLlmTrialRunner.Listener() {
                public void state(AutoLlmExecutionContext.Snapshot snapshot) {}
                public void event(String kind, Object detail) {}
            }, () -> true, sessions);
            var future = new FutureTask<Void>(() -> {
                Throwable failure = null; try { runner.cancelOutstanding(); } catch (Throwable e) { failure = e; }
                var error = failure;
                enqueue(() -> { if (!owns(key, token)) return; jobs.remove(key); if (error != null) current.problem = AutoLlmFailureUtil.describe(error, root); if (jobs.isEmpty()) finishCancellation(); }); return null;
            });
            queued = true; jobs.put(key, new Running(token, runner, future)); workers.execute(future);
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
            if (jobs.containsKey("$trial-" + number)) return jobs.get("$trial-" + number).runner;
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
