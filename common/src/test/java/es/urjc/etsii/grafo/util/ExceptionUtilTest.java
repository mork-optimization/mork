package es.urjc.etsii.grafo.util;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    public void testFilteresStacktrace(){
        var list = List.of("a,b,c,d,e,f");
        try {
            list.stream().sorted().forEach(e -> {throw new IllegalArgumentException();});
        } catch (IllegalArgumentException e){
            var fullStacktrace = ExceptionUtils.getStackTrace(e);
            var simplifiedStacktrace = ExceptionUtil.filteredStacktrace(e);
            assertTrue(fullStacktrace.contains("java.util.stream"));
            assertFalse(simplifiedStacktrace.contains("java.util.stream"));
        }
    }
}
