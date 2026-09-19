package com.shiyu.controller;

import com.shiyu.annotation.OperationLog;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shiyu.common.ApiResponse;
import com.shiyu.entity.SystemConfig;
import com.shiyu.mapper.SystemConfigMapper;
import com.shiyu.mapper.OrderRecordMapper;
import com.shiyu.mapper.RecipeMapper;
import com.shiyu.mapper.GalleryImageMapper;
import com.shiyu.mapper.SyncLogMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    @Autowired
    private SystemConfigMapper systemConfigMapper;

    @Autowired
    private OrderRecordMapper orderRecordMapper;

    @Autowired
    private RecipeMapper recipeMapper;

    @Autowired
    private GalleryImageMapper galleryImageMapper;

    @Autowired
    private SyncLogMapper syncLogMapper;

    @OperationLog(action = "QUERY", target = "系统设置")
    @GetMapping
    public ApiResponse<List<SystemConfig>> getSettings() {
        return ApiResponse.success(systemConfigMapper.selectList(null));
    }

    @OperationLog(action = "UPDATE", target = "系统设置")
    @PutMapping("/{key}")
    public ApiResponse<Void> updateSetting(@PathVariable String key, @RequestBody Map<String, String> body) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getConfigKey, key);
        SystemConfig config = systemConfigMapper.selectOne(wrapper);
        if (config != null) {
            config.setConfigValue(body.get("value"));
            systemConfigMapper.updateById(config);
        }
        return ApiResponse.success();
    }

    @OperationLog(action = "DELETE", target = "系统设置")
    @PostMapping("/clear-data")
    public ApiResponse<Void> clearData(@RequestBody Map<String, String> body) {
        String type = body.get("type");
        if ("orders".equals(type)) {
            orderRecordMapper.delete(null);
        } else if ("recipes".equals(type)) {
            recipeMapper.delete(null);
        } else if ("gallery".equals(type)) {
            galleryImageMapper.delete(null);
        } else if ("sync".equals(type)) {
            syncLogMapper.delete(null);
        }
        return ApiResponse.success();
    }
}
