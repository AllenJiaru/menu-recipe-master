package com.shiyu.service;

import com.shiyu.dto.request.InventoryRequest;
import com.shiyu.dto.response.PageResponse;
import com.shiyu.entity.Inventory;
import java.util.List;

public interface InventoryService {
    PageResponse<Inventory> getInventory(int page, int size, String keyword, String category);
    Inventory getInventoryById(Long id);
    void createInventory(InventoryRequest request);
    void updateInventory(Long id, InventoryRequest request);
    void deleteInventory(Long id);
    List<Inventory> getLowStockItems();
    void restock(Long id, Double quantity);
}
