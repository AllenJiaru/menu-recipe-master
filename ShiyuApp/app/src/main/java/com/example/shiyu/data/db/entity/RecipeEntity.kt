package com.example.shiyu.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipe")
data class RecipeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: Int,
    val description: String = "",
    val image_path: String = "",
    val cooking_time: Int = 0,
    val difficulty: Int = 1,
    val is_favorite: Int = 0,
    val status: Int = 1,
    val create_time: Long,
    val update_time: Long,
    val sync_id: Long? = null,
    // 营养信息
    val calories: Float = 0f,
    val protein: Float = 0f,
    val fat: Float = 0f,
    val carbs: Float = 0f,
    val fiber: Float = 0f,
    // 成本定价
    val cost: Float = 0f,
    val price: Float = 0f
)
