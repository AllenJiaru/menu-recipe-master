package com.example.shiyu.ui.diner

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shiyu.data.db.entity.OrderEntity
import com.example.shiyu.data.db.entity.RecipeEntity
import com.example.shiyu.data.repository.BackendRepository
import com.example.shiyu.data.repository.OrderRepository
import com.example.shiyu.data.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DinerOrderViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val orderRepository: OrderRepository,
    private val backendRepository: BackendRepository
) : ViewModel() {

    private val _recipe = mutableStateOf<RecipeEntity?>(null)
    val recipe: State<RecipeEntity?> = _recipe

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _orderSuccess = mutableStateOf(false)
    val orderSuccess: State<Boolean> = _orderSuccess

    fun loadRecipe(recipeId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _recipe.value = recipeRepository.getRecipeById(recipeId)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun submitOrder(recipeId: Long, recipeName: String, recipeImage: String, remark: String) {
        viewModelScope.launch {
            try {
                val order = OrderEntity(
                    recipe_id = recipeId,
                    recipe_name = recipeName,
                    recipe_image = recipeImage,
                    remark = remark,
                    order_time = System.currentTimeMillis(),
                    user_id = backendRepository.getSavedUserId().takeIf { it > 0 }
                )
                val localId = orderRepository.insertOrder(order)

                // 若已登录后端，尝试推送订单
                if (backendRepository.isLoggedIn) {
                    val recipe = recipeRepository.getRecipeById(recipeId)
                    val serverId = recipe?.sync_id
                    val pushResult = if (serverId != null) {
                        backendRepository.pushOrder(
                            order.copy(
                                recipe_id = serverId,
                                id = localId
                            )
                        )
                    } else {
                        null
                    }
                    if (pushResult != null) {
                        orderRepository.updateOrder(order.copy(id = localId, sync_id = pushResult))
                    }
                }
                _orderSuccess.value = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
