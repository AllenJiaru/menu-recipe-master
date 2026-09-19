package com.shiyu.controller;

import com.shiyu.common.ApiResponse;
import com.shiyu.config.AiConfig;
import com.shiyu.dto.request.*;
import com.shiyu.dto.response.AiResponse;
import com.shiyu.entity.AiChatMessage;
import com.shiyu.entity.AiChatSession;
import com.shiyu.security.UserDetailsImpl;
import com.shiyu.service.AiService;
import com.shiyu.service.ai.AiConfigPersistence;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ai")
public class AiController {
    private final AiService aiService;
    private final AiConfig aiConfig;
    private final AiConfigPersistence configPersistence;

    public AiController(AiService aiService, AiConfig aiConfig, AiConfigPersistence configPersistence) {
        this.aiService = aiService;
        this.aiConfig = aiConfig;
        this.configPersistence = configPersistence;
    }

    private Long getCurrentUserId() {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return userDetails.getUserId();
        } catch (Exception e) {
            return null;
        }
    }

    @GetMapping("/info")
    public ApiResponse<Map<String, Object>> getProviderInfo() {
        return ApiResponse.success(aiService.getProviderInfo());
    }

    @PostMapping("/chat")
    public ApiResponse<AiResponse> chat(@RequestBody AiRequest request) {
        return ApiResponse.success(aiService.chat(request));
    }

    @GetMapping("/recommend")
    public ApiResponse<AiResponse> smartRecommend(@RequestParam(defaultValue = "1") String userId) {
        return ApiResponse.success(aiService.smartRecommend(userId));
    }

    @PostMapping("/ingredient-to-recipe")
    public ApiResponse<AiResponse> ingredientToRecipe(@RequestBody AiRecipeRequest request) {
        return ApiResponse.success(aiService.ingredientToRecipe(request));
    }

    @PostMapping("/nutrition")
    public ApiResponse<AiResponse> analyzeNutrition(@RequestBody AiNutritionRequest request) {
        return ApiResponse.success(aiService.analyzeNutrition(request));
    }

    @PostMapping("/meal-plan")
    public ApiResponse<AiResponse> generateMealPlan(@RequestBody AiMealPlanRequest request) {
        return ApiResponse.success(aiService.generateMealPlan(request));
    }

    @PostMapping("/recognize")
    public ApiResponse<AiResponse> recognizeDish(@RequestBody Map<String, String> request) {
        return ApiResponse.success(aiService.recognizeDish(request.getOrDefault("image", "")));
    }

    @PostMapping("/shopping-list")
    public ApiResponse<AiResponse> generateShoppingList(@RequestBody Map<String, String> request) {
        return ApiResponse.success(aiService.generateShoppingList(request.getOrDefault("mealPlan", "")));
    }

    @PostMapping("/leftover")
    public ApiResponse<AiResponse> leftoverSuggestion(@RequestBody AiRequest request) {
        return ApiResponse.success(aiService.leftoverSuggestion(request.getMessage()));
    }

    @PostMapping("/score")
    public ApiResponse<AiResponse> scoreRecipe(@RequestBody AiRequest request) {
        return ApiResponse.success(aiService.scoreRecipe(request.getMessage()));
    }

    @PostMapping("/cooking-qa")
    public ApiResponse<AiResponse> cookingQA(@RequestBody AiRequest request) {
        return ApiResponse.success(aiService.cookingQA(request.getMessage()));
    }

    @GetMapping("/health-report")
    public ApiResponse<AiResponse> healthReport(@RequestParam(defaultValue = "1") String userId) {
        return ApiResponse.success(aiService.healthReport(userId));
    }

    @PostMapping("/translate")
    public ApiResponse<AiResponse> translateRecipe(@RequestBody AiTranslationRequest request) {
        return ApiResponse.success(aiService.translateRecipe(request));
    }

    @GetMapping("/config")
    public ApiResponse<Map<String, Object>> getConfig() {
        Map<String, Object> result = new HashMap<>();
        result.put("provider", aiConfig.getProvider());
        Map<String, Object> providers = new HashMap<>();
        if (aiConfig.getProviders() != null) {
            aiConfig.getProviders().forEach((id, p) -> {
                Map<String, Object> m = new HashMap<>();
                m.put("type", p.getType());
                m.put("apiKey", maskKey(p.getApiKey()));
                m.put("apiSecret", p.getApiSecret() != null && !p.getApiSecret().isBlank() ? "****" : "");
                m.put("baseUrl", p.getBaseUrl());
                m.put("model", p.getModel());
                m.put("configured", p.getApiKey() != null && !p.getApiKey().isBlank());
                providers.put(id, m);
            });
        }
        result.put("providers", providers);
        return ApiResponse.success(result);
    }

    @PostMapping("/config")
    public ApiResponse<String> updateConfig(@RequestBody AiConfigRequest request) {
        if (request.getProvider() != null && !request.getProvider().isBlank()) {
            aiConfig.setProvider(request.getProvider());
        }
        if (request.getProviders() != null) {
            request.getProviders().forEach((id, newP) -> {
                AiConfig.Provider p = aiConfig.getProviders().get(id);
                if (p == null) {
                    p = new AiConfig.Provider();
                    aiConfig.getProviders().put(id, p);
                }
                if (newP.getApiKey() != null && !newP.getApiKey().isBlank() && !newP.getApiKey().contains("****")) {
                    p.setApiKey(newP.getApiKey());
                }
                if (newP.getApiSecret() != null && !newP.getApiSecret().isBlank() && !newP.getApiSecret().contains("****")) {
                    p.setApiSecret(newP.getApiSecret());
                }
                if (newP.getBaseUrl() != null && !newP.getBaseUrl().isBlank()) p.setBaseUrl(newP.getBaseUrl());
                if (newP.getModel() != null && !newP.getModel().isBlank()) p.setModel(newP.getModel());
                if (newP.getType() != null && !newP.getType().isBlank()) p.setType(newP.getType());
            });
        }
        configPersistence.saveProvider(aiConfig.getProvider());
        configPersistence.saveProviders(aiConfig.getProviders());
        return ApiResponse.success("配置已更新");
    }

    @PostMapping("/test")
    public ApiResponse<Map<String, Object>> testConnection() {
        long start = System.currentTimeMillis();
        try {
            AiResponse result = aiService.cookingQA("回答：测试连接成功");
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", true);
            resp.put("provider", aiConfig.getProvider());
            resp.put("responseTime", System.currentTimeMillis() - start);
            resp.put("response", result.getContent().substring(0, Math.min(result.getContent().length(), 100)));
            return ApiResponse.success(resp);
        } catch (Exception e) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("success", false);
            resp.put("provider", aiConfig.getProvider());
            resp.put("error", e.getMessage());
            return ApiResponse.success(resp);
        }
    }

    private String maskKey(String key) {
        if (key == null || key.isBlank()) return "";
        if (key.length() < 8) return "****";
        return key.substring(0, 4) + "****" + key.substring(key.length() - 4);
    }

    @GetMapping("/sessions")
    public ApiResponse<List<AiChatSession>> listSessions() {
        return ApiResponse.success(aiService.listSessions(getCurrentUserId()));
    }

    @PostMapping("/sessions")
    public ApiResponse<AiChatSession> createSession(@RequestBody(required = false) CreateSessionRequest request) {
        String title = request != null ? request.getTitle() : null;
        return ApiResponse.success(aiService.createSession(title, getCurrentUserId()));
    }

    @DeleteMapping("/sessions/{id}")
    public ApiResponse<String> deleteSession(@PathVariable Long id) {
        aiService.deleteSession(id, getCurrentUserId());
        return ApiResponse.success("已删除");
    }

    @GetMapping("/sessions/{id}/messages")
    public ApiResponse<List<AiChatMessage>> listMessages(@PathVariable Long id) {
        return ApiResponse.success(aiService.listMessages(id, getCurrentUserId()));
    }

    @PostMapping("/chat-session")
    public ApiResponse<AiResponse> chatWithSession(@RequestBody ChatMessageRequest request) {
        return ApiResponse.success(aiService.chatWithSession(request, getCurrentUserId()));
    }
}
