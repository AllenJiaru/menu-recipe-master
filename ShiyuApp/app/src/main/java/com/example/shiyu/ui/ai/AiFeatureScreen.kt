package com.example.shiyu.ui.ai

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shiyu.R
import com.example.shiyu.api.ApiClient
import com.example.shiyu.api.AiResponseDto
import com.example.shiyu.ui.theme.Primary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiFeatureScreen(
    title: String,
    featureType: String,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var input by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    val placeholders = when (featureType) {
        "recommend" -> stringResource(R.string.ai_recommend_hint)
        "meal_plan" -> stringResource(R.string.ai_meal_plan_hint)
        "nutrition" -> stringResource(R.string.ai_nutrition_hint)
        "leftover" -> stringResource(R.string.ai_leftover_hint)
        else -> stringResource(R.string.ai_default_hint)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                placeholder = { Text(placeholders, fontSize = 13.sp) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 6,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = {
                    if (input.isBlank()) return@Button
                    isLoading = true
                    result = ""
                    scope.launch {
                        try {
                            val response: AiResponseDto? = when (featureType) {
                                "recommend" -> {
                                    val res = ApiClient.backendApi.aiRecommend()
                                    res.data
                                }
                                "meal_plan" -> {
                                    val res = ApiClient.backendApi.aiMealPlan(mapOf("preferences" to input))
                                    res.data
                                }
                                "nutrition" -> {
                                    val parts = input.split("，", ",", limit = 2)
                                    val name = parts.getOrElse(0) { input }
                                    val ingredients = parts.getOrElse(1) { "" }
                                    val res = ApiClient.backendApi.aiNutrition(
                                        mapOf("recipeName" to name, "ingredients" to ingredients.split("、", ",").map { it.trim() })
                                    )
                                    res.data
                                }
                                "leftover" -> {
                                    val res = ApiClient.backendApi.aiLeftover(mapOf("message" to input))
                                    res.data
                                }
                                else -> null
                            }
                            result = response?.content ?: context.getString(R.string.ai_no_result)
                        } catch (e: Exception) {
                            result = context.getString(R.string.ai_request_failed, e.message ?: "")
                        }
                        isLoading = false
                    }
                },
                enabled = !isLoading && input.isNotBlank(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                    Spacer(Modifier.width(8.dp))
                }
                Text(stringResource(R.string.start_analysis))
            }

            Spacer(Modifier.height(16.dp))

            if (result.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    MarkdownText(
                        text = result,
                        modifier = Modifier.padding(16.dp),
                        textColor = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp,
                        lineHeight = 22.sp
                    )
                }
            }
        }
    }
}
