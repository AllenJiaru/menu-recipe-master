package com.example.shiyu.ui.chef

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.shiyu.ui.role.FormField
import com.example.shiyu.ui.theme.*
import com.example.shiyu.util.Constants
import com.example.shiyu.util.FileUtils
import com.example.shiyu.util.RecipeTypeMap
import androidx.compose.ui.res.stringResource
import com.example.shiyu.R
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

private data class MaterialInput(
    val name: String = "",
    val amount: String = "",
    val unit: String = ""
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddRecipeScreen(
    recipeId: Long? = null,
    onNavigateBack: () -> Unit
) {
    val viewModel: AddRecipeViewModel = hiltViewModel()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var recipeName by remember { mutableStateOf("") }
    var recipeDescription by remember { mutableStateOf("") }
    var recipeType by remember { mutableIntStateOf(Constants.RECIPE_TYPE_MEAT) }
    var cookingTime by remember { mutableIntStateOf(30) }
    var difficulty by remember { mutableIntStateOf(1) }
    var recipeImage by remember { mutableStateOf("") }

    val materials = remember { mutableStateListOf(MaterialInput()) }
    val steps = remember { mutableStateListOf("") }

    val isEditMode = recipeId != null
    val existing by viewModel.existingRecipe

    // 编辑模式：加载已有菜谱数据
    LaunchedEffect(recipeId) {
        if (recipeId != null) {
            viewModel.loadRecipe(recipeId)
        }
    }

    // 预填数据（仅加载一次）
    var loaded by remember { mutableStateOf(false) }
    LaunchedEffect(existing) {
        val data = existing
        if (isEditMode && data != null && !loaded) {
            loaded = true
            recipeName = data.recipe.name
            recipeDescription = data.recipe.description
            recipeType = data.recipe.type
            cookingTime = data.recipe.cooking_time
            difficulty = data.recipe.difficulty
            recipeImage = data.recipe.image_path
            if (data.materials.isNotEmpty()) {
                materials.clear()
                data.materials.forEach { m ->
                    materials.add(MaterialInput(m.name, m.amount, m.unit))
                }
            }
            if (data.steps.isNotEmpty()) {
                steps.clear()
                data.steps.sortedBy { it.step_number }.forEach { s ->
                    steps.add(s.description)
                }
            }
        }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val savedPath = FileUtils.saveImage(context, it)
            if (savedPath != null) {
                recipeImage = savedPath
            }
        }
    }

    val onSave: () -> Unit = {
        scope.launch {
            if (recipeName.isNotBlank()) {
                viewModel.saveRecipe(
                    recipeId = recipeId,
                    name = recipeName,
                    type = recipeType,
                    description = recipeDescription,
                    imagePath = recipeImage,
                    cookingTime = cookingTime,
                    difficulty = difficulty,
                    materials = materials.map { Triple(it.name, it.amount, it.unit) },
                    steps = steps.toList()
                )
                onNavigateBack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEditMode) stringResource(R.string.edit_recipe) else stringResource(R.string.add_recipe)) },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text(stringResource(R.string.cancel), color = TextPrimary)
                    }
                },
                actions = {
                    TextButton(onClick = onSave) {
                        Text(stringResource(R.string.save), color = Primary)
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // 菜谱图片
            SectionLabel(stringResource(R.string.recipe_image))
            if (recipeImage.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    AsyncImage(
                        model = recipeImage,
                        contentDescription = stringResource(R.string.recipe_image),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Text(
                        text = "×",
                        fontSize = 24.sp,
                        color = Color.White,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .clickable { recipeImage = "" }
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceGray)
                        .clickable { imagePickerLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "+", fontSize = 40.sp, color = TextHint)
                        Text(text = stringResource(R.string.add_image), fontSize = 14.sp, color = TextHint)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            FormField(
                label = stringResource(R.string.dish_name),
                placeholder = stringResource(R.string.enter_dish_name),
                value = recipeName,
                onValueChange = { recipeName = it }
            )

            Spacer(modifier = Modifier.height(16.dp))

            SectionLabel(stringResource(R.string.description))
            OutlinedTextField(
                value = recipeDescription,
                onValueChange = { recipeDescription = it },
                placeholder = { Text(stringResource(R.string.enter_recipe_desc), color = TextHint) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Primary,
                    unfocusedBorderColor = BorderColor
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            SectionLabel(stringResource(R.string.category_label))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RecipeTypeMap.forEach { (type, name) ->
                    FilterChip(
                        selected = recipeType == type,
                        onClick = { recipeType = type },
                        label = { Text(name) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = stringResource(R.string.cooking_time), fontSize = 14.sp, color = TextSecondary)
                Spacer(modifier = Modifier.weight(1f))
                Text(text = "${cookingTime}${stringResource(R.string.minutes_unit)}", fontSize = 14.sp, color = Primary)
            }
            Slider(
                value = cookingTime.toFloat(),
                onValueChange = { cookingTime = it.toInt() },
                valueRange = 5f..180f,
                steps = 34,
                colors = SliderDefaults.colors(
                    thumbColor = Primary,
                    activeTrackColor = Primary
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            SectionLabel(stringResource(R.string.difficulty_level))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                (1..5).forEach { level ->
                    Button(
                        onClick = { difficulty = level },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (difficulty == level) Primary else SurfaceGray,
                            contentColor = if (difficulty == level) Color.White else TextPrimary
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(level.toString())
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 食材清单
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionLabel(stringResource(R.string.ingredient_list), modifier = Modifier.weight(1f))
                TextButton(onClick = { materials.add(MaterialInput()) }) {
                    Text(stringResource(R.string.add_ingredient), color = Primary)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            materials.forEachIndexed { index, material ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = material.name,
                        onValueChange = { materials[index] = material.copy(name = it) },
                        placeholder = { Text(stringResource(R.string.ingredient_name), fontSize = 13.sp, color = TextHint) },
                        modifier = Modifier.weight(1.4f),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = BorderColor
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    OutlinedTextField(
                        value = material.amount,
                        onValueChange = { materials[index] = material.copy(amount = it) },
                        placeholder = { Text(stringResource(R.string.amount), fontSize = 13.sp, color = TextHint) },
                        modifier = Modifier.weight(0.8f),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = BorderColor
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    OutlinedTextField(
                        value = material.unit,
                        onValueChange = { materials[index] = material.copy(unit = it) },
                        placeholder = { Text(stringResource(R.string.unit), fontSize = 13.sp, color = TextHint) },
                        modifier = Modifier.weight(0.7f),
                        singleLine = true,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = BorderColor
                        )
                    )
                    IconButton(
                        onClick = { if (materials.size > 1) materials.removeAt(index) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Text("✕", color = TextHint, fontSize = 16.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 烹饪步骤
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SectionLabel(stringResource(R.string.cooking_steps), modifier = Modifier.weight(1f))
                TextButton(onClick = { steps.add("") }) {
                    Text(stringResource(R.string.add_step), color = Primary)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            steps.forEachIndexed { index, step ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${index + 1}",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = step,
                        onValueChange = { steps[index] = it },
                        placeholder = { Text(stringResource(R.string.step_desc, index + 1), fontSize = 13.sp, color = TextHint) },
                        modifier = Modifier.weight(1f),
                        minLines = 2,
                        shape = RoundedCornerShape(8.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Primary,
                            unfocusedBorderColor = BorderColor
                        )
                    )
                    IconButton(
                        onClick = { if (steps.size > 1) steps.removeAt(index) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Text("✕", color = TextHint, fontSize = 16.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = onSave,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(24.dp)
            ) {
                Text(stringResource(R.string.save_recipe), fontSize = 16.sp, color = Color.White)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = TextPrimary,
        modifier = modifier.padding(bottom = 8.dp)
    )
}
