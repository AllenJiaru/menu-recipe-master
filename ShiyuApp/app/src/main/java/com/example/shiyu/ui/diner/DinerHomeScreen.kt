package com.example.shiyu.ui.diner

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
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
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DinerHomeScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToOrder: (Long) -> Unit = {},
    onNavigateToOrderDetail: (Long) -> Unit = {},
    onNavigateToRecipeDetail: (Long) -> Unit = {},
    onNavigateToNotificationCenter: () -> Unit = {},
    onNavigateToAiChat: () -> Unit = {},
    onNavigateToAiRecommend: () -> Unit = {}
) {
    val viewModel: DinerHomeViewModel = hiltViewModel()
    var currentTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(stringResource(R.string.food_browser), stringResource(R.string.my_orders), stringResource(R.string.food_gallery))
    val randomRecipe by viewModel.randomRecipe

    LaunchedEffect(Unit) { viewModel.refreshData() }

    LaunchedEffect(randomRecipe) {
        randomRecipe?.let {
            onNavigateToRecipeDetail(it.id)
            viewModel.clearRandomRecipe()
        }
    }

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
                color = Accent,
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
                            text = stringResource(R.string.food_browser),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = viewModel.nickname.value.ifEmpty { viewModel.username.value.ifEmpty { stringResource(R.string.diner_default_name) } },
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
                        listOf(DinerColor.copy(alpha = 0.15f), Background),
                        startY = 0f,
                        endY = 200f
                    )
                )
        ) {
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
                        val icons = listOf("🍜", "📦", "🖼")
                        val isSelected = currentTab == index
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { currentTab = index },
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) DinerColor else Color.Transparent
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

            when (currentTab) {
                0 -> BrowseTab(
                    recipes = viewModel.recipes,
                    recipeCount = viewModel.recipeCount.value,
                    isLoading = viewModel.isLoading.value,
                    isOffline = viewModel.isOffline.value,
                    onRecipeClick = onNavigateToRecipeDetail,
                    onRandomPick = { viewModel.fetchRandomRecipes() },
                    onSearchBackend = { keyword, categoryId, difficulty ->
                        viewModel.searchRecipesFromBackend(keyword, categoryId, difficulty)
                    },
                    isLoggedIn = viewModel.isLoggedIn,
                    onRefresh = { viewModel.refreshData() }
                )
                1 -> MyOrdersTab(
                    orders = viewModel.myOrders,
                    orderCount = viewModel.orderCount.value,
                    onOrderClick = onNavigateToOrderDetail,
                    onCancel = { order, reason -> viewModel.cancelOrder(order, reason) },
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
fun BrowseTab(
    recipes: List<RecipeEntity>,
    recipeCount: Int,
    isLoading: Boolean,
    isOffline: Boolean = false,
    onRecipeClick: (Long) -> Unit = {},
    onRandomPick: () -> Unit = {},
    onSearchBackend: (String, Long?, Int) -> Unit = { _, _, _ -> },
    isLoggedIn: Boolean = false,
    onRefresh: () -> Unit = {}
) {
    var selectedCategory by remember { mutableIntStateOf(0) }
    var searchKey by remember { mutableStateOf("") }
    var selectedDifficulty by remember { mutableIntStateOf(0) }
    var showFilters by remember { mutableStateOf(false) }
    var refreshing by remember { mutableStateOf(false) }

    LaunchedEffect(refreshing) {
        if (refreshing) {
            onRefresh()
            delay(300)
            refreshing = false
        }
    }

    val hasActiveFilter = selectedCategory != 0 || selectedDifficulty != 0

    val categories = listOf(stringResource(R.string.all), stringResource(R.string.type_meat), stringResource(R.string.type_vegetarian), stringResource(R.string.type_soup), stringResource(R.string.type_dessert), stringResource(R.string.type_steamed), stringResource(R.string.type_stewed), stringResource(R.string.type_cold), stringResource(R.string.type_stir_fry), stringResource(R.string.type_braised), stringResource(R.string.type_other))
    val categoryTypes = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
    val backendCategoryIds = listOf(null, 11L, 12L, 13L, 14L, 15L, 16L, 17L, 18L, 19L, 20L)

    val filtered = remember(recipes, selectedCategory, searchKey, selectedDifficulty) {
        recipes.filter { recipe ->
            val matchType = selectedCategory == 0 || recipe.type == categoryTypes[selectedCategory]
            val matchSearch = searchKey.isBlank() || recipe.name.contains(searchKey.trim())
            val matchDifficulty = selectedDifficulty == 0 || recipe.difficulty == selectedDifficulty
            matchType && matchSearch && matchDifficulty
        }
    }

    LaunchedEffect(searchKey, selectedCategory, selectedDifficulty) {
        if (isLoggedIn && (searchKey.isNotBlank() || selectedCategory != 0 || selectedDifficulty != 0)) {
            delay(500)
            onSearchBackend(searchKey, backendCategoryIds[selectedCategory], selectedDifficulty)
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
                .padding(horizontal = 16.dp)
                .padding(top = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
                OutlinedTextField(
                    value = searchKey,
                    onValueChange = { searchKey = it },
                    placeholder = { Text(stringResource(R.string.search_food), fontSize = 13.sp, color = TextHint) },
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp),
                singleLine = true,
                shape = RoundedCornerShape(22.dp),
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DinerColor,
                    unfocusedBorderColor = BorderColor
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(
                onClick = { showFilters = !showFilters },
                colors = ButtonDefaults.textButtonColors(
                    containerColor = if (showFilters || hasActiveFilter) DinerColor.copy(alpha = 0.1f) else Color.Transparent
                ),
                modifier = Modifier
                    .height(44.dp)
                    .clip(RoundedCornerShape(22.dp))
            ) {
                Text(
                    if (showFilters) "🔽 ${stringResource(R.string.filter)}" else "🔍 ${stringResource(R.string.filter)}",
                    fontSize = 13.sp,
                    color = if (hasActiveFilter) DinerColor else TextSecondary
                )
            }
            Button(
                onClick = onRandomPick,
                modifier = Modifier.height(44.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DinerColor),
                shape = RoundedCornerShape(22.dp),
                contentPadding = PaddingValues(horizontal = 14.dp)
            ) {
                Text("🎲 ${stringResource(R.string.random_pick)}", fontSize = 13.sp, color = Color.White)
            }
        }

        AnimatedVisibility(visible = showFilters) {
            Column {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories.size) { index ->
                        FilterChip(
                            selected = selectedCategory == index,
                            onClick = { selectedCategory = index },
                            label = { Text(categories[index], fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = DinerColor,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = selectedDifficulty == 0,
                        onClick = { selectedDifficulty = 0 },
                        label = { Text(stringResource(R.string.all_difficulty), fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DinerColor,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = selectedDifficulty == 1,
                        onClick = { selectedDifficulty = 1 },
                        label = { Text(stringResource(R.string.easy), fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DinerColor,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = selectedDifficulty == 3,
                        onClick = { selectedDifficulty = 3 },
                        label = { Text(stringResource(R.string.medium), fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DinerColor,
                            selectedLabelColor = Color.White
                        )
                    )
                    FilterChip(
                        selected = selectedDifficulty == 5,
                        onClick = { selectedDifficulty = 5 },
                        label = { Text(stringResource(R.string.hard), fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DinerColor,
                            selectedLabelColor = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(
                        onClick = {
                            selectedCategory = 0
                            selectedDifficulty = 0
                            searchKey = ""
                        }
                    ) {
                        Text(stringResource(R.string.reset), fontSize = 12.sp, color = DinerColor)
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(horizontal = 16.dp)
        ) {
            // Offline banner
            if (isOffline) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Warning.copy(alpha = 0.15f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.WifiOff,
                            contentDescription = null,
                            tint = Warning,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.offline_mode),
                            fontSize = 13.sp,
                            color = Warning,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.chefs_recipes),
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
                    icon = "\u2764",
                    title = if (searchKey.isBlank() && selectedCategory == 0) stringResource(R.string.no_recipes_yet) else stringResource(R.string.no_matching_recipes),
                    subtitle = if (searchKey.isBlank() && selectedCategory == 0) stringResource(R.string.wait_for_chef) else stringResource(R.string.try_other_keywords),
                    modifier = Modifier.weight(1f)
                )
                else -> PullToRefreshBox(
                    isRefreshing = refreshing,
                    onRefresh = { refreshing = true },
                    modifier = Modifier.weight(1f)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyOrdersTab(
    orders: List<OrderEntity>,
    orderCount: Int,
    onOrderClick: (Long) -> Unit,
    onCancel: (OrderEntity, String) -> Unit,
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.my_orders),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = stringResource(R.string.order_count, orderCount),
                fontSize = 12.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = if (selectionMode) stringResource(R.string.cancel) else stringResource(R.string.multi_select),
                fontSize = 13.sp,
                color = Primary,
                modifier = Modifier.clickable {
                    selectionMode = !selectionMode
                    selectedIds.clear()
                }
            )
        }

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
                        selectedContainerColor = DinerColor,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        if (filtered.isEmpty()) {
            EmptyState(
                icon = if (statusFilter == -1) "\uD83D\uDD50" else "🔍",
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
                                isChef = false,
                                onClick = {
                                    if (selectionMode) {
                                        if (order.id in selectedIds) selectedIds.remove(order.id)
                                        else selectedIds.add(order.id)
                                    } else {
                                        onOrderClick(order.id)
                                    }
                                },
                                onCancel = { reason -> onCancel(order, reason) }
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
                                    colors = CheckboxDefaults.colors(checkedColor = DinerColor)
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
private fun GalleryTab() {
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
