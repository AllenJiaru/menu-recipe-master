package com.example.shiyu.ui.settings

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shiyu.data.repository.BackendRepository
import com.example.shiyu.sync.SyncManager
import com.example.shiyu.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val backendRepository: BackendRepository
) : ViewModel() {

    private val syncManager = SyncManager(context)

    private val _serverUrl = mutableStateOf(backendRepository.getServerUrl())
    val serverUrl: State<String> = _serverUrl

    private val _username = mutableStateOf(backendRepository.getSavedUsername())
    val username: State<String> = _username

    private val _nickname = mutableStateOf(backendRepository.getSavedNickname())
    val nickname: State<String> = _nickname

    private val _role = mutableStateOf(backendRepository.getSavedRole())
    val role: State<String> = _role

    private val _avatar = mutableStateOf(backendRepository.getSavedAvatar())
    val avatar: State<String> = _avatar

    val avatarUrl: String
        get() = backendRepository.avatarUrl(_avatar.value)

    val roleName: String
        get() = when (_role.value) {
            Constants.ROLE_CHEF -> "主厨"
            Constants.ROLE_DINER -> "食客"
            else -> if (_role.value.isEmpty()) "未登录" else _role.value
        }

    private val _password = mutableStateOf("")
    val password: State<String> = _password

    private val _isLoggedIn = mutableStateOf(backendRepository.isLoggedIn)
    val isLoggedIn: State<Boolean> = _isLoggedIn

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _statusMessage = mutableStateOf("")
    val statusMessage: State<String> = _statusMessage

    private val _connected = mutableStateOf<Boolean?>(null)
    val connected: State<Boolean?> = _connected

    private val _lastSyncTime = mutableStateOf(syncManager.getLastSyncTimeFormatted())
    val lastSyncTime: State<String> = _lastSyncTime

    private val _syncStatus = mutableStateOf("")
    val syncStatus: State<String> = _syncStatus

    fun updateServerUrl(url: String) {
        _serverUrl.value = url
        backendRepository.saveServerUrl(url)
    }

    fun updateUsername(value: String) { _username.value = value }
    fun updatePassword(value: String) { _password.value = value }

    fun refreshAccount() {
        _username.value = backendRepository.getSavedUsername()
        _nickname.value = backendRepository.getSavedNickname()
        _role.value = backendRepository.getSavedRole()
        _avatar.value = backendRepository.getSavedAvatar()
        _isLoggedIn.value = backendRepository.isLoggedIn
        _lastSyncTime.value = syncManager.getLastSyncTimeFormatted()
    }

    fun login() {
        if (_isLoading.value) return
        viewModelScope.launch {
            _isLoading.value = true
            _statusMessage.value = "正在登录..."
            val result = backendRepository.login(_username.value, _password.value)
            result.onSuccess {
                refreshAccount()
                _statusMessage.value = "登录成功"
            }.onFailure {
                _isLoggedIn.value = false
                _statusMessage.value = "登录失败：${it.message ?: "网络错误"}"
            }
            _isLoading.value = false
        }
    }

    fun logout() {
        backendRepository.logout()
        refreshAccount()
        _statusMessage.value = ""
    }

    fun changePassword(oldPassword: String, newPassword: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = backendRepository.changePassword(oldPassword, newPassword)
            result.onSuccess { onResult(it) }.onFailure { onResult("修改失败：${it.message ?: "网络错误"}") }
            _isLoading.value = false
        }
    }

    fun checkConnection() {
        viewModelScope.launch {
            _connected.value = null
            _connected.value = backendRepository.checkConnection()
        }
    }

    fun syncRecipes() {
        if (_isLoading.value) return
        viewModelScope.launch {
            _isLoading.value = true
            _statusMessage.value = "正在同步菜谱（含图片下载，可能需要一些时间）..."
            val result = backendRepository.pullRecipes()
            result.onSuccess {
                _statusMessage.value = it
            }.onFailure {
                _statusMessage.value = "同步失败：${it.message ?: "网络错误"}"
            }
            _isLoading.value = false
        }
    }

    fun syncOrders() {
        if (_isLoading.value) return
        viewModelScope.launch {
            _isLoading.value = true
            _statusMessage.value = "正在同步订单..."
            val result = backendRepository.pullOrders()
            result.onSuccess {
                _statusMessage.value = it
            }.onFailure {
                _statusMessage.value = "同步失败：${it.message ?: "网络错误"}"
            }
            _isLoading.value = false
        }
    }

    fun syncNow() {
        if (_isLoading.value) return
        viewModelScope.launch {
            _isLoading.value = true
            _syncStatus.value = "syncing"
            _statusMessage.value = "正在同步数据..."
            val result = syncManager.fullSync()
            if (result.success) {
                _statusMessage.value = result.message
                _syncStatus.value = "success"
            } else {
                _statusMessage.value = "同步失败：${result.message}"
                _syncStatus.value = "failed"
            }
            _lastSyncTime.value = syncManager.getLastSyncTimeFormatted()
            _isLoading.value = false
        }
    }
}
