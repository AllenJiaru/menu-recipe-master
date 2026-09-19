package com.example.shiyu.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipe_review")
data class RecipeReviewEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val recipe_id: Long,
    val recipe_name: String,
    val status: Int = 0, // 0=pending, 1=approved, 2=rejected
    val reviewer: String = "",
    val comment: String = "",
    val create_time: Long = System.currentTimeMillis(),
    val review_time: Long = 0
)
