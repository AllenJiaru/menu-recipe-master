package com.shiyu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.shiyu.common.BusinessException;
import com.shiyu.dto.request.InventoryRequest;
import com.shiyu.dto.response.PageResponse;
import com.shiyu.entity.Inventory;
import com.shiyu.mapper.InventoryMapper;
import com.shiyu.service.InventoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class InventoryServiceImpl implements InventoryService {

    @Autowired
    private InventoryMapper inventoryMapper;

    @Override
    public PageResponse<Inventory> getInventory(int page, int size, String keyword, String category) {
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(keyword), Inventory::getName, keyword);
        wrapper.eq(StringUtils.hasText(category), Inventory::getCategory, category);
        wrapper.orderByDesc(Inventory::getCreateTime);

        Page<Inventory> pageResult = inventoryMapper.selectPage(new Page<>(page, size), wrapper);
        return PageResponse.of(pageResult.getRecords(), pageResult.getTotal(), page, size);
    }

    @Override
    public Inventory getInventoryById(Long id) {
        Inventory inventory = inventoryMapper.selectById(id);
        if (inventory == null) {
            throw new BusinessException("Inventory item not found");
        }
        return inventory;
    }

    @Override
    public void createInventory(InventoryRequest request) {
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Inventory::getName, request.getName());
        Long count = inventoryMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException("Inventory item name already exists");
        }

        Inventory inventory = new Inventory();
        inventory.setName(request.getName());
        inventory.setCategory(request.getCategory());
        inventory.setQuantity(request.getQuantity());
        inventory.setUnit(request.getUnit());
        inventory.setThreshold(request.getThreshold());
        inventory.setStatus(request.getQuantity() != null && request.getThreshold() != null
                ? (request.getQuantity() < request.getThreshold() ? 0 : 1) : 1);
        inventory.setCreateTime(LocalDateTime.now());
        inventory.setUpdateTime(LocalDateTime.now());
        inventoryMapper.insert(inventory);
    }

    @Override
    public void updateInventory(Long id, InventoryRequest request) {
        Inventory existing = inventoryMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("Inventory item not found");
        }

        existing.setName(request.getName());
        existing.setCategory(request.getCategory());
        existing.setQuantity(request.getQuantity());
        existing.setUnit(request.getUnit());
        existing.setThreshold(request.getThreshold());
        existing.setStatus(request.getQuantity() != null && request.getThreshold() != null
                ? (request.getQuantity() < request.getThreshold() ? 0 : 1) : existing.getStatus());
        existing.setUpdateTime(LocalDateTime.now());
        inventoryMapper.updateById(existing);
    }

    @Override
    public void deleteInventory(Long id) {
        Inventory existing = inventoryMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("Inventory item not found");
        }
        inventoryMapper.deleteById(id);
    }

    @Override
    public void restock(Long id, Double quantity) {
        Inventory existing = inventoryMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException("Inventory item not found");
        }

        double newQuantity = (existing.getQuantity() != null ? existing.getQuantity() : 0) + quantity;
        existing.setQuantity(newQuantity);
        existing.setLastRestockTime(LocalDateTime.now());
        existing.setStatus(existing.getThreshold() != null && newQuantity < existing.getThreshold() ? 0 : 1);
        existing.setUpdateTime(LocalDateTime.now());
        inventoryMapper.updateById(existing);
    }

    @Override
    public List<Inventory> getLowStockItems() {
        return inventoryMapper.findLowStock();
    }
}
