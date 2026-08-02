package es.urjc.etsii.grafo.autoconfig.controller;

import es.urjc.etsii.grafo.autoconfig.service.AutoconfigRunState;
import es.urjc.etsii.grafo.autoconfig.service.AutoconfigSearchSpaceService;
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
        var searchSpace = mock(AutoconfigSearchSpaceService.class);
        when(runState.status()).thenReturn(new AutoconfigRunState.StatusSnapshot(
                null,
                AutoconfigRunState.Role.DISABLED,
                AutoconfigRunState.RunStatus.NOT_STARTED,
                null,
                null,
                null,
                null,
                new AutoconfigRunState.BudgetSnapshot(0, 0, 0),
                new AutoconfigRunState.EvaluationCounts(0, 0, 0, 0, 0, 0),
                0,
                new AutoconfigRunState.IraceProgress(null, 0, null, false),
                null
        ));
        when(searchSpace.getSnapshot()).thenReturn(
                new AutoconfigSearchSpaceService.SearchSpaceSnapshot(
                        new AutoconfigSearchSpaceService.GenerationLimits(4, 2),
                        new AutoconfigSearchSpaceService.SearchSpaceSummary(0, 0, 0, 0, 0),
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
    void exposesStableStatusAndSearchSpaceResources() throws Exception {
        mockMvc.perform(get("/api/autoconfig/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("DISABLED"))
                .andExpect(jsonPath("$.state").value("NOT_STARTED"))
                .andExpect(jsonPath("$.budget.used").value(0));

        mockMvc.perform(get("/api/autoconfig/search-space"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.limits.treeDepth").value(4))
                .andExpect(jsonPath("$.roots").isArray());
    }

    @Test
    void doesNotExposeRemovedDebugOrExpandedTreeEndpoints() throws Exception {
        mockMvc.perform(get("/auto/debug/decode/ROOT=A"))
                .andExpect(status().isNotFound());
        mockMvc.perform(get("/api/autoconfig/search-space/tree"))
                .andExpect(status().isNotFound());
    }

    @Test
    void missingEvaluationUsesProblemResponse() throws Exception {
        when(runState.evaluation(99)).thenReturn(null);

        mockMvc.perform(get("/api/autoconfig/evaluations/99"))
                .andExpect(status().isNotFound());
    }
}
