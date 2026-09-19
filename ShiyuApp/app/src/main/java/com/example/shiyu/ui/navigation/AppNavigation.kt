package com.example.shiyu.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.shiyu.ui.splash.SplashScreen
import com.example.shiyu.ui.auth.LoginScreen
import com.example.shiyu.ui.auth.RegisterScreen
import com.example.shiyu.ui.auth.ForgotPasswordScreen
import com.example.shiyu.ui.chef.ChefHomeScreen
import com.example.shiyu.ui.chef.AddRecipeScreen
import com.example.shiyu.ui.chef.RecipeCategoryScreen
import com.example.shiyu.ui.chef.FavoriteRecipeScreen
import com.example.shiyu.ui.chef.RecipeDetailScreen
import com.example.shiyu.ui.diner.DinerHomeScreen
import com.example.shiyu.ui.diner.DinerOrderScreen
import com.example.shiyu.ui.diner.OrderDetailScreen
import com.example.shiyu.ui.gallery.GalleryScreen
import com.example.shiyu.ui.settings.SettingsScreen
import com.example.shiyu.ui.profile.ProfileScreen
import com.example.shiyu.ui.notice.NoticeScreen
import com.example.shiyu.ui.notice.NotificationCenterScreen
import com.example.shiyu.ui.mealplan.MealPlanScreen
import com.example.shiyu.ui.shopping.ShoppingListScreen
import com.example.shiyu.ui.dashboard.DashboardScreen
import com.example.shiyu.ui.inventory.InventoryScreen
import com.example.shiyu.ui.supplier.SupplierScreen
import com.example.shiyu.ui.category.CategoryManageScreen
import com.example.shiyu.ui.review.ReviewScreen
import com.example.shiyu.ui.export.DataExportScreen
import com.example.shiyu.ui.logs.OperationLogScreen
import com.example.shiyu.ui.health.SystemHealthScreen
import com.example.shiyu.ui.print.RecipePrintScreen
import com.example.shiyu.ui.version.VersionHistoryScreen
import com.example.shiyu.ui.analysis.RecipeAnalysisScreen
import com.example.shiyu.ui.ai.AiChatScreen
import com.example.shiyu.util.Constants
import com.example.shiyu.util.PermissionManager
import com.example.shiyu.data.repository.BackendRepository
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.example.shiyu.R
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn

@EntryPoint
@InstallIn(SingletonComponent::class)
interface BackendRepositoryEntryPoint {
    fun backendRepository(): BackendRepository
}

object Routes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"
    const val CHEF_HOME = "chef_home"
    const val DINER_HOME = "diner_home"
    const val ADD_RECIPE = "add_recipe?recipeId={recipeId}"
    const val RECIPE_CATEGORY = "recipe_category"
    const val FAVORITE_RECIPE = "favorite_recipe"
    const val RECIPE_DETAIL = "recipe_detail/{recipeId}"
    const val DINER_ORDER = "diner_order/{recipeId}"
    const val ORDER_DETAIL = "order_detail/{orderId}"
    const val GALLERY = "gallery"
    const val SETTINGS = "settings"
    const val PROFILE = "profile"
    const val NOTICE = "notice"
    const val NOTIFICATION_CENTER = "notification_center"
    const val MEAL_PLAN = "meal_plan"
    const val SHOPPING = "shopping"
    const val DASHBOARD = "dashboard"
    const val INVENTORY = "inventory"
    const val SUPPLIER = "supplier"
    const val CATEGORY_MANAGE = "category_manage"
    const val REVIEW = "review"
    const val DATA_EXPORT = "data_export"
    const val OPERATION_LOG = "operation_log"
    const val SYSTEM_HEALTH = "system_health"
    const val RECIPE_PRINT = "recipe_print/{recipeId}"
    const val VERSION_HISTORY = "version_history/{recipeId}"
    const val RECIPE_ANALYSIS = "recipe_analysis/{recipeId}"
    const val AI_CHAT = "ai_chat"
    const val AI_RECOMMEND = "ai_recommend"
    const val AI_MEAL_PLAN = "ai_meal_plan"
    const val AI_NUTRITION = "ai_nutrition"
    const val AI_LEFTOVER = "ai_leftover"

    fun recipeDetail(recipeId: Long) = "recipe_detail/$recipeId"
    fun dinerOrder(recipeId: Long) = "diner_order/$recipeId"
    fun orderDetail(orderId: Long) = "order_detail/$orderId"
    fun addRecipe(recipeId: Long? = null) = if (recipeId != null) "add_recipe?recipeId=$recipeId" else "add_recipe?recipeId=-1"
    fun recipePrint(recipeId: Long) = "recipe_print/$recipeId"
    fun versionHistory(recipeId: Long) = "version_history/$recipeId"
    fun recipeAnalysis(recipeId: Long) = "recipe_analysis/$recipeId"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = androidx.compose.ui.platform.LocalContext.current
    val backendRepo = remember {
        EntryPointAccessors.fromApplication(context.applicationContext, BackendRepositoryEntryPoint::class.java).backendRepository()
    }
    val currentRole = remember { backendRepo.getSavedRole() }

    val navigateToRoleHome: (String) -> Unit = { role ->
        when (role) {
            Constants.ROLE_CHEF -> navController.navigate(Routes.CHEF_HOME) {
                popUpTo(0) { inclusive = true }
            }
            Constants.ROLE_DINER -> navController.navigate(Routes.DINER_HOME) {
                popUpTo(0) { inclusive = true }
            }
            else -> navController.navigate(Routes.CHEF_HOME) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    // 权限守卫：无权限时返回上一页
    fun requirePermission(feature: String, onGranted: () -> Unit) {
        if (PermissionManager.hasPermission(currentRole, feature)) {
            onGranted()
        }
    }

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> -fullWidth / 3 },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> -fullWidth / 3 },
                animationSpec = tween(300)
            ) + fadeIn(animationSpec = tween(300))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(300)
            ) + fadeOut(animationSpec = tween(300))
        }
    ) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToChefHome = {
                    navController.navigate(Routes.CHEF_HOME) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                },
                onNavigateToDinerHome = {
                    navController.navigate(Routes.DINER_HOME) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = navigateToRoleHome,
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
                onNavigateToForgotPassword = { navController.navigate(Routes.FORGOT_PASSWORD) }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegisterSuccess = navigateToRoleHome,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.CHEF_HOME) {
            ChefHomeScreen(
                onNavigateToAddRecipe = { navController.navigate(Routes.addRecipe(null)) },
                onNavigateToCategory = { navController.navigate(Routes.RECIPE_CATEGORY) },
                onNavigateToFavorite = { navController.navigate(Routes.FAVORITE_RECIPE) },
                onNavigateToRecipeDetail = { recipeId ->
                    navController.navigate(Routes.recipeDetail(recipeId))
                },
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                onNavigateToOrderDetail = { orderId ->
                    navController.navigate(Routes.orderDetail(orderId))
                },
                onNavigateToNotificationCenter = { navController.navigate(Routes.NOTIFICATION_CENTER) },
                onNavigateToAiChat = { navController.navigate(Routes.AI_CHAT) },
                onNavigateToAiRecommend = { navController.navigate(Routes.AI_RECOMMEND) }
            )
        }

        composable(Routes.DINER_HOME) {
            DinerHomeScreen(
                onNavigateToSettings = { navController.navigate(Routes.SETTINGS) },
                onNavigateToOrder = { recipeId ->
                    navController.navigate(Routes.dinerOrder(recipeId))
                },
                onNavigateToOrderDetail = { orderId ->
                    navController.navigate(Routes.orderDetail(orderId))
                },
                onNavigateToRecipeDetail = { recipeId ->
                    navController.navigate(Routes.recipeDetail(recipeId))
                },
                onNavigateToNotificationCenter = { navController.navigate(Routes.NOTIFICATION_CENTER) },
                onNavigateToAiChat = { navController.navigate(Routes.AI_CHAT) },
                onNavigateToAiRecommend = { navController.navigate(Routes.AI_RECOMMEND) }
            )
        }

        composable(
            route = Routes.ADD_RECIPE,
            arguments = listOf(
                navArgument("recipeId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getLong("recipeId") ?: -1L
            AddRecipeScreen(
                recipeId = recipeId.takeIf { it > 0 },
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.RECIPE_CATEGORY) {
            RecipeCategoryScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToRecipeDetail = { recipeId ->
                    navController.navigate(Routes.recipeDetail(recipeId))
                }
            )
        }

        composable(Routes.FAVORITE_RECIPE) {
            FavoriteRecipeScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToRecipeDetail = { recipeId ->
                    navController.navigate(Routes.recipeDetail(recipeId))
                }
            )
        }

        composable(
            route = Routes.RECIPE_DETAIL,
            arguments = listOf(navArgument("recipeId") { type = NavType.LongType })
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getLong("recipeId") ?: 0L
            RecipeDetailScreen(
                recipeId = recipeId,
                onNavigateBack = { navController.popBackStack() },
                onOrderNow = { id ->
                    navController.navigate(Routes.dinerOrder(id))
                },
                onNavigateToEdit = { id ->
                    navController.navigate(Routes.addRecipe(id))
                },
                onNavigateToAnalysis = { id ->
                    navController.navigate(Routes.recipeAnalysis(id))
                }
            )
        }

        composable(
            route = Routes.DINER_ORDER,
            arguments = listOf(navArgument("recipeId") { type = NavType.LongType })
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getLong("recipeId") ?: 0L
            DinerOrderScreen(
                recipeId = recipeId,
                onNavigateBack = { navController.popBackStack() },
                onOrderSuccess = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Routes.ORDER_DETAIL,
            arguments = listOf(navArgument("orderId") { type = NavType.LongType })
        ) { backStackEntry ->
            val orderId = backStackEntry.arguments?.getLong("orderId") ?: 0L
            OrderDetailScreen(
                orderId = orderId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.GALLERY) {
            GalleryScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onLoggedOut = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onNavigateToProfile = { navController.navigate(Routes.PROFILE) },
                onNavigateToNotice = { navController.navigate(Routes.NOTICE) },
                onNavigateToMealPlan = { navController.navigate(Routes.MEAL_PLAN) },
                onNavigateToShopping = { navController.navigate(Routes.SHOPPING) },
                onNavigateToDashboard = { navController.navigate(Routes.DASHBOARD) },
                onNavigateToInventory = { navController.navigate(Routes.INVENTORY) },
                onNavigateToSupplier = { navController.navigate(Routes.SUPPLIER) },
                onNavigateToCategoryManage = { navController.navigate(Routes.CATEGORY_MANAGE) },
                onNavigateToReview = { navController.navigate(Routes.REVIEW) },
                onNavigateToDataExport = { navController.navigate(Routes.DATA_EXPORT) },
                onNavigateToOperationLog = { navController.navigate(Routes.OPERATION_LOG) },
                onNavigateToSystemHealth = { navController.navigate(Routes.SYSTEM_HEALTH) }
            )
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.NOTICE) {
            NoticeScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.NOTIFICATION_CENTER) {
            NotificationCenterScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.MEAL_PLAN) {
            MealPlanScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SHOPPING) {
            ShoppingListScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Routes.DASHBOARD) {
            if (PermissionManager.hasPermission(currentRole, PermissionManager.FEATURE_DASHBOARD)) {
                DashboardScreen(onBack = { navController.popBackStack() })
            } else {
                LaunchedEffect(Unit) { navController.popBackStack() }
            }
        }

        composable(Routes.INVENTORY) {
            if (PermissionManager.hasPermission(currentRole, PermissionManager.FEATURE_INVENTORY)) {
                InventoryScreen(onBack = { navController.popBackStack() })
            } else {
                LaunchedEffect(Unit) { navController.popBackStack() }
            }
        }

        composable(Routes.SUPPLIER) {
            if (PermissionManager.hasPermission(currentRole, PermissionManager.FEATURE_SUPPLIER)) {
                SupplierScreen(onBack = { navController.popBackStack() })
            } else {
                LaunchedEffect(Unit) { navController.popBackStack() }
            }
        }

        composable(Routes.CATEGORY_MANAGE) {
            if (PermissionManager.hasPermission(currentRole, PermissionManager.FEATURE_CATEGORY_MANAGE)) {
                CategoryManageScreen(onBack = { navController.popBackStack() })
            } else {
                LaunchedEffect(Unit) { navController.popBackStack() }
            }
        }

        composable(Routes.REVIEW) {
            if (PermissionManager.hasPermission(currentRole, PermissionManager.FEATURE_REVIEW)) {
                ReviewScreen(onBack = { navController.popBackStack() })
            } else {
                LaunchedEffect(Unit) { navController.popBackStack() }
            }
        }

        composable(Routes.DATA_EXPORT) {
            if (PermissionManager.hasPermission(currentRole, PermissionManager.FEATURE_DATA_EXPORT)) {
                DataExportScreen(onBack = { navController.popBackStack() })
            } else {
                LaunchedEffect(Unit) { navController.popBackStack() }
            }
        }

        composable(Routes.OPERATION_LOG) {
            if (PermissionManager.hasPermission(currentRole, PermissionManager.FEATURE_OPERATION_LOG)) {
                OperationLogScreen(onBack = { navController.popBackStack() })
            } else {
                LaunchedEffect(Unit) { navController.popBackStack() }
            }
        }

        composable(Routes.SYSTEM_HEALTH) {
            if (PermissionManager.hasPermission(currentRole, PermissionManager.FEATURE_SYSTEM_HEALTH)) {
                SystemHealthScreen(onBack = { navController.popBackStack() })
            } else {
                LaunchedEffect(Unit) { navController.popBackStack() }
            }
        }

        composable(
            route = Routes.RECIPE_PRINT,
            arguments = listOf(navArgument("recipeId") { type = NavType.LongType })
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getLong("recipeId") ?: 0L
            RecipePrintScreen(recipeId = recipeId, onBack = { navController.popBackStack() })
        }

        composable(
            route = Routes.VERSION_HISTORY,
            arguments = listOf(navArgument("recipeId") { type = NavType.LongType })
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getLong("recipeId") ?: 0L
            VersionHistoryScreen(recipeId = recipeId, onBack = { navController.popBackStack() })
        }

        composable(
            route = Routes.RECIPE_ANALYSIS,
            arguments = listOf(navArgument("recipeId") { type = NavType.LongType })
        ) { backStackEntry ->
            val recipeId = backStackEntry.arguments?.getLong("recipeId") ?: 0L
            RecipeAnalysisScreen(recipeId = recipeId, onBack = { navController.popBackStack() })
        }

        composable(
            Routes.AI_CHAT,
            enterTransition = { slideInHorizontally(tween(300)) { -it } + fadeIn(tween(300)) },
            exitTransition = { slideOutHorizontally(tween(300)) { -it } + fadeOut(tween(300)) },
            popEnterTransition = { slideInHorizontally(tween(300)) { it } + fadeIn(tween(300)) },
            popExitTransition = { slideOutHorizontally(tween(300)) { it } + fadeOut(tween(300)) }
        ) {
            com.example.shiyu.ui.ai.AiChatScreen(onBack = { navController.popBackStack() })
        }
        composable(
            Routes.AI_RECOMMEND,
            enterTransition = { slideInHorizontally(tween(300)) { -it } + fadeIn(tween(300)) },
            exitTransition = { slideOutHorizontally(tween(300)) { -it } + fadeOut(tween(300)) },
            popEnterTransition = { slideInHorizontally(tween(300)) { it } + fadeIn(tween(300)) },
            popExitTransition = { slideOutHorizontally(tween(300)) { it } + fadeOut(tween(300)) }
        ) {
            com.example.shiyu.ui.ai.AiFeatureScreen(title = stringResource(R.string.ai_recommend_title), featureType = "recommend", onBack = { navController.popBackStack() })
        }
        composable(
            Routes.AI_MEAL_PLAN,
            enterTransition = { slideInHorizontally(tween(300)) { -it } + fadeIn(tween(300)) },
            exitTransition = { slideOutHorizontally(tween(300)) { -it } + fadeOut(tween(300)) },
            popEnterTransition = { slideInHorizontally(tween(300)) { it } + fadeIn(tween(300)) },
            popExitTransition = { slideOutHorizontally(tween(300)) { it } + fadeOut(tween(300)) }
        ) {
            com.example.shiyu.ui.ai.AiFeatureScreen(title = stringResource(R.string.ai_meal_plan_title), featureType = "meal_plan", onBack = { navController.popBackStack() })
        }
        composable(
            Routes.AI_NUTRITION,
            enterTransition = { slideInHorizontally(tween(300)) { -it } + fadeIn(tween(300)) },
            exitTransition = { slideOutHorizontally(tween(300)) { -it } + fadeOut(tween(300)) },
            popEnterTransition = { slideInHorizontally(tween(300)) { it } + fadeIn(tween(300)) },
            popExitTransition = { slideOutHorizontally(tween(300)) { it } + fadeOut(tween(300)) }
        ) {
            com.example.shiyu.ui.ai.AiFeatureScreen(title = stringResource(R.string.ai_nutrition_title), featureType = "nutrition", onBack = { navController.popBackStack() })
        }
        composable(
            Routes.AI_LEFTOVER,
            enterTransition = { slideInHorizontally(tween(300)) { -it } + fadeIn(tween(300)) },
            exitTransition = { slideOutHorizontally(tween(300)) { -it } + fadeOut(tween(300)) },
            popEnterTransition = { slideInHorizontally(tween(300)) { it } + fadeIn(tween(300)) },
            popExitTransition = { slideOutHorizontally(tween(300)) { it } + fadeOut(tween(300)) }
        ) {
            com.example.shiyu.ui.ai.AiFeatureScreen(title = stringResource(R.string.ai_leftover_title), featureType = "leftover", onBack = { navController.popBackStack() })
        }
    }
}
