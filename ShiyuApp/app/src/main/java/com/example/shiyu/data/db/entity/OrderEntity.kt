package com.example.shiyu.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "order_record",
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
data class OrderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val recipe_id: Long,
    val recipe_name: String,
    val recipe_image: String = "",
    val status: Int = 0,
    val remark: String = "",
    val reject_reason: String = "",
    val order_time: Long,
    val accept_time: Long? = null,
    val complete_time: Long? = null,
    val sync_id: Long? = null,
    val user_id: Long? = null
)
