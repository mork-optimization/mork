package es.urjc.etsii.grafo.solver.services.messaging;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Telegram configuration, see event.telegram section inside the application.yml file.
 * {@see application.yml}
 */
@Configuration
@ConfigurationProperties(prefix = "event.telegram")
public class TelegramConfig {
    private boolean enabled = false;
    private String token;
    private String chatId = "none";

    /**
     * Is the Telegram integration enabled?
     *
     * @return true if enabled, false otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Is the Telegram integration enabled?
     *
     * @param enabled true to enablee, false otherwise
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Telegram API token, as returned by BotFather
     *
     * @return Telegram API token
     */
    public String getToken() {
        return token;
    }

    /**
     * Telegram API token, as returned by BotFather
     *
     * @param token Telegram API token
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Destination Chat ID, where we will send the messages
     *
     * @return destination chat ID.
     */
    public String getChatId() {
        return chatId;
    }

    /**
     * Destination Chat ID, where we will send the messages
     *
     * @param chatId  destination chat ID.
     */
    public void setChatId(String chatId) {
        this.chatId = chatId;
    }
}
