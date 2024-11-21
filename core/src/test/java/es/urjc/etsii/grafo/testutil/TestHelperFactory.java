package es.urjc.etsii.grafo.testutil;

import es.urjc.etsii.grafo.events.types.ExperimentEndedEvent;
import es.urjc.etsii.grafo.events.types.InstanceProcessingEndedEvent;
import es.urjc.etsii.grafo.events.types.SolutionGeneratedEvent;
import es.urjc.etsii.grafo.experiment.reference.ReferenceResult;
import es.urjc.etsii.grafo.experiment.reference.ReferenceResultProvider;
import es.urjc.etsii.grafo.io.InstanceManager;
import es.urjc.etsii.grafo.metrics.MetricsStorage;
import org.mockito.Mockito;

import java.util.*;

import static org.mockito.ArgumentMatchers.anyString;

public class TestHelperFactory {
    public static SolutionGeneratedEvent<TestSolution, TestInstance> solutionGenerated(String instanceName, String expName, String algName, int iter, double score, long time, long ttb){
        var solution = new TestSolution(new TestInstance(instanceName), score);
        var algorithm = new TestAlgorithm(algName);
        return new SolutionGeneratedEvent<>(true, String.valueOf(iter), instanceName, solution, expName, algorithm, time, ttb, new MetricsStorage(), new ArrayList<>());
    }

//    public static SolutionGeneratedEvent<TestSolution, TestInstance> solutionGenerated(String instanceName, String expName, String algName, int iter, double score, long time, long ttb, Map<String, TreeSet<TimeValue>> properties){
//        var solution = new TestSolution(new TestInstance(instanceName), score, properties);
//        var calculatedProperties= WorkUnitResult.calculateProperties(solution);
//
//        var algorithm = new TestAlgorithm(algName);
//        return new SolutionGeneratedEvent<>(iter, solution, expName, algorithm, time, ttb, calculatedProperties);
//    }

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
                rs.addScores(Map.of("Test", score));
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

//    public static List<SolutionGeneratedEvent<TestSolution, TestInstance>> solutionWithCustomPropertiesGenerator() {
//        return Arrays.asList(
//                TestHelperFactory.solutionGenerated("fakeInstance", "fakeExp", "fakeAlg", -1, 2, 10, 8, Map.of("prop1", s-> 4, "prop2", s->1)),
//                TestHelperFactory.solutionGenerated("fakeInstance0", "fakeExp2", "fakeAlg2", 2, 4, 12, 7, Map.of("prop1", s-> 6, "prop2", s->2)),
//                TestHelperFactory.solutionGenerated("fakeInstance1", "fakeExp3", "fakeAlg3", 3, 5, 14, 6, Map.of("prop1", s-> 8, "prop2", s->3))
//        );
//    }


    @SuppressWarnings("unchecked")
    public static InstanceManager<TestInstance> emptyInstanceManager(){
        InstanceManager<TestInstance> instanceManager = Mockito.mock(InstanceManager.class);
        Mockito.when(instanceManager.getInstanceSolveOrder(anyString())).thenReturn(new ArrayList<>());
        return instanceManager;
    }

    @SuppressWarnings("unchecked")
    public static InstanceManager<TestInstance> simpleInstanceManager(TestInstance instance){
        InstanceManager<TestInstance> instanceManager = Mockito.mock(InstanceManager.class);
        Mockito.when(instanceManager.getInstanceSolveOrder(anyString())).thenReturn(List.of(instance.getId()));
        Mockito.when(instanceManager.getInstance(instance.getId())).thenReturn(instance);
        return instanceManager;
    }
}
