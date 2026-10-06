package es.urjc.etsii.grafo.services;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import es.urjc.etsii.grafo.config.ErrorConfig;
import es.urjc.etsii.grafo.monitoring.MonitoringArtifactRegistry;
import es.urjc.etsii.grafo.monitoring.NormalExperimentRunState;
import es.urjc.etsii.grafo.results.ResultStore;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestSolution;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.ObjectProvider;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class IOManagerMonitoringTest {
    @TempDir Path directory;
    @Test @SuppressWarnings("unchecked") void errorReportFailureDoesNotMaskComputationFailure() throws Exception {
        var blocked = directory.resolve("not-a-directory"); Files.writeString(blocked, "file");
        var config = new ErrorConfig(); config.setErrorsToFile(true); config.setFolder(blocked.resolve("reports").toString());
        var state = new NormalExperimentRunState<TestSolution, TestInstance>(new ResultStore<>(), mock(ObjectProvider.class));
        state.prepare(Map.of());
        var io = new IOManager<TestSolution, TestInstance>(config, List.of(), new MonitoringArtifactRegistry(), state);
        var algorithm = new Algorithm<TestSolution, TestInstance>("algorithm") {
            public TestSolution algorithm(TestInstance instance) { return new TestSolution(instance, 1); }
        };
        assertDoesNotThrow(() -> io.exportError("experiment", algorithm, new TestInstance("instance"), new IllegalArgumentException("original"), "trace"));
        var issue = state.issues(state.status().runId(), null, 0, 25).items().getFirst();
        assertEquals("WARNING", issue.severity()); assertEquals("EXPORT", issue.scope());
        assertEquals("experiment", issue.experimentName());
    }
}
