package com.example.shiyu.ui.chef

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shiyu.api.CommentDto
import com.example.shiyu.api.CommentStatsDto
import com.example.shiyu.data.db.dao.RecipeWithDetails
import com.example.shiyu.data.repository.BackendRepository
import com.example.shiyu.data.repository.RecipeRepository
import com.example.shiyu.util.Constants
import com.example.shiyu.util.RoleManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeDetailViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val backendRepository: BackendRepository,
    private val roleManager: RoleManager
) : ViewModel() {

    private val _recipe = mutableStateOf<RecipeWithDetails?>(null)
    val recipe: State<RecipeWithDetails?> = _recipe

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _isDiner = mutableStateOf(false)
    val isDiner: State<Boolean> = _isDiner

    private val _comments = mutableStateListOf<CommentDto>()
    val comments: List<CommentDto> = _comments

    private val _commentsLoading = mutableStateOf(false)
    val commentsLoading: State<Boolean> = _commentsLoading

    private val _commentPosted = mutableStateOf(false)
    val commentPosted: State<Boolean> = _commentPosted

    val isLoggedIn: State<Boolean>
        get() = mutableStateOf(backendRepository.isLoggedIn)

    private val _commentStats = mutableStateOf<CommentStatsDto?>(null)
    val commentStats: State<CommentStatsDto?> = _commentStats

    init {
        viewModelScope.launch {
            roleManager.currentRole.collect { role ->
                _isDiner.value = role == Constants.ROLE_DINER
            }
        }
    }

    fun loadRecipe(recipeId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _recipe.value = recipeRepository.getRecipeWithDetailsById(recipeId)
                loadComments(recipeId)
                loadCommentStats(recipeId)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
        // 若本地缺少食材/步骤，从后端懒加载详情
        viewModelScope.launch {
            try {
                val details = recipeRepository.getRecipeWithDetailsById(recipeId) ?: return@launch
                val syncId = details.recipe.sync_id
                if (details.materials.isEmpty() && details.steps.isEmpty() &&
                    syncId != null && backendRepository.isLoggedIn
                ) {
                    backendRepository.fetchRecipeDetail(syncId)
                    _recipe.value = recipeRepository.getRecipeWithDetailsById(recipeId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun loadComments(recipeId: Long) {
        if (!backendRepository.isLoggedIn) return
        _commentsLoading.value = true
        try {
            val list = backendRepository.getComments(recipeId)
            _comments.clear()
            _comments.addAll(list)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            _commentsLoading.value = false
        }
    }

    private suspend fun loadCommentStats(recipeId: Long) {
        if (!backendRepository.isLoggedIn) return
        try {
            _commentStats.value = backendRepository.getCommentStats(recipeId)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun refreshComments() {
        _recipe.value?.let { details ->
            viewModelScope.launch { loadComments(details.recipe.id) }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            _recipe.value?.let { details ->
                val recipe = details.recipe
                val newFavorite = if (recipe.is_favorite == 1) 0 else 1
                recipeRepository.updateFavorite(recipe.id, newFavorite)
                _recipe.value = details.copy(recipe = recipe.copy(is_favorite = newFavorite))
                if (backendRepository.isLoggedIn) {
                    val syncId = recipe.sync_id
                    if (syncId != null) {
                        backendRepository.toggleFavoriteBackend(syncId)
                    }
                }
            }
        }
    }

    val currentUserId: Long
        get() = backendRepository.getSavedUserId()

    val serverUrl: String
        get() = backendRepository.getServerUrl()

    fun postComment(content: String, rating: Int) {
        if (content.isBlank()) return
        val recipeId = _recipe.value?.recipe?.id ?: return
        viewModelScope.launch {
            val success = backendRepository.addComment(recipeId, content.trim(), rating)
            _commentPosted.value = success
            if (success) {
                refreshComments()
            }
        }
    }

    fun deleteComment(commentId: Long) {
        viewModelScope.launch {
            val success = backendRepository.deleteComment(commentId)
            if (success) {
                refreshComments()
            }
        }
    }

    private val _deleted = mutableStateOf(false)
    val deleted: State<Boolean> = _deleted

    fun deleteRecipe() {
        viewModelScope.launch {
            _recipe.value?.let { details ->
                recipeRepository.deleteRecipe(details.recipe.id)
                _deleted.value = true
            }
        }
    }
}
