package es.urjc.etsii.grafo.services.messaging;

import es.urjc.etsii.grafo.events.MorkEventListener;
import es.urjc.etsii.grafo.events.types.ErrorEvent;
import es.urjc.etsii.grafo.events.types.ExecutionEndedEvent;
import es.urjc.etsii.grafo.events.types.ExperimentEndedEvent;
import es.urjc.etsii.grafo.events.types.MorkEvent;

import static es.urjc.etsii.grafo.util.TimeUtil.nanosToSecs;

/**
 * Sends telegram messages on certain MorkEvents
 */
public class TelegramEventListener implements MorkEventListener {

    private final TelegramService telegramService;
    private volatile boolean errorNotified = false;

    /**
     * <p>Constructor for TelegramEventListener.</p>
     *
     * @param telegramService a {@link TelegramService} object.
     */
    public TelegramEventListener(TelegramService telegramService) {
        this.telegramService = telegramService;
    }

    @Override
    public void onEvent(MorkEvent event) {
        switch (event) {
            case ExperimentEndedEvent experimentEndedEvent -> onExperimentEnd(experimentEndedEvent);
            case ErrorEvent errorEvent -> onError(errorEvent);
            case ExecutionEndedEvent ignored -> telegramService.stop();
            default -> {
            }
        }
    }

    /**
     * Send message when experiment ends
     *
     * @param event experiment ended event
     */
    private void onExperimentEnd(ExperimentEndedEvent event) {
        if (!telegramService.ready()) return;
        telegramService.sendMessage(String.format("Experiment %s ended. Execution time: %s seconds", event.experimentName(), nanosToSecs(event.executionTime())));
    }

    /**
     * Send message on first error
     *
     * @param event error event
     */
    private void onError(ErrorEvent event) {
        if (!telegramService.ready()) return;
        // Only notify first error to prevent spamming
        if (!errorNotified) {
            errorNotified = true;
            var t = event.throwable();
            telegramService.sendMessage(String.format("Execution Error: %s. Further errors will NOT be notified.", t));
        }
    }
}
