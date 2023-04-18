package org.chatgpt.telegram;

import org.chatgpt.api.dream.DreamApi;
import org.chatgpt.api.gpt.ChatGPTApi;
import org.chatgpt.utils.ResourceBundleUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.*;
import java.util.function.Consumer;

import static org.chatgpt.constants.Constants.*;
import static org.chatgpt.constants.TranslationConstants.*;
import static org.chatgpt.utils.ResourceBundleUtils.*;

public class TelegramBot extends TelegramLongPollingBot {

    private boolean isHandlingMessages = true;
    private boolean isHandlingImages = false;
    private boolean isHandlingDreamImages = false;
    private boolean isHandlingGPTImages = false;
    private String quantity;
    private String size;
    private List<String> quantityList;
    private List<String> sizeList;

    private final BotConfig botConfig;
    private final ChatGPTApi chatGPTApi;
    Map<String, Consumer<Long>> messageHandlers;
    Map<Long, List<String>> context;
    private final DreamApi dreamApi;
    Map<String, String> styles;
    private String currentStyle;

    public TelegramBot(BotConfig botConfig) {
        super(botConfig.getToken());
        this.botConfig = botConfig;
        this.chatGPTApi = new ChatGPTApi();
        this.messageHandlers = new LinkedHashMap<>();
        this.context = new HashMap<>();
        this.dreamApi = new DreamApi();
        initSettingsList();
    }

    private void initSettingsList() {
        this.quantityList = new ArrayList<>(List.of("1", "2", "3", "4", "5"));
        this.sizeList = new ArrayList<>(List.of("256x256", "512x512", "1024x1024"));
        this.styles = dreamApi.getStyles();
    }

    @Override
    public String getBotUsername() {
        return botConfig.getBotName();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            CallbackQuery callbackQuery = update.getCallbackQuery();
            long chatId = callbackQuery.getMessage().getChatId();
            String query = callbackQuery.getData();
            if (query.equals(DREAM_IMAGE_STRATEGY) || isHandlingDreamImages) {
                isHandlingDreamImages = true;
                handleDreamImages(query, chatId);
            } else if (query.equals(GPT_IMAGE_STRATEGY) || isHandlingGPTImages) {
                isHandlingGPTImages = true;
                handleGPTImages(query, chatId);
            }
        } else if (update.hasMessage()) {
            Message message = update.getMessage();
            long chatId = message.getChatId();
            String messageText = message.getText();
            User user = message.getFrom();

            if (!user.getLanguageCode().equals(ResourceBundleUtils.getLanguageCode())) {
                this.messageHandlers = new LinkedHashMap<>();
                ResourceBundleUtils.setLanguageCode(user.getLanguageCode());
            }

            System.out.println("Message from: " + user.getFirstName() + " (" + user.getUserName() + "(" + user.getLanguageCode() + ")" + ") " + user.getLastName()
                    + ": " + messageText);

            if (messageText != null) {
                messageHandlers.put(getTranslate(COMMAND_START), (ch) -> handleStartCommand(chatId));
                messageHandlers.put(getTranslate(COMMAND_MESSAGE), (ch) -> handleMessagesMode(chatId));
                messageHandlers.put(getTranslate(COMMAND_IMAGE), (ch) -> handleImagesMode(chatId));
                messageHandlers.put(getTranslate(COMMAND_DONATE), (ch) -> handleSupportCommand(chatId));
                messageHandlers.put(getTranslate(COMMAND_ABOUT), (ch) -> handleAboutCommand(chatId));
                messageHandlers.put(getTranslate(COMMAND_REFRESH), (ch) -> handleResetCommand(chatId));

                Consumer<Long> defaultHandler = (ch) -> {
                    if (!messageHandlers.containsKey(messageText) && message.hasText()) {
                        if (isHandlingMessages) {
                            handleMessagesRequest(chatId, messageText);
                        } else if (isHandlingImages) {
                            handleImagesRequest(chatId, messageText);
                        }
                    }
                };

                messageHandlers.getOrDefault(messageText, defaultHandler).accept(chatId);
            }
        }
    }

    private void handleImageStrategy(long chatId) {
        List<String> strategy = new ArrayList<>();
        strategy.add(GPT_IMAGE_STRATEGY);
        strategy.add(DREAM_IMAGE_STRATEGY);
        sendMessage(getOptions(strategy), getTranslate(MESSAGE_IMAGE_SELECT_STRATEGY), chatId);
    }

    private void handleImagesRequest(long chatId, String messageText) {
        if (quantity != null && size != null) {
            sendMessage(getTranslate(MESSAGE_IMAGE_WRITE), chatId);
            sendImage(generateImageAnswerFromChatGPT(messageText, quantity, size), chatId);
            resetValues();
            isHandlingMessages = true;
            isHandlingImages = false;
            isHandlingGPTImages = false;
        }
        if (currentStyle != null) {
            sendMessage(getTranslate(MESSAGE_IMAGE_WRITE), chatId);
            sendImage(dreamApi.generateImages(styles.get(currentStyle), messageText), chatId);
            resetValues();
            isHandlingMessages = true;
            isHandlingImages = false;
            isHandlingDreamImages = false;
        }
    }

    private void handleDreamImages(String query, long chatId) {
        if (isHandlingDreamImages && !quantityList.contains(query) && !sizeList.contains(query)) {
            if (styles.containsKey(query)) {
                currentStyle = query;
            }
            if (currentStyle == null) {
                sendMessage(getStyleOptions(dreamApi.getStyles().keySet()), getTranslate(MESSAGE_IMAGE_STYLE_WRITE), chatId);
            }
            if (currentStyle != null) {
                sendMessage(getTranslate(MESSAGE_IMAGE_STYLE_RESULT) + currentStyle, chatId);
                sendMessage(getTranslate(MESSAGE_IMAGE_DESCRIPTION), chatId);
            }
        }
    }

    private void handleGPTImages(String query, long chatId) {
        if (isHandlingGPTImages && !styles.containsKey(query)) {
            if (quantityList.contains(query)) {
                quantity = query;
                sendMessage(getOptions(sizeList), getTranslate(MESSAGE_IMAGE_QUANTITY_WRITE), chatId);
            }
            if (sizeList.contains(query)) {
                size = query;
                sendMessage(getTranslate(MESSAGE_IMAGE_DESCRIPTION), chatId);
            }
            if (quantity == null && size == null) {
                sendMessage(getOptions(quantityList), getTranslate(MESSAGE_IMAGE_SIZE_WRITE), chatId);
            }
        }
    }

    private void handleMessagesRequest(long chatId, String messageText) {
        resetValues();
        sendMessage(getTranslate(MESSAGE_MESSAGE_WRITE), chatId);
        String generateMessageAnswerFromChatGPT = generateMessageAnswerFromChatGPT(messageText, chatId);
        System.out.println("Result: " + generateMessageAnswerFromChatGPT);
        if (errorHandler(generateMessageAnswerFromChatGPT)) {
            sendMessage(getTranslate(ERROR_TIMEOUT), chatId);
        } else {
            sendMessage(generateMessageAnswerFromChatGPT, chatId);
        }
    }

    private void handleSupportCommand(long chatId) {
        sendMessage(getTranslate(MESSAGE_DONATE_BEFORE) + getTranslate(MESSAGE_DONATE_LINKS) + getTranslate(MESSAGE_DONATE_AFTER), chatId);
    }

    private void handleResetCommand(long chatId) {
        resetValues();
        if (isHandlingImages) {
            sendMessage(getOptions(quantityList), getTranslate(MESSAGE_IMAGE_SIZE_WRITE), chatId);
        }
        if (isHandlingMessages) {
            context.put(chatId, new ArrayList<>());
        }
        sendMessage(getTranslate(MESSAGE_REFRESH), chatId);
    }

    private void handleAboutCommand(long chatId) {
        sendMessage(getTranslate(MESSAGE_ABOUT), chatId);
    }

    private void handleStartCommand(long chatId) {
        sendMessage(getTranslate(MESSAGE_START), chatId);
        this.isHandlingImages = false;
        this.isHandlingMessages = true;
    }

    private void handleMessagesMode(long chatId) {
        isHandlingMessages = true;
        isHandlingImages = false;
        sendMessage(getTranslate(MESSAGE_MESSAGE), chatId);
        resetValues();
    }

    private void handleImagesMode(long chatId) {
        isHandlingMessages = false;
        isHandlingImages = true;
        sendMessage(getTranslate(MESSAGE_IMAGE), chatId);
        resetValues();
        handleImageStrategy(chatId);
    }

    private boolean errorHandler(String response) {
        return response.equals("Timeout waiting for connection from pool") || response.equals("Read timed out");
    }

    private String generateMessageAnswerFromChatGPT(String request, long chatId) {
        List<String> currentContext = context.get(chatId);
        if (currentContext == null || !tokenCounter(currentContext)) {
            currentContext = new ArrayList<>();
            context.put(chatId, currentContext);
        }
        currentContext.add(request);
        String response = chatGPTApi.executeMessage(currentContext);
        if (errorHandler(response)) {
            currentContext.add(ERROR_TIMEOUT);
        }
        if (tokenCounter(currentContext)) {
            currentContext.add(response);
        }
        context.put(chatId, currentContext);
        return response;
    }

    private boolean tokenCounter(List<String> context) {
        int tokenCount = 0;
        for (String sentence : context) {
            String[] tokens = sentence.split("\\s+");
            tokenCount += tokens.length;
        }
        System.out.println("Current token count: " + tokenCount);
        return tokenCount < 2048;
    }

    private List<InputFile> generateImageAnswerFromChatGPT(String request, String quantity, String size) {
        return chatGPTApi.executeImage(request, quantity, size);
    }

    private void sendMessage(String message, long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(message);
        sendMessage.setReplyMarkup(attributes());
        sendMessage.setParseMode(ParseMode.MARKDOWN);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            System.out.println("TelegramApiException: " + e.getMessage());
        }
    }

    private void sendMessage(InlineKeyboardMarkup markup, String message, long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(message);
        sendMessage.enableMarkdown(true);
        sendMessage.setReplyMarkup(markup);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            System.out.println("TelegramApiException: " + e.getMessage());
        }
    }

    private void sendImage(List<InputFile> images, long chatId) {
        for (InputFile image : images) {
            SendPhoto sendPhoto = new SendPhoto();
            sendPhoto.setChatId(chatId);
            sendPhoto.setPhoto(image);
            sendPhoto.setReplyMarkup(attributes());
            sendPhoto.setParseMode(ParseMode.MARKDOWN);
            try {
                execute(sendPhoto);
            } catch (TelegramApiException e) {
                System.out.println("TelegramApiException: " + e.getMessage());
            }
        }
    }

    private void sendImage(InputFile image, long chatId) {
        SendPhoto sendPhoto = new SendPhoto();
        sendPhoto.setChatId(chatId);
        sendPhoto.setPhoto(image);
        sendPhoto.setReplyMarkup(attributes());
        sendPhoto.setParseMode(ParseMode.MARKDOWN);
        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            System.out.println("TelegramApiException: " + e.getMessage());
        }
    }

    private ReplyKeyboardMarkup attributes() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        int i = 0;
        for (String command : messageHandlers.keySet()) {
            if (i > 0 && i < 3) {
                row.add(command);
            }
            if (i > 2) {
                row2.add(command);
            }
            i++;
        }
        keyboard.add(row);
        keyboard.add(row2);
        replyKeyboardMarkup.setKeyboard(keyboard);
        replyKeyboardMarkup.setResizeKeyboard(true);
        return replyKeyboardMarkup;
    }

    private InlineKeyboardMarkup getOptions(List<String> values) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        for (String value : values) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(value);
            button.setCallbackData(value);
            row.add(button);
        }
        rows.add(row);
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }

    private InlineKeyboardMarkup getStyleOptions(Set<String> values) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        int counter = 0;
        for (String value : values) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(value);
            button.setCallbackData(value);
            row.add(button);
            counter++;
            if (counter == 4) {
                rows.add(row);
                row = new ArrayList<>();
                counter = 0;
            }
        }
        if (counter > 0) {
            rows.add(row);
        }
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }

    private void resetValues() {
        this.quantity = null;
        this.size = null;
        this.currentStyle = null;
    }
}