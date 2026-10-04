package es.urjc.etsii.grafo.autoconfig.irace;

import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.autoconfig.service.AutoconfigRunState;
import es.urjc.etsii.grafo.config.SolverConfig;
import es.urjc.etsii.grafo.util.Context;

/** Builds the public description of the cost value returned to IRACE. */
public final class IraceCostMetricUtil {

    private IraceCostMetricUtil() {
    }

    public static AutoconfigRunState.CostMetricSnapshot describe(
            AutoconfigRunState.RunMode mode,
            SolverConfig solverConfig,
            IraceConfig iraceConfig
    ) {
        if (mode == AutoconfigRunState.RunMode.DISABLED) {
            throw new IllegalArgumentException("Cannot describe an IRACE cost for disabled mode");
        }
        var objective = Context.getMainObjective();
        boolean areaUnderCurve = mode == AutoconfigRunState.RunMode.AUTOCONFIG || iraceConfig.isAuc();
        var kind = areaUnderCurve
                ? AutoconfigRunState.CostMetricKind.AREA_UNDER_CURVE
                : AutoconfigRunState.CostMetricKind.OBJECTIVE;
        var auc = areaUnderCurve
                ? new AutoconfigRunState.AucSettings(
                        solverConfig.getIgnoreInitialMillis(),
                        solverConfig.getIntervalDurationMillis(),
                        solverConfig.isLogScaleArea()
                )
                : null;
        return new AutoconfigRunState.CostMetricSnapshot(
                objective.getName(),
                objective.getFMode(),
                kind,
                FMode.MINIMIZE,
                objective.getFMode() == FMode.MAXIMIZE,
                auc
        );
    }
}
