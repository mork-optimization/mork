package es.urjc.etsii.grafo.autoconfig.fill;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AlgorithmNameParamTest {

    @Test
    void checkNames(){
        String[] valid = new String[]{"algorithmName", "name", "componentName"};
        String[] invalid = new String[]{"ALGORITHMNAME", "Name", "different"};

        var paramProvider = new AlgorithmNameParam();

        for(String s: valid){
            assertTrue(paramProvider.provides(String.class, s));
            assertFalse(paramProvider.provides(Object.class, s));
            assertDoesNotThrow(() -> paramProvider.getValue(String.class, s));
        }

        for(String s: invalid){
            assertFalse(paramProvider.provides(String.class, s));
            assertThrows(IllegalArgumentException.class, () -> paramProvider.getValue(String.class, s));
        }
    }
}
