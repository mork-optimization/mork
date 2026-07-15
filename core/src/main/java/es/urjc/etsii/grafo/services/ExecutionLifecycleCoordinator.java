package es.urjc.etsii.grafo.services;

import es.urjc.etsii.grafo.events.EventWebserverConfig;
import es.urjc.etsii.grafo.events.MorkEventPublisher;
import es.urjc.etsii.grafo.events.types.ExecutionEndedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Coordinates the terminal execution event, event-bus draining, and application shutdown.
 */
@Service
public class ExecutionLifecycleCoordinator {
    private static final Logger log = LoggerFactory.getLogger(ExecutionLifecycleCoordinator.class);
    private static final String SHUTDOWN_THREAD_NAME = "mork-shutdown";

    private final ConfigurableApplicationContext applicationContext;
    private final MorkEventPublisher eventPublisher;
    private final boolean stopOnExecutionEnd;
    private final AtomicBoolean completed = new AtomicBoolean();

    public ExecutionLifecycleCoordinator(
            ConfigurableApplicationContext applicationContext,
            MorkEventPublisher eventPublisher,
            EventWebserverConfig eventWebserverConfig
    ) {
        this.applicationContext = applicationContext;
        this.eventPublisher = eventPublisher;
        this.stopOnExecutionEnd = eventWebserverConfig.isStopOnExecutionEnd();
    }

    /**
     * Complete a terminal orchestration path exactly once.
     *
     * @param executionTime total execution time in nanoseconds
     */
    public void complete(long executionTime) {
        if (!completed.compareAndSet(false, true)) {
            throw new IllegalStateException("Execution lifecycle has already been completed");
        }

        if (!stopOnExecutionEnd) {
            eventPublisher.publish(new ExecutionEndedEvent(executionTime));
            log.info("event.webserver.stopOnExecutionEnd disabled, app must be manually stopped by user");
            return;
        }

        log.info("event.webserver.stopOnExecutionEnd enabled, draining events before closing the application context");
        eventPublisher.publishFinalAndBeginDraining(new ExecutionEndedEvent(executionTime));
        var shutdownThread = new Thread(() -> {
            eventPublisher.drainAndStop();
            log.debug("Event dispatcher drained, closing application context");
            applicationContext.close();
        }, SHUTDOWN_THREAD_NAME);
        shutdownThread.start();
    }
}
