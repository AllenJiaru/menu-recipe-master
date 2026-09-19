package com.example.shiyu.ui.export

import android.content.Context
import android.os.Environment
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shiyu.R
import com.example.shiyu.data.db.dao.OrderDao
import com.example.shiyu.data.db.dao.RecipeDao
import com.example.shiyu.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ExportViewModel @Inject constructor(private val recipeDao: RecipeDao, private val orderDao: OrderDao) : ViewModel() {
    var message = mutableStateOf("")
    fun exportRecipes(context: Context) {
        viewModelScope.launch {
            try {
                val recipes = recipeDao.getAllRecipesOnce().map { it.recipe }
                val csv = StringBuilder("ID,名称,类型,难度,状态,创建时间\n")
                recipes.forEach { r -> csv.appendLine("${r.id},${r.name},${r.type},${r.difficulty},${r.status},${r.create_time}") }
                val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "食遇")
                dir.mkdirs()
                val file = File(dir, "菜谱导出_${System.currentTimeMillis()}.csv")
                file.writeText(csv.toString())
                message.value = context.getString(R.string.exported_to, file.absolutePath)
            } catch (e: Exception) { message.value = context.getString(R.string.export_failed, e.message) }
        }
    }
    fun exportOrders(context: Context) {
        viewModelScope.launch {
            try {
                val orders = orderDao.getAllOrdersOnce()
                val csv = StringBuilder("ID,菜名,状态,备注,创建时间\n")
                orders.forEach { o -> csv.appendLine("${o.id},${o.recipe_name},${o.status},${o.remark},${o.order_time}") }
                val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "食遇")
                dir.mkdirs()
                val file = File(dir, "订单导出_${System.currentTimeMillis()}.csv")
                file.writeText(csv.toString())
                message.value = context.getString(R.string.exported_to, file.absolutePath)
            } catch (e: Exception) { message.value = context.getString(R.string.export_failed, e.message) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataExportScreen(onBack: () -> Unit, vm: ExportViewModel = hiltViewModel()) {
    val ctx = LocalContext.current
    Scaffold(topBar = {
        CenterAlignedTopAppBar(title = { Text(stringResource(R.string.data_export), fontWeight = FontWeight.SemiBold) },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back)) } },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White))
    }, containerColor = Background) { padding ->
        Column(Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.MenuBook, null, tint = Primary, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) { Text(stringResource(R.string.export_recipes), fontWeight = FontWeight.Medium); Text(stringResource(R.string.export_as_csv), fontSize = 12.sp, color = TextHint) }
                    Button(onClick = { vm.exportRecipes(ctx) }, shape = RoundedCornerShape(10.dp)) { Text(stringResource(R.string.export)) }
                }
            }
            Card(shape = RoundedCornerShape(14.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.ShoppingCart, null, tint = Info, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) { Text(stringResource(R.string.export_orders), fontWeight = FontWeight.Medium); Text(stringResource(R.string.export_as_csv), fontSize = 12.sp, color = TextHint) }
                    Button(onClick = { vm.exportOrders(ctx) }, shape = RoundedCornerShape(10.dp), colors = ButtonDefaults.buttonColors(containerColor = Info)) { Text(stringResource(R.string.export)) }
                }
            }
            if (vm.message.value.isNotEmpty()) Card(shape = RoundedCornerShape(10.dp), colors = CardDefaults.cardColors(containerColor = Success.copy(alpha = 0.1f))) {
                Text(vm.message.value, modifier = Modifier.padding(12.dp), fontSize = 13.sp, color = Success)
            }
        }
    }
}
