package com.shiyu.controller;

import com.shiyu.entity.Recipe;
import com.shiyu.entity.RecipeMaterial;
import com.shiyu.entity.RecipeStep;
import com.shiyu.service.RecipeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/share")
public class ShareController {

    @Autowired
    private RecipeService recipeService;

    @GetMapping(value = "/{id}", produces = MediaType.TEXT_HTML_VALUE)
    public String shareRecipe(@PathVariable Long id) {
        Recipe recipe = recipeService.getRecipeById(id);
        if (recipe == null) {
            return errorPage("菜谱不存在或已被删除");
        }

        String categoryName = getCategoryName(recipe.getType());
        String coverUrl = recipe.getCoverImage() != null && !recipe.getCoverImage().isEmpty()
                ? recipe.getCoverImage() : "";
        String desc = recipe.getDescription() != null ? recipe.getDescription() : "暂无简介";

        List<RecipeMaterial> materials = recipe.getMaterials();
        List<RecipeStep> steps = recipe.getSteps();

        StringBuilder materialsHtml = new StringBuilder();
        if (materials != null && !materials.isEmpty()) {
            for (RecipeMaterial m : materials) {
                String amount = (m.getAmount() != null && !m.getAmount().isEmpty())
                        ? m.getAmount() + (m.getUnit() != null ? m.getUnit() : "") : "";
                materialsHtml.append("<div class=\"material-item\">")
                        .append("<span class=\"dot\"></span>")
                        .append("<span class=\"mat-name\">").append(esc(m.getName())).append("</span>")
                        .append("<span class=\"mat-amount\">").append(esc(amount)).append("</span>")
                        .append("</div>");
            }
        } else {
            materialsHtml.append("<div class=\"empty-hint\">暂无食材信息</div>");
        }

        StringBuilder stepsHtml = new StringBuilder();
        if (steps != null && !steps.isEmpty()) {
            steps.stream().sorted((a, b) -> Integer.compare(a.getStepNumber(), b.getStepNumber()))
                .forEach(s -> {
                    stepsHtml.append("<div class=\"step-item\">")
                            .append("<div class=\"step-num\">").append(s.getStepNumber()).append("</div>")
                            .append("<div class=\"step-content\">").append(esc(s.getDescription())).append("</div>")
                            .append("</div>");
                });
        } else {
            stepsHtml.append("<div class=\"empty-hint\">暂无烹饪步骤</div>");
        }

        return "<!DOCTYPE html><html lang=\"zh-CN\"><head><meta charset=\"UTF-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1,maximum-scale=1,user-scalable=no\">"
                + "<title>" + esc(recipe.getName()) + " - 食遇</title>"
                + "<style>"
                + "*{margin:0;padding:0;box-sizing:border-box;}"
                + "body{font-family:-apple-system,BlinkMacSystemFont,'Segoe UI',Roboto,sans-serif;"
                + "background:#f5f5f5;color:#333;-webkit-font-smoothing:antialiased;}"
                + ".header{background:linear-gradient(135deg,#FF6B6B,#FF8E8E);padding:20px 16px 16px;color:#fff;text-align:center;}"
                + ".header h1{font-size:22px;font-weight:700;margin-bottom:6px;}"
                + ".header .tag{display:inline-block;background:rgba(255,255,255,0.25);padding:3px 12px;border-radius:20px;font-size:12px;}"
                + ".cover{width:100%;max-height:300px;object-fit:cover;}"
                + ".card{background:#fff;margin:12px;border-radius:14px;padding:16px;box-shadow:0 1px 4px rgba(0,0,0,0.06);}"
                + ".card-title{font-size:16px;font-weight:700;margin-bottom:12px;display:flex;align-items:center;gap:8px;}"
                + ".card-title::before{content:'';width:4px;height:16px;background:#FF6B6B;border-radius:2px;}"
                + ".info-row{display:flex;justify-content:space-around;padding:12px 0;}"
                + ".info-item{text-align:center;}"
                + ".info-item .val{font-size:16px;font-weight:700;color:#FF6B6B;}"
                + ".info-item .lbl{font-size:12px;color:#999;margin-top:2px;}"
                + ".material-item{display:flex;align-items:center;padding:10px 0;border-bottom:1px solid #f0f0f0;}"
                + ".material-item:last-child{border-bottom:none;}"
                + ".dot{width:6px;height:6px;background:#FF6B6B;border-radius:50%;margin-right:10px;flex-shrink:0;}"
                + ".mat-name{flex:1;font-size:14px;}"
                + ".mat-amount{font-size:14px;color:#666;font-weight:500;}"
                + ".step-item{display:flex;padding:8px 0;gap:10px;}"
                + ".step-num{width:28px;height:28px;background:#FF6B6B;color:#fff;border-radius:50%;"
                + "display:flex;align-items:center;justify-content:center;font-size:13px;font-weight:700;flex-shrink:0;}"
                + ".step-content{font-size:14px;line-height:1.6;color:#444;flex:1;padding-top:3px;}"
                + ".empty-hint{color:#bbb;font-size:13px;padding:12px 0;text-align:center;}"
                + ".desc{font-size:14px;line-height:1.8;color:#555;}"
                + ".footer{text-align:center;padding:20px 16px;color:#ccc;font-size:12px;}"
                + ".brand{color:#FF6B6B;font-weight:600;}"
                + "</style></head><body>"

                // Header
                + "<div class=\"header\">"
                + "<h1>" + esc(recipe.getName()) + "</h1>"
                + "<span class=\"tag\">" + esc(categoryName) + "</span>"
                + "</div>"

                // Cover image
                + (coverUrl.isEmpty() ? "" : "<img class=\"cover\" src=\"" + esc(coverUrl) + "\" alt=\"\">")

                // Info card
                + "<div class=\"card\"><div class=\"info-row\">"
                + "<div class=\"info-item\"><div class=\"val\">" + recipe.getCookingTime() + "分钟</div><div class=\"lbl\">烹饪时间</div></div>"
                + "<div class=\"info-item\"><div class=\"val\">难度" + recipe.getDifficulty() + "</div><div class=\"lbl\">难度等级</div></div>"
                + "<div class=\"info-item\"><div class=\"val\">" + (materials != null ? materials.size() : 0) + "种</div><div class=\"lbl\">食材</div></div>"
                + "</div></div>"

                // Description
                + "<div class=\"card\"><div class=\"card-title\">简介</div>"
                + "<div class=\"desc\">" + esc(desc) + "</div></div>"

                // Materials
                + "<div class=\"card\"><div class=\"card-title\">食材清单</div>"
                + materialsHtml.toString() + "</div>"

                // Steps
                + "<div class=\"card\"><div class=\"card-title\">烹饪步骤</div>"
                + stepsHtml.toString() + "</div>"

                // Footer
                + "<div class=\"footer\">来自 <span class=\"brand\">食遇</span> · 情侣菜谱点餐</div>"

                + "</body></html>";
    }

    private String getCategoryName(Integer type) {
        if (type == null) return "其他";
        switch (type) {
            case 1: return "荤菜";
            case 2: return "蔬菜";
            case 3: return "汤类";
            case 4: return "甜点";
            case 5: return "蒸菜";
            case 6: return "炖菜";
            case 7: return "凉菜";
            case 8: return "炒菜";
            case 9: return "红烧";
            default: return "其他";
        }
    }

    private String esc(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }

    private String errorPage(String msg) {
        return "<!DOCTYPE html><html><head><meta charset=\"UTF-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">"
                + "<title>错误 - 食遇</title></head><body style=\"display:flex;justify-content:center;align-items:center;height:100vh;"
                + "font-family:sans-serif;color:#999;text-align:center;\">"
                + "<div><div style=\"font-size:48px;margin-bottom:16px;\">😢</div><div>" + esc(msg) + "</div></div>"
                + "</body></html>";
    }
}
