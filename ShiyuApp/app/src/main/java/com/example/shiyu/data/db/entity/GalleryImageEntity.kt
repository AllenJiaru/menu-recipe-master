package com.example.shiyu.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "gallery_img",
    foreignKeys = [
        ForeignKey(
            entity = OrderEntity::class,
            parentColumns = ["id"],
            childColumns = ["order_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["order_id"])]
)
data class GalleryImageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val image_path: String,
    val description: String = "",
    val order_id: Long? = null,
    val create_time: Long,
    val sync_id: String? = null,
    val sync_time: Long? = null,
    val image_url: String? = null,
    val backend_id: Long? = null
)
