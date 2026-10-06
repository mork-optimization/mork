package es.urjc.etsii.grafo.util;

import es.urjc.etsii.grafo.monitoring.NormalExperimentRunState;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class MonitoringExportUtilTest {
    @Test void quotesTextAndProtectsFormulaCellsWithoutChangingNumbers() {
        var result = new NormalExperimentRunState.ResultView(UUID.randomUUID(), "instance", "=formula", "quoted,\"name\"", "algorithm",
                1, "first\nlabel", -2, NormalExperimentRunState.WorkState.SUCCEEDED, Map.of("Cost", -5.0), Map.of(), 20L, 10L, 1L, 2L);
        var csv = MonitoringExportUtil.csv(List.of(result));
        assertTrue(csv.contains("\"'=formula\"")); assertTrue(csv.contains("\"quoted,\"\"name\"\"\""));
        assertTrue(csv.contains("\"first\nlabel\"")); assertTrue(csv.contains("\"-2\"")); assertFalse(csv.contains("\"'-2\""));
    }
}
