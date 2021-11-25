package es.urjc.etsii.grafo.solver.services.messaging;

import es.urjc.etsii.grafo.solver.services.events.AbstractEventListener;
import es.urjc.etsii.grafo.solver.services.events.MorkEventListener;
import es.urjc.etsii.grafo.solver.services.events.types.ErrorEvent;
import es.urjc.etsii.grafo.solver.services.events.types.ExecutionEndedEvent;
import es.urjc.etsii.grafo.solver.services.events.types.ExperimentEndedEvent;

/**
 * Sends telegram messages on certain MorkEvents
 */
public class TelegramEventListener extends AbstractEventListener {

    private final TelegramService telegramService;
    private volatile boolean errorNotified = false;

    /**
     * <p>Constructor for TelegramEventListener.</p>
     *
     * @param telegramService a {@link es.urjc.etsii.grafo.solver.services.messaging.TelegramService} object.
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
        telegramService.sendMessage(String.format("Experiment %s ended. Execution time: %s seconds", event.getExperimentName(), event.getExecutionTime() / 1_000_000_000));
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
