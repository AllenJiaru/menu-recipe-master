package com.example.shiyu.ui.health

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shiyu.R
import com.example.shiyu.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemHealthScreen(onBack: () -> Unit) {
    val calculatingText = stringResource(R.string.calculating)
    var dbSize by remember { mutableStateOf(calculatingText) }
    var recipeCount by remember { mutableIntStateOf(0) }
    var orderCount by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) { recipeCount = 113; orderCount = 4; dbSize = "2.3 MB" }
    Scaffold(topBar = {
        CenterAlignedTopAppBar(title = { Text(stringResource(R.string.system_status), fontWeight = FontWeight.SemiBold) },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back)) } },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White))
    }, containerColor = Background) { padding ->
        Column(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Column(Modifier.padding(16.dp)) {
                    Text(stringResource(R.string.system_status), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    Spacer(Modifier.height(12.dp))
                    HealthRow(stringResource(R.string.database), stringResource(R.string.normal), Success)
                    HealthRow(stringResource(R.string.database_size), dbSize, Info)
                    HealthRow(stringResource(R.string.recipe_count), "$recipeCount", Primary)
                    HealthRow(stringResource(R.string.order_count), "$orderCount", Warning)
                }
            }
        }
    }
}

@Composable
private fun HealthRow(label: String, value: String, color: Color) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).padding(0.dp), contentAlignment = Alignment.Center) { Surface(Modifier.size(8.dp), shape = MaterialTheme.shapes.small, color = color) {} }
        Spacer(Modifier.width(8.dp))
        Text(label, fontSize = 14.sp, modifier = Modifier.weight(1f))
        Text(value, fontSize = 14.sp, color = color, fontWeight = FontWeight.Medium)
    }
}
