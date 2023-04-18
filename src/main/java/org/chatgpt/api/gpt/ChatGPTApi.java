package org.chatgpt.api.gpt;

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.HttpResponse;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ChatGPTApi {

    private static final String API_KEY = "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";
    private static final String API_MESSAGE_URL = "https://api.openai.com/v1/chat/completions";
    private static final String API_IMAGE_URL = "https://api.openai.com/v1/images/generations";

    private final GPTMessage gptMessage;
    private final GPTImage gptImage;
    HttpClient client;
    HttpPost post;

    public ChatGPTApi() {
        this.gptMessage = new GPTMessage();
        this.gptImage = new GPTImage();
    }

    private void createConnection() {
        this.client = HttpClientBuilder.create().build();
        this.post = new HttpPost();

        post.setHeader("Authorization", "Bearer " + API_KEY);
        post.setHeader("Accept", "application/json");
        post.setHeader("Content-Type", "application/json; utf-8");

        int timeout = 120;
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(timeout * 1000)
                .setConnectionRequestTimeout(timeout * 1000)
                .setSocketTimeout(timeout * 1000)
                .build();
        post.setConfig(config);
    }

    private String createRequest(String url, StringEntity entity) {
        createConnection();
        StringBuilder result = new StringBuilder();
        try {
            post.setURI(URI.create(url));
            post.setEntity(entity);
            HttpResponse response = client.execute(post);
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
            return e.getMessage();

        }
        return result.toString();
    }

    public String executeMessage(List<String> context) {
        StringEntity entity = new StringEntity(gptMessage.createRequest(context), StandardCharsets.UTF_8);
        String requestResult = createRequest(API_MESSAGE_URL, entity);
        return gptMessage.createResponse(requestResult);
    }

    public List<InputFile> executeImage(String request, String quantity, String size) {
        StringEntity entity = new StringEntity(gptImage.createRequest(request, Integer.parseInt(quantity), size), StandardCharsets.UTF_8);
        String requestResult = createRequest(API_IMAGE_URL, entity);
        return gptImage.createResponse(requestResult);
    }
}
