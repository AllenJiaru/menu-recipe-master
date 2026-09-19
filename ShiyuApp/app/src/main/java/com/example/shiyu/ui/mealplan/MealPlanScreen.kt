package com.example.shiyu.ui.mealplan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.shiyu.R
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shiyu.api.MealPlanDto
import com.example.shiyu.data.db.entity.RecipeEntity
import com.example.shiyu.data.repository.BackendRepository
import com.example.shiyu.data.repository.RecipeRepository
import com.example.shiyu.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MealPlanViewModel @Inject constructor(
    private val backendRepository: BackendRepository,
    private val recipeRepository: RecipeRepository
) : ViewModel() {

    private val _plans = mutableStateListOf<MealPlanDto>()
    val plans: List<MealPlanDto> = _plans

    private val _recipes = mutableStateListOf<RecipeEntity>()
    val recipes: List<RecipeEntity> = _recipes

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _message = mutableStateOf("")
    val message: State<String> = _message

    private val _stats = mutableStateOf<Map<String, Any>?>(null)
    val stats: State<Map<String, Any>?> = _stats

    init {
        viewModelScope.launch {
            _stats.value = backendRepository.getMealPlanStats()
        }
    }

    fun loadData(startDate: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val list = backendRepository.getWeeklyMealPlans(startDate)
                _plans.clear()
                _plans.addAll(list)
                val localRecipes = recipeRepository.getAllRecipesOnce()
                _recipes.clear()
                _recipes.addAll(localRecipes.map { it.recipe })
            } catch (e: Exception) {
                _message.value = "加载失败：${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addPlan(planDate: String, mealType: String, recipe: RecipeEntity) {
        viewModelScope.launch {
            val ok = backendRepository.addMealPlan(planDate, mealType, recipe.id, recipe.name)
            _message.value = if (ok) "已添加${recipe.name}" else "添加失败"
            if (ok) loadData(planDate)
        }
    }

    fun deletePlan(plan: MealPlanDto) {
        val id = plan.id ?: return
        val date = plan.planDate ?: return
        viewModelScope.launch {
            backendRepository.deleteMealPlan(id)
            loadData(date)
        }
    }

    fun getPlansForSlot(date: String, mealType: String): List<MealPlanDto> =
        plans.filter { it.planDate == date && it.mealType == mealType }

    companion object {
        @androidx.annotation.StringRes
        fun mealTypeLabelRes(type: String): Int = when (type) {
            "breakfast" -> R.string.meal_breakfast
            "lunch" -> R.string.meal_lunch
            "dinner" -> R.string.meal_dinner
            "snack" -> R.string.meal_snack
            else -> R.string.meal_snack
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPlanScreen(
    onNavigateBack: () -> Unit
) {
    val viewModel: MealPlanViewModel = androidx.hilt.navigation.compose.hiltViewModel()
    var weekOffset by remember { mutableIntStateOf(0) }
    var refreshing by remember { mutableStateOf(false) }

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val calendar = Calendar.getInstance()
    calendar.firstDayOfWeek = Calendar.MONDAY
    calendar.add(Calendar.WEEK_OF_YEAR, weekOffset)
    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

    val weekStart = dateFormat.format(calendar.time)
    val days = (0..6).map { i ->
        val c = calendar.clone() as Calendar
        c.add(Calendar.DAY_OF_MONTH, i)
        dateFormat.format(c.time)
    }
    val dayLabels = listOf(
        stringResource(R.string.day_monday),
        stringResource(R.string.day_tuesday),
        stringResource(R.string.day_wednesday),
        stringResource(R.string.day_thursday),
        stringResource(R.string.day_friday),
        stringResource(R.string.day_saturday),
        stringResource(R.string.day_sunday)
    )
    val mealTypes = listOf("breakfast", "lunch", "dinner", "snack")

    LaunchedEffect(weekStart) {
        viewModel.loadData(weekStart)
    }

    LaunchedEffect(refreshing) {
        if (refreshing) {
            viewModel.loadData(weekStart)
            delay(300)
            refreshing = false
        }
    }

    var pickerVisible by remember { mutableStateOf(false) }
    var pickerDate by remember { mutableStateOf("") }
    var pickerMealType by remember { mutableStateOf("") }
    var selectedRecipes by remember { mutableStateOf(setOf<Long>()) }
    var deleteConfirm by remember { mutableStateOf<MealPlanDto?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.week_meal_plan)) },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text(stringResource(R.string.back), color = TextPrimary)
                    }
                },
                actions = {
                    TextButton(onClick = { weekOffset-- }) { Text("‹", fontSize = 22.sp) }
                    TextButton(onClick = { weekOffset++ }) { Text("›", fontSize = 22.sp) }
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
            Text(
                text = "$weekStart ${stringResource(R.string.week_label)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(16.dp)
            )

            PullToRefreshBox(
                isRefreshing = refreshing,
                onRefresh = { refreshing = true },
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Meal plan stats
                    val statsData = viewModel.stats.value
                    if (statsData != null) {
                        val plannedMeals = (statsData["plannedMeals"] as? Number)?.toInt() ?: 0
                        val recipesUsed = (statsData["recipesUsed"] as? Number)?.toInt() ?: 0
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("$plannedMeals", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Primary)
                                    Text(stringResource(R.string.planned_meals), fontSize = 12.sp, color = TextSecondary)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("$recipesUsed", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = PrimaryDark)
                                    Text(stringResource(R.string.recipes_used), fontSize = 12.sp, color = TextSecondary)
                                }
                            }
                        }
                    }

                    if (viewModel.message.value.isNotEmpty()) {
                        Text(
                            text = viewModel.message.value,
                            fontSize = 12.sp,
                            color = Primary,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                    }

                    if (viewModel.isLoading.value) {
                        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Primary)
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(days.size) { dayIndex ->
                                val date = days[dayIndex]
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Surface)
                                ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = dayLabels[dayIndex],
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Primary
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = date,
                                        fontSize = 12.sp,
                                        color = TextHint
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                mealTypes.forEach { mealType ->
                                    val slotPlans = viewModel.getPlansForSlot(date, mealType)
                                    Column {
                                        // 餐次标题行
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(SurfaceGray)
                                                .clickable {
                                                    pickerDate = date
                                                    pickerMealType = mealType
                                                    selectedRecipes = emptySet()
                                                    pickerVisible = true
                                                }
                                                .padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = stringResource(MealPlanViewModel.mealTypeLabelRes(mealType)),
                                                fontSize = 12.sp,
                                                color = TextSecondary,
                                                modifier = Modifier.width(44.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = if (slotPlans.isEmpty()) stringResource(R.string.tap_to_add_recipe) else "${slotPlans.size}道菜",
                                                fontSize = 13.sp,
                                                color = if (slotPlans.isEmpty()) TextHint else Primary,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text("+", fontSize = 18.sp, color = Primary)
                                        }
                                        // 已选菜谱列表
                                        slotPlans.forEach { plan ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(start = 52.dp, top = 4.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(PrimaryLight.copy(alpha = 0.1f))
                                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = plan.recipeName ?: stringResource(R.string.unknown_recipe),
                                                    fontSize = 13.sp,
                                                    color = TextPrimary,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Text(
                                                    text = "✕",
                                                    fontSize = 12.sp,
                                                    color = TextHint,
                                                    modifier = Modifier.clickable {
                                                        deleteConfirm = plan
                                                    }
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(6.dp))
                                    }
                                }
                            }
                        }
                    }
                    }
                }
            }
        }
    }

    // 多选菜谱弹窗
    if (pickerVisible) {
        AlertDialog(
            onDismissRequest = { pickerVisible = false },
            title = {
                Text(stringResource(R.string.select_recipes_multi))
            },
            text = {
                if (viewModel.recipes.isEmpty()) {
                    Text(stringResource(R.string.no_local_recipes), fontSize = 13.sp, color = TextHint)
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .height(400.dp)
                            .fillMaxWidth()
                    ) {
                        items(viewModel.recipes) { recipe ->
                            val isSelected = recipe.id in selectedRecipes
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        selectedRecipes = if (isSelected) {
                                            selectedRecipes - recipe.id
                                        } else {
                                            selectedRecipes + recipe.id
                                        }
                                    }
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { checked ->
                                        selectedRecipes = if (checked) {
                                            selectedRecipes + recipe.id
                                        } else {
                                            selectedRecipes - recipe.id
                                        }
                                    },
                                    colors = CheckboxDefaults.colors(checkedColor = Primary)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = recipe.name,
                                    fontSize = 14.sp,
                                    color = TextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = stringResource(RecipeTypeNameRes(recipe.type)),
                                    fontSize = 11.sp,
                                    color = TextHint
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // 批量添加选中的菜谱
                        val recipesToAdd = viewModel.recipes.filter { it.id in selectedRecipes }
                        recipesToAdd.forEach { recipe ->
                            viewModel.addPlan(pickerDate, pickerMealType, recipe)
                        }
                        pickerVisible = false
                        selectedRecipes = emptySet()
                    },
                    enabled = selectedRecipes.isNotEmpty()
                ) {
                    Text("${stringResource(R.string.add_btn)} (${selectedRecipes.size})", color = if (selectedRecipes.isNotEmpty()) Primary else TextHint)
                }
            },
            dismissButton = {
                TextButton(onClick = { pickerVisible = false; selectedRecipes = emptySet() }) {
                    Text(stringResource(R.string.cancel), color = TextSecondary)
                }
            }
        )
    }

    deleteConfirm?.let { plan ->
        AlertDialog(
            onDismissRequest = { deleteConfirm = null },
            title = { Text(stringResource(R.string.remove_recipe)) },
            text = { Text(stringResource(R.string.confirm_remove_recipe, plan.recipeName ?: "")) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deletePlan(plan)
                    deleteConfirm = null
                }) {
                    Text(stringResource(R.string.remove), color = Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirm = null }) {
                    Text(stringResource(R.string.cancel), color = TextSecondary)
                }
            }
        )
    }
}
}

@androidx.annotation.StringRes
private fun RecipeTypeNameRes(type: Int): Int = when (type) {
    1 -> R.string.category_meat_dish
    2 -> R.string.category_vegetable_dish
    3 -> R.string.category_soup
    4 -> R.string.category_dessert
    5 -> R.string.category_steamed
    6 -> R.string.category_stewed
    7 -> R.string.category_cold_dish
    8 -> R.string.category_stir_fry
    9 -> R.string.category_braised
    else -> R.string.category_other
}
