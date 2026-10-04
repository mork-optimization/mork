package es.urjc.etsii.grafo.autoconfig.controller;

import es.urjc.etsii.grafo.autoconfig.service.AutoconfigArtifactService;
import es.urjc.etsii.grafo.autoconfig.service.AutoconfigRunState;
import es.urjc.etsii.grafo.autoconfig.service.AutoconfigSearchSpace;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * Read-only REST API for observing the current autoconfig process.
 */
@RestController
@RequestMapping("/api/autoconfig")
public class AutoconfigController {

    private final AutoconfigRunState runState;
    private final AutoconfigSearchSpace searchSpace;
    private final AutoconfigArtifactService artifactService;

    public AutoconfigController(
            AutoconfigRunState runState,
            AutoconfigSearchSpace searchSpace,
            AutoconfigArtifactService artifactService
    ) {
        this.runState = runState;
        this.searchSpace = searchSpace;
        this.artifactService = artifactService;
    }

    @GetMapping("/status")
    public AutoconfigRunState.StatusSnapshot status() {
        return runState.status();
    }

    @GetMapping("/search-space")
    public AutoconfigSearchSpace.SearchSpaceSnapshot searchSpace() {
        if (!runState.hasGeneratedSearchSpace()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Automatic search space is not available for the current run"
            );
        }
        return searchSpace.snapshot();
    }

    @GetMapping("/elites")
    public AutoconfigRunState.EliteSnapshot elites() {
        return runState.eliteSnapshot();
    }

    @GetMapping("/elites/history")
    public AutoconfigRunState.EliteHistorySnapshot eliteHistory() {
        return runState.eliteHistory();
    }

    @GetMapping("/evaluations")
    public AutoconfigRunState.EvaluationPage evaluations(
            @RequestParam(required = false) Long after,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) AutoconfigRunState.EvaluationState state
    ) {
        return runState.evaluations(after, limit, state);
    }

    @GetMapping("/evaluations/changes")
    public AutoconfigRunState.EvaluationChangePage evaluationChanges(
            @RequestParam(required = false) Long after,
            @RequestParam(required = false) Integer limit
    ) {
        return runState.evaluationChanges(after, limit);
    }

    @GetMapping("/candidates/{configurationId}")
    public AutoconfigRunState.CandidateView candidate(@PathVariable String configurationId) {
        var candidate = runState.candidate(configurationId);
        if (candidate == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Unknown configuration " + configurationId
            );
        }
        return candidate;
    }

    @GetMapping("/artifacts")
    public AutoconfigArtifactService.ArtifactManifest artifacts() {
        var status = runState.status();
        if (status.role() != AutoconfigRunState.Role.COORDINATOR) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No coordinator artifacts are available");
        }
        var manifest = artifactService.manifest(status.runId());
        if (manifest == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No artifacts are available for the current run");
        }
        return manifest;
    }

    @GetMapping("/artifacts/{artifactId}")
    public ResponseEntity<FileSystemResource> artifact(@PathVariable String artifactId) {
        var status = runState.status();
        if (status.role() != AutoconfigRunState.Role.COORDINATOR) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No coordinator artifacts are available");
        }
        var artifact = artifactService.download(status.runId(), artifactId);
        if (artifact == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Artifact is not available: " + artifactId);
        }
        var disposition = ContentDisposition.attachment().filename(artifact.filename()).build();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(artifact.mediaType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(new FileSystemResource(artifact.path()));
    }
}
