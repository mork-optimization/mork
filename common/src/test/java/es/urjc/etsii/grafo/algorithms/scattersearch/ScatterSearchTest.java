package es.urjc.etsii.grafo.algorithms.scattersearch;

import es.urjc.etsii.grafo.create.Constructive;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestMove;
import es.urjc.etsii.grafo.testutil.TestSolution;
import es.urjc.etsii.grafo.util.Context;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class ScatterSearchTest {

    private static final Objective<TestMove,TestSolution,TestInstance> minObj = Objective.ofMinimizing("TestMin", TestSolution::getScore, TestMove::getScoreChange);

    @BeforeAll
    static void setupObjectives(){
        Context.Configurator.setObjectives(minObj);
    }

    @Test
    void testBuilder() {
        var builder = new ScatterSearchBuilder<TestSolution, TestInstance>();

        // Ensure that certain fields have default values
        assertFalse(builder.name.isBlank());
        assertTrue(builder.maxIterations > 10_000);
        assertTrue(builder.initialRatio > 0);

        // Assign constructor, before empty, after both assigned
        assertNull(builder.constructiveDiverseValues);
        assertNull(builder.constructiveGoodValues);

        // If diverse is null when assigning builder.constructiveGoodValues assign diverse too
        var goodConstructor = new ScatterSearchTestConstructive();
        builder.withConstructive(goodConstructor);
        assertNotNull(builder.constructiveDiverseValues);
        assertNotNull(builder.constructiveGoodValues);
        assertEquals(goodConstructor, builder.constructiveDiverseValues);
        assertEquals(goodConstructor, builder.constructiveGoodValues);

        var diverseConstructor = new ScatterSearchTestConstructive();
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
        assertNull(builder.objective);
        builder.withObjective(minObj);
        assertEquals(minObj, builder.objective);

        // Refset size
        assertThrows(IllegalArgumentException.class, () -> builder.withRefsetSize(Integer.MAX_VALUE));
        assertThrows(IllegalArgumentException.class, () -> builder.withRefsetSize(-1));
        assertThrows(IllegalArgumentException.class, () -> builder.withRefsetSize(1));
        builder.withRefsetSize(50);
        assertEquals(50, builder.refsetSize);

        // Diversity ratio, by default none
        assertEquals(0, builder.diversityRatio);
        assertThrows(IllegalArgumentException.class, () -> builder.withDiversity(1.01));
        assertThrows(IllegalArgumentException.class, () -> builder.withDiversity(-0.1));
        assertThrows(IllegalArgumentException.class, () -> builder.withDiversity(Double.NaN));
        builder.withDiversity(0.5);
        assertEquals(0.5, builder.diversityRatio);

        // Initial ratio
        assertThrows(IllegalArgumentException.class, () -> builder.withInitialRatio(Integer.MAX_VALUE));
        assertThrows(IllegalArgumentException.class, () -> builder.withInitialRatio(-1));
        assertThrows(IllegalArgumentException.class, () -> builder.withInitialRatio(0));
        builder.withInitialRatio(5);
        assertEquals(5, builder.initialRatio);

        // Name
        assertThrows(NullPointerException.class, () -> builder.withName(null));
        assertThrows(IllegalArgumentException.class, () -> builder.withName(""));
        builder.withName("TestScatterMyName");
        assertEquals(builder.name, "TestScatterMyName");

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
        builder.withMaxIterations(1_000_000);
        assertEquals(1_000_000, builder.maxIterations);
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

    @Test
    void testInitializeSimple() {
        Objective<TestMove,TestSolution, TestInstance> objective = Objective.ofMinimizing("Test", TestSolution::getScore, TestMove::getScoreChange);
        var constructiveGood = new ScatterSearchTestConstructive();
        var constructiveDiverse = new ScatterSearchTestConstructive(50, 1);
        int refsetSize = 10;
        int ratio = 5; // Total 50 generated solutions
        double byDiversityRatio = 0.2;
        var sc = new ScatterSearch<>("Test", ratio, refsetSize, constructiveGood, constructiveDiverse,
                Improver.nul(), new CombinatorTestHelper(), objective, 100, byDiversityRatio,
                new DistanceTestHelper(), true);
        sc.setBuilder(new TestSolutionBuilder());
        var inst = new TestInstance("TestInstance");
        var refset = sc.initializeRefset(TestSolution.class, inst);

        assertEquals(refsetSize * ratio * (1-byDiversityRatio), constructiveGood.getnTimesCalled());
        assertEquals(refsetSize * ratio * byDiversityRatio, constructiveDiverse.getnTimesCalled());
        assertEquals(refsetSize, refset.currentRefset.size());
    }

    @Test
    void testInitializeFailoverDiverse() {
        Objective<TestMove,TestSolution, TestInstance> objective = Objective.ofMinimizing("Test", TestSolution::getScore, TestMove::getScoreChange);
        var constructiveGood = new ScatterSearchTestConstructive(0, 0);
        var constructiveDiverse = new ScatterSearchTestConstructive(50,1);
        int refsetSize = 10;
        int ratio = 5; // Total 50 generated solutions
        double byDiversityRatio = 0;
        var sc = new ScatterSearch<>("Test", ratio, refsetSize, constructiveGood,
                constructiveDiverse, Improver.nul(), new CombinatorTestHelper(),
                objective, 100, byDiversityRatio, new DistanceTestHelper(), true);
        sc.setBuilder(new TestSolutionBuilder());
        var inst = new TestInstance("TestInstance");
        var refset = sc.initializeRefset(TestSolution.class, inst);

        assertEquals(refsetSize * ratio, constructiveGood.getnTimesCalled());
        assertEquals(refsetSize - 1, constructiveDiverse.getnTimesCalled()); // All except first
        assertEquals(refsetSize, refset.currentRefset.size());
    }

    @Test
    void testSoftReset() {
        Objective<TestMove,TestSolution, TestInstance> objective = Objective.ofMinimizing("Test", TestSolution::getScore, TestMove::getScoreChange);
        var constructiveGood = new ScatterSearchTestConstructive();
        var constructiveDiverse = new ScatterSearchTestConstructive(50, 1);
        int refsetSize = 10;
        int ratio = 5; // Total 50 generated solutions
        double byDiversityRatio = 0.2;
        var sc = new ScatterSearch<>("Test", ratio, refsetSize, constructiveGood,
                constructiveDiverse, Improver.nul(), new CombinatorTestHelper(),
                objective, 100, byDiversityRatio, new DistanceTestHelper(), true);
        sc.setBuilder(new TestSolutionBuilder());
        var inst = new TestInstance("TestInstance");

        var refset = sc.initializeRefset(TestSolution.class, inst);

        double bestValue = refset.solutions[0].getScore();
        var knownScores = new HashSet<Double>(1_000);
        for(var solution: refset.solutions){
            knownScores.add(solution.getScore());
        }

        var newScores = new HashSet<Double>(1_000);
        var newRefset = sc.softRestart(TestSolution.class, inst, refset);
        for(var solution: newRefset.solutions){
            newScores.add(solution.getScore());
        }
        assertTrue(newScores.contains(bestValue));
        var intersect = new HashSet<>(knownScores);
        intersect.retainAll(newScores);
        assertEquals(1, intersect.size());
    }

    @Test
    void replaceWorstIsActuallyBest(){
        Objective<TestMove,TestSolution, TestInstance> objective = Objective.ofMaximizing("Test", TestSolution::getScore, TestMove::getScoreChange);

        // When reinitializing the refset, all solutions may improve best value known, and that case is perfectly valid, test it
        var constructiveGood = new ScatterSearchTestConstructive(300, 1);
        var constructiveDiverse = new ScatterSearchTestConstructive(50, 1);
        int refsetSize = 10;
        int ratio = 5; // Total 50 generated solutions
        double byDiversityRatio = 0;
        var sc = new ScatterSearch<>("Test", ratio, refsetSize, constructiveGood,
                constructiveDiverse, Improver.nul(), new CombinatorTestHelper(),
                objective, 100, byDiversityRatio, new DistanceTestHelper(), true);
        sc.setBuilder(new TestSolutionBuilder());
        var inst = new TestInstance("TestInstance");

        var refset = sc.initializeRefset(TestSolution.class, inst);

        double bestValue = refset.solutions[0].getScore();
        var knownScores = new HashSet<Double>(1_000);
        for(var solution: refset.solutions){
            knownScores.add(solution.getScore());
        }

        var newScores = new HashSet<Double>(1_000);
        var newRefset = sc.softRestart(TestSolution.class, inst, refset);
        for(var solution: newRefset.solutions){
            newScores.add(solution.getScore());
        }
        assertFalse(newScores.contains(bestValue)); // Al values are greater
        var intersect = new HashSet<>(knownScores);
        intersect.retainAll(newScores);
        assertEquals(0, intersect.size());
    }
    @Test
    void mergeSetTest(){
        Objective<TestMove,TestSolution, TestInstance> objective = Objective.ofMinimizing("Test", TestSolution::getScore, TestMove::getScoreChange);
        int refsetSize = 10;
        int ratio = 5; // Total 50 generated solutions
        double byDiversityRatio = 0.2;
        var c = new ScatterSearchTestConstructive();
        var sc = new ScatterSearch<>("Test", ratio, refsetSize, c, c, Improver.nul(),
                new CombinatorTestHelper(), objective, 100, byDiversityRatio,
                new DistanceTestHelper(), true);
        var inst = new TestInstance("TestInstance");

        // As we are minimizing, values
        var refset = new RefSet<>(TestSolution.from(inst, 1, 3, 5, 7, 9, 11, 13, 15, 17, 19), 8, 2);
        var newSolutions = TestSolution.from(inst, 13.1, 9, 20);
        var expectedScores = Set.of(1d, 3d, 5d, 7d, 9d, 11d, 13d, 13.1d, 17d, 19d); // 13.1 replaces 15 as it is the worst nearest, 9 not inserted as it is duplicated, 20 is worse

        var insertedSolutions = sc.mergeToSetByScore(refset, new HashSet<>(Arrays.asList(newSolutions)));
        assertEquals(1, insertedSolutions.size());
        assertEquals(13.1, insertedSolutions.iterator().next().getScore());

        var resultingScores = refset.currentRefset.stream().map(TestSolution::getScore).collect(Collectors.toSet());
        assertEquals(expectedScores, resultingScores);
    }

    @Test
    void testAlgorithm() {
        Objective<TestMove,TestSolution, TestInstance> objective = Objective.ofMaximizing("Test", TestSolution::getScore, TestMove::getScoreChange);
        // Example run with 100 iterations, always keeps improving, final solution score must be greater than 1_000_000
        // When reinitializing the refset, all solutions may improve best value known, and that case is perfectly valid, test it
        var constructiveGood = new ScatterSearchTestConstructive(300, 1);
        var constructiveDiverse = new ScatterSearchTestConstructive(50, 1);
        int refsetSize = 10;
        int ratio = 5; // Total 50 generated solutions
        double byDiversityRatio = 0;
        var sc = new ScatterSearch<>("Test", ratio, refsetSize, constructiveGood,
                constructiveDiverse, Improver.nul(), new CombinatorTestHelper(),
                objective, 100, byDiversityRatio, new DistanceTestHelper(), true
        );
        sc.setBuilder(new TestSolutionBuilder());
        var solution = sc.algorithm(new TestInstance("TestInstance"));
        assertTrue(solution.getScore() > 10e20);
    }

    private static class CombinatorTestHelper extends SolutionCombinator<TestSolution, TestInstance> {
        @Override
        protected List<TestSolution> apply(TestSolution left, TestSolution right) {
            return List.of(new TestSolution(left.getInstance(), left.getScore() + right.getScore()));
        }
    }

    private static class DistanceTestHelper extends SolutionDistance<TestSolution, TestInstance> {

        @Override
        public double distances(TestSolution sa, TestSolution sb) {
            return Math.abs(sa.getScore() - sb.getScore());
        }
    }

    private static class ScatterSearchTestConstructive extends Constructive<TestSolution, TestInstance> {

        private final int increment;

        public ScatterSearchTestConstructive(int initialScore, int increment) {
            this.lastScore = initialScore;
            this.increment = increment;
        }

        public ScatterSearchTestConstructive() {
            this(0, 1);
        }

        int lastScore;
        int nTimesCalled = 0;
        @Override
        public TestSolution construct(TestSolution solution) {
            nTimesCalled++;
            solution.setScore(lastScore);
            lastScore += increment;
            return solution;
        }

        public int getnTimesCalled() {
            return nTimesCalled;
        }
    }

}