package com.example.shiyu.ui.review

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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shiyu.data.db.dao.RecipeReviewDao
import com.example.shiyu.data.db.entity.RecipeReviewEntity
import com.example.shiyu.ui.theme.*
import androidx.compose.ui.res.stringResource
import com.example.shiyu.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ReviewViewModel @Inject constructor(private val dao: RecipeReviewDao) : ViewModel() {
    var items = mutableStateListOf<RecipeReviewEntity>()
    init { viewModelScope.launch { items.clear(); items.addAll(dao.getAllOnce()) } }
    fun approve(id: Long) { viewModelScope.launch { val r = items.find { it.id == id } ?: return@launch; dao.update(r.copy(status = 1, review_time = System.currentTimeMillis())); items.clear(); items.addAll(dao.getAllOnce()) } }
    fun reject(id: Long, comment: String) { viewModelScope.launch { val r = items.find { it.id == id } ?: return@launch; dao.update(r.copy(status = 2, comment = comment, review_time = System.currentTimeMillis())); items.clear(); items.addAll(dao.getAllOnce()) } }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(onBack: () -> Unit, vm: ReviewViewModel = hiltViewModel()) {
    var filter by remember { mutableIntStateOf(-1) }
    val filtered = remember(vm.items, filter) { if (filter == -1) vm.items else vm.items.filter { it.status == filter } }
    val rejectDefault = stringResource(R.string.reject_reason_default)
    Scaffold(topBar = {
        CenterAlignedTopAppBar(title = { Text(stringResource(R.string.recipe_review_title), fontWeight = FontWeight.SemiBold) },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back)) } },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White))
    }, containerColor = Background) { padding ->
        LazyColumn(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            item { Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf(-1 to stringResource(R.string.filter_all), 0 to stringResource(R.string.filter_pending), 1 to stringResource(R.string.filter_approved), 2 to stringResource(R.string.filter_rejected)).forEach { (s, t) -> FilterChip(selected = filter == s, onClick = { filter = s }, label = { Text(t, fontSize = 12.sp) }) } } }
            items(filtered) { item ->
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(item.recipe_name, fontSize = 15.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                            val (color, text) = when(item.status) { 1->Success to stringResource(R.string.status_approved); 2->Error to stringResource(R.string.status_rejected); else->Warning to stringResource(R.string.status_pending) }
                            Badge(containerColor = color) { Text(text, fontSize = 10.sp) }
                        }
                        if (item.comment.isNotEmpty()) Text(item.comment, fontSize = 12.sp, color = TextSecondary, modifier = Modifier.padding(top = 4.dp))
                        if (item.status == 0) Row(Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { vm.approve(item.id) }, colors = ButtonDefaults.buttonColors(containerColor = Success), shape = RoundedCornerShape(8.dp)) { Text(stringResource(R.string.approve), fontSize = 12.sp) }
                            OutlinedButton(onClick = { vm.reject(item.id, rejectDefault) }, shape = RoundedCornerShape(8.dp)) { Text(stringResource(R.string.reject_btn), fontSize = 12.sp) }
                        }
                    }
                }
            }
        }
    }
}
