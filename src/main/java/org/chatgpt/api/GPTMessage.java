package org.chatgpt.api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
public class GPTMessage {

    public String createRequest(String request) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("model", "gpt-3.5-turbo");

        JsonArray jsonArray = new JsonArray();
        JsonObject role = new JsonObject();
        role.addProperty("role", "user");
        role.addProperty("content", request);
        jsonArray.add(role);
        jsonObject.add("messages", jsonArray);
        return jsonObject.toString();
    }

    public String createResponse(String response) {
        Gson jsonResult = new Gson().newBuilder().setPrettyPrinting().create();
        JsonObject object = jsonResult.fromJson(response, JsonObject.class);
        JsonArray jsonArray = object.getAsJsonArray("choices");
        JsonElement jsonElement = jsonArray.get(0);
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        JsonObject message = jsonObject.getAsJsonObject("message");
        JsonElement content = message.get("content");
        return content.getAsString();
    }
}
