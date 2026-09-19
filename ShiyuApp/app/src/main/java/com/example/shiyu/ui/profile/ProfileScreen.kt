package com.example.shiyu.ui.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.example.shiyu.data.repository.BackendRepository
import com.example.shiyu.ui.theme.*
import com.example.shiyu.util.Constants
import com.example.shiyu.util.FileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val backendRepository: BackendRepository
) : ViewModel() {

    private val _nickname = mutableStateOf(backendRepository.getSavedNickname())
    val nickname: State<String> = _nickname

    private val _username = mutableStateOf(backendRepository.getSavedUsername())
    val username: State<String> = _username

    private val _role = mutableStateOf(backendRepository.getSavedRole())
    val role: State<String> = _role

    private val _avatar = mutableStateOf(backendRepository.getSavedAvatar())
    val avatar: State<String> = _avatar

    val roleName: String
        get() = when (_role.value) {
            Constants.ROLE_CHEF -> "主厨"
            Constants.ROLE_DINER -> "食客"
            else -> _role.value
        }

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _message = mutableStateOf("")
    val message: State<String> = _message

    val avatarUrl: String
        get() = backendRepository.avatarUrl(_avatar.value)

    fun updateNickname(v: String) { _nickname.value = v }

    fun saveProfile() {
        if (_isLoading.value) return
        val nickname = _nickname.value.trim()
        if (nickname.isEmpty()) {
            _message.value = "昵称不能为空"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _message.value = ""
            val result = backendRepository.updateProfile(nickname, _avatar.value)
            result.onSuccess {
                _message.value = it
            }.onFailure {
                _message.value = "保存失败：${it.message ?: "网络错误"}"
            }
            _isLoading.value = false
        }
    }

    fun uploadAvatar(localPath: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _message.value = "正在上传头像..."
            val result = backendRepository.uploadAvatar(File(localPath))
            if (result != null) {
                _avatar.value = result
                _message.value = "头像上传成功，点击保存生效"
            } else {
                _message.value = "头像上传失败"
            }
            _isLoading.value = false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onNavigateBack: () -> Unit
) {
    val viewModel: ProfileViewModel = androidx.hilt.navigation.compose.hiltViewModel()
    val context = LocalContext.current

    val avatarPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val savedPath = FileUtils.saveImage(context, it)
            if (savedPath != null) {
                viewModel.uploadAvatar(savedPath)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("个人中心") },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text("返回", color = TextPrimary)
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // 头像区域
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(listOf(PrimaryLight, Primary, PrimaryDark)),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.3f))
                            .clickable { avatarPickerLauncher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        if (viewModel.avatarUrl.isNotEmpty()) {
                            AsyncImage(
                                model = viewModel.avatarUrl,
                                contentDescription = "头像",
                                modifier = Modifier
                                    .size(96.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Text(
                                text = viewModel.nickname.value.take(1).ifEmpty { "食" },
                                fontSize = 40.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        }
                        // 相机角标
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("📷", fontSize = 14.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "点击头像更换",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = viewModel.nickname.value.ifEmpty { viewModel.username.value },
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = viewModel.roleName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Text(
                        text = "@${viewModel.username.value}",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 资料编辑
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "基本资料",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "昵称",
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = viewModel.nickname.value,
                        onValueChange = { viewModel.updateNickname(it) },
                        placeholder = { Text("请输入昵称", color = TextHint) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "用户名",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            modifier = Modifier.width(70.dp)
                        )
                        Text(
                            text = viewModel.username.value,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "角色",
                            fontSize = 13.sp,
                            color = TextSecondary,
                            modifier = Modifier.width(70.dp)
                        )
                        Text(
                            text = viewModel.roleName,
                            fontSize = 14.sp,
                            color = Primary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    if (viewModel.message.value.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = viewModel.message.value,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(SurfaceGray, RoundedCornerShape(10.dp))
                                .padding(12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Button(
                        onClick = { viewModel.saveProfile() },
                        enabled = !viewModel.isLoading.value,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        if (viewModel.isLoading.value) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(22.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("保存", fontSize = 15.sp, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
