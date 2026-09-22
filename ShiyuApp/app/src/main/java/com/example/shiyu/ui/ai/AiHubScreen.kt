package com.example.shiyu.ui.ai

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shiyu.R

data class AiFeatureItem(
    val emoji: String,
    val titleResId: Int,
    val route: String,
    val needsInput: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiHubScreen(
    onBack: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToFeature: (String) -> Unit
) {
    val features = listOf(
        AiFeatureItem("\uD83D\uDCAC", R.string.ai_hub_chat, "chat", false),
        AiFeatureItem("\u2B50", R.string.ai_recommend_title, "ai_recommend"),
        AiFeatureItem("\uD83D\uDCCB", R.string.ai_meal_plan_title, "ai_meal_plan"),
        AiFeatureItem("\uD83E\uDD57", R.string.ai_nutrition_title, "ai_nutrition"),
        AiFeatureItem("\uD83C\uDF7D\uFE0F", R.string.ai_scene_menu_title, "ai_scene_menu"),
        AiFeatureItem("\uD83D\uDD2C", R.string.ai_order_analysis_title, "ai_order_analysis"),
        AiFeatureItem("\uD83D\uDCC8", R.string.ai_data_insight_title, "ai_data_insight"),
        AiFeatureItem("\uD83D\uDD0D", R.string.ai_semantic_search_title, "ai_semantic_search"),
        AiFeatureItem("\u270F\uFE0F", R.string.ai_recipe_assist_title, "ai_recipe_assist"),
        AiFeatureItem("\uD83D\uDCCB", R.string.ai_inventory_advisor_title, "ai_inventory_advisor"),
        AiFeatureItem("\uD83D\uDD2E", R.string.ai_inventory_predict_title, "ai_inventory_predict"),
        AiFeatureItem("\u270D\uFE0F", R.string.ai_copywriting_title, "ai_copywriting"),
        AiFeatureItem("\uD83D\uDEC1", R.string.ai_smart_schedule_title, "ai_smart_schedule"),
        AiFeatureItem("\uD83D\uDD2E", R.string.ai_trend_predict_title, "ai_trend_predict"),
        AiFeatureItem("\uD83D\uDCCA", R.string.ai_menu_analysis_title, "ai_menu_analysis"),
        AiFeatureItem("\uD83C\uDF7D\uFE0F", R.string.ai_smart_order_title, "ai_smart_order"),
        AiFeatureItem("\uD83D\uDC64", R.string.ai_user_profile_title, "ai_user_profile"),
        AiFeatureItem("\uD83E\uDDC1", R.string.ai_leftover_title, "ai_leftover")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.ai_hub_title), fontSize = 16.sp, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(features) { feature ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .clickable {
                            if (feature.route == "chat") onNavigateToChat()
                            else onNavigateToFeature(feature.route)
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(feature.emoji, fontSize = 28.sp)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = stringResource(feature.titleResId),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}
