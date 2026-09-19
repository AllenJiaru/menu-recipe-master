package com.example.shiyu.util

/**
 * 角色权限管理器
 * 定义各角色可访问的功能模块
 */
object PermissionManager {

    // ── 功能模块常量 ──
    const val FEATURE_DASHBOARD = "dashboard"
    const val FEATURE_CATEGORY_MANAGE = "category_manage"
    const val FEATURE_INVENTORY = "inventory"
    const val FEATURE_SUPPLIER = "supplier"
    const val FEATURE_REVIEW = "review"
    const val FEATURE_DATA_EXPORT = "data_export"
    const val FEATURE_OPERATION_LOG = "operation_log"
    const val FEATURE_SYSTEM_HEALTH = "system_health"
    const val FEATURE_RECIPE_CREATE = "recipe_create"
    const val FEATURE_RECIPE_EDIT = "recipe_edit"
    const val FEATURE_ORDER_MANAGE = "order_manage"
    const val FEATURE_RECIPE_BROWSE = "recipe_browse"
    const val FEATURE_ORDER_PLACE = "order_place"
    const val FEATURE_GALLERY = "gallery"
    const val FEATURE_SETTINGS = "settings"
    const val FEATURE_MEAL_PLAN = "meal_plan"
    const val FEATURE_SHOPPING = "shopping"
    const val FEATURE_PROFILE = "profile"
    const val FEATURE_NOTICE = "notice"

    // ── 主厨角色拥有的权限 ──
    private val chefPermissions = setOf(
        FEATURE_DASHBOARD,
        FEATURE_CATEGORY_MANAGE,
        FEATURE_INVENTORY,
        FEATURE_SUPPLIER,
        FEATURE_REVIEW,
        FEATURE_DATA_EXPORT,
        FEATURE_OPERATION_LOG,
        FEATURE_SYSTEM_HEALTH,
        FEATURE_RECIPE_CREATE,
        FEATURE_RECIPE_EDIT,
        FEATURE_ORDER_MANAGE,
        FEATURE_RECIPE_BROWSE,
        FEATURE_ORDER_PLACE,
        FEATURE_GALLERY,
        FEATURE_SETTINGS,
        FEATURE_MEAL_PLAN,
        FEATURE_SHOPPING,
        FEATURE_PROFILE,
        FEATURE_NOTICE
    )

    // ── 食客角色拥有的权限 ──
    private val dinerPermissions = setOf(
        FEATURE_RECIPE_BROWSE,
        FEATURE_ORDER_PLACE,
        FEATURE_GALLERY,
        FEATURE_SETTINGS,
        FEATURE_MEAL_PLAN,
        FEATURE_SHOPPING,
        FEATURE_PROFILE,
        FEATURE_NOTICE
    )

    // ── 管理员角色拥有的权限（全部） ──
    private val adminPermissions = chefPermissions

    /**
     * 获取指定角色的功能权限列表
     */
    fun getPermissions(role: String): Set<String> = when (role) {
        Constants.ROLE_CHEF -> chefPermissions
        Constants.ROLE_DINER -> dinerPermissions
        "admin", "super_admin" -> adminPermissions
        else -> emptySet()
    }

    /**
     * 检查指定角色是否拥有某功能权限
     */
    fun hasPermission(role: String, feature: String): Boolean {
        return getPermissions(role).contains(feature)
    }

    /**
     * 检查当前角色是否为管理层（主厨/管理员）
     */
    fun isManager(role: String): Boolean {
        return role == Constants.ROLE_CHEF || role == "admin" || role == "super_admin"
    }

    /**
     * 检查当前角色是否为食客
     */
    fun isDiner(role: String): Boolean {
        return role == Constants.ROLE_DINER
    }
}
