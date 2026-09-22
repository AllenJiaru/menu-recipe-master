package com.shiyu.service;

import com.shiyu.dto.request.*;
import com.shiyu.dto.response.AiResponse;

import java.util.Map;

public interface AiService {
    AiResponse chat(AiRequest request);
    AiResponse smartRecommend(String userId);
    AiResponse ingredientToRecipe(AiRecipeRequest request);
    AiResponse analyzeNutrition(AiNutritionRequest request);
    AiResponse generateMealPlan(AiMealPlanRequest request);
    AiResponse recognizeDish(String base64Image);
    AiResponse generateShoppingList(String mealPlanContext);
    AiResponse leftoverSuggestion(String leftovers);
    AiResponse scoreRecipe(String recipeContext);
    AiResponse cookingQA(String question);
    AiResponse healthReport(String userId);
    AiResponse translateRecipe(AiTranslationRequest request);

    // P0 新增
    AiResponse semanticSearch(AiRequest request);
    AiResponse smartOrder(AiRequest request);
    AiResponse recipeAssist(AiRequest request);
    AiResponse inventoryAdvisor(AiRequest request);

    // P1 新增
    AiResponse inventoryPredict(AiRequest request);
    AiResponse sceneMenu(AiRequest request);
    AiResponse dataInsight(AiRequest request);
    AiResponse copywriting(AiRequest request);

    // P2 新增
    AiResponse smartSchedule(AiRequest request);
    AiResponse userProfile(AiRequest request);
    AiResponse trendPredict(AiRequest request);
    AiResponse menuAnalysis(AiRequest request);
    AiResponse orderAnalysis(AiRequest request);

    Map<String, Object> getProviderInfo();
    java.util.List<com.shiyu.entity.AiChatSession> listSessions(Long userId);
    com.shiyu.entity.AiChatSession createSession(String title, Long userId);
    void deleteSession(Long sessionId, Long userId);
    java.util.List<com.shiyu.entity.AiChatMessage> listMessages(Long sessionId, Long userId);
    AiResponse chatWithSession(ChatMessageRequest request, Long userId);
}
