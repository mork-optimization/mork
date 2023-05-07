package es.urjc.etsii.grafo.autoconfig.testutil;

import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.autoconfig.AlgorithmBuilderUtil;
import es.urjc.etsii.grafo.autoconfig.exception.AlgorithmParsingException;
import es.urjc.etsii.grafo.autoconfig.fakecomponents.FakeGRASPConstructive;
import es.urjc.etsii.grafo.autoconfig.fill.FModeParam;
import es.urjc.etsii.grafo.create.grasp.GRASPListManager;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestMove;
import es.urjc.etsii.grafo.testutil.TestSolution;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static es.urjc.etsii.grafo.autoconfig.AlgorithmBuilderUtil.isAssignable;
import static es.urjc.etsii.grafo.autoconfig.AlgorithmBuilderUtil.prepareParameterValue;
import static org.junit.jupiter.api.Assertions.*;

class AlgorithmBuilderUtilTest {

    @Test
    void testFindMultipleConstructors() {
        Map<String, Class<?>> params = Map.of(
                "alpha", String.class,
                "tetha", int.class
        );

        var constructor = AlgorithmBuilderUtil.findConstructor(AlgorithmA.class, params, List.of());
        assertNotNull(constructor);
        assertDoesNotThrow(() -> constructor.newInstance(0, "0"));
    }

    @Test
    void testFindNameCheck() {
        Map<String, Class<?>> params = Map.of(
                "alpha", byte.class
        );

        var constructor = AlgorithmBuilderUtil.findConstructor(AlgorithmA.class, params, List.of());
        assertNotNull(constructor);
        var cParams = constructor.getParameters();
        assertEquals(1, cParams.length);
        assertEquals("alpha", cParams[0].getName());
        assertDoesNotThrow(() -> constructor.newInstance(1));
    }

    @Test
    void testFindNoArg() {
        Map<String, Class<?>> params = Map.of(
        );

        var constructor = AlgorithmBuilderUtil.findConstructor(AlgorithmA.class, params, List.of());
        assertNotNull(constructor);
        assertDoesNotThrow(() -> constructor.newInstance());
    }

    @Test
    void testFindImpossible() {
        Map<String, Class<?>> params = Map.of(
                "alpha", String.class
        );

        var constructor = AlgorithmBuilderUtil.findConstructor(AlgorithmA.class, params, List.of());
        assertNull(constructor);
    }

    GRASPListManager<TestMove, TestSolution, TestInstance> getCLManager(){
        return new GRASPListManager<>() {
            @Override
            public List<TestMove> buildInitialCandidateList(TestSolution solution) {
                throw new UnsupportedOperationException();
            }

            @Override
            public List<TestMove> updateCandidateList(TestSolution solution, TestMove move, List<TestMove> candidateList, int index) {
                return buildInitialCandidateList(solution);
            }
        };
    }

    @Test
    void buildGraspExplicitFMode(){
        Map<String, Object> params = Map.of(
                "ofmode", FMode.MINIMIZE,
                "alpha", 0.75,
                "candidateListManager", getCLManager());
        var greedyRandom = AlgorithmBuilderUtil.build(FakeGRASPConstructive.class, params, List.of());
        assertNotNull(greedyRandom);
    }

    @Test
    void buildGraspNoFMode(){
        Map<String, Object> params = Map.of(
                "alpha", 0.75,
                "candidateListManager", getCLManager());
        assertThrows(AlgorithmParsingException.class, () -> AlgorithmBuilderUtil.build(FakeGRASPConstructive.class, params, List.of()));
    }

    @Test
    void buildGraspProvidedFMode(){
        Map<String, Object> params = Map.of(
                "alpha", 0.75,
                "candidateListManager", getCLManager());
        assertThrows(AlgorithmParsingException.class, () -> AlgorithmBuilderUtil.build(FakeGRASPConstructive.class, params, List.of(new FModeParam())));
    }

    @Test
    void isAssignableTest(){
        assertTrue(isAssignable(Integer.class, Double.class));
        assertTrue(isAssignable(Number.class, Double.class));
        assertTrue(isAssignable(Long.class, Double.class));
        assertTrue(isAssignable(Float.class, Double.class));
        assertTrue(isAssignable(Double.class, Number.class));

        assertFalse(isAssignable(Double.class, Integer.class));
        assertFalse(isAssignable(Double.class, Long.class));
        assertFalse(isAssignable(Double.class, Float.class));

        assertTrue(isAssignable(String.class, FakeEnum.class));
        assertFalse(isAssignable(FakeEnum.class, String.class));
    }

    @Test
    void prepareParameterTest(){
        assertEquals(FakeEnum.A, prepareParameterValue("A", FakeEnum.class));
        assertEquals(FakeEnum.B, prepareParameterValue("B", FakeEnum.class));
        assertEquals(FakeEnum.C, prepareParameterValue("C", FakeEnum.class));
        assertThrows(RuntimeException.class, () -> prepareParameterValue("a", FakeEnum.class));
        assertThrows(RuntimeException.class, () -> prepareParameterValue("b", FakeEnum.class));
        assertThrows(RuntimeException.class, () -> prepareParameterValue("asdad", FakeEnum.class));
        assertThrows(RuntimeException.class, () -> prepareParameterValue("", FakeEnum.class));

        assertEquals("A", prepareParameterValue("A", String.class));
        assertEquals("B", prepareParameterValue("B", String.class));
        assertEquals("C", prepareParameterValue("C", String.class));
        assertEquals("asdad", prepareParameterValue("asdad", String.class));
        assertEquals("", prepareParameterValue("", String.class));


        assertEquals(Double.class, prepareParameterValue(3, Double.class).getClass());
        assertEquals(3D, prepareParameterValue(3, Double.class));

        assertEquals(Integer.class, prepareParameterValue(3, Integer.class).getClass());
        assertEquals(3, prepareParameterValue(3, Integer.class));
    }

    private enum FakeEnum {
        A, B, C
    }
}
