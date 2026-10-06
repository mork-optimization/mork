package es.urjc.etsii.grafo.restcontroller;

import es.urjc.etsii.grafo.io.Instance;
import es.urjc.etsii.grafo.solution.Solution;
import es.urjc.etsii.grafo.monitoring.NormalExperimentRunState;
import es.urjc.etsii.grafo.monitoring.MonitoringArtifactRegistry;
import es.urjc.etsii.grafo.util.MonitoringExportUtil;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
@RequestMapping("/api/experiments")
public class NormalExperimentController<S extends Solution<S, I>, I extends Instance> {
    private final NormalExperimentRunState<S, I> state;
    private final MonitoringArtifactRegistry artifacts;
    public NormalExperimentController(NormalExperimentRunState<S, I> state, MonitoringArtifactRegistry artifacts) {
        this.state = state; this.artifacts = artifacts;
    }
    @GetMapping("/status")
    public NormalExperimentRunState.Status status() { return state.status(); }

    @GetMapping("/instances")
    public NormalExperimentRunState.Page<NormalExperimentRunState.InstanceSummary> instances(
            @RequestParam String runId, @RequestParam(required = false) String experiment,
            @RequestParam(required = false) String search, @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "25") int limit) { return state.instances(runId, experiment, search, offset, limit); }

    @GetMapping("/instances/{id}")
    public NormalExperimentRunState.InstanceDetail instance(@RequestParam String runId, @PathVariable String id, @RequestParam String objective) {
        return state.instance(runId, id, objective);
    }
    @GetMapping("/results")
    public NormalExperimentRunState.Page<NormalExperimentRunState.ResultView> results(
            @RequestParam String runId, @RequestParam(required = false) String instanceId,
            @RequestParam(required = false) String algorithm, @RequestParam(required = false) NormalExperimentRunState.WorkState state,
            @RequestParam(required = false) String search, @RequestParam(defaultValue = "PLANNED") NormalExperimentRunState.ResultOrder order,
            @RequestParam(defaultValue = "0") int offset, @RequestParam(defaultValue = "25") int limit) {
        return this.state.results(runId, instanceId, algorithm, state, search, order, offset, limit);
    }
    @GetMapping("/results/{id}")
    public NormalExperimentRunState.ResultDetail result(@RequestParam String runId, @PathVariable UUID id) { return state.result(runId, id); }

    @GetMapping("/issues")
    public NormalExperimentRunState.Page<NormalExperimentRunState.Issue> issues(@RequestParam String runId,
            @RequestParam(required = false) String severity, @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "25") int limit) { return state.issues(runId, severity, offset, limit); }

    public record Manifest(String runId, List<MonitoringArtifactRegistry.Artifact> artifacts) {}
    @GetMapping("/artifacts")
    public Manifest artifacts(@RequestParam String runId) { state.requireRun(runId); return new Manifest(runId, artifacts.manifest()); }

    @GetMapping("/artifacts/{id}")
    public ResponseEntity<FileSystemResource> download(@RequestParam String runId, @PathVariable String id) {
        state.requireRun(runId);
        var path = artifacts.available(id);
        if (path == null) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Artifact is unavailable");
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment().filename(path.getFileName().toString()).build().toString())
                .body(new FileSystemResource(path));
    }

    @GetMapping("/results/export")
    public ResponseEntity<String> export(@RequestParam String runId, @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false) String instanceId, @RequestParam(required = false) String algorithm,
            @RequestParam(required = false) NormalExperimentRunState.WorkState state, @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "PLANNED") NormalExperimentRunState.ResultOrder order) {
        if (!format.equals("csv") && !format.equals("json")) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Format must be csv or json");
        List<NormalExperimentRunState.ResultView> rows;
        long revision;
        synchronized (this.state) {
            this.state.requireRun(runId);
            revision = this.state.status().revision();
            rows = this.state.exportResults(runId, instanceId, algorithm, state, search, order);
        }
        String body = format.equals("csv") ? MonitoringExportUtil.csv(rows)
                : new tools.jackson.databind.json.JsonMapper().writeValueAsString(new Export(runId, revision, rows));
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(format.equals("csv") ? "text/csv;charset=UTF-8" : "application/json"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=results." + format).body(body);
    }
    private record Export(String runId, long revision, List<NormalExperimentRunState.ResultView> results) {}
}
