package es.urjc.etsii.grafo.autoconfig.controller;

import es.urjc.etsii.grafo.autoconfig.controller.dto.ExecuteResponse;
import es.urjc.etsii.grafo.autoconfig.controller.dto.EliteConfiguration;
import es.urjc.etsii.grafo.autoconfig.controller.dto.IraceProgressDetails;
import es.urjc.etsii.grafo.autoconfig.irace.IraceOrchestrator;
import es.urjc.etsii.grafo.autoconfig.irace.IraceTargetEvaluator;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestSolution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

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
    private IraceOrchestrator<TestSolution, TestInstance> orchestrator;
    private IraceTargetEvaluator<TestSolution, TestInstance> targetEvaluator;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setup() {
        orchestrator = (IraceOrchestrator<TestSolution, TestInstance>) mock(IraceOrchestrator.class);
        targetEvaluator = (IraceTargetEvaluator<TestSolution, TestInstance>) mock(IraceTargetEvaluator.class);
        when(orchestrator.getIntegrationKey()).thenReturn("secret");
        when(targetEvaluator.evaluateBatch(anyList()))
                .thenReturn(List.of(new ExecuteResponse(4.5, 0.2)));
        when(targetEvaluator.preflightBatch(anyList()))
                .thenReturn(List.of(new ExecuteResponse(4.5, 0.2)));
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ExecutionController<>(orchestrator, targetEvaluator))
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
        verify(targetEvaluator).evaluateBatch(anyList());

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

        verify(targetEvaluator, never()).evaluateBatch(anyList());
        verify(targetEvaluator, never()).preflightBatch(anyList());
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

        verify(targetEvaluator).preflightBatch(anyList());
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
                  ],
                  "progress": {
                    "nbIterations": 8,
                    "maxExperiments": 100,
                    "experimentsUsed": 30,
                    "remainingBudget": 70,
                    "remainingBudgetEstimated": false,
                    "currentBudget": 12,
                    "currentBudgetUsed": 10,
                    "maxTime": 0,
                    "timeUsed": 0,
                    "futureCounter": 123
                  }
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
                )),
                new IraceProgressDetails(
                        8, 100, 30, 70, false, 12, 10,
                        0, 0, null, null
                )
        );
    }

    @Test
    void rejectsProgressWithoutIraceDetails() throws Exception {
        String request = """
                {
                  "key": "secret",
                  "runId": "run-1",
                  "iteration": 2,
                  "elites": []
                }
                """;

        mockMvc.perform(post("/internal/autoconfig/irace/progress")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isBadRequest());
    }
}
