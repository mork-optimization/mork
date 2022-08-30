package es.urjc.etsii.grafo.autoconfig.testutil;

import es.urjc.etsii.grafo.autoconfig.AlgorithmBuilderUtil;
import es.urjc.etsii.grafo.autoconfig.fakecomponents.FakeGRASPConstructive;
import es.urjc.etsii.grafo.create.grasp.GRASPListManager;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestMove;
import es.urjc.etsii.grafo.testutil.TestSolution;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

public class AlgorithmBuilderUtilTest {

    @Test
    void testFindMultipleConstructors() {
        Map<String, Class<?>> params = Map.of(
                "alpha", String.class,
                "tetha", int.class
        );

        var constructor = AlgorithmBuilderUtil.findConstructor(AlgorithmA.class, params);
        Assertions.assertNotNull(constructor);
        Assertions.assertDoesNotThrow(() -> constructor.newInstance(0, "0"));
    }

    @Test
    void testFindNameCheck() {
        Map<String, Class<?>> params = Map.of(
                "alpha", byte.class
        );

        var constructor = AlgorithmBuilderUtil.findConstructor(AlgorithmA.class, params);
        Assertions.assertNotNull(constructor);
        var cParams = constructor.getParameters();
        Assertions.assertEquals(1, cParams.length);
        Assertions.assertEquals("alpha", cParams[0].getName());
        Assertions.assertDoesNotThrow(() -> constructor.newInstance(1));
    }

    @Test
    void testFindNoArg() {
        Map<String, Class<?>> params = Map.of(
        );

        var constructor = AlgorithmBuilderUtil.findConstructor(AlgorithmA.class, params);
        Assertions.assertNotNull(constructor);
        Assertions.assertDoesNotThrow(() -> constructor.newInstance());
    }

    @Test
    void testFindImpossible() {
        Map<String, Class<?>> params = Map.of(
                "alpha", String.class
        );

        var constructor = AlgorithmBuilderUtil.findConstructor(AlgorithmA.class, params);
        Assertions.assertNull(constructor);
    }

    @Test
    void buildGrasp(){
        Map<String, Object> params = Map.of(
                "maximizing", false,
                "alpha", 0.75,
                "candidateListManager", new GRASPListManager<TestMove, TestSolution, TestInstance>() {
                    @Override
                    public List<TestMove> buildInitialCandidateList(TestSolution solution) {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public List<TestMove> updateCandidateList(TestSolution solution, TestMove move, List<TestMove> candidateList, int index) {
                        return buildInitialCandidateList(solution);
                    }
                });
        var greedyRandom = AlgorithmBuilderUtil.build(FakeGRASPConstructive.class, params);
        Assertions.assertNotNull(greedyRandom);
    }
}
