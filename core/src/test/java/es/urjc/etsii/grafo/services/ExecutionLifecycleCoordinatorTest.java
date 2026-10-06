package es.urjc.etsii.grafo.services;

import es.urjc.etsii.grafo.metrics.timing.TimeStatsService;
import es.urjc.etsii.grafo.events.EventWebserverConfig;
import es.urjc.etsii.grafo.events.InMemoryEventLog;
import es.urjc.etsii.grafo.events.MorkEventListener;
import es.urjc.etsii.grafo.events.MorkEventPublisher;
import es.urjc.etsii.grafo.events.types.ExecutionEndedEvent;
import es.urjc.etsii.grafo.events.types.PingEvent;
import es.urjc.etsii.grafo.monitoring.NormalExperimentRunState;
import es.urjc.etsii.grafo.results.ResultStore;
import es.urjc.etsii.grafo.testutil.TestInstance;
import es.urjc.etsii.grafo.testutil.TestSolution;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class ExecutionLifecycleCoordinatorTest {
    @SuppressWarnings("unchecked")
    private static NormalExperimentRunState<TestSolution, TestInstance> emptyState() {
        return new NormalExperimentRunState<TestSolution, TestInstance>(new ResultStore<>(), mock(ObjectProvider.class));
    }


    @Test
    void drainsBeforeClosingContextOnDedicatedThread() throws InterruptedException {
        var context = mock(ConfigurableApplicationContext.class);
        var publisher = mock(MorkEventPublisher.class);
        var config = new EventWebserverConfig();
        config.setStopOnExecutionEnd(true);
        var closeLatch = new CountDownLatch(1);
        var closeThread = new AtomicReference<String>();
        doAnswer(invocation -> {
            closeThread.set(Thread.currentThread().getName());
            closeLatch.countDown();
            return null;
        }).when(context).close();

        var coordinator = new ExecutionLifecycleCoordinator(context, publisher, config, mock(TimeStatsService.class), emptyState());
        coordinator.complete(123L, ExecutionEndedEvent.Outcome.SUCCEEDED);

        assertTrue(closeLatch.await(1, TimeUnit.SECONDS));
        assertEquals("mork-shutdown", closeThread.get());
        var ordered = inOrder(publisher, context);
        ordered.verify(publisher).publishFinalAndBeginDraining(isA(ExecutionEndedEvent.class));
        ordered.verify(publisher).drainAndStop();
        ordered.verify(context).close();
    }

    @Test
    void shutdownDisabledPublishesWithoutDrainingOrClosing() {
        var context = mock(ConfigurableApplicationContext.class);
        var publisher = mock(MorkEventPublisher.class);
        var config = new EventWebserverConfig();
        config.setStopOnExecutionEnd(false);
        var timing = mock(TimeStatsService.class);
        var coordinator = new ExecutionLifecycleCoordinator(context, publisher, config, timing, emptyState());

        coordinator.complete(456L, ExecutionEndedEvent.Outcome.SUCCEEDED);

        var ordered = inOrder(timing, publisher);
        ordered.verify(timing).close();
        ordered.verify(publisher).publish(isA(ExecutionEndedEvent.class));
        verify(publisher).publish(isA(ExecutionEndedEvent.class));
        verify(publisher, never()).beginDraining();
        verify(publisher, never()).drainAndStop();
        verify(context, never()).close();
        assertThrows(IllegalStateException.class, () -> coordinator.complete(789L, ExecutionEndedEvent.Outcome.SUCCEEDED));
    }

    @Test
    void contextClosesAfterFinalEventAndRecursiveEventsAreConsumed() throws InterruptedException {
        var context = mock(ConfigurableApplicationContext.class);
        var listener = mock(MorkEventListener.class);
        var messagingTemplate = mock(SimpMessagingTemplate.class);
        var eventLog = new InMemoryEventLog();
        var publisherRef = new AtomicReference<MorkEventPublisher>();
        var closeLatch = new CountDownLatch(1);
        var eventCountAtClose = new AtomicReference<Integer>();

        doAnswer(invocation -> {
            if (invocation.getArgument(0) instanceof ExecutionEndedEvent) {
                publisherRef.get().publish(new PingEvent());
            }
            return null;
        }).when(listener).onEvent(org.mockito.ArgumentMatchers.any());
        doAnswer(invocation -> {
            eventCountAtClose.set(eventLog.size());
            closeLatch.countDown();
            return null;
        }).when(context).close();

        var publisher = new MorkEventPublisher(List.of(listener), messagingTemplate, eventLog);
        publisherRef.set(publisher);
        var config = new EventWebserverConfig();
        config.setStopOnExecutionEnd(true);

        new ExecutionLifecycleCoordinator(context, publisher, config, mock(TimeStatsService.class), emptyState()).complete(123L, ExecutionEndedEvent.Outcome.SUCCEEDED);

        assertTrue(closeLatch.await(1, TimeUnit.SECONDS));
        assertEquals(2, eventCountAtClose.get());
        assertTrue(eventLog.getEvent(0).payload() instanceof ExecutionEndedEvent);
        assertTrue(eventLog.getEvent(1).payload() instanceof PingEvent);
    }

    @Test
    void timingFinalizationFailurePreservesTheTerminalOutcomeInBothContracts() {
        var publisher = mock(MorkEventPublisher.class);
        var config = new EventWebserverConfig(); config.setStopOnExecutionEnd(false);
        var timing = mock(TimeStatsService.class);
        doThrow(new IllegalStateException("disk unavailable")).when(timing).close();
        var state = emptyState(); state.prepare(java.util.Map.of());
        new ExecutionLifecycleCoordinator(mock(ConfigurableApplicationContext.class), publisher, config, timing, state)
                .complete(456L, ExecutionEndedEvent.Outcome.SUCCEEDED);
        assertEquals(es.urjc.etsii.grafo.monitoring.NormalExperimentRunState.Phase.COMPLETED_WITH_ERRORS, state.status().phase());
        assertEquals("TIMING", state.issues(state.status().runId(), null, 0, 25).items().getFirst().scope());
        verify(publisher).publish(new ExecutionEndedEvent(456L, ExecutionEndedEvent.Outcome.COMPLETED_WITH_ERRORS));
    }
}
