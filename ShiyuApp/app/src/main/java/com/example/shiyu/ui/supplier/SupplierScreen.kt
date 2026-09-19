package com.example.shiyu.ui.supplier

import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shiyu.data.db.dao.SupplierDao
import com.example.shiyu.data.db.entity.SupplierEntity
import com.example.shiyu.ui.theme.*
import androidx.compose.ui.res.stringResource
import com.example.shiyu.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SupplierViewModel @Inject constructor(private val dao: SupplierDao) : ViewModel() {
    var items = mutableStateListOf<SupplierEntity>()
    init { viewModelScope.launch { items.clear(); items.addAll(dao.getAllOnce()) } }
    fun addItem(item: SupplierEntity) { viewModelScope.launch { dao.insert(item); items.clear(); items.addAll(dao.getAllOnce()) } }
    fun updateItem(item: SupplierEntity) { viewModelScope.launch { dao.update(item); items.clear(); items.addAll(dao.getAllOnce()) } }
    fun deleteItem(id: Long) { viewModelScope.launch { dao.delete(id); items.clear(); items.addAll(dao.getAllOnce()) } }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplierScreen(onBack: () -> Unit, vm: SupplierViewModel = hiltViewModel()) {
    var showAdd by remember { mutableStateOf(false) }
    var editItem by remember { mutableStateOf<SupplierEntity?>(null) }
    val context = LocalContext.current

    Scaffold(topBar = {
        CenterAlignedTopAppBar(title = { Text(stringResource(R.string.supplier_management), fontWeight = FontWeight.SemiBold) },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back)) } },
            actions = { IconButton(onClick = { showAdd = true }) { Icon(Icons.Filled.Add, stringResource(R.string.add_btn_desc)) } },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White))
    }, containerColor = Background) { padding ->
        LazyColumn(Modifier.padding(padding).padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(vertical = 12.dp)) {
            items(vm.items) { item ->
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(item.name, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                            Text(item.phone.ifEmpty { item.contact }, fontSize = 12.sp, color = TextHint)
                            Row { repeat(5) { i -> Icon(Icons.Filled.Star, null, tint = if (i < item.rating) Warning else Color.LightGray, modifier = Modifier.size(14.dp)) } }
                        }
                        if (item.phone.isNotBlank()) {
                            IconButton(onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${item.phone}"))
                                context.startActivity(intent)
                            }) { Icon(Icons.Filled.Phone, null, modifier = Modifier.size(18.dp), tint = Primary) }
                        }
                        IconButton(onClick = { editItem = item }) { Icon(Icons.Outlined.Edit, null, modifier = Modifier.size(18.dp)) }
                        IconButton(onClick = { vm.deleteItem(item.id) }) { Icon(Icons.Outlined.Delete, null, tint = Error, modifier = Modifier.size(18.dp)) }
                    }
                }
            }
        }
    }
    if (showAdd) AddSupplierDialog(onDismiss = { showAdd = false }, onConfirm = { vm.addItem(it) })
    editItem?.let { item -> AddSupplierDialog(onDismiss = { editItem = null }, item = item, onConfirm = { vm.updateItem(it); editItem = null }) }
}

@Composable
private fun AddSupplierDialog(onDismiss: () -> Unit, item: SupplierEntity? = null, onConfirm: (SupplierEntity) -> Unit) {
    var name by remember { mutableStateOf(item?.name ?: "") }
    var contact by remember { mutableStateOf(item?.contact ?: "") }
    var phone by remember { mutableStateOf(item?.phone ?: "") }
    var rating by remember { mutableIntStateOf(item?.rating ?: 3) }
    var category by remember { mutableStateOf(item?.category ?: "") }
    var address by remember { mutableStateOf(item?.address ?: "") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(if (item == null) stringResource(R.string.add_supplier) else stringResource(R.string.edit_supplier)) }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(stringResource(R.string.name_label)) }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(10.dp))
            OutlinedTextField(value = contact, onValueChange = { contact = it }, label = { Text(stringResource(R.string.contact_person)) }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(10.dp))
            OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text(stringResource(R.string.phone_label)) }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(10.dp))
            OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text(stringResource(R.string.category_label)) }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(10.dp))
            OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text(stringResource(R.string.address_label)) }, modifier = Modifier.fillMaxWidth(), singleLine = true, shape = RoundedCornerShape(10.dp))
            Text(stringResource(R.string.rating_label_short), fontSize = 13.sp, color = TextSecondary)
            Row { repeat(5) { i -> IconButton(onClick = { rating = i + 1 }) { Icon(Icons.Filled.Star, null, tint = if (i < rating) Warning else Color.LightGray, modifier = Modifier.size(28.dp)) } } }
        }
    }, confirmButton = {
        Button(onClick = { if (name.isNotBlank()) onConfirm(SupplierEntity(id = item?.id ?: 0, name = name, contact = contact, phone = phone, rating = rating, category = category, address = address)) }, shape = RoundedCornerShape(10.dp)) { Text(stringResource(R.string.confirm_btn)) }
    }, dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } })
}
