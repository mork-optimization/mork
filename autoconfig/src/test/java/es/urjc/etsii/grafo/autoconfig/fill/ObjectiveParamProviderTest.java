package es.urjc.etsii.grafo.autoconfig.fill;

import es.urjc.etsii.grafo.solution.Objective;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ObjectiveParamProviderTest {
    @Test
    void checkNames(){
        String[] valid = new String[]{"objective"};
        String[] invalid = new String[]{"moda", "different", "fmode", "mode", "fMode"};

        var paramProvider = new ObjectiveParamProvider();

        for(String s: valid){
            assertTrue(paramProvider.provides(Objective.class, s));
            assertFalse(paramProvider.provides(Object.class, s));
            assertDoesNotThrow(() -> paramProvider.getValue(Objective.class, s));
        }

        for(String s: invalid){
            assertFalse(paramProvider.provides(Objective.class, s));
            assertThrows(IllegalArgumentException.class, () -> paramProvider.getValue(Objective.class, s));
        }
    }
}
