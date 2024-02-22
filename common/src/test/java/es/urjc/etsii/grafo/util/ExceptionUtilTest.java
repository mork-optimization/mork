package es.urjc.etsii.grafo.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExceptionUtilTest {
    @Test
    public void testGetRootCause() {
        var rootCause = new RuntimeException("Root cause");
        // No cause
        assertEquals(rootCause, ExceptionUtil.getRootCause(rootCause));
        assertEquals(rootCause, ExceptionUtil.getRootCause(new RuntimeException(rootCause)));
        assertEquals(rootCause, ExceptionUtil.getRootCause(new IllegalStateException(new RuntimeException(rootCause))));
        assertEquals(rootCause, ExceptionUtil.getRootCause(new IllegalArgumentException(new IllegalStateException(new RuntimeException(rootCause)))));
        assertThrows(IllegalArgumentException.class, () -> ExceptionUtil.getRootCause(null));
    }
}
