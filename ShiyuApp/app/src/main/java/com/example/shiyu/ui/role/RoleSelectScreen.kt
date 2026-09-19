package com.example.shiyu.ui.role

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shiyu.R
import com.example.shiyu.ui.theme.*
import com.example.shiyu.util.Constants
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleSelectScreen(
    onNavigateToChefHome: () -> Unit,
    onNavigateToDinerHome: () -> Unit
) {
    val viewModel: RoleSelectViewModel = hiltViewModel()
    val scope = rememberCoroutineScope()

    var selectedRole by remember { mutableStateOf("") }
    var spaceName by remember { mutableStateOf("") }
    var chefName by remember { mutableStateOf("") }
    var dinerName by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }

    val canSubmit = selectedRole.isNotEmpty() &&
            spaceName.isNotBlank() &&
            chefName.isNotBlank() &&
            dinerName.isNotBlank() &&
            !isSubmitting

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.select_your_role)) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.start_food_journey),
                fontSize = 14.sp,
                color = TextSecondary,
                modifier = Modifier.padding(bottom = 30.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                // 主厨卡片
                RoleCard(
                    title = stringResource(R.string.role_chef),
                    desc = stringResource(R.string.role_chef_desc),
                    color = ChefColor,
                    bgColor = ChefColorLight,
                    isSelected = selectedRole == Constants.ROLE_CHEF,
                    onClick = { selectedRole = Constants.ROLE_CHEF },
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(20.dp))

                // 食客卡片
                RoleCard(
                    title = stringResource(R.string.role_diner),
                    desc = stringResource(R.string.role_diner_desc),
                    color = DinerColor,
                    bgColor = DinerColorLight,
                    isSelected = selectedRole == Constants.ROLE_DINER,
                    onClick = { selectedRole = Constants.ROLE_DINER },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(30.dp))

            if (selectedRole.isNotEmpty()) {
                // 情侣空间名称
                FormField(
                    label = stringResource(R.string.couple_space_name),
                    placeholder = stringResource(R.string.couple_space_name_hint),
                    value = spaceName,
                    onValueChange = { spaceName = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 主厨昵称
                FormField(
                    label = stringResource(R.string.chef_nickname),
                    placeholder = stringResource(R.string.chef_nickname_hint),
                    value = chefName,
                    onValueChange = { chefName = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 食客昵称
                FormField(
                    label = stringResource(R.string.diner_nickname),
                    placeholder = stringResource(R.string.diner_nickname_hint),
                    value = dinerName,
                    onValueChange = { dinerName = it }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = {
                    isSubmitting = true
                    scope.launch {
                        val success = viewModel.saveConfig(
                            spaceName = spaceName,
                            chefName = chefName,
                            dinerName = dinerName,
                            role = selectedRole
                        )
                        if (success) {
                            if (selectedRole == Constants.ROLE_CHEF) {
                                onNavigateToChefHome()
                            } else {
                                onNavigateToDinerHome()
                            }
                        }
                        isSubmitting = false
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(bottom = 40.dp),
                enabled = canSubmit,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Primary,
                    disabledContainerColor = Primary.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (isSubmitting) stringResource(R.string.submitting) else stringResource(R.string.confirm_selection),
                    fontSize = 18.sp,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun RoleCard(
    title: String,
    desc: String,
    color: Color,
    bgColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(160.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = if (isSelected) BorderStroke(3.dp, color) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = desc,
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun FormField(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = TextHint) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Primary,
                unfocusedBorderColor = BorderColor
            )
        )
    }
}
