package es.urjc.etsii.grafo.autoconfig.irace;

import java.util.List;

/** Parameter declarations and constraints are separate so only declarations count toward budgets. */
public record IraceParameterSpace(List<String> parameters, List<String> forbiddenExpressions) {
    public IraceParameterSpace {
        parameters = List.copyOf(parameters);
        forbiddenExpressions = List.copyOf(forbiddenExpressions);
    }
}
