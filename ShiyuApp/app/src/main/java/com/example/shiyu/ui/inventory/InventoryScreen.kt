package com.example.shiyu.ui.inventory

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shiyu.data.db.dao.InventoryDao
import com.example.shiyu.data.db.entity.InventoryEntity
import com.example.shiyu.data.repository.BackendRepository
import com.example.shiyu.ui.theme.*
import androidx.compose.ui.res.stringResource
import com.example.shiyu.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val dao: InventoryDao,
    private val backendRepository: BackendRepository
) : ViewModel() {
    var items = mutableStateListOf<InventoryEntity>()
    var lowStockCount = mutableIntStateOf(0)
    var restockResult by mutableStateOf<Int?>(null)
    var restocking by mutableStateOf(false)
    init { viewModelScope.launch { refresh() } }
    suspend fun refresh() {
        items.clear(); items.addAll(dao.getAllOnce())
        lowStockCount.intValue = dao.getLowStock().size
    }
    fun addItem(name: String, category: String, quantity: Float, unit: String, threshold: Float) {
        viewModelScope.launch { dao.insert(InventoryEntity(name = name, category = category, quantity = quantity, unit = unit, threshold = threshold)); refresh() }
    }
    fun updateItem(item: InventoryEntity) { viewModelScope.launch { dao.update(item); refresh() } }
    fun deleteItem(id: Long) { viewModelScope.launch { dao.delete(id); refresh() } }

    fun autoRestock() {
        if (restocking) return
        restocking = true
        restockResult = null
        viewModelScope.launch {
            try {
                val lowStockItems = backendRepository.getLowStockItems()
                if (lowStockItems.isEmpty()) {
                    // Fall back to local low stock items
                    val localLowStock = dao.getLowStock()
                    var count = 0
                    for (item in localLowStock) {
                        val added = backendRepository.addShoppingItem(
                            name = item.name,
                            quantity = "1",
                            unit = item.unit
                        )
                        if (added) count++
                    }
                    restockResult = count
                } else {
                    var count = 0
                    for (item in lowStockItems) {
                        val added = backendRepository.addShoppingItem(
                            name = item.name ?: "",
                            quantity = "1",
                            unit = item.unit ?: ""
                        )
                        if (added) count++
                    }
                    restockResult = count
                }
            } catch (e: Exception) {
                restockResult = 0
            } finally {
                restocking = false
            }
        }
    }

    fun clearRestockResult() { restockResult = null }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(onBack: () -> Unit, vm: InventoryViewModel = hiltViewModel()) {
    var showAdd by remember { mutableStateOf(false) }
    var editItem by remember { mutableStateOf<InventoryEntity?>(null) }
    var showRestockDialog by remember { mutableStateOf(false) }
    var scannedBarcode by remember { mutableStateOf<String?>(null) }

    val scanLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val barcode = result.data?.getStringExtra("SCAN_RESULT") ?: ""
            if (barcode.isNotBlank()) {
                scannedBarcode = barcode
                editItem = null
                showAdd = true
            }
        }
    }

    fun launchBarcodeScan() {
        try {
            val intent = Intent("com.google.zxing.client.android.SCAN").apply {
                putExtra("SCAN_MODE", "PRODUCT_MODE")
            }
            scanLauncher.launch(intent)
        } catch (_: Exception) {
            showAdd = true
        }
    }

    Scaffold(topBar = {
        CenterAlignedTopAppBar(title = { Text(stringResource(R.string.inventory_management), fontWeight = FontWeight.SemiBold) },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back)) } },
            actions = {
                IconButton(onClick = { launchBarcodeScan() }) {
                    Icon(Icons.Outlined.QrCodeScanner, stringResource(R.string.scan_add))
                }
                IconButton(onClick = { showAdd = true }) { Icon(Icons.Filled.Add, stringResource(R.string.add_btn_desc)) }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White))
    }, containerColor = Background) { padding ->
        LazyColumn(Modifier.padding(padding).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 12.dp)) {
            if (vm.lowStockCount.value > 0) item {
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Warning.copy(alpha = 0.1f))) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.Warning, null, tint = Warning, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.low_stock_warning, vm.lowStockCount.value), fontSize = 14.sp, color = Warning, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.weight(1f))
                        Button(
                            onClick = { vm.autoRestock(); showRestockDialog = true },
                            enabled = !vm.restocking,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            if (vm.restocking) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = Color.White)
                                Spacer(Modifier.width(6.dp))
                            }
                            Text(stringResource(R.string.restock), fontSize = 12.sp, color = Color.White)
                        }
                    }
                }
            }
            items(vm.items) { item ->
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(item.name, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            Text("${item.quantity} ${item.unit} · ${item.category}", fontSize = 12.sp, color = TextHint)
                        }
                        if (item.threshold > 0 && item.quantity <= item.threshold) {
                            Badge(containerColor = Warning) { Text(stringResource(R.string.low_label), fontSize = 10.sp) }
                            Spacer(Modifier.width(8.dp))
                        }
                        IconButton(onClick = { editItem = item }) { Icon(Icons.Outlined.Edit, null, modifier = Modifier.size(18.dp)) }
                        IconButton(onClick = { vm.deleteItem(item.id) }) { Icon(Icons.Outlined.Delete, null, tint = Error, modifier = Modifier.size(18.dp)) }
                    }
                }
            }
        }
    }
    if (showAdd) AddInventoryDialog(onDismiss = { showAdd = false; scannedBarcode = null }, initialName = scannedBarcode, onConfirm = { n, c, q, u, t -> vm.addItem(n, c, q, u, t) })
    editItem?.let { item -> AddInventoryDialog(onDismiss = { editItem = null }, item = item, onConfirm = { n, c, q, u, t -> vm.updateItem(item.copy(name = n, category = c, quantity = q, unit = u, threshold = t)); editItem = null }) }

    if (showRestockDialog) {
        AlertDialog(
            onDismissRequest = { showRestockDialog = false; vm.clearRestockResult() },
            title = { Text(stringResource(R.string.restock_title)) },
            text = {
                Text(
                    when {
                        vm.restocking -> stringResource(R.string.restocking)
                        vm.restockResult != null && vm.restockResult!! > 0 -> stringResource(R.string.restock_success, vm.restockResult!!)
                        vm.restockResult != null -> stringResource(R.string.restock_empty)
                        else -> ""
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = { showRestockDialog = false; vm.clearRestockResult() },
                    shape = RoundedCornerShape(10.dp)
                ) { Text(stringResource(R.string.confirm_btn_short)) }
            }
        )
    }
}

@Composable
private fun AddInventoryDialog(onDismiss: () -> Unit, item: InventoryEntity? = null, initialName: String? = null, onConfirm: (String, String, Float, String, Float) -> Unit) {
    var name by remember { mutableStateOf(item?.name ?: initialName ?: "") }
    var category by remember { mutableStateOf(item?.category ?: "") }
    var quantity by remember { mutableStateOf(item?.quantity?.toString() ?: "") }
    var unit by remember { mutableStateOf(item?.unit ?: "") }
    var threshold by remember { mutableStateOf(item?.threshold?.toString() ?: "") }
    var barcodeInput by remember { mutableStateOf("") }
    var showManualBarcode by remember { mutableStateOf(false) }

    val scanLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val barcode = result.data?.getStringExtra("SCAN_RESULT") ?: ""
            if (barcode.isNotBlank()) {
                name = barcode
            }
        }
    }

    fun launchScan() {
        try {
            val intent = Intent("com.google.zxing.client.android.SCAN").apply {
                putExtra("SCAN_MODE", "PRODUCT_MODE")
            }
            scanLauncher.launch(intent)
        } catch (_: Exception) {
            showManualBarcode = true
        }
    }

    AlertDialog(onDismissRequest = onDismiss, title = { Text(if (item == null) stringResource(R.string.add_inventory) else stringResource(R.string.edit_inventory)) }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it }, label = { Text(stringResource(R.string.name_label)) },
                    modifier = Modifier.weight(1f), singleLine = true, shape = RoundedCornerShape(10.dp)
                )
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = { launchScan() }) {
                    Icon(Icons.Outlined.QrCodeScanner, stringResource(R.string.scan_qr), tint = Primary)
                }
            }
            if (showManualBarcode) {
                OutlinedTextField(
                    value = barcodeInput,
                    onValueChange = { barcodeInput = it },
                    label = { Text(stringResource(R.string.manual_barcode)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    trailingIcon = {
                        TextButton(onClick = { name = barcodeInput; showManualBarcode = false }) {
                            Text(stringResource(R.string.confirm_scan), color = Primary)
                        }
                    }
                )
            }
            OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text(stringResource(R.string.category_label_short)) }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(10.dp))
            OutlinedTextField(value = quantity, onValueChange = { quantity = it }, label = { Text(stringResource(R.string.quantity_label)) }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(10.dp))
            OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text(stringResource(R.string.unit_label)) }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(10.dp))
            OutlinedTextField(value = threshold, onValueChange = { threshold = it }, label = { Text(stringResource(R.string.alert_threshold)) }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(10.dp))
        }
    }, confirmButton = {
        Button(onClick = { if (name.isNotBlank()) onConfirm(name, category, quantity.toFloatOrNull() ?: 0f, unit, threshold.toFloatOrNull() ?: 0f) }, shape = RoundedCornerShape(10.dp)) { Text(stringResource(R.string.confirm_btn_short)) }
    }, dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } })
}
