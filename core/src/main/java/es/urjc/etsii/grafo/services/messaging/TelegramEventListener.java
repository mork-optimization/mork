package es.urjc.etsii.grafo.services.messaging;

import es.urjc.etsii.grafo.events.AbstractEventListener;
import es.urjc.etsii.grafo.events.MorkEventListener;
import es.urjc.etsii.grafo.events.types.ErrorEvent;
import es.urjc.etsii.grafo.events.types.ExecutionEndedEvent;
import es.urjc.etsii.grafo.events.types.ExperimentEndedEvent;

import static es.urjc.etsii.grafo.util.TimeUtil.nanosToSecs;

/**
 * Sends telegram messages on certain MorkEvents
 */
public class TelegramEventListener extends AbstractEventListener {

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

    /**
     * Send message when experiment ends
     *
     * @param event experiment ended event
     */
    @MorkEventListener
    public void onExperimentEnd(ExperimentEndedEvent event) {
        if (!telegramService.ready()) return;
        telegramService.sendMessage(String.format("Experiment %s ended. Execution time: %s seconds", event.getExperimentName(), nanosToSecs(event.getExecutionTime())));
    }

    /**
     * Send message on first error
     *
     * @param event error event
     */
    @MorkEventListener
    public void onError(ErrorEvent event) {
        if (!telegramService.ready()) return;
        // Only notify first error to prevent spamming
        if (!errorNotified) {
            errorNotified = true;
            var t = event.getThrowable();
            telegramService.sendMessage(String.format("Execution Error: %s. Further errors will NOT be notified.", t));
        }
    }

    /**
     * Stop Telegram service on execution end
     *
     * @param event execution ended event
     */
    @MorkEventListener
    public void onExecutionEnd(ExecutionEndedEvent event) {
        telegramService.stop();
    }
}
