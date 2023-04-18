package org.chatgpt.api.dream;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class DreamApi {
    private static final String API_KEY = "vDbdhIrB85TvQ8lCIrBi49nouJ9i6NBt";
    private static final String URL = "https://api.luan.tools/api/tasks/";

    private final DreamImage dreamImage;
    private final DreamStyles dreamStyles;
    private HttpClient client;
    private HttpPut put;
    private HttpPost post;

    public DreamApi() {
        this.dreamImage = new DreamImage();
        this.dreamStyles = new DreamStyles();
    }

    private void createConnection() {
        this.client = HttpClientBuilder.create().build();
        this.put = new HttpPut();
        this.post = new HttpPost();

        put.setHeader("Authorization", "Bearer " + API_KEY);
        put.setHeader("Accept", "application/json");
        put.setHeader("Content-Type", "application/json; utf-8");

        post.setHeader("Authorization", "Bearer " + API_KEY);
        post.setHeader("Accept", "application/json");
        post.setHeader("Content-Type", "application/json; utf-8");
    }

    private String createRequest(HttpEntityEnclosingRequestBase httpMethod) {
        StringBuilder result = new StringBuilder();
        try {
            HttpResponse response = client.execute(httpMethod);
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

    public InputFile generateImages(String styleId, String description) {
        createConnection();
        StringEntity entity = new StringEntity(dreamImage.createRequest(styleId, description), StandardCharsets.UTF_8);
        put.setURI(URI.create(URL + createTaskId()));
        put.setEntity(entity);
        String requestResult = createRequest(put);
        return dreamImage.createResponse(requestResult);
    }

    private String createTaskId() {
        String result = "";
        try {
            String jsonInputString = "{ \"use_target_image\": true }";
            StringEntity entity = new StringEntity(jsonInputString, StandardCharsets.UTF_8);
            post.setURI(URI.create(URL));
            post.setEntity(entity);
            String requestResult = createRequest(post);

            System.out.println("Task Result: " + requestResult);

//            String targetImageUrl = requestResult.split("\"target_image_url\":")[1];

//            System.out.println("Task URL: " + targetImageUrl);

//            result = targetImageUrl.split("/")[2];

            Gson jsonResult = new Gson().newBuilder().setPrettyPrinting().create();
            JsonObject object = jsonResult.fromJson(requestResult, JsonObject.class);

            System.out.println("Response: " + object.toString());

            JsonElement id = object.get("id");

            result = id.getAsString();
            System.out.println("Task ID: " + result);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return result;
    }

    public Map<String, String> getStyles() {
        return dreamStyles.getStyles();
    }
}
