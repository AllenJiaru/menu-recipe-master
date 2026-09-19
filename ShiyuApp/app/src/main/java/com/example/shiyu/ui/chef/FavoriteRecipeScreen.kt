package com.example.shiyu.ui.chef

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.res.stringResource
import com.example.shiyu.R
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteRecipeScreen(
    onNavigateBack: () -> Unit,
    onNavigateToRecipeDetail: (Long) -> Unit
) {
    val viewModel: FavoriteRecipeViewModel = hiltViewModel()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.my_favorites)) },
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
            when {
                viewModel.isLoading.value -> LoadingIndicator(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
                viewModel.recipes.isEmpty() ->                 EmptyState(
                    icon = "❤️",
                    title = stringResource(R.string.no_favorites),
                    subtitle = stringResource(R.string.go_browse),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                )
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
