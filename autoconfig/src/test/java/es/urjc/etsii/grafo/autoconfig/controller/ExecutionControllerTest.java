package es.urjc.etsii.grafo.autoconfig.controller;

import es.urjc.etsii.grafo.autoconfig.controller.dto.ExecuteResponse;
import es.urjc.etsii.grafo.autoconfig.controller.dto.EliteConfiguration;
import es.urjc.etsii.grafo.autoconfig.irace.IraceOrchestrator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ExecutionControllerTest {

    private static final String EXPERIMENT = """
            {
              "id_configuration": "12",
              "id_instance": 1,
              "instance": "instance.dat",
              "seed": 123,
              "configuration": {"ROOT": "TestAlgorithm"}
            }
            """;

    private MockMvc mockMvc;
    private IraceOrchestrator<?, ?> orchestrator;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setup() {
        orchestrator = (IraceOrchestrator<?, ?>) mock(IraceOrchestrator.class);
        when(orchestrator.getIntegrationKey()).thenReturn("secret");
        when(orchestrator.iraceMultiCallback(anyList(), anyBoolean()))
                .thenReturn(List.of(new ExecuteResponse(4.5, 0.2)));
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ExecutionController<>(orchestrator))
                .setControllerAdvice(new AutoconfigApiExceptionHandler())
                .build();
    }

    @Test
    void exposesOnlyNamespacedBatchEndpoint() throws Exception {
        String request = """
                {
                  "key": "secret",
                  "experiments": [%s]
                }
                """.formatted(EXPERIMENT);

        mockMvc.perform(post("/internal/autoconfig/irace/evaluations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].cost").value(4.5));
        verify(orchestrator).iraceMultiCallback(anyList(), eq(true));

        mockMvc.perform(post("/batchExecute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isNotFound());

        mockMvc.perform(post("/execute")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsInvalidAuthenticationAndMalformedBatches() throws Exception {
        String invalidKey = """
                {
                  "key": "wrong",
                  "experiments": [%s]
                }
                """.formatted(EXPERIMENT);

        mockMvc.perform(post("/internal/autoconfig/irace/evaluations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidKey))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/internal/autoconfig/irace/evaluations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"key\":\"secret\"}"))
                .andExpect(status().isBadRequest());

        verify(orchestrator, never()).iraceMultiCallback(anyList(), anyBoolean());
    }

    @Test
    void executesPreflightBatchesWithoutRecordingEvaluations() throws Exception {
        String request = """
                {
                  "key": "secret",
                  "preflight": true,
                  "experiments": [%s]
                }
                """.formatted(EXPERIMENT);

        mockMvc.perform(post("/internal/autoconfig/irace/evaluations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isOk());

        verify(orchestrator).iraceMultiCallback(anyList(), eq(false));
    }

    @Test
    void acceptsAuthenticatedIterationProgress() throws Exception {
        String request = """
                {
                  "key": "secret",
                  "runId": "run-1",
                  "iteration": 2,
                  "elites": [
                    {
                      "configurationId": "12",
                      "parameters": {"ROOT": "TestAlgorithm"}
                    }
                  ]
                }
                """;

        mockMvc.perform(post("/internal/autoconfig/irace/progress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isNoContent());

        verify(orchestrator).iraceProgressCallback(
                "run-1",
                2,
                List.of(new EliteConfiguration(
                        "12",
                        Map.of("ROOT", "TestAlgorithm")
                ))
        );
    }
}
