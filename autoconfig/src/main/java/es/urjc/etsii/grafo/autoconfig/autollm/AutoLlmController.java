package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.monitoring.MonitorModeState;
import org.springframework.core.env.Environment;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.io.*;
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
    private final Map<Path, CachedComparison> comparisons = new java.util.concurrent.ConcurrentHashMap<>();
    private record CachedComparison(long size, long modified, Object value) {}
    public AutoLlmController(AutoLlmCampaignService service, AutoLlmCampaignStore store, MonitorModeState mode, Environment environment) {
        this.service = service; this.store = store; this.mode = mode; this.environment = environment;
        this.artifact = new org.springframework.boot.system.ApplicationHome().getSource();
        this.artifactSha256 = artifact != null && artifact.isFile() ? AutoLlmFileUtil.hash(artifact.toPath()) : null;
    }
    @GetMapping("/capabilities") public Map<String,Object> capabilities() {
        var result = new LinkedHashMap<String,Object>();
        result.put("mode", mode.mode().name()); result.put("apiVersion", 2); result.put("checkpointVersion", AutoLlmCampaign.SCHEMA_VERSION);
        result.put("runnerProtocol", es.urjc.etsii.grafo.autoconfig.autollm.execution.WorkloadProtocol.VERSION);
        result.put("minimumJavaFeature", es.urjc.etsii.grafo.autoconfig.autollm.execution.TargetPreflightUtil.minimumJavaFeature());
        result.put("workspace", store.root().toString()); result.put("pid", ProcessHandle.current().pid());
        result.put("processStartedAt", ProcessHandle.current().info().startInstant().map(java.time.Instant::toString).orElse(null));
        result.put("workingDirectory", Path.of("").toAbsolutePath().toString());
        result.put("artifact", artifact == null ? null : artifact.getAbsolutePath());
        result.put("artifactSha256", artifactSha256);
        result.put("baseUrl", "http://127.0.0.1:" + environment.getProperty("local.server.port", environment.getProperty("server.port", "8080")));
        result.put("features", List.of("LOCAL", "SSH", "SCREENING_JOBS", "CODEX_PROGRESS", "BYTE_LOG_CURSORS", "STRUCTURED_FAILURES"));
        return result;
    }
    @org.springframework.context.event.EventListener(org.springframework.boot.context.event.ApplicationReadyEvent.class)
    public void saveSession() { AutoLlmFileUtil.write(store.root().resolve("session.json"), capabilities()); }
    @PostMapping("/effective-config") public Object effectiveConfig(@RequestBody AutoLlmConfig config) { return AutoLlmSummaryUtil.summarize(config, null, Map.of()); }
    @GetMapping("/campaigns/{id}/summary") public Object summary(@PathVariable String id) {
        var campaign = store.load(id); Path budgets = store.directory(id).resolve("control/comparison-time-limits.json");
        return AutoLlmSummaryUtil.summarize(campaign.config, campaign.executionHost, Files.exists(budgets) ? AutoLlmFileUtil.read(budgets, new tools.jackson.core.type.TypeReference<>() {}) : Map.of());
    }
    @GetMapping("/defaults") public AutoLlmConfig defaults() { return service.defaults(); }
    @PostMapping("/validate") public Map<String, Object> validate(@RequestBody AutoLlmConfig config) { var errors = service.validate(config); return Map.of("valid", errors.isEmpty(), "errors", errors); }
    @PostMapping("/targets/probe") public Object probe(@RequestBody AutoLlmConfig config) { requireMode(); return service.probeTarget(config); }
    @PostMapping("/campaigns/{id}/baseline-check") public Object baselineCheck(@PathVariable String id) { requireMode(); return service.baselineCheck(id); }
    @PostMapping("/campaigns/{id}/trials/{number}/search-space-check") public Object metadataCheck(@PathVariable String id, @PathVariable int number, @RequestBody Map<String,String> options) { requireMode(); return service.metadataCheck(id, number, options.get("blacklist")); }
    @GetMapping("/campaigns") public Map<String, Object> list() { var active = service.active(); return Map.of("active", active == null ? "" : active, "campaigns", store.list()); }
    @PostMapping("/campaigns") public AutoLlmCampaign create(@RequestBody AutoLlmConfig config) { requireMode(); return service.create(config); }
    @GetMapping("/campaigns/{id}") public AutoLlmCampaign campaign(@PathVariable String id) { return store.load(id); }
    @PostMapping("/campaigns/{id}/start") public AutoLlmCampaign start(@PathVariable String id) { requireMode(); return service.start(id, false); }
    @PostMapping("/campaigns/{id}/resume") public AutoLlmCampaign resume(@PathVariable String id) { requireMode(); return service.start(id, true); }
    @PostMapping("/campaigns/{id}/cancel") public AutoLlmCampaign cancel(@PathVariable String id) { requireMode(); return service.cancel(id); }
    @PostMapping("/campaigns/{id}/report") public AutoLlmCampaign report(@PathVariable String id) { requireMode(); return service.report(id); }
    @GetMapping("/campaigns/{id}/artifacts") public List<AutoLlmCampaignStore.Artifact> artifacts(@PathVariable String id) { store.load(id); return store.artifacts(id); }
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
    @GetMapping("/campaigns/{id}/trials/{number}/progress") public Object progress(@PathVariable String id, @PathVariable int number) {
        var campaign = store.load(id);
        for (var trial : campaign.trials) if (trial.number == number) {
            var progress = new LinkedHashMap<String,Object>(); progress.put("status", trial.status); progress.put("problem", trial.problem);
            var jobRetries = new HashMap<String,Integer>();
            for (Path file : AutoLlmFileUtil.ownedFiles(store.directory(id).resolve("trials/%03d".formatted(number)), "remote-status.json")) {
                var remote = AutoLlmFileUtil.read(file, Map.class); Object retries = remote.get("connectionRetries");
                if (remote.get("jobId") instanceof String job && retries instanceof Number count) jobRetries.merge(job, count.intValue(), Math::max);
            }
            int retries = trial.infrastructureRetries; for (int count : jobRetries.values()) retries += count;
            progress.put("infrastructureRetries", retries); progress.put("repairTurns", trial.repairTurns);
            if (!trial.attempts.isEmpty()) {
                var attempt = trial.attempts.getLast(); Path directory = store.directory(id).resolve(attempt.directory);
                progress.put("attempt", attempt); progress.put("substage", attempt.substage);
                progress.put("elapsedMillis", java.time.Duration.between(java.time.Instant.parse(attempt.startedAt), attempt.finishedAt == null ? java.time.Instant.now() : java.time.Instant.parse(attempt.finishedAt)).toMillis());
                Path remote = directory.resolve("remote-status.json");
                if (attempt.substage != null) remote = directory.resolve(switch (attempt.substage) { case "BUILD" -> "build"; case "METADATA" -> "check"; case "PROBES" -> "probe"; case "PILOT" -> "pilot"; default -> ""; }).resolve("remote-status.json");
                if (Files.exists(remote)) progress.put("remote", AutoLlmFileUtil.read(remote, Object.class));
                if (List.of("GENERATION", "REPAIR").contains(attempt.stage)) progress.put("generation", CodexProgressUtil.refresh(directory));
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
        for (var trial : store.load(id).trials) if (trial.number == number) for (var candidate : trial.attempts) if (candidate.number == attempt && List.of("GENERATION", "REPAIR").contains(candidate.stage)) {
            Path directory = store.directory(id).resolve(candidate.directory); CodexProgressUtil.refresh(directory);
            return AutoLlmLogUtil.read(directory.resolve("activity.jsonl"), cursor, 65536);
        }
        throw new NoSuchElementException("Generation attempt unavailable");
    }
    @GetMapping("/campaigns/{id}/trials/{number}/comparison") public Object comparison(@PathVariable String id, @PathVariable int number) {
        var campaign = store.load(id);
        for (var trial : campaign.trials) if (trial.number == number) {
            if (trial.comparisonDirectory == null) return Map.of("status", "NOT_STARTED");
            Path file = store.directory(id).resolve(trial.comparisonDirectory).resolve("execution/events.jsonl");
            if (!Files.exists(file)) return Map.of("status", trial.status);
            try {
                long size = Files.size(file), modified = Files.getLastModifiedTime(file).toMillis();
                var cached = comparisons.get(file); if (cached != null && cached.size == size && cached.modified == modified) return cached.value;
                var scores = new TreeMap<String, Map<String, es.urjc.etsii.grafo.util.StatisticsUtil.Accumulator>>();
                var counts = new TreeMap<String, Map<String, Integer>>();
                var curves = new ArrayList<Map<String,Object>>();
                var timing = new TreeMap<String, es.urjc.etsii.grafo.util.StatisticsUtil.Accumulator>();
                var timeToBest = new TreeMap<String, es.urjc.etsii.grafo.util.StatisticsUtil.Accumulator>();
                for (var event : AutoLlmFileUtil.events(file)) {
                    if (!"evaluation-result".equals(event.get("kind")) || !Boolean.TRUE.equals(event.get("measured"))) continue;
                    String algorithm = (String) event.get("algorithm");
                    counts.computeIfAbsent(algorithm, _ -> new TreeMap<>()).merge((String) event.get("state"), 1, Integer::sum);
                    if ("SUCCEEDED".equals(event.get("state"))) {
                        timing.computeIfAbsent(algorithm, _ -> new es.urjc.etsii.grafo.util.StatisticsUtil.Accumulator()).add(((Number) event.get("executionTimeNanos")).doubleValue() / 1_000_000);
                        timeToBest.computeIfAbsent(algorithm, _ -> new es.urjc.etsii.grafo.util.StatisticsUtil.Accumulator()).add(((Number) event.get("timeToBestNanos")).doubleValue() / 1_000_000);
                        curves.add(Map.of("instance", event.get("instance"), "instanceId", event.get("instanceId"), "algorithm", algorithm, "iteration", event.get("iteration"), "curves", event.get("curves")));
                    }
                    if ("SUCCEEDED".equals(event.get("state"))) for (var entry : AutoLlmFileUtil.objectMap(event.get("objectives")).entrySet()) {
                        if (entry.getValue() instanceof Number value) scores.computeIfAbsent(algorithm, _ -> new TreeMap<>()).computeIfAbsent(entry.getKey(), _ -> new es.urjc.etsii.grafo.util.StatisticsUtil.Accumulator()).add(value.doubleValue());
                    }
                }
                var summary = new TreeMap<String, Object>();
                for (var entry : scores.entrySet()) {
                    var objectives = new TreeMap<String, Object>();
                    for (var objective : entry.getValue().entrySet()) {
                        var definition = es.urjc.etsii.grafo.util.Context.getObjectivesW().get(objective.getKey());
                        var direction = definition == null ? es.urjc.etsii.grafo.algorithms.FMode.MINIMIZE : definition.getFMode();
                        objectives.put(objective.getKey(), objective.getValue().summary(direction, null));
                    }
                    summary.put(entry.getKey(), objectives);
                }
                var executionTimes = new TreeMap<String,Object>(); var bestTimes = new TreeMap<String,Object>();
                for (var entry : timing.entrySet()) executionTimes.put(entry.getKey(), entry.getValue().summary(es.urjc.etsii.grafo.algorithms.FMode.MINIMIZE, null));
                for (var entry : timeToBest.entrySet()) bestTimes.put(entry.getKey(), entry.getValue().summary(es.urjc.etsii.grafo.algorithms.FMode.MINIMIZE, null));
                Object value = Map.of("counts", counts, "objectives", summary, "executionTimeMillis", executionTimes, "timeToBestMillis", bestTimes, "curves", curves);
                comparisons.put(file, new CachedComparison(size, modified, value)); return value;
            } catch (IOException e) { throw new UncheckedIOException(e); }
        }
        throw new NoSuchElementException("Trial unavailable");
    }
    private void requireMode() { if (mode.mode() != MonitorModeState.Mode.AUTOLLM) throw new ResponseStatusException(HttpStatus.CONFLICT, "Launch Mork using --autollm"); }
    @ExceptionHandler(IllegalArgumentException.class) public ResponseEntity<Map<String,String>> invalid(IllegalArgumentException e) { return ResponseEntity.badRequest().body(Map.of("message", e.getMessage())); }
    @ExceptionHandler(IllegalStateException.class) public ResponseEntity<Map<String,String>> conflict(IllegalStateException e) { return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", e.getMessage())); }
    @ExceptionHandler(NoSuchElementException.class) public ResponseEntity<Map<String,String>> missing(NoSuchElementException e) { return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage())); }
}
