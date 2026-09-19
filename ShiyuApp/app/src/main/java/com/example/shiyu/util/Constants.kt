package com.example.shiyu.util

object Constants {
    // 应用基础信息
    const val APP_NAME = "食遇-情侣菜谱点餐"
    const val APP_VERSION = "1.0.0"

    // 数据库配置
    const val DB_NAME = "MenuRecipe.db"
    const val DB_VERSION = 8

    // 角色类型
    const val ROLE_CHEF = "chef"
    const val ROLE_DINER = "diner"

    // 订单状态
    const val ORDER_STATUS_PENDING = 0
    const val ORDER_STATUS_ACCEPTED = 1
    const val ORDER_STATUS_COOKING = 2
    const val ORDER_STATUS_COMPLETED = 3
    const val ORDER_STATUS_CANCELLED = 4

    // 菜谱类型
    const val RECIPE_TYPE_MEAT = 1
    const val RECIPE_TYPE_VEGETABLE = 2
    const val RECIPE_TYPE_SOUP = 3
    const val RECIPE_TYPE_DESSERT = 4
    const val RECIPE_TYPE_STEAMED = 5
    const val RECIPE_TYPE_STEWED = 6
    const val RECIPE_TYPE_COLD = 7
    const val RECIPE_TYPE_STIR_FRY = 8
    const val RECIPE_TYPE_BRAISED = 9
    const val RECIPE_TYPE_OTHER = 10

    // 存储键名
    const val PREFS_NAME = "menu_recipe_prefs"
    const val KEY_CURRENT_ROLE = "current_role"

    // 图片相关
    const val IMAGE_QUALITY = 80
    const val MAX_IMAGE_SIZE = 1024 * 1024L

    // 分页配置
    const val PAGE_SIZE = 20
}

// 菜谱类型映射
val RecipeTypeMap = mapOf(
    Constants.RECIPE_TYPE_MEAT to "荤菜",
    Constants.RECIPE_TYPE_VEGETABLE to "素菜",
    Constants.RECIPE_TYPE_SOUP to "汤类",
    Constants.RECIPE_TYPE_DESSERT to "甜点",
    Constants.RECIPE_TYPE_STEAMED to "蒸菜",
    Constants.RECIPE_TYPE_STEWED to "炖菜",
    Constants.RECIPE_TYPE_COLD to "凉菜",
    Constants.RECIPE_TYPE_STIR_FRY to "炒菜",
    Constants.RECIPE_TYPE_BRAISED to "红烧",
    Constants.RECIPE_TYPE_OTHER to "其他"
)

// 订单状态映射
val OrderStatusMap = mapOf(
    Constants.ORDER_STATUS_PENDING to "待处理",
    Constants.ORDER_STATUS_ACCEPTED to "已接受",
    Constants.ORDER_STATUS_COOKING to "制作中",
    Constants.ORDER_STATUS_COMPLETED to "已完成",
    Constants.ORDER_STATUS_CANCELLED to "已取消"
)
