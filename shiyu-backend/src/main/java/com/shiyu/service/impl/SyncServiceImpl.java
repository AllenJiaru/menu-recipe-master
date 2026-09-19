package com.shiyu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shiyu.dto.request.SyncRequest;
import com.shiyu.entity.GalleryImage;
import com.shiyu.entity.OrderRecord;
import com.shiyu.entity.Recipe;
import com.shiyu.entity.SyncLog;
import com.shiyu.mapper.GalleryImageMapper;
import com.shiyu.mapper.OrderRecordMapper;
import com.shiyu.mapper.RecipeMapper;
import com.shiyu.mapper.SyncLogMapper;
import com.shiyu.service.SyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SyncServiceImpl implements SyncService {

    private static final Logger log = LoggerFactory.getLogger(SyncServiceImpl.class);

    @Autowired
    private RecipeMapper recipeMapper;

    @Autowired
    private OrderRecordMapper orderRecordMapper;

    @Autowired
    private GalleryImageMapper galleryImageMapper;

    @Autowired
    private SyncLogMapper syncLogMapper;

    @Override
    public Map<String, Object> pull(SyncRequest request) {
        SyncLog syncLog = createSyncLog(request.getDeviceId(), "pull", "full");

        Map<String, Object> result = new HashMap<>();
        try {
            LocalDateTime lastSyncTime = request.getLastSyncTime() != null
                    ? LocalDateTime.ofInstant(Instant.ofEpochMilli(request.getLastSyncTime()), ZoneId.systemDefault())
                    : LocalDateTime.of(2000, 1, 1, 0, 0);

            List<Recipe> recipes = recipeMapper.findModifiedAfter(lastSyncTime);
            List<OrderRecord> orders = orderRecordMapper.findModifiedAfter(lastSyncTime);
            List<GalleryImage> galleryImages = galleryImageMapper.findModifiedAfter(lastSyncTime);

            result.put("recipes", recipes);
            result.put("orders", orders);
            result.put("gallery", galleryImages);
            result.put("syncTime", System.currentTimeMillis());

            syncLog.setRecordCount(recipes.size() + orders.size() + galleryImages.size());
            syncLog.setStatus("success");
            syncLog.setEndTime(LocalDateTime.now());
            syncLogMapper.updateById(syncLog);
        } catch (Exception e) {
            log.error("Sync pull failed", e);
            syncLog.setStatus("failed");
            syncLog.setErrorMessage(e.getMessage());
            syncLog.setEndTime(LocalDateTime.now());
            syncLogMapper.updateById(syncLog);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @Override
    public Map<String, Object> push(SyncRequest request) {
        SyncLog syncLog = createSyncLog(request.getDeviceId(), "push", "full");

        Map<String, Object> result = new HashMap<>();
        List<Map<String, String>> conflicts = new ArrayList<>();
        int upsertCount = 0;

        try {
            if (request.getRecipes() != null) {
                for (SyncRequest.RecipeSyncData data : request.getRecipes()) {
                    Recipe existing = recipeMapper.findBySyncId(data.getSyncId());
                    if (existing == null) {
                        Recipe recipe = recipeMapper.selectById(data.getId());
                        if (recipe != null) {
                            recipeMapper.insert(recipe);
                            upsertCount++;
                        }
                    } else {
                        LocalDateTime remoteSync = LocalDateTime.ofInstant(
                                Instant.ofEpochMilli(data.getSyncTime()), ZoneId.systemDefault());
                        if (existing.getSyncTime() != null && existing.getSyncTime().isAfter(remoteSync)) {
                            Map<String, String> conflict = new HashMap<>();
                            conflict.put("type", "recipe");
                            conflict.put("syncId", data.getSyncId());
                            conflict.put("serverTime", String.valueOf(existing.getSyncTime()));
                            conflict.put("clientTime", String.valueOf(remoteSync));
                            conflicts.add(conflict);
                        } else {
                            Recipe recipe = recipeMapper.selectById(data.getId());
                            if (recipe != null) {
                                recipe.setId(existing.getId());
                                recipeMapper.updateById(recipe);
                                upsertCount++;
                            }
                        }
                    }
                }
            }

            if (request.getOrders() != null) {
                for (SyncRequest.OrderSyncData data : request.getOrders()) {
                    OrderRecord existing = orderRecordMapper.selectOne(
                            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OrderRecord>()
                                    .eq(OrderRecord::getSyncId, data.getSyncId()));
                    if (existing == null) {
                        OrderRecord order = orderRecordMapper.selectById(data.getId());
                        if (order != null) {
                            orderRecordMapper.insert(order);
                            upsertCount++;
                        }
                    } else {
                        LocalDateTime remoteSync = LocalDateTime.ofInstant(
                                Instant.ofEpochMilli(data.getSyncTime()), ZoneId.systemDefault());
                        if (existing.getSyncTime() != null && existing.getSyncTime().isAfter(remoteSync)) {
                            Map<String, String> conflict = new HashMap<>();
                            conflict.put("type", "order");
                            conflict.put("syncId", data.getSyncId());
                            conflicts.add(conflict);
                        } else {
                            OrderRecord order = orderRecordMapper.selectById(data.getId());
                            if (order != null) {
                                order.setId(existing.getId());
                                orderRecordMapper.updateById(order);
                                upsertCount++;
                            }
                        }
                    }
                }
            }

            if (request.getGallery() != null) {
                for (SyncRequest.GallerySyncData data : request.getGallery()) {
                    GalleryImage existing = galleryImageMapper.selectOne(
                            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<GalleryImage>()
                                    .eq(GalleryImage::getSyncId, data.getSyncId()));
                    if (existing == null) {
                        GalleryImage image = galleryImageMapper.selectById(data.getId());
                        if (image != null) {
                            galleryImageMapper.insert(image);
                            upsertCount++;
                        }
                    }
                }
            }

            result.put("upsertCount", upsertCount);
            result.put("conflicts", conflicts);
            result.put("syncTime", System.currentTimeMillis());

            syncLog.setRecordCount(upsertCount);
            syncLog.setStatus(conflicts.isEmpty() ? "success" : "partial");
            syncLog.setEndTime(LocalDateTime.now());
            syncLogMapper.updateById(syncLog);
        } catch (Exception e) {
            log.error("Sync push failed", e);
            syncLog.setStatus("failed");
            syncLog.setErrorMessage(e.getMessage());
            syncLog.setEndTime(LocalDateTime.now());
            syncLogMapper.updateById(syncLog);
            result.put("error", e.getMessage());
        }
        return result;
    }

    @Override
    public Map<String, Object> getSyncLogs(Integer page, Integer size) {
        Page<SyncLog> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<SyncLog> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(SyncLog::getCreateTime);
        Page<SyncLog> result = syncLogMapper.selectPage(pageParam, wrapper);
        Map<String, Object> map = new HashMap<>();
        map.put("records", result.getRecords());
        map.put("total", result.getTotal());
        map.put("page", page);
        map.put("size", size);
        return map;
    }

    private SyncLog createSyncLog(String deviceId, String syncType, String syncDirection) {
        SyncLog syncLog = new SyncLog();
        syncLog.setDeviceId(deviceId);
        syncLog.setSyncType(syncType);
        syncLog.setSyncDirection(syncDirection);
        syncLog.setStartTime(LocalDateTime.now());
        syncLogMapper.insert(syncLog);
        return syncLog;
    }
}
