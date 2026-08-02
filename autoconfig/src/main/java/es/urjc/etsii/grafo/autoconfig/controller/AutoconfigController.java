package es.urjc.etsii.grafo.autoconfig.controller;

import es.urjc.etsii.grafo.autoconfig.service.AutoconfigRunState;
import es.urjc.etsii.grafo.autoconfig.service.AutoconfigSearchSpaceService;
import org.springframework.http.HttpStatus;
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
    private final AutoconfigSearchSpaceService searchSpace;

    public AutoconfigController(
            AutoconfigRunState runState,
            AutoconfigSearchSpaceService searchSpace
    ) {
        this.runState = runState;
        this.searchSpace = searchSpace;
    }

    @GetMapping("/status")
    public AutoconfigRunState.StatusSnapshot status() {
        return runState.status();
    }

    @GetMapping("/search-space")
    public AutoconfigSearchSpaceService.SearchSpaceSnapshot searchSpace() {
        return searchSpace.getSnapshot();
    }

    @GetMapping("/elites")
    public AutoconfigRunState.EliteSnapshot elites() {
        return runState.eliteSnapshot();
    }

    @GetMapping("/evaluations")
    public AutoconfigRunState.EvaluationPage evaluations(
            @RequestParam(required = false) Long after,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) AutoconfigRunState.EvaluationState state,
            @RequestParam(required = false) String configurationId,
            @RequestParam(required = false) Boolean slow
    ) {
        return runState.evaluations(after, limit, state, configurationId, slow);
    }

    @GetMapping("/evaluations/{evaluationId}")
    public AutoconfigRunState.EvaluationDetail evaluation(@PathVariable long evaluationId) {
        var evaluation = runState.evaluation(evaluationId);
        if (evaluation == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Unknown or no longer retained evaluation " + evaluationId
            );
        }
        return evaluation;
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
}
