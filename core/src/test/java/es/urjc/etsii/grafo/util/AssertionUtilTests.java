package es.urjc.etsii.grafo.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AssertionUtilTests {
    @Test
    public void implicationTest(){
        Assertions.assertTrue(AssertionUtil.implication(true, true));
        Assertions.assertTrue(AssertionUtil.implication(false, true));
        Assertions.assertTrue(AssertionUtil.implication(false, false));
        Assertions.assertFalse(AssertionUtil.implication(true, false));
    }

    @Test
    public void biimplicationTest(){
        Assertions.assertTrue(AssertionUtil.biimplication(true, true));
        Assertions.assertFalse(AssertionUtil.biimplication(false, true));
        Assertions.assertTrue(AssertionUtil.biimplication(false, false));
        Assertions.assertFalse(AssertionUtil.biimplication(true, false));
    }
}
