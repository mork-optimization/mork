package es.urjc.etsii.grafo.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;

public class CombinationGeneratorTests {
    @Test
    public void testCombinations(){
        var list = Arrays.asList(1, 2, 3);
        var result = CombinationGenerator.generate(list);
        var expected = Set.of(
                Set.of(),
                Set.of(1),
                Set.of(2),
                Set.of(3),
                Set.of(1,2),
                Set.of(2,3),
                Set.of(1,3),
                Set.of(1,2,3)
        );

        Assertions.assertEquals(expected, result);
    }
}
