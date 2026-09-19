package com.example.shiyu.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "supplier")
data class SupplierEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val contact: String = "",
    val phone: String = "",
    val rating: Int = 3,
    val category: String = "",
    val address: String = "",
    val create_time: Long = System.currentTimeMillis()
)
