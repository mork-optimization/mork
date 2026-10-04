package es.urjc.etsii.grafo.autoconfig.irace;

import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.autoconfig.service.AutoconfigRunState;
import es.urjc.etsii.grafo.config.SolverConfig;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.util.Context;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IraceCostMetricUtilTest {

    @AfterEach
    void resetContext() {
        Context.reset();
    }

    @Test
    void autoconfigAlwaysDescribesAreaUnderTheCurve() {
        Context.Configurator.setObjectives(Objective.ofMinimizing("distance", solution -> 0));
        var solver = new SolverConfig();
        solver.setIgnoreInitialMillis(12);
        solver.setIntervalDurationMillis(34);
        solver.setLogScaleArea(false);

        var metric = IraceCostMetricUtil.describe(
                AutoconfigRunState.RunMode.AUTOCONFIG,
                solver,
                new IraceConfig()
        );

        assertEquals("distance", metric.objectiveName());
        assertEquals(AutoconfigRunState.CostMetricKind.AREA_UNDER_CURVE, metric.kind());
        assertEquals(12, metric.auc().ignoreInitialMillis());
        assertEquals(34, metric.auc().intervalDurationMillis());
        assertFalse(metric.auc().logScale());
        assertFalse(metric.negated());
    }

    @Test
    void manualIraceDescribesTheConfiguredRawObjective() {
        Context.Configurator.setObjectives(Objective.ofMinimizing("distance", solution -> 0));

        var metric = IraceCostMetricUtil.describe(
                AutoconfigRunState.RunMode.IRACE,
                new SolverConfig(),
                new IraceConfig()
        );

        assertEquals(AutoconfigRunState.CostMetricKind.OBJECTIVE, metric.kind());
        assertEquals(FMode.MINIMIZE, metric.costMode());
        assertNull(metric.auc());
    }

    @Test
    void maximizingManualAucIsReportedAsNegatedMinimizationCost() {
        Context.Configurator.setObjectives(Objective.ofMaximizing("profit", solution -> 0));
        var irace = new IraceConfig();
        irace.setAuc(true);

        var metric = IraceCostMetricUtil.describe(
                AutoconfigRunState.RunMode.IRACE,
                new SolverConfig(),
                irace
        );

        assertEquals(FMode.MAXIMIZE, metric.objectiveMode());
        assertEquals(FMode.MINIMIZE, metric.costMode());
        assertEquals(AutoconfigRunState.CostMetricKind.AREA_UNDER_CURVE, metric.kind());
        assertTrue(metric.negated());
    }
}
