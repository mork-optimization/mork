package es.urjc.etsii.grafo.autoconfig.testutil;

import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.autoconfig.AlgorithmBuilderUtil;
import es.urjc.etsii.grafo.autoconfig.exception.AlgorithmParsingException;
import es.urjc.etsii.grafo.autoconfig.fakecomponents.FakeGRASPConstructive;
import es.urjc.etsii.grafo.autoconfig.fill.FModeParam;
import es.urjc.etsii.grafo.autoconfig.testutil.findautoconfig.Has0;
import es.urjc.etsii.grafo.autoconfig.testutil.findautoconfig.Has1;
import es.urjc.etsii.grafo.autoconfig.testutil.findautoconfig.Has2;
import es.urjc.etsii.grafo.create.grasp.GRASPListManager;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestMove;
import es.urjc.etsii.grafo.testutil.TestSolution;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
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
    void testFindWrongType() {
        Map<String, Class<?>> params = Map.of(
                "alpha", String.class
        );

        var constructor = AlgorithmBuilderUtil.findConstructor(AlgorithmA.class, params, List.of());
        assertNotNull(constructor);
    }

    @Test
    void testFindWrongName() {
        Map<String, Class<?>> params = Map.of(
                "alfa", double.class
        );

        var constructor = AlgorithmBuilderUtil.findConstructor(AlgorithmA.class, params, List.of());
        assertNull(constructor);
    }

    GRASPListManager<TestMove, TestSolution, TestInstance> getCLManager() {
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
    void buildGraspExplicitFMode() {
        Map<String, Object> params = Map.of(
                "ofmode", FMode.MINIMIZE,
                "alpha", 0.75,
                "candidateListManager", getCLManager());
        var greedyRandom = AlgorithmBuilderUtil.build(FakeGRASPConstructive.class, params, List.of());
        assertNotNull(greedyRandom);
    }

    @Test
    void buildGraspNoFMode() {
        Map<String, Object> params = Map.of(
                "alpha", 0.75,
                "candidateListManager", getCLManager());
        assertThrows(AlgorithmParsingException.class, () -> AlgorithmBuilderUtil.build(FakeGRASPConstructive.class, params, List.of()));
    }

    @Test
    void buildGraspProvidedFMode() {
        Map<String, Object> params = Map.of(
                "alpha", 0.75,
                "candidateListManager", getCLManager());
        assertThrows(AlgorithmParsingException.class, () -> AlgorithmBuilderUtil.build(FakeGRASPConstructive.class, params, List.of(new FModeParam())));
    }

    @Test
    void isAssignableTest() {
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
    void prepareParameterTest() {
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


        // From numbers to any transformations: INVALID DUE TO LOSS OF PRECISION
        assertThrows(IllegalArgumentException.class, () -> prepareParameterValue(3.4028235e+40d, Float.class));
        assertThrows(IllegalArgumentException.class, () -> prepareParameterValue(3.01, Long.class));
        assertThrows(IllegalArgumentException.class, () -> prepareParameterValue(10e20, Long.class));
        assertThrows(IllegalArgumentException.class, () -> prepareParameterValue(3.01, Integer.class));
        assertThrows(IllegalArgumentException.class, () -> prepareParameterValue(Long.MAX_VALUE, Integer.class));
        assertThrows(IllegalArgumentException.class, () -> prepareParameterValue(2E33, Integer.class));
        assertThrows(IllegalArgumentException.class, () -> prepareParameterValue(3.4028235e+40d, Float.class));
    }

    @Test
    void prepareParamWithNoConversion() {
        var r1 = prepareParameterValue(3d, Double.class);
        assertEquals(Double.class, r1.getClass());
        assertEquals(3d, r1);
        r1 = prepareParameterValue(3d, double.class);
        assertEquals(Double.class, r1.getClass());
        assertEquals(3d, r1);
        r1 = prepareParameterValue("3d", double.class);
        assertEquals(Double.class, r1.getClass());
        assertEquals(3d, r1);

        var r2 = prepareParameterValue(3f, Float.class);
        assertEquals(Float.class, r2.getClass());
        assertEquals(3f, r2);
        r2 = prepareParameterValue(3f, float.class);
        assertEquals(Float.class, r2.getClass());
        assertEquals(3f, r2);
        r2 = prepareParameterValue("3f", float.class);
        assertEquals(Float.class, r2.getClass());
        assertEquals(3f, r2);

        var r3 = prepareParameterValue(-3, Integer.class);
        assertEquals(Integer.class, r3.getClass());
        assertEquals(-3, r3);
        r3 = prepareParameterValue(-3, int.class);
        assertEquals(Integer.class, r3.getClass());
        assertEquals(-3, r3);
        r3 = prepareParameterValue("-3", int.class);
        assertEquals(Integer.class, r3.getClass());
        assertEquals(-3, r3);

        var r4 = prepareParameterValue(Long.MAX_VALUE, Long.class);
        assertEquals(Long.class, r4.getClass());
        assertEquals(Long.MAX_VALUE, r4);
        r4 = prepareParameterValue(Long.MAX_VALUE, long.class);
        assertEquals(Long.class, r4.getClass());
        assertEquals(Long.MAX_VALUE, r4);
        r4 = prepareParameterValue("9223372036854775807", long.class);
        assertEquals(Long.class, r4.getClass());
        assertEquals(Long.MAX_VALUE, r4);

        var r5 = prepareParameterValue(Short.MIN_VALUE, Short.class);
        assertEquals(Short.class, r5.getClass());
        assertEquals(Short.MIN_VALUE, r5);
        r5 = prepareParameterValue(Short.MIN_VALUE, short.class);
        assertEquals(Short.class, r5.getClass());
        assertEquals(Short.MIN_VALUE, r5);
        r5 = prepareParameterValue("-32768", short.class);
        assertEquals(Short.class, r5.getClass());
        assertEquals(Short.MIN_VALUE, r5);

        var r6 = prepareParameterValue(Byte.MAX_VALUE, Byte.class);
        assertEquals(Byte.class, r6.getClass());
        assertEquals(Byte.MAX_VALUE, r6);
        r6 = prepareParameterValue(Byte.MAX_VALUE, byte.class);
        assertEquals(Byte.class, r6.getClass());
        assertEquals(Byte.MAX_VALUE, r6);
        r6 = prepareParameterValue("127", byte.class);
        assertEquals(Byte.class, r6.getClass());
        assertEquals(Byte.MAX_VALUE, r6);

        var r7 = prepareParameterValue(0.1234d, String.class);
        assertEquals(String.class, r7.getClass());
        assertEquals("0.1234", r7);
        r7 = prepareParameterValue("any", String.class);
        assertEquals(String.class, r7.getClass());
        assertEquals("any", r7);
    }

    @Test
    void prepareParamWithConversionDown() {
        var r2 = prepareParameterValue(3d, Float.class);
        assertEquals(Float.class, r2.getClass());
        assertEquals(3f, r2);
        r2 = prepareParameterValue(3d, float.class);
        assertEquals(Float.class, r2.getClass());
        assertEquals(3f, r2);
        r2 = prepareParameterValue("3.1", float.class);
        assertEquals(Float.class, r2.getClass());
        assertEquals(3.1f, r2);

        var r3 = prepareParameterValue(-3D, Integer.class);
        assertEquals(Integer.class, r3.getClass());
        assertEquals(-3, r3);
        r3 = prepareParameterValue(-3D, int.class);
        assertEquals(Integer.class, r3.getClass());
        assertEquals(-3, r3);
        r3 = prepareParameterValue("-3", int.class);
        assertEquals(Integer.class, r3.getClass());
        assertEquals(-3, r3);

        var r4 = prepareParameterValue(0d, Long.class);
        assertEquals(Long.class, r4.getClass());
        assertEquals(0L, r4);
        r4 = prepareParameterValue(0d, long.class);
        assertEquals(Long.class, r4.getClass());
        assertEquals(0L, r4);
        r4 = prepareParameterValue("0", long.class);
        assertEquals(Long.class, r4.getClass());
        assertEquals(0L, r4);

        var r5 = prepareParameterValue(1d, Short.class);
        assertEquals(Short.class, r5.getClass());
        assertEquals((short) 1, r5);
        r5 = prepareParameterValue(1d, short.class);
        assertEquals(Short.class, r5.getClass());
        assertEquals((short) 1, r5);
        r5 = prepareParameterValue("1", short.class);
        assertEquals(Short.class, r5.getClass());
        assertEquals((short) 1, r5);

        var r6 = prepareParameterValue(-1d, Byte.class);
        assertEquals(Byte.class, r6.getClass());
        assertEquals((byte) -1, r6);
        r6 = prepareParameterValue(-1d, byte.class);
        assertEquals(Byte.class, r6.getClass());
        assertEquals((byte) -1, r6);
        r6 = prepareParameterValue("-1", byte.class);
        assertEquals(Byte.class, r6.getClass());
        assertEquals((byte) -1, r6);
    }

    @Test
    void prepareParamWithConversionUp() {
        var r1 = prepareParameterValue((byte) 3, Double.class);
        assertEquals(Double.class, r1.getClass());
        assertEquals(3d, r1);
        r1 = prepareParameterValue((byte) 3, double.class);
        assertEquals(Double.class, r1.getClass());
        assertEquals(3d, r1);

        var r2 = prepareParameterValue((byte) 3, Float.class);
        assertEquals(Float.class, r2.getClass());
        assertEquals(3f, r2);
        r2 = prepareParameterValue((byte) 3, float.class);
        assertEquals(Float.class, r2.getClass());
        assertEquals(3f, r2);

        var r3 = prepareParameterValue((byte) -3, Integer.class);
        assertEquals(Integer.class, r3.getClass());
        assertEquals(-3, r3);
        r3 = prepareParameterValue((byte) -3, int.class);
        assertEquals(Integer.class, r3.getClass());
        assertEquals(-3, r3);

        var r4 = prepareParameterValue((byte) 0, Long.class);
        assertEquals(Long.class, r4.getClass());
        assertEquals(0L, r4);
        r4 = prepareParameterValue((byte) 0, long.class);
        assertEquals(Long.class, r4.getClass());
        assertEquals(0L, r4);

        var r5 = prepareParameterValue((byte) 0, Short.class);
        assertEquals(Short.class, r5.getClass());
        assertEquals((short) 0, r5);
        r5 = prepareParameterValue((byte) 0, short.class);
        assertEquals(Short.class, r5.getClass());
        assertEquals((short) 0, r5);
    }

    @Test
    void prepareBooleanParam() {
        var r1 = prepareParameterValue(true, Boolean.class);
        assertEquals(Boolean.class, r1.getClass());
        assertEquals(true, r1);
        r1 = prepareParameterValue("true", Boolean.class);
        assertEquals(Boolean.class, r1.getClass());
        assertEquals(true, r1);
        var r2 = prepareParameterValue(false, Boolean.class);
        assertEquals(Boolean.class, r1.getClass());
        assertEquals(false, r2);
        r2 = prepareParameterValue("false", Boolean.class);
        assertEquals(Boolean.class, r1.getClass());
        assertEquals(false, r2);
    }

    @Test
    void findAutoconfigConstructors() throws NoSuchMethodException {
        assertNull(AlgorithmBuilderUtil.findAutoconfigConstructor(Has0.class));
        Constructor<?> c = Has1.class.getConstructor(int.class);
        assertEquals(c, AlgorithmBuilderUtil.findAutoconfigConstructor(Has1.class));
        assertThrows(IllegalArgumentException.class, () -> AlgorithmBuilderUtil.findAutoconfigConstructor(Has2.class));
    }

    private enum FakeEnum {
        A, B, C
    }
}
