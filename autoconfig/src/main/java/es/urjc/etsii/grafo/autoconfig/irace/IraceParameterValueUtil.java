package es.urjc.etsii.grafo.autoconfig.irace;

import es.urjc.etsii.grafo.autoconfig.irace.params.ParameterType;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

/**
 * Converts IRACE's textual parameter values to the scalar values used by component specifications.
 */
public final class IraceParameterValueUtil {

    private static final JsonMapper JSON_MAPPER = new JsonMapper();

    private IraceParameterValueUtil() {
        // Static utility class
    }

    public static Object decode(ParameterType type, String parameterName, String value) {
        return switch (type) {
            case INTEGER -> decodeInteger(type, parameterName, value);
            case REAL -> decodeReal(type, parameterName, value);
            case CATEGORICAL, ORDINAL -> decodeCategorical(type, parameterName, value);
            case PROVIDED, NOT_ANNOTATED, COMBINATION ->
                    throw new IllegalArgumentException("Parameter %s is not an IRACE scalar"
                            .formatted(parameterName));
        };
    }

    public static String encodeCategorical(String value) {
        try {
            String jsonString = JSON_MAPPER.writeValueAsString(value);
            String rString = jsonString
                    .replace("\\", "\\\\")
                    .replace("'", "\\'");
            return "'" + rString + "'";
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Failed to encode categorical IRACE value", e);
        }
    }

    private static Object decodeInteger(ParameterType type, String parameterName, String value) {
        try {
            long parsed = Long.parseLong(value);
            if (parsed >= Integer.MIN_VALUE && parsed <= Integer.MAX_VALUE) {
                return (int) parsed;
            }
            return parsed;
        } catch (NumberFormatException e) {
            throw invalid(type, parameterName, value, e);
        }
    }

    private static double decodeReal(ParameterType type, String parameterName, String value) {
        try {
            double parsed = Double.parseDouble(value);
            if (!Double.isFinite(parsed)) {
                throw invalid(type, parameterName, value, null);
            }
            return parsed;
        } catch (NumberFormatException e) {
            throw invalid(type, parameterName, value, e);
        }
    }

    private static String decodeCategorical(ParameterType type, String parameterName, String value) {
        boolean startsWithQuote = value.startsWith("\"");
        boolean endsWithQuote = value.endsWith("\"");
        if (!startsWithQuote && !endsWithQuote) {
            return value;
        }
        if (!startsWithQuote || !endsWithQuote) {
            throw invalid(type, parameterName, value, null);
        }
        try {
            return JSON_MAPPER.readValue(value, String.class);
        } catch (JacksonException e) {
            throw invalid(type, parameterName, value, e);
        }
    }

    private static IllegalArgumentException invalid(ParameterType type, String parameterName, String value, Exception cause) {
        String message = "Invalid %s value '%s' for IRACE parameter %s"
                .formatted(type, value, parameterName);
        return cause == null ? new IllegalArgumentException(message) : new IllegalArgumentException(message, cause);
    }
}
