package es.urjc.etsii.grafo.autoconfig.fill;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AlgorithmNameParamTest {

    @Test
    void checkNames(){
        String[] valid = new String[]{"algorithmName", "name", "componentName"};
        String[] invalid = new String[]{"ALGORITHMNAME", "Name", "different"};

        var testType = Object.class;
        var paramProvider = new AlgorithmNameParam();

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