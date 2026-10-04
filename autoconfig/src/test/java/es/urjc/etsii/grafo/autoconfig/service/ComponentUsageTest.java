package es.urjc.etsii.grafo.autoconfig.service;

import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.autoconfig.controller.AutoconfigApiExceptionHandler;
import es.urjc.etsii.grafo.autoconfig.controller.AutoconfigController;
import es.urjc.etsii.grafo.autoconfig.controller.dto.EliteConfiguration;
import es.urjc.etsii.grafo.autoconfig.controller.dto.IraceProgressDetails;
import es.urjc.etsii.grafo.autoconfig.irace.AlgorithmConfiguration;
import es.urjc.etsii.grafo.autoconfig.irace.AutomaticAlgorithmBuilder;
import es.urjc.etsii.grafo.autoconfig.irace.IraceRuntimeConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Map;

import static es.urjc.etsii.grafo.autoconfig.service.ComponentUsage.Basis.*;
import static es.urjc.etsii.grafo.autoconfig.service.ComponentUsage.Scope.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ComponentUsageTest {
    private static final JsonMapper MAPPER = JsonMapper.builder().build();
    private static final String TREE = """
            {"$component":"Root", "iterations":10,
             "improvers":[{"$component":"Local", "neighborhood":{"$component":"Move"}},
                          {"$component":"Local", "neighborhood":{"$component":"Move"}}],
             "fallback":{"$component":"Move"}}
            """;

    @Test
    void preservesMultiplicityRolesAndOnlyDirectEdges() {
        var footprint = ComponentUsageUtil.analyze(MAPPER.readTree(TREE));
        assertEquals("Root", footprint.root());
        assertEquals(Map.of("Root", 1L, "Local", 2L, "Move", 3L), footprint.components());
        assertEquals(Map.of(
                new ComponentUsage.RelationshipKey("Root", "improvers[]", "Local"), 2L,
                new ComponentUsage.RelationshipKey("Local", "neighborhood", "Move"), 2L,
                new ComponentUsage.RelationshipKey("Root", "fallback", "Move"), 1L), footprint.relationships());
        assertThrows(IllegalArgumentException.class, () -> ComponentUsageUtil.analyze(MAPPER.readTree("{}")));
        var mixed = ComponentUsageUtil.analyze(MAPPER.readTree("""
                {"$component":"A","items":[null,1,"text",[{"$component":"B"}]]}
                """));
        assertEquals(1L, mixed.relationships().get(new ComponentUsage.RelationshipKey("A", "items[][]", "B")));
    }

    @Test
    void countsUniqueTreesOnceAndEveryAcceptedEvaluationRegardlessOfOutcome() {
        var builder = builder();
        var state = state(builder);
        var first = state.evaluationStarted(configuration("1", "repeated"));
        var second = state.evaluationStarted(configuration("1", "repeated"));
        var third = state.evaluationStarted(configuration("1", "repeated"));
        state.evaluationSucceeded(first, 1, .2, 0);
        state.evaluationRejected(second, "INVALID", "invalid", 0);
        state.evaluationFailed(third, new IllegalStateException("failed"));
        var snapshot = state.componentUsage(ALL);
        assertEquals(1, snapshot.configurationCount());
        assertEquals(3, snapshot.evaluationCount());
        assertEquals(6, snapshot.candidatePlacementCount());
        assertEquals(18, snapshot.evaluationPlacementCount());
        assertEquals(6, snapshot.latestEvaluationRevision());
        var local = component(snapshot, "Local");
        assertEquals(2, local.candidatePlacements());
        assertEquals(1, local.configurationCount());
        assertEquals(6, local.evaluationPlacements());
        assertEquals(1, component(snapshot, "Root").rootConfigurationCount());
        assertEquals(3, component(snapshot, "Root").rootEvaluationPlacements());
        verify(builder, times(1)).asJsonTree(any());
        var frozen = snapshot;
        state.evaluationStarted(configuration("2", "simple"));
        assertEquals(1, frozen.configurationCount());
        assertEquals(2, state.componentUsage(ALL).configurationCount());
    }

    @Test
    void scopesToCurrentElitesIncludingUnevaluatedTreesAndClearsOnReset() {
        var state = state(builder());
        state.evaluationStarted(configuration("1", "repeated"));
        state.evaluationStarted(configuration("1", "repeated"));
        state.publishProgress(state.getRunId(), 1, List.of(elite("1", "repeated"), elite("2", "simple")), progress());
        var eliteSnapshot = state.componentUsage(CURRENT_ELITES);
        assertEquals(2, eliteSnapshot.configurationCount());
        assertEquals(7, eliteSnapshot.candidatePlacementCount());
        assertEquals(12, eliteSnapshot.evaluationPlacementCount());
        assertEquals(1, state.componentUsage(ALL).configurationCount());
        assertNotNull(eliteSnapshot.eliteUpdatedAt());
        state.publishProgress(state.getRunId(), 2, List.of(elite("2", "simple")), progress());
        assertEquals(1, state.componentUsage(CURRENT_ELITES).candidatePlacementCount());
        assertEquals(0, state.componentUsage(CURRENT_ELITES).evaluationPlacementCount());
        state.evaluationStarted(configuration("2", "simple"));
        assertEquals(1, state.componentUsage(CURRENT_ELITES).evaluationPlacementCount());
        state.publishFinalElites(state.getRunId(), List.of(elite("1", "repeated")));
        assertEquals(12, state.componentUsage(CURRENT_ELITES).evaluationPlacementCount());
        state.prepareCoordinator(AutoconfigRunState.RunMode.AUTOCONFIG, metric());
        assertEquals(0, state.componentUsage(ALL).configurationCount());
        assertTrue(state.componentUsage(CURRENT_ELITES).components().isEmpty());
    }

    @Test
    void unavailableTreesDoNotInterruptEvaluationAndAreExcludedFromPlacements() {
        var builder = builder();
        doThrow(new IllegalArgumentException("cannot decode")).when(builder).asJsonTree(any());
        var state = state(builder);
        state.evaluationStarted(configuration("1", "broken"));
        var snapshot = state.componentUsage(ALL);
        assertEquals(1, snapshot.unavailableConfigurationCount());
        assertEquals(1, snapshot.evaluationCount());
        assertEquals(0, snapshot.candidatePlacementCount());
        doReturn(MAPPER.readTree("{}")).when(builder).asJsonTree(any());
        state.evaluationStarted(configuration("2", "malformed"));
        assertEquals(2, state.componentUsage(ALL).unavailableConfigurationCount());
        assertEquals(2, state.status().budget().used());
    }

    @Test
    void pagesExactComponentAndRoleSelectorsWithMetricOrderingAndEliteRanks() {
        var state = state(builder());
        state.evaluationStarted(configuration("z", "repeated"));
        state.evaluationStarted(configuration("a", "simple"));
        for (int i = 0; i < 8; i++) state.evaluationStarted(configuration("a", "simple"));
        state.publishProgress(state.getRunId(), 1, List.of(elite("z", "repeated"), elite("only-elite", "simple")), progress());
        var structure = state.componentCandidates(ALL, CANDIDATE, "Move", null, null, null, 0, 1);
        assertEquals(2, structure.total());
        assertEquals(1, structure.nextOffset());
        assertEquals("z", structure.candidates().getFirst().configurationId());
        assertEquals(3, structure.candidates().getFirst().occurrences());
        assertEquals(1, structure.candidates().getFirst().elitePosition());
        var exposure = state.componentCandidates(ALL, EVALUATION, "Move", null, null, null, 0, 1);
        assertEquals("a", exposure.candidates().getFirst().configurationId());
        assertEquals(9, exposure.candidates().getFirst().evaluationPlacements());
        var last = state.componentCandidates(ALL, CANDIDATE, "Move", null, null, null, 1, 1);
        assertNull(last.nextOffset());
        assertEquals("a", last.candidates().getFirst().configurationId());
        var edge = state.componentCandidates(ALL, CANDIDATE, null, "Root", "improvers[]", "Local", 0, 25);
        assertEquals(1, edge.total());
        assertEquals(2, edge.candidates().getFirst().occurrences());
        assertEquals(2, state.componentCandidates(CURRENT_ELITES, CANDIDATE, "Move", null, null, null, 0, 25).total());
        assertEquals(0, state.componentCandidates(ALL, CANDIDATE, "absent", null, null, null, 0, 25).total());
        assertTrue(state.componentCandidates(ALL, CANDIDATE, "Move", null, null, null, 100, 25).candidates().isEmpty());
        assertThrows(IllegalArgumentException.class, () -> state.componentCandidates(ALL, CANDIDATE, "Move", "Root", "x", "Move", 0, 25));
        assertThrows(IllegalArgumentException.class, () -> state.componentCandidates(ALL, CANDIDATE, null, "Root", null, "Move", 0, 25));
        assertThrows(IllegalArgumentException.class, () -> state.componentCandidates(ALL, CANDIDATE, " ", null, null, null, 0, 25));
        assertThrows(IllegalArgumentException.class, () -> state.componentCandidates(ALL, CANDIDATE, "Move", null, null, null, -1, 25));
        assertThrows(IllegalArgumentException.class, () -> state.componentCandidates(ALL, CANDIDATE, "Move", null, null, null, 0, 101));
    }

    @Test
    void servesLiveAggregatesAndValidatesThePublicContract() throws Exception {
        var state = state(builder());
        var mvc = MockMvcBuilders.standaloneSetup(new AutoconfigController(state,
                mock(AutoconfigSearchSpace.class), mock(AutoconfigArtifactService.class)))
                .setControllerAdvice(new AutoconfigApiExceptionHandler()).build();
        mvc.perform(get("/api/autoconfig/components/usage")).andExpect(status().isOk())
                .andExpect(jsonPath("$.scope").value("ALL")).andExpect(jsonPath("$.configurationCount").value(0));
        state.evaluationStarted(configuration("1", "repeated"));
        state.evaluationStarted(configuration("1", "repeated"));
        mvc.perform(get("/api/autoconfig/components/usage")).andExpect(status().isOk())
                .andExpect(jsonPath("$.candidatePlacementCount").value(6))
                .andExpect(jsonPath("$.evaluationPlacementCount").value(12))
                .andExpect(jsonPath("$.components[0].name").value("Local"));
        mvc.perform(get("/api/autoconfig/components/candidates").param("component", "Move"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.candidates[0].occurrences").value(3))
                .andExpect(jsonPath("$.candidates[0].evaluations.running").value(2));
        mvc.perform(get("/api/autoconfig/components/candidates")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/autoconfig/components/usage").param("scope", "UNKNOWN")).andExpect(status().isBadRequest());
        state.prepareCoordinator(AutoconfigRunState.RunMode.IRACE, metric());
        mvc.perform(get("/api/autoconfig/components/usage")).andExpect(status().isNotFound());
        state.prepareWorker(AutoconfigRunState.RunMode.AUTOCONFIG, metric());
        mvc.perform(get("/api/autoconfig/components/candidates").param("component", "Move")).andExpect(status().isNotFound());
    }

    private static ComponentUsage.Component component(ComponentUsage.Snapshot snapshot, String name) {
        for (var component : snapshot.components()) if (component.name().equals(name)) return component;
        throw new AssertionError("Missing component " + name);
    }

    private static AutomaticAlgorithmBuilder<?, ?> builder() {
        var builder = mock(AutomaticAlgorithmBuilder.class);
        when(builder.asJsonTree(any())).thenAnswer(invocation -> {
            var config = (AlgorithmConfiguration) invocation.getArgument(0);
            return MAPPER.readTree(config.getValue("ROOT", "").equals("repeated") ? TREE : "{\"$component\":\"Move\"}");
        });
        return builder;
    }

    private static AutoconfigRunState state(AutomaticAlgorithmBuilder<?, ?> builder) {
        var state = new AutoconfigRunState(builder);
        state.prepareCoordinator(AutoconfigRunState.RunMode.AUTOCONFIG, metric());
        state.markRunning(100);
        return state;
    }

    private static AutoconfigRunState.CostMetricSnapshot metric() {
        return new AutoconfigRunState.CostMetricSnapshot("cost", FMode.MINIMIZE,
                AutoconfigRunState.CostMetricKind.OBJECTIVE, FMode.MINIMIZE, false, null);
    }

    private static IraceRuntimeConfiguration configuration(String id, String root) {
        return new IraceRuntimeConfiguration(id, "instance", 1, "instance.dat", new AlgorithmConfiguration(Map.of("ROOT", root)));
    }

    private static EliteConfiguration elite(String id, String root) { return new EliteConfiguration(id, Map.of("ROOT", root)); }

    private static IraceProgressDetails progress() {
        return new IraceProgressDetails(5, 100, 2, 98, false, 10, 2, 0, 0, null, null);
    }
}
