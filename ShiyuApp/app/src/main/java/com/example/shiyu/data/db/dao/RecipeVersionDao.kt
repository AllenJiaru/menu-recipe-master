package com.example.shiyu.data.db.dao

import androidx.room.*
import com.example.shiyu.data.db.entity.RecipeVersionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeVersionDao {
    @Query("SELECT * FROM recipe_version WHERE recipe_id = :recipeId ORDER BY version DESC")
    fun getByRecipeId(recipeId: Long): Flow<List<RecipeVersionEntity>>

    @Query("SELECT * FROM recipe_version WHERE recipe_id = :recipeId ORDER BY version DESC")
    suspend fun getByRecipeIdOnce(recipeId: Long): List<RecipeVersionEntity>

    @Insert
    suspend fun insert(version: RecipeVersionEntity): Long

    @Query("DELETE FROM recipe_version WHERE recipe_id = :recipeId")
    suspend fun deleteByRecipeId(recipeId: Long)
}
