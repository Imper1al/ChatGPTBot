package org.chatgpt.telegram;

import org.chatgpt.api.dream.DreamApi;
import org.chatgpt.api.gpt.ChatGPTApi;
import org.chatgpt.constants.Constants;
import org.chatgpt.database.ChatIds;
import org.chatgpt.utils.PhotoUtil;
import org.chatgpt.utils.ResourceBundleUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
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
    private boolean isWidth = false;
    private boolean isHeight = false;
    private boolean isDescription = false;
    private boolean isPagination = false;
    private boolean isCreateAd = false;
    private boolean isCreateAdImage = false;
    private boolean isCreateAdMessage = false;
    private boolean tehrab = true;
    private boolean isAdmin = false;
    private String currentAdminStrategy;
    private String quantity;
    private String size;
    private List<String> quantityList;
    private List<String> sizeList;

    private final BotConfig botConfig;
    private final ChatGPTApi chatGPTApi;
    private final ChatIds chatIds;
    Map<String, Consumer<Long>> messageHandlers;
    Map<Long, List<String>> context;
    private final DreamApi dreamApi;
    Map<String, String> styles;
    List<String> adminStrategy;
    private String currentStyle;
    private String width;
    private String height;
    private int currentPage = 1;
    private int totalPages;
    private String adminMessage;
    private String messageText;

    public TelegramBot(BotConfig botConfig) {
        super(botConfig.getToken());
        this.botConfig = botConfig;
        this.chatGPTApi = new ChatGPTApi();
        this.chatIds = new ChatIds();
        this.messageHandlers = new LinkedHashMap<>();
        this.context = new HashMap<>();
        this.dreamApi = new DreamApi();
        initSettingsList();
        initAdminStrategyList();
    }

    private void initSettingsList() {
        this.quantityList = new ArrayList<>(List.of("1", "2", "3", "4", "5"));
        this.sizeList = new ArrayList<>(List.of("256x256", "512x512", "1024x1024"));
        try {
            this.styles = dreamApi.getStyles();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private void initAdminStrategyList() {
        adminStrategy = new ArrayList<>();
        adminStrategy.add(getTranslate(ADMIN_COMMAND_WITH_IMAGE));
        adminStrategy.add(getTranslate(ADMIN_COMMAND_WITHOUT_IMAGE));
        adminStrategy.add(getTranslate(ADMIN_COMMAND_WITH_IMAGE_TEST));
        adminStrategy.add(getTranslate(ADMIN_COMMAND_WITHOUT_IMAGE_TEST));
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
            if (isAdmin && isCreateAd) {
                currentAdminStrategy = query;
                if (!isCreateAdMessage) {
                    handleCreateAdCommandMessage(chatId);
                }
            }
            if (isHandlingDreamImages && isPagination && (query.equals(getTranslate(PAGINATION_PREVIOUS)) || query.equals(getTranslate(PAGINATION_NEXT)))) {
                checkPaginationCallback(query, chatId, callbackQuery.getMessage().getMessageId());
            }
            if ((query.equals(DREAM_IMAGE_STRATEGY) || isHandlingDreamImages) && !isHandlingGPTImages) {
                isHandlingDreamImages = true;
                handleDreamImages(query, chatId, callbackQuery.getMessage());
            }
            if ((query.equals(GPT_IMAGE_STRATEGY) || isHandlingGPTImages) && !isHandlingDreamImages) {
                isHandlingGPTImages = true;
                handleGPTImages(query, chatId);
            }
        } else if (update.hasMessage()) {
            Message message = update.getMessage();
            long chatId = message.getChatId();
            chatIds.addIdToDatabase(chatId);
            if(message.hasText()) {
                messageText = message.getText();
            }
            User user = message.getFrom();

            if (tehrab && !user.getUserName().equals(ADMIN)) {
                sendMessageWithImage(getTranslate(TEHWORKS_MESSAGE), message.getChatId(), TEHRAB_IMAGE_URL);
            }
            if (!tehrab || (tehrab && user.getUserName().equals(ADMIN))) {

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
                    messageHandlers.put(getTranslate(COMMAND_COOPERATION), (ch) -> handleCooperationCommand(chatId));
                    messageHandlers.put(getTranslate(COMMAND_REFRESH), (ch) -> handleResetCommand(chatId));
                    if (user.getUserName().equals(ADMIN)) {
                        isAdmin = true;
                        messageHandlers.put(getTranslate(COMMAND_CREATE_AD), (ch) -> handleCreateAdCommand(chatId));
                        messageHandlers.put(getTranslate(COMMAND_USER_COUNTER), (ch) -> handleUserCounter(chatId));
                    }

                    Consumer<Long> defaultHandler = (ch) -> {
                        if (!messageHandlers.containsKey(messageText) && message.hasText()) {
                            if (isHandlingMessages) {
                                handleMessagesRequest(chatId, messageText);
                            }
                            else if (isHandlingImages) {
                                handleImagesRequest(chatId, messageText);
                            }
                            else if(isCreateAd) {
                                handleAdminRequest(chatId, message);
                            }
                        }
                    };

                    messageHandlers.getOrDefault(messageText, defaultHandler).accept(chatId);
                }
            }
        }
        }

    private void errorHandler(long chatId) {
        if(!dreamApi.getErrors().isEmpty()) {
            sendMessage(dreamApi.getErrors().get(DREAM_IMAGE_STATUS_FAILED), chatId);
            dreamApi.getErrors().clear();
            isHandlingMessages = true;
            isHandlingImages = false;
            isHandlingDreamImages = false;
            isDescription = false;
            isHeight = false;
            isWidth = false;
            resetValues();
        }
    }

    private void handleImageStrategy(long chatId) {
        List<String> strategy = new ArrayList<>();
        strategy.add(GPT_IMAGE_STRATEGY);
        if(styles != null) {
            strategy.add(DREAM_IMAGE_STRATEGY);
        }
        sendMessage(getOptions(strategy), getTranslate(MESSAGE_IMAGE_SELECT_STRATEGY), chatId);
    }

    private void handleImagesRequest(long chatId, String messageText) {
        if (quantity != null && size != null) {
            Message message = sendWriteMessage(MESSAGE_IMAGE_GPT_WRITE, chatId);
            sendImage(generateImageAnswerFromChatGPT(messageText, quantity, size), chatId);
            deleteMessage(message, chatId);
            resetValues();
            isHandlingMessages = true;
            isHandlingImages = false;
            isHandlingGPTImages = false;
        }
        if (currentStyle != null) {
            if (isDescription) {
                try {
                    isHandlingMessages = true;
                    isHandlingImages = false;
                    isHandlingDreamImages = false;
                    isDescription = false;
                    isHeight = false;
                    isWidth = false;
                    Message message = sendWriteMessage(MESSAGE_IMAGE_DREAM_WRITE, chatId);
                    sendImage(dreamApi.generateImages(styles.get(currentStyle), width, height, messageText), chatId);
                    deleteMessage(message, chatId);
                    if (dreamApi.getResultStatus().equals(Constants.DREAM_IMAGE_STATUS_FAILED)) {
                        sendMessage(getTranslate(ERROR_GENERATION), chatId);
                    }
                    resetValues();
                }catch (Exception e) {
                    System.out.println(e.getMessage());
                    errorHandler(chatId);
                }
            }
            heightAndWeightCheck(messageText, chatId);
        }
    }

    private void handleAdminRequest(long chatId, Message message) {
        if (isAdmin && isCreateAd && isCreateAdMessage) {
            adminMessage = message.getText();
            if (!isCreateAdImage) {
                if (getTranslate(ADMIN_COMMAND_WITHOUT_IMAGE).equals(currentAdminStrategy)) {
                    createAdWithText(adminMessage);
                    resetAdminValues();
                }
                if (getTranslate(ADMIN_COMMAND_WITHOUT_IMAGE_TEST).equals(currentAdminStrategy)) {
                    createAdWithTextTest(adminMessage, chatId);
                    resetAdminValues();
                }
            }
            if (!isCreateAdImage && (getTranslate(ADMIN_COMMAND_WITH_IMAGE).equals(currentAdminStrategy) || getTranslate(ADMIN_COMMAND_WITH_IMAGE_TEST).equals(currentAdminStrategy))) {
                handleCreateAdCommandImage(chatId);
            }
            System.out.println("Has photo: " + message.hasEntities());
            System.out.println("Chat photo:" + message.getEntities().get(0).getType());
            boolean hasPhotoEntity = message.getEntities().stream()
                    .anyMatch(entity -> entity.getType().equals("photo"));
            System.out.println("Has photo entity: " + hasPhotoEntity);
            if (isCreateAdImage && message.hasEntities()) {
                System.out.println("Start photo set");
                PhotoSize photoSize = message.getPhoto().get(0);
                GetFile getFile = new GetFile();
                getFile.setFileId(photoSize.getFileId());

                try {
                    File file = execute(getFile);
                    String filePath = file.getFilePath();
                    System.out.println("filePath: " + filePath);
                    if (getTranslate(ADMIN_COMMAND_WITH_IMAGE).equals(currentAdminStrategy)) {
                        createAdWithImage(adminMessage, filePath);
                        resetAdminValues();
                    }
                    if (getTranslate(ADMIN_COMMAND_WITH_IMAGE_TEST).equals(currentAdminStrategy)) {
                        createAdWithImageTest(adminMessage, filePath, chatId);
                        resetAdminValues();
                    }
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void heightAndWeightCheck(String messageText, long chatId) {
        if (currentStyle != null) {
            if (isHeight) {
                try {
                    int result = Integer.parseInt(messageText);
                    if (result >= 1 && result <= 8000) {
                        height = messageText;
                        isHeight = false;
                        sendMessage(getTranslate(MESSAGE_IMAGE_DESCRIPTION), chatId);
                        isDescription = true;
                    } else {
                        sendMessage(getTranslate(ERROR_HEIGHT), chatId);
                    }
                } catch (NumberFormatException e) {
                    sendMessage(getTranslate(ERROR_HEIGHT), chatId);
                }
            }
            if (isWidth) {
                try {
                    int result = Integer.parseInt(messageText);
                    if (result >= 1 && result <= 8000) {
                        width = messageText;
                        isWidth = false;
                        isHeight = true;
                        sendMessage(getTranslate(MESSAGE_IMAGE_HEIGHT_WRITE), chatId);
                    } else {
                        sendMessage(getTranslate(ERROR_WIDTH), chatId);
                    }
                } catch (NumberFormatException e) {
                    sendMessage(getTranslate(ERROR_WIDTH), chatId);
                }
            }
        }
    }

    private void handleDreamImages(String query, long chatId, Message message) {
        if (isHandlingDreamImages && !quantityList.contains(query) && !sizeList.contains(query)) {
            if (styles.containsKey(query)) {
                currentStyle = query;
            }
            if (currentStyle == null && !isPagination) {
                message.setText(getTranslate(MESSAGE_IMAGE_STYLE_WRITE));
                checkPaginationCallback(query, chatId, message.getMessageId());
                isPagination = true;
            }
            if (currentStyle != null && width == null) {
                isPagination = false;
                currentPage = 1;
                sendMessage(getTranslate(MESSAGE_IMAGE_STYLE_RESULT) + currentStyle, chatId);
                sendMessage(getTranslate(MESSAGE_IMAGE_WIDTH_WRITE), chatId);
                isWidth = true;
            }
        }
    }

    private void handleGPTImages(String query, long chatId) {
        if (isHandlingGPTImages) {
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
        Message message = sendWriteMessage(MESSAGE_MESSAGE_WRITE, chatId);
        String generateMessageAnswerFromChatGPT = generateMessageAnswerFromChatGPT(messageText, chatId);
        System.out.println("Result: " + generateMessageAnswerFromChatGPT);
        deleteMessage(message, chatId);
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
        resetAdminValues();
        sendMessage(getTranslate(MESSAGE_REFRESH), chatId);
        isHandlingDreamImages = false;
        isHandlingGPTImages = false;
        isWidth = false;
        isHeight = false;
        isDescription = false;
        isPagination = false;
        if (isHandlingImages) {
            handleImagesMode(chatId);
        }
        if (isHandlingMessages) {
            context.put(chatId, new ArrayList<>());
        }
    }

    private void handleCooperationCommand(long chatId) {
        sendMessageWithImage(getTranslate(MESSAGE_COOPERATION), chatId, COOPERATION_IMAGE_URL);
    }

    private void handleStartCommand(long chatId) {
        sendMessageWithImage(getTranslate(MESSAGE_START), chatId, START_IMAGE_URL);
        this.isHandlingImages = false;
        this.isHandlingMessages = true;
    }

    private void handleCreateAdCommand(long chatId) {
        isCreateAd = true;
        isHandlingMessages = false;
        isHandlingImages = false;
        sendMessage(getAdminOptions(adminStrategy), getTranslate(MESSAGE_IMAGE_SELECT_STRATEGY), chatId);
    }

    private void handleUserCounter(long chatId) {
        sendMessage(chatIds.userCounter(), chatId);
    }

    private void handleCreateAdCommandMessage(Long chatId) {
        if (isCreateAd) {
            isCreateAdMessage = true;
            sendMessage(getTranslate(ADMIN_WRITE_TEXT), chatId);
        }
    }

    private void handleCreateAdCommandImage(Long chatId) {
        if (isCreateAd) {
            isCreateAdImage = true;
            sendMessage(getTranslate(ADMIN_WRITE_IMAGE), chatId);
        }
    }

    private void handleMessagesMode(long chatId) {
        isHandlingMessages = true;
        isHandlingImages = false;
        isCreateAdMessage = false;
        sendMessageWithImage(getTranslate(MESSAGE_MESSAGE), chatId, MESSAGES_IMAGE_URL);
        resetValues();
    }

    private void handleImagesMode(long chatId) {
        isHandlingMessages = false;
        isCreateAdMessage = false;
        isHandlingImages = true;
        isHandlingGPTImages = false;
        isHandlingDreamImages = false;
        sendMessageWithImage(getTranslate(MESSAGE_IMAGE), chatId, IMAGES_IMAGE_URL);
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
        if (isAdmin) {
            sendMessage.setReplyMarkup(adminAttributes());
        } else {
            sendMessage.setReplyMarkup(attributes());
        }
        sendMessage.setParseMode(ParseMode.MARKDOWN);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            System.out.println("TelegramApiException: " + e.getMessage());
        }
    }

    private void sendMessageWithImage(String message, long chatId, String imageUrl) {
        SendPhoto sendPhoto = SendPhoto.builder()
                .caption(message)
                .chatId(chatId)
                .photo(PhotoUtil.getInputFileByPath(imageUrl))
                .build();
        try {
            execute(sendPhoto);
        } catch (TelegramApiException e) {
            System.out.println("TelegramApiException: " + e.getMessage());
        }
    }

    private Message sendWriteMessage(String message, long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(message);
        if (isAdmin) {
            sendMessage.setReplyMarkup(adminAttributes());
        } else {
            sendMessage.setReplyMarkup(attributes());
        }
        sendMessage.setParseMode(ParseMode.MARKDOWN);
        Message execute = new Message();
        try {
            execute = execute(sendMessage);
        } catch (TelegramApiException e) {
            System.out.println("TelegramApiException: " + e.getMessage());
        }
        return execute;
    }

    private void deleteMessage(Message message, long chatId) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setMessageId(message.getMessageId());
        deleteMessage.setChatId(chatId);
        try {
            execute(deleteMessage);
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
            if (isAdmin) {
                sendPhoto.setReplyMarkup(adminAttributes());
            } else {
                sendPhoto.setReplyMarkup(attributes());
            }
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
        if (isAdmin) {
            sendPhoto.setReplyMarkup(adminAttributes());
        } else {
            sendPhoto.setReplyMarkup(attributes());
        }
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

    private ReplyKeyboardMarkup adminAttributes() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        KeyboardRow row2 = new KeyboardRow();
        KeyboardRow row3 = new KeyboardRow();
        int i = 0;
        for (String command : messageHandlers.keySet()) {
            if (i > 0 && i < 3) {
                row.add(command);
            }
            if (i > 2 && i < 5) {
                row2.add(command);
            }
            if (i > 4) {
                row3.add(command);
            }
            i++;
        }
        keyboard.add(row);
        keyboard.add(row2);
        keyboard.add(row3);
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

    private InlineKeyboardMarkup getAdminOptions(List<String> values) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (String value : values) {
            List<InlineKeyboardButton> row = new ArrayList<>();
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(value);
            button.setCallbackData(value);
            row.add(button);
            rows.add(row);
        }
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }

    private InlineKeyboardMarkup getStyleOptions(Set<String> values) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        int counter = 0;
        int startIndex = (currentPage - 1) * 9;
        int endIndex = Math.min(startIndex + 9, values.size());
        List<String> pageValues = new ArrayList<>(values).subList(startIndex, endIndex);
        for (String value : pageValues) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(value);
            button.setCallbackData(value);
            row.add(button);
            counter++;
            if (counter == 3) {
                rows.add(row);
                row = new ArrayList<>();
                counter = 0;
            }
        }
        if (counter > 0) {
            rows.add(row);
        }
        List<InlineKeyboardButton> paginationRow = new ArrayList<>();
        if (currentPage > 1) {
            InlineKeyboardButton previousButton = new InlineKeyboardButton();
            previousButton.setText(getTranslate(PAGINATION_PREVIOUS));
            previousButton.setCallbackData(getTranslate(PAGINATION_PREVIOUS));
            paginationRow.add(previousButton);
        }
        if (currentPage < totalPages) {
            InlineKeyboardButton nextButton = new InlineKeyboardButton();
            nextButton.setText(getTranslate(PAGINATION_NEXT));
            nextButton.setCallbackData(getTranslate(PAGINATION_NEXT));
            paginationRow.add(nextButton);
        }
        rows.add(paginationRow);
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
    }

    private void checkPaginationCallback(String paginationData, long chatId, int messageId) {
        totalPages = (int) Math.ceil((double) styles.keySet().size() / 9);
        if (paginationData.equals(getTranslate(PAGINATION_PREVIOUS))) {
            currentPage -= 1;
        }
        if (paginationData.equals(getTranslate(PAGINATION_NEXT))) {
            currentPage += 1;
        }
        InlineKeyboardMarkup markup = getStyleOptions(styles.keySet());
        EditMessageReplyMarkup editMarkup = new EditMessageReplyMarkup();
        editMarkup.setChatId(chatId);
        editMarkup.setMessageId(messageId);
        editMarkup.setReplyMarkup(markup);
        try {
            execute(editMarkup);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void createAdWithImage(String message, String photoPath) {
        Set<Long> ids = chatIds.getIds();
        for (Long id : ids) {
            sendMessageWithImage(message, id, photoPath);
        }

    }

    private void createAdWithText(String message) {
        Set<Long> ids = chatIds.getIds();
        for (Long id : ids) {
            sendMessage(message, id);
        }
    }

    private void createAdWithImageTest(String message, String photoPath, Long chatId) {
        sendMessageWithImage(message, chatId, photoPath);
    }

    private void createAdWithTextTest(String message, Long chatId) {
        sendMessage(message, chatId);
    }

    private void resetValues() {
        this.quantity = null;
        this.size = null;
        this.currentStyle = null;
        this.width = null;
        this.height = null;
    }

    private void resetAdminValues() {
        this.isCreateAdMessage = false;
        this.isCreateAd = false;
        this.isCreateAdImage = false;
        this.isHandlingMessages = true;
        this.adminMessage = null;
    }
}