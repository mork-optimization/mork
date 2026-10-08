package es.urjc.etsii.grafo.autoconfig.irace;

import es.urjc.etsii.grafo.autoconfig.irace.params.ParameterType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IraceParameterValueUtilTest {

    @Test
    void decodesTypedScalarValues() {
        assertInstanceOf(Integer.class, decode(ParameterType.INTEGER, "12"));
        assertInstanceOf(Long.class, decode(ParameterType.INTEGER, "2147483648"));
        assertEquals(0.25, decode(ParameterType.REAL, "0.25"));
        assertEquals("raw", decode(ParameterType.CATEGORICAL, "raw"));
        assertEquals("line\nquote \"", decode(ParameterType.CATEGORICAL, "\"line\\nquote \\\"\""));
        assertEquals("ordered", decode(ParameterType.ORDINAL, "\"ordered\""));
    }

    @Test
    void rejectsInvalidScalarValues() {
        assertThrows(
                IllegalArgumentException.class,
                () -> decode(ParameterType.INTEGER, "1.2")
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> decode(ParameterType.REAL, "Infinity")
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> decode(ParameterType.CATEGORICAL, "\"broken")
        );
    }

    @Test
    void encodesCategoricalValuesAsJsonInsideAnRString() {
        assertEquals("'\"simple\"'", IraceParameterValueUtil.encodeCategorical("simple"));

        String encoded = IraceParameterValueUtil.encodeCategorical("a\\b\"'");
        assertTrue(encoded.startsWith("'\""));
        assertTrue(encoded.endsWith("\"'"));
        assertTrue(encoded.contains("\\\\\\\\"));
        assertTrue(encoded.contains("\\\\\""));
        assertTrue(encoded.contains("\\'"));
    }

    private static Object decode(ParameterType type, String value) {
        return IraceParameterValueUtil.decode(type, "parameter", value);
    }
}
