package org.chatgpt.api.gpt;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.Map;

public class GPTMessage {

    public String createRequest(List<String> context) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("model", "gpt-3.5-turbo");

        if(!context.isEmpty()) {
            JsonArray jsonContext = new JsonArray();
            for (int i = 0; i < context.size(); i += 2) {
                String userContent = context.get(i);
                JsonObject userMsg = new JsonObject();
                userMsg.addProperty("role", "user");
                userMsg.addProperty("content", userContent);
                jsonContext.add(userMsg);

                if (i + 1 < context.size()) {
                    String assistantContent = context.get(i + 1);
                    JsonObject assistantMsg = new JsonObject();
                    assistantMsg.addProperty("role", "assistant");
                    assistantMsg.addProperty("content", assistantContent);
                    jsonContext.add(assistantMsg);
                }
            }
            jsonObject.add("messages", jsonContext);
        }

        System.out.println("Request: " + jsonObject);
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
        System.out.println("Response" + response);
        return content.getAsString();
    }
}
