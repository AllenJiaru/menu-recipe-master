package com.example.shiyu.ui.diner

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DinerHomeViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val orderRepository: OrderRepository,
    private val backendRepository: BackendRepository
) : ViewModel() {

    private val _recipes = mutableStateListOf<RecipeEntity>()
    val recipes: List<RecipeEntity> = _recipes

    private val _orders = mutableStateListOf<OrderEntity>()
    val orders: List<OrderEntity> = _orders

    /** 食客只展示本人的订单 */
    private val _myOrders = mutableStateListOf<OrderEntity>()
    val myOrders: List<OrderEntity> = _myOrders

    private val _recipeCount = mutableIntStateOf(0)
    val recipeCount: State<Int> = _recipeCount

    private val _orderCount = mutableIntStateOf(0)
    val orderCount: State<Int> = _orderCount

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _isOffline = mutableStateOf(false)
    val isOffline: State<Boolean> = _isOffline

    private val _randomRecipe = mutableStateOf<RecipeEntity?>(null)
    val randomRecipe: State<RecipeEntity?> = _randomRecipe

    private var autoSynced = false
    private var searchJob: Job? = null

    val nickname: State<String>
        get() = mutableStateOf(backendRepository.getSavedNickname())

    val username: State<String>
        get() = mutableStateOf(backendRepository.getSavedUsername())

    val isLoggedIn: Boolean
        get() = backendRepository.isLoggedIn

    init {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val recipes = recipeRepository.getAllRecipesOnce()
                _recipes.clear()
                _recipes.addAll(recipes.map { it.recipe })
                val orders = orderRepository.getAllOrdersOnce()
                _orders.clear()
                _orders.addAll(orders)
                _myOrders.clear()
                val currentUserId = backendRepository.getSavedUserId()
                _myOrders.addAll(
                    orders.filter { it.user_id == null || it.user_id == 0L || it.user_id == currentUserId }
                )
                _recipeCount.value = recipeRepository.getRecipeCountOnce()
                _orderCount.value = _myOrders.size

                if (!autoSynced && backendRepository.isLoggedIn) {
                    autoSynced = true
                    try {
                        backendRepository.pullRecipes()
                        backendRepository.pullOrders()
                        _isOffline.value = false
                    } catch (e: Exception) {
                        _isOffline.value = true
                    }
                    val freshRecipes = recipeRepository.getAllRecipesOnce()
                    _recipes.clear()
                    _recipes.addAll(freshRecipes.map { it.recipe })
                    val freshOrders = orderRepository.getAllOrdersOnce()
                    _orders.clear()
                    _orders.addAll(freshOrders)
                    _myOrders.clear()
                    _myOrders.addAll(
                        freshOrders.filter { it.user_id == null || it.user_id == 0L || it.user_id == currentUserId }
                    )
                    _recipeCount.value = recipeRepository.getRecipeCountOnce()
                    _orderCount.value = _myOrders.size
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
                _myOrders.clear()
                val currentUserId = backendRepository.getSavedUserId()
                _myOrders.addAll(
                    orders.filter { it.user_id == null || it.user_id == 0L || it.user_id == currentUserId }
                )

                _recipeCount.value = recipeRepository.getRecipeCountOnce()
                _orderCount.value = _myOrders.size
            } catch (e: Exception) {
                _isOffline.value = true
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun refreshData() {
        loadData()
    }

    fun refreshFromBackend() {
        viewModelScope.launch {
            if (!backendRepository.isLoggedIn) return@launch
            try {
                backendRepository.pullOrders()
                val orders = orderRepository.getAllOrdersOnce()
                _orders.clear()
                _orders.addAll(orders)
                _myOrders.clear()
                val currentUserId = backendRepository.getSavedUserId()
                _myOrders.addAll(
                    orders.filter { it.user_id == null || it.user_id == 0L || it.user_id == currentUserId }
                )
                _orderCount.value = _myOrders.size
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun searchRecipesFromBackend(
        keyword: String = "",
        categoryId: Long? = null,
        difficulty: Int = 0
    ) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            if (keyword.isBlank() && categoryId == null && difficulty == 0) {
                loadData()
                return@launch
            }
            _isLoading.value = true
            try {
                val result = backendRepository.searchRecipes(
                    keyword = keyword.ifBlank { null },
                    categoryId = categoryId,
                    difficulty = if (difficulty != 0) difficulty else null
                )
                result.onSuccess { dtos ->
                    _isOffline.value = false
                    val synced = backendRepository.syncRecipesToLocal(dtos)
                    val freshRecipes = recipeRepository.getAllRecipesOnce()
                    _recipes.clear()
                    _recipes.addAll(freshRecipes.map { it.recipe })
                    _recipeCount.value = freshRecipes.size
                }
                result.onFailure {
                    _isOffline.value = true
                }
            } catch (e: Exception) {
                _isOffline.value = true
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchRandomRecipes() {
        viewModelScope.launch {
            if (!backendRepository.isLoggedIn) {
                _isOffline.value = true
                if (_recipes.isNotEmpty()) {
                    val recipe = _recipes.random()
                    _randomRecipe.value = recipe
                }
                return@launch
            }
            _isLoading.value = true
            try {
                val result = backendRepository.getRandomRecipes(6)
                result.onSuccess { dtos ->
                    _isOffline.value = false
                    backendRepository.syncRecipesToLocal(dtos)
                    val freshRecipes = recipeRepository.getAllRecipesOnce()
                    _recipes.clear()
                    _recipes.addAll(freshRecipes.map { it.recipe })
                    _recipeCount.value = freshRecipes.size
                    if (freshRecipes.isNotEmpty()) {
                        val random = freshRecipes.random().recipe
                        _randomRecipe.value = random
                    }
                }
                result.onFailure {
                    _isOffline.value = true
                }
            } catch (e: Exception) {
                _isOffline.value = true
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteOrders(orderIds: List<Long>) {
        viewModelScope.launch {
            val toDelete = _myOrders.filter { it.id in orderIds }
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

    fun pickRandomRecipe(): RecipeEntity? {
        if (_recipes.isEmpty()) return null
        val recipe = _recipes.random()
        _randomRecipe.value = recipe
        return recipe
    }

    fun clearRandomRecipe() {
        _randomRecipe.value = null
    }

    fun cancelOrder(order: OrderEntity, reason: String = "") {
        viewModelScope.launch {
            val updated = order.copy(status = 4, reject_reason = reason)
            orderRepository.updateOrder(updated)
            if (backendRepository.isLoggedIn && order.sync_id != null) {
                backendRepository.pushOrderStatus(updated)
            }
            loadData()
        }
    }
}
