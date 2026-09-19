package com.example.shiyu.ui.notice

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shiyu.R
import com.example.shiyu.api.NoticeDto
import com.example.shiyu.ui.components.EmptyState
import com.example.shiyu.ui.components.LoadingIndicator
import com.example.shiyu.ui.theme.*
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCenterScreen(
    onNavigateBack: () -> Unit
) {
    val viewModel: NoticeViewModel = hiltViewModel()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.notification_center)) },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text(stringResource(R.string.back), color = TextPrimary)
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
            when {
                viewModel.isLoading.value -> LoadingIndicator(modifier = Modifier.weight(1f))
                viewModel.notices.isEmpty() -> EmptyState(
                    icon = "🔔",
                    title = stringResource(R.string.no_data),
                    subtitle = stringResource(R.string.new_notifications_will_appear),
                    modifier = Modifier.weight(1f)
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(viewModel.notices) { notice ->
                        NotificationCard(notice = notice)
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(notice: NoticeDto) {
    var expanded by remember { mutableStateOf(false) }
    val priority = notice.priority ?: 0
    val priorityColor = when {
        priority >= 2 -> Error
        priority == 1 -> Warning
        else -> Primary
    }
    val priorityLabel = when {
        priority >= 2 -> stringResource(R.string.priority_urgent)
        priority == 1 -> stringResource(R.string.priority_important)
        else -> stringResource(R.string.priority_normal)
    }
    val content = notice.content ?: ""

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = priorityColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = priorityLabel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = priorityColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = notice.publishTime?.replace("T", " ")?.take(10) ?: "",
                    fontSize = 11.sp,
                    color = TextHint
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = notice.title ?: stringResource(R.string.announcement),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (expanded) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = content,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 20.sp
                )
            } else {
                Text(
                    text = content,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 19.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (content.length > 60) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(R.string.tap_to_expand),
                        fontSize = 11.sp,
                        color = Primary
                    )
                }
            }
        }
    }
}
