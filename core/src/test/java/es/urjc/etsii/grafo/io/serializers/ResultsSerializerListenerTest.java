package es.urjc.etsii.grafo.io.serializers;

import es.urjc.etsii.grafo.io.serializers.AbstractResultSerializerConfig.Frequency;
import es.urjc.etsii.grafo.solver.services.events.AbstractEventStorage;
import es.urjc.etsii.grafo.solver.services.events.types.SolutionGeneratedEvent;
import es.urjc.etsii.grafo.testutil.TestHelperFactory;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestSerializerConfig;
import es.urjc.etsii.grafo.testutil.TestSolution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.stubbing.Answer;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

public class ResultsSerializerListenerTest {

    private final String expName = "TestExp";
    private AbstractEventStorage<TestSolution, TestInstance> eventStorage;
    private ResultsSerializer<TestSolution, TestInstance> serializer1; // After each instance should be triggered
    private ResultsSerializer<TestSolution, TestInstance> serializer2; // Only triggered after the experiment ends
    private ResultsSerializer<TestSolution, TestInstance> serializer3; // Always disabled


    private List<SolutionGeneratedEvent<TestSolution,TestInstance>> data = Arrays.asList(
            TestHelperFactory.solutionGenerated("fakeInstance", "fakeExp", "fakeAlg", 1, 2, 10, 8),
            TestHelperFactory.solutionGenerated("fakeInstance2", "fakeExp2", "fakeAlg2", 2, 4, 12, 7),
            TestHelperFactory.solutionGenerated("fakeInstance3", "fakeExp3", "fakeAlg3", 3, 5, 14, 6)
    );
    private ResultsSerializerListener<TestSolution, TestInstance> listener;

    @SuppressWarnings("unchecked")
    @BeforeEach
    public void prepareMocks(@TempDir Path temp) {
        Answer<?> answer = inv -> {
            String experimentName = inv.getArgument(0);
            List<?> data = inv.getArgument(1);
            Path p = inv.getArgument(2);
            p.toFile().createNewFile();
            return null;
        };

        this.eventStorage = (AbstractEventStorage<TestSolution, TestInstance>) mock(AbstractEventStorage.class);
        when(eventStorage.solutionsInMemory(expName)).thenReturn((long) data.size());
        when(eventStorage.getGeneratedSolEventForExp(expName)).thenReturn(data.stream());


        this.serializer1 = mock(ResultsSerializer.class);
        when(this.serializer1.getConfig()).thenReturn(new TestSerializerConfig(true, Frequency.PER_INSTANCE, temp));
        doAnswer(answer).when(this.serializer1).serializeResults(anyString(), anyList(), any());

        this.serializer2 = mock(ResultsSerializer.class);
        when(this.serializer2.getConfig()).thenReturn(new TestSerializerConfig(true, Frequency.EXPERIMENT_END, temp));
        doAnswer(answer).when(this.serializer2).serializeResults(anyString(), anyList(), any());

        this.serializer3 = mock(ResultsSerializer.class);
        when(this.serializer3.getConfig()).thenReturn(new TestSerializerConfig(false, Frequency.EXPERIMENT_END, temp));
        doAnswer(answer).when(this.serializer3).serializeResults(anyString(), anyList(), any());

        this.listener = new ResultsSerializerListener<>(this.eventStorage, Arrays.asList(this.serializer1, this.serializer2, this.serializer3));
    }


    @Test
    public void checkOnExperimentEnd() {
        this.listener.saveOnExperimentEnd(TestHelperFactory.experimentEnd(expName));
        verify(this.serializer1, atLeastOnce()).getConfig();
        verify(this.serializer2, atLeastOnce()).getConfig();
        verify(this.serializer3, atLeastOnce()).getConfig();

        verify(this.serializer1, times(0)).serializeResults(anyString(), anyList(), any());
        verify(this.serializer2, times(1)).serializeResults(anyString(), anyList(), any());
        verify(this.serializer3, times(0)).serializeResults(anyString(), anyList(), any());
    }

    @Test
    public void checkOnInstanceEnd() {
        this.listener.saveOnInstanceEnd(TestHelperFactory.instanceEnd(expName));
        verify(this.serializer1, atLeastOnce()).getConfig();
        verify(this.serializer2, atLeastOnce()).getConfig();
        verify(this.serializer3, atLeastOnce()).getConfig();

        verify(this.serializer1, times(1)).serializeResults(anyString(), anyList(), any());
        verify(this.serializer2, times(0)).serializeResults(anyString(), anyList(), any());
        verify(this.serializer3, times(0)).serializeResults(anyString(), anyList(), any());
    }

    @Test
    public void skipIfNoResults1() {
        this.listener.saveOnInstanceEnd(TestHelperFactory.instanceEnd("asdasdas"));

        verify(this.serializer1, times(0)).serializeResults(anyString(), anyList(), any());
        verify(this.serializer2, times(0)).serializeResults(anyString(), anyList(), any());
        verify(this.serializer3, times(0)).serializeResults(anyString(), anyList(), any());
    }

    @Test
    public void skipIfNoResults2() {
        this.listener.saveOnExperimentEnd(TestHelperFactory.experimentEnd("asdasdas"));

        verify(this.serializer1, times(0)).serializeResults(anyString(), anyList(), any());
        verify(this.serializer2, times(0)).serializeResults(anyString(), anyList(), any());
        verify(this.serializer3, times(0)).serializeResults(anyString(), anyList(), any());
    }
}
