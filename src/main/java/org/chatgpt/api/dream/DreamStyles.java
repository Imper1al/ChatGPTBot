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
import java.util.ArrayList;
import java.util.List;

public class DreamStyles {

    private static final String STYLES_URL = "https://api.luan.tools/api/styles/";
    private HttpClient client;
    private HttpGet get;

    public List<String> getStyles() {
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

    private List<String> createResponse(String response) {
        JsonElement element = JsonParser.parseString(response);
        JsonArray jsonArray = element.getAsJsonArray();

        List<String> styles = new ArrayList<>();

        for (JsonElement jsonElement : jsonArray) {
            String name = jsonElement.getAsJsonObject().get("name").getAsString();
            styles.add(name);
        }
        return styles;
    }
}
