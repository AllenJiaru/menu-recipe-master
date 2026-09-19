package com.example.shiyu.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shiyu.ui.theme.*
import com.example.shiyu.util.Constants
import com.example.shiyu.util.LocaleHelper
import com.example.shiyu.util.PermissionManager
import com.example.shiyu.R
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onLoggedOut: () -> Unit,
    onNavigateToProfile: () -> Unit = {},
    onNavigateToNotice: () -> Unit = {},
    onNavigateToMealPlan: () -> Unit = {},
    onNavigateToShopping: () -> Unit = {},
    onNavigateToDashboard: () -> Unit = {},
    onNavigateToInventory: () -> Unit = {},
    onNavigateToSupplier: () -> Unit = {},
    onNavigateToCategoryManage: () -> Unit = {},
    onNavigateToReview: () -> Unit = {},
    onNavigateToDataExport: () -> Unit = {},
    onNavigateToOperationLog: () -> Unit = {},
    onNavigateToSystemHealth: () -> Unit = {}
) {
    val viewModel: SettingsViewModel = hiltViewModel()
    var showAboutDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showChangePwdDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.refreshAccount() }

    // ── 对话框 ──
    if (showAboutDialog) AboutDialog(onDismiss = { showAboutDialog = false })
    if (showLogoutDialog) LogoutDialog(
        onConfirm = { showLogoutDialog = false; viewModel.logout(); onLoggedOut() },
        onDismiss = { showLogoutDialog = false }
    )
    if (showChangePwdDialog) ChangePasswordDialog(
        onDismiss = { showChangePwdDialog = false },
        onSubmitWithCallback = { old, new, cb -> viewModel.changePassword(old, new, cb) }
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(stringResource(R.string.settings), fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White
                )
            )
        },
        containerColor = Background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // ═══════════ 账户卡片 ═══════════
            AccountCard(
                nickname = viewModel.nickname.value.ifEmpty { viewModel.username.value },
                username = viewModel.username.value,
                roleName = viewModel.roleName,
                role = viewModel.role.value,
                avatarUrl = viewModel.avatarUrl,
                isLoggedIn = viewModel.isLoggedIn.value,
                onClick = onNavigateToProfile,
                onLogout = { showLogoutDialog = true }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ═══════════ 功能入口 ═══════════
            SettingsSection {
                SettingsRow(
                    icon = Icons.Outlined.Person,
                    iconTint = Primary,
                    title = stringResource(R.string.personal_center),
                    subtitle = stringResource(R.string.change_nickname_avatar),
                    onClick = onNavigateToProfile
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Outlined.Notifications,
                    iconTint = Warning,
                    title = stringResource(R.string.announcements),
                    subtitle = stringResource(R.string.view_announcements),
                    onClick = onNavigateToNotice
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Outlined.CalendarMonth,
                    iconTint = Info,
                    title = stringResource(R.string.weekly_meal_plan),
                    subtitle = stringResource(R.string.plan_weekly_meals),
                    onClick = onNavigateToMealPlan
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Outlined.ShoppingCart,
                    iconTint = Success,
                    title = stringResource(R.string.shopping_cart),
                    subtitle = stringResource(R.string.manage_ingredients),
                    onClick = onNavigateToShopping
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ═══════════ 管理功能（仅主厨/管理员可见） ═══════════
            if (PermissionManager.isManager(viewModel.role.value)) {
                SettingsSection {
                    if (PermissionManager.hasPermission(viewModel.role.value, PermissionManager.FEATURE_DASHBOARD)) {
                        SettingsRow(
                            icon = Icons.Outlined.Dashboard,
                            iconTint = Primary,
                        title = stringResource(R.string.data_statistics),
                        subtitle = stringResource(R.string.view_recipe_order_stats),
                            onClick = onNavigateToDashboard
                        )
                        SettingsDivider()
                    }
                    if (PermissionManager.hasPermission(viewModel.role.value, PermissionManager.FEATURE_CATEGORY_MANAGE)) {
                        SettingsRow(
                            icon = Icons.Outlined.Category,
                            iconTint = Info,
                        title = stringResource(R.string.category_management),
                        subtitle = stringResource(R.string.manage_categories),
                            onClick = onNavigateToCategoryManage
                        )
                        SettingsDivider()
                    }
                    if (PermissionManager.hasPermission(viewModel.role.value, PermissionManager.FEATURE_INVENTORY)) {
                        SettingsRow(
                            icon = Icons.Outlined.Inventory,
                            iconTint = Warning,
                        title = stringResource(R.string.inventory_management),
                        subtitle = stringResource(R.string.manage_ingredients_stock),
                            onClick = onNavigateToInventory
                        )
                        SettingsDivider()
                    }
                    if (PermissionManager.hasPermission(viewModel.role.value, PermissionManager.FEATURE_SUPPLIER)) {
                        SettingsRow(
                            icon = Icons.Outlined.LocalShipping,
                            iconTint = Color(0xFF722ED1),
                        title = stringResource(R.string.supplier_management),
                        subtitle = stringResource(R.string.manage_suppliers),
                            onClick = onNavigateToSupplier
                        )
                        SettingsDivider()
                    }
                    if (PermissionManager.hasPermission(viewModel.role.value, PermissionManager.FEATURE_REVIEW)) {
                        SettingsRow(
                            icon = Icons.Outlined.RateReview,
                            iconTint = Success,
                        title = stringResource(R.string.recipe_review),
                        subtitle = stringResource(R.string.review_submitted_recipes),
                            onClick = onNavigateToReview
                        )
                        SettingsDivider()
                    }
                    if (PermissionManager.hasPermission(viewModel.role.value, PermissionManager.FEATURE_DATA_EXPORT)) {
                        SettingsRow(
                            icon = Icons.Outlined.FileDownload,
                            iconTint = Info,
                        title = stringResource(R.string.data_export),
                        subtitle = stringResource(R.string.export_recipe_order_data),
                            onClick = onNavigateToDataExport
                        )
                        SettingsDivider()
                    }
                    if (PermissionManager.hasPermission(viewModel.role.value, PermissionManager.FEATURE_OPERATION_LOG)) {
                        SettingsRow(
                            icon = Icons.Outlined.History,
                            iconTint = TextSecondary,
                        title = stringResource(R.string.operation_log),
                        subtitle = stringResource(R.string.view_system_operation_log),
                            onClick = onNavigateToOperationLog
                        )
                        SettingsDivider()
                    }
                    if (PermissionManager.hasPermission(viewModel.role.value, PermissionManager.FEATURE_SYSTEM_HEALTH)) {
                        SettingsRow(
                            icon = Icons.Outlined.MonitorHeart,
                            iconTint = Color(0xFF722ED1),
                        title = stringResource(R.string.system_monitoring),
                        subtitle = stringResource(R.string.view_system_status),
                            onClick = onNavigateToSystemHealth
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // ═══════════ 账号安全 ═══════════
            SettingsSection {
                SettingsRow(
                    icon = Icons.Outlined.Lock,
                    iconTint = Color(0xFF722ED1),
                    title = stringResource(R.string.change_password),
                    subtitle = stringResource(R.string.change_login_password),
                    onClick = { showChangePwdDialog = true }
                )
                SettingsDivider()
                SettingsRow(
                    icon = Icons.Outlined.Info,
                    iconTint = TextSecondary,
                    title = stringResource(R.string.about),
                    subtitle = stringResource(R.string.version_info, Constants.APP_VERSION),
                    onClick = { showAboutDialog = true }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // ═══════════ 外观设置 ═══════════
            ThemeToggleSection()

            Spacer(modifier = Modifier.height(24.dp))

            // ═══════════ 语言设置 ═══════════
            LanguageSelectorSection()

            Spacer(modifier = Modifier.height(24.dp))

            // ═══════════ 云端同步 ═══════════
            SettingsSection {
                // 连接状态
                ConnectionStatusRow(
                    connected = viewModel.connected.value,
                    onCheck = { viewModel.checkConnection() }
                )
                SettingsDivider()

                // 服务器地址
                SettingsInputRow(
                    icon = Icons.Outlined.Language,
                    label = stringResource(R.string.server_address),
                    value = viewModel.serverUrl.value,
                    onValueChange = { viewModel.updateServerUrl(it) },
                    placeholder = "http://192.168.1.3:8081"
                )
                SettingsDivider()

                // 用户名
                SettingsInputRow(
                    icon = Icons.Outlined.Badge,
                    label = stringResource(R.string.username),
                    value = viewModel.username.value,
                    onValueChange = { viewModel.updateUsername(it) },
                    placeholder = stringResource(R.string.username_hint)
                )
                SettingsDivider()

                // 密码
                SettingsInputRow(
                    icon = Icons.Outlined.Key,
                    label = stringResource(R.string.password),
                    value = viewModel.password.value,
                    onValueChange = { viewModel.updatePassword(it) },
                    placeholder = stringResource(R.string.password_hint),
                    isPassword = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 登录按钮
                Button(
                    onClick = { viewModel.login() },
                    enabled = !viewModel.isLoading.value,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (viewModel.isLoading.value) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        if (viewModel.isLoggedIn.value) stringResource(R.string.re_login) else stringResource(R.string.login),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 同步按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { viewModel.syncRecipes() },
                        enabled = !viewModel.isLoading.value,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = !viewModel.isLoading.value)
                    ) {
                        Icon(Icons.Outlined.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.sync_recipes), fontSize = 13.sp)
                    }
                    OutlinedButton(
                        onClick = { viewModel.syncOrders() },
                        enabled = !viewModel.isLoading.value,
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Primary),
                        border = ButtonDefaults.outlinedButtonBorder(enabled = !viewModel.isLoading.value)
                    ) {
                        Icon(Icons.Outlined.Inventory2, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(stringResource(R.string.sync_orders), fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 立即同步按钮
                Button(
                    onClick = { viewModel.syncNow() },
                    enabled = !viewModel.isLoading.value,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (viewModel.isLoading.value && viewModel.syncStatus.value == "syncing") {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    } else {
                        Icon(Icons.Outlined.Sync, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        if (viewModel.isLoading.value && viewModel.syncStatus.value == "syncing") stringResource(R.string.syncing) else stringResource(R.string.sync_now),
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 上次同步时间
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = TextHint,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.last_sync_time, viewModel.lastSyncTime.value),
                        fontSize = 12.sp,
                        color = TextHint
                    )
                }

                // 状态消息
                if (viewModel.statusMessage.value.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = when {
                                viewModel.syncStatus.value == "syncing" -> Info.copy(alpha = 0.08f)
                                viewModel.statusMessage.value.contains("成功") || viewModel.statusMessage.value.contains("完成") -> Success.copy(alpha = 0.08f)
                                viewModel.syncStatus.value == "failed" || viewModel.statusMessage.value.contains("失败") -> Error.copy(alpha = 0.08f)
                                else -> SurfaceGray
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                when {
                                    viewModel.syncStatus.value == "syncing" -> Icons.Filled.Sync
                                    viewModel.statusMessage.value.contains("成功") || viewModel.statusMessage.value.contains("完成") -> Icons.Filled.CheckCircle
                                    viewModel.syncStatus.value == "failed" || viewModel.statusMessage.value.contains("失败") -> Icons.Filled.Error
                                    else -> Icons.Filled.Info
                                },
                                contentDescription = null,
                                tint = when {
                                    viewModel.syncStatus.value == "syncing" -> Info
                                    viewModel.statusMessage.value.contains("成功") || viewModel.statusMessage.value.contains("完成") -> Success
                                    viewModel.syncStatus.value == "failed" || viewModel.statusMessage.value.contains("失败") -> Error
                                    else -> TextSecondary
                                },
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = viewModel.statusMessage.value,
                                fontSize = 12.sp,
                                color = TextSecondary,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(40.dp))

            // ═══════════ 退出登录按钮 ═══════════
            if (viewModel.isLoggedIn.value) {
                Button(
                    onClick = { showLogoutDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 1.dp)
                ) {
                    Icon(Icons.Outlined.Logout, contentDescription = null, tint = Error, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.exit_login), fontSize = 15.sp, color = Error, fontWeight = FontWeight.Medium)
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // 版本信息
            Text(
                text = "${Constants.APP_NAME} v${Constants.APP_VERSION}",
                fontSize = 12.sp,
                color = TextHint,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

// ═══════════ 账户卡片 ═══════════
@Composable
private fun AccountCard(
    nickname: String,
    username: String,
    roleName: String,
    role: String,
    avatarUrl: String,
    isLoggedIn: Boolean,
    onClick: () -> Unit,
    onLogout: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(8.dp, RoundedCornerShape(24.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(KawaiiPink, PrimaryLight, KawaiiLavender.copy(alpha = 0.6f))
                    )
                )
                .padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // 头像 - 卡哇伊风格
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .shadow(4.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    if (avatarUrl.isNotEmpty()) {
                        coil.compose.AsyncImage(
                            model = avatarUrl,
                            contentDescription = stringResource(R.string.avatar),
                            modifier = Modifier
                                .size(68.dp)
                                .clip(CircleShape),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    } else {
                        Text(
                            text = nickname.take(1).ifEmpty { "🍽" },
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // 用户信息
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = nickname.ifEmpty { stringResource(R.string.not_logged_in) },
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (role.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.3f)
                            ) {
                                Text(
                                    text = roleName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (username.isNotEmpty()) "@$username" else stringResource(R.string.click_to_login),
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }

                // 右箭头
                Icon(
                    Icons.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// ═══════════ 分组容器 - 卡哇伊风格 ═══════════
@Composable
private fun SettingsSection(content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(content = content)
    }
}

// ═══════════ 设置行（带图标 + 箭头） ═══════════
@Composable
private fun SettingsRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 图标 - 卡哇伊风格
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconTint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = TextHint,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
        Icon(
            Icons.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color(0xFFCCCCCC),
            modifier = Modifier.size(22.dp)
        )
    }
}

// ═══════════ 输入行 ═══════════
@Composable
private fun SettingsInputRow(
    icon: ImageVector,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(SurfaceGray),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, fontSize = 14.sp, color = TextHint) },
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            singleLine = true,
            visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
            keyboardOptions = if (isPassword) KeyboardOptions(keyboardType = KeyboardType.Password) else KeyboardOptions.Default,
            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp, color = TextPrimary),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Primary.copy(alpha = 0.4f),
                unfocusedContainerColor = SurfaceGray,
                focusedContainerColor = SurfaceGray
            ),
            shape = RoundedCornerShape(10.dp)
        )
    }
}

// ═══════════ 连接状态行 ═══════════
@Composable
private fun ConnectionStatusRow(
    connected: Boolean?,
    onCheck: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(SurfaceGray),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Outlined.Wifi, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.server_connection), fontSize = 15.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            when (connected) {
                                true -> Success
                                false -> Error
                                else -> TextHint
                            }
                        )
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = when (connected) {
                        true -> stringResource(R.string.connected)
                        false -> stringResource(R.string.cannot_connect)
                        else -> stringResource(R.string.not_checked)
                    },
                    fontSize = 12.sp,
                    color = when (connected) {
                        true -> Success
                        false -> Error
                        else -> TextHint
                    }
                )
            }
        }
        TextButton(onClick = onCheck) {
            Text(stringResource(R.string.check), fontSize = 13.sp, color = Primary)
        }
    }
}

// ═══════════ 分割线 ═══════════
@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(start = 66.dp, end = 16.dp),
        color = DividerColor,
        thickness = 0.5.dp
    )
}

// ═══════════ 关于对话框 ═══════════
@Composable
private fun AboutDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Outlined.Info, contentDescription = null, tint = Primary, modifier = Modifier.size(40.dp))
        },
        title = {
            Text(Constants.APP_NAME, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.version_info, Constants.APP_VERSION), fontSize = 13.sp, color = TextSecondary)
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    stringResource(R.string.about_description),
                    fontSize = 13.sp,
                    color = TextSecondary,
                    lineHeight = 20.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(stringResource(R.string.got_it), color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }
    )
}

// ═══════════ 退出对话框 ═══════════
@Composable
private fun LogoutDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Outlined.Logout, contentDescription = null, tint = Error, modifier = Modifier.size(40.dp))
        },
        title = { Text(stringResource(R.string.logout), fontWeight = FontWeight.Bold) },
        text = { Text(stringResource(R.string.confirm_logout), fontSize = 14.sp, color = TextSecondary) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Error),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(stringResource(R.string.exit), color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(stringResource(R.string.cancel), color = TextSecondary)
            }
        }
    )
}

// ═══════════ 修改密码对话框 ═══════════
@Composable
private fun ChangePasswordDialog(
    onDismiss: () -> Unit,
    onSubmitWithCallback: (String, String, (String) -> Unit) -> Unit
) {
    var oldPwd by remember { mutableStateOf("") }
    var newPwd by remember { mutableStateOf("") }
    var confirmPwd by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var submitted by remember { mutableStateOf(false) }
    val pleaseEnterPwd = stringResource(R.string.please_enter_password)
    val pwdTooShort = stringResource(R.string.password_too_short)
    val pwdMismatch = stringResource(R.string.password_mismatch)
    val pwdChanged = stringResource(R.string.password_changed)

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(Icons.Outlined.Lock, contentDescription = null, tint = Primary, modifier = Modifier.size(40.dp))
        },
        title = { Text(stringResource(R.string.change_password), fontWeight = FontWeight.Bold) },
        text = {
            Column {
                OutlinedTextField(
                    value = oldPwd,
                    onValueChange = { oldPwd = it },
                    label = { Text(stringResource(R.string.current_password), fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(10.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = newPwd,
                    onValueChange = { newPwd = it },
                    label = { Text(stringResource(R.string.new_password), fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(10.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = confirmPwd,
                    onValueChange = { confirmPwd = it },
                    label = { Text(stringResource(R.string.confirm_new_password), fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(10.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
                )
                if (message.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = message,
                        fontSize = 12.sp,
                        color = if (submitted) Success else Error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        oldPwd.isBlank() -> message = pleaseEnterPwd
                        newPwd.length < 6 -> message = pwdTooShort
                        newPwd != confirmPwd -> message = pwdMismatch
                        else -> {
                            onSubmitWithCallback(oldPwd, newPwd) { msg ->
                                message = msg
                                submitted = msg.startsWith(pwdChanged)
                                if (submitted) onDismiss()
                            }
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(stringResource(R.string.confirm_change), color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(stringResource(R.string.cancel), color = TextSecondary)
            }
        }
    )
}

// ═══════════ 主题切换 ═══════════
@Composable
private fun ThemeToggleSection() {
    val context = LocalContext.current
    val themeManager = remember { ThemeManager(context) }
    val scope = rememberCoroutineScope()
    val currentMode by themeManager.themeMode.collectAsState(initial = ThemeMode.SYSTEM)

    SettingsSection {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.DarkMode, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = stringResource(R.string.appearance_mode),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
        }

        SettingsDivider()

        ThemeMode.values().forEach { mode ->
            val label = when (mode) {
                ThemeMode.SYSTEM -> stringResource(R.string.system_default)
                ThemeMode.LIGHT -> stringResource(R.string.light_mode)
                ThemeMode.DARK -> stringResource(R.string.dark_mode)
            }
            val icon = when (mode) {
                ThemeMode.SYSTEM -> Icons.Outlined.SettingsBrightness
                ThemeMode.LIGHT -> Icons.Outlined.LightMode
                ThemeMode.DARK -> Icons.Outlined.DarkMode
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        scope.launch { themeManager.setThemeMode(mode) }
                    }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (currentMode == mode) Primary.copy(alpha = 0.12f)
                            else Color.Transparent
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = if (currentMode == mode) Primary else TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = label,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                if (currentMode == mode) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = stringResource(R.string.selected),
                        tint = Primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            if (mode != ThemeMode.values().last()) {
                SettingsDivider()
            }
        }
    }
}

// ═══════════ 语言选择 ═══════════
@Composable
private fun LanguageSelectorSection() {
    val context = LocalContext.current
    val currentLanguage by remember { mutableStateOf(LocaleHelper.getSavedLanguage(context)) }

    SettingsSection {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Outlined.Language, contentDescription = null, tint = Primary, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = stringResource(R.string.language),
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            )
        }

        SettingsDivider()

        LanguageOption(
            label = "中文",
            isSelected = currentLanguage == "zh",
            onClick = {
                LocaleHelper.updateLocale(context, "zh")
                val activity = (context as? android.app.Activity)
                activity?.recreate()
            }
        )
        SettingsDivider()
        LanguageOption(
            label = "English",
            isSelected = currentLanguage == "en",
            onClick = {
                LocaleHelper.updateLocale(context, "en")
                val activity = (context as? android.app.Activity)
                activity?.recreate()
            }
        )
    }
}

@Composable
private fun LanguageOption(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (isSelected) Primary.copy(alpha = 0.12f) else Color.Transparent
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Outlined.Language,
                contentDescription = null,
                tint = if (isSelected) Primary else TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )
        if (isSelected) {
            Icon(
                Icons.Filled.Check,
                contentDescription = stringResource(R.string.selected),
                tint = Primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
