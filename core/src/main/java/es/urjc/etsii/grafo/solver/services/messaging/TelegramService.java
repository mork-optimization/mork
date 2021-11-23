package es.urjc.etsii.grafo.solver.services.messaging;

import es.urjc.etsii.grafo.solver.services.events.AbstractEventListener;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.BotSession;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.logging.Logger;

/**
 * This class create a Bot for Mork framework.
 * Particularly, this bot will be generally used to send the following message when the experiment ends:
 *     Experiment {{experimentName}} ended. Execution time: {{time}} seconds
 */
public class TelegramService extends AbstractEventListener {

    private static final Logger log = Logger.getLogger(MorkTelegramBot.class.getName());
    private static final int longPollingTimeoutInSeconds = 10;
    private final TelegramConfig telegramConfig;

    private volatile boolean errorNotified = false;

    private MorkTelegramBot telegramBot;
    private BotSession session;

    /**
     * Creates a TelegramLongPollingCommandBot using default options
     * Use ICommandRegistry's methods on this bot to register commands
     *
     * @param telegramConfig a {@link es.urjc.etsii.grafo.solver.services.messaging.TelegramConfig} object.
     */
    public TelegramService(TelegramConfig telegramConfig) {
        this.telegramConfig = telegramConfig;
        if (telegramConfig.isEnabled()) {
            initializeTelegramBot(telegramConfig.getChatId(), telegramConfig.getToken());
        }
    }

    /**
     * Initialize a Telegram bot using default option
     *
     * @param chatId chat id
     * @param token  token
     */
    private void initializeTelegramBot(String chatId, String token) {
        log.info("Registering Telegram bot...");
        var options = new DefaultBotOptions();
        options.setGetUpdatesTimeout(longPollingTimeoutInSeconds);
        this.telegramBot = new MorkTelegramBot(chatId, token, options);
        try {
            var api = new TelegramBotsApi(DefaultBotSession.class);
            session = api.registerBot(telegramBot);
            log.info("Telegram integration enabled");
        } catch (TelegramApiException e) {
            log.warning("Failed bot registration: " + e);
            telegramBot = null;
        }
    }

    /**
     * Check if the telegram bot is working
     *
     * @return true if the bot is working, false in other case
     */
    public boolean ready() {
        return this.telegramBot != null && this.telegramBot.ready();
    }


    /**
     * Method for creating a message and sending it.
     *
     * @param message message
     */
    public void sendMessage(String message) {
        if (!ready()) {
            throw new IllegalStateException("Tried to send a message while bot is not ready");
        }
        telegramBot.sendMessage(message);
    }

    /**
     * Method for stopping a telegram bot
     */
    public void stop() {
        if (!this.telegramConfig.isEnabled()) return;
        log.info("Stopping telegram bot... This can take up to 10 seconds.");
        if (session != null) {
            session.stop();
        }
        log.info("Stopped telegram bot.");
    }

    /**
     *   This class adds functionality to the TelegramLongPollingBot
     *
     */
    private static class MorkTelegramBot extends TelegramLongPollingBot {

        private static final Logger log = Logger.getLogger(MorkTelegramBot.class.getName());

        /**
         * chat id obtained by bot father
         */
        private final String chatId;

        /**
         * bot's token for communicating with the Telegram server
         */
        private final String token;

        /**
         * Constructor
         *
         * @param chatId  chat id
         * @param token   token
         * @param options options
         */
        private MorkTelegramBot(String chatId, String token, DefaultBotOptions options) {
            super(options);
            this.chatId = chatId;
            this.token = token;
        }

        @Override
        public String getBotUsername() {
            return "MorkTelegramIntegration";
        }


        /**
         * This method returns the bot's token for communicating with the Telegram server
         *
         * @return the bot's token
         */
        @Override
        public String getBotToken() {
            return token;
        }

        /**
         * Method for receiving messages.
         *
         * @param update Contains a message from the user.
         */
        @Override
        public void onUpdateReceived(Update update) {
            log.info(String.format("Recieved message %s", update));
            if (update.hasMessage()) {
                var message = update.getMessage();
                var chatId = message.getChatId().toString();
                var action = new SendMessage(chatId, String.format("Chat id: %s", chatId));
                try {
                    this.execute(action);
                } catch (TelegramApiException e) {
                    log.info("Failed sending chatId to user: " + e);
                }
            }
        }

        /**
         * Method for creating a message and sending it.
         *
         * @param str message
         */
        public void sendMessage(String str) {
            var action = new SendMessage(this.chatId, str);
            try {
                this.execute(action);
            } catch (TelegramApiException e) {
                log.warning("Failed to send message:" + e);
            }
        }

        /**
         * Check if we are ready
         * @return true if ready, false otherwise.
         */
        public boolean ready() {
            return !this.chatId.equals("none");
        }
    }
}
