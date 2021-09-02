package es.urjc.etsii.grafo.solver.services.events;

import es.urjc.etsii.grafo.solver.services.events.types.ExperimentEndedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.logging.Logger;

public class TelegramEventListener extends AbstractEventListener {

    private static final Logger log = Logger.getLogger(MorkTelegramBot.class.getName());

    private MorkTelegramBot telegramBot;

    public TelegramEventListener(
            @Value("${event.telegram.enabled:false}") boolean enabled,
            @Value("${event.telegram.chatId:none}")String chatId,
            @Value("${event.telegram.token}")String token
    ) {
        if (enabled) {
            this.telegramBot = new MorkTelegramBot(chatId, token);
            try {
                var api = new TelegramBotsApi(DefaultBotSession.class);
                api.registerBot(telegramBot);
            } catch (TelegramApiException e) {
                log.warning("Failed bot registration: " + e);
                telegramBot = null;
            }
        }
    }

    @MorkEventListener
    public void onExperimentEnd(ExperimentEndedEvent event){
        if(telegramBot != null && telegramBot.ready()){
            telegramBot.sendMessage(String.format("Experiment %s ended. Execution time: %s seconds", event.getExperimentName(), event.getExecutionTime() / 1_000_000_000));
        }
    }

    private static class MorkTelegramBot extends TelegramLongPollingBot {

        private static final Logger log = Logger.getLogger(MorkTelegramBot.class.getName());

        private final String chatId;
        private final String token;

        private MorkTelegramBot(String chatId, String token) {
            this.chatId = chatId;
            this.token = token;
        }

        @Override
        public String getBotUsername() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getBotToken() {
            return token;
        }

        @Override
        public void onUpdateReceived(Update update) {
            log.info(String.format("Recieved message %s", update));
            if(update.hasMessage()){
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

        public void sendMessage(String str){
            var action = new SendMessage(this.chatId, str);
            try {
                this.execute(action);
            } catch (TelegramApiException e) {
                log.warning("Failed to send message:" + e);
            }
        }

        public boolean ready(){
            return this.chatId.equals("none");
        }
    }
}
