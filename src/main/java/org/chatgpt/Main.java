package org.chatgpt;

import org.chatgpt.telegram.BotConfig;
import org.chatgpt.telegram.BotInitializer;
import org.chatgpt.telegram.TelegramBot;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static org.chatgpt.constants.GeneralConstants.*;
import static org.chatgpt.utils.ResourceBundleUtils.*;

public class Main {
    public static void main(String[] args) {
        try {
            BotConfig botConfig = new BotConfig(getGeneralProperties(BOT_NAME), getGeneralProperties(BOT_SECRET));
            TelegramBot telegramBot = new TelegramBot(botConfig);
            BotInitializer botInitializer = new BotInitializer(telegramBot);
            botInitializer.initBot();
        } catch (TelegramApiException e) {
            System.out.println(e.getMessage());
        }
    }
}