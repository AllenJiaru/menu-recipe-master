package com.example.shiyu.api

import retrofit2.http.*
import okhttp3.ResponseBody

// ===== 通用响应包装 =====
data class ApiResponse<T>(
    val code: Int,
    val message: String,
    val data: T?
)

data class PageResponse<T>(
    val records: List<T>,
    val total: Long,
    val page: Int,
    val size: Int
)

// ===== 登录/用户 =====
data class LoginRequest(val username: String, val password: String)

data class LoginResponseData(
    val token: String,
    val userId: Long?,
    val username: String?,
    val nickname: String?,
    val role: String?
)

data class RegisterRequest(
    val username: String,
    val password: String,
    val nickname: String? = null,
    val role: String? = null,
    val email: String? = null
)

data class ResetPasswordRequest(
    val username: String,
    val newPassword: String
)

data class SendResetCodeRequest(val username: String)
data class VerifyResetPasswordRequest(val username: String, val code: String, val newPassword: String)

data class UserDto(
    val id: Long?,
    val username: String?,
    val nickname: String?,
    val avatar: String?,
    val role: String?,
    val coupleId: Long?,
    val status: Int?
)

data class UserCreateRequest(
    val username: String,
    val password: String,
    val nickname: String? = null,
    val role: String? = null,
    val status: Int? = 1
)

// ===== 菜谱 =====
data class RecipeDto(
    val id: Long,
    val name: String,
    val categoryId: Long?,
    val type: Int?,
    val description: String?,
    val coverImage: String?,
    val cookingTime: Int?,
    val difficulty: Int?,
    val isFavorite: Int?,
    val orderCount: Int?,
    val status: Int?,
    val calories: Double?,
    val protein: Double?,
    val fat: Double?,
    val carbs: Double?,
    val fiber: Double?,
    val cost: Double?,
    val price: Double?,
    val createTime: String?,
    val materials: List<RecipeMaterialDto>?,
    val steps: List<RecipeStepDto>?
)

data class RecipeMaterialDto(
    val id: Long?,
    val recipeId: Long?,
    val name: String,
    val amount: String?,
    val unit: String?,
    val sortOrder: Int?
)

data class RecipeStepDto(
    val id: Long?,
    val recipeId: Long?,
    val stepNumber: Int?,
    val description: String?,
    val imageUrl: String?
)

data class RecipeCreateRequest(
    val name: String,
    val categoryId: Long? = null,
    val type: Int? = null,
    val description: String? = null,
    val coverImage: String? = null,
    val cookingTime: Int? = null,
    val difficulty: Int? = null,
    val status: Int? = 1,
    val materials: List<RecipeMaterialDto>? = null,
    val steps: List<RecipeStepDto>? = null
)

data class RecipeUpdateRequest(
    val name: String? = null,
    val categoryId: Long? = null,
    val type: Int? = null,
    val description: String? = null,
    val coverImage: String? = null,
    val cookingTime: Int? = null,
    val difficulty: Int? = null,
    val status: Int? = null,
    val materials: List<RecipeMaterialDto>? = null,
    val steps: List<RecipeStepDto>? = null
)

data class RecipeCategoryDto(
    val id: Long?,
    val name: String?,
    val description: String?,
    val sortOrder: Int?,
    val createTime: String?
)

data class CategoryRequest(
    val name: String,
    val description: String? = null,
    val sortOrder: Int? = null
)

// ===== 订单 =====
data class OrderDto(
    val id: Long,
    val recipeId: Long?,
    val recipeName: String?,
    val recipeImage: String?,
    val status: Int?,
    val remark: String?,
    val rejectReason: String?,
    val orderTime: String?,
    val acceptTime: String?,
    val completeTime: String?,
    val userId: Long?
)

data class OrderCreateRequest(
    val coupleId: Long? = null,
    val recipeId: Long,
    val remark: String? = null
)

data class OrderStatsDto(
    val totalOrders: Int?,
    val pendingOrders: Int?,
    val completedOrders: Int?,
    val cancelledOrders: Int?,
    val todayOrders: Int?,
    val averagePerDay: Double?
)

// ===== 文件上传 =====
data class FileUploadResponse(
    val code: Int,
    val message: String,
    val data: String?
)

// ===== 评论 =====
data class CommentDto(
    val id: Long?,
    val recipeId: Long?,
    val userId: Long?,
    val username: String?,
    val nickname: String?,
    val avatar: String?,
    val content: String?,
    val rating: Int?,
    val parentId: Long?,
    val createTime: String?
)

data class CommentRequest(
    val recipeId: Long,
    val content: String,
    val rating: Int? = null,
    val parentId: Long? = null
)

data class CommentStatsDto(
    val totalComments: Int?,
    val averageRating: Double?,
    val ratingDistribution: Map<String, Int>?
)

// ===== 公告 =====
data class NoticeDto(
    val id: Long?,
    val title: String?,
    val content: String?,
    val type: String?,
    val status: Int?,
    val priority: Int?,
    val authorName: String?,
    val publishTime: String?
)

data class NoticeRequest(
    val title: String,
    val content: String,
    val type: String? = "info",
    val priority: Int? = 0
)

// ===== 周菜谱 =====
data class MealPlanDto(
    val id: Long?,
    val userId: Long?,
    val planDate: String?,
    val mealType: String?,
    val recipeId: Long?,
    val recipeName: String?,
    val recipeCover: String?,
    val servings: Int?,
    val note: String?
)

data class MealPlanRequest(
    val planDate: String,
    val mealType: String,
    val recipeId: Long,
    val recipeName: String? = null,
    val recipeCover: String? = null,
    val servings: Int? = null,
    val note: String? = null
)

// ===== 采购清单 =====
data class ShoppingItemDto(
    val id: Long?,
    val listName: String?,
    val ingredientName: String?,
    val quantity: String?,
    val unit: String?,
    val category: String?,
    val isChecked: Boolean?,
    val sourceRecipeId: Long?,
    val sourceRecipeName: String?
)

data class ShoppingItemRequest(
    val listName: String? = null,
    val ingredientName: String,
    val quantity: String? = null,
    val unit: String? = null,
    val category: String? = null,
    val sourceRecipeId: Long? = null,
    val sourceRecipeName: String? = null
)

// ===== 相册 =====
data class GalleryImageDto(
    val id: Long?,
    val imageUrl: String?,
    val thumbnailUrl: String?,
    val description: String?,
    val orderId: Long?,
    val syncId: String?,
    val createTime: String?
)

// ===== 库存 =====
data class InventoryDto(
    val id: Long?,
    val name: String?,
    val category: String?,
    val quantity: Double?,
    val unit: String?,
    val threshold: Double?,
    val lastUpdated: String?
)

data class InventoryRequest(
    val name: String,
    val category: String? = "",
    val quantity: Double? = 0.0,
    val unit: String? = "",
    val threshold: Double? = 0.0
)

// ===== 供应商 =====
data class SupplierDto(
    val id: Long?,
    val name: String?,
    val contact: String?,
    val phone: String?,
    val rating: Int?,
    val category: String?,
    val address: String?,
    val createTime: String?
)

data class SupplierRequest(
    val name: String,
    val contact: String? = "",
    val phone: String? = "",
    val rating: Int? = 3,
    val category: String? = "",
    val address: String? = ""
)

// ===== 菜谱审核 =====
data class ReviewDto(
    val id: Long?,
    val recipeId: Long?,
    val recipeName: String?,
    val status: Int?,
    val reviewer: String?,
    val comment: String?,
    val createTime: String?,
    val reviewTime: String?
)

data class ReviewRequest(
    val recipeId: Long,
    val recipeName: String,
    val status: Int? = 0,
    val comment: String? = ""
)

// ===== 菜谱版本 =====
data class RecipeVersionDto(
    val id: Long?,
    val recipeId: Long?,
    val version: Int?,
    val name: String?,
    val data: String?,
    val createTime: String?
)

// ===== 健康监控 =====
data class HealthDto(
    val status: String?,
    val database: Map<String, Any>?,
    val disk: Map<String, Any>?,
    val cache: Map<String, Any>?
)

// ===== 系统设置 =====
data class SettingDto(
    val key: String?,
    val value: String?,
    val description: String?
)

// ===== 角色 =====
data class RoleDto(
    val id: Long?,
    val name: String?,
    val code: String?,
    val description: String?,
    val status: Int?,
    val createTime: String?
)

data class RoleRequest(
    val name: String,
    val code: String? = null,
    val description: String? = null,
    val status: Int? = 1
)

// ===== 权限 =====
data class PermissionDto(
    val id: Long?,
    val name: String?,
    val code: String?,
    val type: Int?,
    val parentId: Long?,
    val path: String?,
    val icon: String?,
    val sortOrder: Int?,
    val children: List<PermissionDto>?
)

data class PermissionRequest(
    val name: String,
    val code: String? = null,
    val type: Int? = null,
    val parentId: Long? = null,
    val path: String? = null,
    val icon: String? = null,
    val sortOrder: Int? = null
)

// ===== 情侣配置 =====
data class CoupleDto(
    val id: Long?,
    val user1Id: Long?,
    val user2Id: Long?,
    val status: Int?,
    val createTime: String?
)

// ===== 数据导出 =====
data class ExportRequest(
    val format: String? = "csv",
    val startDate: String? = null,
    val endDate: String? = null
)

// ===== 批量操作 =====
data class BatchRequest(
    val ids: List<Long>,
    val status: Int? = null
)

// ===== Dashboard =====
data class DashboardDto(
    val totalRecipes: Int?,
    val totalOrders: Int?,
    val totalUsers: Int?,
    val todayOrders: Int?,
    val pendingOrders: Int?,
    val completedOrders: Int?,
    val recentOrders: List<OrderDto>?
)

// ===== 操作日志 =====
data class OperationLogDto(
    val id: Long?,
    val username: String?,
    val action: String?,
    val target: String?,
    val targetId: Long?,
    val detail: String?,
    val ip: String?,
    val createTime: String?
)

// ===== 统计 =====
data class StatisticsDto(
    val totalOrders: Int?,
    val completedOrders: Int?,
    val cancelledOrders: Int?,
    val averagePerDay: Double?,
    val topRecipes: List<RecipeDto>?,
    val dailyStats: List<Map<String, Any>>?
)

data class AiChatSessionDto(
    val id: Long?,
    val title: String?,
    val provider: String?,
    val model: String?,
    val createTime: String?,
    val updateTime: String?
)

data class AiChatMessageDto(
    val id: Long?,
    val sessionId: Long?,
    val role: String?,
    val content: String?,
    val createTime: String?
)

data class AiResponseDto(
    val content: String?,
    val provider: String?,
    val executionTimeMs: Long?
)

data class AiInfoDto(
    val currentProvider: String?,
    val providers: Map<String, Boolean>?
)

data class AiConfigDto(
    val provider: String?,
    val providers: Map<String, AiProviderConfig>?
)

data class AiProviderConfig(
    val type: String?,
    val apiKey: String?,
    val apiSecret: String?,
    val baseUrl: String?,
    val model: String?,
    val configured: Boolean?
)

data class AiChatRequest(val sessionId: Long?, val message: String)
data class AiCreateSessionRequest(val title: String?)

// ======================================================================
// API 接口
// ======================================================================
interface BackendApi {

    // ===== Auth =====
    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): ApiResponse<LoginResponseData>

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): ApiResponse<LoginResponseData>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body body: ResetPasswordRequest): ApiResponse<Void>

    @POST("auth/send-reset-code")
    suspend fun sendResetCode(@Body body: SendResetCodeRequest): ApiResponse<Void>

    @POST("auth/verify-reset-password")
    suspend fun verifyResetPassword(@Body body: VerifyResetPasswordRequest): ApiResponse<Void>

    @GET("auth/info")
    suspend fun getUserInfo(): ApiResponse<UserDto>

    @PUT("auth/password")
    suspend fun changePassword(@Body body: Map<String, String>): ApiResponse<Void>

    @PUT("auth/profile")
    suspend fun updateProfile(@Body body: Map<String, String>): ApiResponse<UserDto>

    @GET("auth/permissions")
    suspend fun getUserPermissions(): ApiResponse<List<Map<String, Any>>>

    // ===== Recipes =====
    @GET("recipes")
    suspend fun getRecipes(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 100,
        @Query("categoryId") categoryId: Long? = null,
        @Query("keyword") keyword: String? = null,
        @Query("difficulty") difficulty: Int? = null,
        @Query("status") status: Int? = null
    ): ApiResponse<PageResponse<RecipeDto>>

    @GET("recipes/{id}")
    suspend fun getRecipeDetail(@Path("id") id: Long): ApiResponse<RecipeDto>

    @POST("recipes")
    suspend fun createRecipe(@Body body: RecipeCreateRequest): ApiResponse<RecipeDto>

    @PUT("recipes/{id}")
    suspend fun updateRecipe(@Path("id") id: Long, @Body body: RecipeUpdateRequest): ApiResponse<RecipeDto>

    @DELETE("recipes/{id}")
    suspend fun deleteRecipe(@Path("id") id: Long): ApiResponse<Void>

    @PUT("recipes/{id}/favorite")
    suspend fun toggleRecipeFavorite(@Path("id") id: Long): ApiResponse<Void>

    @PUT("recipes/{id}/status")
    suspend fun updateRecipeStatus(@Path("id") id: Long, @Body body: Map<String, Int>): ApiResponse<Void>

    @POST("recipes/batch/delete")
    suspend fun batchDeleteRecipes(@Body body: BatchRequest): ApiResponse<Void>

    @POST("recipes/batch/status")
    suspend fun batchUpdateRecipeStatus(@Body body: BatchRequest): ApiResponse<Void>

    @GET("recipes/categories")
    suspend fun getRecipeCategories(): ApiResponse<List<RecipeCategoryDto>>

    @GET("recipes/random")
    suspend fun getRandomRecipes(
        @Query("count") count: Int = 6
    ): ApiResponse<List<RecipeDto>>

    // ===== Favorites =====
    @POST("favorites/toggle")
    suspend fun toggleFavorite(@Body body: Map<String, Any?>): ApiResponse<Void>

    @GET("favorites/check/{recipeId}")
    suspend fun checkFavorite(@Path("recipeId") recipeId: Long): ApiResponse<Map<String, Boolean>>

    @GET("favorites")
    suspend fun getUserFavorites(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 100
    ): ApiResponse<PageResponse<RecipeDto>>

    // ===== Categories =====
    @GET("categories")
    suspend fun getCategories(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 100
    ): ApiResponse<PageResponse<RecipeCategoryDto>>

    @GET("categories/{id}")
    suspend fun getCategoryById(@Path("id") id: Long): ApiResponse<RecipeCategoryDto>

    @POST("categories")
    suspend fun createCategory(@Body body: CategoryRequest): ApiResponse<Void>

    @PUT("categories/{id}")
    suspend fun updateCategory(@Path("id") id: Long, @Body body: CategoryRequest): ApiResponse<Void>

    @DELETE("categories/{id}")
    suspend fun deleteCategory(@Path("id") id: Long): ApiResponse<Void>

    // ===== Orders =====
    @GET("orders")
    suspend fun getOrders(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 100,
        @Query("status") status: Int? = null,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): ApiResponse<PageResponse<OrderDto>>

    @GET("orders/{id}")
    suspend fun getOrderById(@Path("id") id: Long): ApiResponse<OrderDto>

    @POST("orders")
    suspend fun createOrder(@Body body: OrderCreateRequest): ApiResponse<OrderDto>

    @PUT("orders/{id}/accept")
    suspend fun acceptOrder(@Path("id") id: Long): ApiResponse<Void>

    @PUT("orders/{id}/cooking")
    suspend fun cookingOrder(@Path("id") id: Long): ApiResponse<Void>

    @PUT("orders/{id}/complete")
    suspend fun completeOrder(@Path("id") id: Long): ApiResponse<Void>

    @PUT("orders/{id}/cancel")
    suspend fun cancelOrder(
        @Path("id") id: Long,
        @Body body: Map<String, String> = emptyMap()
    ): ApiResponse<Void>

    @DELETE("orders/{id}")
    suspend fun deleteOrder(@Path("id") id: Long): ApiResponse<Void>

    @POST("orders/batch/delete")
    suspend fun batchDeleteOrders(@Body body: BatchRequest): ApiResponse<Void>

    @GET("orders/stats")
    suspend fun getOrderStats(
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): ApiResponse<OrderStatsDto>

    // ===== Comments =====
    @GET("comments/recipe/{recipeId}")
    suspend fun getComments(
        @Path("recipeId") recipeId: Long,
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 50
    ): ApiResponse<PageResponse<CommentDto>>

    @POST("comments")
    suspend fun addComment(@Body body: CommentRequest): ApiResponse<Void>

    @DELETE("comments/{id}")
    suspend fun deleteComment(@Path("id") id: Long): ApiResponse<Void>

    @GET("comments/stats/{recipeId}")
    suspend fun getCommentStats(@Path("recipeId") recipeId: Long): ApiResponse<CommentStatsDto>

    // ===== Notices =====
    @GET("notices/published")
    suspend fun getPublishedNotices(): ApiResponse<List<NoticeDto>>

    @GET("notices")
    suspend fun getNotices(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 100,
        @Query("type") type: String? = null,
        @Query("status") status: Int? = null
    ): ApiResponse<PageResponse<NoticeDto>>

    @GET("notices/{id}")
    suspend fun getNoticeById(@Path("id") id: Long): ApiResponse<NoticeDto>

    @POST("notices")
    suspend fun createNotice(@Body body: NoticeRequest): ApiResponse<Void>

    @PUT("notices/{id}")
    suspend fun updateNotice(@Path("id") id: Long, @Body body: NoticeRequest): ApiResponse<Void>

    @DELETE("notices/{id}")
    suspend fun deleteNotice(@Path("id") id: Long): ApiResponse<Void>

    @PUT("notices/{id}/publish")
    suspend fun publishNotice(@Path("id") id: Long): ApiResponse<Void>

    // ===== Meal Plans =====
    @GET("meal-plans/weekly")
    suspend fun getWeeklyMealPlans(@Query("startDate") startDate: String): ApiResponse<List<MealPlanDto>>

    @POST("meal-plans")
    suspend fun addMealPlan(@Body body: MealPlanRequest): ApiResponse<Void>

    @DELETE("meal-plans/{id}")
    suspend fun deleteMealPlan(@Path("id") id: Long): ApiResponse<Void>

    @GET("meal-plans/stats")
    suspend fun getMealPlanStats(): ApiResponse<Map<String, Any>>

    // ===== Shopping =====
    @GET("shopping")
    suspend fun getShoppingList(@Query("listName") listName: String = "default"): ApiResponse<List<ShoppingItemDto>>

    @POST("shopping")
    suspend fun addShoppingItem(@Body body: ShoppingItemRequest): ApiResponse<Void>

    @POST("shopping/from-recipe/{recipeId}")
    suspend fun addShoppingFromRecipe(
        @Path("recipeId") recipeId: Long,
        @Query("recipeName") recipeName: String? = null
    ): ApiResponse<Void>

    @PUT("shopping/{id}/toggle")
    suspend fun toggleShoppingItem(@Path("id") id: Long): ApiResponse<Void>

    @DELETE("shopping/{id}")
    suspend fun deleteShoppingItem(@Path("id") id: Long): ApiResponse<Void>

    @DELETE("shopping/clear-checked")
    suspend fun clearCheckedShoppingItems(): ApiResponse<Void>

    @GET("shopping/stats")
    suspend fun getShoppingStats(): ApiResponse<Map<String, Any>>

    // ===== Gallery =====
    @GET("gallery")
    suspend fun getGalleryImages(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 100
    ): ApiResponse<PageResponse<GalleryImageDto>>

    @Multipart
    @POST("gallery")
    suspend fun uploadGalleryImage(
        @Part file: okhttp3.MultipartBody.Part,
        @Part("description") description: okhttp3.RequestBody?,
        @Part("orderId") orderId: okhttp3.RequestBody?,
        @Part("coupleId") coupleId: okhttp3.RequestBody?
    ): ApiResponse<GalleryImageDto>

    @DELETE("gallery/{id}")
    suspend fun deleteGalleryImage(@Path("id") id: Long): ApiResponse<Void>

    @HTTP(method = "DELETE", path = "gallery/batch", hasBody = true)
    suspend fun batchDeleteGalleryImages(@Body ids: List<Long>): ApiResponse<Void>

    // ===== Inventory =====
    @GET("inventory")
    suspend fun getInventory(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 100,
        @Query("keyword") keyword: String? = null,
        @Query("category") category: String? = null
    ): ApiResponse<PageResponse<InventoryDto>>

    @GET("inventory/{id}")
    suspend fun getInventoryById(@Path("id") id: Long): ApiResponse<InventoryDto>

    @POST("inventory")
    suspend fun createInventory(@Body body: InventoryRequest): ApiResponse<Void>

    @PUT("inventory/{id}")
    suspend fun updateInventory(@Path("id") id: Long, @Body body: InventoryRequest): ApiResponse<Void>

    @DELETE("inventory/{id}")
    suspend fun deleteInventory(@Path("id") id: Long): ApiResponse<Void>

    @GET("inventory/low-stock")
    suspend fun getLowStockItems(): ApiResponse<List<InventoryDto>>

    @PUT("inventory/{id}/restock")
    suspend fun restockInventory(@Path("id") id: Long, @Query("quantity") quantity: Double): ApiResponse<Void>

    // ===== Suppliers =====
    @GET("suppliers")
    suspend fun getSuppliers(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 100,
        @Query("keyword") keyword: String? = null,
        @Query("category") category: String? = null
    ): ApiResponse<PageResponse<SupplierDto>>

    @GET("suppliers/{id}")
    suspend fun getSupplierById(@Path("id") id: Long): ApiResponse<SupplierDto>

    @POST("suppliers")
    suspend fun createSupplier(@Body body: SupplierRequest): ApiResponse<Void>

    @PUT("suppliers/{id}")
    suspend fun updateSupplier(@Path("id") id: Long, @Body body: SupplierRequest): ApiResponse<Void>

    @DELETE("suppliers/{id}")
    suspend fun deleteSupplier(@Path("id") id: Long): ApiResponse<Void>

    // ===== Reviews =====
    @GET("reviews")
    suspend fun getReviews(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 100,
        @Query("status") status: String? = null
    ): ApiResponse<PageResponse<ReviewDto>>

    @POST("reviews")
    suspend fun createReview(@Body body: ReviewRequest): ApiResponse<Void>

    @PUT("reviews/{id}")
    suspend fun updateReview(@Path("id") id: Long, @Body body: ReviewRequest): ApiResponse<Void>

    @DELETE("reviews/{id}")
    suspend fun deleteReview(@Path("id") id: Long): ApiResponse<Void>

    @POST("reviews/batch/audit")
    suspend fun batchAuditReviews(@Body body: BatchRequest): ApiResponse<Void>

    @POST("reviews/batch/delete")
    suspend fun batchDeleteReviews(@Body body: BatchRequest): ApiResponse<Void>

    @GET("reviews/pending")
    suspend fun getPendingReviews(): ApiResponse<List<ReviewDto>>

    // ===== Recipe Versions =====
    @GET("recipe-versions/{recipeId}")
    suspend fun getVersionHistory(@Path("recipeId") recipeId: Long): ApiResponse<List<RecipeVersionDto>>

    @GET("recipe-versions/detail/{id}")
    suspend fun getVersionDetail(@Path("id") id: Long): ApiResponse<RecipeVersionDto>

    @POST("recipe-versions/{recipeId}")
    suspend fun saveVersion(@Path("recipeId") recipeId: Long, @Query("changeNote") changeNote: String? = null): ApiResponse<Void>

    @POST("recipe-versions/{id}/restore")
    suspend fun restoreVersion(@Path("id") id: Long): ApiResponse<Void>

    // ===== Health =====
    @GET("health")
    suspend fun getSystemHealth(): ApiResponse<Map<String, Any>>

    @GET("health/database")
    suspend fun getDatabaseHealth(): ApiResponse<Map<String, Any>>

    @GET("health/disk")
    suspend fun getDiskHealth(): ApiResponse<Map<String, Any>>

    @GET("health/cache")
    suspend fun getCacheHealth(): ApiResponse<Map<String, Any>>

    @POST("health/cache/clear")
    suspend fun clearCache(): ApiResponse<Void>

    @POST("health/database/optimize")
    suspend fun optimizeDatabase(): ApiResponse<Void>

    @GET("health/backups")
    suspend fun getBackupList(): ApiResponse<Map<String, Any>>

    @POST("health/backups/create")
    suspend fun createBackup(): ApiResponse<Void>

    @POST("health/backups/{name}/restore")
    suspend fun restoreBackup(@Path("name") name: String): ApiResponse<Void>

    // ===== Settings =====
    @GET("settings")
    suspend fun getSettings(): ApiResponse<List<SettingDto>>

    @PUT("settings/{key}")
    suspend fun updateSetting(@Path("key") key: String, @Body body: Map<String, String>): ApiResponse<Void>

    @POST("settings/clear-data")
    suspend fun clearData(@Body body: Map<String, String>): ApiResponse<Void>

    // ===== Roles =====
    @GET("roles")
    suspend fun getRoles(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 100
    ): ApiResponse<PageResponse<RoleDto>>

    @GET("roles/all")
    suspend fun getAllRoles(): ApiResponse<List<RoleDto>>

    @POST("roles")
    suspend fun createRole(@Body body: RoleRequest): ApiResponse<Void>

    @PUT("roles/{id}")
    suspend fun updateRole(@Path("id") id: Long, @Body body: RoleRequest): ApiResponse<Void>

    @DELETE("roles/{id}")
    suspend fun deleteRole(@Path("id") id: Long): ApiResponse<Void>

    @GET("roles/{id}/permissions")
    suspend fun getRolePermissions(@Path("id") id: Long): ApiResponse<List<PermissionDto>>

    @PUT("roles/{id}/permissions")
    suspend fun assignPermissions(@Path("id") id: Long, @Body body: List<Long>): ApiResponse<Void>

    // ===== Permissions =====
    @GET("permissions/tree")
    suspend fun getPermissionTree(): ApiResponse<List<PermissionDto>>

    @GET("permissions/user")
    suspend fun getUserPermissionList(): ApiResponse<List<PermissionDto>>

    @POST("permissions")
    suspend fun createPermission(@Body body: PermissionRequest): ApiResponse<Void>

    @PUT("permissions/{id}")
    suspend fun updatePermission(@Path("id") id: Long, @Body body: PermissionRequest): ApiResponse<Void>

    @DELETE("permissions/{id}")
    suspend fun deletePermission(@Path("id") id: Long): ApiResponse<Void>

    @GET("permissions/check")
    suspend fun checkPermission(@Query("code") code: String): ApiResponse<Boolean>

    // ===== Couples =====
    @GET("couples")
    suspend fun getCouples(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 10
    ): ApiResponse<Map<String, Any>>

    @GET("couples/{id}")
    suspend fun getCoupleById(@Path("id") id: Long): ApiResponse<CoupleDto>

    @POST("couples")
    suspend fun createCouple(@Body body: CoupleDto): ApiResponse<Void>

    @PUT("couples/{id}")
    suspend fun updateCouple(@Path("id") id: Long, @Body body: CoupleDto): ApiResponse<Void>

    @DELETE("couples/{id}")
    suspend fun deleteCouple(@Path("id") id: Long): ApiResponse<Void>

    // ===== Sync (handled by SyncApi.kt) =====

    @GET("sync/logs")
    suspend fun getSyncLogs(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 20
    ): ApiResponse<Map<String, Any>>

    // ===== Statistics =====
    @GET("statistics/orders")
    suspend fun getOrderStatistics(
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null
    ): ApiResponse<StatisticsDto>

    // ===== Dashboard =====
    @GET("dashboard/overview")
    suspend fun getDashboardOverview(): ApiResponse<DashboardDto>

    // ===== Operation Logs =====
    @GET("logs")
    suspend fun getOperationLogs(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 100,
        @Query("keyword") keyword: String? = null
    ): ApiResponse<PageResponse<OperationLogDto>>

    // ===== Users =====
    @GET("users")
    suspend fun getUsers(
        @Query("page") page: Int = 1,
        @Query("size") size: Int = 100,
        @Query("keyword") keyword: String? = null,
        @Query("role") role: String? = null
    ): ApiResponse<PageResponse<UserDto>>

    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: Long): ApiResponse<UserDto>

    @POST("users")
    suspend fun createUser(@Body body: UserCreateRequest): ApiResponse<UserDto>

    @PUT("users/{id}")
    suspend fun updateUser(@Path("id") id: Long, @Body body: UserDto): ApiResponse<UserDto>

    @DELETE("users/{id}")
    suspend fun deleteUser(@Path("id") id: Long): ApiResponse<Void>

    @PUT("users/{id}/status")
    suspend fun updateUserStatus(@Path("id") id: Long, @Body body: Map<String, Int>): ApiResponse<Void>

    @PUT("users/{id}/role")
    suspend fun updateUserRole(@Path("id") id: Long, @Body body: Map<String, String>): ApiResponse<Void>

    @PUT("users/{id}/reset-password")
    suspend fun resetUserPassword(@Path("id") id: Long): ApiResponse<String>

    @PUT("users/batch-status")
    suspend fun batchUpdateUserStatus(@Body body: Map<String, Any>): ApiResponse<Void>

    @HTTP(method = "DELETE", path = "users/batch-delete", hasBody = true)
    suspend fun batchDeleteUsers(@Body body: Map<String, List<Long>>): ApiResponse<Void>

    // ===== Export =====
    @POST("export/recipes")
    suspend fun exportRecipes(@Body body: ExportRequest): ResponseBody

    @POST("export/orders")
    suspend fun exportOrders(@Body body: ExportRequest): ResponseBody

    // ===== Files =====
    @Multipart
    @POST("files/upload")
    suspend fun uploadFile(@Part file: okhttp3.MultipartBody.Part): ApiResponse<String>

    @Streaming
    @GET
    suspend fun downloadFile(@Url url: String): ResponseBody

    @GET("ai/info")
    suspend fun aiInfo(): ApiResponse<AiInfoDto>

    @GET("ai/config")
    suspend fun aiGetConfig(): ApiResponse<AiConfigDto>

    @POST("ai/config")
    suspend fun aiUpdateConfig(@Body body: Map<String, Any>): ApiResponse<String>

    @POST("ai/test")
    suspend fun aiTestConnection(): ApiResponse<Map<String, Any>>

    @GET("ai/sessions")
    suspend fun aiListSessions(): ApiResponse<List<AiChatSessionDto>>

    @POST("ai/sessions")
    suspend fun aiCreateSession(@Body body: AiCreateSessionRequest = AiCreateSessionRequest(null)): ApiResponse<AiChatSessionDto>

    @DELETE("ai/sessions/{id}")
    suspend fun aiDeleteSession(@Path("id") id: Long): ApiResponse<String>

    @GET("ai/sessions/{id}/messages")
    suspend fun aiListMessages(@Path("id") id: Long): ApiResponse<List<AiChatMessageDto>>

    @POST("ai/chat-session")
    suspend fun aiChatSession(@Body body: AiChatRequest): ApiResponse<AiResponseDto>

    @POST("ai/chat")
    suspend fun aiChat(@Body body: Map<String, String>): ApiResponse<AiResponseDto>

    @GET("ai/recommend")
    suspend fun aiRecommend(@Query("userId") userId: String = "1"): ApiResponse<AiResponseDto>

    @POST("ai/ingredient-to-recipe")
    suspend fun aiIngredientToRecipe(@Body body: Map<String, Any>): ApiResponse<AiResponseDto>

    @POST("ai/nutrition")
    suspend fun aiNutrition(@Body body: Map<String, Any>): ApiResponse<AiResponseDto>

    @POST("ai/meal-plan")
    suspend fun aiMealPlan(@Body body: Map<String, Any>): ApiResponse<AiResponseDto>

    @POST("ai/shopping-list")
    suspend fun aiShoppingList(@Body body: Map<String, String>): ApiResponse<AiResponseDto>

    @POST("ai/leftover")
    suspend fun aiLeftover(@Body body: Map<String, String>): ApiResponse<AiResponseDto>

    @POST("ai/score")
    suspend fun aiScore(@Body body: Map<String, String>): ApiResponse<AiResponseDto>

    @POST("ai/cooking-qa")
    suspend fun aiCookingQA(@Body body: Map<String, String>): ApiResponse<AiResponseDto>

    @GET("ai/health-report")
    suspend fun aiHealthReport(@Query("userId") userId: String = "1"): ApiResponse<AiResponseDto>

    @POST("ai/translate")
    suspend fun aiTranslate(@Body body: Map<String, Any>): ApiResponse<AiResponseDto>

    @POST("ai/semantic-search")
    suspend fun aiSemanticSearch(@Body body: Map<String, String>): ApiResponse<AiResponseDto>

    @POST("ai/recipe-assist")
    suspend fun aiRecipeAssist(@Body body: Map<String, String>): ApiResponse<AiResponseDto>

    @POST("ai/inventory-advisor")
    suspend fun aiInventoryAdvisor(@Body body: Map<String, String>): ApiResponse<AiResponseDto>

    @POST("ai/inventory-predict")
    suspend fun aiInventoryPredict(@Body body: Map<String, String>): ApiResponse<AiResponseDto>

    @POST("ai/scene-menu")
    suspend fun aiSceneMenu(@Body body: Map<String, String>): ApiResponse<AiResponseDto>

    @POST("ai/order-analysis")
    suspend fun aiOrderAnalysis(@Body body: Map<String, String>): ApiResponse<AiResponseDto>

    @POST("ai/data-insight")
    suspend fun aiDataInsight(@Body body: Map<String, String>): ApiResponse<AiResponseDto>

    @POST("ai/copywriting")
    suspend fun aiCopywriting(@Body body: Map<String, String>): ApiResponse<AiResponseDto>

    @POST("ai/smart-schedule")
    suspend fun aiSmartSchedule(@Body body: Map<String, String>): ApiResponse<AiResponseDto>

    @POST("ai/trend-predict")
    suspend fun aiTrendPredict(@Body body: Map<String, String>): ApiResponse<AiResponseDto>

    @POST("ai/menu-analysis")
    suspend fun aiMenuAnalysis(@Body body: Map<String, String>): ApiResponse<AiResponseDto>

    @POST("ai/smart-order")
    suspend fun aiSmartOrder(@Body body: Map<String, String>): ApiResponse<AiResponseDto>

    @POST("ai/user-profile")
    suspend fun aiUserProfile(@Body body: Map<String, String>): ApiResponse<AiResponseDto>
}
