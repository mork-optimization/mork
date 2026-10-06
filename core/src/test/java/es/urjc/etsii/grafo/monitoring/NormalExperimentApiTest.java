package es.urjc.etsii.grafo.monitoring;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.events.InMemoryEventLog;
import es.urjc.etsii.grafo.events.MorkEventPublisher;
import es.urjc.etsii.grafo.experiment.Experiment;
import es.urjc.etsii.grafo.restcontroller.EventController;
import es.urjc.etsii.grafo.restcontroller.NormalExperimentController;
import es.urjc.etsii.grafo.results.ResultStore;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestSolution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class NormalExperimentApiTest {
    @TempDir Path directory;
    private NormalExperimentRunState<TestSolution, TestInstance> state;
    private final MonitoringArtifactRegistry artifacts = new MonitoringArtifactRegistry();
    private MockMvc mvc;
    private String run;
    @BeforeEach @SuppressWarnings("unchecked") void setup() {
        state = new NormalExperimentRunState<TestSolution, TestInstance>(new ResultStore<>(), mock(ObjectProvider.class));
        state.prepare(Map.of("Cost", FMode.MINIMIZE));
        var algorithm = new Algorithm<TestSolution, TestInstance>("algorithm") {
            public TestSolution algorithm(TestInstance instance) { return new TestSolution(instance, 1); }
        };
        state.plan(new Experiment<>("experiment", getClass(), List.of(algorithm)), List.of("instance"), 2, 7);
        run = state.status().runId();
        mvc = MockMvcBuilders.standaloneSetup(new NormalExperimentController<>(state, artifacts), new EventController(new InMemoryEventLog(), mock(MorkEventPublisher.class))).build();
    }
    @Test void emptyEventLogHasAnExplicitHeadAndRejectsStaleIdentity() throws Exception {
        mvc.perform(get("/lastevent")).andExpect(status().isOk()).andExpect(jsonPath("$.lastEventId").value(-1)).andExpect(jsonPath("$.eventLogId").isString());
        mvc.perform(get("/events").param("eventLogId", "old-log").param("from", "0").param("to", "1")).andExpect(status().isConflict());
    }
    @Test void snapshotsAreIdentityBoundAndPagesValidated() throws Exception {
        mvc.perform(get("/api/experiments/status")).andExpect(status().isOk()).andExpect(jsonPath("$.counts.planned").value(2));
        mvc.perform(get("/api/experiments/results").param("runId", run).param("offset", "1").param("limit", "1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.total").value(2)).andExpect(jsonPath("$.items[0].ordinal").value(2)).andExpect(jsonPath("$.items[0].seed").value(8));
        mvc.perform(get("/api/experiments/instances").param("runId", "stale")).andExpect(status().isConflict());
        mvc.perform(get("/api/experiments/results").param("runId", run).param("limit", "1001")).andExpect(status().isBadRequest());
        mvc.perform(get("/api/experiments/results/not-a-uuid").param("runId", run)).andExpect(status().isBadRequest());
    }
    @Test void exportsUseTheSameFiltersAndDoNotMatchTheUuidRoute() throws Exception {
        mvc.perform(get("/api/experiments/results/export").param("runId", run).param("format", "json").param("state", "PENDING"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.runId").value(run)).andExpect(jsonPath("$.results", hasSize(2)));
        mvc.perform(get("/api/experiments/results/export").param("runId", run).param("algorithm", "missing"))
                .andExpect(status().isOk()).andExpect(content().string(not(containsString("PENDING"))));
        mvc.perform(get("/api/experiments/results/export").param("runId", run).param("format", "html")).andExpect(status().isBadRequest());
    }
    @Test void downloadsOnlyServeRegisteredArtifacts() throws Exception {
        var path = directory.resolve("results.csv"); Files.writeString(path, "sample");
        artifacts.register(path, "experiment", null);
        var id = artifacts.manifest().getFirst().id();
        mvc.perform(get("/api/experiments/artifacts/" + id).param("runId", run)).andExpect(status().isOk()).andExpect(content().string("sample"));
        mvc.perform(get("/api/experiments/artifacts/unknown").param("runId", run)).andExpect(status().isNotFound());
        Files.delete(path);
        mvc.perform(get("/api/experiments/artifacts/" + id).param("runId", run)).andExpect(status().isNotFound());
    }
}
