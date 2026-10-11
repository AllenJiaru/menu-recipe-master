package com.shiyu.controller;

import com.shiyu.annotation.OperationLog;
import com.shiyu.common.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import com.shiyu.dto.request.ShoppingItemRequest;
import com.shiyu.entity.ShoppingItem;
import com.shiyu.entity.User;
import com.shiyu.security.UserDetailsImpl;
import com.shiyu.service.ShoppingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/shopping")
public class ShoppingController {

    @Autowired
    private ShoppingService shoppingService;

    private User getCurrentUser() {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userDetails.getUser();
    }

    @OperationLog(action = "QUERY", target = "采购清单")
    @GetMapping
    @PreAuthorize("hasAuthority('business:shopping')")
    public ApiResponse<List<ShoppingItem>> getShoppingList(
            @RequestParam(required = false) String listName) {
        User user = getCurrentUser();
        String name = listName != null ? listName : "default";
        return ApiResponse.success(shoppingService.getShoppingList(user.getId(), name));
    }

    @OperationLog(action = "CREATE", target = "采购清单")
    @PostMapping
    @PreAuthorize("hasAuthority('business:shopping:create')")
    public ApiResponse<Void> addItem(@Valid @RequestBody ShoppingItemRequest request) {
        User user = getCurrentUser();
        shoppingService.addItem(request, user.getId());
        return ApiResponse.success();
    }

    @OperationLog(action = "CREATE", target = "采购清单")
    @PostMapping("/from-recipe/{recipeId}")
    @PreAuthorize("hasAuthority('business:shopping:create')")
    public ApiResponse<Void> addFromRecipe(@PathVariable Long recipeId, @RequestParam(required = false) String recipeName) {
        User user = getCurrentUser();
        String name = recipeName != null ? recipeName : "";
        shoppingService.addFromRecipe(user.getId(), recipeId, name);
        return ApiResponse.success();
    }

    @OperationLog(action = "UPDATE", target = "采购清单")
    @PutMapping("/{id}/toggle")
    @PreAuthorize("hasAuthority('business:shopping:edit')")
    public ApiResponse<Void> toggleItem(@PathVariable Long id) {
        shoppingService.toggleItem(id);
        return ApiResponse.success();
    }

    @OperationLog(action = "DELETE", target = "采购清单")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('business:shopping:delete')")
    public ApiResponse<Void> deleteItem(@PathVariable Long id) {
        shoppingService.deleteItem(id);
        return ApiResponse.success();
    }

    @OperationLog(action = "DELETE", target = "采购清单")
    @DeleteMapping("/clear-checked")
    @PreAuthorize("hasAuthority('business:shopping:delete')")
    public ApiResponse<Void> clearChecked() {
        User user = getCurrentUser();
        shoppingService.clearChecked(user.getId());
        return ApiResponse.success();
    }

    @OperationLog(action = "QUERY", target = "采购清单")
    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('business:shopping')")
    public ApiResponse<Map<String, Object>> getShoppingStats() {
        User user = getCurrentUser();
        return ApiResponse.success(shoppingService.getShoppingStats(user.getId()));
    }
}
