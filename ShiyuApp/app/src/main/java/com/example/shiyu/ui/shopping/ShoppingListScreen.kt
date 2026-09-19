package com.example.shiyu.ui.shopping

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.example.shiyu.R
import com.example.shiyu.ui.theme.Primary
import com.example.shiyu.ui.theme.Secondary
import com.example.shiyu.ui.theme.Warning
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shiyu.api.ShoppingItemDto
import com.example.shiyu.data.repository.BackendRepository
import com.example.shiyu.ui.components.EmptyState
import com.example.shiyu.ui.components.LoadingIndicator
import com.example.shiyu.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlinx.coroutines.delay

@HiltViewModel
class ShoppingViewModel @Inject constructor(
    private val backendRepository: BackendRepository
) : ViewModel() {

    private val _items = mutableStateListOf<ShoppingItemDto>()
    val items: List<ShoppingItemDto> = _items

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _message = mutableStateOf("")
    val message: State<String> = _message

    private val _stats = mutableStateOf<Map<String, Any>?>(null)
    val stats: State<Map<String, Any>?> = _stats

    init {
        loadItems()
        loadStats()
    }

    fun loadItems() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val list = backendRepository.getShoppingList()
                _items.clear()
                _items.addAll(list)
            } catch (e: Exception) {
                _message.value = "加载失败：${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadStats() {
        viewModelScope.launch {
            _stats.value = backendRepository.getShoppingStats()
        }
    }

    fun addItem(name: String, quantity: String, unit: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            val ok = backendRepository.addShoppingItem(name.trim(), quantity.trim(), unit.trim())
            _message.value = if (ok) "已添加" else "添加失败"
            if (ok) { loadItems(); loadStats() }
        }
    }

    fun toggleItem(item: ShoppingItemDto) {
        val id = item.id ?: return
        viewModelScope.launch {
            backendRepository.toggleShoppingItem(id)
            loadItems()
            loadStats()
        }
    }

    fun deleteItem(item: ShoppingItemDto) {
        val id = item.id ?: return
        viewModelScope.launch {
            backendRepository.deleteShoppingItem(id)
            loadItems()
            loadStats()
        }
    }

    fun clearChecked() {
        viewModelScope.launch {
            backendRepository.clearCheckedShoppingItems()
            loadItems()
            loadStats()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    onNavigateBack: () -> Unit
) {
    val viewModel: ShoppingViewModel = androidx.hilt.navigation.compose.hiltViewModel()
    var itemName by remember { mutableStateOf("") }
    var itemQuantity by remember { mutableStateOf("") }
    var itemUnit by remember { mutableStateOf("") }
    val checkedCount = viewModel.items.count { it.isChecked == true }
    var refreshing by remember { mutableStateOf(false) }

    LaunchedEffect(refreshing) {
        if (refreshing) {
            viewModel.loadItems()
            viewModel.loadStats()
            delay(300)
            refreshing = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.shopping_list)) },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text(stringResource(R.string.back), color = TextPrimary)
                    }
                },
                actions = {
                    if (checkedCount > 0) {
                        TextButton(onClick = { viewModel.clearChecked() }) {
                            Text(stringResource(R.string.clear_purchased), color = Primary)
                        }
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
            // 添加输入区
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = itemName,
                    onValueChange = { itemName = it },
                    placeholder = { Text(stringResource(R.string.ingredient_name_placeholder), fontSize = 13.sp) },
                    modifier = Modifier.weight(1.4f),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                OutlinedTextField(
                    value = itemQuantity,
                    onValueChange = { itemQuantity = it },
                    placeholder = { Text(stringResource(R.string.quantity_placeholder), fontSize = 13.sp) },
                    modifier = Modifier.weight(0.8f),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                OutlinedTextField(
                    value = itemUnit,
                    onValueChange = { itemUnit = it },
                    placeholder = { Text(stringResource(R.string.unit_placeholder), fontSize = 13.sp) },
                    modifier = Modifier.weight(0.7f),
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Button(
                    onClick = {
                        viewModel.addItem(itemName, itemQuantity, itemUnit)
                        itemName = ""; itemQuantity = ""; itemUnit = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp)
                ) {
                    Text(stringResource(R.string.add_btn), fontSize = 13.sp, color = Color.White)
                }
            }

            PullToRefreshBox(
                isRefreshing = refreshing,
                onRefresh = { refreshing = true },
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (viewModel.message.value.isNotEmpty()) {
                        Text(
                            text = viewModel.message.value,
                            fontSize = 12.sp,
                            color = TextHint,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }

                    // Stats bar
                    val statsData = viewModel.stats.value
                    if (statsData != null) {
                        val total = (statsData["totalItems"] as? Number)?.toInt() ?: viewModel.items.size
                        val checked = (statsData["checkedItems"] as? Number)?.toInt() ?: checkedCount
                        val unchecked = total - checked
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White)
                        ) {
                            Row(
                                Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("$total", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Primary)
                                    Text(stringResource(R.string.total_label), fontSize = 12.sp, color = TextSecondary)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("$checked", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Secondary)
                                    Text(stringResource(R.string.purchased), fontSize = 12.sp, color = TextSecondary)
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("$unchecked", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Warning)
                                    Text(stringResource(R.string.to_buy), fontSize = 12.sp, color = TextSecondary)
                                }
                            }
                        }
                    }

                    when {
                        viewModel.isLoading.value -> LoadingIndicator(modifier = Modifier.weight(1f))
                        viewModel.items.isEmpty() -> EmptyState(
                            icon = "🛒",
                            title = stringResource(R.string.shopping_list_empty),
                            subtitle = stringResource(R.string.add_ingredients_to_buy),
                            modifier = Modifier.weight(1f)
                        )
                        else -> LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(viewModel.items, key = { it.id ?: 0L }) { item ->
                                ShoppingItemRow(
                                    item = item,
                                    onToggle = { viewModel.toggleItem(item) },
                                    onDelete = { viewModel.deleteItem(item) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ShoppingItemRow(
    item: ShoppingItemDto,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val checked = item.isChecked == true
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (checked) SurfaceGray else Surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = checked,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(checkedColor = Primary)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.ingredientName ?: "",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (checked) TextHint else TextPrimary,
                    textDecoration = if (checked) TextDecoration.LineThrough else null
                )
                if (!item.sourceRecipeName.isNullOrBlank()) {
                    Text(
                        text = stringResource(R.string.from_recipe, item.sourceRecipeName),
                        fontSize = 11.sp,
                        color = TextHint
                    )
                }
            }
            if (!(item.quantity.isNullOrBlank() && item.unit.isNullOrBlank())) {
                Text(
                    text = "${item.quantity ?: ""} ${item.unit ?: ""}".trim(),
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "🗑",
                fontSize = 14.sp,
                modifier = Modifier.clickable { onDelete() }
            )
        }
    }
}
