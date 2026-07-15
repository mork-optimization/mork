package es.urjc.etsii.grafo.testutil;

import es.urjc.etsii.grafo.events.types.ExperimentEndedEvent;
import es.urjc.etsii.grafo.events.types.InstanceProcessingEndedEvent;
import es.urjc.etsii.grafo.experiment.reference.ReferenceResult;
import es.urjc.etsii.grafo.experiment.reference.ReferenceResultProvider;
import es.urjc.etsii.grafo.executors.WorkUnitResult;
import es.urjc.etsii.grafo.config.InstanceConfiguration;
import es.urjc.etsii.grafo.config.SolverConfig;
import es.urjc.etsii.grafo.io.InstanceImporter;
import es.urjc.etsii.grafo.io.InstanceManager;
import es.urjc.etsii.grafo.metrics.MetricsStorage;

import java.io.BufferedReader;
import java.util.*;

public class TestHelperFactory {
    public static WorkUnitResult<TestSolution, TestInstance> solutionGenerated(String instanceName, String expName, String algName, int iter, double score, long time, long ttb){
        return solutionGenerated(instanceName, expName, algName, iter, score, time, ttb, Map.of());
    }

    public static WorkUnitResult<TestSolution, TestInstance> solutionGenerated(String instanceName, String expName, String algName, int iter, double score, long time, long ttb, Map<String, Object> solutionProperties){
        var solution = new TestSolution(new TestInstance(instanceName), score);
        var algorithm = new TestAlgorithm(algName);
        return new WorkUnitResult<>(
                UUID.randomUUID(),
                true,
                expName,
                instanceName,
                instanceName,
                algorithm,
                String.valueOf(iter),
                solution,
                Map.of("Test", score),
                solutionProperties,
                time,
                ttb,
                new MetricsStorage(),
                new ArrayList<>()
        );
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

    public static List<WorkUnitResult<TestSolution, TestInstance>> solutionGenerator() {
        return Arrays.asList(
                TestHelperFactory.solutionGenerated("fakeInstance", "fakeExp", "fakeAlg", -1, 2, 10, 8),
                TestHelperFactory.solutionGenerated("fakeInstance0", "fakeExp2", "fakeAlg2", 2, 4, 12, 7),
                TestHelperFactory.solutionGenerated("fakeInstance1", "fakeExp3", "fakeAlg3", 3, 5, 14, 6)
        );
    }

    public static InstanceManager<TestInstance> emptyInstanceManager(){
        return testInstanceManager(Map.of());
    }

    public static InstanceManager<TestInstance> simpleInstanceManager(TestInstance instance){
        return testInstanceManager(Map.of(instance.getId(), instance));
    }

    private static InstanceManager<TestInstance> testInstanceManager(Map<String, TestInstance> instances) {
        var instanceConfiguration = new InstanceConfiguration();
        instanceConfiguration.setPath(Map.of("default", "."));

        var importer = new InstanceImporter<TestInstance>() {
            @Override
            public TestInstance importInstance(BufferedReader reader, String suggestedInstanceId) {
                throw new UnsupportedOperationException("Test instance manager does not import instances");
            }
        };

        return new InstanceManager<>(instanceConfiguration, new SolverConfig(), importer) {
            @Override
            public synchronized List<String> getInstanceSolveOrder(String expName) {
                return List.copyOf(instances.keySet());
            }

            @Override
            public synchronized TestInstance getInstance(String path) {
                var instance = instances.get(path);
                if (instance == null) {
                    throw new IllegalArgumentException("Unknown test instance: " + path);
                }
                return instance;
            }
        };
    }
}
