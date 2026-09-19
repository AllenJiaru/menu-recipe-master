package com.example.shiyu.ui.print

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shiyu.data.db.dao.RecipeDao
import com.example.shiyu.data.db.entity.RecipeEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.compose.ui.res.stringResource
import com.example.shiyu.R
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrintViewModel @Inject constructor(private val dao: RecipeDao) : ViewModel() {
    var recipe = mutableStateOf<RecipeEntity?>(null)
    fun load(id: Long) { viewModelScope.launch { recipe.value = dao.getRecipeById(id) } }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipePrintScreen(recipeId: Long, onBack: () -> Unit, vm: PrintViewModel = hiltViewModel()) {
    LaunchedEffect(recipeId) { vm.load(recipeId) }
    val r = vm.recipe.value
    Scaffold(topBar = {
        CenterAlignedTopAppBar(title = { Text(stringResource(R.string.recipe_detail_print), fontWeight = FontWeight.SemiBold) },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back)) } },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White))
    }, containerColor = Color.White) { padding ->
        if (r != null) Column(Modifier.padding(padding).padding(20.dp).verticalScroll(rememberScrollState())) {
            Text(r.name, fontSize = 24.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            HorizontalDivider(color = Color(0xFFEEEEEE))
            Spacer(Modifier.height(12.dp))
            InfoRow(stringResource(R.string.cooking_time), "${r.cooking_time} ${stringResource(R.string.minutes_unit)}")
            InfoRow(stringResource(R.string.difficulty_level), "${stringResource(R.string.difficulty)}${r.difficulty}")
            if (r.calories > 0) InfoRow(stringResource(R.string.calories_label), "${r.calories} kcal")
            if (r.protein > 0) InfoRow(stringResource(R.string.protein_label), "${r.protein} g")
            if (r.fat > 0) InfoRow(stringResource(R.string.fat_label), "${r.fat} g")
            if (r.carbs > 0) InfoRow(stringResource(R.string.carbs_label), "${r.carbs} g")
            if (r.cost > 0) InfoRow(stringResource(R.string.cost_label), "¥${r.cost}")
            if (r.price > 0) InfoRow(stringResource(R.string.price_label), "¥${r.price}")
            if (r.description.isNotEmpty()) { Spacer(Modifier.height(16.dp)); Text(stringResource(R.string.description_brief), fontWeight = FontWeight.SemiBold, fontSize = 16.sp); Text(r.description, fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp)) }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) { Text(label, fontSize = 14.sp, color = Color(0xFF666666), modifier = Modifier.width(80.dp)); Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium) }
}
