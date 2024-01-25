package es.urjc.etsii.grafo.algorithms;

import es.urjc.etsii.grafo.create.builder.SolutionBuilder;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestSolution;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EmptyAlgorithmTest {
    @Test
    void testName(){
        var name = "myTestName";
        var alg = new EmptyAlgorithm<>(name);
        Assertions.assertEquals(name, alg.getName());
        Assertions.assertTrue(alg.toString().contains(name));
    }

    @Test
    void testNullAndBlank(){
        Assertions.assertThrows(IllegalArgumentException.class, () -> new EmptyAlgorithm<>(null));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new EmptyAlgorithm<>("\t"));
        Assertions.assertThrows(IllegalArgumentException.class, () -> new EmptyAlgorithm<>(" \n  "));
    }

    @Test
    void doesReturn(){
        var alg = new EmptyAlgorithm<TestSolution, TestInstance>("whatever");
        @SuppressWarnings("unchecked")
        SolutionBuilder<TestSolution, TestInstance> builder = Mockito.mock(SolutionBuilder.class);
        var instance = new TestInstance("testinstance");
        var solution = new TestSolution(instance);
        when(builder.initializeSolution(any())).thenReturn(solution);
        alg.setBuilder(builder);
        var result = alg.algorithm(instance);
        Assertions.assertSame(solution, result);
        verify(builder).initializeSolution(instance);
        verifyNoMoreInteractions(builder);
    }
}
