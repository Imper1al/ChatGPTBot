package org.chatgpt.api.dream;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
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

    private final DreamImage dreamImage;
    private final DreamStyles dreamStyles;
    private HttpClient client;
    private HttpPut put;

    public DreamApi() {
        this.dreamImage = new DreamImage();
        this.dreamStyles = new DreamStyles();
    }

    private void createConnection() {
        this.client = HttpClientBuilder.create().build();
        this.put = new HttpPut();

        put.setHeader("Authorization", "Bearer " + API_KEY);
        put.setHeader("Accept", "application/json");
        put.setHeader("Content-Type", "application/json; utf-8");
    }

    private String createRequest(String url, StringEntity entity) {
        createConnection();
        StringBuilder result = new StringBuilder();
        try {
            put.setURI(URI.create(url));
            put.setEntity(entity);
            HttpResponse response = client.execute(put);
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
        StringEntity entity = new StringEntity(dreamImage.createRequest(styleId, description), StandardCharsets.UTF_8);
        String requestResult = createRequest("https://api.luan.tools/api/tasks/" + createTaskId(), entity);
        return dreamImage.createResponse(requestResult);
    }

    private String createTaskId() {
        String result = "";
        try {
            URL url = new URL("https://api.luan.tools/api/tasks/");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Authorization", "Bearer " + API_KEY);
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Accept", "application/json");

            con.setDoOutput(true);

            String jsonInputString = "{ \"use_target_image\": true }";

            try (var os = con.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
            }

            String targetImageUrl = response.toString().split("\"target_image_url\":")[1];

            result = targetImageUrl.split("/")[2];
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return result;
    }

    public Map<String, String> getStyles() {
        return dreamStyles.getStyles();
    }
}
