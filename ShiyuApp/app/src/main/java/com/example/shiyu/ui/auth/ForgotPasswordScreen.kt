package com.example.shiyu.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.shiyu.R
import com.example.shiyu.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit
) {
    val viewModel: ForgotPasswordViewModel = hiltViewModel()
    val step = viewModel.step.value

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.forgot_password_title)) },
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
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = if (step == 3) "✅" else "🔑", fontSize = 44.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = when (step) {
                    1 -> stringResource(R.string.forgot_step1_subtitle)
                    2 -> stringResource(R.string.forgot_step2_subtitle)
                    else -> stringResource(R.string.forgot_step3_subtitle)
                },
                fontSize = 14.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    // Step indicators
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("1", "2", "3").forEachIndexed { index, num ->
                            val isActive = step > index
                            val isCurrent = step == index + 1
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(
                                        color = if (isActive) Primary else if (isCurrent) PrimaryLight else SurfaceGray,
                                        shape = RoundedCornerShape(16.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = num,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isActive) Color.White else TextSecondary
                                )
                            }
                            if (index < 2) {
                                Box(
                                    modifier = Modifier
                                        .width(40.dp)
                                        .height(2.dp)
                                        .background(if (step > index + 1) Primary else SurfaceGray)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    when (step) {
                        1 -> {
                            Text(
                                text = stringResource(R.string.forgot_step1_label),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = viewModel.username.value,
                                onValueChange = { viewModel.updateUsername(it) },
                                label = { Text(stringResource(R.string.username_or_email_label)) },
                                placeholder = { Text(stringResource(R.string.forgot_password_username_hint), color = TextHint) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Primary,
                                    unfocusedBorderColor = BorderColor
                                )
                            )
                            Spacer(modifier = Modifier.height(22.dp))
                            Button(
                                onClick = { viewModel.sendCode() },
                                enabled = !viewModel.isLoading.value,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                shape = RoundedCornerShape(25.dp)
                            ) {
                                if (viewModel.isLoading.value) {
                                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                                } else {
                                    Text(stringResource(R.string.send_code_button), fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        2 -> {
                            Text(
                                text = stringResource(R.string.forgot_step2_label),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = viewModel.code.value,
                                onValueChange = { viewModel.updateCode(it) },
                                label = { Text(stringResource(R.string.verification_code_label)) },
                                placeholder = { Text(stringResource(R.string.verification_code_hint), color = TextHint) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Primary,
                                    unfocusedBorderColor = BorderColor
                                )
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            OutlinedTextField(
                                value = viewModel.newPassword.value,
                                onValueChange = { viewModel.updateNewPassword(it) },
                                label = { Text(stringResource(R.string.new_password_label)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Primary,
                                    unfocusedBorderColor = BorderColor
                                )
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                            OutlinedTextField(
                                value = viewModel.confirmPassword.value,
                                onValueChange = { viewModel.updateConfirmPassword(it) },
                                label = { Text(stringResource(R.string.confirm_new_password_label)) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Primary,
                                    unfocusedBorderColor = BorderColor
                                )
                            )
                            Spacer(modifier = Modifier.height(22.dp))
                            Button(
                                onClick = { viewModel.verifyCode() },
                                enabled = !viewModel.isLoading.value,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                shape = RoundedCornerShape(25.dp)
                            ) {
                                if (viewModel.isLoading.value) {
                                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                                } else {
                                    Text(stringResource(R.string.reset_password_button), fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = stringResource(R.string.forgot_resend_code),
                                fontSize = 13.sp,
                                color = Primary,
                                modifier = Modifier.clickable { viewModel.sendCode() }
                            )
                        }

                        3 -> {
                            Text(
                                text = stringResource(R.string.forgot_success_message),
                                fontSize = 15.sp,
                                color = Success
                            )
                            Spacer(modifier = Modifier.height(22.dp))
                            Button(
                                onClick = { onNavigateBack() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                                shape = RoundedCornerShape(25.dp)
                            ) {
                                Text(stringResource(R.string.back_to_login), fontSize = 16.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (viewModel.errorMessage.value.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = viewModel.errorMessage.value, fontSize = 13.sp, color = Error)
                    }
                    if (viewModel.message.value.isNotEmpty() && step != 3) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(text = viewModel.message.value, fontSize = 13.sp, color = Success)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
