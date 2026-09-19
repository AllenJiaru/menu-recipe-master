package com.shiyu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.shiyu.common.BusinessException;
import com.shiyu.dto.request.ShoppingItemRequest;
import com.shiyu.entity.Recipe;
import com.shiyu.entity.RecipeMaterial;
import com.shiyu.entity.ShoppingItem;
import com.shiyu.mapper.RecipeMapper;
import com.shiyu.mapper.RecipeMaterialMapper;
import com.shiyu.mapper.ShoppingItemMapper;
import com.shiyu.service.ShoppingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ShoppingServiceImpl implements ShoppingService {

    @Autowired
    private ShoppingItemMapper shoppingItemMapper;

    @Autowired
    private RecipeMapper recipeMapper;

    @Autowired
    private RecipeMaterialMapper recipeMaterialMapper;

    @Override
    public List<ShoppingItem> getShoppingList(Long userId, String listName) {
        return shoppingItemMapper.findByUserAndList(userId, listName);
    }

    @Override
    public void addItem(ShoppingItemRequest request, Long userId) {
        ShoppingItem item = new ShoppingItem();
        item.setUserId(userId);
        item.setListName(request.getListName() != null ? request.getListName() : "default");
        item.setIngredientName(request.getIngredientName());
        item.setQuantity(request.getQuantity());
        item.setUnit(request.getUnit());
        item.setCategory(request.getCategory());
        item.setIsChecked(false);
        item.setSourceRecipeId(request.getSourceRecipeId());
        item.setSourceRecipeName(request.getSourceRecipeName());
        item.setCreateTime(LocalDateTime.now());
        item.setUpdateTime(LocalDateTime.now());
        item.setDeleted(0);
        shoppingItemMapper.insert(item);
    }

    @Override
    public void addFromRecipe(Long userId, Long recipeId, String recipeName) {
        Recipe recipe = recipeMapper.selectById(recipeId);
        if (recipe == null) {
            throw new BusinessException("Recipe not found");
        }

        List<RecipeMaterial> materials = recipeMaterialMapper.findByRecipeId(recipeId);
        if (materials == null || materials.isEmpty()) {
            return;
        }

        for (RecipeMaterial material : materials) {
            ShoppingItem item = new ShoppingItem();
            item.setUserId(userId);
            item.setListName(recipeName);
            item.setIngredientName(material.getName());
            item.setUnit(material.getUnit());
            item.setIsChecked(false);
            item.setSourceRecipeId(recipeId);
            item.setSourceRecipeName(recipeName);
            item.setCreateTime(LocalDateTime.now());
            item.setUpdateTime(LocalDateTime.now());
            item.setDeleted(0);
            shoppingItemMapper.insert(item);
        }
    }

    @Override
    public void toggleItem(Long id) {
        ShoppingItem item = shoppingItemMapper.selectById(id);
        if (item == null) {
            throw new BusinessException("Shopping item not found");
        }
        item.setIsChecked(!item.getIsChecked());
        item.setUpdateTime(LocalDateTime.now());
        shoppingItemMapper.updateById(item);
    }

    @Override
    public void deleteItem(Long id) {
        ShoppingItem item = shoppingItemMapper.selectById(id);
        if (item == null) {
            throw new BusinessException("Shopping item not found");
        }
        shoppingItemMapper.deleteById(id);
    }

    @Override
    public void clearChecked(Long userId) {
        LambdaQueryWrapper<ShoppingItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingItem::getUserId, userId);
        wrapper.eq(ShoppingItem::getIsChecked, true);
        List<ShoppingItem> checkedItems = shoppingItemMapper.selectList(wrapper);
        for (ShoppingItem item : checkedItems) {
            shoppingItemMapper.deleteById(item.getId());
        }
    }

    @Override
    public Map<String, Object> getShoppingStats(Long userId) {
        LambdaQueryWrapper<ShoppingItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingItem::getUserId, userId);
        Long totalItems = shoppingItemMapper.selectCount(wrapper);

        LambdaQueryWrapper<ShoppingItem> checkedWrapper = new LambdaQueryWrapper<>();
        checkedWrapper.eq(ShoppingItem::getUserId, userId);
        checkedWrapper.eq(ShoppingItem::getIsChecked, true);
        Long checkedItems = shoppingItemMapper.selectCount(checkedWrapper);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalItems", totalItems);
        stats.put("checkedItems", checkedItems);
        stats.put("uncheckedItems", totalItems - checkedItems);
        return stats;
    }
}
