package es.urjc.etsii.grafo.annotations;

import es.urjc.etsii.grafo.algorithms.Algorithm;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProvidedParamTypeTest {
    @Test
    void isAssignable(){
        assertTrue(ProvidedParamType.UNKNOWN.isAssignableTo(Object.class));
        assertTrue(ProvidedParamType.MAXIMIZE.isAssignableTo(Boolean.class));
        assertTrue(ProvidedParamType.MAXIMIZE.isAssignableTo(Boolean.TYPE));
        assertTrue(ProvidedParamType.MAXIMIZE.isAssignableTo(Object.class));
        assertTrue(ProvidedParamType.ALGORITHM_NAME.isAssignableTo(String.class));
        assertTrue(ProvidedParamType.ALGORITHM_NAME.isAssignableTo(CharSequence.class));
        assertTrue(ProvidedParamType.ALGORITHM_NAME.isAssignableTo(Object.class));

        assertFalse(ProvidedParamType.UNKNOWN.isAssignableTo(Algorithm.class));
        assertFalse(ProvidedParamType.MAXIMIZE.isAssignableTo(Integer.class));
        assertFalse(ProvidedParamType.ALGORITHM_NAME.isAssignableTo(Character.class));
    }
}