package com.example.shiyu.ui.chef

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shiyu.data.db.entity.CoupleConfigEntity
import com.example.shiyu.data.db.entity.OrderEntity
import com.example.shiyu.data.db.entity.RecipeEntity
import com.example.shiyu.data.repository.BackendRepository
import com.example.shiyu.data.repository.OrderRepository
import com.example.shiyu.data.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChefHomeViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val orderRepository: OrderRepository,
    private val backendRepository: BackendRepository
) : ViewModel() {

    private val _coupleConfig = mutableStateOf<CoupleConfigEntity?>(null)
    val coupleConfig: State<CoupleConfigEntity?> = _coupleConfig

    private val _recipes = mutableStateListOf<RecipeEntity>()
    val recipes: List<RecipeEntity> = _recipes

    private val _orders = mutableStateListOf<OrderEntity>()
    val orders: List<OrderEntity> = _orders

    private val _pendingOrderCount = mutableIntStateOf(0)
    val pendingOrderCount: State<Int> = _pendingOrderCount

    private val _recipeCount = mutableIntStateOf(0)
    val recipeCount: State<Int> = _recipeCount

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private var autoSynced = false

    val nickname: State<String>
        get() = mutableStateOf(backendRepository.getSavedNickname())

    val username: State<String>
        get() = mutableStateOf(backendRepository.getSavedUsername())

    init {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 先加载本地数据
                val recipes = recipeRepository.getAllRecipesOnce()
                _recipes.clear()
                _recipes.addAll(recipes.map { it.recipe })
                val orders = orderRepository.getAllOrdersOnce()
                _orders.clear()
                _orders.addAll(orders)
                _recipeCount.value = recipeRepository.getRecipeCountOnce()
                _pendingOrderCount.value = orderRepository.getPendingOrderCountOnce()

                // 登录后首次进入，后台静默同步再刷新
                if (!autoSynced && backendRepository.isLoggedIn) {
                    autoSynced = true
                    backendRepository.pullRecipes()
                    backendRepository.pullOrders()
                    val freshRecipes = recipeRepository.getAllRecipesOnce()
                    _recipes.clear()
                    _recipes.addAll(freshRecipes.map { it.recipe })
                    val freshOrders = orderRepository.getAllOrdersOnce()
                    _orders.clear()
                    _orders.addAll(freshOrders)
                    _recipeCount.value = recipeRepository.getRecipeCountOnce()
                    _pendingOrderCount.value = orderRepository.getPendingOrderCountOnce()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadData() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val recipes = recipeRepository.getAllRecipesOnce()
                _recipes.clear()
                _recipes.addAll(recipes.map { it.recipe })

                val orders = orderRepository.getAllOrdersOnce()
                _orders.clear()
                _orders.addAll(orders)

                _recipeCount.value = recipeRepository.getRecipeCountOnce()
                _pendingOrderCount.value = orderRepository.getPendingOrderCountOnce()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshData() {
        loadData()
    }

    /** 从后端拉取订单并刷新本地列表（用于实时同步） */
    fun refreshFromBackend() {
        viewModelScope.launch {
            if (!backendRepository.isLoggedIn) return@launch
            try {
                backendRepository.pullOrders()
                val orders = orderRepository.getAllOrdersOnce()
                _orders.clear()
                _orders.addAll(orders)
                _pendingOrderCount.value = orderRepository.getPendingOrderCountOnce()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private val _syncMessage = mutableStateOf("")
    val syncMessage: State<String> = _syncMessage

    fun syncFromBackend() {
        viewModelScope.launch {
            _syncMessage.value = "正在从云端同步菜谱..."
            val result = backendRepository.pullRecipes()
            _syncMessage.value = result.getOrNull() ?: "同步失败：${result.exceptionOrNull()?.message}"
            loadData()
        }
    }

    fun acceptOrder(order: OrderEntity) = updateStatus(order, 1, accept = true)
    fun startCooking(order: OrderEntity) = updateStatus(order, 2)
    fun completeOrder(order: OrderEntity) = updateStatus(order, 3, complete = true)
    fun rejectOrder(order: OrderEntity, reason: String = "") {
        viewModelScope.launch {
            val updated = order.copy(status = 4, reject_reason = reason)
            orderRepository.updateOrder(updated)
            if (backendRepository.isLoggedIn && updated.sync_id != null) {
                backendRepository.pushOrderStatus(updated)
            }
            loadData()
        }
    }

    fun deleteOrders(orderIds: List<Long>) {
        viewModelScope.launch {
            val toDelete = _orders.filter { it.id in orderIds }
            toDelete.forEach { orderRepository.deleteOrder(it.id) }
            if (backendRepository.isLoggedIn) {
                toDelete.filter { it.sync_id != null }.let { synced ->
                    if (synced.isNotEmpty()) {
                        backendRepository.batchDeleteBackendOrders(synced)
                    }
                }
            }
            loadData()
        }
    }

    private fun updateStatus(order: OrderEntity, status: Int, accept: Boolean = false, complete: Boolean = false) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val updated = order.copy(
                status = status,
                accept_time = if (accept) now else order.accept_time,
                complete_time = if (complete) now else order.complete_time
            )
            orderRepository.updateOrder(updated)
            if (backendRepository.isLoggedIn && updated.sync_id != null) {
                backendRepository.pushOrderStatus(updated)
            }
            loadData()
        }
    }
}
