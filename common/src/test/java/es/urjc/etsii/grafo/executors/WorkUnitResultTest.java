package es.urjc.etsii.grafo.executors;

import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestSolution;
import es.urjc.etsii.grafo.util.StringUtil;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Random;

import static es.urjc.etsii.grafo.executors.WorkUnitResult.computeSolutionProperties;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WorkUnitResultTest {

    TestInstance instance = new TestInstance("dummy");


    @Test
    void customPropertiesTest() {
        TestSolution emptyProperties = new TestSolution(instance, 0, new HashMap<>());
        assertTrue(computeSolutionProperties(emptyProperties).isEmpty());

        TestSolution nullProperties = new TestSolution(instance, 0, null);
        assertTrue(computeSolutionProperties(nullProperties).isEmpty());

        var rndString1 = StringUtil.randomAlgorithmName();
        TestSolution singleProperty = new TestSolution(instance, 0, new HashMap<>() {{
            put("key", s -> rndString1);
        }});
        var props = computeSolutionProperties(singleProperty);
        assertEquals(1, props.size());
        assertEquals(rndString1, props.get("key"));

        var rndString2 = StringUtil.randomAlgorithmName();
        var rndNumber = new Random(0).nextDouble();
        TestSolution twoProperties = new TestSolution(instance, 0, new HashMap<>() {{
            put("key1", s -> rndString2);
            put("randomNumber", s -> rndNumber);
        }});
        props = computeSolutionProperties(twoProperties);
        assertEquals(2, props.size());
        assertEquals(rndString2, props.get("key1"));
        assertEquals(rndNumber, props.get("randomNumber"));
    }
}
