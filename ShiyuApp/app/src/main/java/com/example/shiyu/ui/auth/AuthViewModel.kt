package com.example.shiyu.ui.auth

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.State
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shiyu.api.LoginResponseData
import com.example.shiyu.data.repository.BackendRepository
import com.example.shiyu.util.Constants
import com.example.shiyu.util.RoleManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val backendRepository: BackendRepository,
    private val roleManager: RoleManager
) : ViewModel() {

    private val _username = mutableStateOf(backendRepository.getSavedUsername())
    val username: State<String> = _username

    private val _password = mutableStateOf("")
    val password: State<String> = _password

    private val _showPassword = mutableStateOf(false)
    val showPassword: State<Boolean> = _showPassword

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _errorMessage = mutableStateOf("")
    val errorMessage: State<String> = _errorMessage

    private val _loginSuccess = mutableStateOf<LoginResponseData?>(null)
    val loginSuccess: State<LoginResponseData?> = _loginSuccess

    private val _serverUrl = mutableStateOf(backendRepository.getServerUrl())
    val serverUrl: State<String> = _serverUrl

    fun updateUsername(v: String) { _username.value = v }
    fun updatePassword(v: String) { _password.value = v }
    fun toggleShowPassword() { _showPassword.value = !_showPassword.value }
    fun updateServerUrl(v: String) {
        _serverUrl.value = v
        backendRepository.saveServerUrl(v)
    }

    fun login() {
        if (_isLoading.value) return
        val username = _username.value.trim()
        val password = _password.value
        if (username.isEmpty() || password.isEmpty()) {
            _errorMessage.value = "请输入用户名和密码"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = ""
            val result = backendRepository.login(username, password)
            result.onSuccess { data ->
                viewModelScope.launch {
                    roleManager.setCurrentRole(data.role ?: Constants.ROLE_DINER)
                }
                _loginSuccess.value = data
            }.onFailure {
                _errorMessage.value = "登录失败：${it.message ?: "网络错误"}"
            }
            _isLoading.value = false
        }
    }
}

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val backendRepository: BackendRepository,
    private val roleManager: RoleManager
) : ViewModel() {

    private val _username = mutableStateOf("")
    val username: State<String> = _username

    private val _password = mutableStateOf("")
    val password: State<String> = _password

    private val _confirmPassword = mutableStateOf("")
    val confirmPassword: State<String> = _confirmPassword

    private val _nickname = mutableStateOf("")
    val nickname: State<String> = _nickname

    private val _role = mutableStateOf(Constants.ROLE_CHEF)
    val role: State<String> = _role

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _errorMessage = mutableStateOf("")
    val errorMessage: State<String> = _errorMessage

    private val _registerSuccess = mutableStateOf<LoginResponseData?>(null)
    val registerSuccess: State<LoginResponseData?> = _registerSuccess

    fun updateUsername(v: String) { _username.value = v }
    fun updatePassword(v: String) { _password.value = v }
    fun updateConfirmPassword(v: String) { _confirmPassword.value = v }
    fun updateNickname(v: String) { _nickname.value = v }
    fun updateRole(v: String) { _role.value = v }

    fun register() {
        if (_isLoading.value) return
        val username = _username.value.trim()
        val password = _password.value
        when {
            username.isEmpty() -> { _errorMessage.value = "请输入用户名"; return }
            password.length < 6 -> { _errorMessage.value = "密码至少6位"; return }
            password != _confirmPassword.value -> { _errorMessage.value = "两次输入的密码不一致"; return }
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = ""
            val result = backendRepository.register(
                username = username,
                password = password,
                nickname = _nickname.value.trim().ifEmpty { username },
                role = _role.value
            )
            result.onSuccess { data ->
                viewModelScope.launch {
                    roleManager.setCurrentRole(data.role ?: _role.value)
                }
                _registerSuccess.value = data
            }.onFailure {
                _errorMessage.value = "注册失败：${it.message ?: "网络错误"}"
            }
            _isLoading.value = false
        }
    }
}

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val backendRepository: BackendRepository
) : ViewModel() {

    private val _username = mutableStateOf("")
    val username: State<String> = _username

    private val _newPassword = mutableStateOf("")
    val newPassword: State<String> = _newPassword

    private val _confirmPassword = mutableStateOf("")
    val confirmPassword: State<String> = _confirmPassword

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _message = mutableStateOf("")
    val message: State<String> = _message

    private val _errorMessage = mutableStateOf("")
    val errorMessage: State<String> = _errorMessage

    fun updateUsername(v: String) { _username.value = v }
    fun updateNewPassword(v: String) { _newPassword.value = v }
    fun updateConfirmPassword(v: String) { _confirmPassword.value = v }

    fun reset() {
        if (_isLoading.value) return
        val username = _username.value.trim()
        when {
            username.isEmpty() -> { _errorMessage.value = "请输入用户名"; return }
            _newPassword.value.length < 6 -> { _errorMessage.value = "新密码至少6位"; return }
            _newPassword.value != _confirmPassword.value -> { _errorMessage.value = "两次输入的密码不一致"; return }
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = ""
            val result = backendRepository.resetPassword(username, _newPassword.value)
            result.onSuccess {
                _message.value = it
            }.onFailure {
                _errorMessage.value = "重置失败：${it.message ?: "网络错误"}"
            }
            _isLoading.value = false
        }
    }
}
