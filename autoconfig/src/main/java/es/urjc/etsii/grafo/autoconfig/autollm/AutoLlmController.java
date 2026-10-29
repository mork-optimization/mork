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
        result.put("mode", mode.mode().name()); result.put("apiVersion", 4); result.put("checkpointVersion", AutoLlmCampaign.SCHEMA_VERSION);
        result.put("runnerProtocol", es.urjc.etsii.grafo.autoconfig.autollm.execution.WorkloadProtocol.VERSION);
        result.put("minimumJavaFeature", es.urjc.etsii.grafo.autoconfig.autollm.execution.TargetPreflightUtil.minimumJavaFeature());
        result.put("workspace", store.root().toString()); result.put("pid", ProcessHandle.current().pid());
        result.put("processStartedAt", ProcessHandle.current().info().startInstant().map(java.time.Instant::toString).orElse(null));
        result.put("workingDirectory", Path.of("").toAbsolutePath().toString());
        result.put("artifact", artifact == null ? null : artifact.getAbsolutePath());
        result.put("artifactSha256", artifactSha256);
        result.put("baseUrl", "http://127.0.0.1:" + environment.getProperty("local.server.port", environment.getProperty("server.port", "8080")));
        result.put("features", List.of("LOCAL", "SSH", "SCREENING_JOBS", "CODEX_PROGRESS", "BYTE_LOG_CURSORS", "STRUCTURED_FAILURES", "SSH_SERVER_POOL", "DYNAMIC_TARGETS", "FAIL_BLOCKED_TRIAL", "RECOVERY_ACTIONS", "FILE_TAIL_PREVIEWS", "ASYNC_TARGET_PREFLIGHT"));
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
    public record StartRequest(Set<String> targets) {}
    @GetMapping("/defaults") public AutoLlmConfig defaults() { return service.defaults(); }
    @PostMapping("/validate") public Map<String, Object> validate(@RequestBody AutoLlmConfig config) { var errors = service.validate(config); return Map.of("valid", errors.isEmpty(), "errors", errors); }
    @PostMapping("/targets/probe") public Object probe(@RequestBody AutoLlmConfig config) { requireMode(); return service.probeTarget(config); }
    @PostMapping("/campaigns/{id}/baseline-check") public Object baselineCheck(@PathVariable String id) { requireMode(); return service.baselineCheck(id); }
    @PostMapping("/campaigns/{id}/trials/{number}/search-space-check") public Object metadataCheck(@PathVariable String id, @PathVariable int number, @RequestBody Map<String,String> options) { requireMode(); return service.metadataCheck(id, number, options.get("blacklist")); }
    @GetMapping("/targets/probes/{id}") public AutoLlmTargetProbeService.Probe targetProbe(@PathVariable String id) { return service.targetProbe(id); }
    @GetMapping("/campaigns") public Map<String, Object> list() { var active = service.active(); return Map.of("active", active == null ? "" : active, "campaigns", store.statuses()); }
    @PostMapping("/campaigns") public AutoLlmCampaign create(@RequestBody AutoLlmConfig config) { requireMode(); return service.create(config); }
    @GetMapping("/campaigns/{id}") public AutoLlmCampaign campaign(@PathVariable String id) { return store.load(id); }
    @GetMapping("/campaigns/{id}/status") public AutoLlmCampaign status(@PathVariable String id) { return store.status(id); }
    @PostMapping("/campaigns/{id}/start") public AutoLlmCampaign start(@PathVariable String id, @RequestBody(required = false) StartRequest request) { requireMode(); return service.start(id, false, request == null || request.targets() == null ? Set.of() : request.targets()); }
    @PostMapping("/campaigns/{id}/resume") public AutoLlmCampaign resume(@PathVariable String id) { requireMode(); return service.start(id, true, Set.of()); }
    @PostMapping("/campaigns/{id}/cancel") public AutoLlmCampaign cancel(@PathVariable String id) { requireMode(); return service.cancel(id); }
    @PostMapping("/campaigns/{id}/trials/{number}/fail") public AutoLlmCampaign failTrial(@PathVariable String id, @PathVariable int number, @RequestBody Map<String,String> request) { requireMode(); return service.failTrial(id, number, request.get("reason")); }
    @GetMapping("/campaigns/{id}/recovery") public AutoLlmRecoveryUtil.Recovery recovery(@PathVariable String id) { return service.recovery(id); }
    @PostMapping("/campaigns/{id}/trials/{number}/resume") public AutoLlmCampaign resumeTrial(@PathVariable String id, @PathVariable int number) { requireMode(); return service.resumeTrial(id, number); }
    @PostMapping("/campaigns/{id}/report") public AutoLlmCampaign report(@PathVariable String id) { requireMode(); return service.report(id); }
    @GetMapping("/campaigns/{id}/targets") public Object targets(@PathVariable String id) { var c = store.load(id); return Map.of("poolRevision", c.poolRevision, "targets", c.targets); }
    @PutMapping("/campaigns/{id}/targets/{targetId}") public AutoLlmCampaign addTarget(@PathVariable String id, @PathVariable String targetId, @RequestBody AutoLlmConfig.Server server) { requireMode(); return service.addTarget(id, targetId, server); }
    @PostMapping("/campaigns/{id}/targets/{targetId}/drain") public AutoLlmCampaign drainTarget(@PathVariable String id, @PathVariable String targetId) { requireMode(); return service.drainTarget(id, targetId, false); }
    @PostMapping("/campaigns/{id}/targets/{targetId}/resume") public AutoLlmCampaign resumeTarget(@PathVariable String id, @PathVariable String targetId) { requireMode(); return service.resumeTarget(id, targetId); }
    @PostMapping("/campaigns/{id}/targets/{targetId}/requalify") public AutoLlmCampaign requalifyTarget(@PathVariable String id, @PathVariable String targetId) { requireMode(); return service.requalifyTarget(id, targetId); }
    @DeleteMapping("/campaigns/{id}/targets/{targetId}") public AutoLlmCampaign removeTarget(@PathVariable String id, @PathVariable String targetId) { requireMode(); return service.drainTarget(id, targetId, true); }
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
        var campaign = store.status(id);
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
                    if (Files.exists(directory.resolve(name))) { progress.put("workload", AutoLlmFileUtil.read(directory.resolve(name), Object.class)); break; }
                }
            }
            return progress;
        }
        throw new NoSuchElementException("Trial unavailable");
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
