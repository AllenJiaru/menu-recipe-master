package com.example.shiyu.ui.version

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shiyu.R
import com.example.shiyu.data.db.dao.RecipeVersionDao
import com.example.shiyu.data.db.entity.RecipeVersionEntity
import com.example.shiyu.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class VersionViewModel @Inject constructor(private val dao: RecipeVersionDao) : ViewModel() {
    var versions = mutableStateListOf<RecipeVersionEntity>()
    fun loadVersions(recipeId: Long) { viewModelScope.launch { versions.clear(); versions.addAll(dao.getByRecipeIdOnce(recipeId)) } }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VersionHistoryScreen(recipeId: Long, onBack: () -> Unit, vm: VersionViewModel = hiltViewModel()) {
    LaunchedEffect(recipeId) { vm.loadVersions(recipeId) }
    val fmt = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
    Scaffold(topBar = {
        CenterAlignedTopAppBar(title = { Text(stringResource(R.string.version_history), fontWeight = FontWeight.SemiBold) },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back)) } },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White))
    }, containerColor = Background) { padding ->
        LazyColumn(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (vm.versions.isEmpty()) item { Text(stringResource(R.string.no_version_history), color = TextHint, modifier = Modifier.padding(top = 40.dp)) }
            items(vm.versions) { v ->
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(Modifier.padding(12.dp)) {
                        Row { Text("v${v.version}", fontWeight = FontWeight.Bold, color = Primary); Spacer(Modifier.width(8.dp)); Text(v.name, fontWeight = FontWeight.Medium) }
                        Text(fmt.format(Date(v.create_time)), fontSize = 12.sp, color = TextHint, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }
        }
    }
}
