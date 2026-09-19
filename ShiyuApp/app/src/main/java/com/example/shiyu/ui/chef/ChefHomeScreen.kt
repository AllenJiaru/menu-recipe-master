package com.example.shiyu.ui.chef

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.sp
import com.example.shiyu.data.db.entity.OrderEntity
import com.example.shiyu.data.db.entity.RecipeEntity
import com.example.shiyu.ui.components.EmptyState
import com.example.shiyu.ui.components.LoadingIndicator
import com.example.shiyu.ui.components.OrderCard
import com.example.shiyu.ui.components.RecipeCard
import com.example.shiyu.ui.gallery.GalleryGrid
import com.example.shiyu.ui.gallery.GalleryViewModel
import com.example.shiyu.R
import com.example.shiyu.ui.theme.*
import com.example.shiyu.util.Constants
import com.example.shiyu.util.FileUtils
import com.example.shiyu.util.RecipeTypeMap
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChefHomeScreen(
    onNavigateToAddRecipe: () -> Unit,
    onNavigateToCategory: () -> Unit,
    onNavigateToFavorite: () -> Unit,
    onNavigateToRecipeDetail: (Long) -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToOrderDetail: (Long) -> Unit = {},
    onNavigateToNotificationCenter: () -> Unit = {},
    onNavigateToAiChat: () -> Unit = {},
    onNavigateToAiRecommend: () -> Unit = {}
) {
    val viewModel: ChefHomeViewModel = hiltViewModel()
    var currentTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(stringResource(R.string.recipe_management), stringResource(R.string.order_management), stringResource(R.string.food_gallery))

    // 每次回到页面刷新数据
    LaunchedEffect(Unit) { viewModel.refreshData() }

    // 订单页实时同步：每5秒拉取一次后端订单状态
    LaunchedEffect(currentTab) {
        while (currentTab == 1) {
            kotlinx.coroutines.delay(5000)
            viewModel.refreshFromBackend()
        }
    }

    Scaffold(
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Primary,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.chef_workspace),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = viewModel.nickname.value.ifEmpty { viewModel.username.value.ifEmpty { stringResource(R.string.chef_default_name) } },
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                    Surface(
                        onClick = onNavigateToNotificationCenter,
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Text(
                            "🔔",
                            fontSize = 20.sp,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        onClick = onNavigateToAiChat,
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Text(
                            "🤖",
                            fontSize = 20.sp,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        onClick = onNavigateToSettings,
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Text(
                            "⚙",
                            fontSize = 20.sp,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }
            }
        },
        containerColor = Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    Brush.verticalGradient(
                        listOf(PrimaryLight.copy(alpha = 0.3f), Background),
                        startY = 0f,
                        endY = 200f
                    )
                )
        ) {
            // 订单提醒区域
            if (viewModel.pendingOrderCount.value > 0) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = KawaiiPeach),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🔔", fontSize = 22.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.new_order_alert),
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = stringResource(R.string.pending_orders, viewModel.pendingOrderCount.value),
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Primary
                        ) {
                            Text(
                                text = stringResource(R.string.view),
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }

            // Tab Row - 卡哇伊风格
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp)
                ) {
                    tabs.forEachIndexed { index, title ->
                        val icons = listOf("📋", "📦", "🖼")
                        val isSelected = currentTab == index
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { currentTab = index },
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) Primary else Color.Transparent
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                ) {
                                Text(text = icons[index], fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = title,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) Color.White else TextSecondary
                                )
                            }
                        }
                }
            }
        }

            // Content
            when (currentTab) {
                0 -> RecipeTab(
                    recipes = viewModel.recipes,
                    recipeCount = viewModel.recipeCount.value,
                    isLoading = viewModel.isLoading.value,
                    onAddRecipe = onNavigateToAddRecipe,
                    onCategory = onNavigateToCategory,
                    onFavorite = onNavigateToFavorite,
                    onRecipeClick = onNavigateToRecipeDetail,
                    onSync = { viewModel.syncFromBackend() },
                    syncMessage = viewModel.syncMessage.value,
                    onRefresh = { viewModel.refreshData() }
                )
                1 -> OrderTab(
                    orders = viewModel.orders,
                    pendingCount = viewModel.pendingOrderCount.value,
                    onOrderClick = onNavigateToOrderDetail,
                    onAccept = { viewModel.acceptOrder(it) },
                    onReject = { order, reason -> viewModel.rejectOrder(order, reason) },
                    onStartCooking = { viewModel.startCooking(it) },
                    onComplete = { viewModel.completeOrder(it) },
                    onDelete = { ids -> viewModel.deleteOrders(ids) },
                    onRefresh = { viewModel.refreshData() }
                )
                2 -> GalleryTab()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeTab(
    recipes: List<RecipeEntity>,
    recipeCount: Int,
    isLoading: Boolean,
    onAddRecipe: () -> Unit,
    onCategory: () -> Unit,
    onFavorite: () -> Unit,
    onRecipeClick: (Long) -> Unit,
    onSync: () -> Unit = {},
    syncMessage: String = "",
    onRefresh: () -> Unit = {}
) {
    var searchKey by remember { mutableStateOf("") }
    var selectedType by remember { mutableIntStateOf(0) }
    var selectedDifficulty by remember { mutableIntStateOf(0) }
    var selectedStatus by remember { mutableIntStateOf(-1) }
    var showFilters by remember { mutableStateOf(false) }
    var refreshing by remember { mutableStateOf(false) }

    LaunchedEffect(refreshing) {
        if (refreshing) {
            onRefresh()
            delay(300)
            refreshing = false
        }
    }

    val hasActiveFilter = selectedType != 0 || selectedDifficulty != 0 || selectedStatus != -1

    val filtered = remember(recipes, searchKey, selectedType, selectedDifficulty, selectedStatus) {
        recipes.filter { recipe ->
            val matchType = selectedType == 0 || recipe.type == selectedType
            val matchSearch = searchKey.isBlank() || recipe.name.contains(searchKey.trim()) ||
                    recipe.description.contains(searchKey.trim())
            val matchDifficulty = selectedDifficulty == 0 || recipe.difficulty == selectedDifficulty
            val matchStatus = selectedStatus == -1 || recipe.status == selectedStatus
            matchType && matchSearch && matchDifficulty && matchStatus
        }
    }

    val favoriteCount = recipes.count { it.is_favorite == 1 }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // 统计卡片
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$recipeCount",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Primary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.total_recipes),
                        fontSize = 12.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$favoriteCount",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Accent
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.my_favorites),
                        fontSize = 12.sp,
                        color = TextSecondary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Surface(
                onClick = onAddRecipe,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                color = Primary,
                shadowElevation = 3.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "+",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.new_recipe),
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        if (syncMessage.isNotEmpty()) {
            Text(
                text = syncMessage,
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 8.dp)
            )
        }

        // 搜索框
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = TextHint,
                    modifier = Modifier.padding(start = 12.dp)
                )
                OutlinedTextField(
                    value = searchKey,
                    onValueChange = { searchKey = it },
                    placeholder = { Text(stringResource(R.string.search_hint), fontSize = 14.sp, color = TextHint) },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, color = TextPrimary),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )
            }
        }

        // 操作按钮行
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ActionChip(
                icon = Icons.Default.FilterList,
                label = stringResource(R.string.filter),
                isActive = showFilters || hasActiveFilter,
                onClick = { showFilters = !showFilters }
            )
            ActionChip(
                icon = Icons.Default.Sync,
                label = stringResource(R.string.sync),
                onClick = onSync
            )
            ActionChip(
                icon = Icons.Default.FavoriteBorder,
                label = stringResource(R.string.favorite),
                onClick = onFavorite
            )
            ActionChip(
                icon = Icons.Default.FolderOpen,
                label = stringResource(R.string.category),
                onClick = onCategory
            )
        }

        // 筛选区域（可折叠）
        AnimatedVisibility(visible = showFilters) {
            Column {
                // 难度筛选
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedDifficulty == 0,
                        onClick = { selectedDifficulty = 0 },
                        label = { Text(stringResource(R.string.all_difficulty), fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Primary,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = selectedDifficulty == 1,
                        onClick = { selectedDifficulty = 1 },
                        label = { Text(stringResource(R.string.easy), fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Primary,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = selectedDifficulty == 3,
                        onClick = { selectedDifficulty = 3 },
                        label = { Text(stringResource(R.string.medium), fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Primary,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = selectedDifficulty == 5,
                        onClick = { selectedDifficulty = 5 },
                        label = { Text(stringResource(R.string.hard), fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }

                // 状态筛选
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedStatus == -1,
                        onClick = { selectedStatus = -1 },
                        label = { Text(stringResource(R.string.all_status), fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Primary,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = selectedStatus == 1,
                        onClick = { selectedStatus = 1 },
                        label = { Text(stringResource(R.string.on_shelf), fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Primary,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = selectedStatus == 0,
                        onClick = { selectedStatus = 0 },
                        label = { Text(stringResource(R.string.off_shelf), fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Primary,
                            selectedLabelColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(
                        onClick = {
                            selectedType = 0
                            selectedDifficulty = 0
                            selectedStatus = -1
                            searchKey = ""
                        }
                    ) {
                        Text(stringResource(R.string.reset), fontSize = 12.sp, color = Primary)
                    }
                }

                // 分类筛选
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedType == 0,
                            onClick = { selectedType = 0 },
                            label = { Text(stringResource(R.string.all), fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Primary,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                    items(RecipeTypeMap.entries.size) { index ->
                        val entry = RecipeTypeMap.entries.elementAt(index)
                        FilterChip(
                            selected = selectedType == entry.key,
                            onClick = { selectedType = entry.key },
                    label = { Text(entry.value, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Primary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }
        } // Column
        } // AnimatedVisibility end

        Spacer(modifier = Modifier.height(4.dp))

        // 菜谱列表
        PullToRefreshBox(
            isRefreshing = refreshing,
            onRefresh = { refreshing = true },
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
                    .padding(horizontal = 16.dp)
            ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedType == 0) stringResource(R.string.my_recipes) else (RecipeTypeMap[selectedType] ?: stringResource(R.string.recipe_management)),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = stringResource(R.string.recipe_count, filtered.size),
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            when {
                isLoading -> LoadingIndicator(modifier = Modifier.weight(1f))
                filtered.isEmpty() -> EmptyState(
                    icon = if (searchKey.isBlank() && selectedType == 0) "📁" else "🔍",
                    title = if (searchKey.isBlank() && selectedType == 0) stringResource(R.string.no_recipes_yet) else stringResource(R.string.no_matching_recipes),
                    subtitle = if (searchKey.isBlank() && selectedType == 0) stringResource(R.string.click_to_create) else stringResource(R.string.try_other_keywords),
                    modifier = Modifier.weight(1f)
                )
                else -> LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 12.dp)
                ) {
                    items(filtered) { recipe ->
                        RecipeCard(
                            recipe = recipe,
                            onClick = { onRecipeClick(recipe.id) }
                        )
                    }
                }
            }
        }
        }
    }
}

@Composable
fun ActionChip(
    icon: ImageVector,
    label: String,
    isActive: Boolean = false,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = if (isActive) Primary.copy(alpha = 0.12f) else Color.White,
        shadowElevation = if (isActive) 0.dp else 1.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isActive) Primary else TextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = if (isActive) Primary else TextSecondary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderTab(
    orders: List<OrderEntity>,
    pendingCount: Int,
    onOrderClick: (Long) -> Unit,
    onAccept: (OrderEntity) -> Unit,
    onReject: (OrderEntity, String) -> Unit,
    onStartCooking: (OrderEntity) -> Unit,
    onComplete: (OrderEntity) -> Unit,
    onDelete: (List<Long>) -> Unit,
    onRefresh: () -> Unit = {}
) {
    var statusFilter by remember { mutableIntStateOf(-1) }
    var selectionMode by remember { mutableStateOf(false) }
    val selectedIds = remember { mutableStateListOf<Long>() }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var refreshing by remember { mutableStateOf(false) }

    LaunchedEffect(refreshing) {
        if (refreshing) {
            onRefresh()
            delay(300)
            refreshing = false
        }
    }

    val statusTabs = listOf(
        -1 to stringResource(R.string.all),
        Constants.ORDER_STATUS_PENDING to stringResource(R.string.pending),
        Constants.ORDER_STATUS_ACCEPTED to stringResource(R.string.accepted),
        Constants.ORDER_STATUS_COOKING to stringResource(R.string.cooking),
        Constants.ORDER_STATUS_COMPLETED to stringResource(R.string.completed),
        Constants.ORDER_STATUS_CANCELLED to stringResource(R.string.cancelled)
    )

    val filtered = remember(orders, statusFilter) {
        if (statusFilter == -1) orders else orders.filter { it.status == statusFilter }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.delete_order)) },
            text = { Text(stringResource(R.string.delete_order_confirm, selectedIds.size)) },
            confirmButton = {
                TextButton(onClick = {
                    onDelete(selectedIds.toList())
                    selectedIds.clear()
                    selectionMode = false
                    showDeleteConfirm = false
                }) {
                    Text(stringResource(R.string.delete), color = Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.cancel), color = TextSecondary)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        // 统计栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.order_management),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.weight(1f))
            if (pendingCount > 0) {
                Text(
                    text = "🔔 ${stringResource(R.string.pending_orders_badge, pendingCount)}",
                    fontSize = 12.sp,
                    color = ChefColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .background(ChefColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = if (selectionMode) stringResource(R.string.cancel) else stringResource(R.string.multi_select),
                fontSize = 13.sp,
                color = ChefColor,
                modifier = Modifier.clickable {
                    selectionMode = !selectionMode
                    selectedIds.clear()
                }
            )
        }

        // 状态筛选
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(statusTabs.size) { index ->
                val (value, label) = statusTabs[index]
                val count = if (value == -1) orders.size else orders.count { it.status == value }
                FilterChip(
                    selected = statusFilter == value,
                    onClick = { statusFilter = value },
                    label = { Text(if (count > 0) "$label $count" else label, fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ChefColor,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        if (filtered.isEmpty()) {
            EmptyState(
                icon = if (statusFilter == -1) "🕐" else "🔍",
                title = if (statusFilter == -1) stringResource(R.string.no_orders_yet) else stringResource(R.string.no_orders_in_status),
                subtitle = if (statusFilter == -1) stringResource(R.string.wait_for_order) else stringResource(R.string.switch_status),
                modifier = Modifier.weight(1f)
            )
        } else {
            PullToRefreshBox(
                isRefreshing = refreshing,
                onRefresh = { refreshing = true },
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 12.dp)
                ) {
                    items(filtered) { order ->
                        Box {
                            OrderCard(
                                order = order,
                                isChef = true,
                                onClick = {
                                    if (selectionMode) {
                                        if (order.id in selectedIds) selectedIds.remove(order.id)
                                        else selectedIds.add(order.id)
                                    } else {
                                        onOrderClick(order.id)
                                    }
                                },
                                onAccept = { onAccept(order) },
                                onReject = { reason -> onReject(order, reason) },
                                onStartCooking = { onStartCooking(order) },
                                onComplete = { onComplete(order) }
                            )
                            if (selectionMode) {
                                Checkbox(
                                    checked = order.id in selectedIds,
                                    onCheckedChange = { checked ->
                                        if (checked) selectedIds.add(order.id)
                                        else selectedIds.remove(order.id)
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(8.dp),
                                    colors = CheckboxDefaults.colors(checkedColor = ChefColor)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 多选底部操作栏
        if (selectionMode && selectedIds.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.selected_count, selectedIds.size),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = { showDeleteConfirm = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Error),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp)
                    ) {
                        Text(stringResource(R.string.delete), fontSize = 14.sp, color = Color.White)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryTab() {
    val viewModel: GalleryViewModel = hiltViewModel()
    val context = LocalContext.current
    var refreshing by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        uris.forEach { uri ->
            val savedPath = FileUtils.saveImage(context, uri)
            if (savedPath != null) {
                viewModel.addImage(savedPath)
            }
        }
        if (uris.isNotEmpty()) {
            viewModel.loadImages()
        }
    }

    LaunchedEffect(Unit) { viewModel.loadImages() }

    LaunchedEffect(refreshing) {
        if (refreshing) {
            viewModel.loadImages()
            delay(300)
            refreshing = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.food_gallery),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(R.string.image_count, viewModel.images.size),
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        PullToRefreshBox(
            isRefreshing = refreshing,
            onRefresh = { refreshing = true },
            modifier = Modifier.fillMaxSize()
        ) {
            GalleryGrid(
                images = viewModel.images,
                isLoading = viewModel.isLoading.value,
                onAddClick = { imagePickerLauncher.launch("image/*") },
                onDelete = { viewModel.deleteImage(it) },
            modifier = Modifier.fillMaxSize()
        )
        }
    }
}
