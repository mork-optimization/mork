package es.urjc.etsii.grafo.testutil;

import org.junit.jupiter.api.Assertions;

public class TestAssertions {

    public static void toStringImpl(Object o){
        String s = o.toString();
        Assertions.assertFalse(o.toString().matches(".*@[\\da-fA-F]{8}.*"));
    }
}
