package es.urjc.etsii.grafo.autoconfig.controller;

import es.urjc.etsii.grafo.autoconfig.controller.dto.IraceProgressDetails;
import es.urjc.etsii.grafo.autoconfig.service.AutoconfigRunState;
import es.urjc.etsii.grafo.autoconfig.service.AutoconfigSearchSpace;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AutoconfigControllerTest {

    private AutoconfigRunState runState;
    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        runState = mock(AutoconfigRunState.class);
        var searchSpace = mock(AutoconfigSearchSpace.class);
        when(runState.status()).thenReturn(new AutoconfigRunState.StatusSnapshot(
                null,
                AutoconfigRunState.Role.DISABLED,
                AutoconfigRunState.RunStatus.NOT_STARTED,
                null,
                null,
                null,
                null,
                new AutoconfigRunState.BudgetSnapshot(0, 0, 0),
                new AutoconfigRunState.EvaluationCounts(0, 0, 0, 0, 0),
                0,
                new AutoconfigRunState.IraceProgress(null, 0, null, false, null),
                null
        ));
        when(searchSpace.snapshot()).thenReturn(
                new AutoconfigSearchSpace.SearchSpaceSnapshot(
                        new AutoconfigSearchSpace.GenerationLimits(4, 2),
                        new AutoconfigSearchSpace.SearchSpaceSummary(0, 0, 0, 0, 0),
                        List.of(),
                        List.of()
                )
        );
        mockMvc = MockMvcBuilders
                .standaloneSetup(new AutoconfigController(runState, searchSpace))
                .setControllerAdvice(new AutoconfigApiExceptionHandler())
                .build();
    }

    @Test
    void exposesStableStatusAndHidesUnpublishedSearchSpace() throws Exception {
        mockMvc.perform(get("/api/autoconfig/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("DISABLED"))
                .andExpect(jsonPath("$.state").value("NOT_STARTED"))
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
                AutoconfigRunState.Role.COORDINATOR,
                AutoconfigRunState.RunStatus.RUNNING,
                null,
                null,
                null,
                0L,
                new AutoconfigRunState.BudgetSnapshot(100, 30, 70),
                new AutoconfigRunState.EvaluationCounts(0, 30, 0, 0, 0),
                5,
                new AutoconfigRunState.IraceProgress(2, 1, null, false, iraceProgress),
                null
        ));

        mockMvc.perform(get("/api/autoconfig/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.budget.used").value(30))
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
}
