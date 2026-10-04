package es.urjc.etsii.grafo.autoconfig.controller;

import es.urjc.etsii.grafo.autoconfig.controller.dto.IraceProgressDetails;
import es.urjc.etsii.grafo.autoconfig.service.AutoconfigArtifactService;
import es.urjc.etsii.grafo.autoconfig.service.AutoconfigRunState;
import es.urjc.etsii.grafo.autoconfig.service.AutoconfigSearchSpace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AutoconfigControllerTest {

    private AutoconfigRunState runState;
    private AutoconfigArtifactService artifactService;
    private MockMvc mockMvc;

    @TempDir
    Path temporaryDirectory;

    @BeforeEach
    void setup() {
        runState = mock(AutoconfigRunState.class);
        artifactService = mock(AutoconfigArtifactService.class);
        var searchSpace = mock(AutoconfigSearchSpace.class);
        when(runState.status()).thenReturn(new AutoconfigRunState.StatusSnapshot(
                null,
                AutoconfigRunState.RunMode.DISABLED,
                AutoconfigRunState.Role.DISABLED,
                AutoconfigRunState.RunStatus.NOT_STARTED,
                AutoconfigRunState.RunPhase.NOT_STARTED,
                null,
                null,
                null,
                null,
                new AutoconfigRunState.BudgetSnapshot(0, 0, 0),
                new AutoconfigRunState.EvaluationCounts(0, 0, 0, 0, 0),
                0,
                0,
                null,
                null,
                new AutoconfigRunState.IraceProgress(null, 0, null, false, null),
                null
        ));
        when(searchSpace.snapshot()).thenReturn(
                new AutoconfigSearchSpace.SearchSpaceSnapshot(
                        new AutoconfigSearchSpace.GenerationLimits(4, 2),
                        new AutoconfigSearchSpace.SearchSpaceSummary(0, 0, 0, 0, 0, 0),
                        List.of(),
                        List.of(),
                        null
                )
        );
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AutoconfigController(
                        runState,
                        searchSpace,
                        artifactService
                ))
                .setControllerAdvice(new AutoconfigApiExceptionHandler())
                .build();
    }

    @Test
    void exposesStableStatusAndHidesUnpublishedSearchSpace() throws Exception {
        mockMvc.perform(get("/api/autoconfig/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mode").value("DISABLED"))
                .andExpect(jsonPath("$.role").value("DISABLED"))
                .andExpect(jsonPath("$.state").value("NOT_STARTED"))
                .andExpect(jsonPath("$.phase").value("NOT_STARTED"))
                .andExpect(jsonPath("$.budget.used").value(0))
                .andExpect(jsonPath("$.evaluations.total").doesNotExist())
                .andExpect(jsonPath("$.irace.progress").doesNotExist());

        mockMvc.perform(get("/api/autoconfig/search-space"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value(
                        "Automatic search space is not available for the current run"
                ));
    }

    @Test
    void exposesPublishedSearchSpace() throws Exception {
        when(runState.hasGeneratedSearchSpace()).thenReturn(true);

        mockMvc.perform(get("/api/autoconfig/search-space"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.limits.treeDepth").value(4))
                .andExpect(jsonPath("$.roots").isArray());
    }

    @Test
    void exposesMorkAndIraceBudgetsWithoutConsistencyFlags() throws Exception {
        var iraceProgress = new IraceProgressDetails(
                8, 100, 29, 71, false, 12, 10,
                0, 0, null, null
        );
        when(runState.status()).thenReturn(new AutoconfigRunState.StatusSnapshot(
                "run-1",
                AutoconfigRunState.RunMode.AUTOCONFIG,
                AutoconfigRunState.Role.COORDINATOR,
                AutoconfigRunState.RunStatus.RUNNING,
                AutoconfigRunState.RunPhase.RACING,
                null,
                null,
                null,
                0L,
                new AutoconfigRunState.BudgetSnapshot(100, 30, 70),
                new AutoconfigRunState.EvaluationCounts(0, 30, 0, 0, 0),
                60,
                5,
                32,
                null,
                new AutoconfigRunState.IraceProgress(2, 1, null, false, iraceProgress),
                null
        ));

        mockMvc.perform(get("/api/autoconfig/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.budget.used").value(30))
                .andExpect(jsonPath("$.trainingInstanceCount").value(32))
                .andExpect(jsonPath("$.irace.progress.experimentsUsed").value(29))
                .andExpect(jsonPath("$.irace.consistent").doesNotExist());
    }

    @Test
    void doesNotExposeRemovedDebugOrExpandedTreeEndpoints() throws Exception {
        mockMvc.perform(get("/auto/debug/decode/ROOT=A"))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/autoconfig/search-space/tree"))
                .andExpect(status().isNotFound());
    }

    @Test
    void doesNotExposeEvaluationDetailEndpoint() throws Exception {
        mockMvc.perform(get("/api/autoconfig/evaluations/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void filtersEvaluationPagesByState() throws Exception {
        var rejected = new AutoconfigRunState.EvaluationView(
                7, "42", "instance-1", "instance.dat", 123,
                AutoconfigRunState.EvaluationState.REJECTED,
                null, null, null, null,
                "INVALID_SOLUTION", "Invalid tour", false, null
        );
        when(runState.evaluations(5L, 10, AutoconfigRunState.EvaluationState.REJECTED))
                .thenReturn(new AutoconfigRunState.EvaluationPage(7, 7, List.of(rejected)));

        mockMvc.perform(get("/api/autoconfig/evaluations")
                        .param("after", "5")
                        .param("limit", "10")
                        .param("state", "REJECTED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.evaluations[0].state").value("REJECTED"))
                .andExpect(jsonPath("$.evaluations[0].reasonCode").value("INVALID_SOLUTION"))
                .andExpect(jsonPath("$.nextCursor").value(7));
        verify(runState).evaluations(5L, 10, AutoconfigRunState.EvaluationState.REJECTED);
    }

    @Test
    void exposesRevisionedEvaluationChanges() throws Exception {
        var evaluation = new AutoconfigRunState.EvaluationView(
                7, "42", "instance-1", "instance.dat", 123,
                AutoconfigRunState.EvaluationState.SUCCEEDED,
                1.5, 0.2, null, null,
                null, null, false, null
        );
        when(runState.evaluationChanges(4L, 10)).thenReturn(
                new AutoconfigRunState.EvaluationChangePage(
                        "run-1",
                        5,
                        5,
                        List.of(new AutoconfigRunState.EvaluationChange(5, evaluation))
                )
        );

        mockMvc.perform(get("/api/autoconfig/evaluations/changes")
                        .param("after", "4")
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.runId").value("run-1"))
                .andExpect(jsonPath("$.latestRevision").value(5))
                .andExpect(jsonPath("$.changes[0].evaluation.configurationId").value("42"));
        verify(runState).evaluationChanges(4L, 10);
    }

    @Test
    void hidesArtifactsOutsideACoordinatorRun() throws Exception {
        mockMvc.perform(get("/api/autoconfig/artifacts"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("No coordinator artifacts are available"));
    }

    @Test
    void streamsWhitelistedCoordinatorArtifactsAsAttachments() throws Exception {
        var artifactPath = temporaryDirectory.resolve("plots.pdf");
        Files.writeString(artifactPath, "pdf-data");
        when(runState.status()).thenReturn(new AutoconfigRunState.StatusSnapshot(
                "run-1",
                AutoconfigRunState.RunMode.AUTOCONFIG,
                AutoconfigRunState.Role.COORDINATOR,
                AutoconfigRunState.RunStatus.COMPLETED,
                AutoconfigRunState.RunPhase.COMPLETED,
                null, null, null, null,
                new AutoconfigRunState.BudgetSnapshot(10, 10, 0),
                new AutoconfigRunState.EvaluationCounts(0, 10, 0, 0, 0),
                20, 1, null, null,
                new AutoconfigRunState.IraceProgress(1, 1, null, true, null),
                null
        ));
        when(artifactService.download("run-1", "plots")).thenReturn(
                new AutoconfigArtifactService.ArtifactDownload("plots.pdf", "application/pdf", artifactPath)
        );

        mockMvc.perform(get("/api/autoconfig/artifacts/plots"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"plots.pdf\""))
                .andExpect(content().contentType("application/pdf"))
                .andExpect(content().string("pdf-data"));
    }
}
