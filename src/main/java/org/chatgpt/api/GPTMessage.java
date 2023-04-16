package org.chatgpt.api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.List;

public class GPTMessage {

    public String createRequest(String request, List<String> pastContext) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("model", "gpt-3.5-turbo");

        JsonArray jsonArray = new JsonArray();
        JsonObject role = new JsonObject();
        role.addProperty("role", "user");
        role.addProperty("content", request);
        jsonArray.add(role);
        jsonObject.add("messages", jsonArray);

        if(!pastContext.isEmpty()) {
            JsonArray jsonContext = new JsonArray();
            for (String contextElement : pastContext) {
                jsonContext.add(contextElement);
            }
            jsonObject.add("context", jsonContext);
        }
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
