package com.shiyu.service.impl;

import com.shiyu.config.AiConfig;
import com.shiyu.dto.request.*;
import com.shiyu.dto.response.AiResponse;
import com.shiyu.entity.AiChatSession;
import com.shiyu.entity.AiChatMessage;
import com.shiyu.mapper.AiChatSessionMapper;
import com.shiyu.mapper.AiChatMessageMapper;
import com.shiyu.service.AiService;
import com.shiyu.service.ai.AiClient;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AiServiceImpl implements AiService {
    private final AiClient aiClient;
    private final AiConfig aiConfig;
    private final AiChatSessionMapper sessionMapper;
    private final AiChatMessageMapper messageMapper;

    public AiServiceImpl(AiClient aiClient, AiConfig aiConfig, AiChatSessionMapper sessionMapper, AiChatMessageMapper messageMapper) {
        this.aiClient = aiClient;
        this.aiConfig = aiConfig;
        this.sessionMapper = sessionMapper;
        this.messageMapper = messageMapper;
    }

    private String getTimeContext() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int month = cal.get(Calendar.MONTH) + 1;
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        String[] weekDays = {"", "周日", "周一", "周二", "周三", "周四", "周五", "周六"};

        String mealTime;
        String mealSuggestion;
        if (hour < 6) { mealTime = "凌晨"; mealSuggestion = "夜宵或早餐准备"; }
        else if (hour < 9) { mealTime = "早餐时段"; mealSuggestion = "营养早餐，开启元气一天"; }
        else if (hour < 11) { mealTime = "上午"; mealSuggestion = "上午加餐或午餐准备"; }
        else if (hour < 14) { mealTime = "午餐时段"; mealSuggestion = "丰盛午餐，补充能量"; }
        else if (hour < 17) { mealTime = "下午茶时段"; mealSuggestion = "下午茶点心，提神醒脑"; }
        else if (hour < 19) { mealTime = "晚餐时段"; mealSuggestion = "温馨晚餐，家人共享"; }
        else if (hour < 22) { mealTime = "晚间"; mealSuggestion = "宵夜或明日菜单规划"; }
        else { mealTime = "深夜"; mealSuggestion = "轻食宵夜，避免过饱"; }

        String season;
        String[] seasonalFoods;
        if (month >= 3 && month <= 5) { season = "春季"; seasonalFoods = new String[]{"春笋", "香椿", "荠菜", "蚕豆", "草莓", "樱桃"}; }
        else if (month >= 6 && month <= 8) { season = "夏季"; seasonalFoods = new String[]{"西瓜", "黄瓜", "苦瓜", "绿豆", "茄子", "番茄"}; }
        else if (month >= 9 && month <= 11) { season = "秋季"; seasonalFoods = new String[]{"螃蟹", "莲藕", "山药", "板栗", "银杏", "柿子"}; }
        else { season = "冬季"; seasonalFoods = new String[]{"白菜", "萝卜", "羊肉", "牛肉", "红薯", "核桃"}; }

        return String.format("当前时间：%s %s（%s），%s。季节：%s。当季食材：%s。",
                mealTime, weekDays[dayOfWeek], String.format("%d月%d日", month, cal.get(Calendar.DAY_OF_MONTH)),
                mealSuggestion, season, String.join("、", seasonalFoods));
    }

    private String getSystemPrompt(String role) {
        return switch (role) {
            case "chat" -> """
                你是「食遇」App的专业AI美食助手，名叫「小食」。你拥有以下专业能力：

                ## 你的身份
                - 一位拥有10年经验的中西餐大厨
                - 精通营养学、食材搭配、烹饪科学
                - 熟悉中国八大菜系（川、鲁、粤、苏、浙、闽、湘、徽）及世界各地美食
                - 擅长根据时令、体质、场景推荐菜品

                ## 回答原则
                1. **实用性优先**：推荐的菜谱必须是家庭可操作的，步骤清晰具体
                2. **营养均衡**：每餐建议包含蛋白质、碳水、蔬菜、适量脂肪
                3. **因时制宜**：根据当前季节、时间段推荐应景菜品
                4. **因人而异**：关注用户提到的口味偏好、忌口、健康需求
                5. **专业但亲切**：用通俗易懂的语言解释专业烹饪知识

                ## 回答格式
                - 使用Markdown格式排版，包括标题、列表、表格、加粗等
                - 菜谱推荐包含：菜名、食材清单（含用量）、详细步骤、烹饪时间、难度、小贴士
                - 营养信息用表格展示
                - 重要提示用加粗或引用块强调

                ## 特殊能力
                - 可以根据冰箱剩余食材推荐菜品
                - 可以分析菜品营养价值
                - 可以制定一周膳食计划
                - 可以解答烹饪技巧和食材知识
                - 可以翻译菜谱到多种语言

                请始终以专业、热情、有帮助的态度回答。如果不确定，诚实说明并提供可能的建议。
                """;
            case "recommend" -> """
                你是「食遇」的智能菜品推荐引擎。你的任务是根据以下因素精准推荐菜品：

                ## 推荐维度
                1. **时间维度**：当前时段（早餐/午餐/晚餐/下午茶）、星期几（工作日/周末）
                2. **季节维度**：当季食材、时令菜品、节气饮食
                3. **场景维度**：家庭聚餐、一人食、快手菜、宴客菜、便当菜
                4. **营养维度**：低脂、高蛋白、补铁补钙、控制热量
                5. **难度维度**：新手友好、进阶挑战、大厨级别

                ## 输出格式
                每道菜用Markdown格式：
                ### 🍽️ 菜名
                **简介**：一句话描述
                | 项目 | 详情 |
                |------|------|
                | 食材 | 主要食材列表 |
                | 时间 | 烹饪时间 |
                | 难度 | ⭐/⭐⭐/⭐⭐⭐ |
                | 热量 | 约XXXkcal |

                **做法要点**：简要步骤

                推荐3-5道菜，风格各异，满足不同需求。
                """;
            case "meal_plan" -> """
                你是「食遇」的专业膳食规划师。你擅长制定科学、美味、可执行的膳食计划。

                ## 规划原则
                1. **营养均衡**：每日蛋白质、碳水、脂肪比例合理（约3:5:2）
                2. **食材多样**：一周内涵盖至少20种不同食材
                3. **色彩丰富**：红黄绿白黑五色搭配
                4. **烹饪多样**：蒸、炒、煮、炖、烤交替
                5. **预算可控**：在用户预算内实现最优营养

                ## 输出格式
                用Markdown表格展示每日菜单：

                ### 📅 第X天（周X）
                | 餐次 | 菜品 | 主要食材 | 烹饪时间 |
                |------|------|----------|----------|
                | 早餐 | XXX | XXX | XX分钟 |
                | 午餐 | XXX | XXX | XX分钟 |
                | 晚餐 | XXX | XXX | XX分钟 |

                **当日营养小结**：简要说明营养亮点
                **采购清单**：当日所需食材汇总

                最后给出一周采购汇总和营养总结。
                """;
            case "nutrition" -> """
                你是「食遇」的注册营养师。你拥有深厚的营养学知识，能精确分析菜品的营养价值。

                ## 分析维度
                1. **宏量营养素**：热量(kcal)、蛋白质(g)、脂肪(g)、碳水化合物(g)
                2. **微量营养素**：膳食纤维、维生素、矿物质
                3. **营养密度**：单位热量提供的营养素含量
                4. **GI值**：血糖生成指数估算
                5. **过敏原**：常见过敏原标注

                ## 输出格式
                ### 🥗 营养分析报告

                **基础信息**
                | 营养素 | 含量 | 占每日推荐% |
                |--------|------|------------|
                | 热量 | XXX kcal | XX% |
                | 蛋白质 | XX g | XX% |
                | ... | ... | ... |

                **营养亮点**：🌟 优点
                **注意事项**：⚠️ 需注意
                **搭配建议**：🍽️ 如何搭配更健康
                **适合人群**：👨‍👩‍👧‍👦 推荐食用
                **不适合人群**：🚫 慎食
                """;
            case "leftover" -> """
                你是「食遇」的剩菜创意大师。你擅长将剩余食材变废为宝，创造美味新菜。

                ## 创意原则
                1. **零浪费**：充分利用每一种剩余食材
                2. **口味升级**：让剩菜焕发新生，不单调重复
                3. **营养互补**：通过搭配提升整体营养价值
                4. **快手操作**：15分钟内完成，简单高效
                5. **创意惊喜**：让家人眼前一亮的新吃法

                ## 输出格式
                对每种剩余食材，给出2-3种创意方案：

                ### 🎨 方案X：菜名
                **创意点**：为什么这样搭配
                **额外食材**：需要添加什么（尽量少）
                **步骤**：1-2-3 清晰步骤
                **时间**：XX分钟
                **适合**：搭配什么主食

                最后给出「剩余食材利用总结表」。
                """;
            case "cooking_qa" -> """
                你是「食遇」的烹饪百科全书。你拥有丰富的烹饪知识和实践经验。

                ## 知识领域
                1. **烹饪技巧**：刀工、火候、调味、腌制、发面、勾芡
                2. **食材知识**：挑选、保存、处理、搭配禁忌
                3. **厨房科学**：美拉德反应、蛋白质变性、淀粉糊化
                4. **食品安全**：食材保鲜、交叉污染、中心温度
                5. **厨具使用**：锅具选择、烤箱使用、小家电妙用
                6. **地方美食**：各地特色、老字号、街头小吃

                ## 回答原则
                - 先直接回答问题，再扩展相关知识
                - 用通俗语言解释科学原理
                - 给出实用的操作建议
                - 涉及安全问题时特别提醒
                - 适当分享小窍门和秘诀
                """;
            default -> """
                你是「食遇」App的AI美食助手。你精通中西餐烹饪、营养学、食材搭配。
                请用专业、热情、有帮助的态度回答用户关于美食、菜谱、烹饪的问题。
                回答要简洁实用，使用Markdown格式排版，让用户阅读更舒适。
                """;
        };
    }

    @Override
    public AiResponse chat(AiRequest request) {
        long start = System.currentTimeMillis();
        String timeContext = getTimeContext();
        String systemPrompt = getSystemPrompt("chat") + "\n\n## 当前环境信息\n" + timeContext;
        String context = request.getContext() != null ? "\n\n## 用户偏好\n" + request.getContext() : "";
        String result = aiClient.chat(systemPrompt, request.getMessage() + context);
        return new AiResponse(result, aiConfig.getProvider(), System.currentTimeMillis() - start);
    }

    @Override
    public AiResponse smartRecommend(String userId) {
        long start = System.currentTimeMillis();
        String timeContext = getTimeContext();
        String systemPrompt = getSystemPrompt("recommend") + "\n\n## 当前环境\n" + timeContext;
        String userMsg = "请根据当前时间和季节，推荐适合的菜品。考虑当季食材和时令特色，推荐3-5道既营养又美味的家常菜。";
        String result = aiClient.chat(systemPrompt, userMsg);
        return new AiResponse(result, aiConfig.getProvider(), System.currentTimeMillis() - start);
    }

    @Override
    public AiResponse ingredientToRecipe(AiRecipeRequest request) {
        long start = System.currentTimeMillis();
        String systemPrompt = getSystemPrompt("chat") + """
            \n\n## 特殊任务：食材变菜谱
            用户提供了手头的食材，请根据这些食材推荐2-3道可以制作的菜品。
            要求：
            1. 优先使用用户提供的食材，减少额外采购
            2. 给出完整的食材清单和用量
            3. 步骤要详细到新手也能操作
            4. 考虑食材的搭配禁忌和营养互补
            """;
        StringBuilder userMsg = new StringBuilder("我手头有以下食材：\n");
        if (request.getIngredients() != null && !request.getIngredients().isEmpty()) {
            userMsg.append("**").append(String.join("、", request.getIngredients())).append("**\n");
        }
        if (request.getTaste() != null) userMsg.append("口味偏好：").append(request.getTaste()).append("\n");
        if (request.getCuisine() != null) userMsg.append("菜系：").append(request.getCuisine()).append("\n");
        if (request.getServings() != null) userMsg.append("用餐人数：").append(request.getServings()).append("人\n");
        if (request.getMaxTime() != null) userMsg.append("最长烹饪时间：").append(request.getMaxTime()).append("分钟\n");
        if (request.getDifficulty() != null) userMsg.append("难度要求：").append(request.getDifficulty()).append("\n");
        userMsg.append("\n请推荐最适合的菜品，告诉我每道菜需要哪些食材（含用量）、详细做法、烹饪时间、难度和营养亮点。");
        String result = aiClient.chat(systemPrompt, userMsg.toString());
        return new AiResponse(result, aiConfig.getProvider(), System.currentTimeMillis() - start);
    }

    @Override
    public AiResponse analyzeNutrition(AiNutritionRequest request) {
        long start = System.currentTimeMillis();
        String systemPrompt = getSystemPrompt("nutrition");
        StringBuilder userMsg = new StringBuilder("请对以下菜品进行全面的营养分析：\n\n");
        userMsg.append("**菜名**：").append(request.getRecipeName()).append("\n");
        if (request.getIngredients() != null && !request.getIngredients().isEmpty()) {
            userMsg.append("**食材**：").append(String.join("、", request.getIngredients())).append("\n");
        }
        if (request.getServings() != null) {
            userMsg.append("**份量**：").append(request.getServings()).append("人份\n");
        }
        userMsg.append("\n请给出详细的营养分析报告，包括热量、三大营养素、维生素矿物质、营养评分、健康建议和搭配推荐。");
        String result = aiClient.chat(systemPrompt, userMsg.toString());
        return new AiResponse(result, aiConfig.getProvider(), System.currentTimeMillis() - start);
    }

    @Override
    public AiResponse generateMealPlan(AiMealPlanRequest request) {
        long start = System.currentTimeMillis();
        String timeContext = getTimeContext();
        String systemPrompt = getSystemPrompt("meal_plan") + "\n\n## 当前环境\n" + timeContext;
        int days = request.getDays() != null ? request.getDays() : 7;
        StringBuilder userMsg = new StringBuilder();
        userMsg.append("请制定").append(days).append("天的详细膳食计划。\n\n");

        if (request.getBudget() != null) userMsg.append("**预算**：每周").append(request.getBudget()).append("元\n");
        if (request.getPreferences() != null && !request.getPreferences().isEmpty()) {
            userMsg.append("**口味偏好**：").append(String.join("、", request.getPreferences())).append("\n");
        }
        if (request.getAllergies() != null && !request.getAllergies().isEmpty()) {
            userMsg.append("**忌口/过敏**：").append(String.join("、", request.getAllergies())).append("（务必避开！）\n");
        }
        if (request.getStyle() != null) userMsg.append("**饮食风格**：").append(request.getStyle()).append("\n");
        userMsg.append("\n要求：\n1. 每日三餐+加餐，营养均衡\n2. 食材多样化，一周内不重复\n3. 考虑烹饪时间，工作日快手菜，周末可复杂些\n4. 给出每周采购清单\n5. 标注每日营养小结");

        String result = aiClient.chat(systemPrompt, userMsg.toString());
        return new AiResponse(result, aiConfig.getProvider(), System.currentTimeMillis() - start);
    }

    @Override
    public AiResponse recognizeDish(String base64Image) {
        long start = System.currentTimeMillis();
        String systemPrompt = "你是「食遇」的菜品识别专家。根据用户描述的菜品特征，识别可能的菜品。\n\n" +
                "请从以下角度分析：\n" +
                "1. 菜品名称和所属菜系\n" +
                "2. 主要食材和调味料\n" +
                "3. 烹饪方法（炒、煮、蒸、烤等）\n" +
                "4. 口味特点（酸甜苦辣咸鲜）\n" +
                "5. 营养价值概览\n" +
                "6. 家庭复刻建议\n\n" +
                "如果无法确定，给出最可能的2-3种猜测，并说明判断依据。";
        String result = aiClient.chat(systemPrompt, "请识别并分析这道菜品：" + base64Image.substring(0, Math.min(base64Image.length(), 100)) + "...");
        return new AiResponse(result, aiConfig.getProvider(), System.currentTimeMillis() - start);
    }

    @Override
    public AiResponse generateShoppingList(String mealPlanContext) {
        long start = System.currentTimeMillis();
        String systemPrompt = "你是「食遇」的智能采购助手。根据菜谱计划，生成精确的购物清单。\n\n" +
                "## 生成原则\n" +
                "1. **精确计量**：所有食材给出具体克数/个数\n" +
                "2. **合并同类**：相同食材合并计算总量\n" +
                "3. **分类整理**：按蔬菜、肉类、调味品、主食等分类\n" +
                "4. **价格估算**：给出当地市场参考价\n" +
                "5. **采购建议**：标注哪些可以一次多买、哪些需要新鲜采购\n\n" +
                "## 输出格式\n" +
                "用Markdown表格展示：\n\n" +
                "### 🛒 购物清单\n\n" +
                "| 分类 | 食材 | 用量 | 预估价格 | 采购建议 |\n" +
                "|------|------|------|----------|----------|\n" +
                "| 🥬 蔬菜 | ... | ... | ... | ... |\n" +
                "| 🥩 肉类 | ... | ... | ... | ... |\n\n" +
                "**总价预估**：约 XX-XX 元\n" +
                "**省钱小贴士**：如何更经济地采购";
        String result = aiClient.chat(systemPrompt, "根据以下菜谱计划生成购物清单：\n" + mealPlanContext);
        return new AiResponse(result, aiConfig.getProvider(), System.currentTimeMillis() - start);
    }

    @Override
    public AiResponse leftoverSuggestion(String leftovers) {
        long start = System.currentTimeMillis();
        String systemPrompt = getSystemPrompt("leftover");
        String userMsg = "我手头有这些剩余食材/菜品需要处理：\n\n" + leftovers +
                "\n\n请发挥创意，推荐2-3种让这些食材焕发新生的做法。要求操作简单、时间短、味道好，最好能让家人以为是新菜！";
        String result = aiClient.chat(systemPrompt, userMsg);
        return new AiResponse(result, aiConfig.getProvider(), System.currentTimeMillis() - start);
    }

    @Override
    public AiResponse scoreRecipe(String recipeContext) {
        long start = System.currentTimeMillis();
        String systemPrompt = "你是「食遇」的专业美食评审，拥有米其林评委级别的品鉴能力。\n\n" +
                "## 评分维度（每项1-10分）\n" +
                "1. **口味** (taste)：酸甜苦辣咸鲜的平衡，层次感，回味\n" +
                "2. **营养价值** (nutrition)：营养均衡度，食材搭配科学性\n" +
                "3. **制作难度** (difficulty)：步骤复杂度，所需技巧，容错率\n" +
                "4. **性价比** (cost)：食材成本，时间成本，产出比\n" +
                "5. **外观** (presentation)：摆盘美感，色彩搭配，食欲感\n" +
                "6. **创新度** (creativity)：传统与创新的结合，独特性\n\n" +
                "## 输出格式\n" +
                "### 🏆 评审报告\n\n" +
                "**综合评分**：X.X / 10\n\n" +
                "| 维度 | 评分 | 点评 |\n" +
                "|------|------|------|\n" +
                "| 口味 | ⭐ X/10 | ... |\n" +
                "| ... | ... | ... |\n\n" +
                "**亮点**：🌟 ...\n" +
                "**改进建议**：💡 ...\n" +
                "**适合人群**：👨‍👩‍👧‍👦 ...\n" +
                "**推荐搭配**：🍽️ ...";

        String result = aiClient.chat(systemPrompt, "请以专业评审的角度评价以下菜品：\n\n" + recipeContext);
        return new AiResponse(result, aiConfig.getProvider(), System.currentTimeMillis() - start);
    }

    @Override
    public AiResponse cookingQA(String question) {
        long start = System.currentTimeMillis();
        String systemPrompt = getSystemPrompt("cooking_qa");
        String result = aiClient.chat(systemPrompt, question);
        return new AiResponse(result, aiConfig.getProvider(), System.currentTimeMillis() - start);
    }

    @Override
    public AiResponse healthReport(String userId) {
        long start = System.currentTimeMillis();
        String timeContext = getTimeContext();
        String systemPrompt = "你是「食遇」的健康管理师，擅长根据饮食数据生成个性化健康报告。\n\n" +
                "## 报告框架\n" +
                "### 1. 📊 整体评分\n" +
                "用雷达图维度：营养均衡、多样性、规律性、健康度、满意度\n\n" +
                "### 2. 🍽️ 饮食结构分析\n" +
                "- 三大营养素比例是否合理\n" +
                "- 蔬果摄入是否充足\n" +
                "- 蛋白质来源是否多样\n" +
                "- 油盐糖摄入是否超标\n\n" +
                "### 3. 💪 健康评估\n" +
                "- 当前饮食对健康的影响\n" +
                "- 潜在风险提示\n" +
                "- 改善建议\n\n" +
                "### 4. 🎯 下周目标\n" +
                "- 具体可执行的改善计划\n" +
                "- 推荐菜品方向\n" +
                "- 注意事项\n\n" +
                "用数据说话，给出具体的克数、比例、建议量。";
        String userMsg = "请为用户" + userId + "生成本月饮食健康报告。\n\n" + timeContext +
                "\n\n由于目前缺少详细饮食记录数据，请基于通用健康饮食原则，给出：\n" +
                "1. 理想饮食结构建议\n" +
                "2. 各类食材推荐摄入量\n" +
                "3. 常见饮食误区提醒\n" +
                "4. 个性化改善方案\n" +
                "5. 本周推荐菜品方向";
        String result = aiClient.chat(systemPrompt, userMsg);
        return new AiResponse(result, aiConfig.getProvider(), System.currentTimeMillis() - start);
    }

    @Override
    public AiResponse translateRecipe(AiTranslationRequest request) {
        long start = System.currentTimeMillis();
        Map<String, String> langMap = new LinkedHashMap<>();
        langMap.put("en", "English");
        langMap.put("ja", "Japanese (日本語)");
        langMap.put("ko", "Korean (한국어)");
        langMap.put("fr", "French (Français)");
        langMap.put("de", "German (Deutsch)");
        langMap.put("es", "Spanish (Español)");
        langMap.put("pt", "Portuguese (Português)");
        langMap.put("ru", "Russian (Русский)");
        langMap.put("it", "Italian (Italiano)");
        langMap.put("th", "Thai (ไทย)");

        String targetLang = langMap.getOrDefault(request.getTargetLang() != null ? request.getTargetLang().toLowerCase() : "en", "English");
        String systemPrompt = "你是「食遇」的专业菜谱翻译专家，精通中英日韩法德意西等多种语言。\n\n" +
                "## 翻译原则\n" +
                "1. **准确性**：菜名、食材名、烹饪术语翻译准确\n" +
                "2. **本地化**：中国特色食材保留中文名+英文解释\n" +
                "3. **格式保持**：Markdown排版、表格、列表格式不变\n" +
                "4. **度量转换**：适当转换计量单位（如两→g，碗→cups）\n" +
                "5. **文化适配**：解释中国文化特有的烹饪概念\n\n" +
                "## 特殊处理\n" +
                "- 「翻炒」→ stir-fry\n" +
                "- 「焯水」→ blanch\n" +
                "- 「勾芡」→ thicken with cornstarch\n" +
                "- 「八角」→ star anise\n" +
                "- 「料酒」→ Chinese cooking wine\n" +
                "- 「生抽/老抽」→ light soy sauce / dark soy sauce";

        String userMsg = String.format("请将以下菜谱翻译成%s，保持专业准确：\n\n%s", targetLang, request.getText());
        String result = aiClient.chat(systemPrompt, userMsg);
        return new AiResponse(result, aiConfig.getProvider(), System.currentTimeMillis() - start);
    }

    @Override
    public Map<String, Object> getProviderInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("currentProvider", aiConfig.getProvider());
        Map<String, Boolean> providers = new HashMap<>();
        if (aiConfig.getProviders() != null) {
            aiConfig.getProviders().forEach((id, p) ->
                providers.put(id, p.getApiKey() != null && !p.getApiKey().isBlank())
            );
        }
        info.put("providers", providers);
        return info;
    }

    @Override
    public List<AiChatSession> listSessions(Long userId) {
        return sessionMapper.findByUserId(userId);
    }

    @Override
    public AiChatSession createSession(String title, Long userId) {
        AiChatSession session = new AiChatSession();
        session.setUserId(userId);
        session.setTitle(title != null && !title.isBlank() ? title : "新对话");
        session.setProvider(aiConfig.getProvider());
        AiConfig.Provider p = aiConfig.getProviders().get(aiConfig.getProvider());
        if (p != null) session.setModel(p.getModel());
        sessionMapper.insert(session);
        return session;
    }

    @Override
    public void deleteSession(Long sessionId, Long userId) {
        AiChatSession session = sessionMapper.selectById(sessionId);
        if (session != null && userId != null && !userId.equals(session.getUserId())) {
            return;
        }
        sessionMapper.deleteById(sessionId);
    }

    @Override
    public List<AiChatMessage> listMessages(Long sessionId, Long userId) {
        AiChatSession session = sessionMapper.selectById(sessionId);
        if (session != null && userId != null && !userId.equals(session.getUserId())) {
            return java.util.Collections.emptyList();
        }
        return messageMapper.findBySessionId(sessionId);
    }

    @Override
    public AiResponse chatWithSession(ChatMessageRequest request, Long userId) {
        long start = System.currentTimeMillis();
        if (request.getSessionId() == null) {
            AiChatSession session = createSession(null, userId);
            request.setSessionId(session.getId());
        }
        AiChatMessage userMsg = new AiChatMessage();
        userMsg.setSessionId(request.getSessionId());
        userMsg.setRole("user");
        userMsg.setContent(request.getMessage());
        messageMapper.insert(userMsg);
        List<AiChatMessage> history = messageMapper.findBySessionId(request.getSessionId());
        String timeContext = getTimeContext();
        String systemPrompt = getSystemPrompt("chat") + "\n\n## 当前环境信息\n" + timeContext;
        List<Map<String, String>> historyMaps = new ArrayList<>();
        for (AiChatMessage m : history) {
            Map<String, String> msg = new HashMap<>();
            msg.put("role", m.getRole());
            msg.put("content", m.getContent());
            historyMaps.add(msg);
        }
        String result;
        try {
            result = aiClient.chat(systemPrompt, historyMaps);
        } catch (Exception e) {
            String errMsg = e.getMessage();
            if (errMsg != null && errMsg.contains("401")) {
                result = "🔐 **认证失败**\n\nAPI Key 无效或已过期，请前往 **AI 设置** 更新密钥后重试。";
            } else if (errMsg != null && errMsg.contains("429")) {
                result = "⏳ **请求过于频繁**\n\nAI 服务繁忙，请稍等片刻后再试。";
            } else if (errMsg != null && (errMsg.contains("timeout") || errMsg.contains("timed out"))) {
                result = "⏱️ **响应超时**\n\nAI 服务响应较慢，请稍后重试或切换到其他模型。";
            } else {
                result = "❌ **服务暂时不可用**\n\n" + (errMsg != null && errMsg.length() < 150 ? errMsg : "请检查网络连接和 AI 配置后重试");
            }
        }
        AiChatMessage assistantMsg = new AiChatMessage();
        assistantMsg.setSessionId(request.getSessionId());
        assistantMsg.setRole("assistant");
        assistantMsg.setContent(result);
        messageMapper.insert(assistantMsg);
        AiChatSession session = sessionMapper.selectById(request.getSessionId());
        if (session != null && "新对话".equals(session.getTitle())) {
            String shortTitle = request.getMessage();
            if (shortTitle.length() > 20) shortTitle = shortTitle.substring(0, 20) + "...";
            session.setTitle(shortTitle);
            sessionMapper.updateById(session);
        }
        return new AiResponse(result, aiConfig.getProvider(), System.currentTimeMillis() - start);
    }
}
