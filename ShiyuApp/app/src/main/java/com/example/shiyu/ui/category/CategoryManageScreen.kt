package com.example.shiyu.ui.category

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shiyu.ui.theme.*
import androidx.compose.ui.res.stringResource
import com.example.shiyu.R

private data class CategoryItem(val id: Int, val name: String, val icon: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryManageScreen(onBack: () -> Unit) {
    val defaultCategories = listOf(
        CategoryItem(1, stringResource(R.string.type_meat), "🥩"), CategoryItem(2, stringResource(R.string.type_vegetarian), "🥬"), CategoryItem(3, stringResource(R.string.type_soup), "🍲"),
        CategoryItem(4, stringResource(R.string.type_dessert), "🍰"), CategoryItem(5, stringResource(R.string.type_steamed), "🥘"), CategoryItem(6, stringResource(R.string.type_stewed), "🫕"),
        CategoryItem(7, stringResource(R.string.type_cold), "🥗"), CategoryItem(8, stringResource(R.string.type_stir_fry), "🍳"), CategoryItem(9, stringResource(R.string.type_braised), "🍖"),
        CategoryItem(10, stringResource(R.string.type_other), "🍽")
    )
    var categories by remember { mutableStateOf(defaultCategories) }
    var showDialog by remember { mutableStateOf(false) }
    var editItem by remember { mutableStateOf<CategoryItem?>(null) }
    Scaffold(topBar = {
        CenterAlignedTopAppBar(title = { Text(stringResource(R.string.category_manage), fontWeight = FontWeight.SemiBold) },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back)) } },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White))
    }, containerColor = Background) { padding ->
        LazyColumn(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(categories) { cat ->
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(cat.icon, fontSize = 24.sp)
                        Spacer(Modifier.width(12.dp))
                        Text(cat.name, fontSize = 15.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                        Text("#${cat.id}", fontSize = 12.sp, color = TextHint)
                    }
                }
            }
        }
    }
}
