package org.chatgpt.telegram;

import lombok.Data;

@Data
public class BotConfig {

    String botName;
    String token;

    public BotConfig(String botName, String token) {
        this.botName = botName;
        this.token = token;
    }

    public String getBotName() {
        return botName;
    }

    public String getToken() {
        return token;
    }
}
