package com.shiyu.service.ai;

import com.google.gson.*;
import com.shiyu.config.AiConfig;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
public class AiClient {
    private final AiConfig config;
    private final OkHttpClient httpClient;
    private final Gson gson = new GsonBuilder().create();

    public AiClient(AiConfig config) {
        this.config = config;
        this.httpClient = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(120, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();
    }

    public String chat(String systemPrompt, String userMessage) {
        String name = config.getProvider() == null ? "openai" : config.getProvider().toLowerCase();
        AiConfig.Provider p = config.getProviders() != null ? config.getProviders().get(name) : null;
        if (p == null && config.getProviders() != null) p = config.getProviders().get("openai");
        if (p == null) throw new RuntimeException("AI 提供商未配置: " + name);
        String type = p.getType() != null ? p.getType().toLowerCase() : "openai-compatible";
        return switch (type) {
            case "claude" -> callClaude(p, systemPrompt, userMessage);
            case "gemini" -> callGemini(p, systemPrompt, userMessage);
            case "ollama" -> callOllama(p, systemPrompt, userMessage);
            default -> callOpenAiCompatible(p, systemPrompt, userMessage);
        };
    }

    public String chat(String systemPrompt, java.util.List<java.util.Map<String, String>> messages) {
        String name = config.getProvider() == null ? "openai" : config.getProvider().toLowerCase();
        AiConfig.Provider p = config.getProviders() != null ? config.getProviders().get(name) : null;
        if (p == null && config.getProviders() != null) p = config.getProviders().get("openai");
        if (p == null) throw new RuntimeException("AI 提供商未配置: " + name);
        String type = p.getType() != null ? p.getType().toLowerCase() : "openai-compatible";
        if ("claude".equals(type)) return callClaudeWithHistory(p, systemPrompt, messages);
        if ("gemini".equals(type)) return callGeminiWithHistory(p, systemPrompt, messages);
        if ("ollama".equals(type)) return callOllamaWithHistory(p, systemPrompt, messages);
        return callOpenAiCompatibleWithHistory(p, systemPrompt, messages);
    }

    private String callOpenAiCompatible(AiConfig.Provider p, String systemPrompt, String userMessage) {
        String baseUrl = p.getBaseUrl() != null && !p.getBaseUrl().isBlank() ? p.getBaseUrl() : "https://api.openai.com/v1";
        JsonObject body = new JsonObject();
        body.addProperty("model", p.getModel());
        JsonArray messages = new JsonArray();
        JsonObject sysMsg = new JsonObject();
        sysMsg.addProperty("role", "system");
        sysMsg.addProperty("content", systemPrompt);
        messages.add(sysMsg);
        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        userMsg.addProperty("content", userMessage);
        messages.add(userMsg);
        body.add("messages", messages);
        body.addProperty("temperature", 0.85);
        body.addProperty("max_tokens", 8192);
        body.addProperty("top_p", 0.9);

        String authValue;
        if (p.getApiSecret() != null && !p.getApiSecret().isBlank()) {
            authValue = p.getApiKey() + ":" + p.getApiSecret();
        } else {
            authValue = p.getApiKey();
        }

        Request request = new Request.Builder()
            .url(baseUrl + "/chat/completions")
            .addHeader("Authorization", "Bearer " + authValue)
            .addHeader("Content-Type", "application/json")
            .post(RequestBody.create(body.toString(), MediaType.parse("application/json")))
            .build();
        String response = executeRequest(request);
        JsonObject resp = JsonParser.parseString(response).getAsJsonObject();
        return resp.getAsJsonArray("choices").get(0).getAsJsonObject()
            .getAsJsonObject("message").get("content").getAsString();
    }

    private String callClaude(AiConfig.Provider p, String systemPrompt, String userMessage) {
        JsonObject body = new JsonObject();
        body.addProperty("model", p.getModel());
        body.addProperty("max_tokens", 4096);
        body.addProperty("system", systemPrompt);
        JsonArray messages = new JsonArray();
        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        userMsg.addProperty("content", userMessage);
        messages.add(userMsg);
        body.add("messages", messages);

        Request request = new Request.Builder()
            .url("https://api.anthropic.com/v1/messages")
            .addHeader("x-api-key", p.getApiKey())
            .addHeader("anthropic-version", "2023-06-01")
            .addHeader("Content-Type", "application/json")
            .post(RequestBody.create(body.toString(), MediaType.parse("application/json")))
            .build();
        String response = executeRequest(request);
        JsonObject resp = JsonParser.parseString(response).getAsJsonObject();
        return resp.getAsJsonArray("content").get(0).getAsJsonObject().get("text").getAsString();
    }

    private String callGemini(AiConfig.Provider p, String systemPrompt, String userMessage) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + p.getModel() + ":generateContent?key=" + p.getApiKey();
        JsonObject body = new JsonObject();
        JsonArray contents = new JsonArray();
        JsonObject sysPart = new JsonObject();
        sysPart.addProperty("text", systemPrompt);
        JsonObject sysContent = new JsonObject();
        sysContent.add("parts", new JsonArray());
        sysContent.getAsJsonArray("parts").add(sysPart);
        sysContent.addProperty("role", "user");
        contents.add(sysContent);
        JsonObject userPart = new JsonObject();
        userPart.addProperty("text", userMessage);
        JsonObject userContent = new JsonObject();
        userContent.add("parts", new JsonArray());
        userContent.getAsJsonArray("parts").add(userPart);
        userContent.addProperty("role", "user");
        contents.add(userContent);
        body.add("contents", contents);

        Request request = new Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .post(RequestBody.create(body.toString(), MediaType.parse("application/json")))
            .build();
        String response = executeRequest(request);
        JsonObject resp = JsonParser.parseString(response).getAsJsonObject();
        return resp.getAsJsonArray("candidates").get(0).getAsJsonObject()
            .getAsJsonObject("content").getAsJsonArray("parts").get(0).getAsJsonObject().get("text").getAsString();
    }

    private String callOllama(AiConfig.Provider p, String systemPrompt, String userMessage) {
        JsonObject body = new JsonObject();
        body.addProperty("model", p.getModel());
        body.addProperty("stream", false);
        JsonArray messages = new JsonArray();
        JsonObject sysMsg = new JsonObject();
        sysMsg.addProperty("role", "system");
        sysMsg.addProperty("content", systemPrompt);
        messages.add(sysMsg);
        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        userMsg.addProperty("content", userMessage);
        messages.add(userMsg);
        body.add("messages", messages);

        Request request = new Request.Builder()
            .url(p.getBaseUrl() + "/api/chat")
            .addHeader("Content-Type", "application/json")
            .post(RequestBody.create(body.toString(), MediaType.parse("application/json")))
            .build();
        String response = executeRequest(request);
        JsonObject resp = JsonParser.parseString(response).getAsJsonObject();
        return resp.getAsJsonObject("message").get("content").getAsString();
    }

    private String executeRequest(Request request) {
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "No body";
                throw new RuntimeException("AI API error " + response.code() + ": " + errorBody);
            }
            return response.body() != null ? response.body().string() : "";
        } catch (IOException e) {
            throw new RuntimeException("AI API call failed: " + e.getMessage(), e);
        }
    }

    private String callOpenAiCompatibleWithHistory(AiConfig.Provider p, String systemPrompt, java.util.List<java.util.Map<String, String>> messages) {
        String baseUrl = p.getBaseUrl() != null && !p.getBaseUrl().isBlank() ? p.getBaseUrl() : "https://api.openai.com/v1";
        JsonObject body = new JsonObject();
        body.addProperty("model", p.getModel());
        JsonArray msgs = new JsonArray();
        JsonObject sysMsg = new JsonObject();
        sysMsg.addProperty("role", "system");
        sysMsg.addProperty("content", systemPrompt);
        msgs.add(sysMsg);
        for (java.util.Map<String, String> m : messages) {
            JsonObject msg = new JsonObject();
            msg.addProperty("role", m.get("role"));
            msg.addProperty("content", m.get("content"));
            msgs.add(msg);
        }
        body.add("messages", msgs);
        body.addProperty("temperature", 0.85);
        body.addProperty("max_tokens", 8192);
        body.addProperty("top_p", 0.9);
        String authValue;
        if (p.getApiSecret() != null && !p.getApiSecret().isBlank()) {
            authValue = p.getApiKey() + ":" + p.getApiSecret();
        } else {
            authValue = p.getApiKey();
        }
        Request request = new Request.Builder()
            .url(baseUrl + "/chat/completions")
            .addHeader("Authorization", "Bearer " + authValue)
            .addHeader("Content-Type", "application/json")
            .post(RequestBody.create(body.toString(), MediaType.parse("application/json")))
            .build();
        String response = executeRequest(request);
        JsonObject resp = JsonParser.parseString(response).getAsJsonObject();
        return resp.getAsJsonArray("choices").get(0).getAsJsonObject()
            .getAsJsonObject("message").get("content").getAsString();
    }

    private String callClaudeWithHistory(AiConfig.Provider p, String systemPrompt, java.util.List<java.util.Map<String, String>> messages) {
        JsonObject body = new JsonObject();
        body.addProperty("model", p.getModel());
        body.addProperty("max_tokens", 4096);
        body.addProperty("system", systemPrompt);
        JsonArray msgs = new JsonArray();
        for (java.util.Map<String, String> m : messages) {
            JsonObject msg = new JsonObject();
            msg.addProperty("role", m.get("role"));
            msg.addProperty("content", m.get("content"));
            msgs.add(msg);
        }
        body.add("messages", msgs);
        Request request = new Request.Builder()
            .url("https://api.anthropic.com/v1/messages")
            .addHeader("x-api-key", p.getApiKey())
            .addHeader("anthropic-version", "2023-06-01")
            .addHeader("Content-Type", "application/json")
            .post(RequestBody.create(body.toString(), MediaType.parse("application/json")))
            .build();
        String response = executeRequest(request);
        JsonObject resp = JsonParser.parseString(response).getAsJsonObject();
        return resp.getAsJsonArray("content").get(0).getAsJsonObject().get("text").getAsString();
    }

    private String callGeminiWithHistory(AiConfig.Provider p, String systemPrompt, java.util.List<java.util.Map<String, String>> messages) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/" + p.getModel() + ":generateContent?key=" + p.getApiKey();
        JsonObject body = new JsonObject();
        JsonArray contents = new JsonArray();
        for (java.util.Map<String, String> m : messages) {
            JsonObject part = new JsonObject();
            part.addProperty("text", m.get("content"));
            JsonObject content = new JsonObject();
            content.add("parts", new JsonArray());
            content.getAsJsonArray("parts").add(part);
            content.addProperty("role", "user".equals(m.get("role")) ? "user" : "model");
            contents.add(content);
        }
        body.add("contents", contents);
        Request request = new Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .post(RequestBody.create(body.toString(), MediaType.parse("application/json")))
            .build();
        String response = executeRequest(request);
        JsonObject resp = JsonParser.parseString(response).getAsJsonObject();
        return resp.getAsJsonArray("candidates").get(0).getAsJsonObject()
            .getAsJsonObject("content").getAsJsonArray("parts").get(0).getAsJsonObject().get("text").getAsString();
    }

    private String callOllamaWithHistory(AiConfig.Provider p, String systemPrompt, java.util.List<java.util.Map<String, String>> messages) {
        JsonObject body = new JsonObject();
        body.addProperty("model", p.getModel());
        body.addProperty("stream", false);
        JsonArray msgs = new JsonArray();
        JsonObject sysMsg = new JsonObject();
        sysMsg.addProperty("role", "system");
        sysMsg.addProperty("content", systemPrompt);
        msgs.add(sysMsg);
        for (java.util.Map<String, String> m : messages) {
            JsonObject msg = new JsonObject();
            msg.addProperty("role", m.get("role"));
            msg.addProperty("content", m.get("content"));
            msgs.add(msg);
        }
        body.add("messages", msgs);
        Request request = new Request.Builder()
            .url(p.getBaseUrl() + "/api/chat")
            .addHeader("Content-Type", "application/json")
            .post(RequestBody.create(body.toString(), MediaType.parse("application/json")))
            .build();
        String response = executeRequest(request);
        JsonObject resp = JsonParser.parseString(response).getAsJsonObject();
        return resp.getAsJsonObject("message").get("content").getAsString();
    }
}
