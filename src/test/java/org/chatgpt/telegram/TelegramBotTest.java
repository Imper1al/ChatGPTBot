package org.chatgpt.telegram;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.List;
import java.util.Map;

import static org.chatgpt.constants.GeneralConstants.*;
import static org.chatgpt.utils.ResourceBundleUtils.*;

class TelegramBotTest {

    public TelegramBot telegramBot;
    public Update update;
    public Update update2;
    public Update update3;
    public Message message;
    public Message message2;
    public Message message3;
    public Chat chat;
    public User user;

    @BeforeEach
    public void init() {
        telegramBot = new TelegramBot(new BotConfig(getGeneralProperties(BOT_NAME), getGeneralProperties(BOT_SECRET)));
        this.update = new Update();
        this.message = new Message();
        message.setText("Test text");
        this.chat = new Chat();
        chat.setId(1L);
        message.setChat(chat);
        this.user = new User();
        user.setId(1L);
        user.setLanguageCode("en");
        message.setFrom(user);

//        this.update2 = new Update();
//        this.message2 = new Message();
//        message2.setText("Test text 2");
//        message2.setFrom(user);
//        message2.setChat(chat);
//
//        this.update3 = new Update();
//        this.message3 = new Message();
//        message3.setText("Test text 3");
//        message3.setFrom(user);
//        message3.setChat(chat);
    }

    @Test
    public void contextTest() {
        update.setMessage(message);
        telegramBot.onUpdateReceived(update);
        Map<Long, List<String>> context = telegramBot.context;
        Assertions.assertFalse(context.isEmpty());
        Assertions.assertEquals(1, context.size());
    }

//    @Test
//    public void bigContextTest() {
//        update.setMessage(message);
//        telegramBot.onUpdateReceived(update);
//        update2.setMessage(message2);
//        telegramBot.onUpdateReceived(update2);
//        update3.setMessage(message3);
//        telegramBot.onUpdateReceived(update3);
//        Map<Long, List<String>> context = telegramBot.context;
//        Assertions.assertFalse(context.isEmpty());
//        Assertions.assertEquals(5, context.size());
//    }
}