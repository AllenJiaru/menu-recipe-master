package com.example.shiyu.ui.chef

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shiyu.data.db.entity.RecipeEntity
import com.example.shiyu.data.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecipeCategoryViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private val _recipes = mutableStateListOf<RecipeEntity>()
    val recipes: List<RecipeEntity> = _recipes

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    fun loadRecipes(type: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val recipes = recipeRepository.getRecipesByTypeOnce(type)
                _recipes.clear()
                _recipes.addAll(recipes.map { it.recipe })
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
