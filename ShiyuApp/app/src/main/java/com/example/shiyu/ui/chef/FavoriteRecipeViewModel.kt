package com.example.shiyu.ui.chef

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shiyu.api.ApiClient
import com.example.shiyu.data.db.entity.RecipeEntity
import com.example.shiyu.data.repository.BackendRepository
import com.example.shiyu.data.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoriteRecipeViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val backendRepository: BackendRepository
) : ViewModel() {

    private val _recipes = mutableStateListOf<RecipeEntity>()
    val recipes: List<RecipeEntity> = _recipes

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _hasMore = mutableStateOf(true)
    val hasMore: State<Boolean> = _hasMore

    private var currentPage = 1
    private val pageSize = 20

    init {
        loadFavorites()
    }

    fun loadFavorites() {
        viewModelScope.launch {
            _isLoading.value = true
            currentPage = 1
            _hasMore.value = true
            try {
                if (backendRepository.isLoggedIn) {
                    val response = ApiClient.backendApi.getUserFavorites(page = currentPage, size = pageSize)
                    if (response.code == 200 && response.data != null) {
                        val dtos = response.data.records
                        val entities = dtos.map { dto ->
                            RecipeEntity(
                                name = dto.name,
                                type = dto.type ?: 10,
                                description = dto.description ?: "",
                                image_path = dto.coverImage ?: "",
                                cooking_time = dto.cookingTime ?: 0,
                                difficulty = dto.difficulty ?: 1,
                                is_favorite = dto.isFavorite ?: 1,
                                status = dto.status ?: 1,
                                create_time = System.currentTimeMillis(),
                                update_time = System.currentTimeMillis(),
                                sync_id = dto.id
                            )
                        }
                        _recipes.clear()
                        _recipes.addAll(entities)
                        _hasMore.value = dtos.size >= pageSize
                    } else {
                        val localRecipes = recipeRepository.getFavoriteRecipesOnce()
                        _recipes.clear()
                        _recipes.addAll(localRecipes.map { it.recipe })
                        _hasMore.value = false
                    }
                } else {
                    val localRecipes = recipeRepository.getFavoriteRecipesOnce()
                    _recipes.clear()
                    _recipes.addAll(localRecipes.map { it.recipe })
                    _hasMore.value = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadMore() {
        if (_isLoading.value || !_hasMore.value) return
        viewModelScope.launch {
            _isLoading.value = true
            try {
                if (backendRepository.isLoggedIn) {
                    val nextPage = currentPage + 1
                    val response = ApiClient.backendApi.getUserFavorites(page = nextPage, size = pageSize)
                    if (response.code == 200 && response.data != null) {
                        val dtos = response.data.records
                        val entities = dtos.map { dto ->
                            RecipeEntity(
                                name = dto.name,
                                type = dto.type ?: 10,
                                description = dto.description ?: "",
                                image_path = dto.coverImage ?: "",
                                cooking_time = dto.cookingTime ?: 0,
                                difficulty = dto.difficulty ?: 1,
                                is_favorite = dto.isFavorite ?: 1,
                                status = dto.status ?: 1,
                                create_time = System.currentTimeMillis(),
                                update_time = System.currentTimeMillis(),
                                sync_id = dto.id
                            )
                        }
                        _recipes.addAll(entities)
                        currentPage = nextPage
                        _hasMore.value = dtos.size >= pageSize
                    } else {
                        _hasMore.value = false
                    }
                } else {
                    _hasMore.value = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleFavorite(recipe: RecipeEntity) {
        viewModelScope.launch {
            val newFavorite = if (recipe.is_favorite == 1) 0 else 1
            recipeRepository.updateFavorite(recipe.id, newFavorite)
            if (backendRepository.isLoggedIn) {
                val syncId = recipe.sync_id
                if (syncId != null) {
                    backendRepository.toggleFavoriteBackend(syncId)
                }
            }
            if (newFavorite == 0) {
                _recipes.removeAll { it.id == recipe.id }
            }
        }
    }
}
