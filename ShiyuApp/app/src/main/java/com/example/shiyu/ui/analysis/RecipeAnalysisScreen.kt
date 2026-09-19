package com.example.shiyu.ui.analysis

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shiyu.data.db.dao.RecipeDao
import com.example.shiyu.data.db.entity.RecipeEntity
import com.example.shiyu.data.db.entity.RecipeMaterialEntity
import com.example.shiyu.data.repository.BackendRepository
import com.example.shiyu.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import androidx.compose.ui.res.stringResource
import com.example.shiyu.R
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@HiltViewModel
class RecipeAnalysisViewModel @Inject constructor(
    private val recipeDao: RecipeDao,
    private val backendRepository: BackendRepository
) : ViewModel() {
    var recipe = mutableStateOf<RecipeEntity?>(null)
    var materials = mutableStateListOf<RecipeMaterialEntity>()
    var loaded = mutableStateOf(false)

    fun getShareUrl(syncId: Long?): String {
        if (syncId == null) return ""
        var url = backendRepository.getServerUrl().trimEnd('/')
        // 如果是模拟器地址，尝试获取真实网络地址
        if (url.contains("10.0.2.2")) {
            try {
                val wifiIp = getDeviceWifiIp()
                if (wifiIp.isNotEmpty()) {
                    url = "http://$wifiIp:8081"
                }
            } catch (_: Exception) {}
        }
        return "$url/share/$syncId"
    }

    private fun getDeviceWifiIp(): String {
        try {
            val interfaces = java.net.NetworkInterface.getNetworkInterfaces()
            while (interfaces.hasMoreElements()) {
                val networkInterface = interfaces.nextElement()
                if (networkInterface.isLoopback || !networkInterface.isUp) continue
                val addresses = networkInterface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val address = addresses.nextElement()
                    if (!address.isLoopbackAddress && address is java.net.Inet4Address) {
                        val ip = address.hostAddress ?: ""
                        if (ip.startsWith("192.168.") || ip.startsWith("10.") || ip.startsWith("172.")) {
                            return ip
                        }
                    }
                }
            }
        } catch (_: Exception) {}
        return ""
    }

    fun load(recipeId: Long) {
        viewModelScope.launch {
            val details = recipeDao.getRecipeWithDetailsById(recipeId)
            recipe.value = details?.recipe
            materials.clear()
            materials.addAll(details?.materials ?: emptyList())
            loaded.value = true
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeAnalysisScreen(
    recipeId: Long,
    onBack: () -> Unit,
    vm: RecipeAnalysisViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    LaunchedEffect(recipeId) { vm.load(recipeId) }

    val recipe = vm.recipe.value
    val materials = vm.materials
    val loaded = vm.loaded.value

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.recipe_analysis), fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    if (recipe != null) {
                        IconButton(onClick = {
                            val shareUrl = vm.getShareUrl(recipe.sync_id)
                            shareAnalysis(context, recipe, materials, shareUrl)
                        }) {
                            Icon(Icons.Filled.Share, contentDescription = stringResource(R.string.share_short))
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Background
    ) { padding ->
        if (!loaded) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        } else if (recipe == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.recipe_not_exists), color = TextHint, fontSize = 14.sp)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 菜谱名称卡片
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            recipe.name,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            com.example.shiyu.util.RecipeTypeMap[recipe.type] ?: "其他",
                            fontSize = 13.sp,
                            color = Primary,
                            modifier = Modifier
                                .background(PrimaryLight.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }

                // ── 营养分析 ──
                SectionCard(stringResource(R.string.nutrition_analysis), Icons.Outlined.Restaurant, Primary) {
                    NutritionBar(stringResource(R.string.nutrition_calories), recipe.calories, "kcal", 500f, Color(0xFFFF6B6B))
                    NutritionBar(stringResource(R.string.nutrition_protein), recipe.protein, "g", 50f, Color(0xFF4ECDC4))
                    NutritionBar(stringResource(R.string.nutrition_fat), recipe.fat, "g", 40f, Color(0xFFFFD93D))
                    NutritionBar(stringResource(R.string.nutrition_carbs), recipe.carbs, "g", 80f, Color(0xFF6BCB77))
                    NutritionBar(stringResource(R.string.nutrition_fiber), recipe.fiber, "g", 15f, Color(0xFF95E1D3))

                    Spacer(Modifier.height(8.dp))
                    // 营养评分
                    val score = calculateNutritionScore(recipe)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.nutrition_score), fontSize = 13.sp, color = TextSecondary)
                        Spacer(Modifier.weight(1f))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = scoreColor(score).copy(alpha = 0.15f)
                        ) {
                            Text(
                                stringResource(scoreTextRes(score)),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = scoreColor(score),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // ── 成本分析 ──
                SectionCard(stringResource(R.string.cost_analysis), Icons.Outlined.Paid, Warning) {
                    CostRow(stringResource(R.string.ingredient_cost), "¥${String.format("%.2f", recipe.cost)}")
                    CostRow(stringResource(R.string.suggested_price), "¥${String.format("%.2f", recipe.price)}")
                    if (recipe.cost > 0 && recipe.price > 0) {
                        val profit = recipe.price - recipe.cost
                        val margin = (profit / recipe.price * 100)
                        HorizontalDivider(color = DividerColor, modifier = Modifier.padding(vertical = 6.dp))
                        CostRow(stringResource(R.string.estimated_profit), "¥${String.format("%.2f", profit)}", Success)
                        CostRow(stringResource(R.string.gross_margin), "${String.format("%.1f", margin)}%", Success)
                    }
                }

                // ── 食材明细 ──
                SectionCard(stringResource(R.string.ingredient_details), Icons.Outlined.ShoppingBasket, Info) {
                    if (materials.isEmpty()) {
                        Text(stringResource(R.string.no_ingredients_info), fontSize = 13.sp, color = TextHint)
                    } else {
                        materials.forEachIndexed { index, m ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Primary)
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(m.name, fontSize = 14.sp, color = TextPrimary, modifier = Modifier.weight(1f))
                                Text(
                                    listOf(m.amount, m.unit).filter { it.isNotBlank() }.joinToString(" "),
                                    fontSize = 14.sp,
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            if (index != materials.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(start = 24.dp),
                                    color = DividerColor
                                )
                            }
                        }
                    }
                }

                // ── 烹饪概况 ──
                SectionCard(stringResource(R.string.cooking_overview), Icons.Outlined.Timer, Color(0xFF722ED1)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        OverviewItem(stringResource(R.string.cooking_time), "${recipe.cooking_time}分钟")
                        OverviewItem(stringResource(R.string.difficulty_level), "难度${recipe.difficulty}")
                        OverviewItem(stringResource(R.string.ingredient_types), "${materials.size}种")
                    }
                }

                // ── 分享按钮 ──
                Button(
                    onClick = {
                        val shareUrl = vm.getShareUrl(recipe.sync_id)
                        shareAnalysis(context, recipe, materials, shareUrl)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Filled.Share, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.share_recipe_analysis), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

// ── 分享功能 ──

private fun shareAnalysis(context: Context, recipe: RecipeEntity, materials: List<RecipeMaterialEntity>, shareUrl: String) {
    val sb = StringBuilder()
    sb.appendLine(context.getString(R.string.share_header))
    sb.appendLine()
    sb.appendLine("【${recipe.name}】")
    sb.appendLine("类型：${com.example.shiyu.util.RecipeTypeMap[recipe.type] ?: "其他"}")
    sb.appendLine("烹饪时间：${recipe.cooking_time}分钟 | 难度：${recipe.difficulty}")
    if (shareUrl.isNotEmpty()) {
        sb.appendLine()
        sb.appendLine("查看完整菜谱：$shareUrl")
    }
    sb.appendLine()
    sb.appendLine(context.getString(R.string.share_footer))

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, sb.toString())
        putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.share_subject, recipe.name))
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_recipe_short)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}

// ── Compose 组件 ──

@Composable
private fun SectionCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, iconTint: Color, content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = iconTint, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text(title, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun NutritionBar(label: String, value: Float, unit: String, max: Float, color: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 13.sp, color = TextSecondary, modifier = Modifier.width(80.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(color.copy(alpha = 0.1f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction = if (max > 0) (value / max).coerceIn(0f, 1f) else 0f)
                    .clip(RoundedCornerShape(6.dp))
                    .background(
                        Brush.horizontalGradient(listOf(color.copy(alpha = 0.6f), color))
                    )
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(
            "${String.format("%.1f", value)}$unit",
            fontSize = 12.sp,
            color = TextPrimary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(70.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun CostRow(label: String, value: String, valueColor: Color = TextPrimary) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 14.sp, color = TextSecondary)
        Spacer(Modifier.weight(1f))
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
private fun OverviewItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Primary)
        Spacer(Modifier.height(2.dp))
        Text(label, fontSize = 12.sp, color = TextHint)
    }
}

// ── 营养评分逻辑 ──

private fun calculateNutritionScore(recipe: RecipeEntity): Int {
    var score = 0
    if (recipe.calories in 100f..500f) score += 25
    else if (recipe.calories > 0) score += 10
    if (recipe.protein > 10f) score += 25
    else if (recipe.protein > 0) score += 10
    if (recipe.fat in 0f..30f) score += 20
    else if (recipe.fat > 0) score += 5
    if (recipe.carbs > 0) score += 15
    if (recipe.fiber > 2f) score += 15
    else if (recipe.fiber > 0) score += 5
    return score.coerceAtMost(100)
}

@androidx.annotation.StringRes
private fun scoreTextRes(score: Int): Int = when {
    score >= 80 -> R.string.score_excellent
    score >= 60 -> R.string.score_good
    score >= 40 -> R.string.score_average
    score > 0 -> R.string.score_needs_improvement
    else -> R.string.no_data
}

private fun scoreColor(score: Int): Color = when {
    score >= 80 -> Success
    score >= 60 -> Info
    score >= 40 -> Warning
    score > 0 -> Color(0xFFFF6B6B)
    else -> TextHint
}
