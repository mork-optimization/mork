package es.urjc.etsii.grafo.autoconfig.generator;

import es.urjc.etsii.grafo.annotations.ComponentParam;
import es.urjc.etsii.grafo.annotations.IntegerParam;
import es.urjc.etsii.grafo.annotations.RealParam;
import es.urjc.etsii.grafo.autoconfig.inventory.AlgorithmInventoryService;
import es.urjc.etsii.grafo.autoconfig.irace.params.ComponentParameter;
import es.urjc.etsii.grafo.autoconfig.testutil.ComponentWhitelistDuringTesting;
import es.urjc.etsii.grafo.autoconfig.testutil.TestUtil;
import es.urjc.etsii.grafo.improve.Improver;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestSolution;
import es.urjc.etsii.grafo.util.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AlgorithmCandidateGeneratorValidationTest {

    private AlgorithmCandidateGenerator generator;

    @BeforeEach
    void setUp() {
        var inventoryService = new AlgorithmInventoryService(new ComponentWhitelistDuringTesting(), TestUtil.getTestFactories(), TestUtil.getTestProviders());
        inventoryService.runComponentDiscovery("es.urjc.etsii");
        generator = new AlgorithmCandidateGenerator(inventoryService, new DefaultExplorationFilter());
    }

    @Test
    void componentParamFiltersDisallowedCandidatesAndSubclasses() throws NoSuchMethodException {
        var parameter = firstParameter(ComponentHolder.class);
        var types = Map.<Class<?>, Collection<Class<?>>>of(
                Improver.class,
                List.of(AllowedImprover.class, DisallowedImprover.class, DisallowedChildImprover.class)
        );

        ComponentParameter componentParameter = generator.toComponentParameter(types, parameter);

        assertArrayEquals(new Object[]{AllowedImprover.class}, componentParameter.getValues());
    }

    @Test
    void componentParamRequiresKnownComponentType() throws NoSuchMethodException {
        var parameter = firstParameter(UnknownComponentHolder.class);

        assertThrows(IllegalArgumentException.class, () -> generator.toComponentParameter(Map.of(), parameter));
    }

    @Test
    void componentParamRejectsRestrictionsOutsideParameterType() throws NoSuchMethodException {
        var parameter = firstParameter(InvalidRestrictionHolder.class);
        var types = Map.<Class<?>, Collection<Class<?>>>of(
                Improver.class,
                List.of(AllowedImprover.class)
        );

        assertThrows(IllegalArgumentException.class, () -> generator.toComponentParameter(types, parameter));
    }

    @Test
    void multipleParameterAnnotationsAreRejected() throws NoSuchMethodException {
        var parameter = firstParameter(MultipleAnnotationsHolder.class);

        assertThrows(IllegalArgumentException.class, () -> generator.toComponentParameter(Map.of(), parameter));
    }

    @Test
    void invalidIntegerRangeIsRejected() throws NoSuchMethodException {
        var parameter = firstParameter(InvalidIntegerRangeHolder.class);

        assertThrows(IllegalArgumentException.class, () -> generator.toComponentParameter(Map.of(), parameter));
    }

    @Test
    void invalidRealTypeIsRejected() throws NoSuchMethodException {
        var parameter = firstParameter(InvalidRealTypeHolder.class);

        assertThrows(IllegalArgumentException.class, () -> generator.toComponentParameter(Map.of(), parameter));
    }

    @Test
    void realParamRejectsIntegerOnlyTypes() throws NoSuchMethodException {
        var parameter = firstParameter(InvalidRealIntegerHolder.class);

        assertThrows(IllegalArgumentException.class, () -> generator.toComponentParameter(Map.of(), parameter));
    }

    private static Parameter firstParameter(Class<?> clazz) throws NoSuchMethodException {
        return clazz.getConstructors()[0].getParameters()[0];
    }

    public static class ComponentHolder {
        public ComponentHolder(@ComponentParam(disallowed = DisallowedImprover.class) Improver<TestSolution, TestInstance> improver) {
        }
    }

    public static class UnknownComponentHolder {
        public UnknownComponentHolder(@ComponentParam String value) {
        }
    }

    public static class InvalidRestrictionHolder {
        public InvalidRestrictionHolder(@ComponentParam(disallowed = String.class) Improver<TestSolution, TestInstance> improver) {
        }
    }

    public static class MultipleAnnotationsHolder {
        public MultipleAnnotationsHolder(@IntegerParam(min = 0, max = 1) @RealParam(min = 0, max = 1) int value) {
        }
    }

    public static class InvalidIntegerRangeHolder {
        public InvalidIntegerRangeHolder(@IntegerParam(min = 2, max = 1) int value) {
        }
    }

    public static class InvalidRealTypeHolder {
        public InvalidRealTypeHolder(@RealParam(min = 0, max = 1) boolean value) {
        }
    }

    public static class InvalidRealIntegerHolder {
        public InvalidRealIntegerHolder(@RealParam(min = 0, max = 1) int value) {
        }
    }

    public static class AllowedImprover extends Improver<TestSolution, TestInstance> {
        protected AllowedImprover() {
            super(Context.getMainObjective());
        }

        @Override
        public TestSolution improve(TestSolution solution) {
            return solution;
        }
    }

    public static class DisallowedImprover extends AllowedImprover {
    }

    public static class DisallowedChildImprover extends DisallowedImprover {
    }
}
