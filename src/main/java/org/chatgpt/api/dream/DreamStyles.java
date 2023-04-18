package org.chatgpt.api.dream;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class DreamStyles {

    private static final String STYLES_URL = "https://api.luan.tools/api/styles/";
    private HttpClient client;
    private HttpGet get;

    public Map<String, String> getStyles() {
        createConnection();
        String request = createRequest();
        return createResponse(request);
    }

    private void createConnection() {
        this.client = HttpClientBuilder.create().build();
        this.get = new HttpGet();

        get.setHeader("Accept", "application/json");
        get.setHeader("Content-Type", "application/json; utf-8");
    }

    private String createRequest() {
        StringBuilder result = new StringBuilder();
        try {
            get.setURI(URI.create(STYLES_URL));
            HttpResponse response = client.execute(get);
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

    private Map<String, String> createResponse(String response) {
        JsonElement element = JsonParser.parseString(response);
        JsonArray jsonArray = element.getAsJsonArray();

        Map<String, String> styles = new LinkedHashMap<>();

        for (JsonElement jsonElement : jsonArray) {
            String name = jsonElement.getAsJsonObject().get("name").getAsString();
            String id = jsonElement.getAsJsonObject().get("id").getAsString();
            styles.put(name, id);
        }
        return styles;
    }
}
