package es.urjc.etsii.grafo.solver.services.messaging;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * <p>TelegramConfig class.</p>
 *
 */
@Configuration
@ConfigurationProperties(prefix = "event.telegram")
public class TelegramConfig {
    private boolean enabled = false;
    private String token;
    private String chatId = "none";

    /**
     * <p>isEnabled.</p>
     *
     * @return a boolean.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * <p>Setter for the field <code>enabled</code>.</p>
     *
     * @param enabled a boolean.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * <p>Getter for the field <code>token</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getToken() {
        return token;
    }

    /**
     * <p>Setter for the field <code>token</code>.</p>
     *
     * @param token a {@link java.lang.String} object.
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * <p>Getter for the field <code>chatId</code>.</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getChatId() {
        return chatId;
    }

    /**
     * <p>Setter for the field <code>chatId</code>.</p>
     *
     * @param chatId a {@link java.lang.String} object.
     */
    public void setChatId(String chatId) {
        this.chatId = chatId;
    }
}
