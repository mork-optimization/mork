package es.urjc.etsii.grafo.events;

import es.urjc.etsii.grafo.events.types.ExecutionEndedEvent;
import es.urjc.etsii.grafo.events.types.MorkEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Distributes the given event to different application components.
 */
@Component
public class EventPublisher {
    private static final Logger log = LoggerFactory.getLogger(EventPublisher.class);
    private static final int MAX_QUEUE_SIZE = 10_000;
    private static EventPublisher eventPublisher;
    private BlockingQueue<MorkEvent> eventQueue;

    /**
     * Disable event propagation
     */
    private boolean blockEvents = false;

    /**
     * Spring integration constructor
     *
     * @param publisher Spring ApplicationEventPublisher
     */
    public EventPublisher(ApplicationEventPublisher publisher) {
        eventQueue = new ArrayBlockingQueue<>(MAX_QUEUE_SIZE);
        var eventInterceptor = new EventInterceptor(eventQueue, publisher);
        new Thread(eventInterceptor).start();
        eventPublisher = this;
    }

    /**
     * Get event publisher instance
     *
     * @return event publisher instance
     */
    public static EventPublisher getInstance() {
        return EventPublisher.eventPublisher;
    }

    /**
     * Asynchronously send and process an event
     *
     * @param event Event to propagate
     */
    public void publishEvent(MorkEvent event) {
        if (blockEvents) {
            log.debug("Event system disabled: " + event);
            return;
        }
        boolean enqueued = eventPublisher.eventQueue.offer(event);
        if (!enqueued) {
            throw new IllegalStateException(
                    String.format("Maximum event queue capacity (%s) reached, cannot keep up? probably a bug", MAX_QUEUE_SIZE)
            );
        }
    }


    /**
     * Block dispatch of all future events. All calls to publishEvent() will be ignored.
     */
    public void block() {
        this.blockEvents = true;
    }

    /**
     * Enable event dispatching.
     */
    public void unblock() {
        this.blockEvents = false;
    }

    private record EventInterceptor(BlockingQueue<MorkEvent> eventQueue,
                                    ApplicationEventPublisher destination) implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    var event = eventQueue.take();
                    log.trace("Publishing event: " + event);
                    this.destination.publishEvent(event);
                    if (event instanceof ExecutionEndedEvent) {
                        log.debug("Stopping event interceptor thread");
                        return;
                    }
                } catch (InterruptedException e) {
                    log.warn("Event interceptor interrupted, exiting thread. Events will stop being propagated");
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }
}
