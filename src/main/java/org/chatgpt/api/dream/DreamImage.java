package org.chatgpt.api.dream;

import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DreamImage {
    private final String apiKey;

    public DreamImage(String apiKey) {
        this.apiKey = apiKey;
    }

    private String createRequest(String styleId, String description) {
        JSONObject inputSpec = new JSONObject();
        inputSpec.put("style", Integer.valueOf(styleId));
        inputSpec.put("prompt", description);

        JSONObject json = new JSONObject();
        json.put("input_spec", inputSpec);
        return json.toString();
    }

    public HttpURLConnection apiRequest(String styleName, String description) {
        HttpURLConnection con = null;
        try {
            URL url = new URL("https://api.luan.tools/api/tasks/" + createTaskId());
            con = (HttpURLConnection) url.openConnection();
            con.setDoOutput(true);
            con.setRequestMethod("PUT");
            con.setRequestProperty("Authorization", "Bearer " + apiKey);
            con.setRequestProperty("Content-Type", "application/json");
            OutputStream os = con.getOutputStream();
            String request = createRequest(styleName, description);
            os.write(request.getBytes());
            os.flush();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return con;
    }

    public InputFile getResponse(HttpURLConnection connection) {
        InputFile inputFile = new InputFile();
        try {
            InputStream is = connection.getInputStream();
            byte[] buffer = new byte[4096];
            int bytesRead = -1;
            StringBuilder responseBuilder = new StringBuilder();
            while ((bytesRead = is.read(buffer)) != -1) {
                responseBuilder.append(new String(buffer, 0, bytesRead));
            }
            String response = responseBuilder.toString();
            JSONObject jsonResponse = new JSONObject(response);

            String imageUrl = jsonResponse.getString("url");

            inputFile = saveImage(imageUrl);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return inputFile;
    }

    private String createTaskId() {
        String result = "";
        try {
            URL url = new URL("https://api.luan.tools/api/tasks/");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Authorization", "Bearer " + apiKey);
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

    private InputFile saveImage(String url) {
        InputFile image = null;
        try {
            FileUtils.copyURLToFile(new URL(url), new File("image.png"));
            image = new InputFile(new File("image.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }
}
