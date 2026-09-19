package com.example.shiyu.data.db.dao

import androidx.room.*
import com.example.shiyu.data.db.entity.RecipeReviewEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeReviewDao {
    @Query("SELECT * FROM recipe_review ORDER BY create_time DESC")
    fun getAll(): Flow<List<RecipeReviewEntity>>

    @Query("SELECT * FROM recipe_review ORDER BY create_time DESC")
    suspend fun getAllOnce(): List<RecipeReviewEntity>

    @Query("SELECT * FROM recipe_review WHERE status = :status ORDER BY create_time DESC")
    suspend fun getByStatus(status: Int): List<RecipeReviewEntity>

    @Insert
    suspend fun insert(review: RecipeReviewEntity): Long

    @Update
    suspend fun update(review: RecipeReviewEntity)

    @Query("DELETE FROM recipe_review WHERE id = :id")
    suspend fun delete(id: Long)
}
