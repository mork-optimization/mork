package es.urjc.etsii.grafo.testutil;

import es.urjc.etsii.grafo.solver.SolverConfig;
import es.urjc.etsii.grafo.solver.services.events.types.ExperimentEndedEvent;
import es.urjc.etsii.grafo.solver.services.events.types.InstanceProcessingEndedEvent;
import es.urjc.etsii.grafo.solver.services.events.types.SolutionGeneratedEvent;
import es.urjc.etsii.grafo.solver.services.reference.ReferenceResult;
import es.urjc.etsii.grafo.solver.services.reference.ReferenceResultProvider;
import es.urjc.etsii.grafo.util.random.RandomManager;
import es.urjc.etsii.grafo.util.random.RandomType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TestHelperFactory {
    public static RandomManager getRandomManager(RandomType type, int seed, int repetitions){
        SolverConfig solverConfig = new SolverConfig();
        solverConfig.setSeed(seed);
        solverConfig.setRepetitions(repetitions);
        solverConfig.setRandomType(type);
        RandomManager r = new RandomManager(solverConfig);
        RandomManager.reinitialize(type, seed, repetitions);
        RandomManager.reset(0);
        return r;
    }
    public static SolutionGeneratedEvent<TestSolution, TestInstance> solutionGenerated(String instanceName, String expName, String algName, int iter, double score, long time, long ttb){
        var solution = new TestSolution(new TestInstance(instanceName), score);
        var algorithm = new TestAlgorithm(algName);
        return new SolutionGeneratedEvent<>(iter, solution, expName, algorithm, time, ttb);
    }
    public static InstanceProcessingEndedEvent instanceEnd(){
        return instanceEnd("TestExp");
    }

    public static InstanceProcessingEndedEvent instanceEnd(String expName){
        return new InstanceProcessingEndedEvent(expName, "TestInstance", 10000, 0);
    }

    public static ExperimentEndedEvent experimentEnd() {
        return experimentEnd("TestExp");
    }

    public static ExperimentEndedEvent experimentEnd(String expName) {
        return new ExperimentEndedEvent(expName, 10000, 0);
    }

    public static ArrayList<ReferenceResultProvider> referencesGenerator(double score, double timeInSeconds) {
        return new ArrayList<>(Collections.singleton(new ReferenceResultProvider() {
            @Override
            public ReferenceResult getValueFor(String instanceName) {
                ReferenceResult rs = new ReferenceResult();
                rs.setScore(score);
                rs.setTimeInSeconds(timeInSeconds);
                return rs;
            }

            @Override
            public String getProviderName() {
                return "TestProvider";
            }
        }));

    }

    public static List<SolutionGeneratedEvent<TestSolution, TestInstance>> solutionGenerator() {
        return Arrays.asList(
                TestHelperFactory.solutionGenerated("fakeInstance", "fakeExp", "fakeAlg", -1, 2, 10, 8),
                TestHelperFactory.solutionGenerated("fakeInstance0", "fakeExp2", "fakeAlg2", 2, 4, 12, 7),
                TestHelperFactory.solutionGenerated("fakeInstance1", "fakeExp3", "fakeAlg3", 3, 5, 14, 6)
        );
    }
}
