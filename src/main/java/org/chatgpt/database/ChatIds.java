package org.chatgpt.database;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

public class ChatIds {

    private static final String DATABASE_URL = "src/main/resources/database/chatIds.txt";
    private final Set<Long> ids;
    private static ChatIds instance;

    private ChatIds() {
        this.ids = new HashSet<>();
        parseDatabaseFile();
        for (Long id : ids) {
            System.out.print(id + ",");
        }
    }

    public static ChatIds getInstance() {
        if (instance == null) {
            instance = new ChatIds();
        }
        return instance;
    }

    public void addIdToDatabase(Long chatId) {
        if (!ids.contains(chatId)) {
            String newId = chatId.toString();

            try {
                InputStream inputStream = getClass().getClassLoader().getResourceAsStream(DATABASE_URL);
                assert inputStream != null;
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                String currentContent = reader.readLine();
                reader.close();

                StringBuilder sb = new StringBuilder();
                if (currentContent != null) {
                    sb.append(currentContent);
                    sb.append(",");
                }
                sb.append(newId);
                String updatedContent = sb.toString();

                OutputStream outputStream = new FileOutputStream(DATABASE_URL);
                Writer writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
                writer.write(updatedContent);
                writer.close();

                System.out.println("Идентификатор успешно добавлен в базу данных.");
            } catch (IOException e) {
                System.err.println("Ошибка при добавлении идентификатора в базу данных: " + e.getMessage());
            }
        }
    }

    private void parseDatabaseFile() {
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(DATABASE_URL);
            assert inputStream != null;
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] idArray = line.split(",");
                for (String id : idArray) {
                    ids.add(Long.parseLong(id.trim()));
                }
            }
            reader.close();
        } catch (IOException e) {
            System.err.println("Ошибка при парсинге файла базы данных: " + e.getMessage());
        }

    }

    public Set<Long> getIds() {
        return ids;
    }

    public String userCounter() {
        return "Количество пользователей бота: " + ids.size();
    }
}
