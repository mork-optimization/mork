package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.monitoring.MonitorModeState;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.io.File;
import java.nio.file.*;
import java.util.*;

@RestController
@org.springframework.boot.autoconfigure.condition.ConditionalOnProperty(name = "autollm")
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/autollm")
public class AutoLlmController {
    private final AutoLlmCampaignService service;
    private final AutoLlmCampaignStore store;
    private final MonitorModeState mode;
    private final Environment environment;
    private final File artifact;
    private final String artifactSha256;
    public AutoLlmController(AutoLlmCampaignService service, AutoLlmCampaignStore store, MonitorModeState mode, Environment environment) {
        this.service = service; this.store = store; this.mode = mode; this.environment = environment;
        this.artifact = new org.springframework.boot.system.ApplicationHome().getSource();
        this.artifactSha256 = artifact != null && artifact.isFile() ? AutoLlmFileUtil.hash(artifact.toPath()) : null;
    }
    @GetMapping("/capabilities") public Map<String,Object> capabilities() {
        var result = new LinkedHashMap<String,Object>();
        result.put("mode", mode.mode().name()); result.put("apiVersion", 5); result.put("checkpointVersion", AutoLlmCampaign.SCHEMA_VERSION);
        result.put("runnerProtocol", es.urjc.etsii.grafo.autoconfig.autollm.execution.WorkloadProtocol.VERSION);
        result.put("minimumJavaFeature", es.urjc.etsii.grafo.autoconfig.autollm.execution.TargetPreflightUtil.minimumJavaFeature());
        result.put("workspace", store.root().toString()); result.put("pid", ProcessHandle.current().pid());
        result.put("processStartedAt", ProcessHandle.current().info().startInstant().map(java.time.Instant::toString).orElse(null));
        result.put("workingDirectory", Path.of("").toAbsolutePath().toString());
        result.put("artifact", artifact == null ? null : artifact.getAbsolutePath());
        result.put("artifactSha256", artifactSha256);
        result.put("baseUrl", "http://127.0.0.1:" + environment.getProperty("local.server.port", environment.getProperty("server.port", "8080")));
        result.put("features", List.of("LOCAL", "SSH", "SCREENING_JOBS", "CODEX_PROGRESS", "BYTE_LOG_CURSORS", "STRUCTURED_FAILURES", "SSH_SERVER_POOL", "DYNAMIC_TARGETS", "FAIL_BLOCKED_TRIAL", "RECOVERY_ACTIONS", "FILE_TAIL_PREVIEWS", "ASYNC_TARGET_PREFLIGHT", "COMPACT_OBSERVATIONS", "CAMPAIGN_APPROVAL", "REMOTE_INSPECTION"));
        return result;
    }
    @org.springframework.context.event.EventListener(org.springframework.boot.context.event.ApplicationReadyEvent.class)
    public void saveSession() { AutoLlmFileUtil.write(store.root().resolve("session.json"), capabilities()); }
    @PostMapping("/effective-config") public Object effectiveConfig(@RequestBody AutoLlmConfig config) { return AutoLlmSummaryUtil.summarize(config, null, Map.of(), config.trials()); }
    @GetMapping("/campaigns/{id}/summary") public Object summary(@PathVariable String id) {
        var campaign = store.load(id); var result = new LinkedHashMap<String,Object>();
        result.put("poolRevision", campaign.poolRevision); result.put("maxConcurrentSessions", campaign.config.codex().maxConcurrentSessions());
        var targets = new LinkedHashMap<String,Object>(); Map<String,Long> sharedBudgets = Map.of();
        for (var entry : campaign.targets.entrySet()) {
            var target = entry.getValue(); Path budgets = store.directory(id).resolve(target.directory).resolve("control/comparison-time-limits.json");
            Map<String,Long> limits = Files.exists(budgets) ? AutoLlmFileUtil.read(budgets, new tools.jackson.core.type.TypeReference<>() {}) : Map.of();
            if (!limits.isEmpty()) sharedBudgets = limits;
            int assigned = 0; for (var trial : campaign.trials) if (entry.getKey().equals(trial.targetId)) assigned++;
            targets.put(entry.getKey(), AutoLlmSummaryUtil.summarize(target.resolvedConfig == null ? campaign.config : target.resolvedConfig, target.host,
                    limits, assigned));
        }
        result.put("campaign", AutoLlmSummaryUtil.summarize(campaign.config, null, sharedBudgets, campaign.config.trials()));
        result.put("targets", targets); return result;
    }
    @PostMapping("/campaigns/{id}/proposal") public Object selectProposal(@PathVariable String id, @RequestBody StartRequest request) { requireMode(); return service.selectProposal(id, request.targets() == null ? Set.of() : request.targets()); }
    @GetMapping("/campaigns/{id}/proposal") public Object proposal(@PathVariable String id) { return service.proposal(id); }
    @PostMapping("/campaigns/{id}/amend-trials") public Object amendTrials(@PathVariable String id, @RequestBody Map<String,Integer> body) { requireMode(); if (body.get("trials") == null) throw new IllegalArgumentException("Provide trials"); return service.amendTrials(id, body.get("trials")); }
    @PostMapping("/campaigns/{id}/approve") public Object approve(@PathVariable String id, @RequestBody Map<String,String> body) { requireMode(); var proposal = service.approve(id, body.get("digest")); return Map.of("id", id, "digest", proposal.digest(), "accepted", true); }
    @PutMapping("/campaigns/{id}/dataset") public Object reviseDataset(@PathVariable String id, @RequestBody AutoLlmCampaignService.DatasetSelection selection) { requireMode(); return receipt(service.reviseDataset(id, selection), "reviseDataset", null); }
    @PutMapping("/campaigns/{id}/draft") public Object editDraft(@PathVariable String id, @RequestBody AutoLlmConfig config) { requireMode(); return receipt(service.editDraft(id, config), "editDraft", null); }
    public record Receipt(String id, AutoLlmCampaign.CampaignStatus status, String operation, Integer trial, boolean accepted) {}
    private Receipt receipt(AutoLlmCampaign campaign, String operation, Integer trial) {
        return new Receipt(campaign.id, campaign.status, operation, trial, true);
    }
    public record StartRequest(Set<String> targets) {}
    @GetMapping("/defaults") public AutoLlmConfig defaults() { return service.defaults(); }
    @PostMapping("/validate") public Map<String, Object> validate(@RequestBody AutoLlmConfig config) { var errors = service.validate(config); return Map.of("valid", errors.isEmpty(), "errors", errors); }
    @PostMapping("/targets/probe") public Object probe(@RequestBody AutoLlmConfig config) { requireMode(); return service.probeTarget(config); }
    @PostMapping("/campaigns/{id}/baseline-check") public Object baselineCheck(@PathVariable String id) { requireMode(); return service.baselineCheck(id); }
    @PostMapping("/campaigns/{id}/trials/{number}/search-space-check") public Object metadataCheck(@PathVariable String id, @PathVariable int number, @RequestBody Map<String,String> options) { requireMode(); return service.metadataCheck(id, number, options.get("blacklist")); }
    @GetMapping("/targets/probes/{id}") public AutoLlmTargetProbeService.Probe targetProbe(@PathVariable String id) { return service.targetProbe(id); }
    @GetMapping("/campaigns") public Map<String, Object> list() { var active = service.active(); return Map.of("active", active == null ? "" : active, "campaigns", store.statuses()); }
    @PostMapping("/campaigns") public Receipt create(@RequestBody AutoLlmConfig config) { requireMode(); return receipt(service.create(config), "create", null); }
    @GetMapping("/campaigns/{id}") public AutoLlmCampaign campaign(@PathVariable String id) { return store.load(id); }
    @GetMapping("/campaigns/{id}/status") public AutoLlmCampaign status(@PathVariable String id) { return store.status(id); }
    @PostMapping("/campaigns/{id}/start") public Receipt start(@PathVariable String id, @RequestBody(required = false) StartRequest request) { requireMode(); return receipt(service.start(id, false, request == null || request.targets() == null ? Set.of() : request.targets()), "start", null); }
    @PostMapping("/campaigns/{id}/resume") public Receipt resume(@PathVariable String id) { requireMode(); return receipt(service.start(id, true, Set.of()), "resume", null); }
    @PostMapping("/campaigns/{id}/cancel") public Receipt cancel(@PathVariable String id) { requireMode(); return receipt(service.cancel(id), "cancel", null); }
    @PostMapping("/campaigns/{id}/trials/{number}/fail") public Receipt failTrial(@PathVariable String id, @PathVariable int number, @RequestBody Map<String,String> request) { requireMode(); return receipt(service.failTrial(id, number, request.get("reason")), "failTrial", number); }
    @GetMapping("/campaigns/{id}/recovery") public AutoLlmRecoveryUtil.Recovery recovery(@PathVariable String id) { return service.recovery(id); }
    @PostMapping("/campaigns/{id}/trials/{number}/resume") public Receipt resumeTrial(@PathVariable String id, @PathVariable int number) { requireMode(); return receipt(service.resumeTrial(id, number), "resumeTrial", number); }
    @PostMapping("/campaigns/{id}/report") public Receipt report(@PathVariable String id) { requireMode(); return receipt(service.report(id), "report", null); }
    @GetMapping("/campaigns/{id}/targets") public Object targets(@PathVariable String id) { var c = store.load(id); return Map.of("poolRevision", c.poolRevision, "targets", c.targets); }
    @PutMapping("/campaigns/{id}/targets/{targetId}") public Receipt addTarget(@PathVariable String id, @PathVariable String targetId, @RequestBody AutoLlmConfig.Server server) { requireMode(); return receipt(service.addTarget(id, targetId, server), "addTarget", null); }
    @PostMapping("/campaigns/{id}/targets/{targetId}/drain") public Receipt drainTarget(@PathVariable String id, @PathVariable String targetId) { requireMode(); return receipt(service.drainTarget(id, targetId, false), "drainTarget", null); }
    @PostMapping("/campaigns/{id}/targets/{targetId}/resume") public Receipt resumeTarget(@PathVariable String id, @PathVariable String targetId) { requireMode(); return receipt(service.resumeTarget(id, targetId), "resumeTarget", null); }
    @PostMapping("/campaigns/{id}/targets/{targetId}/requalify") public Receipt requalifyTarget(@PathVariable String id, @PathVariable String targetId) { requireMode(); return receipt(service.requalifyTarget(id, targetId), "requalifyTarget", null); }
    @DeleteMapping("/campaigns/{id}/targets/{targetId}") public Receipt removeTarget(@PathVariable String id, @PathVariable String targetId) { requireMode(); return receipt(service.drainTarget(id, targetId, true), "removeTarget", null); }
    @GetMapping("/campaigns/{id}/artifacts") public List<AutoLlmCampaignStore.Artifact> artifacts(@PathVariable String id,
            @RequestParam(required = false) Integer trial, @RequestParam(defaultValue = "false") boolean hashes) {
        var campaign = store.load(id);
        if (trial == null) return store.artifacts(id, null, hashes);
        for (var candidate : campaign.trials) if (candidate.number == trial) {
            return candidate.directory == null ? List.of() : store.artifacts(id, candidate.directory, hashes);
        }
        throw new NoSuchElementException("Unknown trial " + trial);
    }
    @GetMapping("/campaigns/{id}/artifact") public ResponseEntity<FileSystemResource> download(@PathVariable String id, @RequestParam String path) {
        Path file = store.artifact(id, path);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(file.getFileName().toString()).build().toString())
                .body(new FileSystemResource(file));
    }
    @GetMapping("/campaigns/{id}/log") public AutoLlmLogUtil.Page log(@PathVariable String id, @RequestParam(defaultValue = "campaign-events.jsonl") String path,
            @RequestParam(defaultValue = "0") long cursor, @RequestParam(defaultValue = "65536") int maximumBytes) {
        Path file = store.artifact(id, path);
        if (!(path.endsWith(".log") || path.endsWith(".jsonl") || path.endsWith(".txt"))) throw new IllegalArgumentException("Select a text log");
        return AutoLlmLogUtil.read(file, cursor, maximumBytes);
    }
    @GetMapping("/campaigns/{id}/preview") public AutoLlmLogUtil.Preview preview(@PathVariable String id, @RequestParam String path,
            @RequestParam(defaultValue = "500") int lines) {
        return AutoLlmLogUtil.tail(store.artifact(id, path), lines);
    }
    @GetMapping("/campaigns/{id}/trials/{number}/progress") public Object progress(@PathVariable String id, @PathVariable int number) {
        return progress(store.status(id), number);
    }
    private Object progress(AutoLlmCampaign campaign, int number) {
        String id = campaign.id;
        for (var trial : campaign.trials) if (trial.number == number) {
            var progress = new LinkedHashMap<String,Object>(); progress.put("targetId", trial.targetId); progress.put("poolRevision", trial.targetRevision); progress.put("status", trial.status); progress.put("problem", trial.problem);
            int retries = trial.infrastructureRetries;
            for (int count : trial.connectionRetries.values()) retries += count;
            progress.put("infrastructureRetries", retries); progress.put("repairTurns", trial.repairTurns);
            progress.put("quotaRetryAt", trial.quotaRetryAt);
            if (!trial.attempts.isEmpty()) {
                var attempt = trial.attempts.getLast(); Path directory = store.directory(id).resolve(attempt.directory);
                progress.put("attempt", attempt); progress.put("substage", attempt.substage);
                progress.put("elapsedMillis", java.time.Duration.between(java.time.Instant.parse(attempt.startedAt), attempt.finishedAt == null ? java.time.Instant.now() : java.time.Instant.parse(attempt.finishedAt)).toMillis());
                Path remote = directory.resolve("remote-status.json");
                if (attempt.substage != null) remote = directory.resolve(switch (attempt.substage) { case BUILD -> "build"; case METADATA -> "check"; case PROBES -> "probe"; case PILOT -> "pilot"; default -> ""; }).resolve("remote-status.json");
                if (Files.exists(remote)) progress.put("remote", AutoLlmFileUtil.read(remote, Object.class));
                if (attempt.stage.generation()) progress.put("generation", CodexProgressUtil.refresh(directory));
                for (String name : List.of("execution/tuning-progress.json", "pilot/execution/tuning-progress.json", "execution/tuning-status.json", "probe/execution/probes.json")) {
                    if (Files.exists(directory.resolve(name))) { progress.put("workload", AutoLlmFileUtil.read(directory.resolve(name), Object.class)); try { progress.put("workloadUpdatedAt", Files.getLastModifiedTime(directory.resolve(name)).toInstant().toString()); } catch (java.io.IOException e) { throw new java.io.UncheckedIOException(e); } break; }
                }
            }
            return progress;
        }
        throw new NoSuchElementException("Trial unavailable");
    }
    public record TrialObservation(int number, String targetId, AutoLlmCampaign.TrialStatus status,
                                   AutoLlmCampaign.Stage nextStage, int repairTurns, AutoLlmFailureUtil.Failure problem, Object progress) {}
    public record Observation(String id, AutoLlmCampaign.CampaignStatus status, String checkedAt,
                              List<TrialObservation> trials, Map<String,Object> targets, AutoLlmRecoveryUtil.Recovery recovery,
                              Map<Integer,String> errors, boolean partial, String approvedProposal, String approvedAt, boolean pendingAmendment) {}
    @GetMapping("/campaigns/{id}/observation") public Observation observation(@PathVariable String id) {
        var c = store.status(id); var rows = new ArrayList<TrialObservation>(); var errors = new LinkedHashMap<Integer,String>();
        for (var t : c.trials) {
            Map<String,Object> details = new LinkedHashMap<>();
            if (t.status.inProgress()) try {
                @SuppressWarnings("unchecked") var observed = (Map<String,Object>) progress(c, t.number);
                details.putAll(observed); details.remove("attempt");
            } catch (RuntimeException e) { errors.put(t.number, e.toString()); }
            rows.add(new TrialObservation(t.number, t.targetId, t.status, t.nextStage, t.repairTurns, t.status.terminal() ? null : t.problem, details));
        }
        var targets = new LinkedHashMap<String,Object>();
        for (var e : c.targets.entrySet()) targets.put(e.getKey(), Map.of("status", e.getValue().status, "ready", e.getValue().baselineReady, "draining", e.getValue().draining));
        return new Observation(id, c.status, java.time.Instant.now().toString(), rows, targets, service.recovery(id), errors, !errors.isEmpty(), c.approvedProposal, c.approvedAt, c.proposedTrials != null || !c.reviewedTargets.equals(c.approvedTargets));
    }
    @GetMapping("/campaigns/{id}/trials/{number}/remote-inspection") public Object inspectRemote(@PathVariable String id, @PathVariable int number,
            @RequestParam(defaultValue = "10000") long timeoutMillis) {
        if (timeoutMillis < 100 || timeoutMillis > 30000) throw new IllegalArgumentException("Inspection timeout must be between 100 and 30000 ms");
        var c = store.load(id);
        for (var trial : c.trials) if (trial.number == number) {
            Path root = store.directory(id).resolve(trial.directory);
            Path configFile = root.resolve("control/config.json");
            if (!Files.isRegularFile(configFile)) throw new IllegalStateException("Trial has no saved execution context");
            var config = AutoLlmFileUtil.read(configFile, AutoLlmConfig.class);
            if (config.execution().target() != AutoLlmConfig.Target.SSH) throw new IllegalStateException("Trial has no resolved SSH context");
            var descriptors = AutoLlmFileUtil.ownedFiles(root, "remote-job.json");
            var pending = new ArrayList<Path>();
            for (Path descriptor : descriptors) if (!Files.isRegularFile(descriptor.getParent().resolve("collection-completed.json"))) pending.add(descriptor);
            if (pending.isEmpty() && !descriptors.isEmpty()) pending.add(descriptors.getLast());
            var results = java.util.Collections.synchronizedList(new ArrayList<Object>());
            try (var executor = java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor()) {
                var future = executor.submit(() -> {
                    var transport = new es.urjc.etsii.grafo.autoconfig.autollm.execution.SshTransport(config, root.resolve(".transport"));
                    transport.bootstrap();
                    for (Path descriptor : pending) {
                        var job = AutoLlmFileUtil.read(descriptor, es.urjc.etsii.grafo.autoconfig.autollm.execution.WorkloadProtocol.Job.class);
                        results.add(AutoLlmFileUtil.JSON.readTree(transport.call("inspect", job.root(), job.id(), Map.of())));
                    }
                });
                try { future.get(timeoutMillis, java.util.concurrent.TimeUnit.MILLISECONDS); }
                catch (java.util.concurrent.TimeoutException e) { future.cancel(true); return Map.of("partial", true, "jobs", List.copyOf(results), "error", "Remote inspection deadline expired"); }
                catch (InterruptedException e) { future.cancel(true); Thread.currentThread().interrupt(); throw new java.util.concurrent.CancellationException("Inspection interrupted"); }
                catch (java.util.concurrent.ExecutionException e) { return Map.of("partial", true, "jobs", List.copyOf(results), "error", e.getCause().toString()); }
            }
            return Map.of("partial", false, "jobs", List.copyOf(results));
        }
        throw new NoSuchElementException("Trial unavailable");
    }
    public record TrialSelection(List<Integer> trials) {}
    @PostMapping("/campaigns/{id}/recover") public Object recover(@PathVariable String id, @RequestBody TrialSelection selected) {
        requireMode();
        if (selected.trials() == null || selected.trials().isEmpty()) throw new IllegalArgumentException("Select trials explicitly");
        var results = new LinkedHashMap<Integer,Object>();
        for (int number : new LinkedHashSet<>(selected.trials())) try {
            service.resumeTrial(id, number); results.put(number, Map.of("accepted", true));
        } catch (RuntimeException e) { results.put(number, Map.of("accepted", false, "reason", e.getMessage())); }
        return results;
    }
    @GetMapping("/campaigns/{id}/trials/{number}/activity") public AutoLlmLogUtil.Page activity(@PathVariable String id, @PathVariable int number,
            @RequestParam int attempt, @RequestParam(defaultValue = "0") long cursor) {
        for (var trial : store.status(id).trials) if (trial.number == number) for (var candidate : trial.attempts) if (candidate.number == attempt && candidate.stage.generation()) {
            Path directory = store.directory(id).resolve(candidate.directory); CodexProgressUtil.refresh(directory);
            return AutoLlmLogUtil.read(directory.resolve("activity.jsonl"), cursor, 65536);
        }
        throw new NoSuchElementException("Generation attempt unavailable");
    }
    private final AutoLlmComparisonUtil.Cache comparisons = new AutoLlmComparisonUtil.Cache();
    private Path comparisonJournal(String id, int number) {
        for (var trial : store.status(id).trials) if (trial.number == number) {
            String directory = trial.latestDirectory(AutoLlmCampaign.Stage.COMPARISON);
            return directory == null ? null : store.directory(id).resolve(directory).resolve("execution/events.jsonl");
        }
        throw new NoSuchElementException("Trial unavailable");
    }
    @GetMapping("/campaigns/{id}/trials/{number}/comparison") public Object comparison(@PathVariable String id, @PathVariable int number) {
        Path file = comparisonJournal(id, number);
        return file == null || !Files.exists(file) ? Map.of("status", "NOT_STARTED") : comparisons.summary(file);
    }
    @GetMapping("/campaigns/{id}/comparison") public Object compare(@PathVariable String id, @RequestParam String objective,
            @RequestParam(defaultValue = "MINIMIZE") es.urjc.etsii.grafo.algorithms.FMode mode,
            @RequestParam(defaultValue = "NONE") AutoLlmComparisonReport.Ranking ranking,
            @RequestParam(required = false) Set<Integer> trials) {
        var c = store.load(id); var included = new ArrayList<AutoLlmComparisonReport.Run>(); var excluded = new LinkedHashMap<Integer,String>();
        int expected = es.urjc.etsii.grafo.util.InstancePathUtil.list(c.config.comparisonPath()).size();
        for (var trial : c.trials) {
            if (trials != null && !trials.contains(trial.number)) continue;
            Path journal = comparisonJournal(id, trial.number);
            if (trial.status != AutoLlmCampaign.TrialStatus.SUCCEEDED || journal == null || !Files.isRegularFile(journal)) {
                excluded.put(trial.number, "Comparison incomplete: " + trial.status); continue;
            }
            try {
                included.add(AutoLlmComparisonReport.run(trial.number, trial.targetId, c.config.comparison().repetitions(), expected,
                        store.directory(id).relativize(journal).toString(), comparisons.measured(journal, objective, mode), mode));
            } catch (IllegalStateException e) { excluded.put(trial.number, e.getMessage()); }
        }
        return Map.of("campaignId", id, "objective", objective, "mode", mode, "checkedAt", java.time.Instant.now().toString(),
                "reference", "Each trial's host-matched SOTA; equal instance weights; relative/absolute tie tolerance 1e-9", "trials", included, "excluded", excluded,
                "ranking", ranking == AutoLlmComparisonReport.Ranking.NONE ? List.of() : AutoLlmComparisonReport.rank(included, mode),
                "rankingMethod", ranking == AutoLlmComparisonReport.Ranking.NONE ? "NONE" : "Mean improvement versus per-instance mean SOTA across the included runs; equal instance weights");
    }
    @GetMapping("/campaigns/{id}/trials/{number}/curves") public Object curves(@PathVariable String id, @PathVariable int number,
            @RequestParam String instanceId) {
        Path file = comparisonJournal(id, number);
        return file == null || !Files.exists(file) ? Map.of("curves", List.of(), "sampled", true) : comparisons.curves(file, instanceId);
    }
    private void requireMode() { if (mode.mode() != MonitorModeState.Mode.AUTOLLM) throw new ResponseStatusException(HttpStatus.CONFLICT, "Launch Mork using --autollm"); }
    @ExceptionHandler(IllegalArgumentException.class) public ResponseEntity<Map<String,String>> invalid(IllegalArgumentException e) { return ResponseEntity.badRequest().body(Map.of("message", e.getMessage())); }
    @ExceptionHandler(IllegalStateException.class) public ResponseEntity<Map<String,String>> conflict(IllegalStateException e) { return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage())); }
    @ExceptionHandler(NoSuchElementException.class) public ResponseEntity<Map<String,String>> missing(NoSuchElementException e) { return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage())); }
}
