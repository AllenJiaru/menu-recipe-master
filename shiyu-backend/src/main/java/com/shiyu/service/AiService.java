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
    Map<String, Object> getProviderInfo();
    java.util.List<com.shiyu.entity.AiChatSession> listSessions(Long userId);
    com.shiyu.entity.AiChatSession createSession(String title, Long userId);
    void deleteSession(Long sessionId, Long userId);
    java.util.List<com.shiyu.entity.AiChatMessage> listMessages(Long sessionId, Long userId);
    AiResponse chatWithSession(ChatMessageRequest request, Long userId);
}
