package es.urjc.etsii.grafo.util;

import es.urjc.etsii.grafo.monitoring.NormalExperimentRunState.ResultView;
import java.util.List;

public final class MonitoringExportUtil {
    private MonitoringExportUtil() {}
    public static String csv(List<ResultView> rows) {
        var output = new StringBuilder("resultId,experiment,instance,algorithm,iteration,seed,state,executionTimeNanos,timeToBestNanos,objectives\r\n");
        for (var row : rows) {
            Object[] values = {row.resultId(), row.experimentName(), row.instanceName(), row.algorithmName(), row.iteration(), row.seed(), row.state(), row.executionTime(), row.timeToBest(), row.objectives()};
            for (int i = 0; i < values.length; i++) {
                if (i != 0) output.append(',');
                String value = values[i] == null ? "" : values[i].toString();
                // Prevent spreadsheet formula interpretation for user-controlled text cells.
                if (!(values[i] instanceof Number) && value.matches("^[=+@\\-\\t\\r].*")) value = "'" + value;
                output.append('"').append(value.replace("\"", "\"\"")).append('"');
            }
            output.append("\r\n");
        }
        return output.toString();
    }
}
