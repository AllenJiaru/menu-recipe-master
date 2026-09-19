package com.example.shiyu.ui.ai

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shiyu.api.ApiClient
import com.example.shiyu.api.AiChatMessageDto
import com.example.shiyu.api.AiChatSessionDto
import com.example.shiyu.data.repository.BackendRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatMessage(
    val id: Long? = null,
    val role: String,
    val content: String,
    val createTime: String? = null,
    val displayContent: String = content,
    val isStreaming: Boolean = false
)

@HiltViewModel
class AiChatViewModel @Inject constructor(
    private val backendRepository: BackendRepository
) : ViewModel() {

    val sessions = mutableStateListOf<AiChatSessionDto>()
    val messages = mutableStateListOf<ChatMessage>()
    val currentSessionId = mutableStateOf<Long?>(null)
    val currentSessionTitle = mutableStateOf("AI 美食助手")
    val isLoading = mutableStateOf(false)
    val providerName = mutableStateOf("")
    val inputText = mutableStateOf("")
    val userAvatarUrl = mutableStateOf("")

    private var typewriterJob: Job? = null

    init {
        loadProviderInfo()
        loadSessions()
        loadUserAvatar()
    }

    private fun loadUserAvatar() {
        val avatar = backendRepository.getSavedAvatar()
        userAvatarUrl.value = backendRepository.avatarUrl(avatar)
    }

    private fun loadProviderInfo() {
        viewModelScope.launch {
            try {
                val res = ApiClient.backendApi.aiInfo()
                if (res.code == 200) {
                    providerName.value = res.data?.currentProvider ?: "未配置"
                }
            } catch (_: Exception) {}
        }
    }

    fun loadSessions() {
        viewModelScope.launch {
            try {
                val res = ApiClient.backendApi.aiListSessions()
                if (res.code == 200) {
                    sessions.clear()
                    res.data?.let { sessions.addAll(it) }
                    if (currentSessionId.value == null && sessions.isNotEmpty()) {
                        val firstId = sessions[0].id
                        if (firstId != null) switchSession(firstId)
                    } else if (currentSessionId.value == null && sessions.isEmpty()) {
                        currentSessionTitle.value = "AI 美食助手"
                    }
                }
            } catch (_: Exception) {}
        }
    }

    fun switchSession(sessionId: Long) {
        typewriterJob?.cancel()
        currentSessionId.value = sessionId
        val session = sessions.find { it.id == sessionId }
        currentSessionTitle.value = session?.title ?: "会话"
        viewModelScope.launch {
            try {
                val res = ApiClient.backendApi.aiListMessages(sessionId)
                if (res.code == 200) {
                    messages.clear()
                    res.data?.forEach { m ->
                        messages.add(ChatMessage(m.id, m.role ?: "user", m.content ?: "", m.createTime, m.content ?: ""))
                    }
                }
            } catch (_: Exception) {}
        }
    }

    fun createNewSession() {
        viewModelScope.launch {
            try {
                val res = ApiClient.backendApi.aiCreateSession()
                if (res.code == 200) {
                    loadSessions()
                    res.data?.let {
                        currentSessionId.value = it.id
                        currentSessionTitle.value = it.title ?: "新对话"
                        messages.clear()
                    }
                }
            } catch (_: Exception) {}
        }
    }

    fun deleteSession(id: Long) {
        viewModelScope.launch {
            try {
                ApiClient.backendApi.aiDeleteSession(id)
                if (currentSessionId.value == id) {
                    typewriterJob?.cancel()
                    currentSessionId.value = null
                    currentSessionTitle.value = "AI 美食助手"
                    messages.clear()
                }
                loadSessions()
            } catch (_: Exception) {}
        }
    }

    fun sendMessage(text: String) {
        if (text.isBlank() || isLoading.value) return
        val msg = text.trim()
        inputText.value = ""
        messages.add(ChatMessage(role = "user", content = msg))
        isLoading.value = true
        viewModelScope.launch {
            try {
                val res = ApiClient.backendApi.aiChatSession(
                    com.example.shiyu.api.AiChatRequest(
                        sessionId = currentSessionId.value,
                        message = msg
                    )
                )
                if (res.code == 200) {
                    val content = res.data?.content ?: "抱歉，暂时无法回答"
                    val assistantMsg = ChatMessage(role = "assistant", content = content, displayContent = "", isStreaming = true)
                    messages.add(assistantMsg)
                    val idx = messages.size - 1
                    startTypewriter(idx, content)
                    if (currentSessionId.value == null) {
                        loadSessions()
                        if (sessions.isNotEmpty()) {
                            currentSessionId.value = sessions.first().id
                            currentSessionTitle.value = sessions.first().title ?: "新对话"
                        }
                    } else {
                        loadSessions()
                    }
                } else {
                    messages.add(ChatMessage(role = "assistant", content = "请求失败：${res.message}"))
                }
            } catch (e: Exception) {
                messages.add(ChatMessage(role = "assistant", content = "网络错误：${e.message}"))
            }
            isLoading.value = false
        }
    }

    private fun startTypewriter(idx: Int, fullContent: String) {
        typewriterJob?.cancel()
        typewriterJob = viewModelScope.launch {
            var displayIdx = 0
            val chunkSize = 3
            val intervalMs = 30L
            while (displayIdx < fullContent.length) {
                displayIdx = (displayIdx + chunkSize).coerceAtMost(fullContent.length)
                if (idx < messages.size) {
                    messages[idx] = messages[idx].copy(
                        displayContent = fullContent.substring(0, displayIdx),
                        isStreaming = displayIdx < fullContent.length
                    )
                }
                delay(intervalMs)
            }
            if (idx < messages.size) {
                messages[idx] = messages[idx].copy(displayContent = fullContent, isStreaming = false)
            }
        }
    }
}
