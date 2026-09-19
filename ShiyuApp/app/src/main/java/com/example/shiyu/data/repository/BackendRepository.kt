package com.example.shiyu.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.shiyu.api.*
import com.example.shiyu.data.db.dao.OrderDao
import com.example.shiyu.data.db.dao.RecipeDao
import com.example.shiyu.data.db.entity.OrderEntity
import com.example.shiyu.data.db.entity.RecipeEntity
import com.example.shiyu.data.db.entity.RecipeMaterialEntity
import com.example.shiyu.data.db.entity.RecipeStepEntity
import com.example.shiyu.util.FileUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import java.io.File
import java.io.IOException
import java.net.URLEncoder
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

private suspend fun <T> safeApiCall(call: suspend () -> T): Result<T> = withContext(Dispatchers.IO) {
    try {
        Result.success(call())
    } catch (e: IOException) {
        Result.failure(Exception("网络连接失败"))
    } catch (e: Exception) {
        Result.failure(Exception("操作失败"))
    }
}

@Singleton
class BackendRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val recipeDao: RecipeDao,
    private val orderDao: OrderDao
) {
    companion object {
        private const val PREFS = "backend_prefs"
        private const val KEY_TOKEN = "token"
        private const val KEY_USERNAME = "username"
        private const val KEY_SERVER = "server_url"
        private const val KEY_ROLE = "role"
        private const val KEY_NICKNAME = "nickname"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_AVATAR = "avatar"
    }

    private val prefs: SharedPreferences
        get() = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    val isLoggedIn: Boolean
        get() = !prefs.getString(KEY_TOKEN, "").isNullOrBlank()

    fun getSavedUsername(): String = prefs.getString(KEY_USERNAME, "") ?: ""

    fun getSavedRole(): String = prefs.getString(KEY_ROLE, "") ?: ""

    fun getSavedNickname(): String = prefs.getString(KEY_NICKNAME, "") ?: ""

    fun getSavedUserId(): Long = prefs.getLong(KEY_USER_ID, 0L)

    fun getSavedAvatar(): String = prefs.getString(KEY_AVATAR, "") ?: ""

    fun getServerUrl(): String = prefs.getString(KEY_SERVER, ApiConfig.BASE_URL) ?: ApiConfig.BASE_URL

    fun saveServerUrl(url: String) {
        prefs.edit().putString(KEY_SERVER, url).apply()
    }

    fun logout() {
        prefs.edit()
            .remove(KEY_TOKEN)
            .remove(KEY_USERNAME)
            .remove(KEY_ROLE)
            .remove(KEY_NICKNAME)
            .remove(KEY_USER_ID)
            .remove(KEY_AVATAR)
            .apply()
        ApiClient.setToken(null)
    }

    private fun saveSession(data: LoginResponseData) {
        prefs.edit()
            .putString(KEY_TOKEN, data.token)
            .putString(KEY_USERNAME, data.username ?: "")
            .putString(KEY_ROLE, data.role ?: "")
            .putString(KEY_NICKNAME, data.nickname ?: "")
            .putLong(KEY_USER_ID, data.userId ?: 0L)
            .apply()
        ApiClient.setToken(data.token)
    }

    /** 头像预览地址（本地文件或后端 URL） */
    fun avatarUrl(avatar: String): String {
        if (avatar.isBlank()) return ""
        if (avatar.startsWith("http") || avatar.startsWith("file://")) return avatar
        // 后端上传路径 /uploads/... 走文件预览接口
        val encoded = java.net.URLEncoder.encode(avatar.trimStart('/'), "UTF-8")
        return "${getServerUrl().trimEnd('/')}/api/files/preview?path=$encoded"
    }

    suspend fun login(username: String, password: String): Result<LoginResponseData> = safeApiCall {
        setupApi()
        val response = ApiClient.backendApi.login(LoginRequest(username, password))
        if (response.code == 200 && response.data != null) {
            saveSession(response.data)
            try {
                val info = ApiClient.backendApi.getUserInfo()
                if (info.code == 200 && info.data != null) {
                    prefs.edit()
                        .putString(KEY_AVATAR, info.data.avatar ?: "")
                        .putString(KEY_NICKNAME, info.data.nickname ?: prefs.getString(KEY_NICKNAME, "") ?: "")
                        .apply()
                }
            } catch (_: Exception) {}
            response.data
        } else {
            throw Exception(response.message)
        }
    }

    suspend fun register(
        username: String,
        password: String,
        nickname: String,
        role: String
    ): Result<LoginResponseData> = safeApiCall {
        setupApi()
        val response = ApiClient.backendApi.register(
            RegisterRequest(username = username, password = password, nickname = nickname, role = role)
        )
        if (response.code == 200 && response.data != null) {
            saveSession(response.data)
            response.data
        } else {
            throw Exception(response.message)
        }
    }

    suspend fun resetPassword(username: String, newPassword: String): Result<String> = safeApiCall {
        setupApi()
        val response = ApiClient.backendApi.resetPassword(
            ResetPasswordRequest(username = username, newPassword = newPassword)
        )
        if (response.code == 200) {
            "密码重置成功，请返回登录"
        } else {
            throw Exception(response.message)
        }
    }

    suspend fun changePassword(oldPassword: String, newPassword: String): Result<String> = safeApiCall {
        setupApi()
        val response = ApiClient.backendApi.changePassword(
            mapOf("oldPassword" to oldPassword, "newPassword" to newPassword)
        )
        if (response.code == 200) {
            "密码修改成功"
        } else {
            throw Exception(response.message)
        }
    }

    suspend fun checkConnection(): Boolean = withContext(Dispatchers.IO) {
        try {
            setupApi()
            val info = ApiClient.backendApi.getUserInfo()
            info.code == 200
        } catch (e: Exception) {
            false
        }
    }

    suspend fun autoLogin(): Boolean = withContext(Dispatchers.IO) {
        try {
            val token = prefs.getString(KEY_TOKEN, null)
            if (token.isNullOrBlank()) return@withContext false
            setupApi()
            // 校验 token 是否仍然有效
            val info = ApiClient.backendApi.getUserInfo()
            if (info.code == 200 && info.data != null) {
                val data = info.data
                prefs.edit()
                    .putString(KEY_ROLE, data.role ?: "")
                    .putString(KEY_NICKNAME, data.nickname ?: "")
                    .putString(KEY_USERNAME, data.username ?: "")
                    .putLong(KEY_USER_ID, data.id ?: 0L)
                    .putString(KEY_AVATAR, data.avatar ?: "")
                    .apply()
                true
            } else {
                // token 失效，清除登录状态
                logout()
                false
            }
        } catch (e: Exception) {
            // 网络异常时保留登录状态，避免离线被登出
            true
        }
    }

    private fun parseDateTime(value: String?): Long {
        if (value.isNullOrBlank()) return System.currentTimeMillis()
        return try {
            LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    private suspend fun downloadImage(relativePath: String): String {
        return withContext(Dispatchers.IO) {
            if (relativePath.isBlank()) return@withContext ""
            try {
                if (relativePath.startsWith("http")) {
                    val url = java.net.URL(relativePath)
                    val fileName = FileUtils.generateFileName(
                        relativePath.substringAfterLast('.', "jpg")
                    )
                    val dest = File(FileUtils.getImagesDir(context), fileName)
                    java.net.HttpURLConnection.setFollowRedirects(true)
                    val conn = url.openConnection() as java.net.HttpURLConnection
                    conn.connectTimeout = 15000
                    conn.readTimeout = 30000
                    conn.inputStream.use { input ->
                        dest.outputStream().use { output -> input.copyTo(output) }
                    }
                    dest.absolutePath
                } else {
                    val base = getServerUrl().trimEnd('/')
                    val encoded = URLEncoder.encode(relativePath.replace("\\", "/"), "UTF-8")
                    val url = "$base/api/files/preview?path=$encoded"
                    val fileName = FileUtils.generateFileName(
                        relativePath.substringAfterLast('.', "jpg")
                    )
                    val dest = File(FileUtils.getImagesDir(context), fileName)
                    java.net.HttpURLConnection.setFollowRedirects(true)
                    val conn = java.net.URL(url).openConnection() as java.net.HttpURLConnection
                    conn.connectTimeout = 15000
                    conn.readTimeout = 30000
                    conn.inputStream.use { input ->
                        dest.outputStream().use { output -> input.copyTo(output) }
                    }
                    if (dest.length() < 500) {
                        dest.delete()
                        ""
                    } else {
                        dest.absolutePath
                    }
                }
            } catch (e: Exception) {
                ""
            }
        }
    }

    private fun ensureToken() {
        val saved = prefs.getString(KEY_TOKEN, null)
        if (!saved.isNullOrBlank()) {
            ApiClient.setToken(saved)
        }
    }

    /** 统一设置服务端地址并恢复 JWT token（避免 App 重启后 token 丢失导致 401） */
    private fun setupApi() {
        val url = getServerUrl()
        ApiClient.setBaseUrl(url)
        ensureToken()
    }

    /** 后端分类ID(11-20) 转换为 App 内部类型(1-10) */
    private fun mapCategoryToType(categoryId: Long?, fallbackType: Int?): Int {
        val cid = categoryId?.toInt() ?: 0
        return when {
            cid in 11..20 -> cid - 10
            cid in 1..10 -> cid
            else -> fallbackType ?: 10
        }
    }

    suspend fun pullRecipes(): Result<String> = safeApiCall {
        setupApi()
        if (!isLoggedIn) throw Exception("未登录，请先登录后再同步")
        val response = ApiClient.backendApi.getRecipes(page = 1, size = 200)
        if (response.code != 200 || response.data == null) {
            val msg = if (response.code == 401) "登录已过期，请重新登录" else response.message
            throw Exception(msg)
        }
        val recipes = response.data.records
        var count = 0
        for (dto in recipes) {
            val existing = recipeDao.getBySyncId(dto.id)
            val imagePath = dto.coverImage?.let { downloadImage(it) } ?: ""

            val materials = (dto.materials ?: emptyList())
                .filter { it.name.isNotBlank() }
                .map { m ->
                    RecipeMaterialEntity(
                        recipe_id = 0,
                        name = m.name,
                        amount = m.amount ?: "",
                        unit = m.unit ?: ""
                    )
                }
            val steps = (dto.steps ?: emptyList())
                .filter { !it.description.isNullOrBlank() }
                .mapIndexed { index, s ->
                    RecipeStepEntity(
                        recipe_id = 0,
                        step_number = s.stepNumber ?: (index + 1),
                        description = s.description ?: ""
                    )
                }

            if (existing == null) {
                val now = System.currentTimeMillis()
                val recipe = RecipeEntity(
                    name = dto.name,
                    type = mapCategoryToType(dto.categoryId, dto.type),
                    description = dto.description ?: "",
                    image_path = imagePath,
                    cooking_time = dto.cookingTime ?: 0,
                    difficulty = dto.difficulty ?: 1,
                    is_favorite = dto.isFavorite ?: 0,
                    status = dto.status ?: 1,
                    create_time = parseDateTime(dto.createTime),
                    update_time = now,
                    sync_id = dto.id
                )
                val recipeId = recipeDao.insertRecipe(recipe)
                if (materials.isNotEmpty()) {
                    recipeDao.insertMaterials(materials.map { it.copy(recipe_id = recipeId) })
                }
                if (steps.isNotEmpty()) {
                    recipeDao.insertSteps(steps.map { it.copy(recipe_id = recipeId) })
                }
                count++
            } else {
                recipeDao.updateRecipe(
                    existing.copy(
                        name = dto.name,
                        type = mapCategoryToType(dto.categoryId, dto.type),
                        description = dto.description ?: existing.description,
                        image_path = imagePath.ifEmpty { existing.image_path },
                        cooking_time = dto.cookingTime ?: existing.cooking_time,
                        difficulty = dto.difficulty ?: existing.difficulty,
                        is_favorite = dto.isFavorite ?: existing.is_favorite,
                        status = dto.status ?: existing.status,
                        update_time = System.currentTimeMillis()
                    )
                )
                recipeDao.deleteMaterialsByRecipeId(existing.id)
                recipeDao.deleteStepsByRecipeId(existing.id)
                if (materials.isNotEmpty()) {
                    recipeDao.insertMaterials(materials.map { it.copy(recipe_id = existing.id) })
                }
                if (steps.isNotEmpty()) {
                    recipeDao.insertSteps(steps.map { it.copy(recipe_id = existing.id) })
                }
            }
        }
        "已同步 $count 道菜谱"
    }

    suspend fun pullOrders(): Result<String> = safeApiCall {
        setupApi()
        if (!isLoggedIn) throw Exception("未登录，请先登录后再同步")
        val response = ApiClient.backendApi.getOrders(page = 1, size = 200)
        if (response.code != 200 || response.data == null) {
            val msg = if (response.code == 401) "登录已过期，请重新登录" else response.message
            throw Exception(msg)
        }
        var count = 0
        for (dto in response.data.records) {
            val existing = orderDao.getBySyncId(dto.id)
            if (existing == null) {
                val order = OrderEntity(
                    recipe_id = dto.recipeId ?: 0L,
                    recipe_name = dto.recipeName ?: "未知菜谱",
                    recipe_image = "",
                    status = dto.status ?: 0,
                    remark = dto.remark ?: "",
                    reject_reason = dto.rejectReason ?: "",
                    order_time = parseDateTime(dto.orderTime),
                    accept_time = dto.acceptTime?.let { parseDateTime(it) },
                    complete_time = dto.completeTime?.let { parseDateTime(it) },
                    sync_id = dto.id,
                    user_id = dto.userId
                )
                orderDao.insertOrder(order)
                count++
            } else {
                val updated = existing.copy(
                    status = dto.status ?: existing.status,
                    remark = dto.remark ?: existing.remark,
                    reject_reason = dto.rejectReason ?: existing.reject_reason,
                    accept_time = dto.acceptTime?.let { parseDateTime(it) } ?: existing.accept_time,
                    complete_time = dto.completeTime?.let { parseDateTime(it) } ?: existing.complete_time,
                    user_id = dto.userId ?: existing.user_id
                )
                if (updated != existing) {
                    orderDao.updateOrder(updated)
                }
            }
        }
        "已同步 $count 个订单"
    }

    suspend fun pushOrder(order: OrderEntity): Long? = withContext(Dispatchers.IO) {
        try {
            setupApi()
            val response = ApiClient.backendApi.createOrder(
                OrderCreateRequest(recipeId = order.recipe_id, remark = order.remark)
            )
            if (response.code == 200 && response.data != null) {
                response.data.id
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun pushOrderStatus(order: OrderEntity): Boolean = withContext(Dispatchers.IO) {
        val syncId = order.sync_id ?: return@withContext false
        try {
            setupApi()
            val response = when (order.status) {
                1 -> ApiClient.backendApi.acceptOrder(syncId)
                2 -> ApiClient.backendApi.cookingOrder(syncId)
                3 -> ApiClient.backendApi.completeOrder(syncId)
                4 -> ApiClient.backendApi.cancelOrder(syncId, mapOf("reason" to order.reject_reason))
                else -> null
            }
            response?.code == 200
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteBackendOrder(order: OrderEntity): Boolean = withContext(Dispatchers.IO) {
        val syncId = order.sync_id ?: return@withContext false
        try {
            setupApi()
            ApiClient.backendApi.deleteOrder(syncId).code == 200
        } catch (e: Exception) {
            false
        }
    }

    suspend fun batchDeleteBackendOrders(orders: List<OrderEntity>): Boolean = withContext(Dispatchers.IO) {
        val ids = orders.mapNotNull { it.sync_id }
        if (ids.isEmpty()) return@withContext false
        try {
            setupApi()
            ApiClient.backendApi.batchDeleteOrders(BatchRequest(ids = ids)).code == 200
        } catch (e: Exception) {
            false
        }
    }

    /** 上传头像，返回后端头像路径 */
    suspend fun uploadAvatar(file: File): String? = withContext(Dispatchers.IO) {
        try {
            setupApi()
            val mediaType = when {
                file.name.endsWith("png", true) -> "image/png"
                file.name.endsWith("webp", true) -> "image/webp"
                else -> "image/jpeg"
            }
            val body = okhttp3.RequestBody.create(mediaType.toMediaTypeOrNull(), file)
            val part = okhttp3.MultipartBody.Part.createFormData("file", file.name, body)
            val response = ApiClient.backendApi.uploadFile(part)
            if (response.code == 200 && !response.data.isNullOrBlank()) {
                response.data
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateProfile(nickname: String, avatar: String): Result<String> = safeApiCall {
        setupApi()
        val response = ApiClient.backendApi.updateProfile(
            mapOf("nickname" to nickname, "avatar" to avatar)
        )
        if (response.code == 200) {
            prefs.edit()
                .putString(KEY_NICKNAME, nickname)
                .putString(KEY_AVATAR, avatar)
                .apply()
            "保存成功"
        } else {
            throw Exception(response.message)
        }
    }

    /** 按服务端 ID 获取单个菜谱详情（含食材步骤），并更新本地数据 */
    suspend fun fetchRecipeDetail(syncId: Long): RecipeDto? = withContext(Dispatchers.IO) {
        try {
            setupApi()
            val response = ApiClient.backendApi.getRecipeDetail(syncId)
            if (response.code == 200 && response.data != null) {
                val dto = response.data
                val local = recipeDao.getBySyncId(syncId)
                if (local != null) {
                    recipeDao.deleteMaterialsByRecipeId(local.id)
                    recipeDao.deleteStepsByRecipeId(local.id)
                    val materials = (dto.materials ?: emptyList())
                        .filter { it.name.isNotBlank() }
                        .map { m ->
                            RecipeMaterialEntity(recipe_id = local.id, name = m.name, amount = m.amount ?: "", unit = m.unit ?: "")
                        }
                    val steps = (dto.steps ?: emptyList())
                        .filter { !it.description.isNullOrBlank() }
                        .mapIndexed { index, s ->
                            RecipeStepEntity(recipe_id = local.id, step_number = s.stepNumber ?: (index + 1), description = s.description ?: "")
                        }
                    if (materials.isNotEmpty()) recipeDao.insertMaterials(materials)
                    if (steps.isNotEmpty()) recipeDao.insertSteps(steps)
                }
                dto
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getComments(recipeId: Long): List<CommentDto> = withContext(Dispatchers.IO) {
        try {
            setupApi()
            val response = ApiClient.backendApi.getComments(recipeId)
            if (response.code == 200 && response.data != null) response.data.records
            else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addComment(recipeId: Long, content: String, rating: Int): Boolean = withContext(Dispatchers.IO) {
        try {
            setupApi()
            val response = ApiClient.backendApi.addComment(
                CommentRequest(recipeId = recipeId, content = content, rating = rating)
            )
            response.code == 200
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteComment(commentId: Long): Boolean = withContext(Dispatchers.IO) {
        try {
            setupApi()
            ApiClient.backendApi.deleteComment(commentId).code == 200
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getNotices(): List<NoticeDto> = withContext(Dispatchers.IO) {
        try {
            setupApi()
            val response = ApiClient.backendApi.getPublishedNotices()
            if (response.code == 200 && response.data != null) response.data
            else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getWeeklyMealPlans(startDate: String): List<MealPlanDto> = withContext(Dispatchers.IO) {
        try {
            setupApi()
            val response = ApiClient.backendApi.getWeeklyMealPlans(startDate)
            if (response.code == 200 && response.data != null) response.data
            else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addMealPlan(
        planDate: String,
        mealType: String,
        recipeId: Long,
        recipeName: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            setupApi()
            val response = ApiClient.backendApi.addMealPlan(
                MealPlanRequest(
                    planDate = planDate,
                    mealType = mealType,
                    recipeId = recipeId,
                    recipeName = recipeName
                )
            )
            response.code == 200
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteMealPlan(id: Long): Boolean = withContext(Dispatchers.IO) {
        try {
            setupApi()
            ApiClient.backendApi.deleteMealPlan(id).code == 200
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getShoppingList(): List<ShoppingItemDto> = withContext(Dispatchers.IO) {
        try {
            setupApi()
            val response = ApiClient.backendApi.getShoppingList()
            if (response.code == 200 && response.data != null) response.data
            else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addShoppingItem(name: String, quantity: String, unit: String): Boolean = withContext(Dispatchers.IO) {
        try {
            setupApi()
            val response = ApiClient.backendApi.addShoppingItem(
                ShoppingItemRequest(ingredientName = name, quantity = quantity, unit = unit)
            )
            response.code == 200
        } catch (e: Exception) {
            false
        }
    }

    suspend fun addShoppingFromRecipe(recipeId: Long, recipeName: String): Boolean = withContext(Dispatchers.IO) {
        try {
            setupApi()
            val response = ApiClient.backendApi.addShoppingFromRecipe(recipeId, recipeName)
            response.code == 200
        } catch (e: Exception) {
            false
        }
    }

    suspend fun toggleShoppingItem(id: Long): Boolean = withContext(Dispatchers.IO) {
        try {
            setupApi()
            ApiClient.backendApi.toggleShoppingItem(id).code == 200
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteShoppingItem(id: Long): Boolean = withContext(Dispatchers.IO) {
        try {
            setupApi()
            ApiClient.backendApi.deleteShoppingItem(id).code == 200
        } catch (e: Exception) {
            false
        }
    }

    suspend fun clearCheckedShoppingItems(): Boolean = withContext(Dispatchers.IO) {
        try {
            setupApi()
            ApiClient.backendApi.clearCheckedShoppingItems().code == 200
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getLowStockItems(): List<InventoryDto> = withContext(Dispatchers.IO) {
        try {
            setupApi()
            val response = ApiClient.backendApi.getLowStockItems()
            if (response.code == 200 && response.data != null) response.data
            else emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun searchRecipes(
        keyword: String? = null,
        categoryId: Long? = null,
        difficulty: Int? = null,
        page: Int = 1,
        size: Int = 200
    ): Result<List<RecipeDto>> = safeApiCall {
        setupApi()
        if (!isLoggedIn) throw Exception("未登录")
        val response = ApiClient.backendApi.getRecipes(
            page = page, size = size,
            keyword = keyword?.takeIf { it.isNotBlank() },
            categoryId = categoryId,
            difficulty = difficulty?.takeIf { it != 0 }
        )
        if (response.code == 200 && response.data != null) {
            response.data.records
        } else {
            throw Exception(response.message)
        }
    }

    suspend fun getRandomRecipes(count: Int = 6): Result<List<RecipeDto>> = safeApiCall {
        setupApi()
        val response = ApiClient.backendApi.getRandomRecipes(count)
        if (response.code == 200 && response.data != null) {
            response.data
        } else {
            throw Exception(response.message)
        }
    }

    suspend fun toggleFavoriteBackend(recipeId: Long): Result<Boolean> = safeApiCall {
        setupApi()
        if (!isLoggedIn) throw Exception("未登录")
        val response = ApiClient.backendApi.toggleFavorite(mapOf("recipeId" to recipeId))
        if (response.code == 200) {
            true
        } else {
            throw Exception(response.message)
        }
    }

    suspend fun checkFavoriteStatus(recipeId: Long): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            setupApi()
            if (!isLoggedIn) return@withContext Result.success(false)
            val response = ApiClient.backendApi.checkFavorite(recipeId)
            if (response.code == 200 && response.data != null) {
                Result.success(response.data["isFavorite"] == true)
            } else {
                Result.success(false)
            }
        } catch (e: Exception) {
            Result.success(false)
        }
    }

    suspend fun createRecipeOnBackend(
        name: String,
        type: Int,
        description: String,
        coverImage: String,
        cookingTime: Int,
        difficulty: Int,
        materials: List<Triple<String, String, String>>,
        steps: List<String>
    ): Result<Long> = safeApiCall {
        setupApi()
        if (!isLoggedIn) throw Exception("未登录")
        val categoryId = type.toLong() + 10
        val backendMaterials = materials.filter { it.first.isNotBlank() }.mapIndexed { index, (n, a, u) ->
            RecipeMaterialDto(id = null, recipeId = null, name = n, amount = a.ifBlank { null }, unit = u.ifBlank { null }, sortOrder = index)
        }
        val backendSteps = steps.filter { it.isNotBlank() }.mapIndexed { index, desc ->
            RecipeStepDto(id = null, recipeId = null, stepNumber = index + 1, description = desc, imageUrl = null)
        }
        val coverPath = coverImage.takeIf { it.isNotBlank() }?.let { path ->
            if (path.startsWith("http")) path else null
        }
        val response = ApiClient.backendApi.createRecipe(
            RecipeCreateRequest(
                name = name,
                categoryId = categoryId,
                type = type,
                description = description.takeIf { it.isNotBlank() },
                coverImage = coverPath,
                cookingTime = cookingTime,
                difficulty = difficulty,
                materials = backendMaterials,
                steps = backendSteps
            )
        )
        if (response.code == 200 && response.data != null) {
            response.data.id
        } else {
            throw Exception(response.message)
        }
    }

    suspend fun updateRecipeOnBackend(
        syncId: Long,
        name: String,
        type: Int,
        description: String,
        coverImage: String,
        cookingTime: Int,
        difficulty: Int,
        materials: List<Triple<String, String, String>>,
        steps: List<String>
    ): Result<Boolean> = safeApiCall {
        setupApi()
        if (!isLoggedIn) throw Exception("未登录")
        val categoryId = type.toLong() + 10
        val backendMaterials = materials.filter { it.first.isNotBlank() }.mapIndexed { index, (n, a, u) ->
            RecipeMaterialDto(id = null, recipeId = syncId, name = n, amount = a.ifBlank { null }, unit = u.ifBlank { null }, sortOrder = index)
        }
        val backendSteps = steps.filter { it.isNotBlank() }.mapIndexed { index, desc ->
            RecipeStepDto(id = null, recipeId = syncId, stepNumber = index + 1, description = desc, imageUrl = null)
        }
        val coverPath = coverImage.takeIf { it.isNotBlank() }?.let { path ->
            if (path.startsWith("http")) path else null
        }
        val response = ApiClient.backendApi.updateRecipe(
            syncId,
            RecipeUpdateRequest(
                name = name,
                categoryId = categoryId,
                type = type,
                description = description.takeIf { it.isNotBlank() },
                coverImage = coverPath,
                cookingTime = cookingTime,
                difficulty = difficulty,
                materials = backendMaterials,
                steps = backendSteps
            )
        )
        if (response.code == 200) {
            true
        } else {
            throw Exception(response.message)
        }
    }

    suspend fun syncRecipeToLocal(dto: RecipeDto): Long = withContext(Dispatchers.IO) {
        val existing = recipeDao.getBySyncId(dto.id)
        val imagePath = dto.coverImage?.let { downloadImage(it) } ?: ""
        val materials = (dto.materials ?: emptyList())
            .filter { it.name.isNotBlank() }
            .map { m ->
                RecipeMaterialEntity(recipe_id = 0, name = m.name, amount = m.amount ?: "", unit = m.unit ?: "")
            }
        val steps = (dto.steps ?: emptyList())
            .filter { !it.description.isNullOrBlank() }
            .mapIndexed { index, s ->
                RecipeStepEntity(recipe_id = 0, step_number = s.stepNumber ?: (index + 1), description = s.description ?: "")
            }
        if (existing == null) {
            val now = System.currentTimeMillis()
            val recipe = RecipeEntity(
                name = dto.name,
                type = mapCategoryToType(dto.categoryId, dto.type),
                description = dto.description ?: "",
                image_path = imagePath,
                cooking_time = dto.cookingTime ?: 0,
                difficulty = dto.difficulty ?: 1,
                is_favorite = dto.isFavorite ?: 0,
                status = dto.status ?: 1,
                create_time = parseDateTime(dto.createTime),
                update_time = now,
                sync_id = dto.id
            )
            val recipeId = recipeDao.insertRecipe(recipe)
            if (materials.isNotEmpty()) recipeDao.insertMaterials(materials.map { it.copy(recipe_id = recipeId) })
            if (steps.isNotEmpty()) recipeDao.insertSteps(steps.map { it.copy(recipe_id = recipeId) })
            recipeId
        } else {
            recipeDao.updateRecipe(
                existing.copy(
                    name = dto.name,
                    type = mapCategoryToType(dto.categoryId, dto.type),
                    description = dto.description ?: existing.description,
                    image_path = imagePath.ifEmpty { existing.image_path },
                    cooking_time = dto.cookingTime ?: existing.cooking_time,
                    difficulty = dto.difficulty ?: existing.difficulty,
                    is_favorite = dto.isFavorite ?: existing.is_favorite,
                    status = dto.status ?: existing.status,
                    update_time = System.currentTimeMillis()
                )
            )
            recipeDao.deleteMaterialsByRecipeId(existing.id)
            recipeDao.deleteStepsByRecipeId(existing.id)
            if (materials.isNotEmpty()) recipeDao.insertMaterials(materials.map { it.copy(recipe_id = existing.id) })
            if (steps.isNotEmpty()) recipeDao.insertSteps(steps.map { it.copy(recipe_id = existing.id) })
            existing.id
        }
    }

    suspend fun syncRecipesToLocal(dtos: List<RecipeDto>): Int = withContext(Dispatchers.IO) {
        var count = 0
        for (dto in dtos) {
            syncRecipeToLocal(dto)
            count++
        }
        count
    }

    suspend fun getOrderStats(): OrderStatsDto? = withContext(Dispatchers.IO) {
        try {
            setupApi()
            val response = ApiClient.backendApi.getOrderStats()
            if (response.code == 200) response.data else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getShoppingStats(): Map<String, Any>? = withContext(Dispatchers.IO) {
        try {
            setupApi()
            val response = ApiClient.backendApi.getShoppingStats()
            if (response.code == 200) response.data else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getCommentStats(recipeId: Long): com.example.shiyu.api.CommentStatsDto? = withContext(Dispatchers.IO) {
        try {
            setupApi()
            val response = ApiClient.backendApi.getCommentStats(recipeId)
            if (response.code == 200) response.data else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getMealPlanStats(): Map<String, Any>? = withContext(Dispatchers.IO) {
        try {
            setupApi()
            val response = ApiClient.backendApi.getMealPlanStats()
            if (response.code == 200) response.data else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun syncPullData(lastSyncTime: Long = 0): Result<SyncData?> = withContext(Dispatchers.IO) {
        try {
            setupApi()
            if (!isLoggedIn) return@withContext Result.failure(Exception("未登录，请先登录后再同步"))
            val request = SyncRequest(deviceId = "", coupleId = null, lastSyncTime = lastSyncTime)
            val response = ApiClient.syncApi.pull(request)
            if (response.code == 200) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun syncPushData(
        lastSyncTime: Long = 0,
        recipes: List<RecipeSyncData>? = null,
        orders: List<OrderSyncData>? = null,
        gallery: List<GallerySyncData>? = null
    ): Result<SyncData?> = withContext(Dispatchers.IO) {
        try {
            setupApi()
            if (!isLoggedIn) return@withContext Result.failure(Exception("未登录，请先登录后再同步"))
            val request = SyncRequest(
                deviceId = "",
                coupleId = null,
                lastSyncTime = lastSyncTime,
                recipes = recipes,
                orders = orders,
                gallery = gallery
            )
            val response = ApiClient.syncApi.push(request)
            if (response.code == 200) {
                Result.success(response.data)
            } else {
                Result.failure(Exception(response.message))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
