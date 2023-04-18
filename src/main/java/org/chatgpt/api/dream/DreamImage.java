package org.chatgpt.api.dream;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;
import org.json.JSONObject;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import java.io.*;
import java.net.URL;

public class DreamImage {

    public String createRequest(String styleId, String description) {
        JSONObject inputSpec = new JSONObject();
        inputSpec.put("style", Integer.valueOf(styleId));
        inputSpec.put("prompt", description);

        JSONObject json = new JSONObject();
        json.put("input_spec", inputSpec);

        System.out.println("Request: " + inputSpec);
        return json.toString();
    }

    public InputFile createResponse(String response) {
        Gson jsonResult = new Gson().newBuilder().setPrettyPrinting().create();
        JsonObject object = jsonResult.fromJson(response, JsonObject.class);

        System.out.println("Response: " + object.toString());
        JsonObject result = object.getAsJsonObject("result");
        JsonElement url = result.get("final");
        return saveImage(url.getAsString());
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
