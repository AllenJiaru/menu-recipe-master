package com.example.shiyu.api

import com.example.shiyu.data.db.entity.RecipeEntity
import com.example.shiyu.data.db.entity.OrderEntity
import com.example.shiyu.data.db.entity.GalleryImageEntity
import com.example.shiyu.data.db.entity.CoupleConfigEntity
import retrofit2.http.Body
import retrofit2.http.POST

data class SyncRequest(
    val deviceId: String,
    val coupleId: Long?,
    val lastSyncTime: Long,
    val recipes: List<RecipeSyncData>? = null,
    val orders: List<OrderSyncData>? = null,
    val gallery: List<GallerySyncData>? = null
)

data class RecipeSyncData(val id: Long?, val syncId: String?, val syncTime: Long?)
data class OrderSyncData(val id: Long?, val syncId: String?, val syncTime: Long?)
data class GallerySyncData(val id: Long?, val syncId: String?, val syncTime: Long?)

data class SyncResponse(
    val code: Int,
    val message: String,
    val data: SyncData?
)

data class SyncData(
    val syncTime: Long,
    val recipes: List<RecipeEntity>?,
    val orders: List<OrderEntity>?,
    val gallery: List<GalleryImageEntity>?,
    val coupleConfig: CoupleConfigEntity?
)

interface SyncApi {
    @POST("sync/pull")
    suspend fun pull(@Body request: SyncRequest): SyncResponse

    @POST("sync/push")
    suspend fun push(@Body request: SyncRequest): SyncResponse
}
