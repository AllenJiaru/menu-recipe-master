package com.example.shiyu.ui.chef

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shiyu.data.db.dao.RecipeWithDetails
import com.example.shiyu.data.db.entity.RecipeEntity
import com.example.shiyu.data.db.entity.RecipeMaterialEntity
import com.example.shiyu.data.db.entity.RecipeStepEntity
import com.example.shiyu.data.repository.BackendRepository
import com.example.shiyu.data.repository.RecipeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddRecipeViewModel @Inject constructor(
    private val recipeRepository: RecipeRepository,
    private val backendRepository: BackendRepository
) : ViewModel() {

    private val _existingRecipe = mutableStateOf<RecipeWithDetails?>(null)
    val existingRecipe: State<RecipeWithDetails?> = _existingRecipe

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    fun loadRecipe(recipeId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _existingRecipe.value = recipeRepository.getRecipeWithDetailsById(recipeId)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun saveRecipe(
        recipeId: Long? = null,
        name: String,
        type: Int,
        description: String,
        imagePath: String,
        cookingTime: Int,
        difficulty: Int,
        materials: List<Triple<String, String, String>> = emptyList(),
        steps: List<String> = emptyList()
    ): Long {
        val now = System.currentTimeMillis()
        val materialEntities = materials
            .filter { it.first.isNotBlank() }
            .map { (materialName, amount, unit) ->
                RecipeMaterialEntity(
                    recipe_id = 0,
                    name = materialName.trim(),
                    amount = amount.trim(),
                    unit = unit.trim()
                )
            }
        val stepEntities = steps
            .filter { it.isNotBlank() }
            .mapIndexed { index, desc ->
                RecipeStepEntity(
                    recipe_id = 0,
                    step_number = index + 1,
                    description = desc.trim()
                )
            }

        if (recipeId != null) {
            val existing = recipeRepository.getRecipeById(recipeId) ?: return -1L
            val updated = existing.copy(
                name = name,
                type = type,
                description = description,
                image_path = imagePath.ifEmpty { existing.image_path },
                cooking_time = cookingTime,
                difficulty = difficulty,
                update_time = now
            )
            recipeRepository.updateRecipeWithDetails(updated, materialEntities, stepEntities)
            syncToBackend(updated.sync_id, name, type, description, imagePath.ifEmpty { existing.image_path }, cookingTime, difficulty, materials, steps)
            return recipeId
        } else {
            val recipe = RecipeEntity(
                name = name,
                type = type,
                description = description,
                image_path = imagePath,
                cooking_time = cookingTime,
                difficulty = difficulty,
                create_time = now,
                update_time = now
            )
            val localId = recipeRepository.insertRecipe(recipe, materialEntities, stepEntities)
            syncToBackend(null, name, type, description, imagePath, cookingTime, difficulty, materials, steps)
            return localId
        }
    }

    private fun syncToBackend(
        syncId: Long?,
        name: String,
        type: Int,
        description: String,
        coverImage: String,
        cookingTime: Int,
        difficulty: Int,
        materials: List<Triple<String, String, String>>,
        steps: List<String>
    ) {
        viewModelScope.launch {
            if (!backendRepository.isLoggedIn) return@launch
            try {
                if (syncId != null) {
                    backendRepository.updateRecipeOnBackend(syncId, name, type, description, coverImage, cookingTime, difficulty, materials, steps)
                } else {
                    backendRepository.createRecipeOnBackend(name, type, description, coverImage, cookingTime, difficulty, materials, steps)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
