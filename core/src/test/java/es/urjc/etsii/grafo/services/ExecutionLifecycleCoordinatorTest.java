package es.urjc.etsii.grafo.services;

import es.urjc.etsii.grafo.events.EventWebserverConfig;
import es.urjc.etsii.grafo.events.InMemoryEventLog;
import es.urjc.etsii.grafo.events.MorkEventPublisher;
import es.urjc.etsii.grafo.events.types.ExecutionEndedEvent;
import es.urjc.etsii.grafo.events.types.PingEvent;
import org.junit.jupiter.api.Test;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class ExecutionLifecycleCoordinatorTest {

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

        var coordinator = new ExecutionLifecycleCoordinator(context, publisher, config);
        coordinator.complete(123L);

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
        var coordinator = new ExecutionLifecycleCoordinator(context, publisher, config);

        coordinator.complete(456L);

        verify(publisher).publish(isA(ExecutionEndedEvent.class));
        verify(publisher, never()).beginDraining();
        verify(publisher, never()).drainAndStop();
        verify(context, never()).close();
        assertThrows(IllegalStateException.class, () -> coordinator.complete(789L));
    }

    @Test
    void contextClosesAfterFinalEventAndRecursiveEventsAreConsumed() throws InterruptedException {
        var context = mock(ConfigurableApplicationContext.class);
        var listenerPublisher = mock(ApplicationEventPublisher.class);
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
        }).when(listenerPublisher).publishEvent(org.mockito.ArgumentMatchers.any(Object.class));
        doAnswer(invocation -> {
            eventCountAtClose.set(eventLog.size());
            closeLatch.countDown();
            return null;
        }).when(context).close();

        var publisher = new MorkEventPublisher(listenerPublisher, messagingTemplate, eventLog);
        publisherRef.set(publisher);
        var config = new EventWebserverConfig();
        config.setStopOnExecutionEnd(true);

        new ExecutionLifecycleCoordinator(context, publisher, config).complete(123L);

        assertTrue(closeLatch.await(1, TimeUnit.SECONDS));
        assertEquals(2, eventCountAtClose.get());
        assertTrue(eventLog.getEvent(0).payload() instanceof ExecutionEndedEvent);
        assertTrue(eventLog.getEvent(1).payload() instanceof PingEvent);
    }
}
