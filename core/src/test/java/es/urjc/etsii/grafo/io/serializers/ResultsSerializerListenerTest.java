package es.urjc.etsii.grafo.io.serializers;

import es.urjc.etsii.grafo.algorithms.FMode;
import es.urjc.etsii.grafo.executors.WorkUnitResult;
import es.urjc.etsii.grafo.events.MorkEventListener;
import es.urjc.etsii.grafo.results.ResultStore;
import es.urjc.etsii.grafo.solution.Objective;
import es.urjc.etsii.grafo.testutil.*;
import es.urjc.etsii.grafo.util.Context;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.stubbing.Answer;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.*;

public class ResultsSerializerListenerTest {
    private static final Objective<TestMove, TestSolution, TestInstance> OBJ_MIN = Objective.of("Test", FMode.MINIMIZE, TestSolution::getScore, TestMove::getScoreChange);

    private final String expName = "TestExp";
    private ResultStore<TestSolution, TestInstance> resultStore;
    private ResultsSerializer<TestSolution, TestInstance> serializer1; // After each instance should be triggered
    private ResultsSerializer<TestSolution, TestInstance> serializer2; // Only triggered after the experiment ends
    private ResultsSerializer<TestSolution, TestInstance> serializer3; // Always disabled


    private List<WorkUnitResult<TestSolution,TestInstance>> data;

    private ResultsSerializerListener<TestSolution, TestInstance> listener;

    @SuppressWarnings("unchecked")
    @BeforeEach
    public void prepareMocks(@TempDir Path temp) {
        Context.Configurator.setObjectives(OBJ_MIN);
        data = Arrays.asList(
                TestHelperFactory.solutionGenerated("fakeInstance", "fakeExp", "fakeAlg", 1, 2, 10, 8),
                TestHelperFactory.solutionGenerated("fakeInstance2", "fakeExp2", "fakeAlg2", 2, 4, 12, 7),
                TestHelperFactory.solutionGenerated("fakeInstance3", "fakeExp3", "fakeAlg3", 3, 5, 14, 6)
        );

        Answer<?> answer = inv -> {
            String experimentName = inv.getArgument(0);
            List<?> data = inv.getArgument(1);
            Path p = inv.getArgument(2);
            p.toFile().createNewFile();
            return null;
        };

        this.resultStore = (ResultStore<TestSolution, TestInstance>) mock(ResultStore.class);
        when(resultStore.getResultsForExperiment(expName)).thenReturn(data);


        this.serializer1 = mock(ResultsSerializer.class);
        when(this.serializer1.getConfig()).thenReturn(TestSerializerConfigUtils.create(true, ResultExportFrequency.PER_INSTANCE, temp));
        doAnswer(answer).when(this.serializer1).serializeResults(anyString(), anyList(), any());

        this.serializer2 = mock(ResultsSerializer.class);
        when(this.serializer2.getConfig()).thenReturn(TestSerializerConfigUtils.create(true, ResultExportFrequency.EXPERIMENT_END, temp));
        doAnswer(answer).when(this.serializer2).serializeResults(anyString(), anyList(), any());

        this.serializer3 = mock(ResultsSerializer.class);
        when(this.serializer3.getConfig()).thenReturn(TestSerializerConfigUtils.create(false, ResultExportFrequency.EXPERIMENT_END, temp));
        doAnswer(answer).when(this.serializer3).serializeResults(anyString(), anyList(), any());

        this.listener = new ResultsSerializerListener<>(this.resultStore, Arrays.asList(this.serializer1, this.serializer2, this.serializer3));
    }


    @Test
    public void checkOnExperimentEnd() {
        this.listener.serializeAtExperimentEnd(expName, 0L);
        verify(this.serializer1, atLeastOnce()).getConfig();
        verify(this.serializer2, atLeastOnce()).getConfig();
        verify(this.serializer3, atLeastOnce()).getConfig();

        verify(this.serializer1, times(0)).serializeResults(anyString(), anyList(), any());
        verify(this.serializer2, times(1)).serializeResults(anyString(), anyList(), any());
        verify(this.serializer3, times(0)).serializeResults(anyString(), anyList(), any());
        verify(this.resultStore).releaseSolutionsForExperiment(expName);

        var order = inOrder(this.resultStore, this.serializer2);
        order.verify(this.resultStore).getResultsForExperiment(expName);
        order.verify(this.serializer2).serializeResults(eq(expName), same(data), any());
        order.verify(this.resultStore).releaseSolutionsForExperiment(expName);
    }

    @Test
    public void checkOnInstanceEnd() {
        this.listener.serializePerInstance(expName, 0L);
        verify(this.serializer1, atLeastOnce()).getConfig();
        verify(this.serializer2, atLeastOnce()).getConfig();
        verify(this.serializer3, atLeastOnce()).getConfig();

        verify(this.serializer1, times(1)).serializeResults(anyString(), anyList(), any());
        verify(this.serializer2, times(0)).serializeResults(anyString(), anyList(), any());
        verify(this.serializer3, times(0)).serializeResults(anyString(), anyList(), any());
        verify(this.resultStore, never()).releaseSolutionsForExperiment(anyString());
    }

    @Test
    public void skipIfNoResults1() {
        this.listener.serializePerInstance("asdasdas", 0L);

        verify(this.serializer1, times(0)).serializeResults(anyString(), anyList(), any());
        verify(this.serializer2, times(0)).serializeResults(anyString(), anyList(), any());
        verify(this.serializer3, times(0)).serializeResults(anyString(), anyList(), any());
    }

    @Test
    public void skipIfNoResults2() {
        this.listener.serializeAtExperimentEnd("asdasdas", 0L);

        verify(this.serializer1, times(0)).serializeResults(anyString(), anyList(), any());
        verify(this.serializer2, times(0)).serializeResults(anyString(), anyList(), any());
        verify(this.serializer3, times(0)).serializeResults(anyString(), anyList(), any());
        verify(this.resultStore).releaseSolutionsForExperiment("asdasdas");
    }

    @Test
    public void wrapSerializerFailuresWithContext() {
        doThrow(new IllegalArgumentException("boom")).when(this.serializer2).serializeResults(anyString(), anyList(), any());

        var exception = Assertions.assertThrows(
                SerializerExecutionException.class,
                () -> this.listener.serializeAtExperimentEnd(expName, 0L)
        );

        String message = exception.getMessage();
        Assertions.assertTrue(message.contains("Result serializer"));
        Assertions.assertTrue(message.contains("executing serializer"));
        Assertions.assertTrue(message.contains(expName));
        Assertions.assertTrue(message.contains(ResultExportFrequency.EXPERIMENT_END.name()));
        Assertions.assertTrue(message.contains("Result count: " + data.size()));
        Assertions.assertTrue(message.contains(".tmp"));
        Assertions.assertTrue(message.contains("Root cause: IllegalArgumentException: boom"));
        Assertions.assertInstanceOf(IllegalArgumentException.class, exception.getCause());
        verify(this.resultStore).releaseSolutionsForExperiment(expName);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void failingSerializerDoesNotPreventLaterFinalSerializersFromSeeingSolutions() {
        var laterSerializer = (ResultsSerializer<TestSolution, TestInstance>) mock(ResultsSerializer.class);
        var finalConfig = serializer2.getConfig();
        when(laterSerializer.getConfig()).thenReturn(finalConfig);
        doAnswer(invocation -> {
            var results = (List<WorkUnitResult<TestSolution, TestInstance>>) invocation.getArgument(1);
            Assertions.assertTrue(results.stream().allMatch(result -> result.solution() != null));
            invocation.<Path>getArgument(2).toFile().createNewFile();
            return null;
        }).when(laterSerializer).serializeResults(anyString(), anyList(), any());
        doThrow(new IllegalArgumentException("boom")).when(serializer2).serializeResults(anyString(), anyList(), any());
        listener = new ResultsSerializerListener<>(resultStore, List.of(serializer2, laterSerializer));

        Assertions.assertThrows(
                SerializerExecutionException.class,
                () -> listener.serializeAtExperimentEnd(expName, 0L)
        );

        verify(laterSerializer).serializeResults(eq(expName), same(data), any());
        var ordered = inOrder(serializer2, laterSerializer, resultStore);
        ordered.verify(serializer2).serializeResults(eq(expName), same(data), any());
        ordered.verify(laterSerializer).serializeResults(eq(expName), same(data), any());
        ordered.verify(resultStore).releaseSolutionsForExperiment(expName);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void serializerSetupFailureDoesNotPreventLaterFinalSerializer() {
        var brokenSerializer = (ResultsSerializer<TestSolution, TestInstance>) mock(ResultsSerializer.class);
        when(brokenSerializer.getConfig()).thenThrow(new IllegalStateException("config failed"));
        listener = new ResultsSerializerListener<>(resultStore, List.of(brokenSerializer, serializer2));

        var failure = Assertions.assertThrows(
                SerializerExecutionException.class,
                () -> listener.serializeAtExperimentEnd(expName, 0L)
        );

        Assertions.assertTrue(failure.getMessage().contains("Root cause: IllegalStateException: config failed"));
        Assertions.assertTrue(failure.getMessage().contains("<not resolved>"));
        verify(serializer2).serializeResults(eq(expName), same(data), any());
        var ordered = inOrder(brokenSerializer, serializer2, resultStore);
        ordered.verify(brokenSerializer).getConfig();
        ordered.verify(serializer2).serializeResults(eq(expName), same(data), any());
        ordered.verify(resultStore).releaseSolutionsForExperiment(expName);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void realStoreReleasesStrongSolutionOnlyAfterAllFinalSerializersOnFailure(@TempDir Path temp) {
        var realStore = new ResultStore<TestSolution, TestInstance>();
        var result = TestHelperFactory.solutionGenerated("instance", expName, "algorithm", 1, 2, 10, 8);
        realStore.store(result);
        var failingSerializer = (ResultsSerializer<TestSolution, TestInstance>) mock(ResultsSerializer.class);
        var laterSerializer = (ResultsSerializer<TestSolution, TestInstance>) mock(ResultsSerializer.class);
        when(failingSerializer.getConfig()).thenReturn(TestSerializerConfigUtils.create(true, ResultExportFrequency.EXPERIMENT_END, temp));
        when(laterSerializer.getConfig()).thenReturn(TestSerializerConfigUtils.create(true, ResultExportFrequency.EXPERIMENT_END, temp));
        doThrow(new IllegalArgumentException("boom"))
                .when(failingSerializer).serializeResults(anyString(), anyList(), any());
        doAnswer(invocation -> {
            var results = (List<WorkUnitResult<TestSolution, TestInstance>>) invocation.getArgument(1);
            Assertions.assertSame(result.solution(), results.getFirst().solution());
            invocation.<Path>getArgument(2).toFile().createNewFile();
            return null;
        }).when(laterSerializer).serializeResults(anyString(), anyList(), any());
        var service = new ResultsSerializerListener<>(realStore, List.of(failingSerializer, laterSerializer));

        Assertions.assertThrows(
                SerializerExecutionException.class,
                () -> service.serializeAtExperimentEnd(expName, 0L)
        );

        Assertions.assertNull(realStore.findResult(result.resultId()).orElseThrow().solution());
        Assertions.assertSame(result.solution(), realStore.findSolution(result.resultId()).orElseThrow());
    }

    @Test
    public void isNotAnEventListener() {
        boolean hasListenerMethod = Arrays.stream(ResultsSerializerListener.class.getDeclaredMethods())
                .anyMatch(method -> method.isAnnotationPresent(MorkEventListener.class));

        Assertions.assertFalse(hasListenerMethod);
    }
}
