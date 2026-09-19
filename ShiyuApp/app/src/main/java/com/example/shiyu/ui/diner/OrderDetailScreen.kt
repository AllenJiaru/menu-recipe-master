package com.example.shiyu.ui.diner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.shiyu.ui.components.EmptyState
import com.example.shiyu.ui.components.LoadingIndicator
import com.example.shiyu.ui.theme.*
import com.example.shiyu.util.Constants
import com.example.shiyu.util.OrderStatusMap
import androidx.compose.ui.res.stringResource
import com.example.shiyu.R
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderId: Long,
    onNavigateBack: () -> Unit
) {
    val viewModel: OrderDetailViewModel = hiltViewModel()
    val order by viewModel.order
    val isLoading by viewModel.isLoading
    val isChef by viewModel.isChef

    var showCancelDialog by remember { mutableStateOf(false) }
    var cancelReason by remember { mutableStateOf("") }
    val chefRejectReason = stringResource(R.string.chef_reject_reason)
    val dinerCancelReason = stringResource(R.string.diner_cancel_reason)

    LaunchedEffect(orderId) {
        viewModel.loadOrder(orderId)
    }

    // 实时同步：未完结的订单每3秒轮询后端最新状态
    LaunchedEffect(order?.status) {
        val currentStatus = order?.status
        if (currentStatus == Constants.ORDER_STATUS_COMPLETED ||
            currentStatus == Constants.ORDER_STATUS_CANCELLED
        ) return@LaunchedEffect
        while (true) {
            kotlinx.coroutines.delay(3000)
            viewModel.refreshFromBackend()
            val latest = viewModel.order.value?.status
            if (latest == Constants.ORDER_STATUS_COMPLETED ||
                latest == Constants.ORDER_STATUS_CANCELLED
            ) break
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.order_detail)) },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text(stringResource(R.string.back), color = TextPrimary)
                    }
                }
            )
        }
    ) { paddingValues ->
        val current = order
        when {
            isLoading -> LoadingIndicator(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
            current == null -> EmptyState(
                icon = "📋",
                title = stringResource(R.string.order_not_found),
                subtitle = stringResource(R.string.order_deleted),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Background)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // ===== 状态卡片 =====
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = getStatusColor(current.status).copy(alpha = 0.1f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.current_status),
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = OrderStatusMap[current.status] ?: stringResource(R.string.status_unknown),
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = getStatusColor(current.status)
                            )
                        }
                        Text(
                            text = statusEmoji(current.status),
                            fontSize = 40.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ===== 状态时间轴 =====
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text(
                            text = stringResource(R.string.order_progress),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(14.dp))

                        if (current.status == Constants.ORDER_STATUS_CANCELLED) {
                            TimelineItem(stringResource(R.string.place_order), formatTime(current.order_time), done = true)
                            TimelineItem(stringResource(R.string.order_cancelled_label), if (current.reject_reason.isNotEmpty()) "${stringResource(R.string.reason_prefix)}${current.reject_reason}" else stringResource(R.string.order_cancelled_detail), done = false, isLast = true, color = OrderCancelled)
                        } else {
                            TimelineItem(stringResource(R.string.diner_placed), formatTime(current.order_time), done = true)
                            TimelineItem(
                                stringResource(R.string.chef_accepted),
                                current.accept_time?.let { formatTime(it) } ?: stringResource(R.string.waiting_accept),
                                done = current.status >= Constants.ORDER_STATUS_ACCEPTED
                            )
                            TimelineItem(
                                stringResource(R.string.cooking_label),
                                if (current.status >= Constants.ORDER_STATUS_COOKING) stringResource(R.string.chef_cooking) else stringResource(R.string.waiting_cook),
                                done = current.status >= Constants.ORDER_STATUS_COOKING
                            )
                            TimelineItem(
                                stringResource(R.string.complete_serving),
                                current.complete_time?.let { formatTime(it) } ?: stringResource(R.string.waiting_complete),
                                done = current.status >= Constants.ORDER_STATUS_COMPLETED,
                                isLast = true
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ===== 菜品信息 =====
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (current.recipe_image.isNotEmpty()) {
                            AsyncImage(
                                model = current.recipe_image,
                                contentDescription = current.recipe_name,
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(12.dp)),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(SurfaceGray),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🍽", fontSize = 28.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = current.recipe_name,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = stringResource(R.string.order_time_label, formatTime(current.order_time)),
                                fontSize = 12.sp,
                                color = TextHint
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ===== 备注 =====
                if (current.remark.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = stringResource(R.string.flavor_notes_header),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = current.remark,
                                fontSize = 14.sp,
                                color = TextSecondary,
                                lineHeight = 20.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // ===== 拒单/取消原因 =====
                if (current.status == Constants.ORDER_STATUS_CANCELLED && current.reject_reason.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = OrderCancelled.copy(alpha = 0.08f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = stringResource(R.string.cancel_reason),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = OrderCancelled
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = current.reject_reason,
                                fontSize = 14.sp,
                                color = TextSecondary
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // ===== 操作区 =====
                val actions = orderActions(current.status, isChef)
                if (actions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    when {
                        actions.contains("accept") -> Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    showCancelDialog = true
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Error)
                            ) {
                                Text(stringResource(R.string.reject_order))
                            }
                            Button(
                                onClick = { viewModel.updateOrderStatus(Constants.ORDER_STATUS_ACCEPTED) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = OrderAccepted)
                            ) {
                                Text(stringResource(R.string.accept_order), color = Color.White)
                            }
                        }
                        actions.contains("cook") -> Button(
                            onClick = { viewModel.updateOrderStatus(Constants.ORDER_STATUS_COOKING) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OrderCooking)
                        ) {
                            Text(stringResource(R.string.start_cooking), fontSize = 15.sp, color = Color.White)
                        }
                        actions.contains("complete") -> Button(
                            onClick = { viewModel.updateOrderStatus(Constants.ORDER_STATUS_COMPLETED) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OrderCompleted)
                        ) {
                            Text(stringResource(R.string.complete_serving_btn), fontSize = 15.sp, color = Color.White)
                        }
                        actions.contains("cancel") -> OutlinedButton(
                            onClick = { showCancelDialog = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Error)
                        ) {
                            Text(stringResource(R.string.cancel_order), fontSize = 15.sp)
                        }
                    }
                }

                if (viewModel.message.value.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = viewModel.message.value,
                        fontSize = 13.sp,
                        color = Success
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // 取消/拒绝原因弹窗
    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text(if (isChef) stringResource(R.string.reject_order) else stringResource(R.string.cancel_order)) },
            text = {
                Column {
                    Text(
                        text = stringResource(R.string.fill_reason),
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = cancelReason,
                        onValueChange = { cancelReason = it },
                        placeholder = { Text(if (isChef) stringResource(R.string.reason_hint_insufficient) else stringResource(R.string.reason_hint_busy), fontSize = 13.sp, color = TextHint) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateOrderStatus(
                        Constants.ORDER_STATUS_CANCELLED,
                        cancelReason.ifBlank { if (isChef) chefRejectReason else dinerCancelReason }
                    )
                    showCancelDialog = false
                }) {
                    Text(stringResource(R.string.confirm_btn), color = Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text(stringResource(R.string.reconsider), color = TextSecondary)
                }
            }
        )
    }
}

private fun orderActions(status: Int, isChef: Boolean): List<String> = when {
    isChef -> when (status) {
        Constants.ORDER_STATUS_PENDING -> listOf("accept")
        Constants.ORDER_STATUS_ACCEPTED -> listOf("cook")
        Constants.ORDER_STATUS_COOKING -> listOf("complete")
        else -> emptyList()
    }
    else -> when (status) {
        Constants.ORDER_STATUS_PENDING, Constants.ORDER_STATUS_ACCEPTED -> listOf("cancel")
        else -> emptyList()
    }
}

private fun statusEmoji(status: Int): String = when (status) {
    Constants.ORDER_STATUS_PENDING -> "🕐"
    Constants.ORDER_STATUS_ACCEPTED -> "👍"
    Constants.ORDER_STATUS_COOKING -> "👨‍🍳"
    Constants.ORDER_STATUS_COMPLETED -> "✅"
    Constants.ORDER_STATUS_CANCELLED -> "❌"
    else -> "📋"
}

@Composable
private fun TimelineItem(
    label: String,
    time: String,
    done: Boolean,
    isLast: Boolean = false,
    color: Color? = null
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(28.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(
                        color ?: if (done) Primary else TextHint.copy(alpha = 0.4f)
                    )
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(36.dp)
                        .background(if (done) Primary.copy(alpha = 0.4f) else TextHint.copy(alpha = 0.2f))
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(
            modifier = Modifier.padding(bottom = if (isLast) 0.dp else 18.dp)
        ) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = if (done) FontWeight.Bold else FontWeight.Medium,
                color = if (done) TextPrimary else TextHint
            )
            Text(
                text = time,
                fontSize = 11.sp,
                color = TextHint,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
fun TimeInfo(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = TextHint,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = TextPrimary
        )
    }
}

fun getStatusColor(status: Int): Color = when (status) {
    Constants.ORDER_STATUS_PENDING -> OrderPending
    Constants.ORDER_STATUS_ACCEPTED -> OrderAccepted
    Constants.ORDER_STATUS_COOKING -> OrderCooking
    Constants.ORDER_STATUS_COMPLETED -> OrderCompleted
    Constants.ORDER_STATUS_CANCELLED -> OrderCancelled
    else -> TextHint
}

fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("MM-dd HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
