package com.example.shiyu.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.shiyu.data.db.entity.OrderEntity
import com.example.shiyu.ui.theme.*
import com.example.shiyu.util.Constants
import com.example.shiyu.util.OrderStatusMap
import androidx.compose.ui.res.stringResource
import com.example.shiyu.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun orderStatusColor(status: Int): Color = when (status) {
    0 -> OrderPending
    1 -> OrderAccepted
    2 -> OrderCooking
    3 -> OrderCompleted
    else -> OrderCancelled
}

fun formatOrderTime(time: Long?): String {
    if (time == null) return "-"
    return SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()).format(Date(time))
}

@Composable
fun OrderCard(
    order: OrderEntity,
    isChef: Boolean,
    onClick: () -> Unit,
    onAccept: () -> Unit = {},
    onReject: (String) -> Unit = {},
    onStartCooking: () -> Unit = {},
    onComplete: () -> Unit = {},
    onCancel: (String) -> Unit = {}
) {
    var showReasonDialog by remember { mutableStateOf(false) }
    var reason by remember { mutableStateOf("") }

    if (showReasonDialog) {
        AlertDialog(
            onDismissRequest = { showReasonDialog = false },
            title = { Text(if (isChef) stringResource(R.string.reject_order) else stringResource(R.string.cancel_order)) },
            text = {
                Column {
                    Text(text = stringResource(R.string.fill_reason), fontSize = 13.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        placeholder = { Text(if (isChef) stringResource(R.string.reason_hint_insufficient) else stringResource(R.string.reason_hint_busy), fontSize = 13.sp, color = TextHint) },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showReasonDialog = false
                    if (isChef) onReject(reason) else onCancel(reason)
                }) {
                    Text(stringResource(R.string.confirm_btn), color = Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReasonDialog = false }) {
                    Text(stringResource(R.string.reconsider), color = TextSecondary)
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (order.recipe_image.isNotEmpty()) {
                    AsyncImage(
                        model = order.recipe_image,
                        contentDescription = order.recipe_name,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(10.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceGray),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🍽", fontSize = 22.sp)
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = order.recipe_name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.order_time_label, formatOrderTime(order.order_time)),
                        fontSize = 12.sp,
                        color = TextHint
                    )
                    if (order.remark.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.remark_label, order.remark),
                            fontSize = 12.sp,
                            color = TextSecondary,
                            maxLines = 1
                        )
                    }
                    if (order.status == Constants.ORDER_STATUS_CANCELLED && order.reject_reason.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = stringResource(R.string.reason_label, order.reject_reason),
                            fontSize = 11.sp,
                            color = OrderCancelled,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                StatusBadge(order.status)
            }

            val actions = orderActions(order.status, isChef)
            if (actions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = DividerColor)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    actions.forEach { action ->
                        Button(
                            onClick = {
                                when (action) {
                                    "accept" -> onAccept()
                                    "reject" -> showReasonDialog = true
                                    "cook" -> onStartCooking()
                                    "complete" -> onComplete()
                                    "cancel" -> showReasonDialog = true
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 4.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = actionColor(action),
                                contentColor = Color.White
                            ),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(stringResource(actionLabelRes(action)), fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

private fun orderActions(status: Int, isChef: Boolean): List<String> = when {
    isChef -> when (status) {
        0 -> listOf("accept", "reject")
        1 -> listOf("cook")
        2 -> listOf("complete")
        else -> emptyList()
    }
    else -> when (status) {
        0, 1 -> listOf("cancel")
        else -> emptyList()
    }
}

private fun actionLabelRes(action: String): Int = when (action) {
    "accept" -> R.string.accept_order_btn
    "reject" -> R.string.reject_btn
    "cook" -> R.string.start_cooking
    "complete" -> R.string.complete_action
    "cancel" -> R.string.cancel_order
    else -> R.string.app_name
}

private fun actionColor(action: String): Color = when (action) {
    "accept" -> OrderAccepted
    "reject" -> OrderCancelled
    "cook" -> OrderCooking
    "complete" -> OrderCompleted
    "cancel" -> OrderCancelled
    else -> Primary
}

@Composable
fun StatusBadge(status: Int) {
    val color = orderStatusColor(status)
    Text(
        text = OrderStatusMap[status] ?: stringResource(R.string.status_unknown),
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = color,
        modifier = Modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}
