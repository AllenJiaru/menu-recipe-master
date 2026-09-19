package com.example.shiyu.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inventory")
data class InventoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String = "",
    val quantity: Float = 0f,
    val unit: String = "",
    val threshold: Float = 0f,
    val last_updated: Long = System.currentTimeMillis()
)
