package com.example.shiyu.ui.splash

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.shiyu.R
import com.example.shiyu.ui.theme.Primary
import com.example.shiyu.ui.theme.TextHint
import com.example.shiyu.ui.theme.TextSecondary
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun SplashScreen(
    onNavigateToLogin: () -> Unit,
    onNavigateToChefHome: () -> Unit,
    onNavigateToDinerHome: () -> Unit
) {
    val viewModel: SplashViewModel = hiltViewModel()
    val navigationEvent by viewModel.navigationEvent

    var progress by remember { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 1500),
        label = "progress"
    )

    LaunchedEffect(Unit) {
        progress = 1f
    }

    LaunchedEffect(navigationEvent) {
        when (navigationEvent) {
            is SplashViewModel.NavigationEvent.NavigateToLogin -> onNavigateToLogin()
            is SplashViewModel.NavigationEvent.NavigateToChefHome -> onNavigateToChefHome()
            is SplashViewModel.NavigationEvent.NavigateToDinerHome -> onNavigateToDinerHome()
            null -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.mipmap.ic_launcher),
                contentDescription = "App Icon",
                modifier = Modifier.size(120.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = stringResource(R.string.app_title),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Primary
            )

            Text(
                text = stringResource(R.string.app_subtitle),
                fontSize = 16.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .width(200.dp)
                    .height(4.dp),
                color = Primary,
                trackColor = Color(0xFFE0E0E0),
            )

            Text(
                text = "${(animatedProgress * 100).toInt()}%",
                fontSize = 12.sp,
                color = TextHint,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Text(
            text = stringResource(R.string.copyright),
            fontSize = 12.sp,
            color = TextHint,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 40.dp)
        )
    }
}
