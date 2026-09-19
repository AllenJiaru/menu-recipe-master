package com.example.shiyu.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "recipe_material",
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipe_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["recipe_id"])]
)
data class RecipeMaterialEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val recipe_id: Long,
    val name: String,
    val amount: String = "",
    val unit: String = ""
)
