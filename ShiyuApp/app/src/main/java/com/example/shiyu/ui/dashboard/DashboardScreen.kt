package com.example.shiyu.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.shiyu.R
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shiyu.api.OrderStatsDto
import com.example.shiyu.data.db.dao.OrderDao
import com.example.shiyu.data.db.dao.RecipeDao
import com.example.shiyu.data.db.entity.OrderEntity
import com.example.shiyu.data.db.entity.RecipeEntity
import com.example.shiyu.data.repository.BackendRepository
import com.example.shiyu.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val recipeDao: RecipeDao,
    private val orderDao: OrderDao,
    private val backendRepository: BackendRepository
) : ViewModel() {
    var recipes = mutableStateListOf<RecipeEntity>()
    var orders = mutableStateListOf<OrderEntity>()
    var totalRecipes = mutableIntStateOf(0)
    var totalOrders = mutableIntStateOf(0)
    var pendingOrders = mutableIntStateOf(0)
    var completedOrders = mutableIntStateOf(0)

    var backendOrderStats = mutableStateOf<OrderStatsDto?>(null)

    init {
        viewModelScope.launch {
            recipes.addAll(recipeDao.getAllRecipesOnce().map { it.recipe })
            orders.addAll(orderDao.getAllOrdersOnce())
            totalRecipes.intValue = recipeDao.getRecipeCountOnce()
            totalOrders.intValue = orders.size
            pendingOrders.intValue = orders.count { it.status == 0 }
            completedOrders.intValue = orders.count { it.status == 3 }
        }
        viewModelScope.launch {
            backendOrderStats.value = backendRepository.getOrderStats()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(onBack: () -> Unit, viewModel: DashboardViewModel = hiltViewModel()) {
    val categoryMap = mapOf(
        1 to stringResource(R.string.category_meat_dish),
        2 to stringResource(R.string.category_vegetable_dish),
        3 to stringResource(R.string.category_soup),
        4 to stringResource(R.string.category_dessert),
        5 to stringResource(R.string.category_steamed),
        6 to stringResource(R.string.category_stewed),
        7 to stringResource(R.string.category_cold_dish),
        8 to stringResource(R.string.category_stir_fry),
        9 to stringResource(R.string.category_braised),
        10 to stringResource(R.string.category_other)
    )
    val categoryCounts = remember(viewModel.recipes) {
        viewModel.recipes.groupBy { it.type }.mapValues { it.value.size }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.data_statistics), fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            // Summary cards
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(stringResource(R.string.total_recipes), "${viewModel.totalRecipes.value}", Primary, Modifier.weight(1f))
                    StatCard(stringResource(R.string.total_orders), "${viewModel.totalOrders.value}", Info, Modifier.weight(1f))
                }
            }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(stringResource(R.string.pending_orders), "${viewModel.pendingOrders.value}", Warning, Modifier.weight(1f))
                    StatCard(stringResource(R.string.completed_orders), "${viewModel.completedOrders.value}", Success, Modifier.weight(1f))
                }
            }
            // Order stats from backend
            viewModel.backendOrderStats.value?.let { stats ->
                item {
                    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                        Column(Modifier.padding(16.dp)) {
                            Text(stringResource(R.string.order_statistics), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(12.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                    Text("${stats.totalOrders ?: 0}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Primary)
                                    Text(stringResource(R.string.total_orders_label), fontSize = 12.sp, color = TextSecondary)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                    Text("${stats.pendingOrders ?: 0}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Warning)
                                    Text(stringResource(R.string.pending_orders_label), fontSize = 12.sp, color = TextSecondary)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                    Text("${stats.completedOrders ?: 0}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Success)
                                    Text(stringResource(R.string.completed_orders_label), fontSize = 12.sp, color = TextSecondary)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                                    Text("${stats.todayOrders ?: 0}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Info)
                                    Text(stringResource(R.string.today_orders), fontSize = 12.sp, color = TextSecondary)
                                }
                            }
                        }
                    }
                }
            }
            // Category chart
            item {
                Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(Modifier.padding(16.dp)) {
                        Text(stringResource(R.string.recipe_category_distribution), fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(12.dp))
                        val maxCount = categoryCounts.values.maxOrNull() ?: 1
                        categoryMap.forEach { (type, name) ->
                            val count = categoryCounts[type] ?: 0
                            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(name, fontSize = 12.sp, color = TextSecondary, modifier = Modifier.width(40.dp))
                                Box(Modifier.weight(1f).height(16.dp)) {
                                    Canvas(Modifier.fillMaxHeight().fillMaxWidth(fraction = if (maxCount > 0) count.toFloat() / maxCount else 0f)) {
                                        drawRoundRect(Primary, cornerRadius = androidx.compose.ui.geometry.CornerRadius(8.dp.toPx()))
                                    }
                                }
                                Text("$count", fontSize = 12.sp, color = TextPrimary, modifier = Modifier.width(30.dp))
                            }
                        }
                    }
                }
            }
            // Recent orders
            item {
                Text(stringResource(R.string.recent_orders), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 4.dp))
            }
            items(viewModel.orders.take(10)) { order ->
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(order.recipe_name, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            val statusTextRes = when(order.status) { 0->R.string.order_status_pending; 1->R.string.order_status_accepted; 2->R.string.order_status_cooking; 3->R.string.order_status_completed; 4->R.string.order_status_cancelled; else->R.string.order_status_unknown }
                            Text(stringResource(statusTextRes), fontSize = 12.sp, color = TextHint)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, color: Color, modifier: Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = color)
            Spacer(Modifier.height(4.dp))
            Text(label, fontSize = 13.sp, color = TextSecondary)
        }
    }
}
