package es.urjc.etsii.grafo.autoconfig.fakecomponents;

import es.urjc.etsii.grafo.annotations.AutoconfigConstructor;
import es.urjc.etsii.grafo.create.Constructive;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestSolution;

public class DoNothingConstructive extends Constructive<TestSolution, TestInstance> {

    @AutoconfigConstructor
    public DoNothingConstructive() {}

    @Override
    public TestSolution construct(TestSolution solution) {
        throw new UnsupportedOperationException("Only for testing autoconfig");
    }
}
