package com.example.shiyu.ui.notice

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shiyu.R
import com.example.shiyu.api.NoticeDto
import com.example.shiyu.data.repository.BackendRepository
import com.example.shiyu.ui.components.EmptyState
import com.example.shiyu.ui.components.LoadingIndicator
import com.example.shiyu.ui.theme.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoticeViewModel @Inject constructor(
    private val backendRepository: BackendRepository
) : ViewModel() {

    private val _notices = mutableStateListOf<NoticeDto>()
    val notices: List<NoticeDto> = _notices

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    init {
        loadNotices()
    }

    fun loadNotices() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val list = backendRepository.getNotices()
                _notices.clear()
                _notices.addAll(list)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoticeScreen(
    onNavigateBack: () -> Unit
) {
    val viewModel: NoticeViewModel = androidx.hilt.navigation.compose.hiltViewModel()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.announcement)) },
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
                    icon = "📢",
                    title = stringResource(R.string.no_announcements),
                    subtitle = stringResource(R.string.announcements_will_appear),
                    modifier = Modifier.weight(1f)
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(viewModel.notices) { notice ->
                        NoticeCard(notice)
                    }
                }
            }
        }
    }
}

@Composable
private fun NoticeCard(notice: NoticeDto) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if ((notice.priority ?: 0) >= 2) "🔴" else "📢",
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = notice.title ?: stringResource(R.string.announcement),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = notice.publishTime?.replace("T", " ")?.take(10) ?: "",
                    fontSize = 11.sp,
                    color = TextHint
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = notice.content ?: "",
                fontSize = 13.sp,
                color = TextSecondary,
                lineHeight = 20.sp,
                maxLines = if (expanded) Int.MAX_VALUE else 3
            )
            if ((notice.content ?: "").length > 60) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (expanded) "${stringResource(R.string.collapse)} ▲" else "${stringResource(R.string.expand)} ▼",
                    fontSize = 12.sp,
                    color = Primary
                )
            }
        }
    }
}
