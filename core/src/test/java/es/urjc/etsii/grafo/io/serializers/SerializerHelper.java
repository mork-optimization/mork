package es.urjc.etsii.grafo.io.serializers;

import es.urjc.etsii.grafo.solver.services.events.types.SolutionGeneratedEvent;
import es.urjc.etsii.grafo.solver.services.reference.ReferenceResult;
import es.urjc.etsii.grafo.solver.services.reference.ReferenceResultProvider;
import es.urjc.etsii.grafo.testutil.HelperFactory;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestSolution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SerializerHelper {

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
                HelperFactory.solutionGenerated("fakeInstance", "fakeExp", "fakeAlg", -1, 2, 10, 8),
                HelperFactory.solutionGenerated("fakeInstance0", "fakeExp2", "fakeAlg2", 2, 4, 12, 7),
                HelperFactory.solutionGenerated("fakeInstance1", "fakeExp3", "fakeAlg3", 3, 5, 14, 6)
        );
    }
}
