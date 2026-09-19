package com.example.shiyu.ui.logs

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
import com.example.shiyu.data.db.dao.OperationLogDao
import com.example.shiyu.data.db.entity.OperationLogEntity
import com.example.shiyu.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class LogViewModel @Inject constructor(private val dao: OperationLogDao) : ViewModel() {
    var logs = mutableStateListOf<OperationLogEntity>()
    init { viewModelScope.launch { logs.clear(); logs.addAll(dao.getAllOnce()) } }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperationLogScreen(onBack: () -> Unit, vm: LogViewModel = hiltViewModel()) {
    val fmt = remember { SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()) }
    Scaffold(topBar = {
        CenterAlignedTopAppBar(title = { Text(stringResource(R.string.operation_log), fontWeight = FontWeight.SemiBold) },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back)) } },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White))
    }, containerColor = Background) { padding ->
        LazyColumn(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            if (vm.logs.isEmpty()) item { Text(stringResource(R.string.no_data), color = TextHint, modifier = Modifier.padding(top = 40.dp)) }
            items(vm.logs) { log ->
                Card(shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Column(Modifier.padding(10.dp)) {
                        Row { Text(log.username, fontWeight = FontWeight.Medium, fontSize = 13.sp); Spacer(Modifier.width(8.dp)); Text(log.action, fontSize = 13.sp, color = Primary) }
                        if (log.detail.isNotEmpty()) Text(log.detail, fontSize = 12.sp, color = TextSecondary, modifier = Modifier.padding(top = 2.dp))
                        Text(fmt.format(Date(log.create_time)), fontSize = 11.sp, color = TextHint, modifier = Modifier.padding(top = 2.dp))
                    }
                }
            }
        }
    }
}
