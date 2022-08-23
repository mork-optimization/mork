package es.urjc.etsii.grafo.algorithms;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EmptyAlgorithmTest {
    @Test
    void testName(){
        var name = "myTestName";
        var alg = new EmptyAlgorithm<>(name);
        Assertions.assertEquals(name, alg.getShortName());
        Assertions.assertTrue(alg.toString().contains(name));
    }

    @Test
    void testNullAndBlank(){
        Assertions.assertThrows(IllegalArgumentException.class, () -> new EmptyAlgorithm<>(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new EmptyAlgorithm<>("\t"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new EmptyAlgorithm<>(" \n  "));
    }
}
