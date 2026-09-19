package com.example.shiyu.ui.chef

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shiyu.ui.components.EmptyState
import com.example.shiyu.ui.components.LoadingIndicator
import com.example.shiyu.ui.components.RecipeCard
import com.example.shiyu.ui.theme.*
import com.example.shiyu.util.Constants
import com.example.shiyu.util.RecipeTypeMap
import androidx.compose.ui.res.stringResource
import com.example.shiyu.R
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeCategoryScreen(
    onNavigateBack: () -> Unit,
    onNavigateToRecipeDetail: (Long) -> Unit
) {
    val viewModel: RecipeCategoryViewModel = hiltViewModel()
    var selectedType by remember { mutableIntStateOf(Constants.RECIPE_TYPE_MEAT) }

    LaunchedEffect(selectedType) {
        viewModel.loadRecipes(selectedType)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.recipe_categories)) },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text(stringResource(R.string.back), color = TextPrimary)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Background)
        ) {
            // 分类标签
            ScrollableTabRow(
                selectedTabIndex = RecipeTypeMap.keys.indexOf(selectedType),
                containerColor = Color.White,
                edgePadding = 16.dp
            ) {
                RecipeTypeMap.forEach { (type, name) ->
                    Tab(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        text = {
                            Text(
                                text = name,
                                color = if (selectedType == type) Primary else TextSecondary
                            )
                        }
                    )
                }
            }

            // 菜谱列表
            when {
                viewModel.isLoading.value -> LoadingIndicator(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
                viewModel.recipes.isEmpty() -> Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(R.string.no_recipes_in_category),
                        fontSize = 14.sp,
                        color = TextHint
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onNavigateBack,
                        colors = ButtonDefaults.buttonColors(containerColor = Primary)
                    ) {
                        Text(stringResource(R.string.go_add), color = Color.White)
                    }
                }
                else -> LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(viewModel.recipes) { recipe ->
                        RecipeCard(
                            recipe = recipe,
                            onClick = { onNavigateToRecipeDetail(recipe.id) }
                        )
                    }
                }
            }
        }
    }
}
