package es.urjc.etsii.grafo.autoconfig.fill;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FModeParamTest {
    @Test
    void checkNames(){
        String[] valid = new String[]{"fmode", "mode", "fMode"};
        String[] invalid = new String[]{"moda", "different"};

        var testType = Object.class;
        var paramProvider = new FModeParam();

        for(String s: valid){
            assertTrue(paramProvider.provides(testType, s));
            assertDoesNotThrow(() -> paramProvider.getValue(testType, s));
        }

        for(String s: invalid){
            assertFalse(paramProvider.provides(testType, s));
            assertThrows(IllegalArgumentException.class, () -> paramProvider.getValue(testType, s));
        }
    }
}