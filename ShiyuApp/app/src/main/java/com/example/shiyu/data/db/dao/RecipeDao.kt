package com.example.shiyu.data.db.dao

import androidx.room.*
import com.example.shiyu.data.db.entity.RecipeEntity
import com.example.shiyu.data.db.entity.RecipeMaterialEntity
import com.example.shiyu.data.db.entity.RecipeStepEntity
import kotlinx.coroutines.flow.Flow

data class RecipeWithDetails(
    @Embedded val recipe: RecipeEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "recipe_id"
    )
    val materials: List<RecipeMaterialEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "recipe_id"
    )
    val steps: List<RecipeStepEntity>
)

@Dao
interface RecipeDao {
    @Transaction
    @Query("SELECT * FROM recipe ORDER BY create_time DESC")
    fun getAllRecipes(): Flow<List<RecipeWithDetails>>

    @Transaction
    @Query("SELECT * FROM recipe ORDER BY create_time DESC")
    suspend fun getAllRecipesOnce(): List<RecipeWithDetails>

    @Transaction
    @Query("SELECT * FROM recipe WHERE type = :type ORDER BY create_time DESC")
    fun getRecipesByType(type: Int): Flow<List<RecipeWithDetails>>

    @Transaction
    @Query("SELECT * FROM recipe WHERE type = :type ORDER BY create_time DESC")
    suspend fun getRecipesByTypeOnce(type: Int): List<RecipeWithDetails>

    @Transaction
    @Query("SELECT * FROM recipe WHERE is_favorite = 1 ORDER BY create_time DESC")
    fun getFavoriteRecipes(): Flow<List<RecipeWithDetails>>

    @Transaction
    @Query("SELECT * FROM recipe WHERE is_favorite = 1 ORDER BY create_time DESC")
    suspend fun getFavoriteRecipesOnce(): List<RecipeWithDetails>

    @Query("SELECT * FROM recipe WHERE id = :id")
    suspend fun getRecipeById(id: Long): RecipeEntity?

    @Query("SELECT * FROM recipe WHERE sync_id = :syncId")
    suspend fun getBySyncId(syncId: Long): RecipeEntity?

    @Transaction
    @Query("SELECT * FROM recipe WHERE id = :id")
    suspend fun getRecipeWithDetailsById(id: Long): RecipeWithDetails?

    @Query("SELECT COUNT(*) FROM recipe")
    fun getRecipeCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM recipe")
    suspend fun getRecipeCountOnce(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipe(recipe: RecipeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterials(materials: List<RecipeMaterialEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSteps(steps: List<RecipeStepEntity>)

    @Update
    suspend fun updateRecipe(recipe: RecipeEntity)

    @Query("DELETE FROM recipe WHERE id = :id")
    suspend fun deleteRecipe(id: Long)

    @Query("UPDATE recipe SET is_favorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFavorite: Int)

    @Query("DELETE FROM recipe_material WHERE recipe_id = :recipeId")
    suspend fun deleteMaterialsByRecipeId(recipeId: Long)

    @Query("DELETE FROM recipe_step WHERE recipe_id = :recipeId")
    suspend fun deleteStepsByRecipeId(recipeId: Long)
}
