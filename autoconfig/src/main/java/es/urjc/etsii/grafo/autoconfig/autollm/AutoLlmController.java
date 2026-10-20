package es.urjc.etsii.grafo.autoconfig.autollm;

import es.urjc.etsii.grafo.monitoring.MonitorModeState;
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
    private final Map<Path, CachedComparison> comparisons = new java.util.concurrent.ConcurrentHashMap<>();
    private record CachedComparison(long size, long modified, Object value) {}
    public AutoLlmController(AutoLlmCampaignService service, AutoLlmCampaignStore store, MonitorModeState mode) { this.service = service; this.store = store; this.mode = mode; }
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
    @GetMapping("/campaigns/{id}/log") public Map<String,Object> log(@PathVariable String id, @RequestParam(defaultValue = "campaign-events.jsonl") String path,
            @RequestParam(defaultValue = "0") int offset, @RequestParam(defaultValue = "100") int limit) {
        Path file = store.artifact(id, path);
        if (!(path.endsWith(".log") || path.endsWith(".jsonl") || path.endsWith(".txt"))) throw new IllegalArgumentException("Select a text log");
        if (offset < 0 || limit < 1 || limit > 500) throw new IllegalArgumentException("Invalid page");
        var lines = new ArrayList<String>();
        try (var input = Files.newBufferedReader(file)) {
            for (int i = 0; i < offset; i++) if (input.readLine() == null) break;
            String line; while (lines.size() < limit && (line = input.readLine()) != null) lines.add(line);
            return Map.of("offset", offset, "nextOffset", offset + lines.size(), "lines", lines);
        } catch (IOException e) { throw new UncheckedIOException(e); }
    }
    @GetMapping("/campaigns/{id}/trials/{number}/progress") public Object progress(@PathVariable String id, @PathVariable int number) {
        var campaign = store.load(id);
        for (var trial : campaign.trials) if (trial.number == number) {
            for (int i = trial.attempts.size() - 1; i >= 0; i--) {
                Path directory = store.directory(id).resolve(trial.attempts.get(i).directory);
                for (String name : List.of("execution/tuning-progress.json", "pilot/execution/tuning-progress.json", "execution/tuning-status.json", "probe/execution/probes.json")) {
                    if (Files.exists(directory.resolve(name))) return AutoLlmFileUtil.read(directory.resolve(name), Object.class);
                }
            }
            return Map.of("status", trial.status);
        }
        throw new NoSuchElementException("Trial unavailable");
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
