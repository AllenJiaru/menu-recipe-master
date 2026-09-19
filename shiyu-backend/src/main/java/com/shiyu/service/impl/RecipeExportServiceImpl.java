package com.shiyu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shiyu.entity.OrderRecord;
import com.shiyu.entity.Recipe;
import com.shiyu.mapper.OrderRecordMapper;
import com.shiyu.mapper.RecipeMapper;
import com.shiyu.service.RecipeExportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class RecipeExportServiceImpl implements RecipeExportService {

    @Autowired
    private RecipeMapper recipeMapper;

    @Autowired
    private OrderRecordMapper orderRecordMapper;

    @Override
    public byte[] exportRecipes(String format, String startDate, String endDate) {
        LambdaQueryWrapper<Recipe> wrapper = new LambdaQueryWrapper<>();
        if (startDate != null && !startDate.isEmpty()) {
            LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00");
            wrapper.ge(Recipe::getCreateTime, start);
        }
        if (endDate != null && !endDate.isEmpty()) {
            LocalDateTime end = LocalDateTime.parse(endDate + "T23:59:59");
            wrapper.le(Recipe::getCreateTime, end);
        }
        wrapper.orderByDesc(Recipe::getCreateTime);
        List<Recipe> recipes = recipeMapper.selectList(wrapper);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8))) {
            // BOM for Excel UTF-8 compatibility
            baos.write(0xEF);
            baos.write(0xBB);
            baos.write(0xBF);

            writer.println("ID,Name,Category,Type,Description,CookingTime,Difficulty,IsFavorite,OrderCount,Status,CreateTime");
            for (Recipe recipe : recipes) {
                String name = escapeCsv(recipe.getName());
                String description = escapeCsv(recipe.getDescription());
                String createTime = recipe.getCreateTime() != null ? recipe.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "";
                writer.println(String.join(",",
                        String.valueOf(recipe.getId()),
                        name,
                        recipe.getCategoryId() != null ? String.valueOf(recipe.getCategoryId()) : "",
                        recipe.getType() != null ? String.valueOf(recipe.getType()) : "",
                        description,
                        recipe.getCookingTime() != null ? String.valueOf(recipe.getCookingTime()) : "",
                        recipe.getDifficulty() != null ? String.valueOf(recipe.getDifficulty()) : "",
                        recipe.getIsFavorite() != null ? String.valueOf(recipe.getIsFavorite()) : "0",
                        recipe.getOrderCount() != null ? String.valueOf(recipe.getOrderCount()) : "0",
                        recipe.getStatus() != null ? String.valueOf(recipe.getStatus()) : "",
                        createTime
                ));
            }
            writer.flush();
        } catch (Exception e) {
            throw new RuntimeException("Failed to export recipes", e);
        }
        return baos.toByteArray();
    }

    @Override
    public byte[] exportOrders(String format, String startDate, String endDate) {
        LambdaQueryWrapper<OrderRecord> wrapper = new LambdaQueryWrapper<>();
        if (startDate != null && !startDate.isEmpty()) {
            LocalDateTime start = LocalDateTime.parse(startDate + "T00:00:00");
            wrapper.ge(OrderRecord::getCreateTime, start);
        }
        if (endDate != null && !endDate.isEmpty()) {
            LocalDateTime end = LocalDateTime.parse(endDate + "T23:59:59");
            wrapper.le(OrderRecord::getCreateTime, end);
        }
        wrapper.orderByDesc(OrderRecord::getCreateTime);
        List<OrderRecord> orders = orderRecordMapper.selectList(wrapper);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8))) {
            baos.write(0xEF);
            baos.write(0xBB);
            baos.write(0xBF);

            writer.println("ID,CoupleID,RecipeID,RecipeName,Status,Remark,RejectReason,OrderTime,AcceptTime,CompleteTime,CreateTime");
            for (OrderRecord order : orders) {
                String recipeName = escapeCsv(order.getRecipeName());
                String remark = escapeCsv(order.getRemark());
                String rejectReason = escapeCsv(order.getRejectReason());
                String orderTime = order.getOrderTime() != null ? order.getOrderTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "";
                String acceptTime = order.getAcceptTime() != null ? order.getAcceptTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "";
                String completeTime = order.getCompleteTime() != null ? order.getCompleteTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "";
                String createTime = order.getCreateTime() != null ? order.getCreateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "";
                writer.println(String.join(",",
                        String.valueOf(order.getId()),
                        order.getCoupleId() != null ? String.valueOf(order.getCoupleId()) : "",
                        order.getRecipeId() != null ? String.valueOf(order.getRecipeId()) : "",
                        recipeName,
                        order.getStatus() != null ? String.valueOf(order.getStatus()) : "",
                        remark,
                        rejectReason,
                        orderTime,
                        acceptTime,
                        completeTime,
                        createTime
                ));
            }
            writer.flush();
        } catch (Exception e) {
            throw new RuntimeException("Failed to export orders", e);
        }
        return baos.toByteArray();
    }

    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
