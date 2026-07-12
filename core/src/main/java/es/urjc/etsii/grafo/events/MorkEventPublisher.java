package es.urjc.etsii.grafo.events;

import es.urjc.etsii.grafo.events.types.MorkEvent;
import es.urjc.etsii.grafo.util.ExceptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Single async boundary between solver threads and event consumers.
 */
@Component
public class MorkEventPublisher implements DisposableBean {
    private static final Logger log = LoggerFactory.getLogger(MorkEventPublisher.class);
    private static final int MAX_QUEUE_SIZE = 10_000;
    private static final String EVENT_TOPIC = "/topic/events";

    private final BlockingQueue<MorkEvent> eventQueue = new ArrayBlockingQueue<>(MAX_QUEUE_SIZE);
    private final AtomicInteger nextEventId = new AtomicInteger();
    private final AtomicInteger muteDepth = new AtomicInteger();
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final ApplicationEventPublisher listenerPublisher;
    private final SimpMessagingTemplate messagingTemplate;
    private final InMemoryEventLog eventLog;
    private final Thread dispatcherThread;

    /**
     * Build event publisher, creating the single dispatcher thread.
     *
     * @param listenerPublisher Spring event publisher used to notify backend listeners
     * @param messagingTemplate websocket messaging template
     * @param eventLog in-memory replay log
     */
    public MorkEventPublisher(
            ApplicationEventPublisher listenerPublisher,
            SimpMessagingTemplate messagingTemplate,
            InMemoryEventLog eventLog
    ) {
        this.listenerPublisher = listenerPublisher;
        this.messagingTemplate = messagingTemplate;
        this.eventLog = eventLog;
        this.dispatcherThread = new Thread(this::dispatchLoop, "mork-event-dispatcher");
        this.dispatcherThread.start();
    }

    /**
     * Publish an event without executing listeners on the caller thread.
     *
     * @param event event payload
     */
    public void publish(MorkEvent event) {
        if (!running.get()) {
            throw new IllegalStateException("Cannot publish events after event dispatcher has been stopped");
        }
        if (isMuted()) {
            log.debug("Event system muted: {}", event);
            return;
        }
        if (!eventQueue.offer(event)) {
            throw new IllegalStateException("Maximum event queue capacity (%s) reached, cannot keep up".formatted(MAX_QUEUE_SIZE));
        }
    }

    /**
     * Temporarily mute event publishing. Intended for warm-up or other unmeasured work.
     *
     * @return scope that restores the previous mute depth when closed
     */
    public MuteScope mute() {
        muteDepth.incrementAndGet();
        return new MuteScopeImpl();
    }

    /**
     * Check whether events are muted.
     *
     * @return true if events are muted
     */
    public boolean isMuted() {
        return muteDepth.get() > 0;
    }

    private void dispatchLoop() {
        while (running.get() || !eventQueue.isEmpty()) {
            try {
                var event = running.get() ? eventQueue.take() : eventQueue.poll();
                if (event != null) {
                    dispatch(event);
                }
            } catch (InterruptedException e) {
                if (running.get()) {
                    log.warn("Event dispatcher interrupted while running");
                    Thread.currentThread().interrupt();
                    return;
                }
                // Shutdown requested: keep draining already queued events.
            }
        }
    }

    private void dispatch(MorkEvent event) {
        int eventId = nextEventId.getAndIncrement();
        var envelope = new EventEnvelope(
                eventId,
                event.getType(),
                System.currentTimeMillis(),
                event.getWorkerName(),
                event
        );

        eventLog.append(envelope);
        messagingTemplate.convertAndSend(EVENT_TOPIC, envelope);

        try {
            listenerPublisher.publishEvent(event);
        } catch (RuntimeException e) {
            var rootCause = ExceptionUtil.getRootCause(e);
            log.error(
                    "Mork event listener failed for event {}: {}: {}",
                    event.getType(),
                    rootCause.getClass().getSimpleName(),
                    rootCause.getMessage(),
                    e
            );
        }
    }

    /**
     * Shut down the dispatcher when the Spring context closes.
     */
    @Override
    public void destroy() {
        if (running.getAndSet(false)) {
            dispatcherThread.interrupt();
        }
        if (Thread.currentThread() == dispatcherThread) {
            return;
        }
        try {
            dispatcherThread.join(TimeUnit.SECONDS.toMillis(5));
            if (dispatcherThread.isAlive()) {
                log.warn("Event dispatcher did not stop within timeout, {} queued events may remain", eventQueue.size());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Interrupted while waiting for event dispatcher shutdown");
        }
    }

    /**
     * AutoCloseable mute scope.
     */
    public interface MuteScope extends AutoCloseable {
        @Override
        void close();
    }

    private final class MuteScopeImpl implements MuteScope {
        private boolean closed = false;

        private MuteScopeImpl() {
        }

        @Override
        public void close() {
            if (!closed) {
                closed = true;
                muteDepth.decrementAndGet();
            }
        }
    }
}
