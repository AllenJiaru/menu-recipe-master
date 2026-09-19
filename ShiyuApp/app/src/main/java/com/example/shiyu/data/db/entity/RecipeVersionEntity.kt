package com.example.shiyu.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recipe_version")
data class RecipeVersionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val recipe_id: Long,
    val version: Int = 1,
    val name: String,
    val data: String = "",
    val create_time: Long = System.currentTimeMillis()
)
