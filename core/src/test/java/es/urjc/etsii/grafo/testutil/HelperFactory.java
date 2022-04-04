package es.urjc.etsii.grafo.testutil;

import es.urjc.etsii.grafo.solver.SolverConfig;
import es.urjc.etsii.grafo.solver.services.events.types.ExperimentEndedEvent;
import es.urjc.etsii.grafo.solver.services.events.types.InstanceProcessingEndedEvent;
import es.urjc.etsii.grafo.solver.services.events.types.SolutionGeneratedEvent;
import es.urjc.etsii.grafo.util.random.RandomManager;
import es.urjc.etsii.grafo.util.random.RandomType;

public class HelperFactory {
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
}
