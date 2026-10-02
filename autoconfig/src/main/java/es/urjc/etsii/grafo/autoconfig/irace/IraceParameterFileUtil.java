package es.urjc.etsii.grafo.autoconfig.irace;

/** Formats automatically generated irace parameter files. */
public final class IraceParameterFileUtil {
    private IraceParameterFileUtil() {}

    public static String toFileContents(IraceParameterSpace space) {
        var result = new StringBuilder();
        for (var parameter : space.parameters()) result.append(parameter).append('\n');
        if (!space.forbiddenExpressions().isEmpty()) {
            result.append("\n[forbidden]\n");
            for (var expression : space.forbiddenExpressions()) result.append(expression).append('\n');
        }
        result.append("\n[global]\ndigits = ").append(InitialConfigurationUtil.GENERATED_REAL_DIGITS).append('\n');
        return result.toString();
    }
}
