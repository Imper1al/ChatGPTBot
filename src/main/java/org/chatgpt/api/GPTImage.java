package org.chatgpt.api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class GPTImage {

    public String createRequest(String request, int quantity, String size) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("prompt", request);
        jsonObject.addProperty("n", quantity);
        jsonObject.addProperty("size", size);
        return jsonObject.toString();
    }

    public List<InputFile> createResponse(String response) {
        List<InputFile> images = new ArrayList<>();
        Gson jsonResult = new Gson().newBuilder().setPrettyPrinting().create();
        JsonObject object = jsonResult.fromJson(response, JsonObject.class);
        JsonArray jsonArray = object.getAsJsonArray("data");
        if(jsonArray.size() > 0) {
            int imageCounter = 1;
            for (JsonElement jsonElements : jsonArray) {
                JsonObject jsonObject = jsonElements.getAsJsonObject();
                JsonElement url = jsonObject.get("url");
                images.add(saveImage(url.getAsString(), "image" + imageCounter + ".png"));
                imageCounter++;
            }
        }
        return images;
    }

    private InputFile saveImage(String url, String imageName) {
        InputFile image = null;
        try {
            FileUtils.copyURLToFile(new URL(url), new File(imageName));
            image = new InputFile(new File(imageName));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return image;
    }
}
