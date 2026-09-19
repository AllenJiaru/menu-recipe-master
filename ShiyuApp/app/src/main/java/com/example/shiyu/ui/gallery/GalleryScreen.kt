package com.example.shiyu.ui.gallery

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.example.shiyu.data.db.entity.GalleryImageEntity
import com.example.shiyu.ui.components.EmptyState
import com.example.shiyu.ui.components.LoadingIndicator
import com.example.shiyu.ui.theme.*
import com.example.shiyu.util.FileUtils
import androidx.compose.ui.res.stringResource
import com.example.shiyu.R
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GalleryScreen(
    onNavigateBack: () -> Unit
) {
    val viewModel: GalleryViewModel = hiltViewModel()
    val context = LocalContext.current
    var isUploading by remember { mutableStateOf(false) }
    var refreshing by remember { mutableStateOf(false) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris: List<Uri> ->
        if (uris.isEmpty()) return@rememberLauncherForActivityResult
        isUploading = true
        var successCount = 0
        uris.forEach { uri ->
            val savedPath = FileUtils.saveImage(context, uri)
            if (savedPath != null) {
                viewModel.addImage(savedPath)
                successCount++
            }
        }
        isUploading = false
        viewModel.loadImages()
    }

    LaunchedEffect(refreshing) {
        if (refreshing) {
            viewModel.loadImages()
            delay(300)
            refreshing = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.food_gallery)) },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text(stringResource(R.string.back), color = TextPrimary)
                    }
                },
                actions = {
                    if (isUploading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Primary,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(12.dp))
                    }
                    TextButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                        Text("+", fontSize = 24.sp, color = Primary)
                    }
                }
            )
        }
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = refreshing,
            onRefresh = { refreshing = true },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Background)
        ) {
            GalleryGrid(
                images = viewModel.images,
                isLoading = viewModel.isLoading.value,
                isUploading = isUploading,
                onAddClick = { imagePickerLauncher.launch("image/*") },
                onDelete = { viewModel.deleteImage(it) },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun GalleryGrid(
    images: List<GalleryImageEntity>,
    isLoading: Boolean,
    isUploading: Boolean = false,
    onAddClick: () -> Unit = {},
    onDelete: (GalleryImageEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    when {
        isLoading -> LoadingIndicator(modifier = modifier)
        images.isEmpty() -> Column(
            modifier = modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            EmptyState(
                icon = "🖼",
                title = stringResource(R.string.no_photos),
                subtitle = stringResource(R.string.add_photos_hint),
                modifier = Modifier.weight(1f)
            )
            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(stringResource(R.string.add_photo), fontSize = 15.sp, color = Color.White)
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        else -> Column(modifier = modifier) {
            // 照片数量提示
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.photo_count, images.size),
                    fontSize = 13.sp,
                    color = TextSecondary
                )
                Spacer(Modifier.weight(1f))
                if (isUploading) {
                    Text(
                        text = stringResource(R.string.uploading),
                        fontSize = 13.sp,
                        color = Primary
                    )
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(images, key = { it.id }) { image ->
                    GalleryImageItem(
                        image = image,
                        onDelete = { onDelete(image) }
                    )
                }
                // 底部添加按钮
                item {
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(SurfaceGray)
                            .clickable { onAddClick() },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("+", fontSize = 28.sp, color = Primary)
                            Text(stringResource(R.string.add_btn), fontSize = 11.sp, color = TextHint)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GalleryImageItem(
    image: GalleryImageEntity,
    onDelete: () -> Unit
) {
    var showDelete by remember { mutableStateOf(false) }
    var showPreview by remember { mutableStateOf(false) }

    // 删除确认弹窗
    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text(stringResource(R.string.delete_photo)) },
            text = { Text(stringResource(R.string.confirm_delete_photo)) },
            confirmButton = {
                TextButton(onClick = {
                    showDelete = false
                    onDelete()
                }) {
                    Text(stringResource(R.string.delete), color = Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDelete = false }) {
                    Text(stringResource(R.string.cancel), color = TextSecondary)
                }
            }
        )
    }

    // 全屏预览弹窗
    if (showPreview) {
        Dialog(
            onDismissRequest = { showPreview = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.95f))
                    .clickable { showPreview = false }
            ) {
                AsyncImage(
                    model = image.image_path,
                    contentDescription = image.description,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentScale = ContentScale.Fit
                )
                // 关闭按钮
                Text(
                    text = "✕",
                    fontSize = 20.sp,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .clickable { showPreview = false }
                        .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )
                // 描述文字
                if (image.description.isNotEmpty()) {
                    Text(
                        text = image.description,
                        fontSize = 14.sp,
                        color = Color.White,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.5f))
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
            }
        }
    }

    // 图片卡片
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceGray)
    ) {
        AsyncImage(
            model = image.image_path,
            contentDescription = image.description,
            modifier = Modifier
                .fillMaxSize()
                .clickable { showPreview = true },
            contentScale = ContentScale.Crop
        )
        // 描述标签
        if (image.description.isNotEmpty()) {
            Text(
                text = image.description,
                fontSize = 10.sp,
                color = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .background(Color.Black.copy(alpha = 0.4f))
                    .padding(horizontal = 6.dp, vertical = 3.dp),
                maxLines = 1
            )
        }
        // 删除按钮
        Text(
            text = "✕",
            fontSize = 12.sp,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                .padding(horizontal = 5.dp, vertical = 2.dp)
                .clickable { showDelete = true }
        )
    }
}
