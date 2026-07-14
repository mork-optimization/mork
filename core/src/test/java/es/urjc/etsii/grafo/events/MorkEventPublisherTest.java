package es.urjc.etsii.grafo.events;

import es.urjc.etsii.grafo.events.types.PingEvent;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MorkEventPublisherTest {

    @Test
    void publishQueuesEventsAndDispatchesEnvelopes() throws InterruptedException {
        var listenerPublisher = mock(ApplicationEventPublisher.class);
        var messagingTemplate = mock(SimpMessagingTemplate.class);
        var eventLog = new InMemoryEventLog();
        var publisher = new MorkEventPublisher(listenerPublisher, messagingTemplate, eventLog);
        var dispatchThread = new AtomicReference<String>();
        var latch = new CountDownLatch(1);
        long earliestAcceptance = System.currentTimeMillis();

        doAnswer(invocation -> {
            dispatchThread.set(Thread.currentThread().getName());
            latch.countDown();
            return null;
        }).when(listenerPublisher).publishEvent(isA(PingEvent.class));

        try {
            publisher.publish(new PingEvent());

            assertTrue(latch.await(1, TimeUnit.SECONDS));
            assertEquals("mork-event-dispatcher", dispatchThread.get());
            assertEquals(1, eventLog.size());

            var envelope = eventLog.getEvent(0);
            assertEquals(0, envelope.eventId());
            assertEquals("PingEvent", envelope.type());
            assertEquals(Thread.currentThread().getName(), envelope.workerName());
            assertTrue(envelope.timestamp() >= earliestAcceptance);
            assertInstanceOf(PingEvent.class, envelope.payload());
            verify(messagingTemplate).convertAndSend("/topic/events", envelope);
        } finally {
            publisher.destroy();
        }
    }

    @Test
    void mutedPublishDropsEventsUntilScopeCloses() {
        var listenerPublisher = mock(ApplicationEventPublisher.class);
        var messagingTemplate = mock(SimpMessagingTemplate.class);
        var eventLog = new InMemoryEventLog();
        var publisher = new MorkEventPublisher(listenerPublisher, messagingTemplate, eventLog);

        try {
            try (var ignored = publisher.mute()) {
                publisher.publish(new PingEvent());
            }

            publisher.publish(new PingEvent());

            verify(listenerPublisher, timeout(1_000).times(1)).publishEvent(isA(PingEvent.class));
            assertEquals(1, eventLog.size());
        } finally {
            publisher.destroy();
        }
    }

    @Test
    void destroyDrainsQueuedEvents() {
        var listenerPublisher = mock(ApplicationEventPublisher.class);
        var messagingTemplate = mock(SimpMessagingTemplate.class);
        var eventLog = new InMemoryEventLog();
        var publisher = new MorkEventPublisher(listenerPublisher, messagingTemplate, eventLog);

        for (int i = 0; i < 10; i++) {
            publisher.publish(new PingEvent());
        }

        publisher.destroy();

        assertEquals(10, eventLog.size());
        verify(listenerPublisher, times(10)).publishEvent(isA(PingEvent.class));
    }

    @Test
    void manualSpringContextCloseDrainsPendingEvents() {
        var context = new AnnotationConfigApplicationContext();
        var messagingTemplate = mock(SimpMessagingTemplate.class);
        var eventLog = new InMemoryEventLog();
        context.registerBean(
                MorkEventPublisher.class,
                () -> new MorkEventPublisher(context, messagingTemplate, eventLog)
        );
        context.refresh();
        var publisher = context.getBean(MorkEventPublisher.class);
        for (int i = 0; i < 25; i++) {
            publisher.publish(new PingEvent());
        }

        context.close();

        assertEquals(25, eventLog.size());
    }

    @Test
    void sinkFailuresAreIsolatedAndDoNotStopLaterEvents() {
        var listenerPublisher = mock(ApplicationEventPublisher.class);
        var messagingTemplate = mock(SimpMessagingTemplate.class);
        var eventLog = new InMemoryEventLog();
        var publisher = new MorkEventPublisher(listenerPublisher, messagingTemplate, eventLog);

        doThrow(new IllegalStateException("conversion failed"))
                .doNothing()
                .when(messagingTemplate).convertAndSend(eq("/topic/events"), any(EventEnvelope.class));
        doThrow(new IllegalStateException("listener failed"))
                .doNothing()
                .when(listenerPublisher).publishEvent(isA(PingEvent.class));

        publisher.publish(new PingEvent());
        publisher.publish(new PingEvent());
        publisher.drainAndStop();

        assertEquals(2, eventLog.size());
        verify(messagingTemplate, times(2)).convertAndSend(eq("/topic/events"), any(EventEnvelope.class));
        verify(listenerPublisher, times(2)).publishEvent(isA(PingEvent.class));
    }

    @Test
    void eventLogRecoversAfterATransientAppendFailure() {
        var listenerPublisher = mock(ApplicationEventPublisher.class);
        var messagingTemplate = mock(SimpMessagingTemplate.class);
        var eventLog = new InMemoryEventLog() {
            private boolean failNextAppend = true;

            @Override
            public synchronized void append(EventEnvelope envelope) {
                if (failNextAppend) {
                    failNextAppend = false;
                    throw new IllegalStateException("transient log failure");
                }
                super.append(envelope);
            }
        };
        var publisher = new MorkEventPublisher(listenerPublisher, messagingTemplate, eventLog);

        publisher.publish(new PingEvent());
        publisher.publish(new PingEvent());
        publisher.drainAndStop();

        assertEquals(1, eventLog.size());
        assertEquals(1, eventLog.getEvent(1).eventId());
        verify(listenerPublisher, times(2)).publishEvent(isA(PingEvent.class));
        verify(messagingTemplate, times(2)).convertAndSend(eq("/topic/events"), any(EventEnvelope.class));
    }

    @Test
    void queueExhaustionFailsFastWithoutLosingAcceptedEvents() throws InterruptedException {
        var listenerPublisher = mock(ApplicationEventPublisher.class);
        var messagingTemplate = mock(SimpMessagingTemplate.class);
        var eventLog = new InMemoryEventLog();
        var publisher = new MorkEventPublisher(listenerPublisher, messagingTemplate, eventLog, 2);
        var listenerEntered = new CountDownLatch(1);
        var releaseListener = new CountDownLatch(1);
        var listenerCalls = new AtomicInteger();

        doAnswer(invocation -> {
            if (listenerCalls.incrementAndGet() == 1) {
                listenerEntered.countDown();
                assertTrue(releaseListener.await(5, TimeUnit.SECONDS));
            }
            return null;
        }).when(listenerPublisher).publishEvent(isA(PingEvent.class));

        try {
            publisher.publish(new PingEvent());
            assertTrue(listenerEntered.await(1, TimeUnit.SECONDS));
            publisher.publish(new PingEvent());
            publisher.publish(new PingEvent());

            var failure = assertThrows(IllegalStateException.class, () -> publisher.publish(new PingEvent()));
            assertTrue(failure.getMessage().contains("listeners are likely too slow"));
        } finally {
            releaseListener.countDown();
            publisher.destroy();
        }

        assertEquals(3, eventLog.size());
        assertEquals(3, listenerCalls.get());
    }

    @Test
    void drainProcessesEventsPublishedRecursivelyByListeners() {
        var listenerPublisher = mock(ApplicationEventPublisher.class);
        var messagingTemplate = mock(SimpMessagingTemplate.class);
        var eventLog = new InMemoryEventLog();
        var publisher = new MorkEventPublisher(listenerPublisher, messagingTemplate, eventLog);
        var listenerCalls = new AtomicInteger();

        doAnswer(invocation -> {
            if (listenerCalls.incrementAndGet() == 1) {
                publisher.publish(new PingEvent());
            }
            return null;
        }).when(listenerPublisher).publishEvent(isA(PingEvent.class));

        publisher.publish(new PingEvent());
        publisher.drainAndStop();

        assertEquals(2, eventLog.size());
        assertEquals(2, listenerCalls.get());
        assertThrows(IllegalStateException.class, () -> publisher.publish(new PingEvent()));
    }

    @Test
    void dispatcherCannotSynchronouslyDrainButCanStillPublishRecursivelyWhileExternalDrainRuns() {
        var listenerPublisher = mock(ApplicationEventPublisher.class);
        var messagingTemplate = mock(SimpMessagingTemplate.class);
        var eventLog = new InMemoryEventLog();
        var publisher = new MorkEventPublisher(listenerPublisher, messagingTemplate, eventLog);
        var listenerCalls = new AtomicInteger();
        var rejection = new AtomicReference<IllegalStateException>();

        doAnswer(invocation -> {
            if (listenerCalls.incrementAndGet() == 1) {
                rejection.set(assertThrows(IllegalStateException.class, publisher::drainAndStop));
                publisher.publish(new PingEvent());
            }
            return null;
        }).when(listenerPublisher).publishEvent(isA(PingEvent.class));

        publisher.publish(new PingEvent());
        publisher.drainAndStop();

        assertNotNull(rejection.get());
        assertTrue(rejection.get().getMessage().contains("dispatcher thread"));
        assertEquals(2, eventLog.size());
        assertEquals(2, listenerCalls.get());
    }

    @Test
    void finalEventAcceptanceAndDrainingTransitionRejectLaterExternalEvents() {
        var listenerPublisher = mock(ApplicationEventPublisher.class);
        var messagingTemplate = mock(SimpMessagingTemplate.class);
        var eventLog = new InMemoryEventLog();
        var publisher = new MorkEventPublisher(listenerPublisher, messagingTemplate, eventLog);

        publisher.publishFinalAndBeginDraining(new PingEvent());

        var rejection = assertThrows(IllegalStateException.class, () -> publisher.publish(new PingEvent()));
        assertTrue(rejection.getMessage().contains("draining has started"));
        publisher.drainAndStop();
        assertEquals(1, eventLog.size());
    }

    @Test
    void dispatcherInitiatedDestroyStillDrainsRecursiveEventsAndStops() {
        var listenerPublisher = mock(ApplicationEventPublisher.class);
        var messagingTemplate = mock(SimpMessagingTemplate.class);
        var eventLog = new InMemoryEventLog();
        var publisher = new MorkEventPublisher(listenerPublisher, messagingTemplate, eventLog);
        var listenerCalls = new AtomicInteger();

        doAnswer(invocation -> {
            if (listenerCalls.incrementAndGet() == 1) {
                publisher.destroy();
                publisher.publish(new PingEvent());
            }
            return null;
        }).when(listenerPublisher).publishEvent(isA(PingEvent.class));

        publisher.publish(new PingEvent());

        verify(listenerPublisher, timeout(1_000).times(2)).publishEvent(isA(PingEvent.class));
        publisher.destroy();
        assertEquals(2, eventLog.size());
        assertThrows(IllegalStateException.class, () -> publisher.publish(new PingEvent()));
    }

    @Test
    void externalPublicationsAreRejectedOnceDrainingStarts() throws Exception {
        var listenerPublisher = mock(ApplicationEventPublisher.class);
        var messagingTemplate = mock(SimpMessagingTemplate.class);
        var publisher = new MorkEventPublisher(listenerPublisher, messagingTemplate, new InMemoryEventLog(), 10);
        var listenerEntered = new CountDownLatch(1);
        var releaseListener = new CountDownLatch(1);
        var drainThreadStarted = new CountDownLatch(1);

        doAnswer(invocation -> {
            listenerEntered.countDown();
            assertTrue(releaseListener.await(5, TimeUnit.SECONDS));
            return null;
        }).when(listenerPublisher).publishEvent(isA(PingEvent.class));

        publisher.publish(new PingEvent());
        assertTrue(listenerEntered.await(1, TimeUnit.SECONDS));

        try (var executor = Executors.newSingleThreadExecutor()) {
            var drainResult = executor.submit((Callable<Void>) () -> {
                drainThreadStarted.countDown();
                publisher.drainAndStop();
                return null;
            });
            assertTrue(drainThreadStarted.await(1, TimeUnit.SECONDS));

            IllegalStateException rejection = awaitDrainingRejection(publisher);
            assertTrue(rejection.getMessage().contains("draining has started"));
            assertFalse(drainResult.isDone());

            releaseListener.countDown();
            drainResult.get(2, TimeUnit.SECONDS);
        } finally {
            releaseListener.countDown();
            publisher.destroy();
        }
    }

    private IllegalStateException awaitDrainingRejection(MorkEventPublisher publisher) throws InterruptedException {
        List<IllegalStateException> failures = new ArrayList<>();
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(1);
        while (System.nanoTime() < deadline) {
            try {
                publisher.publish(new PingEvent());
            } catch (IllegalStateException e) {
                failures.add(e);
                if (e.getMessage().contains("draining has started")) {
                    return e;
                }
            }
            Thread.sleep(1);
        }
        fail("Publisher did not enter draining state; observed failures: " + failures);
        throw new AssertionError("unreachable");
    }
}
