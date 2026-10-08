package es.urjc.etsii.grafo.autoconfig.builder;

import es.urjc.etsii.grafo.autoconfig.fakecomponents.TestAlgorithmA;
import es.urjc.etsii.grafo.autoconfig.irace.AlgorithmConfiguration;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestSolution;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

class AlgorithmBuilderTest {

    @Test
    void buildFromStringParamsWithAlgorithmName() {
        var expectedAlgorithm = new TestAlgorithmA();
        var builder = new AlgorithmBuilder<TestSolution, TestInstance>() {
            @Override
            public TestAlgorithmA buildFromConfig(AlgorithmConfiguration config) {
                assertEquals("random", config.getValue("constructive").orElseThrow());
                assertEquals(9, config.getValueAsInt("cyclelength").orElseThrow());
                return expectedAlgorithm;
            }
        };

        var algorithm = builder.buildFromStringParams(
                "custom-name",
                "constructive=random cyclelength=9"
        );

        assertSame(expectedAlgorithm, algorithm);
        assertEquals("custom-name", algorithm.getName());
    }
}
