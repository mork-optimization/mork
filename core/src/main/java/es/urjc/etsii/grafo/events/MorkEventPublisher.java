package es.urjc.etsii.grafo.events;

import es.urjc.etsii.grafo.events.types.MorkEvent;
import es.urjc.etsii.grafo.util.ExceptionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Single async boundary between solver threads and event consumers.
 */
@Component
public class MorkEventPublisher implements DisposableBean {
    private static final Logger log = LoggerFactory.getLogger(MorkEventPublisher.class);
    static final int DEFAULT_QUEUE_CAPACITY = 100_000;
    private static final String EVENT_TOPIC = "/topic/events";

    private final Object acceptanceLock = new Object();
    private final BlockingQueue<QueueItem> eventQueue;
    private final int queueCapacity;
    private final AtomicInteger muteDepth = new AtomicInteger();
    private final Iterable<MorkEventListener> listeners;
    private final SimpMessagingTemplate messagingTemplate;
    private final InMemoryEventLog eventLog;
    private final Thread dispatcherThread;
    private volatile PublisherState state = PublisherState.RUNNING;
    private CompletableFuture<Void> drainCompletion;
    private int queuedEventCount;
    private int nextEventId;

    /**
     * Build event publisher, creating the single dispatcher thread.
     *
     * @param listeners backend event listeners provided by Spring
     * @param messagingTemplate websocket messaging template
     * @param eventLog in-memory replay log
     */
    @Autowired
    public MorkEventPublisher(
            ObjectProvider<MorkEventListener> listeners,
            SimpMessagingTemplate messagingTemplate,
            InMemoryEventLog eventLog
    ) {
        this(listeners, messagingTemplate, eventLog, DEFAULT_QUEUE_CAPACITY);
    }

    /**
     * Build an event publisher with an explicit listener collection.
     *
     * @param listeners backend event listeners
     * @param messagingTemplate websocket messaging template
     * @param eventLog in-memory replay log
     */
    public MorkEventPublisher(
            Iterable<MorkEventListener> listeners,
            SimpMessagingTemplate messagingTemplate,
            InMemoryEventLog eventLog
    ) {
        this(listeners, messagingTemplate, eventLog, DEFAULT_QUEUE_CAPACITY);
    }

    MorkEventPublisher(
            Iterable<MorkEventListener> listeners,
            SimpMessagingTemplate messagingTemplate,
            InMemoryEventLog eventLog,
            int queueCapacity
    ) {
        if (queueCapacity <= 0) {
            throw new IllegalArgumentException("Event queue capacity must be greater than zero");
        }
        this.listeners = listeners;
        this.messagingTemplate = messagingTemplate;
        this.eventLog = eventLog;
        this.queueCapacity = queueCapacity;
        // One slot is reserved for the drain command, so shutdown can always be
        // requested even when every event slot is occupied.
        this.eventQueue = new ArrayBlockingQueue<>(queueCapacity + 1);
        this.dispatcherThread = new Thread(this::dispatchLoop, "mork-event-dispatcher");
        this.dispatcherThread.start();
    }

    /**
     * Publish an event without executing listeners on the caller thread.
     *
     * @param event event payload
     */
    public void publish(MorkEvent event) {
        requireEvent(event);

        synchronized (acceptanceLock) {
            boolean recursiveDispatch = Thread.currentThread() == dispatcherThread;
            if (state == PublisherState.STOPPED || (state == PublisherState.DRAINING && !recursiveDispatch)) {
                throw new IllegalStateException("Cannot publish external events after event dispatcher draining has started");
            }
            if (isMuted()) {
                log.debug("Event system muted: {}", event);
                return;
            }
            enqueueEvent(event);
        }
    }

    /**
     * Atomically accept the terminal event and enter the draining state.
     * No external publisher can insert an event after the terminal event.
     *
     * @param finalEvent terminal event payload
     */
    public void publishFinalAndBeginDraining(MorkEvent finalEvent) {
        requireEvent(finalEvent);
        synchronized (acceptanceLock) {
            if (state != PublisherState.RUNNING) {
                throw new IllegalStateException("Cannot publish a final event after event dispatcher draining has started");
            }
            enqueueEvent(finalEvent);
            startDraining();
        }
    }

    private void requireEvent(MorkEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Event payload cannot be null");
        }
    }

    private void enqueueEvent(MorkEvent event) {
        if (queuedEventCount == queueCapacity) {
            throw new IllegalStateException(
                    "Maximum event queue capacity (%s) reached; one or more event listeners are likely too slow"
                            .formatted(queueCapacity)
            );
        }

        var pendingEvent = new PendingEvent(
                event,
                Thread.currentThread().getName(),
                System.currentTimeMillis()
        );
        if (!eventQueue.offer(pendingEvent)) {
            throw new IllegalStateException("Event queue rejected an event despite having reserved capacity");
        }
        queuedEventCount++;
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
        while (state != PublisherState.STOPPED) {
            try {
                processQueueItem(eventQueue.take());
            } catch (InterruptedException e) {
                logDispatcherInterrupt();
            }
        }
    }

    private void processQueueItem(QueueItem item) {
        switch (item) {
            case PendingEvent pendingEvent -> {
                releaseEventQueueSlot();
                dispatch(pendingEvent);
            }
            case DrainCommand command -> {
                if (eventQueue.isEmpty()) {
                    synchronized (acceptanceLock) {
                        state = PublisherState.STOPPED;
                    }
                    command.completion().complete(null);
                    return;
                }
                // Listener-generated events are appended behind the drain
                // command.
                if (!eventQueue.offer(command)) {
                    throw new IllegalStateException("Unable to rotate event drain command");
                }
            }
        }
    }

    private void logDispatcherInterrupt() {
        // Interrupts must not discard accepted events. Continue until an
        // explicit drain command observes a quiescent queue.
        log.warn("Event dispatcher interrupted; continuing to preserve accepted events");
    }

    private void releaseEventQueueSlot() {
        synchronized (acceptanceLock) {
            queuedEventCount--;
        }
    }

    private void dispatch(PendingEvent pendingEvent) {
        var event = pendingEvent.payload();
        var envelope = new EventEnvelope(
                nextEventId++,
                event.getClass().getSimpleName(),
                pendingEvent.acceptedAt(),
                pendingEvent.producerThreadName(),
                event
        );

        try {
            eventLog.append(envelope);
        } catch (Throwable e) {
            logSinkFailure("event log", event, e);
        }

        try {
            messagingTemplate.convertAndSend(EVENT_TOPIC, envelope);
        } catch (Throwable e) {
            // The event remains recoverable from the replay log when websocket
            // conversion or delivery fails.
            logSinkFailure("websocket", event, e);
        }

        dispatchToListeners(event);
    }

    private void dispatchToListeners(MorkEvent event) {
        try {
            for (var listener : listeners) {
                try {
                    listener.onEvent(event);
                } catch (Throwable e) {
                    logSinkFailure("backend listener " + listener.getClass().getName(), event, e);
                }
            }
        } catch (Throwable e) {
            logSinkFailure("backend listener discovery", event, e);
        }
    }

    /**
     * Atomically start draining without waiting for it to finish.
     * New external publications are rejected as soon as this method returns.
     */
    public void beginDraining() {
        synchronized (acceptanceLock) {
            if (state == PublisherState.STOPPED) {
                return;
            }
            if (state == PublisherState.RUNNING) {
                startDraining();
            }
        }
    }

    private void startDraining() {
        state = PublisherState.DRAINING;
        drainCompletion = new CompletableFuture<>();
        if (!eventQueue.offer(new DrainCommand(drainCompletion))) {
            state = PublisherState.RUNNING;
            drainCompletion = null;
            throw new IllegalStateException("Unable to enqueue event drain command");
        }
    }

    /**
     * Reject new external events, drain every accepted event (including events
     * recursively published by listeners), and stop the dispatcher.
     */
    public void drainAndStop() {
        if (Thread.currentThread() == dispatcherThread) {
            throw new IllegalStateException(
                    "Cannot synchronously drain the event publisher from its dispatcher thread; " +
                            "start draining and wait from another thread"
            );
        }
        beginDraining();
        CompletableFuture<Void> completion;
        synchronized (acceptanceLock) {
            if (state == PublisherState.STOPPED) {
                return;
            }
            completion = drainCompletion;
        }

        completion.join();
    }

    private void logSinkFailure(String sink, MorkEvent event, Throwable failure) {
        var rootCause = ExceptionUtil.getRootCause(failure);
        log.error(
                "Mork event {} failed in {}: {}: {}",
                event.getClass().getSimpleName(),
                sink,
                rootCause.getClass().getSimpleName(),
                rootCause.getMessage(),
                failure
        );
    }

    /**
     * Shut down the dispatcher when the Spring context closes.
     */
    @Override
    public void destroy() {
        if (Thread.currentThread() == dispatcherThread) {
            // A listener can initiate context closure. Waiting here would
            // deadlock the current dispatch, so transition immediately and let
            // the dispatcher consume the drain command after the listener
            // returns. Recursive listener events remain accepted meanwhile.
            log.warn("Spring destroyed the event publisher from its dispatcher thread; draining asynchronously");
            beginDraining();
            return;
        }
        drainAndStop();
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

    private sealed interface QueueItem permits PendingEvent, DrainCommand {
    }

    private record PendingEvent(
            MorkEvent payload,
            String producerThreadName,
            long acceptedAt
    ) implements QueueItem {
    }

    private record DrainCommand(CompletableFuture<Void> completion) implements QueueItem {
    }

    private enum PublisherState {
        RUNNING,
        DRAINING,
        STOPPED
    }
}
