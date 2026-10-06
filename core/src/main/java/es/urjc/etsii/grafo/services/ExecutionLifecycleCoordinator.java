package es.urjc.etsii.grafo.services;

import es.urjc.etsii.grafo.events.types.ErrorEvent;
import es.urjc.etsii.grafo.monitoring.NormalExperimentRunState;

import es.urjc.etsii.grafo.metrics.timing.TimeStatsService;
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
    private final TimeStatsService timeStats;
    private final NormalExperimentRunState<?, ?> runState;
    private final AtomicBoolean completed = new AtomicBoolean();

    public ExecutionLifecycleCoordinator(
            ConfigurableApplicationContext applicationContext,
            MorkEventPublisher eventPublisher,
            EventWebserverConfig eventWebserverConfig, TimeStatsService timeStats,
            NormalExperimentRunState<?, ?> runState
    ) {
        this.applicationContext = applicationContext;
        this.timeStats = timeStats;
        this.runState = runState;
        this.eventPublisher = eventPublisher;
        this.stopOnExecutionEnd = eventWebserverConfig.isStopOnExecutionEnd();
    }

    /**
     * Complete a terminal orchestration path exactly once.
     *
     * @param executionTime total execution time in nanoseconds
     */
    public void complete(long executionTime, ExecutionEndedEvent.Outcome outcome) {
        if (!completed.compareAndSet(false, true)) {
            throw new IllegalStateException("Execution lifecycle has already been completed");
        }

        try {
            timeStats.close();
        } catch (RuntimeException e) {
            log.error("Timing finalization failed", e);
            runState.issue("WARNING", "TIMING", e, null, null);
            eventPublisher.publish(new ErrorEvent(e));
            if (outcome == ExecutionEndedEvent.Outcome.SUCCEEDED) {
                outcome = ExecutionEndedEvent.Outcome.COMPLETED_WITH_ERRORS;
            }
        }
        outcome = runState.finish(outcome);
        if (!stopOnExecutionEnd) {
            eventPublisher.publish(new ExecutionEndedEvent(executionTime, outcome));
            log.info("event.webserver.stopOnExecutionEnd disabled, app must be manually stopped by user");
            return;
        }

        log.info("event.webserver.stopOnExecutionEnd enabled, draining events before closing the application context");
        eventPublisher.publishFinalAndBeginDraining(new ExecutionEndedEvent(executionTime, outcome));
        var shutdownThread = new Thread(() -> {
            eventPublisher.drainAndStop();
            log.debug("Event dispatcher drained, closing application context");
            applicationContext.close();
        }, SHUTDOWN_THREAD_NAME);
        shutdownThread.start();
    }
}
