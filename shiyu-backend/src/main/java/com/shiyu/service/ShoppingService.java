package com.shiyu.service;

import com.shiyu.dto.request.ShoppingItemRequest;
import com.shiyu.entity.ShoppingItem;

import java.util.List;
import java.util.Map;

public interface ShoppingService {
    List<ShoppingItem> getShoppingList(Long userId, String listName);
    void addItem(ShoppingItemRequest request, Long userId);
    void addFromRecipe(Long userId, Long recipeId, String recipeName);
    void toggleItem(Long id);
    void deleteItem(Long id);
    void clearChecked(Long userId);
    Map<String, Object> getShoppingStats(Long userId);
}
