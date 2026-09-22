package com.shiyu.controller;

import com.shiyu.service.RecipeExportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/export")
@Tag(name = "数据导出", description = "菜谱、订单 CSV 导出")
public class ExportController {

    @Autowired
    private RecipeExportService recipeExportService;

    @Operation(summary = "导出菜谱数据", description = "导出菜谱为CSV格式，支持日期范围筛选")
    @PostMapping("/recipes")
    public ResponseEntity<byte[]> exportRecipes(
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        byte[] data = recipeExportService.exportRecipes(format, startDate, endDate);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "recipes." + format);
        return ResponseEntity.ok().headers(headers).body(data);
    }

    @Operation(summary = "导出订单数据", description = "导出订单为CSV格式，支持日期范围筛选")
    @PostMapping("/orders")
    public ResponseEntity<byte[]> exportOrders(
            @RequestParam(defaultValue = "csv") String format,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        byte[] data = recipeExportService.exportOrders(format, startDate, endDate);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "orders." + format);
        return ResponseEntity.ok().headers(headers).body(data);
    }
}
