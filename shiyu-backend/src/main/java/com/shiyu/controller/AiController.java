package com.shiyu.controller;

import com.shiyu.common.ApiResponse;
import com.shiyu.config.AiConfig;
import com.shiyu.dto.request.*;
import com.shiyu.dto.response.AiResponse;
import com.shiyu.entity.AiChatMessage;
import com.shiyu.entity.AiChatSession;
import com.shiyu.entity.AiFeatureLog;
import com.shiyu.mapper.AiFeatureLogMapper;
import com.shiyu.security.UserDetailsImpl;
import com.shiyu.service.AiService;
import com.shiyu.service.ai.AiConfigPersistence;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "AI 智能助手", description = "AI 对话、会话管理、智能推荐、营养分析等")
@RestController
@RequestMapping("/api/ai")
public class AiController {
    private final AiService aiService;
    private final AiConfig aiConfig;
    private final AiConfigPersistence configPersistence;
    private final AiFeatureLogMapper logMapper;

    public AiController(AiService aiService, AiConfig aiConfig, AiConfigPersistence configPersistence, AiFeatureLogMapper logMapper) {
        this.aiService = aiService;
        this.aiConfig = aiConfig;
        this.configPersistence = configPersistence;
        this.logMapper = logMapper;
    }

    private Long getCurrentUserId() {
        try {
            UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            return userDetails.getUserId();
        } catch (Exception e) {
            return null;
        }
    }

    @Operation(summary = "获取 AI 供应商信息", description = "返回当前配置的 AI 供应商和模型信息")
    @GetMapping("/info")
    public ApiResponse<Map<String, Object>> getProviderInfo() {
        return ApiResponse.success(aiService.getProviderInfo());
    }

    @Operation(summary = "AI 单次对话", description = "不保留上下文的单次 AI 问答")
    @PostMapping("/chat")
    public ApiResponse<AiResponse> chat(@RequestBody AiRequest request) {
        return ApiResponse.success(aiService.chat(request));
    }

    @Operation(summary = "智能推荐菜品", description = "根据用户口味和时令季节推荐菜品")
    @GetMapping("/recommend")
    public ApiResponse<AiResponse> smartRecommend(@RequestParam(defaultValue = "1") String userId) {
        return ApiResponse.success(aiService.smartRecommend(userId));
    }

    @Operation(summary = "食材转菜谱", description = "根据现有食材推荐可做的菜品")
    @PostMapping("/ingredient-to-recipe")
    public ApiResponse<AiResponse> ingredientToRecipe(@RequestBody AiRecipeRequest request) {
        return ApiResponse.success(aiService.ingredientToRecipe(request));
    }

    @Operation(summary = "营养分析", description = "分析菜品的营养成分")
    @PostMapping("/nutrition")
    public ApiResponse<AiResponse> analyzeNutrition(@RequestBody AiNutritionRequest request) {
        return ApiResponse.success(aiService.analyzeNutrition(request));
    }

    @Operation(summary = "生成膳食计划", description = "AI 安排多天的餐食计划")
    @PostMapping("/meal-plan")
    public ApiResponse<AiResponse> generateMealPlan(@RequestBody AiMealPlanRequest request) {
        return ApiResponse.success(aiService.generateMealPlan(request));
    }

    @Operation(summary = "菜品识别", description = "通过图片识别菜品")
    @PostMapping("/recognize")
    public ApiResponse<AiResponse> recognizeDish(@RequestBody Map<String, String> request) {
        return ApiResponse.success(aiService.recognizeDish(request.getOrDefault("image", "")));
    }

    @Operation(summary = "生成采购清单", description = "根据膳食计划生成采购清单")
    @PostMapping("/shopping-list")
    public ApiResponse<AiResponse> generateShoppingList(@RequestBody Map<String, String> request) {
        return ApiResponse.success(aiService.generateShoppingList(request.getOrDefault("mealPlan", "")));
    }

    @Operation(summary = "剩余食材利用", description = "根据剩余食材推荐菜品")
    @PostMapping("/leftover")
    public ApiResponse<AiResponse> leftoverSuggestion(@RequestBody AiRequest request) {
        return ApiResponse.success(aiService.leftoverSuggestion(request.getMessage()));
    }

    @Operation(summary = "菜谱评分", description = "AI 对菜谱进行综合评分")
    @PostMapping("/score")
    public ApiResponse<AiResponse> scoreRecipe(@RequestBody AiRequest request) {
        return ApiResponse.success(aiService.scoreRecipe(request.getMessage()));
    }

    @Operation(summary = "烹饪问答", description = "AI 烹饪知识问答")
    @PostMapping("/cooking-qa")
    public ApiResponse<AiResponse> cookingQA(@RequestBody AiRequest request) {
        return ApiResponse.success(aiService.cookingQA(request.getMessage()));
    }

    @Operation(summary = "健康报告", description = "生成用户饮食健康报告")
    @GetMapping("/health-report")
    public ApiResponse<AiResponse> healthReport(@RequestParam(defaultValue = "1") String userId) {
        return ApiResponse.success(aiService.healthReport(userId));
    }

    @Operation(summary = "菜谱翻译", description = "将菜谱翻译为其他语言")
    @PostMapping("/translate")
    public ApiResponse<AiResponse> translateRecipe(@RequestBody AiTranslationRequest request) {
        return ApiResponse.success(aiService.translateRecipe(request));
    }

    @Operation(summary = "获取 AI 配置", description = "返回所有 AI 供应商的配置信息（API Key 脱敏）")
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

    @Operation(summary = "更新 AI 配置", description = "更新当前使用的 AI 供应商和 API Key")
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

    @Operation(summary = "测试 AI 连接", description = "测试当前配置的 AI 供应商是否可用")
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

    @Operation(summary = "获取会话列表", description = "获取当前用户的所有 AI 聊天会话")
    @GetMapping("/sessions")
    public ApiResponse<List<AiChatSession>> listSessions() {
        return ApiResponse.success(aiService.listSessions(getCurrentUserId()));
    }

    @Operation(summary = "创建新会话", description = "创建一个新的 AI 聊天会话")
    @PostMapping("/sessions")
    public ApiResponse<AiChatSession> createSession(@RequestBody(required = false) CreateSessionRequest request) {
        String title = request != null ? request.getTitle() : null;
        return ApiResponse.success(aiService.createSession(title, getCurrentUserId()));
    }

    @Operation(summary = "删除会话", description = "删除指定的 AI 聊天会话")
    @DeleteMapping("/sessions/{id}")
    public ApiResponse<String> deleteSession(@PathVariable Long id) {
        aiService.deleteSession(id, getCurrentUserId());
        return ApiResponse.success("已删除");
    }

    @Operation(summary = "获取会话消息", description = "获取指定会话的所有聊天消息")
    @GetMapping("/sessions/{id}/messages")
    public ApiResponse<List<AiChatMessage>> listMessages(@PathVariable Long id) {
        return ApiResponse.success(aiService.listMessages(id, getCurrentUserId()));
    }

    @Operation(summary = "会话对话", description = "在指定会话中发送消息并获取 AI 回复，支持上下文记忆")
    @PostMapping("/chat-session")
    public ApiResponse<AiResponse> chatWithSession(@RequestBody ChatMessageRequest request) {
        return ApiResponse.success(aiService.chatWithSession(request, getCurrentUserId()));
    }

    // ========== P0: 核心 AI 功能 ==========

    @Operation(summary = "AI 语义搜索菜谱", description = "用自然语言描述需求，智能推荐最匹配的菜谱")
    @PostMapping("/semantic-search")
    public ApiResponse<AiResponse> semanticSearch(@RequestBody AiRequest request) {
        return ApiResponse.success(aiService.semanticSearch(request));
    }

    @Operation(summary = "AI 智能点餐", description = "根据人数/预算/场景推荐最优点餐组合")
    @PostMapping("/smart-order")
    public ApiResponse<AiResponse> smartOrder(@RequestBody AiRequest request) {
        return ApiResponse.success(aiService.smartOrder(request));
    }

    @Operation(summary = "AI 菜谱创作辅助", description = "输入菜名自动生成完整菜谱，或优化现有菜谱")
    @PostMapping("/recipe-assist")
    public ApiResponse<AiResponse> recipeAssist(@RequestBody AiRequest request) {
        return ApiResponse.success(aiService.recipeAssist(request));
    }

    @Operation(summary = "AI 库存顾问", description = "根据现有库存推荐最佳菜品组合")
    @PostMapping("/inventory-advisor")
    public ApiResponse<AiResponse> inventoryAdvisor(@RequestBody AiRequest request) {
        return ApiResponse.success(aiService.inventoryAdvisor(request));
    }

    // ========== P1: 效率提升 AI 功能 ==========

    @Operation(summary = "AI 库存预测", description = "预测未来食材需求，智能补货建议")
    @PostMapping("/inventory-predict")
    public ApiResponse<AiResponse> inventoryPredict(@RequestBody AiRequest request) {
        return ApiResponse.success(aiService.inventoryPredict(request));
    }

    @Operation(summary = "AI 场景菜单策划", description = "根据场景（生日宴/减脂餐/家庭聚餐等）生成完整菜单")
    @PostMapping("/scene-menu")
    public ApiResponse<AiResponse> sceneMenu(@RequestBody AiRequest request) {
        return ApiResponse.success(aiService.sceneMenu(request));
    }

    @Operation(summary = "AI 数据洞察", description = "用自然语言解读经营数据并给出建议")
    @PostMapping("/data-insight")
    public ApiResponse<AiResponse> dataInsight(@RequestBody AiRequest request) {
        return ApiResponse.success(aiService.dataInsight(request));
    }

    @Operation(summary = "AI 文案助手", description = "生成公告、推广、菜品描述等各类文案")
    @PostMapping("/copywriting")
    public ApiResponse<AiResponse> copywriting(@RequestBody AiRequest request) {
        return ApiResponse.success(aiService.copywriting(request));
    }

    // ========== P2: 差异化 AI 功能 ==========

    @Operation(summary = "AI 智能排班", description = "根据主厨专长和订单需求安排最优排班")
    @PostMapping("/smart-schedule")
    public ApiResponse<AiResponse> smartSchedule(@RequestBody AiRequest request) {
        return ApiResponse.success(aiService.smartSchedule(request));
    }

    @Operation(summary = "AI 用户口味画像", description = "分析用户行为，生成口味偏好画像")
    @PostMapping("/user-profile")
    public ApiResponse<AiResponse> userProfile(@RequestBody AiRequest request) {
        return ApiResponse.success(aiService.userProfile(request));
    }

    @Operation(summary = "AI 趋势预测", description = "预测美食趋势和热门菜品")
    @PostMapping("/trend-predict")
    public ApiResponse<AiResponse> trendPredict(@RequestBody AiRequest request) {
        return ApiResponse.success(aiService.trendPredict(request));
    }

    @Operation(summary = "AI 整桌菜分析", description = "分析整桌菜品的营养搭配和上菜顺序")
    @PostMapping("/menu-analysis")
    public ApiResponse<AiResponse> menuAnalysis(@RequestBody AiRequest request) {
        return ApiResponse.success(aiService.menuAnalysis(request));
    }

    @Operation(summary = "AI 订单分析", description = "分析订单数据，发现经营洞察")
    @PostMapping("/order-analysis")
    public ApiResponse<AiResponse> orderAnalysis(@RequestBody AiRequest request) {
        return ApiResponse.success(aiService.orderAnalysis(request));
    }

    // ========== AI 历史记录 ==========

    @Operation(summary = "获取 AI 使用历史", description = "分页查询当前用户的 AI 功能使用记录")
    @GetMapping("/history")
    public ApiResponse<Map<String, Object>> getHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String feature) {
        Long userId = getCurrentUserId();
        int offset = page * size;
        List<AiFeatureLog> logs = logMapper.findLogs(userId, feature, offset, size);
        int total = logMapper.countLogs(userId, feature);
        Map<String, Object> result = new HashMap<>();
        result.put("logs", logs);
        result.put("total", total);
        result.put("page", page);
        result.put("size", size);
        return ApiResponse.success(result);
    }

    @Operation(summary = "获取 AI 使用统计", description = "统计当前用户各 AI 功能的使用次数")
    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> getStats() {
        Long userId = getCurrentUserId();
        Map<String, Object> stats = new HashMap<>();
        String[] features = {"semantic_search", "smart_order", "recipe_assist", "inventory_advisor",
                "inventory_predict", "scene_menu", "data_insight", "copywriting", "smart_schedule",
                "user_profile", "trend_predict", "menu_analysis", "order_analysis", "chat",
                "recommend", "nutrition", "meal_plan", "leftover", "score", "cooking_qa"};
        for (String f : features) {
            int count = logMapper.countLogs(userId, f);
            if (count > 0) stats.put(f, count);
        }
        int totalAll = logMapper.countLogs(userId, null);
        stats.put("total", totalAll);
        return ApiResponse.success(stats);
    }

    @Operation(summary = "删除 AI 历史记录", description = "删除指定的 AI 功能使用记录")
    @DeleteMapping("/history/{id}")
    public ApiResponse<String> deleteHistory(@PathVariable Long id) {
        logMapper.deleteById(id);
        return ApiResponse.success("已删除");
    }
}
