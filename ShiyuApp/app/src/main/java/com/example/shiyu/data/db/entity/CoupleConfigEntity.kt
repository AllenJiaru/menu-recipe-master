package com.example.shiyu.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "couple_config")
data class CoupleConfigEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val space_name: String,
    val chef_name: String,
    val diner_name: String,
    val create_time: Long
)
