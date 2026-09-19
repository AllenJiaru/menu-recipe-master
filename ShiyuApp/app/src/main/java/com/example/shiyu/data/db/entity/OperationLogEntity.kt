package com.example.shiyu.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "operation_log")
data class OperationLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val action: String,
    val target: String = "",
    val target_id: Long = 0,
    val detail: String = "",
    val ip: String = "",
    val create_time: Long = System.currentTimeMillis()
)
