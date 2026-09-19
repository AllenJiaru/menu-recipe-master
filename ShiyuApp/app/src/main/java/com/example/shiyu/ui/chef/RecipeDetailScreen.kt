package com.example.shiyu.ui.chef

import android.content.Intent
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.VibrationEffect
import android.os.Vibrator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.shiyu.api.CommentStatsDto
import com.example.shiyu.ui.components.EmptyState
import com.example.shiyu.ui.components.LoadingIndicator
import com.example.shiyu.ui.theme.*
import com.example.shiyu.util.RecipeTypeMap
import androidx.compose.ui.res.stringResource
import com.example.shiyu.R
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeDetailScreen(
    recipeId: Long,
    onNavigateBack: () -> Unit,
    onOrderNow: (Long) -> Unit = {},
    onNavigateToEdit: (Long) -> Unit = {},
    onNavigateToAnalysis: (Long) -> Unit = {}
) {
    val viewModel: RecipeDetailViewModel = hiltViewModel()
    val details by viewModel.recipe
    val isLoading by viewModel.isLoading
    val isDiner by viewModel.isDiner
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var servings by remember { mutableIntStateOf(1) }

    LaunchedEffect(recipeId) {
        viewModel.loadRecipe(recipeId)
    }

    val deleted by viewModel.deleted
    LaunchedEffect(deleted) {
        if (deleted) onNavigateBack()
    }

    val context = LocalContext.current
    val shareRecipeName = details?.recipe?.name ?: ""
    val shareUrl = viewModel.serverUrl
    val shareText = stringResource(R.string.share_recipe, shareRecipeName, shareUrl, recipeId)

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.delete_recipe)) },
            text = { Text(stringResource(R.string.confirm_delete_recipe, details?.recipe?.name ?: "")) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    viewModel.deleteRecipe()
                }) {
                    Text(stringResource(R.string.delete), color = Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.cancel), color = TextSecondary)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.recipe_detail)) },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text(stringResource(R.string.back), color = TextPrimary)
                    }
                },
                actions = {
                    TextButton(onClick = {
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(sendIntent, null))
                    }) {
                        Text("📤", fontSize = 18.sp)
                    }
                    TextButton(onClick = { onNavigateToAnalysis(recipeId) }) {
                        Text("📊", fontSize = 18.sp)
                    }
                    if (!isDiner) {
                        TextButton(onClick = { onNavigateToEdit(recipeId) }) {
                            Text("✏️", fontSize = 18.sp)
                        }
                        TextButton(onClick = { showDeleteConfirm = true }) {
                            Text("🗑", fontSize = 18.sp)
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        val current = details
        when {
            isLoading -> LoadingIndicator(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
            current == null -> EmptyState(
                icon = "😢",
                title = stringResource(R.string.recipe_not_found),
                subtitle = stringResource(R.string.recipe_deleted),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
            else -> {
                val recipe = current.recipe
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .background(Background)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (recipe.image_path.isNotEmpty()) {
                        AsyncImage(
                            model = recipe.image_path,
                            contentDescription = recipe.name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = recipe.name,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = { viewModel.toggleFavorite() }) {
                                Text(
                                    text = if (recipe.is_favorite == 1) "❤️" else "🤍",
                                    fontSize = 24.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = RecipeTypeMap[recipe.type] ?: stringResource(R.string.type_other),
                            fontSize = 12.sp,
                            color = Primary,
                            modifier = Modifier
                                .background(PrimaryLight.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            InfoItem(stringResource(R.string.cooking_time), "${recipe.cooking_time}${stringResource(R.string.minutes_unit)}")
                            InfoItem(stringResource(R.string.difficulty_level), "${stringResource(R.string.difficulty)}${recipe.difficulty}")
                            InfoItem(stringResource(R.string.ingredient_count), "${current.materials.size}${stringResource(R.string.kinds_unit)}")
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (recipe.cooking_time > 0) {
                            CookingTimer(totalTimeMinutes = recipe.cooking_time)
                        }

                        // 点餐按钮（食客角色）
                        if (isDiner) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { onOrderNow(recipe.id) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                shape = RoundedCornerShape(24.dp)
                            ) {
                                Text(stringResource(R.string.order_now), fontSize = 16.sp, color = androidx.compose.ui.graphics.Color.White)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        if (recipe.description.isNotEmpty()) {
                            SectionTitle(stringResource(R.string.recipe_description))
                            Text(
                                text = recipe.description,
                                fontSize = 14.sp,
                                color = TextSecondary,
                                lineHeight = 20.sp
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                        }

                        RatingDisplay(stats = viewModel.commentStats.value)

                        // 份量选择
                        val baseServings = 1
                        val scaleFactor = servings.toDouble() / baseServings

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            SectionTitle(stringResource(R.string.serving_scale))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { if (servings > 1) servings-- },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Text("−", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (servings > 1) Primary else TextHint)
                                }
                                Text(
                                    text = "${servings}${stringResource(R.string.person_unit)}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                                IconButton(
                                    onClick = { if (servings < 10) servings++ },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Text("+", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = if (servings < 10) Primary else TextHint)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // 食材清单
                        SectionTitle(stringResource(R.string.ingredient_list))
                        Spacer(modifier = Modifier.height(8.dp))
                        if (current.materials.isEmpty()) {
                            EmptyHint(stringResource(R.string.no_ingredients))
                        } else {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Surface)
                            ) {
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    current.materials.forEachIndexed { index, material ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 16.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(Primary)
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = material.name,
                                                fontSize = 14.sp,
                                                color = TextPrimary,
                                                modifier = Modifier.weight(1f)
                                            )
                                            val amountText = listOf(material.amount, material.unit)
                                                .filter { it.isNotBlank() }
                                                .joinToString(" ")
                                            val scaledText = if (scaleFactor != 1.0 && material.amount.isNotBlank()) {
                                                val originalNum = material.amount.toDoubleOrNull()
                                                if (originalNum != null) {
                                                    val scaled = originalNum * scaleFactor
                                                    val scaledStr = if (scaled == scaled.toLong().toDouble()) {
                                                        scaled.toLong().toString()
                                                    } else {
                                                        String.format("%.1f", scaled)
                                                    }
                                                    "$amountText → $scaledStr${material.unit}"
                                                } else {
                                                    amountText
                                                }
                                            } else {
                                                amountText
                                            }
                                            Text(
                                                text = scaledText,
                                                fontSize = 14.sp,
                                                color = if (scaleFactor != 1.0 && material.amount.isNotBlank()) Primary else TextSecondary,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                        if (index != current.materials.lastIndex) {
                                            HorizontalDivider(
                                                modifier = Modifier.padding(start = 36.dp),
                                                color = DividerColor
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // 烹饪步骤
                        SectionTitle(stringResource(R.string.cooking_steps))
                        Spacer(modifier = Modifier.height(8.dp))
                        if (current.steps.isEmpty()) {
                            EmptyHint(stringResource(R.string.no_steps))
                        } else {
                            current.steps.sortedBy { it.step_number }.forEach { step ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(Primary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${step.step_number}",
                                            color = Surface,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = step.description,
                                            fontSize = 14.sp,
                                            color = TextPrimary,
                                            lineHeight = 21.sp
                                        )
                                        if (step.image_path.isNotEmpty()) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            AsyncImage(
                                                model = step.image_path,
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(160.dp)
                                                    .clip(RoundedCornerShape(10.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // 评论评分
                        CommentSection(
                            comments = viewModel.comments,
                            isLoading = viewModel.commentsLoading.value,
                            isLoggedIn = viewModel.isLoggedIn.value,
                            currentUserId = viewModel.currentUserId,
                            onPost = { content, rating ->
                                viewModel.postComment(content, rating)
                            },
                            onDeleteComment = { commentId ->
                                viewModel.deleteComment(commentId)
                            }
                        )

                        Spacer(modifier = Modifier.height(32.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CookingTimer(totalTimeMinutes: Int) {
    var totalSeconds by remember { mutableIntStateOf(totalTimeMinutes * 60) }
    var remainingSeconds by remember { mutableIntStateOf(totalTimeMinutes * 60) }
    var isRunning by remember { mutableStateOf(false) }
    val context = LocalContext.current

    LaunchedEffect(isRunning, remainingSeconds) {
        if (isRunning && remainingSeconds > 0) {
            delay(1000)
            remainingSeconds--
            if (remainingSeconds == 0) {
                isRunning = false
                try {
                    val vibrator = context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? Vibrator
                    vibrator?.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE))
                } catch (_: Exception) {}
                try {
                    val toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
                    toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK, 500)
                } catch (_: Exception) {}
            }
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.cooking_timer),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = String.format("%02d:%02d", remainingSeconds / 60, remainingSeconds % 60),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = if (remainingSeconds == 0 && !isRunning && totalSeconds > 0) Primary else TextPrimary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { isRunning = !isRunning },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRunning) Warning else Primary
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = if (isRunning) stringResource(R.string.pause) else stringResource(R.string.start),
                        fontSize = 13.sp,
                        color = Color.White
                    )
                }
                OutlinedButton(
                    onClick = {
                        isRunning = false
                        remainingSeconds = totalSeconds
                    },
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(stringResource(R.string.timer_reset), fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
fun RatingDisplay(stats: CommentStatsDto?) {
    if (stats == null) return

    val avgRating = stats.averageRating ?: 0.0
    val totalComments = stats.totalComments ?: 0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = String.format("%.1f", avgRating),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Accent
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Row {
                    Text(
                        text = buildString {
                            for (i in 1..5) {
                                append(if (i <= avgRating) "★" else "☆")
                            }
                        },
                        fontSize = 16.sp,
                        color = Accent
                    )
                }
                Text(
                    text = stringResource(R.string.total_reviews, totalComments),
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(width = 4.dp, height = 18.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Primary)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
    }
}

@Composable
private fun EmptyHint(text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceGray)
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            color = TextHint,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
fun InfoItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Primary
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextHint
        )
    }
}

@Composable
private fun CommentSection(
    comments: List<com.example.shiyu.api.CommentDto>,
    isLoading: Boolean,
    isLoggedIn: Boolean,
    currentUserId: Long = 0L,
    onPost: (String, Int) -> Unit,
    onDeleteComment: (Long) -> Unit = {}
) {
    var rating by remember { mutableIntStateOf(5) }
    var content by remember { mutableStateOf("") }
    var deleteTarget by remember { mutableStateOf<Long?>(null) }

    if (deleteTarget != null) {
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text(stringResource(R.string.delete_review)) },
            text = { Text(stringResource(R.string.confirm_delete_review)) },
            confirmButton = {
                TextButton(onClick = {
                    deleteTarget?.let { onDeleteComment(it) }
                    deleteTarget = null
                }) {
                    Text(stringResource(R.string.delete), color = Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text(stringResource(R.string.cancel), color = TextSecondary)
                }
            }
        )
    }

    SectionTitle(stringResource(R.string.reviews))
    Spacer(modifier = Modifier.height(12.dp))

    if (isLoggedIn) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Surface)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.rating_label), fontSize = 13.sp, color = TextSecondary)
                    (1..5).forEach { star ->
                        Text(
                            text = if (star <= rating) "★" else "☆",
                            fontSize = 24.sp,
                            color = if (star <= rating) Accent else TextHint,
                            modifier = Modifier.clickable { rating = star }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    placeholder = { Text(stringResource(R.string.write_review), fontSize = 13.sp, color = TextHint) },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Primary,
                        unfocusedBorderColor = BorderColor
                    )
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = {
                            if (content.isNotBlank()) {
                                onPost(content, rating)
                                content = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 22.dp, vertical = 6.dp)
                    ) {
                        Text(stringResource(R.string.submit_review), fontSize = 13.sp, color = androidx.compose.ui.graphics.Color.White)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(14.dp))
    } else {
        Text(
            text = stringResource(R.string.login_to_review),
            fontSize = 12.sp,
            color = TextHint,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
    }

    when {
        isLoading -> Text(stringResource(R.string.loading_reviews), fontSize = 13.sp, color = TextHint)
        comments.isEmpty() -> Text(stringResource(R.string.no_reviews), fontSize = 13.sp, color = TextHint)
        else -> comments.forEach { comment ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = comment.nickname?.takeIf { it.isNotBlank() }
                                ?: comment.username ?: stringResource(R.string.anonymous),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        if ((comment.rating ?: 0) > 0) {
                            Text(
                                text = "★".repeat(comment.rating ?: 0) + "☆".repeat(5 - (comment.rating ?: 0)),
                                fontSize = 12.sp,
                                color = Accent
                            )
                        }
                        if (comment.userId == currentUserId) {
                            Spacer(modifier = Modifier.width(4.dp))
                            TextButton(
                                onClick = { comment.id?.let { deleteTarget = it } },
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier.size(28.dp)
                            ) {
                                Text("🗑", fontSize = 14.sp)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = comment.content ?: "",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        lineHeight = 19.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = comment.createTime?.replace("T", " ")?.take(16) ?: "",
                        fontSize = 11.sp,
                        color = TextHint
                    )
                }
            }
        }
    }
}
