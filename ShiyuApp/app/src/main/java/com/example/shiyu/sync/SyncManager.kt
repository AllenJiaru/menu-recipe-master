package com.example.shiyu.sync

import android.content.Context
import android.provider.Settings
import com.example.shiyu.api.*
import com.example.shiyu.data.db.AppDatabase
import com.example.shiyu.data.db.entity.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class SyncManager(private val context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val syncApi = ApiClient.syncApi

    private fun getDeviceId(): String {
        return Settings.Secure.ANDROID_ID + "_" + UUID.randomUUID().toString().substring(0, 8)
    }

    suspend fun syncAll(token: String): SyncResult = withContext(Dispatchers.IO) {
        try {
            ApiClient.setToken(token)
            val lastSyncTime = getLastSyncTime()

            val pullRequest = SyncRequest(
                deviceId = getDeviceId(),
                coupleId = null,
                lastSyncTime = lastSyncTime
            )
            val pullResponse = syncApi.pull(pullRequest)

            if (pullResponse.code == 200 && pullResponse.data != null) {
                mergeServerData(pullResponse.data)

                val localChanges = collectLocalChanges(lastSyncTime)
                if (localChanges.recipes.isNotEmpty() || localChanges.orders.isNotEmpty() || localChanges.gallery.isNotEmpty()) {
                    val pushRequest = SyncRequest(
                        deviceId = getDeviceId(),
                        coupleId = null,
                        lastSyncTime = lastSyncTime,
                        recipes = localChanges.recipes,
                        orders = localChanges.orders,
                        gallery = localChanges.gallery
                    )
                    syncApi.push(pushRequest)
                }

                saveLastSyncTime(pullResponse.data.syncTime)
                SyncResult(true, "同步成功")
            } else {
                SyncResult(false, pullResponse.message)
            }
        } catch (e: Exception) {
            SyncResult(false, e.message ?: "同步失败")
        }
    }

    suspend fun pullFromServer(): SyncResult = withContext(Dispatchers.IO) {
        try {
            val lastSyncTime = getLastSyncTime()

            val request = SyncRequest(
                deviceId = getDeviceId(),
                coupleId = null,
                lastSyncTime = lastSyncTime
            )
            val response = syncApi.pull(request)

            if (response.code == 200 && response.data != null) {
                mergeServerData(response.data)
                saveLastSyncTime(response.data.syncTime)
                val recipeCount = response.data.recipes?.size ?: 0
                val orderCount = response.data.orders?.size ?: 0
                val galleryCount = response.data.gallery?.size ?: 0
                SyncResult(true, "拉取成功: 菜谱${recipeCount}道, 订单${orderCount}个, 相册${galleryCount}张")
            } else {
                SyncResult(false, response.message)
            }
        } catch (e: Exception) {
            SyncResult(false, e.message ?: "拉取失败")
        }
    }

    suspend fun pushToServer(): SyncResult = withContext(Dispatchers.IO) {
        try {
            val lastSyncTime = getLastSyncTime()
            val unsyncedRecipes = db.recipeDao().getAllRecipesOnce()
                .filter { it.recipe.sync_id == null }
                .map { recipe ->
                    RecipeSyncData(
                        id = recipe.recipe.id,
                        syncId = recipe.recipe.sync_id?.toString(),
                        syncTime = recipe.recipe.create_time
                    )
                }

            val unsyncedOrders = db.orderDao().getAllOrdersOnce()
                .filter { it.sync_id == null }
                .map { order ->
                    OrderSyncData(
                        id = order.id,
                        syncId = order.sync_id?.toString(),
                        syncTime = order.order_time
                    )
                }

            val unsyncedGallery = db.galleryImageDao().getUnsyncedImages()
                .map { image ->
                    val syncId = image.sync_id ?: UUID.randomUUID().toString()
                    if (image.sync_id == null) {
                        db.galleryImageDao().updateSyncInfo(image.id, syncId, System.currentTimeMillis())
                    }
                    GallerySyncData(
                        id = image.id,
                        syncId = syncId,
                        syncTime = image.create_time
                    )
                }

            if (unsyncedRecipes.isEmpty() && unsyncedOrders.isEmpty() && unsyncedGallery.isEmpty()) {
                return@withContext SyncResult(true, "没有需要同步的数据")
            }

            val request = SyncRequest(
                deviceId = getDeviceId(),
                coupleId = null,
                lastSyncTime = lastSyncTime,
                recipes = unsyncedRecipes,
                orders = unsyncedOrders,
                gallery = unsyncedGallery
            )
            val response = syncApi.push(request)

            if (response.code == 200 && response.data != null) {
                saveLastSyncTime(response.data.syncTime)
                SyncResult(true, "推送成功: 菜谱${unsyncedRecipes.size}道, 订单${unsyncedOrders.size}个, 相册${unsyncedGallery.size}张")
            } else {
                SyncResult(false, response.message)
            }
        } catch (e: Exception) {
            SyncResult(false, e.message ?: "推送失败")
        }
    }

    suspend fun fullSync(): SyncResult = withContext(Dispatchers.IO) {
        val pushResult = pushToServer()
        val pullResult = pullFromServer()

        val success = pushResult.success && pullResult.success
        val message = buildString {
            if (pushResult.success) append(pushResult.message) else append("推送失败: ${pushResult.message}")
            append("\n")
            if (pullResult.success) append(pullResult.message) else append("拉取失败: ${pullResult.message}")
        }
        SyncResult(success, message.trim())
    }

    private suspend fun mergeServerData(data: SyncData) {
        data.coupleConfig?.let { config: CoupleConfigEntity ->
            db.coupleConfigDao().insert(config)
        }
        data.recipes?.forEach { recipe ->
            db.recipeDao().insertRecipe(recipe)
        }
        data.orders?.forEach { order ->
            db.orderDao().insertOrder(order)
        }
        data.gallery?.forEach { image ->
            val existing = image.sync_id?.let { db.galleryImageDao().findBySyncId(it) }
            if (existing == null) {
                db.galleryImageDao().insertImage(image)
            }
        }
    }

    private suspend fun collectLocalChanges(lastSyncTime: Long): SyncChanges {
        val unsyncedGallery = db.galleryImageDao().getUnsyncedImages().map { image ->
            val syncId = image.sync_id ?: UUID.randomUUID().toString()
            if (image.sync_id == null) {
                db.galleryImageDao().updateSyncInfo(image.id, syncId, System.currentTimeMillis())
            }
            GallerySyncData(
                id = image.id,
                syncId = syncId,
                syncTime = image.create_time
            )
        }

        return SyncChanges(
            recipes = emptyList(),
            orders = emptyList(),
            gallery = unsyncedGallery
        )
    }

    private fun getLastSyncTime(): Long {
        val prefs = context.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)
        return prefs.getLong("last_sync_time", 0L)
    }

    private fun saveLastSyncTime(time: Long) {
        context.getSharedPreferences("sync_prefs", Context.MODE_PRIVATE)
            .edit().putLong("last_sync_time", time).apply()
    }

    fun getLastSyncTimeFormatted(): String {
        val time = getLastSyncTime()
        if (time == 0L) return "从未同步"
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(time))
    }

    data class SyncResult(val success: Boolean, val message: String)
    data class SyncChanges(
        val recipes: List<RecipeSyncData>,
        val orders: List<OrderSyncData>,
        val gallery: List<GallerySyncData>
    )
}
