package es.urjc.etsii.grafo.events;

import es.urjc.etsii.grafo.events.types.PingEvent;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
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
}
