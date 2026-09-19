package com.example.shiyu.ui.diner

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shiyu.ui.theme.*
import com.example.shiyu.util.Constants
import com.example.shiyu.util.RecipeTypeMap
import androidx.compose.ui.res.stringResource
import com.example.shiyu.R
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DinerOrderScreen(
    recipeId: Long,
    onNavigateBack: () -> Unit,
    onOrderSuccess: () -> Unit
) {
    val viewModel: DinerOrderViewModel = hiltViewModel()
    val recipe by viewModel.recipe
    val isLoading by viewModel.isLoading
    val orderSuccess by viewModel.orderSuccess
    val scope = rememberCoroutineScope()

    var remark by remember { mutableStateOf("") }

    LaunchedEffect(recipeId) {
        viewModel.loadRecipe(recipeId)
    }

    LaunchedEffect(orderSuccess) {
        if (orderSuccess) {
            onOrderSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.confirm_order)) },
                navigationIcon = {
                    TextButton(onClick = onNavigateBack) {
                        Text(stringResource(R.string.back), color = TextPrimary)
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            isLoading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
            recipe == null -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.recipe_not_exist), color = TextHint)
            }
            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(Background)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // 菜谱信息卡片
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = recipe!!.name,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row {
                            Text(
                                text = RecipeTypeMap[recipe!!.type] ?: stringResource(R.string.type_other),
                                fontSize = 12.sp,
                                color = Primary,
                                modifier = Modifier
                                    .background(PrimaryLight.copy(alpha = 0.3f))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "${recipe!!.cooking_time}${stringResource(R.string.minutes_unit)}",
                                fontSize = 12.sp,
                                color = TextHint
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "${stringResource(R.string.difficulty)}${recipe!!.difficulty}",
                                fontSize = 12.sp,
                                color = TextHint
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 备注输入
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.flavor_remark),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        OutlinedTextField(
                            value = remark,
                            onValueChange = { remark = it },
                            placeholder = { Text(stringResource(R.string.flavor_hint), color = TextHint) },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3,
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Primary,
                                unfocusedBorderColor = BorderColor
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // 提交按钮
                Button(
                    onClick = {
                        scope.launch {
                            viewModel.submitOrder(
                                recipeId = recipe!!.id,
                                recipeName = recipe!!.name,
                                recipeImage = recipe!!.image_path,
                                remark = remark
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(bottom = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DinerColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.submit_order), fontSize = 18.sp, color = Color.White)
                }
            }
        }
    }
}
