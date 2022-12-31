package es.urjc.etsii.grafo.algorithms.scattersearch;

import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.create.Constructive;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestSolution;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScatterSearchTest {

    @Test
    void testBuilder(){
        var builder = new ScatterSearchBuilder<TestSolution, TestInstance>();

        // Ensure that certain fields have default values
        assertFalse(builder.name.isBlank());
        assertTrue(builder.maxIterations > 10_000);
        assertTrue(builder.initialRatio > 0);

        // Assign constructor, before empty, after both assigned
        assertNull(builder.constructiveDiverseValues);
        assertNull(builder.constructiveGoodValues);

        // If diverse is null when assigning builder.constructiveGoodValues assign diverse too
        var goodConstructor = getTestConstructive();
        builder.withConstructive(goodConstructor);
        assertNotNull(builder.constructiveDiverseValues);
        assertNotNull(builder.constructiveGoodValues);
        assertEquals(goodConstructor, builder.constructiveDiverseValues);
        assertEquals(goodConstructor, builder.constructiveGoodValues);

        var diverseConstructor = getTestConstructive();
        builder.withConstructiveForDiversity(diverseConstructor);
        assertNotNull(builder.constructiveDiverseValues);
        assertEquals(goodConstructor, builder.constructiveGoodValues);
        assertEquals(diverseConstructor, builder.constructiveDiverseValues);

        // Assigning good constructor again should not override diverse constructor if already assigned
        builder.withConstructive(goodConstructor);
        assertEquals(goodConstructor, builder.constructiveGoodValues);
        assertEquals(diverseConstructor, builder.constructiveDiverseValues);

        // By default a noop improver
        assertNotNull(builder.improver);
        var newImprover = Improver.<TestSolution, TestInstance>nul();
        assertNotEquals(newImprover, builder.improver);
        builder.withImprover(newImprover);
        assertEquals(newImprover, builder.improver);

        // Maximize
        assertNull(builder.mode);
        builder.withSolvingMode(FMode.MINIMIZE);
        assertEquals(FMode.MINIMIZE, builder.mode);

        // Refset size
        builder.withRefsetSize(50);
        assertEquals(50, builder.refsetSize);

        // Diversity ratio, by default none
        assertEquals(0, builder.diversityRatio);
        builder.withDiversity(0.5);
        assertEquals(0.5, builder.diversityRatio);

        // Combinator
        assertNull(builder.combinator);
        var combinator = new SolutionCombinator<TestSolution, TestInstance>() {
            @Override
            protected List<TestSolution> apply(TestSolution left, TestSolution right) {
                return List.of(left, right);
            }
        };
        builder.withCombinator(combinator);
        assertNotNull(builder.combinator);

        // Distance
        assertNull(builder.solutionDistance);
        var distance = new SolutionDistance<TestSolution, TestInstance>() {
            @Override
            public double distances(TestSolution sa, TestSolution sb) {
                return 0;
            }
        };
        builder.withDistance(distance);
        assertNotNull(builder.solutionDistance);

        // Build tests
        assertDoesNotThrow(builder::build);

        // Changing some values to not valid throws IllegalArg
        assertThrows(IllegalArgumentException.class, () -> builder.withMaxIterations(0));
        assertDoesNotThrow(builder::build);

        // Immediately fail if distance, combinator, constructive or improver are null
        assertThrows(NullPointerException.class, () -> builder.withImprover(null));
        builder.withImprover(Improver.nul());
        assertDoesNotThrow(builder::build);

        assertThrows(NullPointerException.class, () -> builder.withConstructive(null));
        builder.withConstructive(goodConstructor);
        assertDoesNotThrow(builder::build);

        assertThrows(NullPointerException.class, () -> builder.withConstructiveForDiversity(null));
        builder.withConstructive(diverseConstructor);
        assertDoesNotThrow(builder::build);

        assertThrows(NullPointerException.class, () -> builder.withDistance(null));
        builder.withDistance(distance);
        assertDoesNotThrow(builder::build);

        assertThrows(NullPointerException.class, () -> builder.withCombinator(null));
        builder.withCombinator(combinator);
        assertDoesNotThrow(builder::build);
    }

    Constructive<TestSolution, TestInstance> getTestConstructive(){
        return new Constructive<>() {
            @Override
            public TestSolution construct(TestSolution solution) {
                return solution;
            }
        };
    }

    @Test
    void testInitialize(){
        // TODO first refactor initializeRefset to reduce complexity
    }

    @Test
    void testSoftReset(){
        // TODO
    }

    @Test
    void replaceWorstNearest(){
        // TODO
    }

    @Test
    void mergeSetByScore(){
        // TODO
    }

    @Test
    void testAlgorithm(){

        // Mock all methods via override
        // TODO
    }
}