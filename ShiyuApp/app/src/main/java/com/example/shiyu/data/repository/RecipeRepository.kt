package com.example.shiyu.data.repository

import com.example.shiyu.api.RecipeDto
import com.example.shiyu.data.db.dao.RecipeDao
import com.example.shiyu.data.db.dao.RecipeWithDetails
import com.example.shiyu.data.db.entity.RecipeEntity
import com.example.shiyu.data.db.entity.RecipeMaterialEntity
import com.example.shiyu.data.db.entity.RecipeStepEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecipeRepository @Inject constructor(
    private val recipeDao: RecipeDao
) {
    fun getAllRecipes(): Flow<List<RecipeWithDetails>> = recipeDao.getAllRecipes()

    suspend fun getAllRecipesOnce(): List<RecipeWithDetails> = recipeDao.getAllRecipesOnce()

    fun getRecipesByType(type: Int): Flow<List<RecipeWithDetails>> = recipeDao.getRecipesByType(type)

    suspend fun getRecipesByTypeOnce(type: Int): List<RecipeWithDetails> = recipeDao.getRecipesByTypeOnce(type)

    fun getFavoriteRecipes(): Flow<List<RecipeWithDetails>> = recipeDao.getFavoriteRecipes()

    suspend fun getFavoriteRecipesOnce(): List<RecipeWithDetails> = recipeDao.getFavoriteRecipesOnce()

    suspend fun getRecipeById(id: Long): RecipeEntity? = recipeDao.getRecipeById(id)

    suspend fun getRecipeWithDetailsById(id: Long): RecipeWithDetails? = recipeDao.getRecipeWithDetailsById(id)

    fun getRecipeCount(): Flow<Int> = recipeDao.getRecipeCount()

    suspend fun getRecipeCountOnce(): Int = recipeDao.getRecipeCountOnce()

    suspend fun insertRecipe(
        recipe: RecipeEntity,
        materials: List<RecipeMaterialEntity> = emptyList(),
        steps: List<RecipeStepEntity> = emptyList()
    ): Long {
        val recipeId = recipeDao.insertRecipe(recipe)

        val materialsWithRecipeId = materials.map { it.copy(recipe_id = recipeId) }
        recipeDao.insertMaterials(materialsWithRecipeId)

        val stepsWithRecipeId = steps.map { it.copy(recipe_id = recipeId) }
        recipeDao.insertSteps(stepsWithRecipeId)

        return recipeId
    }

    suspend fun updateRecipe(recipe: RecipeEntity) {
        recipeDao.updateRecipe(recipe)
    }

    suspend fun updateRecipeWithDetails(
        recipe: RecipeEntity,
        materials: List<RecipeMaterialEntity> = emptyList(),
        steps: List<RecipeStepEntity> = emptyList()
    ) {
        recipeDao.updateRecipe(recipe)
        recipeDao.deleteMaterialsByRecipeId(recipe.id)
        recipeDao.deleteStepsByRecipeId(recipe.id)
        if (materials.isNotEmpty()) {
            recipeDao.insertMaterials(materials.map { it.copy(recipe_id = recipe.id) })
        }
        if (steps.isNotEmpty()) {
            recipeDao.insertSteps(steps.map { it.copy(recipe_id = recipe.id) })
        }
    }

    suspend fun updateFavorite(id: Long, isFavorite: Int) {
        recipeDao.updateFavorite(id, isFavorite)
    }

    suspend fun deleteRecipe(id: Long) {
        recipeDao.deleteRecipe(id)
    }

    suspend fun fetchRecipesFromBackend(
        keyword: String? = null,
        categoryId: Long? = null,
        difficulty: Int? = null
    ): List<RecipeDto> {
        return try {
            val url = com.example.shiyu.api.ApiClient.getBaseUrl()
            val api = com.example.shiyu.api.ApiClient.backendApi
            val response = api.getRecipes(
                page = 1, size = 200,
                keyword = keyword,
                categoryId = categoryId,
                difficulty = difficulty
            )
            if (response.code == 200 && response.data != null) {
                response.data.records
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
