package org.chatgpt;

import jakarta.ws.rs.core.Application;
import org.chatgpt.telegram.BotConfig;
import org.chatgpt.telegram.BotInitializer;
import org.chatgpt.telegram.TelegramBot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static org.chatgpt.constants.GeneralConstants.*;
import static org.chatgpt.utils.ResourceBundleUtils.*;

@SpringBootApplication
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Application.class,args);
        try {
            BotConfig botConfig = new BotConfig(getGeneralProperties(BOT_NAME), getGeneralProperties(BOT_SECRET));
            TelegramBot telegramBot = new TelegramBot(botConfig);
            telegramBot.execute(SetWebhook.builder()
                    .secretToken("711cc84c-f91d-41e2-96b3-f327f9532657")
                    .url("https://chat-gptbot.herokuapp.com/")
                    .build());
            BotInitializer botInitializer = new BotInitializer(telegramBot);
            botInitializer.initBot();
        } catch (TelegramApiException e) {
            System.out.println(e.getMessage());
        }
    }
}